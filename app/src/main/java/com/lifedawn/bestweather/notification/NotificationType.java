package com.lifedawn.bestweather.notification;

import java.io.Serializable;

public enum NotificationType implements Serializable {
	Always("ALWAYS_NOTI_SHARED_PREFERENCES", 1000, "1001"), Daily("DAILY_NOTI_SHARED_PREFERENCES", 2000, "2001"), Alarm("", 3000, "3001");

	private final String preferenceName;
	private final int notificationId;
	private final String channelId;

	NotificationType(String preferenceName, int notificationId, String channelId) {
		this.preferenceName = preferenceName;
		this.notificationId = notificationId;
		this.channelId = channelId;
	}

	public String getPreferenceName() {
		return preferenceName;
	}

	public int getNotificationId() {
		return notificationId;
	}

	public String getChannelId() {
		return channelId;
	}
}
