package com.lifedawn.bestweather.ui.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback

class WidgetHelper(private val context: Context) {
    private val alarmManager: AlarmManager

    init {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun onSelectedAutoRefreshInterval(`val`: Long) {
        cancelAutoRefresh()
        if (`val` > 0) {
            val refreshIntent = Intent(context, FirstWidgetProvider::class.java)
            refreshIntent.action = context.getString(R.string.com_lifedawn_bestweather_action_REFRESH)
            val pendingIntent = PendingIntent.getBroadcast(
                context, IntentRequestCodes.WIDGET_AUTO_REFRESH.requestCode, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + `val`, `val`, pendingIntent)
        }
    }

    fun cancelAutoRefresh() {
        val pendingIntent = PendingIntent.getBroadcast(
            context, IntentRequestCodes.WIDGET_AUTO_REFRESH.requestCode, Intent(context, FirstWidgetProvider::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    val isRepeating: Boolean
        get() = PendingIntent.getBroadcast(
            context, IntentRequestCodes.WIDGET_AUTO_REFRESH.requestCode, Intent(context, FirstWidgetProvider::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) != null

    fun reDrawWidgets(callback: BackgroundWorkCallback?, vararg appWidgetIds: Int) {
        val widgetRepository: WidgetRepository = WidgetRepository.getINSTANCE()
        widgetRepository.getAll(object : DbQueryCallback<List<WidgetDto?>?>() {
            fun onResultSuccessful(result: List<WidgetDto?>) {
                if (result.size > 0) {
                    val widgetRefreshInterval: Long = refreshInterval
                    if (widgetRefreshInterval > 0L && !isRepeating) onSelectedAutoRefreshInterval(widgetRefreshInterval)
                    val requestCode = 200000
                    val refreshIntent = Intent(context, FirstWidgetProvider::class.java)
                    refreshIntent.action = context.getString(R.string.com_lifedawn_bestweather_action_REDRAW)
                    val bundle = Bundle()
                    val ids = IntArray(result.size)
                    var idx = 0
                    for (widgetDto in result) {
                        ids[idx++] = widgetDto.appWidgetId
                    }
                    bundle.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    refreshIntent.putExtras(bundle)
                    try {
                        PendingIntent.getBroadcast(
                            context, requestCode, refreshIntent,
                            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                        ).send()
                    } catch (e: PendingIntent.CanceledException) {
                        e.printStackTrace()
                    }
                }
                if (callback != null) callback.onFinished()
            }

            fun onResultNoData() {
                if (callback != null) callback.onFinished()
            }
        })
    }

    val refreshInterval: Long
        get() = PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(context.getString(R.string.pref_key_widget_refresh_interval), 0L)
}