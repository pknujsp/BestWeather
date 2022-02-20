package com.lifedawn.bestweather.notification.ongoing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.commons.interfaces.BackgroundCallback;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.NotificationUpdateCallback;
import com.lifedawn.bestweather.notification.model.NotificationDataObj;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractOngoingNotiViewCreator {
	protected final NotificationUpdateCallback notificationUpdateCallback;
	protected final ValueUnits windSpeedUnit;
	protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	protected final ValueUnits tempUnit;
	protected final String tempDegree;
	protected final NotificationType notificationType;

	protected Context context;
	protected NotificationHelper notificationHelper;
	protected NotificationDataObj notificationDataObj;

	public AbstractOngoingNotiViewCreator(Context context, NotificationType notificationType, NotificationUpdateCallback notificationUpdateCallback) {
		this.context = context;
		this.notificationUpdateCallback = notificationUpdateCallback;
		this.notificationType = notificationType;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		tempDegree = ValueUnits.convertToStr(null, tempUnit);
		windSpeedUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		notificationHelper = new NotificationHelper(context);
	}


	abstract public RemoteViews[] createRemoteViews(boolean temp);

	abstract public void initNotification(BackgroundCallback backgroundCallback);

	public void loadCurrentLocation(Context context, RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews) {
		FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final Location location = locationResult.getLocations().get(0);
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						final SharedPreferences sharedPreferences =
								context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						Address address = addressList.get(0);
						notificationDataObj.setAddressName(address.getAddressLine(0))
								.setCountryCode(address.getCountryCode())
								.setLatitude(address.getLatitude()).setLongitude(address.getLongitude());

						editor.putFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), (float) notificationDataObj.getLatitude())
								.putFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(),
										(float) notificationDataObj.getLongitude())
								.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), notificationDataObj.getCountryCode())
								.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), notificationDataObj.getAddressName()).commit();

						loadWeatherData(context, collapsedRemoteViews, expandedRemoteViews);
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				RemoteViewsUtil.ErrorType errorType = null;

				if (fail == Fail.DENIED_LOCATION_PERMISSIONS) {
					errorType = RemoteViewsUtil.ErrorType.DENIED_GPS_PERMISSIONS;
				} else if (fail == Fail.DISABLED_GPS) {
					errorType = RemoteViewsUtil.ErrorType.GPS_OFF;
				} else if (fail == Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
					errorType = RemoteViewsUtil.ErrorType.DENIED_BACKGROUND_LOCATION_PERMISSION;
				} else {
					errorType = RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA;
				}

				expandedRemoteViews.setOnClickPendingIntent(R.id.refreshBtn, getRefreshPendingIntent());
				RemoteViewsUtil.onErrorProcess(expandedRemoteViews, context, errorType);

				makeNotification(collapsedRemoteViews, expandedRemoteViews, R.mipmap.ic_launcher_round, true);
			}
		};

		FusedLocation.getInstance(context).findCurrentLocation(locationCallback, false);
	}


	public void loadWeatherData(Context context, RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews) {
		RemoteViewsUtil.onBeginProcess(expandedRemoteViews);
		makeNotification(collapsedRemoteViews, expandedRemoteViews, R.mipmap.ic_launcher_round, false);

		final Set<WeatherDataType> weatherDataTypeSet = getRequestWeatherDataTypeSet();
		WeatherDataSourceType weatherDataSourceType = notificationDataObj.getWeatherSourceType();

		if (notificationDataObj.isTopPriorityKma() && notificationDataObj.getCountryCode().equals("KR")) {
			weatherDataSourceType = WeatherDataSourceType.KMA_WEB;
		}

		final Set<WeatherDataSourceType> weatherDataSourceTypeSet = new HashSet<>();
		weatherDataSourceTypeSet.add(weatherDataSourceType);
		if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
			weatherDataSourceTypeSet.add(WeatherDataSourceType.AQICN);
		}

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		WeatherDataSourceType finalWeatherDataSourceType = weatherDataSourceType;
		WeatherRequestUtil.loadWeatherData(context, executorService,
				notificationDataObj.getLatitude(), notificationDataObj.getLongitude(), weatherDataTypeSet,
				new MultipleRestApiDownloader() {
					@Override
					public void onResult() {
						setResultViews(context, collapsedRemoteViews, expandedRemoteViews, finalWeatherDataSourceType, this, weatherDataTypeSet);
					}

					@Override
					public void onCanceled() {

					}
				}, weatherDataSourceTypeSet);
	}

	protected PendingIntent getRefreshPendingIntent() {
		Intent refreshIntent = new Intent(context, OngoingNotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putString(NotificationType.class.getName(), notificationType.name());

		refreshIntent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10551, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	abstract protected Set<WeatherDataType> getRequestWeatherDataTypeSet();

	abstract protected void setResultViews(Context context, RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews,
	                                       WeatherDataSourceType requestWeatherDataSourceType, @Nullable MultipleRestApiDownloader multipleRestApiDownloader,
	                                       Set<WeatherDataType> weatherDataTypeSet);

	abstract protected void makeNotification(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews, int icon, boolean isFinished);

	abstract public void loadSavedPreferences();

	abstract public void loadDefaultPreferences();

	public void loadPreferences() {
		SharedPreferences notiPreferences = context.getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);
		if (notiPreferences.getAll().isEmpty()) {
			loadDefaultPreferences();
		} else {
			loadSavedPreferences();
		}
	}

	abstract public void savePreferences();

	protected NotificationDataObj getNotificationDataObj() {
		return notificationDataObj;
	}
}