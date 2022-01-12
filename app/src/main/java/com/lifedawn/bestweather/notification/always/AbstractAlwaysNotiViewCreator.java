package com.lifedawn.bestweather.notification.always;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
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

public abstract class AbstractAlwaysNotiViewCreator {
	protected final NotificationUpdateCallback notificationUpdateCallback;
	protected final ValueUnits windSpeedUnit;
	protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");
	protected final ValueUnits tempUnit;
	protected final String tempDegree;
	protected final NotificationType notificationType;

	protected Context context;
	protected NotificationHelper notificationHelper;
	protected NotificationDataObj notificationDataObj;

	public AbstractAlwaysNotiViewCreator(Context context, NotificationType notificationType, NotificationUpdateCallback notificationUpdateCallback) {
		this.context = context;
		this.notificationUpdateCallback = notificationUpdateCallback;
		this.notificationType = notificationType;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		tempDegree = "°";
		windSpeedUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		notificationHelper = new NotificationHelper(context);
	}


	abstract public RemoteViews createRemoteViews(boolean temp);

	abstract public void initNotification(Handler handler);

	public void loadCurrentLocation(Context context, RemoteViews remoteViews) {

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

						loadWeatherData(context, remoteViews);
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				RemoteViewProcessor.ErrorType errorType = null;

				if (fail == Fail.REJECT_PERMISSION) {
					errorType = RemoteViewProcessor.ErrorType.GPS_PERMISSION_REJECTED;
				} else if (fail == Fail.DISABLED_GPS) {
					errorType = RemoteViewProcessor.ErrorType.GPS_OFF;
				} else {
					errorType = RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA;
				}

				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, getRefreshPendingIntent());
				RemoteViewProcessor.onErrorProcess(remoteViews, context, errorType);
				makeNotification(remoteViews, R.drawable.temp_icon, true);
			}
		};

		FusedLocation.getInstance(context).startLocationUpdates(locationCallback);
	}


	public void loadWeatherData(Context context, RemoteViews remoteViews) {
		RemoteViewProcessor.onBeginProcess(remoteViews);
		makeNotification(remoteViews, R.drawable.temp_icon, false);

		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();
		WeatherDataSourceType weatherDataSourceType = notificationDataObj.getWeatherSourceType();

		if (notificationDataObj.isTopPriorityKma() && notificationDataObj.getCountryCode().equals("KR")) {
			weatherDataSourceType = WeatherDataSourceType.KMA_WEB;
		}

		final Set<WeatherDataSourceType> weatherDataSourceTypeSet = new HashSet<>();
		weatherDataSourceTypeSet.add(weatherDataSourceType);
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.airQuality)) {
			weatherDataSourceTypeSet.add(WeatherDataSourceType.AQICN);
		}

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		WeatherDataSourceType finalWeatherDataSourceType = weatherDataSourceType;
		WeatherRequestUtil.loadWeatherData(context, executorService, notificationDataObj.getCountryCode(),
				notificationDataObj.getLatitude(), notificationDataObj.getLongitude(), requestWeatherDataTypeSet,
				new MultipleRestApiDownloader() {
					@Override
					public void onResult() {
						setResultViews(context, remoteViews, finalWeatherDataSourceType, this, requestWeatherDataTypeSet);
					}

					@Override
					public void onCanceled() {

					}
				}, weatherDataSourceTypeSet);


	}

	protected PendingIntent getRefreshPendingIntent() {
		Intent refreshIntent = new Intent(context, AlwaysNotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putString(NotificationType.class.getName(), notificationType.name());

		refreshIntent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10551, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	abstract protected Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();

	abstract protected void setResultViews(Context context, RemoteViews remoteViews,
	                                       WeatherDataSourceType requestWeatherDataSourceType, @Nullable MultipleRestApiDownloader multipleRestApiDownloader,
	                                       Set<RequestWeatherDataType> requestWeatherDataTypeSet);

	abstract protected void makeNotification(RemoteViews remoteViews, int icon, boolean isFinished);

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
