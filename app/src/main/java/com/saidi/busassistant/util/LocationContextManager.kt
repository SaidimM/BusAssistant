package com.saidi.busassistant.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 位置上下文与地理围栏辅助类 (Phase 2 & 3)
 * 用于判断用户当前是否靠近某个起始站点或换乘枢纽，辅助自动推测通勤方向
 */
@Singleton
class LocationContextManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 检查是否有位置权限
     */
    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * 获取最新已知粗略位置（零后台耗电）
     */
    fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            try {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            } catch (_: SecurityException) {
                // 安全异常静默捕获
            }
        }
        return bestLocation
    }

    /**
     * 计算当前位置到目标经纬度的直线距离（米）
     */
    fun distanceTo(targetLat: Double, targetLon: Double): Float? {
        val current = getLastKnownLocation() ?: return null
        val results = FloatArray(1)
        Location.distanceBetween(
            current.latitude,
            current.longitude,
            targetLat,
            targetLon,
            results
        )
        return results[0]
    }

    /**
     * 判断是否在某站点的近距离范围（如 500 米地理围栏以内）
     */
    fun isNearStation(targetLat: Double, targetLon: Double, radiusMeters: Float = 500f): Boolean {
        val dist = distanceTo(targetLat, targetLon) ?: return false
        return dist <= radiusMeters
    }
}
