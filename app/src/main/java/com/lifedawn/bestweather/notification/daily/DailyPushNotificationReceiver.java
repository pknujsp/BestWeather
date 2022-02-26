package com.lifedawn.bestweather.notification.daily;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationForegroundService;

public class DailyPushNotificationReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		MyApplication.loadValueUnits(context);

		if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			DailyNotificationHelper notiHelper = new DailyNotificationHelper(context);
			notiHelper.reStartNotifications();
		} else {
			Bundle arguments = intent.getExtras();
			final int id = arguments.getInt(BundleKey.dtoId.name());

			Bundle bundle = new Bundle();
			bundle.putString("DailyPushNotificationType", arguments.getString(
					"DailyPushNotificationType"));
			bundle.putInt(BundleKey.dtoId.name(), id);
			bundle.putString("action", action);

			startService(context, action, bundle);
		}
	}

	protected boolean isServiceRunning(Context context) {
		final String serviceName = DailyNotificationForegroundService.class.getName();
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	protected void startService(Context context, String action, @Nullable Bundle bundle) {
		if (isServiceRunning(context)) {
			Toast.makeText(context, R.string.runningUpdateService, Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent(context, DailyNotificationForegroundService.class);
			intent.setAction(action);
			if (bundle != null) {
				intent.putExtras(bundle);
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(intent);
			} else {
				context.startService(intent);
			}
		}
	}

}