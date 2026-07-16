package com.saidi.busassistant.data.local

import androidx.room.*
import com.saidi.busassistant.data.local.entity.BehaviorLogEntity

@Dao
interface BehaviorLogDao {

    @Insert
    suspend fun insertLog(log: BehaviorLogEntity)

    @Query("""
        SELECT viewed_bus_line_id as lineId, 
               viewed_bus_line_number as lineNumber,
               COUNT(*) as frequency
        FROM behavior_logs 
        WHERE weekday = :weekday 
          AND hour BETWEEN :hourStart AND :hourEnd 
          AND location_zone = :locationZone
        GROUP BY viewed_bus_line_id
        ORDER BY frequency DESC
        LIMIT :limit
    """)
    suspend fun getTopLinesByContext(
        weekday: Int,
        hourStart: Int,
        hourEnd: Int,
        locationZone: String,
        limit: Int = 3
    ): List<LineFrequencyResult>

    @Query("SELECT COUNT(*) FROM behavior_logs")
    suspend fun getLogCount(): Int

    @Query("DELETE FROM behavior_logs")
    suspend fun clearAllLogs()

    @Query("""
        SELECT * FROM behavior_logs 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getRecentLogs(limit: Int = 50): List<BehaviorLogEntity>

    /**
     * 获取某条线路在指定时段出现的次数
     */
    @Query("""
        SELECT COUNT(*) FROM behavior_logs 
        WHERE viewed_bus_line_id = :lineId
        AND weekday = :weekday
        AND hour BETWEEN :hourStart AND :hourEnd
    """)
    suspend fun getLineFrequencyInTimeWindow(
        lineId: Long,
        weekday: Int,
        hourStart: Int,
        hourEnd: Int
    ): Int
}

/**
 * 统计结果数据类
 */
data class LineFrequencyResult(
    val lineId: Long,
    val lineNumber: String,
    val frequency: Int
)
