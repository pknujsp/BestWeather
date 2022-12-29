package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.IntentUtil;
import com.lifedawn.bestweather.commons.constants.WeatherDataType;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.data.remote.retrofit.callback.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public abstract class AbstractDailyNotiViewCreator {
	protected final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	protected Context context;
	protected BackgroundWorkCallback callback;
	protected ZoneId zoneId;

	public AbstractDailyNotiViewCreator(Context context) {
		this.context = context;
	}

	abstract public void setTempDataViews(RemoteViews remoteViews);

	abstract public RemoteViews createRemoteViews(boolean needTempData);

	public void setBackgroundCallback(BackgroundWorkCallback callback) {
		this.callback = callback;
	}

	public void makeNotification(RemoteViews remoteViews, int notificationDtoId) {
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Daily);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher_round).setContentIntent(PendingIntent.getActivity(context,
						notificationObj.getNotificationId(),
						IntentUtil.getAppIntent(context), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE))
				.setCustomContentView(remoteViews).setCustomBigContentView(remoteViews);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_HIGH).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		Notification notification = notificationObj.getNotificationBuilder().build();
		notificationManager.notify(notificationObj.getNotificationId() + notificationDtoId, notification);

		if (callback != null) {
			callback.onFinished();
		}
	}

	public void makeFailedNotification(int notificationDtoId, String failText) {
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Daily);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setAutoCancel(true).setContentText(failText).setContentTitle(context.getString(R.string.errorNotification));

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_HIGH).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		Notification notification = notificationObj.getNotificationBuilder().build();
		notificationManager.notify(notificationObj.getNotificationId() + notificationDtoId, notification);

		if (callback != null) {
			callback.onFinished();
		}
	}


	public abstract Set<WeatherDataType> getRequestWeatherDataTypeSet();

	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherProviderType> weatherProviderTypeSet,
	                           @Nullable @org.jetbrains.annotations.Nullable WeatherRestApiDownloader weatherRestApiDownloader,
	                           Set<WeatherDataType> weatherDataTypeSet) {
	}

}
