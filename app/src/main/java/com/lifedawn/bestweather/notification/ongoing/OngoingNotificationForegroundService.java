package com.lifedawn.bestweather.notification.ongoing;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OngoingNotificationForegroundService extends Service {
	public OngoingNotificationForegroundService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		showNotification();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void showNotification() {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.ForegroundService);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getString(R.string.msg_refreshing_weather_data)).setContentTitle(getString(R.string.msg_refreshing_weather_data))
				.setOnlyAlertOnce(true).setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		startForeground((int) System.currentTimeMillis(), notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String action = intent.getAction();
		final Timer timer = new Timer();

		if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());
			ongoingNotificationHelper.reStartNotification(new Callback() {
				@Override
				public void onResult() {
					stopService();
				}
			});
		} else if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			OngoingNotiViewCreator ongoingNotiViewCreator = new OngoingNotiViewCreator(getApplicationContext(), null);
			ongoingNotiViewCreator.loadSavedPreferences();

			if (ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis() > 0) {
				OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());
				if (!ongoingNotificationHelper.isRepeating()) {
					ongoingNotificationHelper.onSelectedAutoRefreshInterval(ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis());
				}
			}

			ongoingNotiViewCreator.initNotification(new Callback() {
				@Override
				public void onResult() {
					stopService();
				}
			});
		}
		return START_NOT_STICKY;
	}

	private void stopService() {
		stopForeground(true);
		stopSelf();
	}
}