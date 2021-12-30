package com.lifedawn.bestweather.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.main.MainActivity;

public class NotificationHelper {
	private Context context;

	public NotificationHelper(Context context) {
		this.context = context;
	}

	private void createNotificationChannel(NotificationObj notificationObj) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(notificationObj.channelId, notificationObj.channelName,
					NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription(notificationObj.channelDescription);

			NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}

	public NotificationObj createNotification(NotificationType notificationType) {
		final NotificationObj notificationObj = getNotificationObj(notificationType);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel(notificationObj);
		}

		Intent clickIntent = new Intent(context, MainActivity.class);
		clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationObj.channelId)
				.setSmallIcon(R.drawable.temp_icon)
				.setContentIntent(pendingIntent);

		notificationObj.setNotificationBuilder(builder);
		return notificationObj;
	}

	public void cancelNotification(int notificationId) {
		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

		for (StatusBarNotification activeNotification : activeNotifications) {
			if (activeNotification.getId() == notificationId) {
				notificationManager.cancel(notificationId);
				break;
			}
		}

	}

	private NotificationObj getNotificationObj(NotificationType notificationType) {
		if (notificationType == NotificationType.Always) {
			return new NotificationObj(NotificationType.Always.getNotificationId(),
					NotificationType.Always.getChannelId(), context.getString(R.string.notificationAlwaysChannelName),
					context.getString(R.string.notificationAlwaysChannelDescription));
		} else if (notificationType == NotificationType.Daily) {
			return new NotificationObj(NotificationType.Daily.getNotificationId(),
					NotificationType.Daily.getChannelId(), context.getString(R.string.notificationDailyChannelName),
					context.getString(R.string.notificationDailyChannelDescription));
		} else {
			return new NotificationObj(NotificationType.Alarm.getNotificationId(),
					NotificationType.Alarm.getChannelId(), context.getString(R.string.notificationAlarmChannelName),
					context.getString(R.string.notificationAlarmChannelDescription));
		}
	}

	public static class NotificationObj {
		private NotificationCompat.Builder notificationBuilder;
		final int notificationId;
		final String channelId;
		final String channelName;
		final String channelDescription;

		public NotificationObj(int notificationId, String channelId, String channelName, String channelDescription) {
			this.notificationId = notificationId;
			this.channelId = channelId;
			this.channelName = channelName;
			this.channelDescription = channelDescription;
		}

		public NotificationCompat.Builder getNotificationBuilder() {
			return notificationBuilder;
		}

		public void setNotificationBuilder(NotificationCompat.Builder notificationBuilder) {
			this.notificationBuilder = notificationBuilder;
		}

		public int getNotificationId() {
			return notificationId;
		}

		public String getChannelId() {
			return channelId;
		}

		public String getChannelName() {
			return channelName;
		}

		public String getChannelDescription() {
			return channelDescription;
		}
	}
}
