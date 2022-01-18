package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.IntentUtil;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.main.MainActivity;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;

import java.time.format.DateTimeFormatter;
import java.util.Set;

public abstract class AbstractDailyNotiViewCreator {
	protected final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	protected Context context;
	protected Handler handler;

	public AbstractDailyNotiViewCreator(Context context) {
		this.context = context;
	}

	abstract public void setTempDataViews(RemoteViews remoteViews);

	abstract public RemoteViews createRemoteViews(boolean needTempData);

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void makeNotification(RemoteViews remoteViews, int notificationDtoId) {
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Daily);
		notificationObj.getNotificationBuilder().setSmallIcon(R.drawable.day_clear);
		notificationObj.getNotificationBuilder().setContentTitle(context.getString(R.string.app_name));
		notificationObj.getNotificationBuilder().setPriority(NotificationCompat.PRIORITY_HIGH);
		notificationObj.getNotificationBuilder().setStyle(new NotificationCompat.BigPictureStyle());
		notificationObj.getNotificationBuilder().setAutoCancel(true);

		notificationObj.getNotificationBuilder().setCustomContentView(remoteViews);
		notificationObj.getNotificationBuilder().setCustomBigContentView(remoteViews);

		remoteViews.setOnClickPendingIntent(R.id.root_layout, PendingIntent.getActivity(context, notificationObj.getNotificationId(),
				IntentUtil.getAppIntent(context),
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
						PendingIntent.FLAG_UPDATE_CURRENT));

		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		Notification notification = notificationObj.getNotificationBuilder().build();
		notificationManager.notify(notificationObj.getNotificationId() + notificationDtoId, notification);

		if (handler != null) {
			Message message = handler.obtainMessage();
			message.obj = "finished";
			handler.sendMessage(message);
		}
	}


	public abstract Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();

	public abstract void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherDataSourceType> weatherDataSourceTypeSet,
	                                    @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader,
	                                    Set<RequestWeatherDataType> requestWeatherDataTypeSet);

}
