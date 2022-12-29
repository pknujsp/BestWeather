package com.lifedawn.bestweather.notification.ongoing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.text.TextPaint;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.constants.WeatherDataType;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.constants.WidgetNotiConstants;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.data.remote.retrofit.callback.WeatherRestApiDownloader;
import com.lifedawn.bestweather.commons.utils.DeviceUtils;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

public class OngoingNotificationProcessor {
	private static OngoingNotificationProcessor INSTANCE;

	public static OngoingNotificationProcessor getINSTANCE() {
		if (INSTANCE == null)
			INSTANCE = new OngoingNotificationProcessor();
		return INSTANCE;
	}

	private OngoingNotificationProcessor() {

	}

	public void loadCurrentLocation(Context context, OngoingNotificationDto ongoingNotificationDto, RemoteViews collapsedRemoteViews,
	                                RemoteViews expandedRemoteViews, BackgroundWorkCallback backgroundWorkCallback) {
		FusedLocation fusedLocation = new FusedLocation(context);

		final FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				ZoneId zoneId = ZoneId.of(PreferenceManager.getDefaultSharedPreferences(context).getString("zoneId", ""));
				final Location location = getBestLocation(locationResult);
				Geocoding.nominatimReverseGeocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.ReverseGeocodingCallback() {
					@Override
					public void onReverseGeocodingResult(Geocoding.AddressDto address) {
						ongoingNotificationDto.setDisplayName(address.displayName);
						ongoingNotificationDto.setCountryCode(address.countryCode);
						ongoingNotificationDto.setLatitude(address.latitude);
						ongoingNotificationDto.setLongitude(address.longitude);
						ongoingNotificationDto.setZoneId(zoneId.getId());

						loadWeatherData(context, ongoingNotificationDto, collapsedRemoteViews, expandedRemoteViews, backgroundWorkCallback);
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

				OngoingNotiViewCreator ongoingNotiViewCreator = new OngoingNotiViewCreator(context, null);
				RemoteViews[] remoteViews = ongoingNotiViewCreator.createFailedNotification(errorType);
				makeNotification(context, ongoingNotificationDto, remoteViews[0], remoteViews[1], R.mipmap.ic_launcher_round, null, true,
						backgroundWorkCallback);
			}
		};

		if (DeviceUtils.Companion.isScreenOn(context)) {
			fusedLocation.findCurrentLocation(locationCallback, false);
		} else {
			LocationResult lastLocation = fusedLocation.getLastCurrentLocation();
			if (lastLocation.getLocations().get(0).getLatitude() == 0.0 ||
					lastLocation.getLocations().get(0).getLongitude() == 0.0) {
				fusedLocation.findCurrentLocation(locationCallback, false);
			} else {
				locationCallback.onSuccessful(lastLocation);
			}
		}

	}


	public void loadWeatherData(Context context, OngoingNotificationDto ongoingNotificationDto, RemoteViews collapsedRemoteViews,
	                            RemoteViews expandedRemoteViews, BackgroundWorkCallback backgroundWorkCallback) {
		RemoteViewsUtil.onBeginProcess(expandedRemoteViews);
		RemoteViewsUtil.onBeginProcess(collapsedRemoteViews);
		makeNotification(context, ongoingNotificationDto, collapsedRemoteViews, expandedRemoteViews, R.mipmap.ic_launcher_round, null, false, null);

		final Set<WeatherDataType> weatherDataTypeSet = getRequestWeatherDataTypeSet();
		WeatherProviderType weatherProviderType = ongoingNotificationDto.getWeatherSourceType();

		if (ongoingNotificationDto.isTopPriorityKma() && ongoingNotificationDto.getCountryCode().equals("KR")) {
			weatherProviderType = WeatherProviderType.KMA_WEB;
		}

		final Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();
		weatherProviderTypeSet.add(weatherProviderType);
		if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
			weatherProviderTypeSet.add(WeatherProviderType.AQICN);
		}

