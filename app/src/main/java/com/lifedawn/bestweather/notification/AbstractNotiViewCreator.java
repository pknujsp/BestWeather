package com.lifedawn.bestweather.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.notification.model.NotificationDataObj;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractNotiViewCreator {
	protected final NotificationUpdateCallback notificationUpdateCallback;
	protected final ValueUnits windSpeedUnit;
	protected final DateTimeFormatter dateTimeFormatter;
	protected final ValueUnits tempUnit;
	protected final String tempDegree;
	protected final NotificationType notificationType;

	protected Context context;
	protected NotificationHelper notificationHelper;

	protected NotificationDataObj notificationDataObj;

	public AbstractNotiViewCreator(Context context, NotificationType notificationType, NotificationUpdateCallback notificationUpdateCallback) {
		this.context = context;
		this.notificationUpdateCallback = notificationUpdateCallback;
		this.notificationType = notificationType;

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
	}


	abstract public RemoteViews createRemoteViews(boolean temp);

	abstract public void initNotification();

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

				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, PendingIntent.getActivity(context, 30, intent,
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

	public void loadWeatherData(Context context, RemoteViews remoteViews) {
		RemoteViewProcessor.onBeginProcess(remoteViews);
		makeNotification(remoteViews, R.drawable.temp_icon);

		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();
		WeatherSourceType weatherDataSourceType = notificationDataObj.getWeatherSourceType();
		if (notificationDataObj.isTopPriorityKma() && notificationDataObj.getCountryCode().equals("KR")) {
			weatherDataSourceType = WeatherSourceType.KMA;
		}

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		WeatherSourceType finalWeatherDataSourceType = weatherDataSourceType;
		WeatherRequestUtil.loadWeatherData(context, executorService, notificationDataObj.getCountryCode(),
				notificationDataObj.getLatitude(), notificationDataObj.getLongitude(), requestWeatherDataTypeSet,
				new MultipleJsonDownloader() {
					@Override
					public void onResult() {
						setResultViews(context, remoteViews, finalWeatherDataSourceType, this, requestWeatherDataTypeSet);
					}

					@Override
					public void onCanceled() {

					}
				}, weatherDataSourceType);


	}

	protected PendingIntent getRefreshPendingIntent() {
		Intent refreshIntent = new Intent(context, NotificationReceiver.class);
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
	                                       WeatherSourceType requestWeatherSourceType, @Nullable MultipleJsonDownloader multipleJsonDownloader,
	                                       Set<RequestWeatherDataType> requestWeatherDataTypeSet);

	abstract protected void makeNotification(RemoteViews remoteViews, int icon);

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