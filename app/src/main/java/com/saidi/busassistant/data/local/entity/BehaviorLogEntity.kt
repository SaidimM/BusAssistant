package com.saidi.busassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

/**
 * User behavior and interaction log entity.
 * Passively recorded on-device to learn commute routines.
 */
@Entity(
    tableName = "behavior_logs",
    indices = [
        Index(value = ["weekday", "hour", "location_zone"]),
        Index(value = ["created_at"])
    ]
)
data class BehaviorLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "weekday")
    val weekday: Int,                  // 1=Mon, 2=Tue, ..., 7=Sun

    @ColumnInfo(name = "hour")
    val hour: Int,                     // 0-23

    @ColumnInfo(name = "location_zone")
    val locationZone: String,          // Coarse geographic cluster ID

    @ColumnInfo(name = "viewed_bus_line_id")
    val viewedBusLineId: Long,         // Associated bus line database ID

    @ColumnInfo(name = "viewed_bus_line_number")
    val viewedBusLineNumber: String,   // Route number, e.g. "375"

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
