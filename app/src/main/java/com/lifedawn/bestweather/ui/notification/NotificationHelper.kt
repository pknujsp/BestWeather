package com.lifedawn.bestweather.ui.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.main.view.MainActivity

class NotificationHelper constructor(private val context: Context) {
    private val notificationManager: NotificationManager

    init {
        notificationManager = context.getSystemService(NotificationManager::class.java)
    }

    fun createNotificationChannel(notificationObj: NotificationObj) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(notificationObj.channelId) == null) {
                val channel: NotificationChannel = NotificationChannel(
                    notificationObj.channelId, notificationObj.channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.setDescription(notificationObj.channelDescription)
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC)
                if ((notificationObj.notificationType == NotificationType.Ongoing) || (
                            notificationObj.notificationType == NotificationType.Location) || (
                            notificationObj.notificationType == NotificationType.ForegroundService)
                ) {
                    channel.enableVibration(false)
                    channel.setVibrationPattern(null)
                    channel.enableLights(false)
                    channel.setShowBadge(false)
                    channel.setSound(null, null)
                    channel.setImportance(NotificationManager.IMPORTANCE_LOW)
                } else if (notificationObj.notificationType == NotificationType.Daily) {
                    channel.setImportance(NotificationManager.IMPORTANCE_HIGH)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun createNotification(notificationType: NotificationType): NotificationObj {
        val notificationObj: NotificationObj = getNotificationObj(notificationType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationObj)
        }
        val clickIntent: Intent = Intent(context, MainActivity::class.java)
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, (notificationObj.channelId)!!)
        builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher_round)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationObj.notificationBuilder = builder
        return notificationObj
    }

    fun cancelNotification(notificationId: Int) {
        val activeNotifications: Array<StatusBarNotification> = notificationManager.getActiveNotifications()
        for (activeNotification: StatusBarNotification in activeNotifications) {
            if (activeNotification.getId() == notificationId) {
                notificationManager.cancel(notificationId)
                break
            }
        }
    }

    fun activeNotification(notificationId: Int): Boolean {
        val activeNotifications: Array<StatusBarNotification> = notificationManager.getActiveNotifications()
        for (activeNotification: StatusBarNotification in activeNotifications) {
            if (activeNotification.getId() == notificationId) {
                return true
            }
        }
        return false
    }

    fun getNotificationObj(notificationType: NotificationType): NotificationObj {
        if (notificationType == NotificationType.Ongoing) {
            return NotificationObj(
                context.getString(R.string.notificationAlwaysChannelName),
                context.getString(R.string.notificationAlwaysChannelDescription), notificationType
            )
        } else if (notificationType == NotificationType.Daily) {
            return NotificationObj(
                context.getString(R.string.notificationDailyChannelName),
                context.getString(R.string.notificationDailyChannelDescription), notificationType
            )
        } else if (notificationType == NotificationType.Alarm) {
            return NotificationObj(
                context.getString(R.string.notificationAlarmChannelName),
                context.getString(R.string.notificationAlarmChannelDescription), notificationType
            )
        } else if (notificationType == NotificationType.ForegroundService) {
            return NotificationObj(
                context.getString(R.string.notificationForegroundServiceChannelName),
                context.getString(R.string.notificationForegroundServiceChannelDescription), notificationType
            )
        } else {
            return NotificationObj(
                context.getString(R.string.notificationLocationChannelName),
                context.getString(R.string.notificationLocationChannelDescription), notificationType
            )
        }
    }

    class NotificationObj constructor(channelName: String, channelDescription: String, notificationType: NotificationType) {
        var notificationBuilder: NotificationCompat.Builder? = null
        val notificationId: Int
        val channelId: String?
        val channelName: String
        val channelDescription: String
        val notificationType: NotificationType

        init {
            notificationId = notificationType.getNotificationId()
            channelId = notificationType.getChannelId()
            this.channelName = channelName
            this.channelDescription = channelDescription
            this.notificationType = notificationType
        }
    }
}