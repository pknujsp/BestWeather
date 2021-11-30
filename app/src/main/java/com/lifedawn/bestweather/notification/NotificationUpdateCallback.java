package com.lifedawn.bestweather.notification;

import android.widget.RemoteViews;

public interface NotificationUpdateCallback {
	void updateNotification(RemoteViews remoteViews);
}