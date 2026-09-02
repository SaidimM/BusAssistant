package com.saidi.busassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * 通勤走廊实体
 * 聚合同一通勤区段的多条候选公交线路（例如：康家沟 -> 四惠东 包含 553路、468路、517路）
 */
@Entity(tableName = "commute_corridors")
data class CommuteCorridorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,                    // 如 "上班通勤 (康家沟 ➔ 四惠东)"

    @ColumnInfo(name = "origin_station")
    val originStation: String,           // 起始站点，如 "康家沟"

    @ColumnInfo(name = "destination_station")
    val destinationStation: String,      // 目的站点，如 "四惠东"

    @ColumnInfo(name = "walking_minutes_after")
    val walkingMinutesAfter: Int = 10,   // 下车后步行至目的地的分钟数（如 10 分钟到工位）

    @ColumnInfo(name = "direction_type")
    val directionType: String = "WORK",  // "WORK" (上班) 或 "HOME" (回家)

    @ColumnInfo(name = "line_numbers")
    val lineNumbers: String,             // 逗号分隔候选线路号，如 "553,468,517"

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
