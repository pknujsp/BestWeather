package com.lifedawn.bestweather.widget.jobservice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Configuration;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public abstract class AbstractWidgetJobService extends JobService {
	protected WidgetRepository widgetRepository;
	protected AppWidgetManager appWidgetManager;
	protected AbstractWidgetCreator widgetViewCreator;
	protected Handler handler;

	public AbstractWidgetJobService() {
		Configuration.Builder builder = new Configuration.Builder();
		builder.setJobSchedulerJobIdRange(100000, Integer.MAX_VALUE);
	}

	@Override
	public boolean onStartJob(JobParameters params) {
		PersistableBundle bundle = params.getExtras();
		final String action = bundle.getString("action");
		final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		Log.e("jobService", action);

		widgetRepository = new WidgetRepository(getApplicationContext());
		appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

		if (action.equals(getString(R.string.com_lifedawn_bestweather_action_INIT))) {
			handler = new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(@NonNull Message msg) {
					if (msg.obj != null && ((String) msg.obj).equals("finished")) {
						jobFinished(params, false);
						return true;
					}
					return false;
				}
			});
			createWidgetViewCreator(appWidgetId);
			widgetViewCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto widgetDto) {
					WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext(), getWidgetProviderClass());
					if (widgetDto.getUpdateIntervalMillis() > 0) {
						widgetHelper.onSelectedAutoRefreshInterval(widgetDto.getUpdateIntervalMillis(), appWidgetId);
					}

					final RemoteViews remoteViews = widgetViewCreator.createRemoteViews();

					RemoteViewsUtil.onBeginProcess(remoteViews);
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

					NetworkStatus networkStatus = NetworkStatus.getInstance(getApplicationContext());

					if (!networkStatus.networkAvailable()) {
						RemoteViewsUtil.ErrorType errorType = RemoteViewsUtil.ErrorType.UNAVAILABLE_NETWORK;

						setRefreshPendingIntent(remoteViews, appWidgetId);
						RemoteViewsUtil.onErrorProcess(remoteViews, getApplicationContext(), errorType);
						appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

						Message message = handler.obtainMessage();
						message.obj = "finished";
						handler.sendMessage(message);
						return;
					}

					if (widgetDto.getLocationType() == LocationType.CurrentLocation) {
						loadCurrentLocation(getApplicationContext(), appWidgetId, remoteViews);
					} else {
						loadWeatherData(getApplicationContext(), remoteViews, appWidgetId, widgetDto);
					}

					widgetDto.setInitialized(true);
					widgetRepository.update(widgetDto, null);
				}

				@Override
				public void onResultNoData() {

				}
			});
		} else if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			handler = new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(@NonNull Message msg) {
					if (msg.obj != null && ((String) msg.obj).equals("finished")) {
						jobFinished(params, false);
						return true;
					}
					return false;
				}
			});
			createWidgetViewCreator(appWidgetId);
			widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto result) {
					RemoteViews remoteViews = widgetViewCreator.createRemoteViews();
					NetworkStatus networkStatus = NetworkStatus.getInstance(getApplicationContext());
					if (!networkStatus.networkAvailable()) {
						RemoteViewsUtil.ErrorType errorType = RemoteViewsUtil.ErrorType.UNAVAILABLE_NETWORK;

						setRefreshPendingIntent(remoteViews, appWidgetId);
						RemoteViewsUtil.onErrorProcess(remoteViews, getApplicationContext(), errorType);
						appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

						Message message = handler.obtainMessage();
						message.obj = "finished";
						handler.sendMessage(message);
						return;
					}

					RemoteViewsUtil.onBeginProcess(remoteViews);
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

					if (result.getLocationType() == LocationType.CurrentLocation) {
						loadCurrentLocation(getApplicationContext(), appWidgetId, remoteViews);
					} else {
						loadWeatherData(getApplicationContext(), remoteViews, appWidgetId, result);
					}
				}

				@Override
				public void onResultNoData() {

				}
			});
		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			onActionBootCompleted(params);
		}

		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		return false;
	}

	public final void setRefreshPendingIntent(RemoteViews remoteViews, int appWidgetId) {
		widgetViewCreator.setRefreshPendingIntent(getWidgetProviderClass(), remoteViews, appWidgetId);
	}

	public void loadCurrentLocation(Context context, int appWidgetId, RemoteViews remoteViews) {
		FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final Location location = locationResult.getLocations().get(0);
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						if (addressList.isEmpty()) {
							onLocationResult(Fail.FAILED_FIND_LOCATION, appWidgetId, remoteViews);
						} else {
							WidgetRepository widgetRepository = new WidgetRepository(context);
							widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
								@Override
								public void onResultSuccessful(WidgetDto result) {
									final Address address = addressList.get(0);

									result.setAddressName(address.getAddressLine(0));
									result.setCountryCode(address.getCountryCode());
									result.setLatitude(address.getLatitude());
									result.setLongitude(address.getLongitude());

									widgetRepository.update(result, new DbQueryCallback<WidgetDto>() {
										@Override
										public void onResultSuccessful(WidgetDto result) {
											onLocationResult(null, appWidgetId, remoteViews);
										}

										@Override
										public void onResultNoData() {

										}
									});
								}

								@Override
								public void onResultNoData() {

								}
							});

						}
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				onLocationResult(fail, appWidgetId, remoteViews);
			}
		};

		FusedLocation.getInstance(this).startLocationUpdates(locationCallback, true);
	}

	private void onLocationResult(@Nullable FusedLocation.MyLocationCallback.Fail fail, int appWidgetId, RemoteViews remoteViews) {
		final boolean succeed = fail == null;

		if (succeed) {
			WidgetRepository widgetRepository = new WidgetRepository(getApplicationContext());
			widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto result) {
					loadWeatherData(getApplicationContext(), remoteViews, appWidgetId, result);
				}

				@Override
				public void onResultNoData() {

				}
			});

		} else {
			RemoteViewsUtil.ErrorType errorType = null;

			if (fail == FusedLocation.MyLocationCallback.Fail.DENIED_LOCATION_PERMISSIONS) {
				errorType = RemoteViewsUtil.ErrorType.DENIED_GPS_PERMISSIONS;
			} else if (fail == FusedLocation.MyLocationCallback.Fail.DISABLED_GPS) {
				errorType = RemoteViewsUtil.ErrorType.GPS_OFF;
			} else if (fail == FusedLocation.MyLocationCallback.Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
				errorType = RemoteViewsUtil.ErrorType.DENIED_BACKGROUND_LOCATION_PERMISSION;
			} else {
				errorType = RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA;
			}

			setRefreshPendingIntent(remoteViews, appWidgetId);
			RemoteViewsUtil.onErrorProcess(remoteViews, getApplicationContext(), errorType);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

			Message message = handler.obtainMessage();
			message.obj = "finished";
			handler.sendMessage(message);
		}

	}


	public void loadWeatherData(Context context, RemoteViews remoteViews, int appWidgetId, WidgetDto widgetDto) {
		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();
		final Set<WeatherDataSourceType> weatherDataSourceTypeSet = widgetDto.getWeatherSourceTypeSet();

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherDataSourceTypeSet.add(WeatherDataSourceType.KMA_WEB);
		}

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.airQuality)) {
			weatherDataSourceTypeSet.add(WeatherDataSourceType.AQICN);
		}

		WeatherRequestUtil.loadWeatherData(context, Executors.newSingleThreadExecutor(), widgetDto.getCountryCode(), widgetDto.getLatitude(),
				widgetDto.getLongitude(), requestWeatherDataTypeSet, new MultipleRestApiDownloader() {
					@Override
					public void onResult() {
						setResultViews(context, appWidgetId, remoteViews, widgetDto, weatherDataSourceTypeSet, this, requestWeatherDataTypeSet);
					}

					@Override
					public void onCanceled() {
						setRefreshPendingIntent(remoteViews, appWidgetId);
						RemoteViewsUtil.onErrorProcess(remoteViews, widgetViewCreator.getContext(), RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
						appWidgetManager.updateAppWidget(widgetViewCreator.getAppWidgetId(), remoteViews);

						Message message = handler.obtainMessage();
						message.obj = "finished";
						handler.sendMessage(message);
					}
				}, weatherDataSourceTypeSet);
	}

	protected void onActionBootCompleted(JobParameters jobParameters) {
		//위젯 자동 업데이트 재 등록
		widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
			@Override
			public void onResultSuccessful(List<WidgetDto> list) {
				WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext(), getWidgetProviderClass());
				for (WidgetDto widgetDto : list) {
					if (widgetDto.getUpdateIntervalMillis() > 0) {
						widgetHelper.onSelectedAutoRefreshInterval(widgetDto.getUpdateIntervalMillis(), widgetDto.getAppWidgetId());
					}
				}
				jobFinished(jobParameters, false);
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	abstract Class<?> getWidgetProviderClass();

	abstract AbstractWidgetCreator createWidgetViewCreator(int appWidgetId);

	abstract Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();

	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews,
	                              WidgetDto widgetDto, Set<WeatherDataSourceType> requestWeatherDataSourceTypeSet, @Nullable MultipleRestApiDownloader multipleRestApiDownloader,
	                              Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		Message message = handler.obtainMessage();
		message.obj = "finished";
		handler.sendMessage(message);
	}
}
