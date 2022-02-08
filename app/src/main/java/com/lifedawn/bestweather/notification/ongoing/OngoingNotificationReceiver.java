package com.lifedawn.bestweather.notification.ongoing;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

public class OngoingNotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(context);
			ongoingNotificationHelper.reStartNotification();
		} else {
			JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

			PersistableBundle persistableBundle = new PersistableBundle();
			persistableBundle.putString("action", action);

			JobInfo jobInfo = new JobInfo.Builder(1001, new ComponentName(context, OngoingNotiJobService.class))
					.setMinimumLatency(0)
					.setOverrideDeadline(1000)
					.setExtras(persistableBundle)
					.build();
			jobScheduler.schedule(jobInfo);
		}
	}

}
