package com.lifedawn.bestweather.notification.ongoing;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;

public class OngoingNotificationHelper {
	private Context context;
	private AlarmManager alarmManager;

	public OngoingNotificationHelper(Context context) {
		this.context = context;
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	public void onSelectedAutoRefreshInterval(long millis) {
		cancelAutoRefresh();

		if (millis > 0) {
			Intent refreshIntent = new Intent(context, OngoingNotificationReceiver.class);
			refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NotificationType.Ongoing.getNotificationId(), refreshIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
					millis, pendingIntent);
		}
	}

	public void cancelAutoRefresh() {
		Intent refreshIntent = new Intent(context, OngoingNotificationReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NotificationType.Ongoing.getNotificationId(), refreshIntent
				, PendingIntent.FLAG_NO_CREATE);

		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

	public boolean isRepeating() {
		Intent refreshIntent = new Intent(context, OngoingNotificationReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NotificationType.Ongoing.getNotificationId(), refreshIntent
				, PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent != null) {
			return true;
		} else {
			return false;
		}
	}

	public void reStartNotification(Callback callback) {
		SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(context);

		final boolean enabledOngoingNotification = sharedPreferences.getBoolean(NotificationType.Ongoing.getPreferenceName(), false);

		if (enabledOngoingNotification) {
			NotificationHelper notificationHelper = new NotificationHelper(context);
			final boolean active = notificationHelper.activeNotification(NotificationType.Ongoing.getNotificationId());

			OngoingNotiViewCreator ongoingNotiViewCreator = new OngoingNotiViewCreator(context, null);
			ongoingNotiViewCreator.loadPreferences();

			if (ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis() > 0 && !isRepeating()) {
				onSelectedAutoRefreshInterval(ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis());
			}

			if (active) {
				callback.onResult();
			} else {
				ongoingNotiViewCreator.initNotification(callback);
			}
		} else {
			callback.onResult();
		}
	}

}
