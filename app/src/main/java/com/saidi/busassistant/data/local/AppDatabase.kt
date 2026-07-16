package com.saidi.busassistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.data.local.entity.BehaviorLogEntity

@Database(
    entities = [
        BusLineEntity::class,
        BehaviorLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busLineDao(): BusLineDao
    abstract fun behaviorLogDao(): BehaviorLogDao

    companion object {
        const val DATABASE_NAME = "bus_assistant.db"
    }
}
