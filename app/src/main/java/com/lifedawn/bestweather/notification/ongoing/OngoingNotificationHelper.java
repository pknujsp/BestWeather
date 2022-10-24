package com.lifedawn.bestweather.notification.ongoing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
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
					PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + millis,
					millis, pendingIntent);
		}
	}

	public void cancelAutoRefresh() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NotificationType.Ongoing.getNotificationId(), new Intent(context, OngoingNotificationReceiver.class)
				, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE);

		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

	public boolean isRepeating() {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NotificationType.Ongoing.getNotificationId(), new Intent(context, OngoingNotificationReceiver.class)
				, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE);
		if (pendingIntent != null) {
			return true;
		} else {
			return false;
		}
	}

	public void reStartNotification(@NonNull BackgroundWorkCallback callback) {
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
				callback.onFinished();
			} else {
				ongoingNotiViewCreator.initNotification(callback);
			}
		} else {
			callback.onFinished();
		}
	}

}
