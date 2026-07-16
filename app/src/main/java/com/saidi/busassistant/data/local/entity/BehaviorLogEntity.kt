package com.saidi.busassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * 用户行为日志 —— 用于习惯学习
 * 仅在用户打开App并查看线路时记录
 */
@Entity(tableName = "behavior_logs")
data class BehaviorLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "weekday")
    val weekday: Int,                 // 1=周一, 7=周日

    @ColumnInfo(name = "hour")
    val hour: Int,                    // 0-23

    @ColumnInfo(name = "location_zone")
    val locationZone: String,         // "home" | "company" | "other"

    @ColumnInfo(name = "viewed_bus_line_id")
    val viewedBusLineId: Long,        // 查看的线路ID

    @ColumnInfo(name = "viewed_bus_line_number")
    val viewedBusLineNumber: String   // 查看的线路号（冗余，便于统计）
)
