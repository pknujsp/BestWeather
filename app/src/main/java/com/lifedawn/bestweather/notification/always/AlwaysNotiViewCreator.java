package com.lifedawn.bestweather.notification.always;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.DataSaver;
import com.lifedawn.bestweather.notification.NotificationKey;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.forremoteviews.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.forremoteviews.dto.DailyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.HourlyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AlwaysNotiViewCreator implements SharedPreferences.OnSharedPreferenceChangeListener {
	private final NotificationType notificationType = NotificationType.Always;
	private final NotificationUpdateCallback notificationUpdateCallback;
	private Context context;

	private LocationType locationType;
	private WeatherSourceType weatherSourceType;
	private boolean kmaTopPriority;
	private long updateInterval;
	private int selectedAddressDtoId;
	private final ValueUnits tempUnit;
	private final String tempDegree;
	private DataSaver dataSaver = new DataSaver();

	public AlwaysNotiViewCreator(Context context, NotificationUpdateCallback notificationUpdateCallback) {
		this.context = context;
		this.notificationUpdateCallback = notificationUpdateCallback;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		tempDegree = context.getString(R.string.degree_symbol);

	}

	public void initValues(SharedPreferences sharedPreferences) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(NotificationKey.NotiAttributes.LOCATION_TYPE.name(), null);
		editor.putBoolean(NotificationKey.NotiAttributes.ENABLED_NOTI.name(), true);
		editor.putString(NotificationKey.NotiAttributes.WEATHER_SOURCE_TYPE.name(), WeatherSourceType.OPEN_WEATHER_MAP.name());
		editor.putBoolean(NotificationKey.NotiAttributes.TOP_PRIORITY_KMA.name(), false);
		editor.putLong(NotificationKey.NotiAttributes.UPDATE_INTERVAL.name(), 0L);
		editor.putInt(NotificationKey.NotiAttributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
		editor.apply();
	}

	public void loadPreferences() {
		SharedPreferences notiPreferences = null;
		notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

		locationType = LocationType.valueOf(
				notiPreferences.getString(NotificationKey.NotiAttributes.LOCATION_TYPE.name(), LocationType.SelectedAddress.name()));
		weatherSourceType = WeatherSourceType.valueOf(notiPreferences.getString(NotificationKey.NotiAttributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name()));
		kmaTopPriority = notiPreferences.getBoolean(NotificationKey.NotiAttributes.TOP_PRIORITY_KMA.name(), false);
		updateInterval = notiPreferences.getLong(NotificationKey.NotiAttributes.UPDATE_INTERVAL.name(), 0L);
		selectedAddressDtoId = notiPreferences.getInt(NotificationKey.NotiAttributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
	}

	public RemoteViews createRemoteViews(boolean temp) {
		RemoteViews remoteViews = createAlwaysNotificationRemoteViews(temp);
		return remoteViews;
	}

	private RemoteViews createAlwaysNotificationRemoteViews(boolean temp) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_always_notification);

		if (temp) {
			setCurrentConditionsViews(remoteViews, dataSaver.getTempCurrentConditionsObj());
			setHourlyForecastViews(remoteViews, dataSaver.getTempHourlyForecastObjs(6));
		}

		return remoteViews;
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsObj currentConditionsObj) {
		if (currentConditionsObj == null) {
			return;
		}
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsObj.getWeatherIcon());
		remoteViews.setTextViewText(R.id.airQuality, currentConditionsObj.getAirQuality() == null ? context.getString(R.string.not_data)
				: AqicnResponseProcessor.getGradeDescription((int) Double.parseDouble(currentConditionsObj.getAirQuality())));
		remoteViews.setTextViewText(R.id.precipitation, currentConditionsObj.getPrecipitation() == null ?
				context.getString(R.string.not_precipitation) : currentConditionsObj.getPrecipitation() + "mm");
		remoteViews.setTextViewText(R.id.temp, ValueUnits.convertTemperature(currentConditionsObj.getTemp(),
				tempUnit) + tempDegree);
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, WeatherJsonObj.HourlyForecasts hourlyForecasts) {
		if (hourlyForecasts == null) {
			return;
		}

		remoteViews.removeAllViews(R.id.hourlyForecast);
		String clock = null;
		ZonedDateTime zonedDateTime = null;

		List<HourlyForecastObj> hourlyForecastObjList = hourlyForecasts.getHourlyForecastObjs();

		for (int i = 0; i < hourlyForecastObjList.size(); i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_linear);

			zonedDateTime = ZonedDateTime.parse(hourlyForecastObjList.get(i).getClock());
			if (zonedDateTime.getHour() == 0) {
				clock = zonedDateTime.format(DateTimeFormatter.ofPattern(context.getString(R.string.time_pattern_if_hours_0_of_hourly_forecast_in_widget)));
			} else {
				clock = String.valueOf(zonedDateTime.getHour());
			}

			childRemoteViews.setTextViewText(R.id.hourly_clock, clock);
			childRemoteViews.setTextViewText(R.id.hourly_temperature, ValueUnits.convertTemperature(hourlyForecastObjList.get(i).getTemp(),
					tempUnit) + tempDegree);
			childRemoteViews.setImageViewResource(R.id.hourly_weather_icon, hourlyForecastObjList.get(i).getWeatherIcon());

			remoteViews.addView(R.id.hourlyForecast, childRemoteViews);
		}
	}

	public void setDailyForecastViews(RemoteViews remoteViews, WeatherJsonObj.DailyForecasts dailyForecasts) {
		if (dailyForecasts == null) {
			return;
		}
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		locationType = LocationType.valueOf(sharedPreferences.getString(NotificationKey.NotiAttributes.LOCATION_TYPE.name(),
				LocationType.CurrentLocation.name()));
		weatherSourceType = WeatherSourceType.valueOf(sharedPreferences.getString(NotificationKey.NotiAttributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name()));
		kmaTopPriority = sharedPreferences.getBoolean(NotificationKey.NotiAttributes.TOP_PRIORITY_KMA.name(), kmaTopPriority);
		updateInterval = sharedPreferences.getLong(NotificationKey.NotiAttributes.UPDATE_INTERVAL.name(), updateInterval);
		selectedAddressDtoId = sharedPreferences.getInt(NotificationKey.NotiAttributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
	}

	public LocationType getLocationType() {
		return locationType;
	}

	public WeatherSourceType getWeatherSourceType() {
		return weatherSourceType;
	}

	public boolean isKmaTopPriority() {
		return kmaTopPriority;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	public int getSelectedAddressDtoId() {
		return selectedAddressDtoId;
	}

	public interface NotificationUpdateCallback {
		void updateNotification();
	}
}
