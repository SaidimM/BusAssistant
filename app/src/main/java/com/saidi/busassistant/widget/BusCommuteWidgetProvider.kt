package com.saidi.busassistant.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.saidi.busassistant.MainActivity
import com.saidi.busassistant.R
import com.saidi.busassistant.data.repository.BusRepository
import com.saidi.busassistant.util.LocationContextManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Home Screen AppWidget Provider.
 * Displays live commute corridor departures or nearby station radar for 1-second glanceability.
 */
class BusCommuteWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun busRepository(): BusRepository
        fun locationContextManager(): LocationContextManager
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BusCommuteWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_commute_corridor)

        // Tap container to open MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

        // Tap refresh button
        val refreshIntent = Intent(context, BusCommuteWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        // Asynchronously query commute corridor or nearest bus stop
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val repository = entryPoint.busRepository()
                val locationContextManager = entryPoint.locationContextManager()

                val corridors = repository.getAllCorridors().firstOrNull() ?: emptyList()
                val lines = repository.getAllLines().firstOrNull() ?: emptyList()

                if (corridors.isNotEmpty()) {
                    // Mode A: Active commute corridor exists
                    val activeCorridor = corridors.first()
                    val candidateLineNumbers = activeCorridor.lineNumbers.split(",").map { it.trim() }
                    val candidateLines = lines.filter { it.lineNumber in candidateLineNumbers }

                    var fastestLineNumber: String? = null
                    var earliestMinutes = 999
                    val linesSummaryList = mutableListOf<String>()

                    for (line in candidateLines) {
                        val result = repository.getRealTimeData(
                            lineId = line.lineNumber,
                            direction = line.direction,
                            boardingStationIndex = line.boardingStationIndex
                        )
                        result.onSuccess { data ->
                            val closest = data.buses?.minByOrNull { it.arrivalTimeEstimate ?: 9999 }
                            val mins = closest?.arrivalTimeEstimate?.div(60) ?: 10
                            linesSummaryList.add("${line.lineNumber}: ${mins}m")
                            if (mins < earliestMinutes) {
                                earliestMinutes = mins
                                fastestLineNumber = line.lineNumber
                            }
                        }
                    }

                    views.setTextViewText(R.id.widget_tag, activeCorridor.corridorTag)
                    views.setTextViewText(
                        R.id.widget_route_title,
                        "${activeCorridor.originStation} ➔ ${activeCorridor.destinationStation}"
                    )

                    if (fastestLineNumber != null && earliestMinutes < 900) {
                        views.setTextViewText(
                            R.id.widget_fastest_label,
                            "Next: $fastestLineNumber (${if (earliestMinutes <= 2) "Arriving" else "Fastest"})"
                        )
                        views.setTextViewText(
                            R.id.widget_fastest_countdown,
                            if (earliestMinutes == 0) "Arrived" else "$earliestMinutes min"
                        )
                    }

                    if (linesSummaryList.isNotEmpty()) {
                        views.setTextViewText(
                            R.id.widget_lines_summary,
                            "Lines: " + linesSummaryList.joinToString("  |  ")
                        )
                    }
                } else {
                    // Mode B: Zero-configuration fallback to Nearby Station Radar
                    val loc = locationContextManager.getLastKnownLocation()
                    val lat = loc?.latitude ?: 39.9982
                    val lon = loc?.longitude ?: 116.4741

                    val (nearestStation, distance) = repository.findNearestStation(lat, lon)
                    val arrivals = repository.getNearbyStationArrivals(nearestStation)
                    val fastest = arrivals.firstOrNull()

                    views.setTextViewText(R.id.widget_tag, "📍 Nearby Stop")
                    views.setTextViewText(
                        R.id.widget_route_title,
                        "${nearestStation.stationName} (${distance.toInt()}m)"
                    )

                    if (fastest != null) {
                        views.setTextViewText(
                            R.id.widget_fastest_label,
                            "Next: ${fastest.lineNumber} (${if (fastest.isArriving) "Arriving" else "Fastest"})"
                        )
                        views.setTextViewText(
                            R.id.widget_fastest_countdown,
                            if (fastest.arrivalMinutes == 0) "Arrived" else "${fastest.arrivalMinutes} min"
                        )
                        val summary = arrivals.map { "${it.lineNumber}: ${it.arrivalMinutes}m" }
                        views.setTextViewText(
                            R.id.widget_lines_summary,
                            "Passing: " + summary.joinToString("  |  ")
                        )
                    } else {
                        views.setTextViewText(R.id.widget_fastest_label, "Tap refresh for live departures")
                        views.setTextViewText(R.id.widget_fastest_countdown, "--")
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.saidi.busassistant.widget.ACTION_REFRESH"
    }
}
