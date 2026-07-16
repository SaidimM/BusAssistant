package com.saidi.busassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saidi.busassistant.data.local.LineFrequencyResult
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.data.remote.dto.BusInfo
import com.saidi.busassistant.data.remote.dto.RealTimeData
import com.saidi.busassistant.data.repository.BusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 首页 ViewModel
 * 管理实时看板数据、智能排序、用户交互
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BusRepository
) : ViewModel() {

    // ========== State ==========

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    // 实时数据缓存: lineId -> RealTimeData
    private val _realTimeDataMap = MutableStateFlow<Map<Long, RealTimeDataDisplay>>(emptyMap())
    val realTimeDataMap: StateFlow<Map<Long, RealTimeDataDisplay>> = _realTimeDataMap.asStateFlow()

    // 智能排序结果
    private val _smartOrder = MutableStateFlow<List<Long>>(emptyList())

    private var refreshJob: Job? = null
    private var autoRefreshJob: Job? = null

    // ========== Init ==========

    init {
        loadLines()
        startAutoRefresh()
    }

    // ========== 线路管理 ==========

    private fun loadLines() {
        viewModelScope.launch {
            repository.getAllLines()
                .collect { lines ->
                    val sortedLines = sortLinesIntelligently(lines)
                    _uiState.update { it.copy(lines = sortedLines) }
                    // 加载实时数据
                    if (lines.isNotEmpty()) {
                        refreshRealTimeData(lines)
                    }
                }
        }
    }

    fun addLine(line: BusLineEntity) {
        viewModelScope.launch {
            val order = (repository.getLineCount())
            val lineWithOrder = line.copy(displayOrder = order)
            repository.addLine(lineWithOrder)
        }
    }

    fun deleteLine(line: BusLineEntity) {
        viewModelScope.launch {
            repository.deleteLine(line)
            // 清除该线路的实时数据
            _realTimeDataMap.update { it - line.id }
        }
    }

    fun updateLineLabel(lineId: Long, label: String?) {
        viewModelScope.launch {
            repository.updateLineLabel(lineId, label)
        }
    }

    // ========== 实时数据刷新 ==========

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _isRefreshing.value = true
            val lines = _uiState.value.lines
            if (lines.isNotEmpty()) {
                refreshRealTimeData(lines)
            }
            _isRefreshing.value = false
        }
    }

    private suspend fun refreshRealTimeData(lines: List<BusLineEntity>) {
        lines.forEach { line ->
            val result = repository.getRealTimeData(
                lineId = line.lineNumber,
                direction = line.direction,
                boardingStationIndex = line.boardingStationIndex
            )

            result.onSuccess { data ->
                val display = RealTimeDataDisplay(
                    lineId = line.id,
                    lineNumber = line.lineNumber,
                    buses = data.buses ?: emptyList(),
                    closestBus = findClosestBus(data.buses, line.boardingStationIndex),
                    isCached = false
                )
                _realTimeDataMap.update { it + (line.id to display) }
            }.onFailure { error ->
                // 错误时保留旧数据
            }
        }
    }

    private fun findClosestBus(buses: List<BusInfo>?, boardingIndex: Int): ClosestBusInfo? {
        if (buses.isNullOrEmpty()) return null

        // 找到最接近用户站点的车辆
        val targetBus = buses
            .filter { it.stationIndex <= boardingIndex + 3 } // 车辆还没走远
            .maxByOrNull { it.stationIndex } ?: return null

        val stationsAway = (boardingIndex - targetBus.stationIndex).coerceAtLeast(0)
        val minutesAway = targetBus.arrivalTimeEstimate?.div(60) ?: (stationsAway * 3)

        return ClosestBusInfo(
            stationsAway = stationsAway,
            minutesAway = minutesAway,
            isArriving = targetBus.isArriving || minutesAway <= 2,
            totalStations = boardingIndex + 5 // 估算总站点数
        )
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(30000) // 30秒自动刷新
                val lines = _uiState.value.lines
                if (lines.isNotEmpty()) {
                    refreshRealTimeData(lines)
                }
            }
        }
    }

    // ========== 智能排序 ==========

    private suspend fun sortLinesIntelligently(lines: List<BusLineEntity>): List<BusLineEntity> {
        if (lines.size <= 1) return lines

        // 获取当前时间上下文
        val now = Calendar.getInstance()
        val weekday = now.get(Calendar.DAY_OF_WEEK).let {
            when (it) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
        }
        val hour = now.get(Calendar.HOUR_OF_DAY)

        // TODO: P1阶段接入真实地理位置，默认 "other"
        val locationZone = "other"

        // 查询习惯数据
        val topLines = repository.getTopLinesByContext(weekday, hour, locationZone, limit = 3)

        return if (topLines.isNotEmpty()) {
            // 按习惯频率排序
            val topLineIds = topLines.map { it.lineId }
            val (matched, unmatched) = lines.partition { it.id in topLineIds }
            val sortedMatched = matched.sortedBy { line ->
                topLineIds.indexOf(line.id)
            }
            sortedMatched + unmatched.sortedBy { it.displayOrder }
        } else {
            // 无习惯数据，按 display_order
            lines.sortedBy { it.displayOrder }
        }
    }

    // ========== 行为记录 ==========

    fun recordLineViewed(line: BusLineEntity) {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val weekday = now.get(Calendar.DAY_OF_WEEK).let {
                when (it) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 1
                }
            }
            val hour = now.get(Calendar.HOUR_OF_DAY)
            val locationZone = "other" // TODO: P1阶段接入真实地理位置

            repository.logBehavior(weekday, hour, locationZone, line.id, line.lineNumber)
        }
    }

    // ========== 设置 ==========

    fun clearAllLearningData() {
        viewModelScope.launch {
            repository.clearAllBehaviorLogs()
            _smartOrder.value = emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        autoRefreshJob?.cancel()
    }
}

// ========== UI State ==========

data class HomeUiState(
    val lines: List<BusLineEntity> = emptyList(),
    val isEmpty: Boolean = true
)

/**
 * 实时数据展示模型
 */
data class RealTimeDataDisplay(
    val lineId: Long,
    val lineNumber: String,
    val buses: List<BusInfo>,
    val closestBus: ClosestBusInfo?,
    val isCached: Boolean = false
)

/**
 * 最近的车辆信息
 */
data class ClosestBusInfo(
    val stationsAway: Int,    // 还有几站到用户站点
    val minutesAway: Int,     // 预计几分钟到达
    val isArriving: Boolean,  // 是否即将到站
    val totalStations: Int    // 总站点数（用于进度条）
) {
    val progress: Float
        get() = if (totalStations > 0) {
            (totalStations - stationsAway).toFloat() / totalStations
        } else 0f
}
