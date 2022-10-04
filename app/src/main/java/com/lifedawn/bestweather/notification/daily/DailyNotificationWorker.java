package com.lifedawn.bestweather.notification.daily;

import android.app.Notification;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.PowerManager;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.LocationResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
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

import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DailyNotificationWorker extends Worker {
	private DailyPushNotificationRepository repository;
	private AbstractDailyNotiViewCreator viewCreator;
	private String action;
	private Integer id;
	private DailyPushNotificationType dailyPushNotificationType;

	public DailyNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		repository = new DailyPushNotificationRepository(context);
		action = workerParams.getInputData().getString("action");
		id = workerParams.getInputData().getInt(BundleKey.dtoId.name(), -1);
		dailyPushNotificationType = DailyPushNotificationType.valueOf(workerParams.getInputData().getString("DailyPushNotificationType"));
	}

	@NonNull
	@Override
	public Result doWork() {
		if (action.equals(getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			workNotification(getApplicationContext(), Executors.newSingleThreadExecutor(), id, dailyPushNotificationType);
		}

		return Result.success();
	}

	@NonNull
	@Override
	public ListenableFuture<ForegroundInfo> getForegroundInfoAsync() {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.ForegroundService);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getApplicationContext().getString(R.string.msg_refreshing_weather_data))
				.setContentTitle(getApplicationContext().getString(R.string.msg_refreshing_weather_data))
				.setOnlyAlertOnce(true).setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_DEFAULT).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		final int notificationId = (int) System.currentTimeMillis();

		ForegroundInfo foregroundInfo = new ForegroundInfo(notificationId, notification);

		return new ListenableFuture<ForegroundInfo>() {
			@Override
			public void addListener(Runnable listener, Executor executor) {

			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public ForegroundInfo get() throws ExecutionException, InterruptedException {
				return foregroundInfo;
			}

			@Override
			public ForegroundInfo get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
				return foregroundInfo;
			}
		};
	}

	@Override
	public void onStopped() {
		super.onStopped();
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


	public void loadCurrentLocation(Context context, ExecutorService executorService, RemoteViews remoteViews,
	                                DailyPushNotificationDto dailyPushNotificationDto) {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		FusedLocation fusedLocation = FusedLocation.getInstance(context);

		FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final ZoneId zoneId = ZoneId.of(PreferenceManager.getDefaultSharedPreferences(context).getString("zoneId", ""));
				notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
				final Location location = getBestLocation(locationResult);
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						final Address address = addressList.get(0);

						dailyPushNotificationDto.setAddressName(address.getAddressLine(0));
						dailyPushNotificationDto.setCountryCode(address.getCountryCode());
						dailyPushNotificationDto.setLatitude(address.getLatitude());
						dailyPushNotificationDto.setLongitude(address.getLongitude());
						dailyPushNotificationDto.setZoneId(zoneId.getId());

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
					failText = getApplicationContext().getString(R.string.message_needs_location_permission);
				} else if (fail == Fail.DISABLED_GPS) {
					failText = getApplicationContext().getString(R.string.request_to_make_gps_on);
				} else if (fail == Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
					failText = getApplicationContext().getString(R.string.message_needs_background_location_permission);
				} else {
					failText = getApplicationContext().getString(R.string.failedFindingLocation);
				}

				viewCreator.makeFailedNotification(dailyPushNotificationDto.getId(), failText);
				wakeLock();
			}
		};

		fusedLocation.startNotification(getApplicationContext());
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
				}, weatherProviderTypeSet, ZoneId.of(dailyPushNotificationDto.getZoneId()));

	}

	private void wakeLock() {
		PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
						PowerManager.ACQUIRE_CAUSES_WAKEUP |
						PowerManager.ON_AFTER_RELEASE,
				"TAG:WAKE_NOTIFICATION");
		wakeLock.acquire(4000L);
		wakeLock.release();
	}
}
