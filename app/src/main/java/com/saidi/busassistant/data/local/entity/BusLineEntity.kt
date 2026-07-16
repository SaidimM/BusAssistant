package com.saidi.busassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * 用户收藏的公交线路
 */
@Entity(tableName = "bus_lines")
data class BusLineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "line_number")
    val lineNumber: String,           // 线路号，如 "375"

    @ColumnInfo(name = "line_name")
    val lineName: String,             // 线路名称，如 "375路"

    @ColumnInfo(name = "direction")
    val direction: String,            // 方向，如 "上行" 或 "下行"

    @ColumnInfo(name = "start_station")
    val startStation: String,         // 起始站

    @ColumnInfo(name = "end_station")
    val endStation: String,           // 终点站

    @ColumnInfo(name = "user_boarding_station")
    val userBoardingStation: String,  // 用户上车站点

    @ColumnInfo(name = "user_alighting_station")
    val userAlightingStation: String, // 用户下车站点

    @ColumnInfo(name = "boarding_station_index")
    val boardingStationIndex: Int,    // 上车站点在全线中的索引（用于计算车辆距离）

    @ColumnInfo(name = "user_label")
    val userLabel: String? = null,    // 用户标注：上班/回家/自定义

    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0,        // 显示顺序

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
