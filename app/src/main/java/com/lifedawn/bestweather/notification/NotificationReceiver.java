package com.lifedawn.bestweather.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.always.AlwaysNotiViewCreator;
import com.lifedawn.bestweather.notification.daily.DailyNotiViewCreator;

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
					NotificationType notificationType = (NotificationType) intent.getSerializableExtra(NotificationType.class.getName());

					if (notificationType == NotificationType.Always) {
						AlwaysNotiViewCreator alwaysNotiViewCreator = new AlwaysNotiViewCreator(context, null);
						alwaysNotiViewCreator.loadPreferences();
						alwaysNotiViewCreator.initNotification();
					} else if (notificationType == NotificationType.Daily) {
						DailyNotiViewCreator dailyNotiViewCreator = new DailyNotiViewCreator(context);
						dailyNotiViewCreator.loadPreferences();
						dailyNotiViewCreator.initNotification();
					}
				}
			}
		});

	}
}
