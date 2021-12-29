package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.daily.DailyPushNotificationType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static android.view.View.MeasureSpec.EXACTLY;

public abstract class AbstractDailyNotiViewCreator {
	protected final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	protected Context context;

	public AbstractDailyNotiViewCreator(Context context) {
		this.context = context;
	}

	protected View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		return view;
	}

	protected Bitmap drawBitmap(ViewGroup rootLayout, RemoteViews remoteViews) {
		final int padding = (int) context.getResources().getDimension(R.dimen.notificationPadding);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

		rootLayout.measure(widthSpec, heightSpec);
		rootLayout.layout(0, 0, rootLayout.getMeasuredWidth(), rootLayout.getMeasuredHeight());

		rootLayout.setDrawingCacheEnabled(true);
		rootLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		Bitmap viewBmp = rootLayout.getDrawingCache();
		remoteViews.setImageViewBitmap(R.id.valuesView, viewBmp);
		return viewBmp;
	}

	abstract public void setTempDataViews(RemoteViews remoteViews);

	public RemoteViews createRemoteViews(boolean needTempData) {
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_notification);

		if (needTempData) {
			setTempDataViews(remoteViews);
		}

		return remoteViews;
	}

	public void makeNotification(RemoteViews remoteViews, int notificationDtoId) {
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Daily);
		notificationObj.getNotificationBuilder().setSmallIcon(R.drawable.day_clear);
		notificationObj.getNotificationBuilder().setContentTitle(context.getString(R.string.app_name));
		notificationObj.getNotificationBuilder().setPriority(NotificationCompat.PRIORITY_DEFAULT);
		notificationObj.getNotificationBuilder().setStyle(new NotificationCompat.BigTextStyle());

		notificationObj.getNotificationBuilder().setCustomContentView(remoteViews);
		notificationObj.getNotificationBuilder().setCustomBigContentView(remoteViews);

		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		Notification notification = notificationObj.getNotificationBuilder().build();
		notificationManager.notify(notificationObj.getNotificationId() + notificationDtoId, notification);
	}


	public abstract Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();

	public abstract void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherSourceType> weatherSourceTypeSet,
	                                    @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader,
	                                    Set<RequestWeatherDataType> requestWeatherDataTypeSet);

}
