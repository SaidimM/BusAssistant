package com.saidi.busassistant.data.remote

import com.saidi.busassistant.data.remote.dto.BusRealTimeRawResponse
import com.saidi.busassistant.data.remote.dto.CheckUpdateResponse
import com.saidi.busassistant.data.remote.dto.LineDetailResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Beijing Real-Time Bus Transit API.
 * Reverse-engineered interface based on leavez/fucking-beijing-bus-api.
 */
interface BeijingBusApi {

    /**
     * Retrieves all transit lines in the Beijing network (~2,000+ line variants).
     */
    @Headers(
        "PID: 5",
        "PLATFORM: ios",
        "CID: 18d31a75a568b1e9fab8e410d398f981",
        "TIME: 1539706356",
        "ABTOKEN: 31d7dae1d869a172f3b66fa14fe274d1",
        "VID: 6",
        "IMEI: 9012",
        "CTYPE: json"
    )
    @GET("ssgj/v1.0.0/checkUpdate")
    suspend fun checkUpdate(
        @Query("version") version: Int = 1,
        @Query("city") city: String = "%E5%8C%97%E4%BA%AC",
        @Query("datatype") dataType: String = "json"
    ): Response<CheckUpdateResponse>

    /**
     * Retrieves detailed route stops, sequences, and GPS coordinates for a line.
     * @param lineId Unique route ID returned by checkUpdate (e.g. "1001")
     */
    @Headers(
        "PID: 5",
        "PLATFORM: ios",
        "CID: 18d31a75a568b1e9fab8e410d398f981",
        "TIME: 1540031093",
        "ABTOKEN: 55750cf92a54b09bd52e23105f7f60aa",
        "VID: 6",
        "IMEI: 9012",
        "CTYPE: json"
    )
    @GET("ssgj/v1.0.0/update")
    suspend fun getLineDetail(
        @Query("id") lineId: String,
        @Query("city") city: String = "%E5%8C%97%E4%BA%AC",
        @Query("datatype") dataType: String = "json"
    ): Response<LineDetailResponse>

    /**
     * Retrieves live real-time vehicle telemetry and arrival estimates for a station.
     * @param lineId Target route identifier
     * @param stationNo Boarding stop sequence number (1-indexed)
     * @param encrypt Enable RC4 obfuscation (fixed at 1)
     */
    @GET("ssgj/bus.php")
    suspend fun getRealTimeBus(
        @Query("id") lineId: String,
        @Query("no") stationNo: Int,
        @Query("encrypt") encrypt: Int = 1,
        @Query("city") city: String = "%E5%8C%97%E4%BA%AC",
        @Query("datatype") dataType: String = "json"
    ): Response<BusRealTimeRawResponse>

    companion object {
        // Official municipal transit telemetry endpoint (HTTP)
        const val BASE_URL = "http://transapp.btic.org.cn:8512/"

        // Request throttling and caching parameters
        const val MIN_REQUEST_INTERVAL = 5000L // 5 seconds
        const val CACHE_VALID_DURATION = 15000L // 15 seconds
    }
}
