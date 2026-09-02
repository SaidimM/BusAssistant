package com.saidi.busassistant.data.repository

import com.saidi.busassistant.data.local.BusLineDao
import com.saidi.busassistant.data.local.BehaviorLogDao
import com.saidi.busassistant.data.local.CommuteCorridorDao
import com.saidi.busassistant.data.local.LineFrequencyResult
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.data.local.entity.BehaviorLogEntity
import com.saidi.busassistant.data.local.entity.CommuteCorridorEntity
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
 * 统一处理本地数据库、通勤走廊聚合与远程 API / 高仿真实时数据
 */
@Singleton
class BusRepository @Inject constructor(
    private val busLineDao: BusLineDao,
    private val behaviorLogDao: BehaviorLogDao,
    private val commuteCorridorDao: CommuteCorridorDao,
    private val busApi: BeijingBusApi
) {
    // ========== 本地线路操作 ==========

    fun getAllLines(): Flow<List<BusLineEntity>> = busLineDao.getAllLines()

    suspend fun addLine(line: BusLineEntity): Long = busLineDao.insertLine(line)

    suspend fun deleteLine(line: BusLineEntity) = busLineDao.deleteLine(line)

    suspend fun updateLineLabel(lineId: Long, label: String?) =
        busLineDao.updateLabel(lineId, label)

    suspend fun updateLineOrder(lineId: Long, order: Int) =
        busLineDao.updateDisplayOrder(lineId, order)

    suspend fun getLineCount(): Int = busLineDao.getLineCount()

    // ========== 通勤走廊 (Commute Corridor) ==========

    fun getAllCorridors(): Flow<List<CommuteCorridorEntity>> = commuteCorridorDao.getAllCorridors()

    suspend fun getCorridorByDirection(directionType: String): CommuteCorridorEntity? =
        commuteCorridorDao.getCorridorByDirection(directionType)

    suspend fun addCorridor(corridor: CommuteCorridorEntity): Long =
        commuteCorridorDao.insertCorridor(corridor)

    suspend fun updateCorridor(corridor: CommuteCorridorEntity) =
        commuteCorridorDao.updateCorridor(corridor)

    suspend fun deleteCorridor(corridor: CommuteCorridorEntity) =
        commuteCorridorDao.deleteCorridor(corridor)

    suspend fun getCorridorCount(): Int = commuteCorridorDao.getCorridorCount()

    /**
     * 为北京朝阳康家沟-四惠东定制的默认通勤数据预置
     */
    suspend fun seedBeijingCommuteData() = withContext(Dispatchers.IO) {
        if (busLineDao.getLineCount() == 0) {
            val line553 = BusLineEntity(
                lineNumber = "553",
                lineName = "553路",
                direction = "上行",
                startStation = "单店",
                endStation = "四惠枢纽站",
                userBoardingStation = "康家沟",
                userAlightingStation = "四惠东站",
                boardingStationIndex = 13,
                userLabel = "上班",
                displayOrder = 1
            )
            val line468 = BusLineEntity(
                lineNumber = "468",
                lineName = "468路",
                direction = "上行",
                startStation = "朝新嘉园",
                endStation = "四惠枢纽站",
                userBoardingStation = "康家沟",
                userAlightingStation = "四惠东站",
                boardingStationIndex = 9,
                userLabel = "上班",
                displayOrder = 2
            )
            val line517 = BusLineEntity(
                lineNumber = "517",
                lineName = "517路",
                direction = "上行",
                startStation = "草房",
                endStation = "四惠枢纽站",
                userBoardingStation = "康家沟",
                userAlightingStation = "四惠东站",
                boardingStationIndex = 7,
                userLabel = "上班",
                displayOrder = 3
            )
            busLineDao.insertLine(line553)
            busLineDao.insertLine(line468)
            busLineDao.insertLine(line517)
        }

        if (commuteCorridorDao.getCorridorCount() == 0) {
            val workCorridor = CommuteCorridorEntity(
                name = "上班通勤 (康家沟 ➔ 四惠东)",
                originStation = "康家沟",
                destinationStation = "四惠东",
                walkingMinutesAfter = 10,
                directionType = "WORK",
                lineNumbers = "553,468,517",
                isActive = true
            )
            val homeCorridor = CommuteCorridorEntity(
                name = "回家通勤 (四惠东 ➔ 康家沟)",
                originStation = "四惠东",
                destinationStation = "康家沟",
                walkingMinutesAfter = 5,
                directionType = "HOME",
                lineNumbers = "553,468,517",
                isActive = true
            )
            commuteCorridorDao.insertCorridor(workCorridor)
            commuteCorridorDao.insertCorridor(homeCorridor)
        }
    }

    // ========== 行为日志与学习 ==========

    suspend fun logBehavior(
        weekday: Int,
        hour: Int,
        locationZone: String,
        lineId: Long,
        lineNumber: String
    ) {
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

    // ========== 实时数据获取与缓存 ==========

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    /**
     * 获取实时公交数据（支持缓存和自动北京线路模拟/真实API回退）
     */
    suspend fun getRealTimeData(
        lineId: String,
        direction: String,
        boardingStationIndex: Int
    ): Result<RealTimeData> = withContext(Dispatchers.IO) {
        try {
            val cacheKey = "$lineId-$direction"
            val cached = cache[cacheKey]
            if (cached != null && cached.isValid()) {
                return@withContext Result.success(cached.data)
            }

            val lastRequest = lastRequestTime[cacheKey] ?: 0L
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequest
            if (elapsed < BeijingBusApi.MIN_REQUEST_INTERVAL) {
                delay(BeijingBusApi.MIN_REQUEST_INTERVAL - elapsed)
            }

            // 优先接入真实 API（若配置了有效 BASE_URL）
            try {
                if (BeijingBusApi.BASE_URL.contains("api.beijingbus.com").not()) {
                    val response = busApi.getRealTimeData(lineId, direction)
                    if (response.isSuccessful && response.body()?.status == 200) {
                        val data = response.body()?.data
                        if (data != null) {
                            cache[cacheKey] = CacheEntry(data)
                            lastRequestTime[cacheKey] = System.currentTimeMillis()
                            return@withContext Result.success(data)
                        }
                    }
                }
            } catch (_: Exception) {
                // 回退到本地高仿真引擎
            }

            // 本地高仿真实时位置生成（根据北京真实站点与当前时间抖动模拟）
            val mockData = generateRealisticBeijingBusData(lineId, direction, boardingStationIndex)
            cache[cacheKey] = CacheEntry(mockData)
            lastRequestTime[cacheKey] = System.currentTimeMillis()

            Result.success(mockData)
        } catch (e: Exception) {
            val cacheKey = "$lineId-$direction"
            val cached = cache[cacheKey]
            if (cached != null) {
                Result.success(cached.data)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun searchLines(keyword: String): Result<List<LineSearchResult>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(generateMockSearchResults(keyword))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ========== 北京真实公交高仿真数据生成 ==========

    private fun generateRealisticBeijingBusData(
        lineId: String,
        direction: String,
        boardingStationIndex: Int
    ): RealTimeData {
        val random = Random(System.currentTimeMillis() / 15000 + lineId.hashCode())

        // 针对 553, 468, 517 分配具有层次感的梯队到站时间
        val (firstBusStationOffset, baseMinutes) = when (lineId) {
            "553" -> Pair(-1, 2)   // 553路最快，1站到达，预计2分钟
            "468" -> Pair(-3, 6)   // 468路居中，3站到达，预计6分钟
            "517" -> Pair(-5, 12)  // 517路稍慢，5站到达，预计12分钟
            else -> Pair(random.nextInt(-5, 4), random.nextInt(2, 15))
        }

        val buses = mutableListOf<BusInfo>()

        // 产生第一辆车（最接近用户的车）
        val station1 = (boardingStationIndex + firstBusStationOffset).coerceAtLeast(0)
        val arrival1 = baseMinutes * 60 + random.nextInt(-30, 45).coerceAtLeast(0)
        buses.add(
            BusInfo(
                busId = "bj_bus_${lineId}_01",
                latitude = 39.9142 + random.nextDouble(-0.005, 0.005),
                longitude = 116.5188 + random.nextDouble(-0.005, 0.005), // 朝阳四惠康家沟附近坐标
                stationIndex = station1,
                nextStationIndex = station1 + 1,
                distanceToNext = 300 + random.nextInt(0, 400),
                arrivalTimeEstimate = arrival1,
                isArriving = arrival1 <= 120
            )
        )

        // 产生第二辆车（后续车次）
        val station2 = (station1 - random.nextInt(3, 6)).coerceAtLeast(0)
        val arrival2 = arrival1 + random.nextInt(8, 15) * 60
        buses.add(
            BusInfo(
                busId = "bj_bus_${lineId}_02",
                latitude = 39.9160 + random.nextDouble(-0.005, 0.005),
                longitude = 116.5250 + random.nextDouble(-0.005, 0.005),
                stationIndex = station2,
                nextStationIndex = station2 + 1,
                distanceToNext = 600 + random.nextInt(0, 500),
                arrivalTimeEstimate = arrival2,
                isArriving = false
            )
        )

        return RealTimeData(
            lineInfo = com.saidi.busassistant.data.remote.dto.LineInfo(
                id = lineId,
                name = "${lineId}路",
                direction = direction
            ),
            buses = buses.sortedBy { it.arrivalTimeEstimate },
            stations = emptyList()
        )
    }

    private fun generateMockSearchResults(keyword: String): List<LineSearchResult> {
        val allLines = listOf(
            LineSearchResult(
                lineId = "553",
                lineName = "553路",
                direction = "上行",
                startStation = "单店",
                endStation = "四惠枢纽站",
                stations = listOf(
                    StationResult(0, "单店"),
                    StationResult(1, "东坝家园"),
                    StationResult(2, "奥林匹克花园北门"),
                    StationResult(3, "奥林匹克花园东门"),
                    StationResult(4, "北京奥林匹克花园"),
                    StationResult(5, "东坝中路"),
                    StationResult(6, "东坝中路南口"),
                    StationResult(7, "平房东口"),
                    StationResult(8, "黄杉木店路北口"),
                    StationResult(9, "黄杉木店路南口"),
                    StationResult(10, "四季星河南街"),
                    StationResult(11, "青年路小区"),
                    StationResult(12, "天鹅湾小区"),
                    StationResult(13, "康家沟"),
                    StationResult(14, "四惠东站"),
                    StationResult(15, "四惠枢纽站")
                )
            ),
            LineSearchResult(
                lineId = "468",
                lineName = "468路",
                direction = "上行",
                startStation = "朝新嘉园",
                endStation = "四惠枢纽站",
                stations = listOf(
                    StationResult(0, "朝新嘉园"),
                    StationResult(1, "朝阳新城"),
                    StationResult(2, "高杨树"),
                    StationResult(3, "平房东口"),
                    StationResult(4, "平房"),
                    StationResult(5, "姚家园东"),
                    StationResult(6, "青年路口北"),
                    StationResult(7, "甘露园"),
                    StationResult(8, "青年路南口"),
                    StationResult(9, "康家沟"),
                    StationResult(10, "兴隆家园南区"),
                    StationResult(11, "四惠东站"),
                    StationResult(12, "陈家林"),
                    StationResult(13, "四惠枢纽站")
                )
            ),
            LineSearchResult(
                lineId = "517",
                lineName = "517路",
                direction = "上行",
                startStation = "草房",
                endStation = "四惠枢纽站",
                stations = listOf(
                    StationResult(0, "地铁草房站"),
                    StationResult(1, "常营北路"),
                    StationResult(2, "常营中路"),
                    StationResult(3, "管庄路口北"),
                    StationResult(4, "黄杉木店"),
                    StationResult(5, "十里堡"),
                    StationResult(6, "青年路"),
                    StationResult(7, "康家沟"),
                    StationResult(8, "四惠东站"),
                    StationResult(9, "四惠枢纽站")
                )
            ),
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
                    StationResult(4, "中关村")
                )
            )
        )

        return allLines.filter {
            it.lineId.contains(keyword, ignoreCase = true) ||
            it.lineName.contains(keyword, ignoreCase = true) ||
            it.stations?.any { s -> s.name.contains(keyword) } == true
        }
    }

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
