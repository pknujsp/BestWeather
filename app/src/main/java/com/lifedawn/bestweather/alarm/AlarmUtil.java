package com.lifedawn.bestweather.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lifedawn.bestweather.R;
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
		LocalTime localTime = LocalTime.parse(alarmDto.getAlarmTime());
		calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
		calendar.set(Calendar.MINUTE, localTime.getMinute());
		calendar.set(Calendar.SECOND, 0);

		cancelAlarm(context, alarmDto);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
	}
}
