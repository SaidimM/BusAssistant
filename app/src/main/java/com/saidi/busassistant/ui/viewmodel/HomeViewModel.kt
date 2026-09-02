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
 * 通用首页 ViewModel
 * 管理实时看板数据、自学习行程推测、多线走廊极速聚合
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

    // 通勤走廊状态
    private val _corridorState = MutableStateFlow(CommuteCorridorUiState())
    val corridorState: StateFlow<CommuteCorridorUiState> = _corridorState.asStateFlow()

    // 用户手动选择的走廊 ID（为空时依据历史习惯自适应预估）
    private val _selectedCorridorId = MutableStateFlow<Long?>(null)

    private var refreshJob: Job? = null
    private var autoRefreshJob: Job? = null

    // ========== Init ==========

    init {
        loadCorridorsAndLines()
        startAutoRefresh()
    }

    // ========== 线路与自适应走廊管理 ==========

    private fun loadCorridorsAndLines() {
        viewModelScope.launch {
            repository.getAllLines().collect { lines ->
                // 自动同步基于同站点的通用通勤走廊
                repository.syncCorridorsFromLines(lines)

                val sortedLines = sortLinesIntelligently(lines)
                _uiState.update { it.copy(lines = sortedLines, isEmpty = sortedLines.isEmpty()) }
                if (lines.isNotEmpty()) {
                    refreshRealTimeData(lines)
                }
            }
        }

        viewModelScope.launch {
            combine(
                repository.getAllCorridors(),
                _realTimeDataMap,
                _selectedCorridorId
            ) { corridors, dataMap, manualSelectedId ->
                computeCorridorUiState(corridors, dataMap, manualSelectedId)
            }.collect { newState ->
                _corridorState.value = newState
            }
        }
    }

    /**
     * 基于时间窗口与行为日志的通用行程预估：
     * 自动推断当前时间用户最可能使用的走廊
     */
    private suspend fun inferBestCorridor(corridors: List<CommuteCorridorEntity>): CommuteCorridorEntity? {
        if (corridors.isEmpty()) return null
        if (corridors.size == 1) return corridors.first()

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

        // 查询当前时段最常查看的线路
        val topLines = repository.getTopLinesByContext(weekday, hour, "other", limit = 5)
        if (topLines.isNotEmpty()) {
            val topNumbers = topLines.map { it.lineNumber }.toSet()
            // 匹配包含该高频线路的走廊
            val matchedCorridor = corridors.maxByOrNull { c ->
                val lineList = c.lineNumbers.split(",").map { it.trim() }
                lineList.count { it in topNumbers }
            }
            if (matchedCorridor != null) return matchedCorridor
        }

        // 默认按 displayOrder 或列表首项
        return corridors.firstOrNull()
    }

    private suspend fun computeCorridorUiState(
        corridors: List<CommuteCorridorEntity>,
        dataMap: Map<Long, RealTimeDataDisplay>,
        manualSelectedId: Long?
    ): CommuteCorridorUiState {
        if (corridors.isEmpty()) {
            return CommuteCorridorUiState(isEmpty = true, availableCorridors = emptyList())
        }

        val activeCorridor = if (manualSelectedId != null) {
            corridors.find { it.id == manualSelectedId } ?: corridors.first()
        } else {
            inferBestCorridor(corridors) ?: corridors.first()
        }

        val candidateNumbers = activeCorridor.lineNumbers.split(",").map { it.trim() }.toSet()
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

        val estimatedArrivalText = earliestMins?.let { waitMins ->
            val totalTransitAndWalk = waitMins + 6 + activeCorridor.walkingMinutesAfter
            val calendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, totalTransitAndWalk)
            }
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = timeFormat.format(calendar.time)
            "预计 $formattedTime 抵达目的地 (含到达后步行${activeCorridor.walkingMinutesAfter}分钟)"
        }

        return CommuteCorridorUiState(
            corridor = activeCorridor,
            isAutoInferred = (manualSelectedId == null),
            candidateLines = rankedCandidates,
            recommendedLineNumber = recommendedLine,
            earliestArrivalMinutes = earliestMins,
            walkingMinutesAfter = activeCorridor.walkingMinutesAfter,
            estimatedOfficeArrivalText = estimatedArrivalText,
            availableCorridors = corridors,
            isEmpty = false
        )
    }

    fun selectCorridor(corridorId: Long) {
        _selectedCorridorId.value = corridorId
    }

    fun switchToNextCorridor() {
        val corridors = _corridorState.value.availableCorridors
        if (corridors.size <= 1) return
        val currentId = _corridorState.value.corridor?.id ?: return
        val currentIndex = corridors.indexOfFirst { it.id == currentId }
        val nextIndex = (currentIndex + 1) % corridors.size
        _selectedCorridorId.value = corridors[nextIndex].id
    }

    fun addLine(line: BusLineEntity) {
        viewModelScope.launch {
            val order = repository.getLineCount()
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
                delay(20000) // 20秒自动刷新
                val lines = _uiState.value.lines
                if (lines.isNotEmpty()) {
                    refreshRealTimeData(lines)
                }
            }
        }
    }

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
            _selectedCorridorId.value = null
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
    val isAutoInferred: Boolean = true,
    val candidateLines: List<CorridorCandidateLine> = emptyList(),
    val recommendedLineNumber: String? = null,
    val earliestArrivalMinutes: Int? = null,
    val walkingMinutesAfter: Int = 10,
    val estimatedOfficeArrivalText: String? = null,
    val availableCorridors: List<CommuteCorridorEntity> = emptyList(),
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
