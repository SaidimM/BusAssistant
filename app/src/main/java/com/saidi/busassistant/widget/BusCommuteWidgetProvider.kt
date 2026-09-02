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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * 桌面微件 (Home Screen AppWidget)
 * 实现通勤走廊“零次点击、一秒即览”核心体验
 */
class BusCommuteWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun busRepository(): BusRepository
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

        // 点击打开 App
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

        // 点击刷新按钮
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

        // 异步查询当前通勤走廊与各线路到站状态
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val repository = entryPoint.busRepository()
                val corridors = repository.getAllCorridors().firstOrNull() ?: emptyList()
                val lines = repository.getAllLines().firstOrNull() ?: emptyList()

                if (corridors.isNotEmpty()) {
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
                            linesSummaryList.add("${line.lineNumber}路: ${mins}分")
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
                            "首选: ${fastestLineNumber}路 (${if (earliestMinutes <= 2) "即将到站" else "离你最近"})"
                        )
                        views.setTextViewText(
                            R.id.widget_fastest_countdown,
                            if (earliestMinutes == 0) "已进站" else "${earliestMinutes} 分钟"
                        )
                    }

                    if (linesSummaryList.isNotEmpty()) {
                        views.setTextViewText(
                            R.id.widget_lines_summary,
                            "候选: " + linesSummaryList.joinToString("  |  ")
                        )
                    }
                } else {
                    views.setTextViewText(R.id.widget_route_title, "暂未设置通勤走廊")
                    views.setTextViewText(R.id.widget_fastest_label, "打开 App 添加常用公交线路")
                    views.setTextViewText(R.id.widget_fastest_countdown, "--")
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {
                // 异常时保留默认视图
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.saidi.busassistant.widget.ACTION_REFRESH"
    }
}
