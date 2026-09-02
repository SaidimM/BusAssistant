package com.saidi.busassistant.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Real-time bus domain DTO models.
 */
data class BusRealTimeResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("msg")
    val message: String?,
    @SerializedName("data")
    val data: RealTimeData?
)

data class RealTimeData(
    @SerializedName("line")
    val lineInfo: LineInfo?,
    @SerializedName("buses")
    val buses: List<BusInfo>?,
    @SerializedName("stations")
    val stations: List<StationInfo>?
)

data class LineInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("direction")
    val direction: String
)

data class BusInfo(
    @SerializedName("bus_id")
    val busId: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
    @SerializedName("station_index")
    val stationIndex: Int,            // Current station index
    @SerializedName("next_station_index")
    val nextStationIndex: Int,        // Next upcoming station index
    @SerializedName("distance_to_next")
    val distanceToNext: Int,          // Distance to next station in meters
    @SerializedName("arrival_time_estimate")
    val arrivalTimeEstimate: Int?,    // Estimated arrival duration in seconds
    @SerializedName("is_arriving")
    val isArriving: Boolean           // Flag indicating imminent arrival
)

data class StationInfo(
    @SerializedName("index")
    val index: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double
)

/**
 * Line search response DTO.
 */
data class LineSearchResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("data")
    val lines: List<LineSearchResult>?
)

data class LineSearchResult(
    @SerializedName("line_id")
    val lineId: String,
    @SerializedName("line_name")
    val lineName: String,
    @SerializedName("direction")
    val direction: String,
    @SerializedName("start_station")
    val startStation: String,
    @SerializedName("end_station")
    val endStation: String,
    @SerializedName("stations")
    val stations: List<StationResult>?
)

data class StationResult(
    @SerializedName("index")
    val index: Int,
    @SerializedName("name")
    val name: String
)
