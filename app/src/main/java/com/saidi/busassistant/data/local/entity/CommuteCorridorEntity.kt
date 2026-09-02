package com.saidi.busassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

/**
 * Commute corridor entity.
 * Aggregates multiple bus lines that share the same origin and destination stops.
 */
@Entity(
    tableName = "commute_corridors",
    indices = [
        Index(value = ["origin_station", "destination_station"], unique = true)
    ]
)
data class CommuteCorridorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,                  // Corridor label, e.g., "Nanhu ➔ Science Park"

    @ColumnInfo(name = "origin_station")
    val originStation: String,         // Common departure stop

    @ColumnInfo(name = "destination_station")
    val destinationStation: String,    // Common arrival stop

    @ColumnInfo(name = "walking_minutes_after")
    val walkingMinutesAfter: Int = 10, // Walking time from bus stop to final destination

    @ColumnInfo(name = "corridor_tag")
    val corridorTag: String = "COMMUTE", // Tag: COMMUTE, HOME, CUSTOM

    @ColumnInfo(name = "line_numbers")
    val lineNumbers: String = "",      // Comma-separated line numbers, e.g., "33,12,84,571"

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
