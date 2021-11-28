package com.lifedawn.bestweather.notification;

public enum NotificationType {
	Always("ALWAYS_NOTI_SHARED_PREFERENCES"), Daily("DAILY_NOTI_SHARED_PREFERENCES");

	private final String preferenceName;

	NotificationType(String preferenceName) {
		this.preferenceName = preferenceName;
	}

	public String getPreferenceName() {
		return preferenceName;
	}
}
