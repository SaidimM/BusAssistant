package com.saidi.busassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saidi.busassistant.data.local.LineFrequencyResult
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.data.local.entity.CommuteCorridorEntity
import com.saidi.busassistant.data.remote.dto.BusInfo
import com.saidi.busassistant.data.remote.dto.RealTimeData
import com.saidi.busassistant.data.repository.BusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * 首页 ViewModel
 * 管理实时看板数据、智能行程预估、通勤走廊多线聚合
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

    // 智能通勤走廊状态
    private val _corridorState = MutableStateFlow(CommuteCorridorUiState())
    val corridorState: StateFlow<CommuteCorridorUiState> = _corridorState.asStateFlow()

    // 用户手动选择的方向（为空时使用算法自动推测）
    private val _manualDirectionOverride = MutableStateFlow<String?>(null)

    // 智能排序结果
    private val _smartOrder = MutableStateFlow<List<Long>>(emptyList())

    private var refreshJob: Job? = null
    private var autoRefreshJob: Job? = null

    // ========== Init ==========

    init {
        viewModelScope.launch {
            // 预置北京康家沟-四惠东通勤线路（若初次运行为空）
            repository.seedBeijingCommuteData()
            loadCorridorsAndLines()
            startAutoRefresh()
        }
    }

    // ========== 线路与通勤走廊管理 ==========

    private fun loadCorridorsAndLines() {
        // 监听线路
        viewModelScope.launch {
            repository.getAllLines().collect { lines ->
                val sortedLines = sortLinesIntelligently(lines)
                _uiState.update { it.copy(lines = sortedLines, isEmpty = sortedLines.isEmpty()) }
                if (lines.isNotEmpty()) {
                    refreshRealTimeData(lines)
                }
            }
        }

        // 监听走廊
        viewModelScope.launch {
            combine(
                repository.getAllCorridors(),
                _realTimeDataMap,
                _manualDirectionOverride
            ) { corridors, dataMap, manualDirection ->
                updateCorridorUiState(corridors, dataMap, manualDirection)
            }.collect { newState ->
                _corridorState.value = newState
            }
        }
    }

    private fun inferCurrentDirection(): String {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        // 6:00 - 12:00 默认为上班通勤 (康家沟 ➔ 四惠东)
        // 16:30 - 22:30 默认为下班回家 (四惠东 ➔ 康家沟)
        return when {
            totalMinutes in (6 * 60)..(12 * 60) -> "WORK"
            totalMinutes in (16 * 60 + 30)..(22 * 60 + 30) -> "HOME"
            else -> "WORK"
        }
    }

    private fun updateCorridorUiState(
        corridors: List<CommuteCorridorEntity>,
        dataMap: Map<Long, RealTimeDataDisplay>,
        manualDirection: String?
    ): CommuteCorridorUiState {
        if (corridors.isEmpty()) {
            return CommuteCorridorUiState(isEmpty = true)
        }

        val activeDirection = manualDirection ?: inferCurrentDirection()
        val currentCorridor = corridors.find { it.directionType == activeDirection }
            ?: corridors.first()

        val candidateNumbers = currentCorridor.lineNumbers.split(",").map { it.trim() }
        val lines = _uiState.value.lines.filter { it.lineNumber in candidateNumbers }

        val candidateCards = lines.map { line ->
            val display = dataMap[line.id]
            val closest = display?.closestBus
            val arrivalMins = closest?.minutesAway ?: 99
            val stops = closest?.stationsAway ?: 99
            val isArriving = closest?.isArriving ?: false

            CorridorCandidateLine(
                line = line,
                realTimeData = display,
                stopsAway = stops,
                arrivalMinutes = arrivalMins,
                isArriving = isArriving,
                isEarliest = false
            )
        }.sortedBy { it.arrivalMinutes }

        // 标记最快到站线路
        val rankedCandidates = if (candidateCards.isNotEmpty() && candidateCards.first().arrivalMinutes < 90) {
            candidateCards.mapIndexed { index, item ->
                if (index == 0) item.copy(isEarliest = true) else item
            }
        } else {
            candidateCards
        }

        val fastest = rankedCandidates.firstOrNull()
        val recommendedLine = fastest?.line?.lineNumber
        val earliestMins = fastest?.arrivalMinutes?.takeIf { it < 90 }

        // 估算到达工位时间：当前时间 + 到站等待 + 公交行驶(约6分) + 下车步行
        val estimatedArrivalText = earliestMins?.let { waitMins ->
            val totalTransitAndWalk = waitMins + 6 + currentCorridor.walkingMinutesAfter
            val calendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, totalTransitAndWalk)
            }
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = timeFormat.format(calendar.time)
            if (activeDirection == "WORK") {
                "预计 $formattedTime 抵达公司工位 (含四惠东步行${currentCorridor.walkingMinutesAfter}分钟)"
            } else {
                "预计 $formattedTime 抵达公寓 (含步行${currentCorridor.walkingMinutesAfter}分钟)"
            }
        }

        return CommuteCorridorUiState(
            corridor = currentCorridor,
            inferredDirection = activeDirection,
            isAutoInferred = (manualDirection == null),
            candidateLines = rankedCandidates,
            recommendedLineNumber = recommendedLine,
            earliestArrivalMinutes = earliestMins,
            walkingMinutesAfter = currentCorridor.walkingMinutesAfter,
            estimatedOfficeArrivalText = estimatedArrivalText,
            isEmpty = false
        )
    }

    fun toggleCorridorDirection() {
        val currentDirection = _corridorState.value.inferredDirection
        val newDirection = if (currentDirection == "WORK") "HOME" else "WORK"
        _manualDirectionOverride.value = newDirection
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
            }
        }
    }

    private fun findClosestBus(buses: List<BusInfo>?, boardingIndex: Int): ClosestBusInfo? {
        if (buses.isNullOrEmpty()) return null

        val targetBus = buses
            .filter { it.stationIndex <= boardingIndex + 2 }
            .maxByOrNull { it.stationIndex } ?: return null

        val stationsAway = (boardingIndex - targetBus.stationIndex).coerceAtLeast(0)
        val minutesAway = targetBus.arrivalTimeEstimate?.div(60) ?: (stationsAway * 3)

        return ClosestBusInfo(
            stationsAway = stationsAway,
            minutesAway = minutesAway,
            isArriving = targetBus.isArriving || minutesAway <= 2,
            totalStations = boardingIndex + 5
        )
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(20000) // 20秒自动刷新实时公交状态
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
        val locationZone = "other"

        val topLines = repository.getTopLinesByContext(weekday, hour, locationZone, limit = 3)

        return if (topLines.isNotEmpty()) {
            val topLineIds = topLines.map { it.lineId }
            val (matched, unmatched) = lines.partition { it.id in topLineIds }
            val sortedMatched = matched.sortedBy { line ->
                topLineIds.indexOf(line.id)
            }
            sortedMatched + unmatched.sortedBy { it.displayOrder }
        } else {
            lines.sortedBy { it.displayOrder }
        }
    }

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
            val locationZone = "other"

            repository.logBehavior(weekday, hour, locationZone, line.id, line.lineNumber)
        }
    }

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

