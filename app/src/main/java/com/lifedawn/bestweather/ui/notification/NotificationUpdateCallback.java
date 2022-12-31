package com.lifedawn.bestweather.ui.notification;

import android.widget.RemoteViews;

public interface NotificationUpdateCallback {
	void updateNotification(RemoteViews remoteViews);
}