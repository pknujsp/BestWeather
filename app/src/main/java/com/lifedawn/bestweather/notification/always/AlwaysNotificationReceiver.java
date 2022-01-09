package com.lifedawn.bestweather.notification.always;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

import com.lifedawn.bestweather.notification.always.AlwaysNotiJobService;

public class AlwaysNotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

		PersistableBundle persistableBundle = new PersistableBundle();
		persistableBundle.putString("action", action);

		JobInfo jobInfo = new JobInfo.Builder(1001, new ComponentName(context, AlwaysNotiJobService.class))
				.setMinimumLatency(0)
				.setOverrideDeadline(1000)
				.setExtras(persistableBundle)
				.build();
		jobScheduler.schedule(jobInfo);
	}

}
