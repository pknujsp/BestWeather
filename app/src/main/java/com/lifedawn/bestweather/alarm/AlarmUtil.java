package com.lifedawn.bestweather.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.app.AlarmManagerCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.alarmnotifications.RepeatAlarmConstants;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.room.dto.AlarmDto;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class AlarmUtil {
	private AlarmUtil() {
	}

	/**
	 * @param days 023 == SunTueWed
	 */
	public static Set<Integer> parseDays(String days) {
		Set<Integer> dayList = new HashSet<>();
		for (int i = 0; i < days.length(); i++) {
			int d = Character.getNumericValue(days.charAt(i));
			dayList.add(d);
		}
		return dayList;
	}

	public static String parseRepeat(Context context, int interval, int count) {
		String val = interval + context.getString(R.string.evenryMinutes) + ", " +
				(count == Integer.MAX_VALUE ? context.getString(R.string.endless) : count + context.getString(R.string.count))
				+ " " + context.getString(R.string.repeat);
		return val;
	}

	public static void cancelAlarm(Context context, AlarmDto alarmDto) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(PendingIntent.getBroadcast(context, 10000 + alarmDto.getId(), new Intent(context, AlarmReceiver.class),
				0));
	}

	public static void modifyAlarm(Context context, AlarmDto alarmDto) {
		if (alarmDto.getEnabled() == 1) {
			cancelAlarm(context, alarmDto);
			registerAlarm(context, alarmDto);
		}
	}

	public static void registerAlarm(Context context, AlarmDto alarmDto) {
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		alarmIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_ALARM));
		Bundle bundle = new Bundle();
		bundle.putInt(BundleKey.dtoId.name(), alarmDto.getId());
		alarmIntent.putExtras(bundle);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10000 + alarmDto.getId(), alarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar calendar = Calendar.getInstance();
		Calendar now = (Calendar) calendar.clone();
		LocalTime localTime = LocalTime.parse(alarmDto.getAlarmTime());

		calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
		calendar.set(Calendar.MINUTE, localTime.getMinute());
		calendar.set(Calendar.SECOND, 0);

		if (calendar.compareTo(now) <= 0) {
			calendar.add(Calendar.DATE, 1);
		}

		cancelAlarm(context, alarmDto);
		AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}

	public static void registerRepeatAlarm(Context context, AlarmDto alarmDto) {
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		alarmIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_ALARM));
		Bundle bundle = new Bundle();
		bundle.putInt(BundleKey.dtoId.name(), alarmDto.getId());
		alarmIntent.putExtras(bundle);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10000 + alarmDto.getId(), alarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.MINUTE, alarmDto.getRepeatInterval());

		AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}

	public static void clearRepeatCount(Context context, int alarmDtoId) {
		SharedPreferences repeatAlarmLogSharedPreferences =
				context.getSharedPreferences(RepeatAlarmConstants.repeatAlarmLog.name(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = repeatAlarmLogSharedPreferences.edit();
		editor.putInt(RepeatAlarmConstants.getPreferenceName(alarmDtoId), 0).apply();
	}

	public static int upRepeatCount(Context context, int alarmDtoId) {
		SharedPreferences repeatAlarmLogSharedPreferences =
				context.getSharedPreferences(RepeatAlarmConstants.repeatAlarmLog.name(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = repeatAlarmLogSharedPreferences.edit();

		String id = RepeatAlarmConstants.getPreferenceName(alarmDtoId);
		int repeatCount = repeatAlarmLogSharedPreferences.getInt(id, 0);
		editor.putInt(id, ++repeatCount);
		editor.apply();

		return repeatCount;
	}
}
