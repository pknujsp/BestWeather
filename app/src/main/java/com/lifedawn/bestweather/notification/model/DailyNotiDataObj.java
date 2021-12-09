package com.lifedawn.bestweather.notification.model;

public class DailyNotiDataObj extends NotificationDataObj {
	private String alarmClock;

	public String getAlarmClock() {
		return alarmClock;
	}

	public DailyNotiDataObj setAlarmClock(String alarmClock) {
		this.alarmClock = alarmClock;
		return this;
	}
}
