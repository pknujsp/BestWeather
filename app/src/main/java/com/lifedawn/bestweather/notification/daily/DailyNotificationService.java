package com.lifedawn.bestweather.notification.daily;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
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
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class DailyNotificationService extends Service {
	private DailyPushNotificationRepository repository;
	private DailyPushNotificationDto dailyPushNotificationDto;
	private AbstractDailyNotiViewCreator viewCreator = null;

	public DailyNotificationService() {
		super();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle bundle = intent.getExtras();
		final int notificationDtoId = bundle.getInt(BundleKey.dtoId.name());
		final DailyPushNotificationType type = DailyPushNotificationType.valueOf(bundle.getString("DailyPushNotificationType"));

		repository = new DailyPushNotificationRepository(getApplicationContext());
		repository.get(notificationDtoId, new DbQueryCallback<DailyPushNotificationDto>() {
			@Override
			public void onResultSuccessful(DailyPushNotificationDto dto) {
				dailyPushNotificationDto = dto;
				switch (type) {
					case First:
						viewCreator = new FirstDailyNotificationViewCreator(getApplicationContext());
						break;
					case Second:
						viewCreator = new SecondDailyNotificationViewCreator(getApplicationContext());
						break;
					case Third:
						viewCreator = new ThirdDailyNotificationViewCreator(getApplicationContext());
						break;
					case Fourth:
						viewCreator = new FourthDailyNotificationViewCreator(getApplicationContext());
						break;
					default:
						viewCreator = new FifthDailyNotificationViewCreator(getApplicationContext());
				}

				RemoteViews remoteViews = viewCreator.createRemoteViews(false);
				if (dto.getLocationType() == LocationType.CurrentLocation) {
					loadCurrentLocation(remoteViews, dto);
				} else {
					loadWeatherData(remoteViews, dto);
				}


			}

			@Override
			public void onResultNoData() {

			}
		});


		return super.onStartCommand(intent, flags, startId);
	}

	public void loadCurrentLocation(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto) {
		Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {
				Geocoding.geocoding(getApplicationContext(), location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						Address address = addressList.get(0);

						dailyPushNotificationDto.setAddressName(address.getAddressLine(0));
						dailyPushNotificationDto.setCountryCode(address.getCountryCode());
						dailyPushNotificationDto.setLatitude(address.getLatitude());
						dailyPushNotificationDto.setLongitude(address.getLongitude());

						repository.update(dailyPushNotificationDto, null);
						loadWeatherData(remoteViews, dailyPushNotificationDto);
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
					intent.setData(Uri.fromParts("package", getApplicationContext().getPackageName(), null));
				} else {
					errorType = RemoteViewProcessor.ErrorType.GPS_OFF;
					intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				}
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, PendingIntent.getActivity(getApplicationContext(), 100,
						intent, PendingIntent.FLAG_UPDATE_CURRENT));
				RemoteViewProcessor.onErrorProcess(remoteViews, getApplicationContext(), errorType);
				viewCreator.makeNotification(remoteViews, dailyPushNotificationDto.getId());
			}
		};

		Gps gps = new Gps(getApplicationContext(), null, null, null);
		if (gps.checkPermissionAndGpsEnabled(getApplicationContext(), locationCallback)) {
			gps.runGps(getApplicationContext(), locationCallback);
		}
	}

	public void loadWeatherData(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto) {
		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = viewCreator.getRequestWeatherDataTypeSet();
		final Set<WeatherSourceType> weatherSourceTypeSet = dailyPushNotificationDto.getWeatherSourceTypeSet();

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.airQuality)) {
			weatherSourceTypeSet.add(WeatherSourceType.AQICN);
		}

		WeatherRequestUtil.loadWeatherData(getApplicationContext(), Executors.newSingleThreadExecutor(), dailyPushNotificationDto.getCountryCode(),
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
}