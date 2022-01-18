package com.lifedawn.bestweather.notification.daily;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.AlarmManagerCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.AlarmReceiver;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;

import java.time.LocalTime;
import java.util.Calendar;

public class DailyNotiHelper {
	private Context context;
	private AlarmManager alarmManager;

	public DailyNotiHelper(Context context) {
		this.context = context;
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	public PendingIntent getRefreshPendingIntent(DailyPushNotificationDto dailyPushNotificationDto) {
		Intent refreshIntent = new Intent(context, DailyPushNotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putInt(BundleKey.dtoId.name(), dailyPushNotificationDto.getId());
		bundle.putString("time", dailyPushNotificationDto.getAlarmClock());
		bundle.putString("DailyPushNotificationType", dailyPushNotificationDto.getNotificationType().name());

		refreshIntent.putExtras(bundle);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, dailyPushNotificationDto.getId() + 6000, refreshIntent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	public void enablePushNotification(DailyPushNotificationDto dailyPushNotificationDto) {
		LocalTime localTime = LocalTime.parse(dailyPushNotificationDto.getAlarmClock());

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
		calendar.set(Calendar.MINUTE, localTime.getMinute());
		calendar.set(Calendar.SECOND, 0);

		if (calendar.compareTo(Calendar.getInstance()) <= 0) {
			calendar.add(Calendar.DATE, 1);
		}

		alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getRefreshPendingIntent(dailyPushNotificationDto));
	}

	public void disablePushNotification(int id) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id + 6000, new Intent(context,
				DailyPushNotificationReceiver.class), Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
				PendingIntent.FLAG_UPDATE_CURRENT);
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
		}
	}

	public void modifyPushNotification(DailyPushNotificationDto dailyPushNotificationDto) {
		disablePushNotification(dailyPushNotificationDto.getId());

		if (dailyPushNotificationDto.isEnabled()) {
			enablePushNotification(dailyPushNotificationDto);
		}
	}

}
