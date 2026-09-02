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
 * Location context and spatial proximity utility.
 * Identifies nearest transit stops for zero-interaction departure radar.
 */
@Singleton
class LocationContextManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Checks if fine or coarse location permission is granted.
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
     * Retrieves best cached location without waking up GPS hardware.
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
                if (bestLocation == null || (loc.accuracy > 0 && loc.accuracy < bestLocation.accuracy)) {
                    bestLocation = loc
                }
            } catch (_: SecurityException) {
                // Ignore security exceptions gracefully
            }
        }
        return bestLocation
    }

    /**
     * Calculates distance from current position to target coordinates in meters.
     */
    fun distanceTo(targetLat: Double, targetLon: Double): Float? {
        val current = getLastKnownLocation() ?: return null
        return calculateDistance(current.latitude, current.longitude, targetLat, targetLon)
    }

    /**
     * Calculates straight-line distance between two geographic coordinates in meters.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * Estimates normal walking duration in minutes (~75 meters/minute walking speed).
     */
    fun calculateWalkingMinutes(distanceMeters: Int): Int {
        val mins = Math.ceil(distanceMeters / 75.0).toInt()
        return mins.coerceAtLeast(1)
    }

    /**
     * Determines whether the user is within a geofenced proximity radius of a bus stop.
     */
    fun isNearStation(targetLat: Double, targetLon: Double, radiusMeters: Float = 800f): Boolean {
        val dist = distanceTo(targetLat, targetLon) ?: return false
        return dist <= radiusMeters
    }
}
