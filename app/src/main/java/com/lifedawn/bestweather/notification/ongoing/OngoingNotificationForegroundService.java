package com.lifedawn.bestweather.notification.ongoing;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OngoingNotificationForegroundService extends Service {
	private Timer timer;
	private OngoingNotiViewCreator ongoingNotiViewCreator;

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
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getString(R.string.updatingNotification)).setContentTitle(getString(R.string.updatingNotification))
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
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				timer = null;
				if (ongoingNotiViewCreator != null) {
					ongoingNotiViewCreator.forceFailedNotification(RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
				}

				stopService();
			}
		}, TimeUnit.SECONDS.toMillis(25L));

		if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());
			ongoingNotificationHelper.reStartNotification(new Callback() {
				@Override
				public void onResult() {
					stopService();
				}
			});
		} else if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			ongoingNotiViewCreator = new OngoingNotiViewCreator(getApplicationContext(), null);
			ongoingNotiViewCreator.loadSavedPreferences();

			if (ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis() > 0) {
				OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());
				if (!ongoingNotificationHelper.isRepeating()) {
					ongoingNotificationHelper.onSelectedAutoRefreshInterval(ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis());
				}
			}

			if (ongoingNotiViewCreator.getNotificationDataObj().getLocationType() == LocationType.CurrentLocation) {
				FusedLocation.getInstance(getApplicationContext()).startForeground(OngoingNotificationForegroundService.this);
			}

			ongoingNotiViewCreator.initNotification(new Callback() {
				@Override
				public void onResult() {
					if (ongoingNotiViewCreator.getNotificationDataObj().getLocationType() == LocationType.CurrentLocation) {
						NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
						notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
					}
					stopService();
				}
			});
		}
		return START_NOT_STICKY;
	}

	private void stopService() {
		if (timer != null) {
			timer.cancel();
			timer = null;

			NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
			if (notificationHelper.activeNotification(NotificationType.Location.getNotificationId())) {
				notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
			}
		}

		stopForeground(true);
		stopSelf();
	}
}