package com.lifedawn.bestweather.notification.always;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Configuration;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.NotificationType;

public class AlwaysNotiJobService extends JobService {
	public AlwaysNotiJobService() {
		Configuration.Builder builder = new Configuration.Builder();
		builder.setJobSchedulerJobIdRange(1000, 2000);
	}

	@Override
	public boolean onStartJob(JobParameters params) {
		PersistableBundle bundle = params.getExtras();
		final String action = bundle.getString("action");

		if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			final AlwaysNotiViewCreator alwaysNotiViewCreator = new AlwaysNotiViewCreator(getApplicationContext(), null);
			alwaysNotiViewCreator.loadSavedPreferences();
			alwaysNotiViewCreator.initNotification(new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(@NonNull Message msg) {
					if (msg.obj != null) {
						String status = (String) msg.obj;
						if (status.equals("finished")) {
							jobFinished(params, false);
							return true;
						}
					}
					return false;
				}
			}));

		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			final boolean enabledAlwaysNotification =
					defaultSharedPreferences.getBoolean(NotificationType.Always.getPreferenceName(), false);

			if (enabledAlwaysNotification) {
				AlwaysNotiViewCreator alwaysNotiViewCreator = new AlwaysNotiViewCreator(getApplicationContext(), null);
				alwaysNotiViewCreator.loadSavedPreferences();
				if (alwaysNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis() > 0) {
					AlwaysNotiHelper alwaysNotiHelper = new AlwaysNotiHelper(getApplicationContext());
					alwaysNotiHelper.onSelectedAutoRefreshInterval(alwaysNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis());
				}

				alwaysNotiViewCreator.initNotification(new Handler(new Handler.Callback() {
					@Override
					public boolean handleMessage(@NonNull Message msg) {
						if (msg.obj != null) {
							String status = (String) msg.obj;
							if (status.equals("finished")) {
								jobFinished(params, false);
								return true;
							}
						}
						return false;
					}
				}));


			}

		}

		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		return false;
	}


}
