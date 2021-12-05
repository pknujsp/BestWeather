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
import com.lifedawn.bestweather.notification.always.AlwaysNotiHelper;
import com.lifedawn.bestweather.notification.always.AlwaysNotiViewCreator;
import com.lifedawn.bestweather.notification.daily.DailyNotiHelper;
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
							AlwaysNotiHelper alwaysNotiHelper = new AlwaysNotiHelper(context);
							alwaysNotiHelper.onSelectedAutoRefreshInterval(alwaysNotiViewCreator.getUpdateInterval());
						}
					}

					if (enabledDailyNotification) {
						DailyNotiViewCreator dailyNotiViewCreator = new DailyNotiViewCreator(context);
						dailyNotiViewCreator.loadPreferences();
						DailyNotiHelper dailyNotiHelper = new DailyNotiHelper(context);
						dailyNotiHelper.setAlarm(dailyNotiViewCreator.getAlarmClock());
					}
				}
			}
		});

	}
}
