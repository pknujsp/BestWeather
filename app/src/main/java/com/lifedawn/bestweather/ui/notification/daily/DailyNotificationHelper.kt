package com.lifedawn.bestweather.ui.notification.daily

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import java.time.LocalTime
import java.util.*

class DailyNotificationHelper constructor(private val context: Context) {
    private val alarmManager: AlarmManager

    init {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun getRefreshPendingIntent(dailyPushNotificationDto: DailyPushNotificationDto, flags: Int): PendingIntent {
        val refreshIntent: Intent = Intent(context, DailyPushNotificationReceiver::class.java)
        refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))
        val bundle: Bundle = Bundle()
        bundle.putInt(BundleKey.dtoId.name, dailyPushNotificationDto.id)
        bundle.putString("time", dailyPushNotificationDto.alarmClock)
        bundle.putString("DailyPushNotificationType", dailyPushNotificationDto.notificationType.name)
        refreshIntent.putExtras(bundle)
        return PendingIntent.getBroadcast(
            context, IntentRequestCodes.DAILY_NOTIFICATION.requestCode + dailyPushNotificationDto.id,
            refreshIntent, flags
        )
    }

    fun enablePushNotification(dailyPushNotificationDto: DailyPushNotificationDto) {
        val localTime: LocalTime = LocalTime.parse(dailyPushNotificationDto.alarmClock)
        val alarmCalendar: Calendar = Calendar.getInstance()
        alarmCalendar.set(Calendar.SECOND, 0)
        alarmCalendar.set(Calendar.MILLISECOND, 0)
        alarmCalendar.set(Calendar.HOUR_OF_DAY, localTime.getHour())
        alarmCalendar.set(Calendar.MINUTE, localTime.getMinute())
        val now: Calendar = Calendar.getInstance()
        if (alarmCalendar.before(now)) {
            alarmCalendar.add(Calendar.DATE, 1)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(),
            getRefreshPendingIntent(dailyPushNotificationDto, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        )
    }

    fun disablePushNotification(dailyPushNotificationDto: DailyPushNotificationDto) {
        val pendingIntent: PendingIntent? =
            getRefreshPendingIntent(dailyPushNotificationDto, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun modifyPushNotification(dailyPushNotificationDto: DailyPushNotificationDto) {
        disablePushNotification(dailyPushNotificationDto)
        if (dailyPushNotificationDto.isEnabled) {
            enablePushNotification(dailyPushNotificationDto)
        }
    }

    fun isRepeating(dailyPushNotificationDto: DailyPushNotificationDto): Boolean {
        val pendingIntent: PendingIntent? =
            getRefreshPendingIntent(dailyPushNotificationDto, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        return pendingIntent != null
    }

    fun reStartNotifications(backgroundWorkCallback: BackgroundWorkCallback?) {
        val repository: DailyPushNotificationRepository = DailyPushNotificationRepository.getINSTANCE()
        repository.getAll(object : DbQueryCallback<List<DailyPushNotificationDto?>?>() {
            fun onResultSuccessful(result: List<DailyPushNotificationDto?>) {
                if (result.size > 0) {
                    for (dto: DailyPushNotificationDto in result) {
                        if (dto.isEnabled && !isRepeating(dto)) enablePushNotification(dto)
                    }
                }
                if (backgroundWorkCallback != null) backgroundWorkCallback.onFinished()
            }

            fun onResultNoData() {
                if (backgroundWorkCallback != null) backgroundWorkCallback.onFinished()
            }
        })
    }
}