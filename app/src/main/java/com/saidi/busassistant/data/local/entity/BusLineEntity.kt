package com.saidi.busassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * User saved / favorite bus line entity.
 */
@Entity(tableName = "bus_lines")
data class BusLineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "line_number")
    val lineNumber: String,           // Line identifier, e.g., "375"

    @ColumnInfo(name = "line_name")
    val lineName: String,             // Display name, e.g., "Line 375"

    @ColumnInfo(name = "direction")
    val direction: String,            // Direction, e.g., "Outbound" or "Inbound"

    @ColumnInfo(name = "start_station")
    val startStation: String,         // Departure origin terminal

    @ColumnInfo(name = "end_station")
    val endStation: String,           // Destination terminal

    @ColumnInfo(name = "user_boarding_station")
    val userBoardingStation: String,  // User designated boarding stop

    @ColumnInfo(name = "user_alighting_station")
    val userAlightingStation: String, // User designated alighting stop

    @ColumnInfo(name = "boarding_station_index")
    val boardingStationIndex: Int,    // 0-indexed position along route

    @ColumnInfo(name = "user_label")
    val userLabel: String? = null,    // User label: work, home, custom

    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0,        // Custom ordering rank

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
