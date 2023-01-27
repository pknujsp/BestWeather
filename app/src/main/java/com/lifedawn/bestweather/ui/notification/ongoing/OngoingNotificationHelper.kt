package com.lifedawn.bestweather.ui.notification.ongoing

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.IntentRequestCodes

class OngoingNotificationHelper constructor(private val context: Context) {
    private val alarmManager: AlarmManager

    init {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun onSelectedAutoRefreshInterval(millis: Long) {
        cancelAutoRefresh()
        if (millis > 0) {
            val refreshIntent: Intent = Intent(context, OngoingNotificationReceiver::class.java)
            refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context, IntentRequestCodes.ONGOING_NOTIFICATION_AUTO_REFRESH.requestCode, refreshIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + millis,
                millis, pendingIntent
            )
        }
    }

    fun cancelAutoRefresh() {
        val pendingIntent: PendingIntent? = PendingIntent.getBroadcast(
            context, IntentRequestCodes.ONGOING_NOTIFICATION_AUTO_REFRESH.requestCode, Intent(
                context,
                OngoingNotificationReceiver::class.java
            ), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_MUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    val isRepeating: Boolean
        get() {
            val pendingIntent: PendingIntent? = PendingIntent.getBroadcast(
                context,
                IntentRequestCodes.ONGOING_NOTIFICATION_AUTO_REFRESH.requestCode,
                Intent(context, OngoingNotificationReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            return pendingIntent != null
        }
    val refreshPendingIntent: PendingIntent
        get() {
            val refreshIntent: Intent = Intent(context, OngoingNotificationReceiver::class.java)
            refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))
            return PendingIntent.getBroadcast(
                context, IntentRequestCodes.ONGOING_NOTIFICATION_MANUALLY_REFRESH.requestCode, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

    fun createManualPendingIntent(action: String?, flags: Int): PendingIntent {
        val intent: Intent = Intent(context, OngoingNotificationReceiver::class.java)
        intent.setAction(action)
        return PendingIntent.getBroadcast(
            context, IntentRequestCodes.ONGOING_NOTIFICATION_MANUALLY_REFRESH.requestCode,
            intent, flags
        )
    }
}