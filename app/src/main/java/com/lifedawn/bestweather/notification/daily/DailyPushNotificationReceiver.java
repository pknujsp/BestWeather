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
import com.lifedawn.bestweather.notification.daily.work.DailyNotificationListenableWorker;

public class DailyPushNotificationReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action != null) {
			Bundle arguments = intent.getExtras();
			final int id = arguments.getInt(BundleKey.dtoId.name());

			Data data = new Data.Builder()
					.putString("DailyPushNotificationType", arguments.getString(
							"DailyPushNotificationType"))
					.putInt(BundleKey.dtoId.name(), id)
					.putString("action", action)
					.build();

			final String tag = DailyNotificationListenableWorker.class.getName() + action;

			OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DailyNotificationListenableWorker.class)
					.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
					.setInputData(data)
					.addTag(tag)
					.build();

			WorkManager workManager = WorkManager.getInstance(context);
			workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request);
		}
	}


}