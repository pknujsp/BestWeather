package com.lifedawn.bestweather.notification.always;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.gridlayout.widget.GridLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.notification.NotificationKey;
import com.lifedawn.bestweather.notification.NotificationType;

import java.time.format.DateTimeFormatter;

public class NotiViewCreator {
	private static final String ALWAYS_NOTI_SHARED_PREFERENCES = "ALWAYS_NOTI_SHARED_PREFERENCES";
	private static final String DAILY_NOTI_SHARED_PREFERENCES = "DAILY_NOTI_SHARED_PREFERENCES";

	private final NotificationType notificationType;
	private LayoutInflater layoutInflater;
	private Context context;

	private LocationType locationType;
	private WeatherSourceType weatherSourceType;
	private boolean kmaTopPriority;
	private long updateInterval;
	private int selectedAddressDtoId;

	public NotiViewCreator(Context context, NotificationType notificationType) {
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
		this.notificationType = notificationType;

		loadPreferences();
	}

	public void loadPreferences() {
		SharedPreferences notiPreferences = null;
		if (notificationType == NotificationType.Always) {
			notiPreferences = context.getSharedPreferences(ALWAYS_NOTI_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		} else {
			notiPreferences = context.getSharedPreferences(DAILY_NOTI_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		}

		locationType = LocationType.valueOf(
				notiPreferences.getString(NotificationKey.NotiAttributes.LOCATION_TYPE.name(), LocationType.SelectedAddress.name()));
		weatherSourceType = WeatherSourceType.valueOf(notiPreferences.getString(NotificationKey.NotiAttributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name()));
		kmaTopPriority = notiPreferences.getBoolean(NotificationKey.NotiAttributes.TOP_PRIORITY_KMA.name(), false);
		updateInterval = notiPreferences.getLong(NotificationKey.NotiAttributes.UPDATE_INTERVAL.name(), 0L);
		selectedAddressDtoId = notiPreferences.getInt(NotificationKey.NotiAttributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
	}

	public RemoteViews createRemoteViews() {
		RemoteViews remoteViews = null;
		if (notificationType == NotificationType.Always) {
			remoteViews = createAlwaysNotificationRemoteViews();
		} else {

		}

		return remoteViews;
	}

	private RemoteViews createAlwaysNotificationRemoteViews() {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_always_notification);

		//hourly forecast
		for (int i = 0; i < 6; i++) {
			remoteViews.addView(R.id.hourlyForecast, addHourlyForecastItem(null, 0, null, i));
		}
		return remoteViews;

	}

	private RemoteViews createDailyNotificationRemoteViews() {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_notification);

		//hourly forecast
		for (int i = 0; i < 6; i++) {
			remoteViews.addView(R.id.hourlyForecast, addHourlyForecastItem(null, 0, null, i));
		}
		return remoteViews;
	}

	public RemoteViews addHourlyForecastItem(String hours, int iconId, String temp, int count) {
		final RemoteViews itemRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_notification);

		itemRemoteViews.setImageViewResource(R.id.hourly_weather_icon, iconId);
		itemRemoteViews.setTextViewText(R.id.hourly_temperature, temp);
		itemRemoteViews.setTextViewText(R.id.hourly_clock, hours);

		return itemRemoteViews;
	}
}
