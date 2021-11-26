package com.lifedawn.bestweather.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.lifedawn.bestweather.notification.always.AlwaysNotiService;
import com.lifedawn.bestweather.notification.daily.DailyNotiService;

public class NotificationReceiver extends BroadcastReceiver {
	public static final String ACTION_REFRESH_DAILY = "ACTION_REFRESH_DAILY";
	public static final String ACTION_REFRESH_ALWAYS = "ACTION_REFRESH_ALWAYS";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(ACTION_REFRESH_ALWAYS)) {
			Intent serviceIntent = new Intent(context, AlwaysNotiService.class);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(serviceIntent);
			} else {
				context.startService(serviceIntent);
			}
		} else if (action.equals(ACTION_REFRESH_DAILY)) {
			Intent serviceIntent = new Intent(context, DailyNotiService.class);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(serviceIntent);
			} else {
				context.startService(serviceIntent);
			}
		}
	}
}
