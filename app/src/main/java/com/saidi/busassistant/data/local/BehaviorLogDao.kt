package com.saidi.busassistant.data.local

import androidx.room.*
import com.saidi.busassistant.data.local.entity.BehaviorLogEntity

/**
 * Data access object for behavior logging and habit mining.
 */
@Dao
interface BehaviorLogDao {

    @Insert
    suspend fun insertLog(log: BehaviorLogEntity): Long

    /**
     * Finds most frequently used lines given day-of-week, hour range, and location zone.
     */
    @Query("""
        SELECT viewed_bus_line_id AS lineId, 
               viewed_bus_line_number AS lineNumber, 
               COUNT(*) AS frequency
        FROM behavior_logs
        WHERE weekday = :weekday
          AND hour BETWEEN :hourStart AND :hourEnd
          AND location_zone = :locationZone
        GROUP BY viewed_bus_line_id, viewed_bus_line_number
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

    /**
     * Extracts frequent lines within specific time-window clusters.
     */
    @Query("""
        SELECT viewed_bus_line_id AS lineId, 
               viewed_bus_line_number AS lineNumber, 
               COUNT(*) AS frequency
        FROM behavior_logs
        WHERE weekday BETWEEN :startWeekday AND :endWeekday
          AND hour BETWEEN :startHour AND :endHour
        GROUP BY viewed_bus_line_id, viewed_bus_line_number
        ORDER BY frequency DESC
        LIMIT :limit
    """)
    suspend fun getFrequentLinesInTimeCluster(
        startWeekday: Int,
        endWeekday: Int,
        startHour: Int,
        endHour: Int,
        limit: Int = 2
    ): List<LineFrequencyResult>

    /**
     * Counts recorded trips within a specific time-window cluster.
     */
    @Query("""
        SELECT COUNT(*)
        FROM behavior_logs
        WHERE weekday BETWEEN :startWeekday AND :endWeekday
          AND hour BETWEEN :startHour AND :endHour
    """)
    suspend fun getTripCountInTimeCluster(
        startWeekday: Int,
        endWeekday: Int,
        startHour: Int,
        endHour: Int
    ): Int

    /**
     * Finds overall most frequented line.
     */
    @Query("""
        SELECT viewed_bus_line_id AS lineId, 
               viewed_bus_line_number AS lineNumber, 
               COUNT(*) AS frequency
        FROM behavior_logs
        GROUP BY viewed_bus_line_id, viewed_bus_line_number
        ORDER BY frequency DESC
        LIMIT 1
    """)
    suspend fun getTopLineOverall(): LineFrequencyResult?

    @Query("SELECT COUNT(*) FROM behavior_logs")
    suspend fun getLogCount(): Int

    @Query("DELETE FROM behavior_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM behavior_logs WHERE viewed_bus_line_number = :lineNumber")
    suspend fun deleteLogsByLineNumber(lineNumber: String)

    @Query("SELECT * FROM behavior_logs ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 50): List<BehaviorLogEntity>
}

data class LineFrequencyResult(
    val lineId: Long,
    val lineNumber: String,
    val frequency: Int
)
