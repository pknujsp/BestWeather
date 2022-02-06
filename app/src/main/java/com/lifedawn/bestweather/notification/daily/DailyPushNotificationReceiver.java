package com.lifedawn.bestweather.notification.daily;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.lifedawn.bestweather.commons.enums.BundleKey;

public class DailyPushNotificationReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			DailyNotificationHelper notiHelper = new DailyNotificationHelper(context);
			notiHelper.reStartNotifications();
		} else {
			Bundle arguments = intent.getExtras();
			final int id = arguments.getInt(BundleKey.dtoId.name());

			PersistableBundle persistableBundle = new PersistableBundle();
			persistableBundle.putString("DailyPushNotificationType", arguments.getString(
					"DailyPushNotificationType"));
			persistableBundle.putInt(BundleKey.dtoId.name(), id);
			persistableBundle.putString("action", action);

			JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

			JobInfo jobInfo = new JobInfo.Builder(10000 + id, new ComponentName(context, DailyPushNotificationJobService.class))
					.setMinimumLatency(0)
					.setOverrideDeadline(20000)
					.setExtras(persistableBundle)
					.build();
			jobScheduler.schedule(jobInfo);
		}
	}


}