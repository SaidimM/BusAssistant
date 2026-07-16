package com.saidi.busassistant.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 实时公交数据 DTO
 * 对接北京公交 API 的返回格式
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
    val stationIndex: Int,            // 车辆当前所在站点索引
    @SerializedName("next_station_index")
    val nextStationIndex: Int,        // 下一站索引
    @SerializedName("distance_to_next")
    val distanceToNext: Int,          // 距下一站距离(米)
    @SerializedName("arrival_time_estimate")
    val arrivalTimeEstimate: Int?,    // 预计到站时间(秒)
    @SerializedName("is_arriving")
    val isArriving: Boolean           // 是否即将到站
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
 * 线路搜索 DTO
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
