package com.lifedawn.bestweather.alarm.alarmnotifications;

public enum RepeatAlarmConstants {
	repeatAlarmLog, alarmId;

	public static String getPreferenceName(int alarmId) {
		return repeatAlarmLog.name() + "_" + alarmId;
	}
}
