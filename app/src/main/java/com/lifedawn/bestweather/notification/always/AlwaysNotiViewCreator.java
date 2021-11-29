package com.lifedawn.bestweather.notification.always;

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
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.JsonDataSaver;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.forremoteviews.WeatherDataRequest;
import com.lifedawn.bestweather.forremoteviews.dto.HeaderObj;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationReceiver;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.forremoteviews.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.forremoteviews.dto.DailyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.HourlyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private JsonDataSaver jsonDataSaver = new JsonDataSaver();
	private final DateTimeFormatter dateTimeFormatter;
	private NotificationHelper notificationHelper;

	public AlwaysNotiViewCreator(Context context, NotificationUpdateCallback notificationUpdateCallback) {
		this.context = context;
		this.notificationUpdateCallback = notificationUpdateCallback;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		tempDegree = context.getString(R.string.degree_symbol);
		ValueUnits clockUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_clock),
				ValueUnits.clock12.name()));
		dateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? context.getString(R.string.datetime_pattern_clock12) :
				context.getString(R.string.datetime_pattern_clock24));
		notificationHelper = new NotificationHelper(context);
	}

	public void loadPreferences() {
		SharedPreferences notiPreferences = null;
		notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

		locationType = LocationType.valueOf(
				notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), LocationType.SelectedAddress.name()));
		weatherSourceType = WeatherSourceType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name()));
		kmaTopPriority = notiPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false);
		updateInterval = notiPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L);
		selectedAddressDtoId = notiPreferences.getInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
	}

	public RemoteViews createRemoteViews(boolean temp) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_always_notification);

		Intent refreshIntent = new Intent(context, NotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		remoteViews.setOnClickPendingIntent(R.id.refreshDateTime, pendingIntent);

		if (temp) {
			setCurrentConditionsViews(remoteViews, jsonDataSaver.getTempCurrentConditionsObj());
			setHourlyForecastViews(remoteViews, jsonDataSaver.getTempHourlyForecastObjs(6));
		}

		return remoteViews;
	}

	public void initNotification() {
		//초기화
		RemoteViews remoteViews = createRemoteViews(false);
		makeNotification(remoteViews);

		RemoteViewProcessor.onBeginProcess(remoteViews);
		makeNotification(remoteViews);

		if (locationType == LocationType.CurrentLocation) {
			loadCurrentLocation(context, remoteViews);
		} else {
			SharedPreferences sharedPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

			if (sharedPreferences.getString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), "").equals("KR")) {
				if (kmaTopPriority) {
					weatherSourceType = WeatherSourceType.KMA;
					sharedPreferences.edit().putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
							WeatherSourceType.KMA.name()).commit();
				}
			}
			loadWeatherData(context, remoteViews);
		}
	}

	public void loadCurrentLocation(Context context, RemoteViews remoteViews) {
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
						if (countryCode.equals("KR")) {
							if (sharedPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false)) {
								editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), WeatherSourceType.KMA.name());
							}
						}
						editor.putString(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), String.valueOf(addressList.get(0).getLatitude()))
								.putString(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), String.valueOf(addressList.get(0).getLongitude()))
								.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), countryCode)
								.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), addressList.get(0).getAddressLine(0)).commit();

						loadWeatherData(context, remoteViews);
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				Intent intent = null;
				String errorMsg = null;
				String btnMsg = null;

				if (fail == Fail.REJECT_PERMISSION) {
					errorMsg = context.getString(R.string.message_needs_location_permission);
					btnMsg = context.getString(R.string.check_permission);
					intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					intent.setData(Uri.fromParts("package", context.getPackageName(), null));
				} else {
					errorMsg = context.getString(R.string.request_to_make_gps_on);
					btnMsg = context.getString(R.string.enable_gps);
					intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				}

				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, PendingIntent.getActivity(context, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT));
				RemoteViewProcessor.onErrorProcess(remoteViews, errorMsg, btnMsg);
				makeNotification(remoteViews);
			}
		};

		Gps gps = new Gps(null, null, null);
		if (gps.checkPermissionAndGpsEnabled(context, locationCallback)) {
			gps.runGps(context, locationCallback);
		}
	}

	public void loadWeatherData(Context context, RemoteViews remoteViews) {
		RemoteViewProcessor.onBeginProcess(remoteViews);
		makeNotification(remoteViews);

		WeatherDataRequest weatherDataRequest = new WeatherDataRequest(context);
		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();

		MultipleJsonDownloader multipleJsonDownloader = new MultipleJsonDownloader() {
			@Override
			public void onResult() {
				setResultViews(context, remoteViews, this, requestWeatherDataTypeSet);
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
		//set.add(RequestWeatherDataType.dailyForecast);
		return set;
	}


	protected final void setResultViews(Context context, RemoteViews remoteViews,
	                                    @Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		SharedPreferences sharedPreferences =
				context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

		final WeatherSourceType requestWeatherSourceType =
				WeatherSourceType.valueOf(sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));

		CurrentConditionsObj currentConditionsObj = null;
		WeatherJsonObj.HourlyForecasts hourlyForecastObjs = null;
		WeatherJsonObj.DailyForecasts dailyForecasts = null;

		WeatherDataRequest weatherDataRequest = new WeatherDataRequest(context);

		HeaderObj headerObj = weatherDataRequest.getHeader(context, notificationType.getPreferenceName());
		setHeaderViews(remoteViews, headerObj);

		boolean successful = true;

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
			currentConditionsObj = weatherDataRequest.getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
					notificationType.getPreferenceName());

			if (!currentConditionsObj.isSuccessful()) {
				RemoteViewProcessor.onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else {
				setCurrentConditionsViews(remoteViews, currentConditionsObj);
			}
		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
			hourlyForecastObjs = weatherDataRequest.getHourlyForecasts(context, requestWeatherSourceType, multipleJsonDownloader,
					notificationType.getPreferenceName());

			if (hourlyForecastObjs.getHourlyForecastObjs().isEmpty() && successful) {
				RemoteViewProcessor.onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else if (!hourlyForecastObjs.getHourlyForecastObjs().isEmpty()) {
				setHourlyForecastViews(remoteViews, hourlyForecastObjs);
			}

		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
			dailyForecasts = weatherDataRequest.getDailyForecasts(requestWeatherSourceType, multipleJsonDownloader);

			if (dailyForecasts.getDailyForecastObjs().isEmpty() && successful) {
				RemoteViewProcessor.onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else if (!dailyForecasts.getDailyForecastObjs().isEmpty()) {
				setDailyForecastViews(remoteViews, dailyForecasts);
			}
		}

		if (successful) {
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		}

		makeNotification(remoteViews);
		JsonDataSaver.saveWeatherData(notificationType.getPreferenceName(), context, headerObj, currentConditionsObj, hourlyForecastObjs,
				dailyForecasts);
	}

	public void makeNotification(RemoteViews remoteViews) {
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(notificationType);
		notificationObj.getNotificationBuilder().setOngoing(true);

		notificationObj.getNotificationBuilder().setCustomContentView(remoteViews);
		notificationUpdateCallback.updateNotification(remoteViews);

		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		Notification notification = notificationObj.getNotificationBuilder().build();
		notificationManager.notify(notificationObj.getNotificationId(), notification);
	}

	public void setHeaderViews(RemoteViews remoteViews, HeaderObj headerObj) {
		if (headerObj == null) {
			return;
		}
		remoteViews.setTextViewText(R.id.address, headerObj.getAddress());
		remoteViews.setTextViewText(R.id.refreshDateTime, ZonedDateTime.parse(headerObj.getRefreshDateTime()).format(dateTimeFormatter));
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

		for (int i = 0; i < 6; i++) {
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
		locationType = LocationType.valueOf(sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(),
				LocationType.CurrentLocation.name()));
		weatherSourceType = WeatherSourceType.valueOf(sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name()));
		kmaTopPriority = sharedPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), kmaTopPriority);
		updateInterval = sharedPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), updateInterval);
		selectedAddressDtoId = sharedPreferences.getInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
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
		void updateNotification(RemoteViews remoteViews);
	}
}
