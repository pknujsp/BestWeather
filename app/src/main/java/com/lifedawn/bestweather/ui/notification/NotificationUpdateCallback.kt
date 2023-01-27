package com.lifedawn.bestweather.ui.notification

import android.widget.RemoteViews

open interface NotificationUpdateCallback {
    fun updateNotification(remoteViews: RemoteViews?)
}