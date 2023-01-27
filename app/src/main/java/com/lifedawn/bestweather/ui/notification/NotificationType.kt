package com.lifedawn.bestweather.ui.notification

import java.io.Serializable

enum class NotificationType(val preferenceName: String, val notificationId: Int, val channelId: String) : Serializable {
    Ongoing("ALWAYS_NOTI_SHARED_PREFERENCES", 1000, "1001"), Daily("DAILY_NOTI_SHARED_PREFERENCES", 2000, "2001"), Alarm("", 3000, "3001"),
    Location("", 4000, "4001"), ForegroundService("", 5000, "5001");

}