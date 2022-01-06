package com.lifedawn.bestweather.notification.daily;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.notification.always.AlwaysNotiJobService;
import com.lifedawn.bestweather.notification.daily.viewcreator.AbstractDailyNotiViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FifthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FirstDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FourthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.SecondDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.ThirdDailyNotificationViewCreator;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyPushNotificationReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (intent.getExtras() == null) {
			return;
		}

		Bundle arguments = intent.getExtras();
		final int id = arguments.getInt(BundleKey.dtoId.name());

		PersistableBundle persistableBundle = new PersistableBundle();
		persistableBundle.putString("DailyPushNotificationType", arguments.getString(
				"DailyPushNotificationType"));
		persistableBundle.putString("time", arguments.getString("time"));
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