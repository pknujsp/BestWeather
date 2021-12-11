package com.lifedawn.bestweather.notification.daily;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.forremoteviews.SimpleWeatherDataProcessor;
import com.lifedawn.bestweather.notification.AbstractNotiViewCreator;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.NotificationUpdateCallback;
import com.lifedawn.bestweather.notification.model.DailyNotiDataObj;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.widget.model.AirQualityObj;
import com.lifedawn.bestweather.widget.model.CurrentConditionsObj;
import com.lifedawn.bestweather.widget.model.HourlyForecastObj;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DailyNotiViewCreator extends AbstractNotiViewCreator {
	private DailyNotiHelper dailyNotiHelper;

	public DailyNotiViewCreator(Context context, NotificationUpdateCallback notificationUpdateCallback) {
		super(context, NotificationType.Daily, notificationUpdateCallback);
		dailyNotiHelper = new DailyNotiHelper(context);
	}

	@Override
	public RemoteViews createRemoteViews(boolean temp) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_notification);
		return remoteViews;
	}

	public void initNotification() {
		RemoteViews remoteViews = createRemoteViews(false);

		/*
		if (!networkStatus.networkAvailable()) {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.UNAVAILABLE_NETWORK);
			remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, dailyNotiHelper.getRefreshPendingIntent());
			makeNotification(remoteViews, R.drawable.temp_icon);
			return;
		}

		 */
		//초기화
		if (notificationDataObj.getLocationType() == LocationType.CurrentLocation) {
			loadCurrentLocation(context, remoteViews);
		} else {
			loadWeatherData(context, remoteViews);
		}
	}


	@Override
	protected Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.currentConditions);
		set.add(RequestWeatherDataType.airQuality);
		set.add(RequestWeatherDataType.hourlyForecast);
		return set;
	}

	@Override
	protected void setResultViews(Context context, RemoteViews remoteViews, WeatherSourceType requestWeatherSourceType, @Nullable @org.jetbrains.annotations.Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		ZoneId zoneId = null;
		setHeaderViews(remoteViews, notificationDataObj.getAddressName(), multipleJsonDownloader.getLocalDateTime().toString());
		int icon = R.drawable.temp_icon;

		final CurrentConditionsObj currentConditionsObj = SimpleWeatherDataProcessor.getCurrentConditionsObj(context, requestWeatherSourceType,
				multipleJsonDownloader);
		if (currentConditionsObj.isSuccessful()) {
			setCurrentConditionsViews(remoteViews, currentConditionsObj);
			zoneId = ZoneId.of(currentConditionsObj.getTimeZoneId());
			icon = currentConditionsObj.getWeatherIcon();
		}

		final List<HourlyForecastObj> hourlyForecastObjList = SimpleWeatherDataProcessor.getHourlyForecasts(context,
				requestWeatherSourceType, multipleJsonDownloader);
		if (!hourlyForecastObjList.isEmpty()) {
			setHourlyForecastViews(remoteViews, hourlyForecastObjList);
		}

		final AirQualityObj airQualityObj = SimpleWeatherDataProcessor.getAirQualityObj(context, multipleJsonDownloader);
		if (airQualityObj.isSuccessful()) {
			setAirQualityViews(remoteViews, airQualityObj);
		}

		final boolean successful = currentConditionsObj.isSuccessful() && !hourlyForecastObjList.isEmpty();

		if (successful) {
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
			remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, dailyNotiHelper.getRefreshPendingIntent());
		}

		makeNotification(remoteViews, icon);
	}

	public void makeNotification(RemoteViews remoteViews, int icon) {
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(notificationType);
		notificationObj.getNotificationBuilder().setSmallIcon(icon);
		notificationObj.getNotificationBuilder().setStyle(new NotificationCompat.BigTextStyle());
		notificationObj.getNotificationBuilder().setPriority(NotificationCompat.PRIORITY_MAX);

		notificationObj.getNotificationBuilder().setCustomContentView(remoteViews);
		notificationObj.getNotificationBuilder().setCustomBigContentView(remoteViews);

		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		Notification notification = notificationObj.getNotificationBuilder().build();
		notificationManager.notify(notificationObj.getNotificationId(), notification);
	}

	@Override
	public void loadSavedPreferences() {
		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
		DailyNotiDataObj dailyNotiDataObj = new DailyNotiDataObj();
		notificationDataObj = dailyNotiDataObj;

		dailyNotiDataObj.setLocationType(LocationType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), LocationType.CurrentLocation.name())));
		dailyNotiDataObj.setWeatherSourceType(WeatherSourceType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name())));
		dailyNotiDataObj.setTopPriorityKma(notiPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false));
		dailyNotiDataObj.setUpdateIntervalMillis(notiPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L));
		dailyNotiDataObj.setSelectedAddressDtoId(notiPreferences.getInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0));
		dailyNotiDataObj.setAlarmClock(notiPreferences.getString(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name(), "08:00"));

		dailyNotiDataObj.setAddressName(notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), ""));
		dailyNotiDataObj.setLatitude(notiPreferences.getFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), 0f));
		dailyNotiDataObj.setLongitude(notiPreferences.getFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), 0f));
		dailyNotiDataObj.setCountryCode(notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), ""));
	}

	@Override
	public void loadDefaultPreferences() {
		DailyNotiDataObj dailyNotiDataObj = new DailyNotiDataObj();

		notificationDataObj = dailyNotiDataObj;
		dailyNotiDataObj.setLocationType(LocationType.CurrentLocation);
		dailyNotiDataObj.setWeatherSourceType(WeatherSourceType.OPEN_WEATHER_MAP);
		dailyNotiDataObj.setTopPriorityKma(false);
		dailyNotiDataObj.setUpdateIntervalMillis(0);
		dailyNotiDataObj.setSelectedAddressDtoId(0);
		dailyNotiDataObj.setAlarmClock("08:00");

		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = notiPreferences.edit();
		editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), dailyNotiDataObj.getLocationType().name());
		editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), dailyNotiDataObj.getWeatherSourceType().name());
		editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), dailyNotiDataObj.isTopPriorityKma());
		editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), dailyNotiDataObj.getUpdateIntervalMillis());
		editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), dailyNotiDataObj.getSelectedAddressDtoId());
		editor.putString(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name(), dailyNotiDataObj.getAlarmClock());
		editor.commit();
	}

	public void setAirQualityViews(RemoteViews remoteViews, AirQualityObj airQualityObj) {
		String airQuality = context.getString(R.string.air_quality) + " " + (airQualityObj.isSuccessful() ? airQualityObj.getAqi() :
				context.getString(R.string.noData));
		remoteViews.setTextViewText(R.id.airQuality, airQuality);
	}

	public void setHeaderViews(RemoteViews remoteViews, String addressName, String dateTime) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(dateTime).format(dateTimeFormatter));
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsObj currentConditionsObj) {
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsObj.getWeatherIcon());
		remoteViews.setTextViewText(R.id.precipitation, currentConditionsObj.getPrecipitationType() + " " + currentConditionsObj.getPrecipitationVolume());
		String windSpeedStr =
				context.getString(R.string.wind) + " " + currentConditionsObj.getWindSpeed();
		remoteViews.setTextViewText(R.id.windSpeed, windSpeedStr);
		remoteViews.setTextViewText(R.id.temp, currentConditionsObj.getTemp());
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, List<HourlyForecastObj> hourlyForecastObjList) {
		remoteViews.removeAllViews(R.id.hourlyForecast);
		final int textColor = ContextCompat.getColor(context, R.color.textColorInNotification);

		for (int i = 0; i < 16; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_linear);

			childRemoteViews.setTextViewText(R.id.hourly_clock, hourlyForecastObjList.get(i).getHours());
			childRemoteViews.setTextViewText(R.id.hourly_temperature, hourlyForecastObjList.get(i).getTemp());
			childRemoteViews.setImageViewResource(R.id.hourly_weather_icon, hourlyForecastObjList.get(i).getWeatherIcon());

			childRemoteViews.setTextColor(R.id.hourly_clock, textColor);
			childRemoteViews.setTextColor(R.id.hourly_temperature, textColor);

			if (i > 7) {
				remoteViews.addView(R.id.hourlyForecast2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourlyForecast1, childRemoteViews);
			}
		}
	}

	public void setTempHourlyForecastViews(RemoteViews remoteViews) {
		remoteViews.removeAllViews(R.id.hourlyForecast);
		final int textColor = ContextCompat.getColor(context, R.color.textColorInNotification);

		for (int i = 0; i < 16; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_linear);

			childRemoteViews.setTextColor(R.id.hourly_clock, textColor);
			childRemoteViews.setTextColor(R.id.hourly_temperature, textColor);

			if (i > 7) {
				remoteViews.addView(R.id.hourlyForecast2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourlyForecast1, childRemoteViews);
			}
		}
	}


	@Override
	public void savePreferences() {
		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = notiPreferences.edit();

		editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), notificationDataObj.getLocationType().name());
		editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), notificationDataObj.getWeatherSourceType().name());
		editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), notificationDataObj.isTopPriorityKma());
		editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), notificationDataObj.getUpdateIntervalMillis());
		editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), notificationDataObj.getSelectedAddressDtoId());
		editor.putString(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name(), ((DailyNotiDataObj) notificationDataObj).getAlarmClock());

		editor.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), notificationDataObj.getAddressName());
		editor.putFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), (float) notificationDataObj.getLatitude());
		editor.putFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), (float) notificationDataObj.getLongitude());
		editor.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), notificationDataObj.getCountryCode());
		editor.commit();
	}

	public DailyNotiDataObj getNotificationDataObj() {
		return (DailyNotiDataObj) notificationDataObj;
	}

}