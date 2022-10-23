package com.lifedawn.bestweather.notification.daily;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.main.MyApplication;

public class DailyPushNotificationReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		MyApplication.loadValueUnits(context, false);

		if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			DailyNotificationHelper notiHelper = new DailyNotificationHelper(context);
			notiHelper.reStartNotifications();
		} else {
			Bundle arguments = intent.getExtras();
			final int id = arguments.getInt(BundleKey.dtoId.name());

			Data data = new Data.Builder()
					.putString("DailyPushNotificationType", arguments.getString(
							"DailyPushNotificationType"))
					.putInt(BundleKey.dtoId.name(), id)
					.putString("action", action)
					.build();

			OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DailyNotificationWorker.class)
					.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
					.setInputData(data)
					.addTag(DailyNotificationWorker.class.getName())
					.build();

			WorkManager workManager = WorkManager.getInstance(context);
			workManager.enqueueUniqueWork(DailyNotificationWorker.class.getName(), ExistingWorkPolicy.KEEP, request);
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