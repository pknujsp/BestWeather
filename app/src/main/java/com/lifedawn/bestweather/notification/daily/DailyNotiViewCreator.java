package com.lifedawn.bestweather.notification.daily;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.JsonDataSaver;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.forremoteviews.WeatherDataRequest;
import com.lifedawn.bestweather.forremoteviews.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.forremoteviews.dto.HeaderObj;
import com.lifedawn.bestweather.forremoteviews.dto.HourlyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DailyNotiViewCreator implements SharedPreferences.OnSharedPreferenceChangeListener {
	private final NotificationType notificationType = NotificationType.Daily;
	private Context context;

	private LocationType locationType;
	private WeatherSourceType weatherSourceType;
	private boolean kmaTopPriority;
	private long updateInterval;
	private Integer selectedAddressDtoId;
	private String alarmClock;
	private final ValueUnits tempUnit;
	private final ValueUnits windSpeedUnit;
	private final String tempDegree;
	private JsonDataSaver jsonDataSaver = new JsonDataSaver();
	private final DateTimeFormatter dateTimeFormatter;
	private NotificationHelper notificationHelper;
	private NetworkStatus networkStatus;

	private DailyNotiHelper dailyNotiHelper;

	private String addressName;

	public DailyNotiViewCreator(Context context) {
		this.context = context;

		networkStatus = NetworkStatus.getInstance(context);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		tempDegree = context.getString(R.string.degree_symbol);
		windSpeedUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits clockUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_clock),
				ValueUnits.clock12.name()));
		dateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? context.getString(R.string.datetime_pattern_clock12) :
				context.getString(R.string.datetime_pattern_clock24));
		notificationHelper = new NotificationHelper(context);

		dailyNotiHelper = new DailyNotiHelper(context);
	}


	public RemoteViews createRemoteViews() {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_notification);
		return remoteViews;
	}

	public void initNotification() {
		if (!networkStatus.networkAvailable()) {
			RemoteViews remoteViews = createRemoteViews();
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.UNAVAILABLE_NETWORK);
			remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, dailyNotiHelper.getRefreshPendingIntent());
			makeNotification(remoteViews, R.drawable.temp_icon);
			return;
		}
		//초기화
		if (locationType == LocationType.CurrentLocation) {
			loadCurrentLocation();
		} else {
			loadWeatherData();
		}
	}

	public void loadCurrentLocation() {
		Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						final SharedPreferences sharedPreferences =
								context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						String countryCode = addressList.get(0).getCountryCode();

						editor.putString(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), String.valueOf(addressList.get(0).getLatitude()))
								.putString(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), String.valueOf(addressList.get(0).getLongitude()))
								.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), countryCode)
								.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), addressList.get(0).getAddressLine(0)).commit();

						loadWeatherData();
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				Intent intent = null;
				RemoteViewProcessor.ErrorType errorType = null;

				if (fail == Fail.REJECT_PERMISSION) {
					errorType = RemoteViewProcessor.ErrorType.GPS_PERMISSION_REJECTED;
					intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					intent.setData(Uri.fromParts("package", context.getPackageName(), null));
				} else {
					errorType = RemoteViewProcessor.ErrorType.GPS_OFF;
					intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				}

				RemoteViews remoteViews = createRemoteViews();
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, PendingIntent.getActivity(context, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT));
				RemoteViewProcessor.onErrorProcess(remoteViews, context, errorType);
				makeNotification(remoteViews, R.drawable.temp_icon);
			}
		};

		Gps gps = new Gps(context, null, null, null);
		if (gps.checkPermissionAndGpsEnabled(context, locationCallback)) {
			gps.runGps(context, locationCallback);
		}
	}

	public void loadWeatherData() {
		WeatherDataRequest weatherDataRequest = new WeatherDataRequest(context);
		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();

		MultipleJsonDownloader multipleJsonDownloader = new MultipleJsonDownloader() {
			@Override
			public void onResult() {
				setResultViews(context, this, requestWeatherDataTypeSet);
			}

			@Override
			public void onCanceled() {
			}
		};
		weatherDataRequest.loadWeatherData(context, notificationType.getPreferenceName(),
				requestWeatherDataTypeSet, multipleJsonDownloader);
	}

	private Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.currentConditions);
		set.add(RequestWeatherDataType.hourlyForecast);
		return set;
	}


	protected final void setResultViews(Context context,
	                                    @Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		SharedPreferences sharedPreferences =
				context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

		WeatherSourceType requestWeatherSourceType =
				WeatherSourceType.valueOf(sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));
		String countryCode = sharedPreferences.getString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), "");
		if (sharedPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), true) &&
				countryCode.equals("KR")) {
			requestWeatherSourceType = WeatherSourceType.KMA;
		}

		CurrentConditionsObj currentConditionsObj = null;
		WeatherJsonObj.HourlyForecasts hourlyForecastObjs = null;
		WeatherJsonObj.DailyForecasts dailyForecasts = null;

		WeatherDataRequest weatherDataRequest = new WeatherDataRequest(context);

		HeaderObj headerObj = weatherDataRequest.getHeader(context, notificationType.getPreferenceName());

		RemoteViews remoteViews = createRemoteViews();
		setHeaderViews(remoteViews, headerObj);

		boolean successful = true;

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
			currentConditionsObj = weatherDataRequest.getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
					notificationType.getPreferenceName());

			if (currentConditionsObj.isSuccessful()) {
				setCurrentConditionsViews(remoteViews, currentConditionsObj);
			} else {
				successful = false;
			}
		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
			hourlyForecastObjs = weatherDataRequest.getHourlyForecasts(context, requestWeatherSourceType, multipleJsonDownloader,
					notificationType.getPreferenceName());

			if (hourlyForecastObjs.isSuccessful()) {
				setHourlyForecastViews(remoteViews, hourlyForecastObjs);
			} else {
				successful = false;
			}

		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
			dailyForecasts = weatherDataRequest.getDailyForecasts(requestWeatherSourceType, multipleJsonDownloader);

			if (dailyForecasts.isSuccessful()) {
				//setDailyForecastViews(remoteViews, dailyForecasts);
			} else {
				successful = false;
			}
		}
		int icon = 0;

		if (successful) {
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
			icon = currentConditionsObj.getWeatherIcon();
		} else {
			icon = R.drawable.temp_icon;
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
			remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, dailyNotiHelper.getRefreshPendingIntent());
		}

		makeNotification(remoteViews, icon);
		JsonDataSaver.saveWeatherData(notificationType.getPreferenceName(), context, headerObj, currentConditionsObj, hourlyForecastObjs,
				dailyForecasts);
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

	public void setHeaderViews(RemoteViews remoteViews, HeaderObj headerObj) {
		remoteViews.setTextViewText(R.id.address, headerObj.getAddress());
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(headerObj.getRefreshDateTime()).format(dateTimeFormatter));
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsObj currentConditionsObj) {
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsObj.getWeatherIcon());
		remoteViews.setTextViewText(R.id.airQuality, currentConditionsObj.getAirQuality() == null ? context.getString(R.string.not_data)
				: AqicnResponseProcessor.getGradeDescription((int) Double.parseDouble(currentConditionsObj.getAirQuality())));
		remoteViews.setTextViewText(R.id.precipitation, currentConditionsObj.getPrecipitation() == null ?
				context.getString(R.string.not_precipitation) :
				currentConditionsObj.getPrecipitationType() + ", " + currentConditionsObj.getPrecipitation() + "mm");

		String windSpeed = currentConditionsObj.getWindSpeed();
		String windSpeedStr =
				windSpeed + ValueUnits.convertToStr(context, windSpeedUnit) + ", " + WeatherResponseProcessor.getSimpleWindSpeedDescription(windSpeed);
		remoteViews.setTextViewText(R.id.windSpeed, windSpeedStr);
		remoteViews.setTextViewText(R.id.temp, ValueUnits.convertTemperature(currentConditionsObj.getTemp(),
				tempUnit) + tempDegree);
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, WeatherJsonObj.HourlyForecasts hourlyForecasts) {
		remoteViews.removeAllViews(R.id.hourlyForecast1);
		remoteViews.removeAllViews(R.id.hourlyForecast2);
		String clock = null;
		ZonedDateTime zonedDateTime = null;

		List<HourlyForecastObj> hourlyForecastObjList = hourlyForecasts.getHourlyForecastObjs();

		final int textColor = ContextCompat.getColor(context, R.color.textColorInNotification);

		for (int i = 0; i < 16; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_linear);

			zonedDateTime = ZonedDateTime.parse(hourlyForecastObjList.get(i).getClock());
			if (zonedDateTime.getHour() == 0) {
				clock = zonedDateTime.format(DateTimeFormatter.ofPattern(context.getString(R.string.time_pattern_if_hours_0_of_hourly_forecast_in_widget)));
			} else {
				clock = String.valueOf(zonedDateTime.getHour());
			}

			childRemoteViews.setTextViewText(R.id.hourly_clock, clock);
			childRemoteViews.setTextColor(R.id.hourly_clock, textColor);
			childRemoteViews.setTextColor(R.id.hourly_temperature, textColor);
			childRemoteViews.setTextViewText(R.id.hourly_temperature, ValueUnits.convertTemperature(hourlyForecastObjList.get(i).getTemp(),
					tempUnit) + tempDegree);
			childRemoteViews.setImageViewResource(R.id.hourly_weather_icon, hourlyForecastObjList.get(i).getWeatherIcon());

			if (i > 7) {
				remoteViews.addView(R.id.hourlyForecast2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourlyForecast1, childRemoteViews);
			}
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

			remoteViews.addView(R.id.dail, childRemoteViews);
		}
	}

	 */

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name())) {
			String locationTypeStr = sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), "");
			locationType = locationTypeStr.isEmpty() ? null : LocationType.valueOf(locationTypeStr);
		}
		if (key.equals(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name())) {
			weatherSourceType = WeatherSourceType.valueOf(sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
					WeatherSourceType.OPEN_WEATHER_MAP.name()));
		}
		if (key.equals(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name())) {
			kmaTopPriority = sharedPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), kmaTopPriority);
		}
		if (key.equals(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name())) {
			updateInterval = sharedPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), updateInterval);
		}
		if (key.equals(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name())) {
			selectedAddressDtoId = sharedPreferences.getInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
		}
		if (key.equals(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name())) {
			alarmClock = sharedPreferences.getString(key, LocalTime.now().toString());
		}
	}

	public void loadPreferences() {
		SharedPreferences notiPreferences = null;
		notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

		String locationTypeStr = notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), "");
		locationType = locationTypeStr.isEmpty() ? null : LocationType.valueOf(locationTypeStr);
		weatherSourceType = WeatherSourceType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name()));
		kmaTopPriority = notiPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false);
		updateInterval = notiPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L);
		selectedAddressDtoId = notiPreferences.getInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
		alarmClock = notiPreferences.getString(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name(), LocalTime.now().toString());
		addressName = notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), "");
	}

	public String getAlarmClock() {
		return alarmClock;
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

	public String getAddressName() {
		return addressName;
	}
}