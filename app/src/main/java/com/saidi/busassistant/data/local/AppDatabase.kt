package com.saidi.busassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.data.local.entity.BehaviorLogEntity
import com.saidi.busassistant.data.local.entity.CommuteCorridorEntity

@Database(
    entities = [
        BusLineEntity::class,
        BehaviorLogEntity::class,
        CommuteCorridorEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busLineDao(): BusLineDao
    abstract fun behaviorLogDao(): BehaviorLogDao
    abstract fun commuteCorridorDao(): CommuteCorridorDao

    companion object {
        const val DATABASE_NAME = "bus_assistant.db"
    }
}