		final ZoneId zoneId = ZoneId.of(ongoingNotificationDto.getZoneId());
		final OngoingNotiViewCreator ongoingNotiViewCreator = new OngoingNotiViewCreator(context, ongoingNotificationDto);
		final WeatherProviderType finalWeatherProviderType = weatherProviderType;

		WeatherRequestUtil.loadWeatherData(context, MyApplication.getExecutorService(),
				ongoingNotificationDto.getLatitude(), ongoingNotificationDto.getLongitude(), weatherDataTypeSet,
				new WeatherRestApiDownloader() {
					@Override
					public void onResult() {
						ongoingNotiViewCreator.setResultViews(collapsedRemoteViews, expandedRemoteViews, finalWeatherProviderType, this,
								new OngoingNotiViewCreator.OnRemoteViewsCallback() {
									@Override
									public void onCreateFinished(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews, int icon, @Nullable String temperature, boolean isFinished) {
										makeNotification(context, ongoingNotificationDto, collapsedRemoteViews, expandedRemoteViews, icon, temperature, isFinished, backgroundWorkCallback);
									}
								});
					}

					@Override
					public void onCanceled() {
						ongoingNotiViewCreator.setResultViews(collapsedRemoteViews, expandedRemoteViews, finalWeatherProviderType, this,
								new OngoingNotiViewCreator.OnRemoteViewsCallback() {
									@Override
									public void onCreateFinished(RemoteViews collapsedRemoteViews, RemoteViews expandedRemoteViews, int icon, @Nullable String temperature, boolean isFinished) {
										makeNotification(context, ongoingNotificationDto, collapsedRemoteViews, expandedRemoteViews, icon,
												temperature, isFinished, backgroundWorkCallback);
									}
								});
					}
				}, weatherProviderTypeSet, zoneId);
	}


	public void makeNotification(Context context, OngoingNotificationDto ongoingNotificationDto, RemoteViews collapsedRemoteViews,
	                             RemoteViews expandedRemoteViews, int icon,
	                             @Nullable String temperature, boolean isFinished, @Nullable BackgroundWorkCallback backgroundWorkCallback) {
		NotificationHelper notificationHelper = new NotificationHelper(context);

		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Ongoing);
		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();

		if (isFinished) {
			if (temperature != null) {
				if (ongoingNotificationDto.getDataTypeOfIcon() == WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE) {
					final int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f,
							context.getResources().getDisplayMetrics());

					TextPaint textPaint = new TextPaint();
					textPaint.setColor(Color.WHITE);
					textPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
					textPaint.setTextAlign(Paint.Align.CENTER);
					textPaint.setTextScaleX(0.9f);
					textPaint.setAntiAlias(true);
					textPaint.setTextSize(textSize);

					Rect textRect = new Rect();
					textPaint.getTextBounds(temperature, 0, temperature.length(), textRect);

					final int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f,
							context.getResources().getDisplayMetrics());
					Bitmap iconBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(iconBitmap);
					canvas.drawText(temperature, canvas.getWidth() / 2f, canvas.getHeight() / 2f + textRect.height() / 2f, textPaint);

					builder.setSmallIcon(IconCompat.createWithBitmap(iconBitmap));
				} else {
					builder.setSmallIcon(icon);
				}
			} else {
				builder.setSmallIcon(icon);
			}
		} else {
			builder.setSmallIcon(icon);
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setDefaults(0).setVibrate(null).setSound(null).setLights(0, 0, 0)
					.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			builder.setAutoCancel(false).setOngoing(true).setOnlyAlertOnce(true)
					.setCustomContentView(collapsedRemoteViews).setCustomBigContentView(expandedRemoteViews).setSilent(true);

		} else {
			builder.setAutoCancel(false).setOngoing(true).setOnlyAlertOnce(true)
					.setCustomContentView(expandedRemoteViews).setCustomBigContentView(expandedRemoteViews).setSilent(true);
		}

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.notify(NotificationType.Ongoing.getNotificationId(), builder.build());

		if (isFinished && backgroundWorkCallback != null)
			backgroundWorkCallback.onFinished();
	}


	protected Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.airQuality);
		set.add(WeatherDataType.hourlyForecast);
		return set;
	}
}
