package com.saidi.busassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * 通用通勤走廊实体
 * 聚合同一起讫点区段的多条候选公交线路（如任意的 [起始站] ➔ [目的站]）
 */
@Entity(tableName = "commute_corridors")
data class CommuteCorridorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,                    // 走廊名称，如 "早间通勤"、"晚间返程"

    @ColumnInfo(name = "origin_station")
    val originStation: String,           // 起始站点名称

    @ColumnInfo(name = "destination_station")
    val destinationStation: String,      // 目的站点名称

    @ColumnInfo(name = "walking_minutes_after")
    val walkingMinutesAfter: Int = 10,   // 下车后步行至最终目的地的分钟数（可由用户自定义）

    @ColumnInfo(name = "corridor_tag")
    val corridorTag: String = "COMMUTE", // 标签类别，例如 "WORK", "HOME", "SCHOOL", "CUSTOM"

    @ColumnInfo(name = "line_numbers")
    val lineNumbers: String,             // 逗号分隔候选线路号（例如 "33,12,84" 或用户添加的任意线路）

    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