data class CorridorCandidateLine(
    val line: BusLineEntity,
    val realTimeData: RealTimeDataDisplay?,
    val stopsAway: Int,
    val arrivalMinutes: Int,
    val isArriving: Boolean,
    val isEarliest: Boolean
)

data class CommuteCorridorUiState(
    val corridor: CommuteCorridorEntity? = null,
    val inferredDirection: String = "WORK", // "WORK" 或 "HOME"
    val isAutoInferred: Boolean = true,
    val candidateLines: List<CorridorCandidateLine> = emptyList(),
    val recommendedLineNumber: String? = null,
    val earliestArrivalMinutes: Int? = null,
    val walkingMinutesAfter: Int = 10,
    val estimatedOfficeArrivalText: String? = null,
    val isEmpty: Boolean = true
)

data class RealTimeDataDisplay(
    val lineId: Long,
    val lineNumber: String,
    val buses: List<BusInfo>,
    val closestBus: ClosestBusInfo?,
    val isCached: Boolean = false
)

data class ClosestBusInfo(
    val stationsAway: Int,
    val minutesAway: Int,
    val isArriving: Boolean,
    val totalStations: Int
) {
    val progress: Float
        get() = if (totalStations > 0) {
            (totalStations - stationsAway).toFloat() / totalStations
        } else 0f
}
