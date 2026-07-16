package com.saidi.busassistant.data.remote

import com.saidi.busassistant.data.remote.dto.BusRealTimeResponse
import com.saidi.busassistant.data.remote.dto.LineSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 北京实时公交 API 接口
 *
 * 注意：此接口基于北京公交官方开放接口设计
 * 实际使用时需要替换为有效的 API 基础地址和认证信息
 *
 * 开发阶段可使用模拟数据（见 BusRepository 中的 Mock 模式）
 */
interface BeijingBusApi {

    /**
     * 搜索公交线路
     * @param keyword 线路号关键词
     */
    @GET("/api/busline/search")
    suspend fun searchLines(
        @Query("keyword") keyword: String
    ): Response<LineSearchResponse>

    /**
     * 获取线路实时数据
     * @param lineId 线路ID
     * @param direction 方向（0=上行, 1=下行）
     */
    @GET("/api/bus/realtime")
    suspend fun getRealTimeData(
        @Query("line_id") lineId: String,
        @Query("direction") direction: String
    ): Response<BusRealTimeResponse>

    /**
     * 获取线路站点列表
     * @param lineId 线路ID
     */
    @GET("/api/busline/stations")
    suspend fun getLineStations(
        @Query("line_id") lineId: String
    ): Response<LineSearchResponse>

    companion object {
        // 北京公交 API 基础地址（示例，需替换为实际地址）
        const val BASE_URL = "https://api.beijingbus.com/v1/"

        // 请求间隔限制（毫秒）
        const val MIN_REQUEST_INTERVAL = 15000L // 15秒

        // 缓存有效期（毫秒）
        const val CACHE_VALID_DURATION = 30000L // 30秒
    }
}
