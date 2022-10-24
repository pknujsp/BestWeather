package com.lifedawn.bestweather.notification.ongoing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.lifedawn.bestweather.notification.ongoing.work.OngoingNotificationListenableWorker;

public class OngoingNotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action != null) {
			Data data = new Data.Builder().putString("action", action).build();
			final String tag = "ongoing_notification";

			OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(OngoingNotificationListenableWorker.class)
					.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
					.setInputData(data)
					.addTag(tag)
					.build();

			WorkManager workManager = WorkManager.getInstance(context);
			workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request);
		}
	}
}
