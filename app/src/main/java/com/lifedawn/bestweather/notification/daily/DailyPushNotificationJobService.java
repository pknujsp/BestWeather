package com.lifedawn.bestweather.notification.daily;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyPushNotificationJobService extends JobService {
	private DailyPushNotificationRepository repository;
	private AbstractDailyNotiViewCreator viewCreator = null;
	private Handler handler;

	public DailyPushNotificationJobService() {
		Configuration.Builder builder = new Configuration.Builder();
		builder.setJobSchedulerJobIdRange(0, Integer.MAX_VALUE);
	}

	@Override
	public boolean onStartJob(JobParameters params) {
		PersistableBundle bundle = params.getExtras();
		final String action = bundle.getString("action");
		DailyPushNotificationRepository repository = new DailyPushNotificationRepository(getApplicationContext());
		DailyNotiHelper dailyNotiHelper = new DailyNotiHelper(getApplicationContext());

		if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			final Integer id = bundle.getInt(BundleKey.dtoId.name());
			final DailyPushNotificationType dailyPushNotificationType = DailyPushNotificationType.valueOf(bundle.getString(
					"DailyPushNotificationType"));

			repository.get(id, new DbQueryCallback<DailyPushNotificationDto>() {
				@Override
				public void onResultSuccessful(DailyPushNotificationDto result) {
					dailyNotiHelper.enablePushNotification(result);
				}

				@Override
				public void onResultNoData() {

				}
			});

			handler = new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(@NonNull Message msg) {
					if (msg.obj != null) {
						if (((String) msg.obj).equals("finished")) {
							jobFinished(params, false);
							return true;
						}
					}
					return false;
				}
			});

			workNotification(getApplicationContext(), Executors.newSingleThreadExecutor(), id, dailyPushNotificationType);
		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			repository.getAll(new DbQueryCallback<List<DailyPushNotificationDto>>() {
				@Override
				public void onResultSuccessful(List<DailyPushNotificationDto> result) {
					for (DailyPushNotificationDto notificationDto : result) {
						if (notificationDto.isEnabled()) {
							dailyNotiHelper.enablePushNotification(notificationDto);
						}
					}
					jobFinished(params, false);
				}

				@Override
				public void onResultNoData() {

				}
			});

		}
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		return false;
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
						final Address address = addressList.get(0);

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

				RemoteViewsUtil.onErrorProcess(remoteViews, context, errorType);
				remoteViews.setViewVisibility(R.id.refreshBtn, View.GONE);
				viewCreator.makeNotification(remoteViews, dailyPushNotificationDto.getId());
			}
		};

		FusedLocation.getInstance(context).startLocationUpdates(locationCallback,true );
	}

	public void loadWeatherData(Context context, ExecutorService executorService, RemoteViews remoteViews,
	                            DailyPushNotificationDto dailyPushNotificationDto) {
		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = viewCreator.getRequestWeatherDataTypeSet();
		final Set<WeatherDataSourceType> weatherDataSourceTypeSet = dailyPushNotificationDto.getWeatherSourceTypeSet();

		WeatherRequestUtil.loadWeatherData(context, executorService, dailyPushNotificationDto.getCountryCode(),
				dailyPushNotificationDto.getLatitude(), dailyPushNotificationDto.getLongitude(), requestWeatherDataTypeSet, new MultipleRestApiDownloader() {
					@Override
					public void onResult() {
						viewCreator.setResultViews(remoteViews, dailyPushNotificationDto, weatherDataSourceTypeSet, this, requestWeatherDataTypeSet);
					}

					@Override
					public void onCanceled() {
					}
				}, weatherDataSourceTypeSet);

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
				viewCreator.setHandler(handler);

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
