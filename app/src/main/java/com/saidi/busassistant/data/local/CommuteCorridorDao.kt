package com.saidi.busassistant.data.local

import androidx.room.*
import com.saidi.busassistant.data.local.entity.CommuteCorridorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommuteCorridorDao {

    @Query("SELECT * FROM commute_corridors ORDER BY id ASC")
    fun getAllCorridors(): Flow<List<CommuteCorridorEntity>>

    @Query("SELECT * FROM commute_corridors WHERE direction_type = :directionType LIMIT 1")
    suspend fun getCorridorByDirection(directionType: String): CommuteCorridorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorridor(corridor: CommuteCorridorEntity): Long

    @Update
    suspend fun updateCorridor(corridor: CommuteCorridorEntity)

    @Delete
    suspend fun deleteCorridor(corridor: CommuteCorridorEntity)

    @Query("SELECT COUNT(*) FROM commute_corridors")
    suspend fun getCorridorCount(): Int
}
