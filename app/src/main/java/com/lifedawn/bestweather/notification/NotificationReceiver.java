package com.lifedawn.bestweather.notification;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.always.AlwaysNotiHelper;
import com.lifedawn.bestweather.notification.always.AlwaysNotiViewCreator;
import com.lifedawn.bestweather.notification.daily.DailyNotiHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

		PersistableBundle persistableBundle = new PersistableBundle();
		persistableBundle.putString("action", action);

		JobInfo jobInfo = new JobInfo.Builder(1001, new ComponentName(context, AlwaysNotiJobService.class))
				.setMinimumLatency(100)
				.setOverrideDeadline(1000)
				.setExtras(persistableBundle)
				.build();
		jobScheduler.schedule(jobInfo);

	}

}
