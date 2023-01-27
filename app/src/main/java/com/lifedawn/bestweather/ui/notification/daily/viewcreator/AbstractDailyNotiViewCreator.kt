package com.lifedawn.bestweather.ui.notification.daily.viewcreator

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.IntentUtil.getAppIntent
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.ui.notification.NotificationHelper
import com.lifedawn.bestweather.ui.notification.NotificationHelper.NotificationObj
import com.lifedawn.bestweather.ui.notification.NotificationType
import java.time.ZoneId
import java.time.format.DateTimeFormatter

abstract class AbstractDailyNotiViewCreator constructor(protected var context: Context) {
    protected val refreshDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm")
    protected var callback: BackgroundWorkCallback? = null
    protected var zoneId: ZoneId? = null
    abstract fun setTempDataViews(remoteViews: RemoteViews)
    abstract fun createRemoteViews(needTempData: Boolean): RemoteViews
    fun setBackgroundCallback(callback: BackgroundWorkCallback?) {
        this.callback = callback
    }

    fun makeNotification(remoteViews: RemoteViews?, notificationDtoId: Int) {
        val notificationHelper: NotificationHelper = NotificationHelper(context)
        val notificationObj: NotificationObj? = notificationHelper.createNotification(NotificationType.Daily)
        val builder: NotificationCompat.Builder? = notificationObj.getNotificationBuilder()
        builder!!.setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher_round).setContentIntent(
            PendingIntent.getActivity(
                context,
                notificationObj.getNotificationId(),
                getAppIntent(context), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        )
            .setCustomContentView(remoteViews).setCustomBigContentView(remoteViews)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
        val notification: Notification = notificationObj.getNotificationBuilder().build()
        notificationManager.notify(notificationObj.getNotificationId() + notificationDtoId, notification)
        if (callback != null) {
            callback!!.onFinished()
        }
    }

    fun makeFailedNotification(notificationDtoId: Int, failText: String?) {
        val notificationHelper: NotificationHelper = NotificationHelper(context)
        val notificationObj: NotificationObj? = notificationHelper.createNotification(NotificationType.Daily)
        val builder: NotificationCompat.Builder? = notificationObj.getNotificationBuilder()
        builder!!.setSmallIcon(R.mipmap.ic_launcher_round).setAutoCancel(true).setContentText(failText).setContentTitle(
            context.getString(R.string.errorNotification)
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
        val notification: Notification = notificationObj.getNotificationBuilder().build()
        notificationManager.notify(notificationObj.getNotificationId() + notificationDtoId, notification)
        if (callback != null) {
            callback!!.onFinished()
        }
    }

    abstract val requestWeatherDataTypeSet: Set<WeatherDataType>
    open fun setResultViews(
        remoteViews: RemoteViews, dailyPushNotificationDto: DailyPushNotificationDto, weatherProviderTypeSet: Set<WeatherProviderType?>?,
        multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
        weatherDataTypeSet: Set<WeatherDataType?>?
    ) {
    }
}