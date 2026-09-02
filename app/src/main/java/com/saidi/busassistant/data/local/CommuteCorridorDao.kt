package com.saidi.busassistant.data.local

import androidx.room.*
import com.saidi.busassistant.data.local.entity.CommuteCorridorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommuteCorridorDao {

    @Query("SELECT * FROM commute_corridors ORDER BY display_order ASC, created_at ASC")
    fun getAllCorridors(): Flow<List<CommuteCorridorEntity>>

    @Query("SELECT * FROM commute_corridors WHERE id = :id LIMIT 1")
    suspend fun getCorridorById(id: Long): CommuteCorridorEntity?

    @Query("SELECT * FROM commute_corridors WHERE origin_station = :origin AND destination_station = :destination LIMIT 1")
    suspend fun findCorridorByStations(origin: String, destination: String): CommuteCorridorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorridor(corridor: CommuteCorridorEntity): Long

    @Update
    suspend fun updateCorridor(corridor: CommuteCorridorEntity)

    @Delete
    suspend fun deleteCorridor(corridor: CommuteCorridorEntity)

    @Query("SELECT COUNT(*) FROM commute_corridors")
    suspend fun getCorridorCount(): Int
}
