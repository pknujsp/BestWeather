package com.lifedawn.bestweather.notification.ongoing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;

import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.NotificationUpdateCallback;
import com.lifedawn.bestweather.notification.model.AlwaysNotiDataObj;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OngoingNotiViewCreator extends AbstractOngoingNotiViewCreator {
	private final int hourlyForecastCount = 8;
	private Handler handler;

	public OngoingNotiViewCreator(Context context, NotificationUpdateCallback notificationUpdateCallback) {
		super(context, NotificationType.Always, notificationUpdateCallback);
	}

	@Override
	public RemoteViews[] createRemoteViews(boolean temp) {
		RemoteViews collapsedRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_ongoing_notification_collapsed);
		RemoteViews expandedRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_ongoing_notification_expanded);

		if (temp) {
			setHourlyForecastViews(expandedRemoteViews, WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount));
		} else {
			collapsedRemoteViews.setOnClickPendingIntent(R.id.refreshLayout, getRefreshPendingIntent());
			expandedRemoteViews.setOnClickPendingIntent(R.id.refreshLayout, getRefreshPendingIntent());
		}

		return new RemoteViews[]{collapsedRemoteViews, expandedRemoteViews};
	}


	@Override
	public void initNotification(Handler handler) {
		this.handler = handler;
		RemoteViews[] remoteViewsArr = createRemoteViews(false);
		RemoteViewsUtil.onBeginProcess(remoteViewsArr[0]);
		RemoteViewsUtil.onBeginProcess(remoteViewsArr[1]);
		makeNotification(remoteViewsArr[0], remoteViewsArr[1], R.drawable.refresh, false);

		if (notificationDataObj.getLocationType() == LocationType.CurrentLocation) {
			loadCurrentLocation(context, remoteViewsArr[0], remoteViewsArr[1]);
		} else {
			loadWeatherData(context, remoteViewsArr[0], remoteViewsArr[1]);
		}
	}

	@Override
	protected Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.airQuality);
		set.add(WeatherDataType.hourlyForecast);
		return set;
	}

	@Override
	protected void setResultViews(Context context, RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews, WeatherDataSourceType requestWeatherDataSourceType, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		setHeaderViews(expandedRemoteViews, notificationDataObj.getAddressName(), multipleRestApiDownloader.getRequestDateTime().toString());
		int icon = R.mipmap.ic_launcher_round;

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				requestWeatherDataSourceType);
		if (currentConditionsDto != null) {
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			setCurrentConditionsViews(expandedRemoteViews, currentConditionsDto);
			icon = currentConditionsDto.getWeatherIcon();
		}

		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				requestWeatherDataSourceType);
		if (!hourlyForecastDtoList.isEmpty()) {
			setHourlyForecastViews(expandedRemoteViews, hourlyForecastDtoList);
		}

		AirQualityDto airQualityDto = null;
		if (zoneOffset != null) {
			airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);
			if (airQualityDto.isSuccessful()) {
				setAirQualityViews(expandedRemoteViews, AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));
			} else {
				setAirQualityViews(expandedRemoteViews, context.getString(R.string.noData));
			}
		} else {
			setAirQualityViews(expandedRemoteViews, context.getString(R.string.noData));
		}
		final boolean successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty();

		if (successful) {
			RemoteViewsUtil.onSuccessfulProcess(expandedRemoteViews);
		} else {
			RemoteViewsUtil.onErrorProcess(expandedRemoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
			expandedRemoteViews.setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
		}

		makeNotification(collapsedRemoteViews, expandedRemoteViews, icon, true);
	}

	public void setHeaderViews(RemoteViews remoteViews, String addressName, String dateTime) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(dateTime).format(dateTimeFormatter));
	}

	public void setAirQualityViews(RemoteViews remoteViews, String value) {
		String airQuality = context.getString(R.string.air_quality) + ": " + value;
		remoteViews.setTextViewText(R.id.airQuality, airQuality);
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsDto currentConditionsDto) {
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());
		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		remoteViews.setTextViewText(R.id.precipitation, precipitation);
		remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		remoteViews.setTextViewText(R.id.feelsLikeTemp, new String(context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp()));

		if (currentConditionsDto.getYesterdayTemp() != null) {
			String yesterdayCompText = WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, context);
			remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText);
		} else {
			remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE);
		}
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, List<HourlyForecastDto> hourlyForecastDtoList) {
		remoteViews.removeAllViews(R.id.hourlyForecast);
		final int textColor = ContextCompat.getColor(context, R.color.textColorInNotification);
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");
		String hours = null;

		boolean haveRain = false;
		boolean haveSnow = false;

		for (int i = 0; i < hourlyForecastCount; i++) {
			if (hourlyForecastDtoList.get(i).isHasRain()) {
				haveRain = true;
			}
			if (hourlyForecastDtoList.get(i).isHasSnow()) {
				haveSnow = true;
			}
		}

		final String mm = "mm";
		final String cm = "cm";

		for (int i = 0; i < hourlyForecastCount; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear);

			if (haveRain) {
				if (hourlyForecastDtoList.get(i).isHasRain()) {
					childRemoteViews.setTextViewText(R.id.rainVolume, hourlyForecastDtoList.get(i).getRainVolume()
							.replace(mm, "").replace(cm, ""));
					childRemoteViews.setTextColor(R.id.rainVolume, textColor);
				} else {
					childRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.INVISIBLE);
				}
			} else {
				childRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			}

			if (haveSnow) {
				if (hourlyForecastDtoList.get(i).isHasSnow()) {
					childRemoteViews.setTextViewText(R.id.snowVolume, hourlyForecastDtoList.get(i).getSnowVolume()
							.replace(mm, "").replace(cm, ""));
					childRemoteViews.setTextColor(R.id.snowVolume, textColor);
				} else {
					childRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.INVISIBLE);
				}
			} else {
				childRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);
			}

			if (hourlyForecastDtoList.get(i).getHours().getHour() == 0) {
				hours = hourlyForecastDtoList.get(i).getHours().format(hour0Formatter);
			} else {
				hours = String.valueOf(hourlyForecastDtoList.get(i).getHours().getHour());
			}

			childRemoteViews.setTextViewText(R.id.dateTime, hours);
			childRemoteViews.setTextViewText(R.id.pop, hourlyForecastDtoList.get(i).getPop());
			childRemoteViews.setTextViewText(R.id.temperature, hourlyForecastDtoList.get(i).getTemp());
			childRemoteViews.setImageViewResource(R.id.leftIcon, hourlyForecastDtoList.get(i).getWeatherIcon());
			childRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

			childRemoteViews.setTextColor(R.id.dateTime, textColor);
			childRemoteViews.setTextColor(R.id.temperature, textColor);
			childRemoteViews.setTextColor(R.id.pop, textColor);

			remoteViews.addView(R.id.hourlyForecast, childRemoteViews);
		}
	}

	public void makeNotification(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews, int icon, boolean isFinished) {
		boolean enabled =
				PreferenceManager.getDefaultSharedPreferences(context).getBoolean(notificationType.getPreferenceName(), false);
		if (enabled) {
			NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(notificationType);

			NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
			builder.setSmallIcon(icon).setAutoCancel(false).setOngoing(true).setOnlyAlertOnce(true)
					.setCustomContentView(expandedRemoteViews).setCustomBigContentView(expandedRemoteViews).setSilent(true);

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
				builder.setDefaults(0).setVibrate(null).setSound(null).setLights(0, 0, 0)
						.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
			}

			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
			notificationManager.notify(notificationType.getNotificationId(), builder.build());
		} else {
			notificationHelper.cancelNotification(notificationType.getNotificationId());
		}

		if (isFinished && handler != null) {
			Message message = handler.obtainMessage();
			message.obj = "finished";
			handler.sendMessage(message);
		}
	}

	/*
		public void setDailyForecastViews(RemoteViews remoteViews, WeatherJsonObj.DailyForecasts dailyForecasts) {
			remoteViews.removeAllViews(R.id.dailyForecast);
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.date_pattern_of_daily_forecast_in_widget));
			List<DailyForecastObj> dailyForecastObjList = dailyForecasts.getDailyForecastObjs();

			for (int day = 0; day < 4; day++) {
				RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_forecast_item_in_linear);

				childRemoteViews.setTextViewText(R.id.daily_date, ZonedDateTime.parse(dailyForecastObjList.get(day).getDate()).format(dateFormatter));
				childRemoteViews.setTextViewText(R.id.daily_temperature, ValueUnits.convertTemperature(dailyForecastObjList.get(day).getMinTemp(),
						tempUnit) + tempDegree + " / " + ValueUnits.convertTemperature(dailyForecastObjList.get(day).getMaxTemp(),
						tempUnit) + tempDegree);

				childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.VISIBLE);
				childRemoteViews.setViewVisibility(R.id.daily_right_weather_icon, View.VISIBLE);

				if (dailyForecastObjList.get(day).isSingle()) {
					childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastObjList.get(day).getLeftWeatherIcon());
					childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.GONE);
				} else {
					childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastObjList.get(day).getLeftWeatherIcon());
					childRemoteViews.setImageViewResource(R.id.daily_right_weather_icon, dailyForecastObjList.get(day).getRightWeatherIcon());
				}

				remoteViews.addView(R.id.dailyForecast, childRemoteViews);
			}
		}


	 */

	@Override
	public void loadSavedPreferences() {
		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

		notificationDataObj = new AlwaysNotiDataObj();
		notificationDataObj.setLocationType(LocationType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), LocationType.CurrentLocation.name())));
		notificationDataObj.setWeatherSourceType(WeatherDataSourceType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
				WeatherDataSourceType.OWM_ONECALL.name())));
		notificationDataObj.setTopPriorityKma(notiPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false));
		notificationDataObj.setUpdateIntervalMillis(notiPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L));
		notificationDataObj.setSelectedAddressDtoId(notiPreferences.getInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0));

		notificationDataObj.setAddressName(notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), ""));
		notificationDataObj.setLatitude(notiPreferences.getFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), 0f));
		notificationDataObj.setLongitude(notiPreferences.getFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), 0f));
		notificationDataObj.setCountryCode(notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), ""));
	}

	@Override
	public void loadDefaultPreferences() {
		notificationDataObj = new AlwaysNotiDataObj();
		notificationDataObj.setLocationType(LocationType.CurrentLocation);
		notificationDataObj.setWeatherSourceType(WeatherDataSourceType.OWM_ONECALL);
		notificationDataObj.setTopPriorityKma(false);
		notificationDataObj.setUpdateIntervalMillis(0);
		notificationDataObj.setSelectedAddressDtoId(0);

		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = notiPreferences.edit();
		editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), notificationDataObj.getLocationType().name());
		editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), notificationDataObj.getWeatherSourceType().name());
		editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), notificationDataObj.isTopPriorityKma());
		editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), notificationDataObj.getUpdateIntervalMillis());
		editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), notificationDataObj.getSelectedAddressDtoId());
		editor.commit();
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

		editor.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), notificationDataObj.getAddressName());
		editor.putFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), (float) notificationDataObj.getLatitude());
		editor.putFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), (float) notificationDataObj.getLongitude());
		editor.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), notificationDataObj.getCountryCode());
		editor.commit();
	}

	public AlwaysNotiDataObj getNotificationDataObj() {
		return (AlwaysNotiDataObj) notificationDataObj;
	}
}
