package com.saidi.busassistant.data.repository

import com.saidi.busassistant.data.local.BusLineDao
import com.saidi.busassistant.data.local.BehaviorLogDao
import com.saidi.busassistant.data.local.LineFrequencyResult
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.data.local.entity.BehaviorLogEntity
import com.saidi.busassistant.data.remote.BeijingBusApi
import com.saidi.busassistant.data.remote.dto.BusInfo
import com.saidi.busassistant.data.remote.dto.BusRealTimeResponse
import com.saidi.busassistant.data.remote.dto.LineSearchResult
import com.saidi.busassistant.data.remote.dto.RealTimeData
import com.saidi.busassistant.data.remote.dto.StationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 公交数据 Repository
 * 统一处理本地数据库和远程 API 的数据流
 */
@Singleton
class BusRepository @Inject constructor(
    private val busLineDao: BusLineDao,
    private val behaviorLogDao: BehaviorLogDao,
    private val busApi: BeijingBusApi
) {
    // ========== 本地数据操作 ==========

    fun getAllLines(): Flow<List<BusLineEntity>> = busLineDao.getAllLines()

    suspend fun addLine(line: BusLineEntity): Long = busLineDao.insertLine(line)

    suspend fun deleteLine(line: BusLineEntity) = busLineDao.deleteLine(line)

    suspend fun updateLineLabel(lineId: Long, label: String?) =
        busLineDao.updateLabel(lineId, label)

    suspend fun updateLineOrder(lineId: Long, order: Int) =
        busLineDao.updateDisplayOrder(lineId, order)

    suspend fun getLineCount(): Int = busLineDao.getLineCount()

    // ========== 行为日志 ==========

    suspend fun logBehavior(
        weekday: Int,
        hour: Int,
        locationZone: String,
        lineId: Long,
        lineNumber: String
    ) {
        // 凌晨0-6点不记录
        if (hour in 0..5) return

        val log = BehaviorLogEntity(
            weekday = weekday,
            hour = hour,
            locationZone = locationZone,
            viewedBusLineId = lineId,
            viewedBusLineNumber = lineNumber
        )
        behaviorLogDao.insertLog(log)
    }

    suspend fun getTopLinesByContext(
        weekday: Int,
        hour: Int,
        locationZone: String,
        limit: Int = 3
    ): List<LineFrequencyResult> {
        return behaviorLogDao.getTopLinesByContext(
            weekday = weekday,
            hourStart = (hour - 1).coerceAtLeast(0),
            hourEnd = (hour + 1).coerceAtMost(23),
            locationZone = locationZone,
            limit = limit
        )
    }

    suspend fun clearAllBehaviorLogs() = behaviorLogDao.clearAllLogs()

    suspend fun getBehaviorLogCount(): Int = behaviorLogDao.getLogCount()

    // ========== 远程 API + 缓存 ==========

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    /**
     * 获取实时公交数据（带缓存）
     * MVP阶段使用 Mock 数据，真实 API 需替换 BASE_URL 和接口
     */
    suspend fun getRealTimeData(
        lineId: String,
        direction: String,
        boardingStationIndex: Int
    ): Result<RealTimeData> = withContext(Dispatchers.IO) {
        try {
            // 检查缓存
            val cacheKey = "$lineId-$direction"
            val cached = cache[cacheKey]
            if (cached != null && cached.isValid()) {
                return@withContext Result.success(cached.data)
            }

            // 请求间隔限制
            val lastRequest = lastRequestTime[cacheKey] ?: 0L
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequest
            if (elapsed < BeijingBusApi.MIN_REQUEST_INTERVAL) {
                delay(BeijingBusApi.MIN_REQUEST_INTERVAL - elapsed)
            }

            // ===== MVP: 使用 Mock 数据 =====
            // 真实 API 调用（取消注释即可）：
            // val response = busApi.getRealTimeData(lineId, direction)
            // if (response.isSuccessful && response.body()?.status == 200) {
            //     val data = response.body()?.data
            //     if (data != null) {
            //         cache[cacheKey] = CacheEntry(data)
            //         lastRequestTime[cacheKey] = System.currentTimeMillis()
            //         return@withContext Result.success(data)
            //     }
            // }

            // Mock 数据生成
            val mockData = generateMockRealTimeData(lineId, direction, boardingStationIndex)
            cache[cacheKey] = CacheEntry(mockData)
            lastRequestTime[cacheKey] = System.currentTimeMillis()

            Result.success(mockData)
        } catch (e: Exception) {
            // 有缓存返回缓存，否则返回错误
            val cacheKey = "$lineId-$direction"
            val cached = cache[cacheKey]
            if (cached != null) {
                Result.success(cached.data)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * 搜索线路（MVP使用 Mock）
     */
    suspend fun searchLines(keyword: String): Result<List<LineSearchResult>> =
        withContext(Dispatchers.IO) {
            try {
                // 真实 API：
                // val response = busApi.searchLines(keyword)
                // Result.success(response.body()?.data ?: emptyList())

                // Mock 搜索结果
                Result.success(generateMockSearchResults(keyword))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ========== Mock 数据生成 ==========

    private fun generateMockRealTimeData(
        lineId: String,
        direction: String,
        boardingStationIndex: Int
    ): RealTimeData {
        val random = Random(System.currentTimeMillis())

        // 生成 1-3 辆车的 Mock 数据
        val busCount = random.nextInt(1, 4)
        val buses = (1..busCount).map { index ->
            // 车辆位置：可能在用户站点之前或之后
            val stationOffset = random.nextInt(-5, 8)
            val currentStation = (boardingStationIndex + stationOffset).coerceAtLeast(0)

            // 计算预计到站时间
            val minutesAway = when {
                stationOffset < 0 -> random.nextInt(1, 5)   // 快到站了
                stationOffset == 0 -> random.nextInt(0, 2)   // 即将到站
                else -> stationOffset * 3 + random.nextInt(0, 3) // 还有几站
            }.coerceAtLeast(0)

            BusInfo(
                busId = "bus_${lineId}_$index",
                latitude = 39.9 + random.nextDouble(-0.1, 0.1),
                longitude = 116.4 + random.nextDouble(-0.1, 0.1),
                stationIndex = currentStation,
                nextStationIndex = currentStation + 1,
                distanceToNext = random.nextInt(100, 2000),
                arrivalTimeEstimate = minutesAway * 60,
                isArriving = minutesAway <= 2
            )
        }.sortedBy { it.stationIndex }

        return RealTimeData(
            lineInfo = com.saidi.busassistant.data.remote.dto.LineInfo(
                id = lineId,
                name = "${lineId}路",
                direction = direction
            ),
            buses = buses,
            stations = emptyList() // Mock 中不返回站点列表
        )
    }

    private fun generateMockSearchResults(keyword: String): List<LineSearchResult> {
        val allLines = listOf(
            LineSearchResult(
                lineId = "375",
                lineName = "375路",
                direction = "上行",
                startStation = "西直门",
                endStation = "中关村",
                stations = listOf(
                    StationResult(0, "西直门"),
                    StationResult(1, "交大东路"),
                    StationResult(2, "皂君庙"),
                    StationResult(3, "四通桥东"),
                    StationResult(4, "中关村一街"),
                    StationResult(5, "中关村"),
                    StationResult(6, "中关村西"),
                    StationResult(7, "海淀桥北"),
                    StationResult(8, "北京大学西门"),
                    StationResult(9, "颐和园路东口")
                )
            ),
            LineSearchResult(
                lineId = "375",
                lineName = "375路",
                direction = "下行",
                startStation = "中关村",
                endStation = "西直门",
                stations = listOf(
                    StationResult(0, "中关村"),
                    StationResult(1, "中关村一街"),
                    StationResult(2, "四通桥东"),
                    StationResult(3, "皂君庙"),
                    StationResult(4, "交大东路"),
                    StationResult(5, "西直门")
                )
            ),
            LineSearchResult(
                lineId = "601",
                lineName = "601路",
                direction = "上行",
                startStation = "颐和园北宫门",
                endStation = "和平东桥",
                stations = listOf(
                    StationResult(0, "颐和园北宫门"),
                    StationResult(1, "地铁北宫门站"),
                    StationResult(2, "青龙桥"),
                    StationResult(3, "军事科学院"),
                    StationResult(4, "厢红旗"),
                    StationResult(5, "林业科学研究院"),
                    StationResult(6, "娘娘府"),
                    StationResult(7, "丰户营"),
                    StationResult(8, "三一八烈士墓"),
                    StationResult(9, "娘娘府北站")
                )
            ),
            LineSearchResult(
                lineId = "601",
                lineName = "601路",
                direction = "下行",
                startStation = "和平东桥",
                endStation = "颐和园北宫门",
                stations = emptyList()
            ),
            LineSearchResult(
                lineId = "特8",
                lineName = "特8路",
                direction = "外环",
                startStation = "航天桥西",
                endStation = "航天桥西",
                stations = listOf(
                    StationResult(0, "航天桥西"),
                    StationResult(1, "花园桥南"),
                    StationResult(2, "紫竹桥南"),
                    StationResult(3, "万寿寺"),
                    StationResult(4, "为公桥"),
                    StationResult(5, "苏州桥南"),
                    StationResult(6, "三义庙"),
                    StationResult(7, "四通桥东"),
                    StationResult(8, "大钟寺"),
                    StationResult(9, "蓟门桥西")
                )
            ),
            LineSearchResult(
                lineId = "快速公交1线",
                lineName = "快速公交1线",
                direction = "上行",
                startStation = "德茂庄",
                endStation = "前门",
                stations = emptyList()
            )
        )

        return allLines.filter {
            it.lineId.contains(keyword) || it.lineName.contains(keyword)
        }
    }

    // ========== 缓存管理 ==========

    private data class CacheEntry(
        val data: RealTimeData,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isValid(): Boolean =
            System.currentTimeMillis() - timestamp < BeijingBusApi.CACHE_VALID_DURATION
    }

    companion object {
        private val lastRequestTime = ConcurrentHashMap<String, Long>()
    }
}
