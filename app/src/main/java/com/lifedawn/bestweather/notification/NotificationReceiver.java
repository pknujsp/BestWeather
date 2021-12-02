package com.lifedawn.bestweather.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.always.AlwaysNotiViewCreator;
import com.lifedawn.bestweather.notification.daily.DailyNotiViewCreator;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationReceiver extends BroadcastReceiver {
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	public void onReceive(Context context, Intent intent) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				String action = intent.getAction();
				Log.e("NotificationReceiver", action);

				if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
					Bundle bundle = intent.getExtras();
					NotificationType notificationType = NotificationType.valueOf(bundle.getString(NotificationType.class.getName()));
					Log.e("NotificationReceiver", notificationType.name());

					if (notificationType == NotificationType.Always) {
						AlwaysNotiViewCreator alwaysNotiViewCreator = new AlwaysNotiViewCreator(context, null);
						alwaysNotiViewCreator.loadPreferences();
						alwaysNotiViewCreator.initNotification();
					} else if (notificationType == NotificationType.Daily) {
						DailyNotiViewCreator dailyNotiViewCreator = new DailyNotiViewCreator(context);
						dailyNotiViewCreator.loadPreferences();
						dailyNotiViewCreator.initNotification();
					}
				} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
					SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

					final boolean enabledAlwaysNotification =
							defaultSharedPreferences.getBoolean(NotificationType.Always.getPreferenceName(), false);
					final boolean enabledDailyNotification =
							defaultSharedPreferences.getBoolean(NotificationType.Daily.getPreferenceName(), false);

					if (enabledAlwaysNotification) {
						AlwaysNotiViewCreator alwaysNotiViewCreator = new AlwaysNotiViewCreator(context, null);
						alwaysNotiViewCreator.loadPreferences();
						alwaysNotiViewCreator.initNotification();

						if (alwaysNotiViewCreator.getUpdateInterval() > 0) {
							Intent refreshIntent = new Intent(context, NotificationReceiver.class);
							refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
							Bundle bundle = new Bundle();
							bundle.putString(NotificationType.class.getName(), NotificationType.Always.name());

							refreshIntent.putExtras(bundle);
							PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 11, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

							AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
							alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
									alwaysNotiViewCreator.getUpdateInterval(), pendingIntent);
						}
					}

					if (enabledDailyNotification) {
						DailyNotiViewCreator dailyNotiViewCreator = new DailyNotiViewCreator(context);
						dailyNotiViewCreator.loadPreferences();

						LocalTime localTime = LocalTime.parse(dailyNotiViewCreator.getAlarmClock());

						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
						calendar.set(Calendar.MINUTE, localTime.getMinute());
						calendar.set(Calendar.SECOND, 0);

						Intent refreshIntent = new Intent(context, NotificationReceiver.class);
						refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
						Bundle bundle = new Bundle();
						bundle.putString(NotificationType.class.getName(), NotificationType.Daily.name());

						refreshIntent.putExtras(bundle);
						PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 20, refreshIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);

						AlarmManager alarmManager =
								(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
						alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
								AlarmManager.INTERVAL_DAY, pendingIntent);
					}
				}
			}
		});

	}
}
