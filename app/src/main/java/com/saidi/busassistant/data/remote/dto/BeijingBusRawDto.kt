package com.saidi.busassistant.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Payload for ssgj/v1.0.0/checkUpdate endpoint.
 */
data class CheckUpdateResponse(
    @SerializedName("lines")
    val lines: LinesWrapper?
)

data class LinesWrapper(
    @SerializedName("line")
    val lineList: List<LineMetaRaw>?
)

data class LineMetaRaw(
    @SerializedName("id")
    val id: String?,
    @SerializedName("linename")
    val lineName: String?,
    @SerializedName("classify")
    val classify: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("version")
    val version: String?
)

/**
 * Payload for ssgj/v1.0.0/update?id={lineId} endpoint.
 */
data class LineDetailResponse(
    @SerializedName("busline")
    val busline: List<BusLineDetailRaw>?
)

data class BusLineDetailRaw(
    @SerializedName("lineid")
    val lineId: String?,
    @SerializedName("shotname")
    val shortName: String?,
    @SerializedName("linename")
    val lineName: String?,
    @SerializedName("time")
    val operationTime: String?,
    @SerializedName("coord")
    val coords: String?,
    @SerializedName("stations")
    val stations: StationsWrapper?
)

data class StationsWrapper(
    @SerializedName("station")
    val stationList: List<StationRawItem>?
)

data class StationRawItem(
    @SerializedName("no")
    val no: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("lat")
    val lat: String?,
    @SerializedName("lon")
    val lon: String?
)

/**
 * Payload for ssgj/bus.php?id={lineId}&no={stationNo}&encrypt=1 endpoint.
 */
data class BusRealTimeRawResponse(
    @SerializedName("root")
    val root: BusRootWrapper?
)

data class BusRootWrapper(
    @SerializedName("data")
    val data: BusDataWrapper?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?
)

data class BusDataWrapper(
    @SerializedName("bus")
    val busList: List<BusRawItem>?
)

data class BusRawItem(
    @SerializedName("id")
    val id: String?,
    @SerializedName("lid")
    val lineId: String?,
    @SerializedName("gt")
    val gpsUpdateTime: String?, // Key seed: key = md5("aibang" + gt)
    @SerializedName("x")
    val lonEncrypted: String?, // RC4 encrypted longitude
    @SerializedName("y")
    val latEncrypted: String?, // RC4 encrypted latitude
    @SerializedName("sd")
    val distanceRemainingEncrypted: String?, // Remaining distance in meters
    @SerializedName("srt")
    val runDurationEncrypted: String?, // Remaining time in seconds
    @SerializedName("st")
    val arrivalTimeEncrypted: String?, // Arrival timestamp
    @SerializedName("ns")
    val nextStationNameEncrypted: String?, // Next station name
    @SerializedName("nsn")
    val nextStationIndexEncrypted: String?, // Next station index
    @SerializedName("nsd")
    val nextStationDistance: String?, // Distance to next station
    @SerializedName("nsrt")
    val nextStationRunTime: String?, // Run time to next station
    @SerializedName("nst")
    val nextStationArrivalTime: String?, // Timestamp to next station
    @SerializedName("t")
    val type: String?,
    @SerializedName("lt")
    val delay: String?
)
