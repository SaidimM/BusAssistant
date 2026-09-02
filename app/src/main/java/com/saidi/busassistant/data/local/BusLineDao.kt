package com.saidi.busassistant.data.local

import androidx.room.*
import com.saidi.busassistant.data.local.entity.BusLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusLineDao {

    @Query("SELECT * FROM bus_lines ORDER BY display_order ASC, created_at ASC")
    fun getAllLines(): Flow<List<BusLineEntity>>

    @Query("SELECT * FROM bus_lines ORDER BY display_order ASC, created_at ASC")
    suspend fun getAllLinesSync(): List<BusLineEntity>

    @Query("SELECT * FROM bus_lines WHERE id = :id")
    suspend fun getLineById(id: Long): BusLineEntity?

    @Query("SELECT COUNT(*) FROM bus_lines")
    suspend fun getLineCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: BusLineEntity): Long

    @Update
    suspend fun updateLine(line: BusLineEntity)

    @Delete
    suspend fun deleteLine(line: BusLineEntity)

    @Query("UPDATE bus_lines SET user_label = :label WHERE id = :lineId")
    suspend fun updateLabel(lineId: Long, label: String?)

    @Query("UPDATE bus_lines SET display_order = :order WHERE id = :lineId")
    suspend fun updateDisplayOrder(lineId: Long, order: Int)

    @Query("SELECT MAX(display_order) FROM bus_lines")
    suspend fun getMaxDisplayOrder(): Int?

    @Query("SELECT * FROM bus_lines WHERE line_number = :lineNumber AND direction = :direction LIMIT 1")
    suspend fun findLineByNumberAndDirection(lineNumber: String, direction: String): BusLineEntity?
}
