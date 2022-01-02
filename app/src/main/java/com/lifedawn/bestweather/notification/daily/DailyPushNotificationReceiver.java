package com.lifedawn.bestweather.notification.daily;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.notification.daily.viewcreator.AbstractDailyNotiViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FifthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FirstDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FourthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.SecondDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.ThirdDailyNotificationViewCreator;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyPushNotificationReceiver extends BroadcastReceiver {
	private DailyPushNotificationRepository repository;
	private AbstractDailyNotiViewCreator viewCreator = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle arguments = intent.getExtras();

		if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			final ExecutorService executorService = Executors.newFixedThreadPool(2);
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					final Integer id = arguments.getInt(BundleKey.dtoId.name());
					final LocalTime localTime = LocalTime.parse(arguments.getString("time"));
					final DailyPushNotificationType dailyPushNotificationType = DailyPushNotificationType.valueOf(arguments.getString(
							"DailyPushNotificationType"));
					if (localTime.isBefore(LocalTime.now())) {
						executorService.shutdown();
					} else {
						workNotification(context, executorService, id, dailyPushNotificationType);
					}
				}
			});

		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			DailyPushNotificationRepository repository = new DailyPushNotificationRepository(context);
			repository.getAll(new DbQueryCallback<List<DailyPushNotificationDto>>() {
				@Override
				public void onResultSuccessful(List<DailyPushNotificationDto> result) {
					DailyNotiHelper dailyNotiHelper = new DailyNotiHelper(context);
					for (DailyPushNotificationDto notificationDto : result) {
						if (notificationDto.isEnabled()) {
							dailyNotiHelper.enablePushNotification(notificationDto);
						}
					}
				}

				@Override
				public void onResultNoData() {

				}
			});

		}

	}

	public void loadCurrentLocation(Context context, ExecutorService executorService, RemoteViews remoteViews,
	                                DailyPushNotificationDto dailyPushNotificationDto) {
		FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final Location location = locationResult.getLocations().get(0);
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						Address address = addressList.get(0);

						dailyPushNotificationDto.setAddressName(address.getAddressLine(0));
						dailyPushNotificationDto.setCountryCode(address.getCountryCode());
						dailyPushNotificationDto.setLatitude(address.getLatitude());
						dailyPushNotificationDto.setLongitude(address.getLongitude());

						repository.update(dailyPushNotificationDto, null);
						loadWeatherData(context, executorService, remoteViews, dailyPushNotificationDto);
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
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, PendingIntent.getActivity(context, 100,
						intent, PendingIntent.FLAG_UPDATE_CURRENT));
				RemoteViewProcessor.onErrorProcess(remoteViews, context, errorType);
				viewCreator.makeNotification(remoteViews, dailyPushNotificationDto.getId());
			}
		};

		FusedLocation.getInstance(context).startLocationUpdates(locationCallback);
	}

	public void loadWeatherData(Context context, ExecutorService executorService, RemoteViews remoteViews,
	                            DailyPushNotificationDto dailyPushNotificationDto) {
		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = viewCreator.getRequestWeatherDataTypeSet();
		final Set<WeatherSourceType> weatherSourceTypeSet = dailyPushNotificationDto.getWeatherSourceTypeSet();

		WeatherRequestUtil.loadWeatherData(context, executorService, dailyPushNotificationDto.getCountryCode(),
				dailyPushNotificationDto.getLatitude(), dailyPushNotificationDto.getLongitude(), requestWeatherDataTypeSet, new MultipleRestApiDownloader() {
					@Override
					public void onResult() {
						viewCreator.setResultViews(remoteViews, dailyPushNotificationDto, weatherSourceTypeSet, this, requestWeatherDataTypeSet);
					}

					@Override
					public void onCanceled() {
					}
				}, weatherSourceTypeSet);

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