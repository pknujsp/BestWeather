package com.lifedawn.bestweather.notification;

import android.app.Notification;
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
	private NotificationManager notificationManager;

	public NotificationHelper(Context context) {
		this.context = context;
		this.notificationManager = context.getSystemService(NotificationManager.class);
	}

	public void createNotificationChannel(NotificationObj notificationObj) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (notificationManager.getNotificationChannel(notificationObj.channelId) == null) {
				NotificationChannel channel = new NotificationChannel(notificationObj.channelId, notificationObj.channelName,
						NotificationManager.IMPORTANCE_DEFAULT);
				channel.setDescription(notificationObj.channelDescription);
				channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

				if (notificationObj.getNotificationType() == NotificationType.Ongoing ||
						notificationObj.getNotificationType() == NotificationType.Location ||
						notificationObj.getNotificationType() == NotificationType.ForegroundService) {
					channel.enableVibration(false);
					channel.setVibrationPattern(null);
					channel.enableLights(false);
					channel.setShowBadge(false);

					channel.setSound(null, null);
					channel.setImportance(NotificationManager.IMPORTANCE_LOW);
				} else if (notificationObj.getNotificationType() == NotificationType.Daily) {
					channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
				}

				notificationManager.createNotificationChannel(channel);
			}
		}
	}

	public NotificationObj createNotification(NotificationType notificationType) {
		final NotificationObj notificationObj = getNotificationObj(notificationType);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel(notificationObj);
		}

		Intent clickIntent = new Intent(context, MainActivity.class);
		clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), clickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationObj.channelId);

		builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher_round)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

		notificationObj.setNotificationBuilder(builder);
		return notificationObj;
	}

	public void cancelNotification(int notificationId) {
		StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

		for (StatusBarNotification activeNotification : activeNotifications) {
			if (activeNotification.getId() == notificationId) {
				notificationManager.cancel(notificationId);
				break;
			}
		}
	}

	public boolean activeNotification(int notificationId) {
		StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

		for (StatusBarNotification activeNotification : activeNotifications) {
			if (activeNotification.getId() == notificationId) {
				return true;
			}
		}
		return false;
	}

	public NotificationObj getNotificationObj(NotificationType notificationType) {
		if (notificationType == NotificationType.Ongoing) {
			return new NotificationObj(context.getString(R.string.notificationAlwaysChannelName),
					context.getString(R.string.notificationAlwaysChannelDescription), notificationType);
		} else if (notificationType == NotificationType.Daily) {
			return new NotificationObj(context.getString(R.string.notificationDailyChannelName),
					context.getString(R.string.notificationDailyChannelDescription), notificationType);
		} else if (notificationType == NotificationType.Alarm) {
			return new NotificationObj(context.getString(R.string.notificationAlarmChannelName),
					context.getString(R.string.notificationAlarmChannelDescription), notificationType);
		} else if (notificationType == NotificationType.ForegroundService) {
			return new NotificationObj(context.getString(R.string.notificationForegroundServiceChannelName),
					context.getString(R.string.notificationForegroundServiceChannelDescription), notificationType);
		} else {
			return new NotificationObj(context.getString(R.string.notificationLocationChannelName),
					context.getString(R.string.notificationLocationChannelDescription), notificationType);
		}
	}

	public static class NotificationObj {
		private NotificationCompat.Builder notificationBuilder;
		final int notificationId;
		final String channelId;
		final String channelName;
		final String channelDescription;
		final NotificationType notificationType;

		public NotificationObj(String channelName, String channelDescription, NotificationType notificationType) {
			this.notificationId = notificationType.getNotificationId();
			this.channelId = notificationType.getChannelId();
			this.channelName = channelName;
			this.channelDescription = channelDescription;
			this.notificationType = notificationType;
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

		public NotificationType getNotificationType() {
			return notificationType;
		}
	}
}
