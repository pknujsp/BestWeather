package com.lifedawn.bestweather.notification.daily;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.NotificationReceiver;
import com.lifedawn.bestweather.notification.NotificationType;

import java.time.LocalTime;
import java.util.Calendar;

public class DailyNotiHelper {
	private Context context;
	private AlarmManager alarmManager;

	public DailyNotiHelper(Context context) {
		this.context = context;
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	public PendingIntent getRefreshPendingIntent() {
		Intent refreshIntent = new Intent(context, NotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putString(NotificationType.class.getName(), NotificationType.Daily.name());

		refreshIntent.putExtras(bundle);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 20, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	public void setAlarm(String alarmClock) {
		LocalTime localTime = LocalTime.parse(alarmClock);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
		calendar.set(Calendar.MINUTE, localTime.getMinute());
		calendar.set(Calendar.SECOND, 0);

		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, getRefreshPendingIntent());
	}

	public void cancelAlarm() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 20, new Intent(context, NotificationReceiver.class), 0);
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

}
