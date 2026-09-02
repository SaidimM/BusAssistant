package com.saidi.busassistant.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.saidi.busassistant.MainActivity
import com.saidi.busassistant.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ongoing Live Commute Notification Manager.
 * Displays live departure countdowns on lock screen and notification shade during commute peaks.
 */
@Singleton
class CommuteNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Live Commute Departures",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing real-time bus countdowns on lock screen and notification shade"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows or updates ongoing live commute notification.
     */
    fun showCommuteLiveNotification(
        corridorOrStationName: String,
        fastestLine: String,
        minutesAway: Int,
        stopsAway: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚡ Next: $fastestLine in ${if (minutesAway <= 2) "1 min (Arriving)" else "$minutesAway min ($stopsAway stops)"}"
        val content = "📍 $corridorOrStationName · Tap to view live corridor departures"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bus)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Dismisses the live commute notification.
     */
    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "commute_live_channel"
        const val NOTIFICATION_ID = 1001
    }
}
