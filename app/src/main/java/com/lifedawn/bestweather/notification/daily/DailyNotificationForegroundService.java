package com.lifedawn.bestweather.notification.daily;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.daily.viewcreator.AbstractDailyNotiViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FifthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FirstDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FourthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.SecondDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.ThirdDailyNotificationViewCreator;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyNotificationForegroundService extends Service {
	private DailyPushNotificationRepository repository;
	private AbstractDailyNotiViewCreator viewCreator;

	public DailyNotificationForegroundService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		repository = new DailyPushNotificationRepository(getApplicationContext());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void showNotification() {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.ForegroundService);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getString(R.string.msg_refreshing_weather_data)).setContentTitle(getString(R.string.msg_refreshing_weather_data))
				.setOnlyAlertOnce(true).setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_DEFAULT).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		startForeground((int) System.currentTimeMillis(), notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		showNotification();
		final String action = intent.getAction();
		if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			Bundle bundle = intent.getExtras();
			final Integer id = bundle.getInt(BundleKey.dtoId.name());
			final DailyPushNotificationType dailyPushNotificationType = DailyPushNotificationType.valueOf(bundle.getString(
					"DailyPushNotificationType"));

			workNotification(getApplicationContext(), Executors.newSingleThreadExecutor(), id, dailyPushNotificationType);
		}
		return START_NOT_STICKY;
	}

	public void loadCurrentLocation(Context context, ExecutorService executorService, RemoteViews remoteViews,
	                                DailyPushNotificationDto dailyPushNotificationDto) {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

		FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
				final Location location = getBestLocation(locationResult);
				Geocoding.nominatimReverseGeocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.ReverseGeocodingCallback() {
					@Override
					public void onReverseGeocodingResult(Geocoding.AddressDto address) {
						dailyPushNotificationDto.setAddressName(address.displayName);
						dailyPushNotificationDto.setCountryCode(address.countryCode);
						dailyPushNotificationDto.setLatitude(address.latitude);
						dailyPushNotificationDto.setLongitude(address.longitude);

						repository.update(dailyPushNotificationDto, null);
						loadWeatherData(context, executorService, remoteViews, dailyPushNotificationDto);
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());

				String failText = null;

				if (fail == Fail.DENIED_LOCATION_PERMISSIONS) {
					failText = getString(R.string.message_needs_location_permission);
				} else if (fail == Fail.DISABLED_GPS) {
					failText = getString(R.string.request_to_make_gps_on);
				} else if (fail == Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
					failText = getString(R.string.message_needs_background_location_permission);
				} else {
					failText = getString(R.string.failedFindingLocation);
				}

				viewCreator.makeFailedNotification(dailyPushNotificationDto.getId(), failText);
				wakeLock();
			}
		};

		FusedLocation fusedLocation = FusedLocation.getInstance(context);
		fusedLocation.startForeground(this);
		fusedLocation.findCurrentLocation(locationCallback, true);
	}

	public void loadWeatherData(Context context, ExecutorService executorService, RemoteViews remoteViews,
	                            DailyPushNotificationDto dailyPushNotificationDto) {
		final Set<WeatherDataType> weatherDataTypeSet = viewCreator.getRequestWeatherDataTypeSet();
		final Set<WeatherProviderType> weatherProviderTypeSet = dailyPushNotificationDto.getWeatherProviderTypeSet();

		if (dailyPushNotificationDto.isTopPriorityKma() && dailyPushNotificationDto.getCountryCode().equals("KR")) {
			if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
				weatherProviderTypeSet.remove(WeatherProviderType.OWM_ONECALL);
				weatherProviderTypeSet.add(WeatherProviderType.KMA_WEB);
			}
		}

		WeatherRequestUtil.loadWeatherData(context, executorService,
				dailyPushNotificationDto.getLatitude(), dailyPushNotificationDto.getLongitude(), weatherDataTypeSet, new WeatherRestApiDownloader() {
					@Override
					public void onResult() {
						viewCreator.setResultViews(remoteViews, dailyPushNotificationDto, weatherProviderTypeSet, this, weatherDataTypeSet);
						wakeLock();
					}

					@Override
					public void onCanceled() {
					}
				}, weatherProviderTypeSet, null);

	}

	private void wakeLock() {
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
						PowerManager.ACQUIRE_CAUSES_WAKEUP |
						PowerManager.ON_AFTER_RELEASE,
				"TAG:WAKE_NOTIFICATION");
		wakeLock.acquire(4000L);
		wakeLock.release();
	}

	public void workNotification(Context context, ExecutorService executorService, Integer notificationDtoId, DailyPushNotificationType type) {
		repository = new DailyPushNotificationRepository(context);
		repository.get(notificationDtoId, new DbQueryCallback<DailyPushNotificationDto>() {
			@Override
			public void onResultSuccessful(DailyPushNotificationDto dto) {
				switch (type) {
					case First:
						viewCreator = new FirstDailyNotificationViewCreator(context);
						break;
					case Second:
						viewCreator = new SecondDailyNotificationViewCreator(context);
						break;
					case Third:
						viewCreator = new ThirdDailyNotificationViewCreator(context);
						break;
					case Fourth:
						viewCreator = new FourthDailyNotificationViewCreator(context);
						break;
					default:
						viewCreator = new FifthDailyNotificationViewCreator(context);
				}
				viewCreator.setBackgroundCallback(new Callback() {
					@Override
					public void onResult() {
						stopForeground(true);
						stopSelf();
					}
				});

				RemoteViews remoteViews = viewCreator.createRemoteViews(false);
				if (dto.getLocationType() == LocationType.CurrentLocation) {
					loadCurrentLocation(context, executorService, remoteViews, dto);
				} else {
					loadWeatherData(context, executorService, remoteViews, dto);
				}
			}

			@Override
			public void onResultNoData() {

			}
		});

	}
}