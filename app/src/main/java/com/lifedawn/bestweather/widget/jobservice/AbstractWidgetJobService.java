package com.lifedawn.bestweather.widget.jobservice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.work.Configuration;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractWidgetJobService extends JobService {
	protected WidgetRepository widgetRepository;
	protected AppWidgetManager appWidgetManager;
	protected Map<Integer, AbstractWidgetCreator> widgetCreatorMap = new HashMap<>();
	protected Map<Integer, Callback> backgroundCallbackMap = new HashMap<>();
	protected Map<Integer, Class<?>> widgetClassMap = new HashMap<>();
	protected ExecutorService executorService = Executors.newFixedThreadPool(4);

	private static final String TAG = "AbstractWidgetJobService";

	public AbstractWidgetJobService() {
		Configuration.Builder builder = new Configuration.Builder();
		builder.setJobSchedulerJobIdRange(0, Integer.MAX_VALUE);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (widgetRepository == null) {
			widgetRepository = new WidgetRepository(getApplicationContext());
		}

		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onStartJob(JobParameters params) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				PersistableBundle bundle = params.getExtras();
				final int jobId = params.getJobId();
				final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
				final String action = bundle.getString("action");
				final String widgetProviderClassName = bundle.getString("widgetProviderClassName");

				Class<?> widgetClass = null;

				try {
					widgetClass = Class.forName(widgetProviderClassName);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				widgetClassMap.put(jobId, widgetClass);

				if (action.equals(getString(R.string.com_lifedawn_bestweather_action_INIT))) {
					addBackgroundCallback(params);
					final AbstractWidgetCreator widgetViewCreator = createWidgetViewCreator(appWidgetId, jobId);
					widgetViewCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
						@Override
						public void onResultSuccessful(WidgetDto widgetDto) {
							WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
							long widgetRefreshInterval = widgetHelper.getRefreshInterval();

							if (widgetRefreshInterval > 0 && !widgetHelper.isRepeating()) {
								widgetHelper.onSelectedAutoRefreshInterval(widgetRefreshInterval);
							}

							final RemoteViews remoteViews = widgetViewCreator.createRemoteViews();

							RemoteViewsUtil.onBeginProcess(remoteViews);
							appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

							NetworkStatus networkStatus = NetworkStatus.getInstance(getApplicationContext());

							if (!networkStatus.networkAvailable()) {
								RemoteViewsUtil.ErrorType errorType = RemoteViewsUtil.ErrorType.UNAVAILABLE_NETWORK;

								setRefreshPendingIntent(remoteViews, appWidgetId, jobId);
								RemoteViewsUtil.onErrorProcess(remoteViews, getApplicationContext(), errorType);
								appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

								backgroundCallbackMap.get(jobId).onResult();
								return;
							}

							if (widgetDto.getLocationType() == LocationType.CurrentLocation) {
								loadCurrentLocation(getApplicationContext(), appWidgetId, remoteViews, jobId);
							} else {
								loadWeatherData(getApplicationContext(), remoteViews, appWidgetId, widgetDto, jobId);
							}

							widgetRepository.update(widgetDto, null);
						}

						@Override
						public void onResultNoData() {

						}
					});
				} else if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
					addBackgroundCallback(params);

					widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
						@Override
						public void onResultSuccessful(WidgetDto result) {
							final RemoteViews remoteViews = createWidgetViewCreator(appWidgetId, jobId).createRemoteViews();
							NetworkStatus networkStatus = NetworkStatus.getInstance(getApplicationContext());

							if (!networkStatus.networkAvailable()) {
								RemoteViewsUtil.ErrorType errorType = RemoteViewsUtil.ErrorType.UNAVAILABLE_NETWORK;

								setRefreshPendingIntent(remoteViews, appWidgetId, jobId);
								RemoteViewsUtil.onErrorProcess(remoteViews, getApplicationContext(), errorType);
								appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

								backgroundCallbackMap.get(jobId).onResult();
							} else {
								RemoteViewsUtil.onBeginProcess(remoteViews);
								appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

								if (result.getLocationType() == LocationType.CurrentLocation) {
									MainThreadWorker.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											loadCurrentLocation(getApplicationContext(), appWidgetId, remoteViews, jobId);
										}
									});
								} else {
									loadWeatherData(getApplicationContext(), remoteViews, appWidgetId, result, jobId);
								}
							}
						}

						@Override
						public void onResultNoData() {

						}
					});
				} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
					onActionBootCompleted(params);
				}

			}
		});

		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		return false;
	}

	public final void setRefreshPendingIntent(RemoteViews remoteViews, int appWidgetId, int jobId) {
		widgetCreatorMap.get(jobId).setRefreshPendingIntent(widgetClassMap.get(jobId), remoteViews);
	}

	public void loadCurrentLocation(Context context, int appWidgetId, RemoteViews remoteViews, int jobId) {
		FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final Location location = locationResult.getLocations().get(0);
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						if (addressList.isEmpty()) {
							onLocationResult(Fail.FAILED_FIND_LOCATION, appWidgetId, remoteViews, jobId);
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
											onLocationResult(null, appWidgetId, remoteViews, jobId);
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
				onLocationResult(fail, appWidgetId, remoteViews, jobId);
			}
		};

		FusedLocation.getInstance(this).findCurrentLocation(locationCallback, true);
	}

	private void onLocationResult(@Nullable FusedLocation.MyLocationCallback.Fail fail, int appWidgetId, RemoteViews remoteViews, int jobId) {
		final boolean succeed = (fail == null);

		if (succeed) {
			widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto result) {
					loadWeatherData(getApplicationContext(), remoteViews, appWidgetId, result, jobId);
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

			Log.e("location", fail.name());

			setRefreshPendingIntent(remoteViews, appWidgetId, jobId);
			RemoteViewsUtil.onErrorProcess(remoteViews, getApplicationContext(), errorType);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

			backgroundCallbackMap.get(jobId).onResult();

		}

	}


	public void loadWeatherData(Context context, RemoteViews remoteViews, int appWidgetId, WidgetDto widgetDto, int jobId) {
		final Set<WeatherDataType> weatherDataTypeSet = getRequestWeatherDataTypeSet();
		final Set<WeatherProviderType> weatherProviderTypeSet = widgetDto.getWeatherProviderTypeSet();

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			if (!widgetDto.isMultipleWeatherDataSource()) {
				weatherProviderTypeSet.remove(WeatherProviderType.OWM_ONECALL);
			}
			weatherProviderTypeSet.add(WeatherProviderType.KMA_WEB);
		}

		if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
			weatherProviderTypeSet.add(WeatherProviderType.AQICN);
		}
		WeatherRequestUtil.loadWeatherData(context, executorService, widgetDto.getLatitude(),
				widgetDto.getLongitude(), weatherDataTypeSet, new MultipleRestApiDownloader() {
					@Override
					public void onResult() {
						setResultViews(context, appWidgetId, remoteViews, widgetDto, weatherProviderTypeSet, this,
								weatherDataTypeSet, jobId);
					}

					@Override
					public void onCanceled() {
						setRefreshPendingIntent(remoteViews, appWidgetId, jobId);
						RemoteViewsUtil.onErrorProcess(remoteViews, getApplicationContext(), RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
						appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

						backgroundCallbackMap.get(jobId).onResult();

					}
				}, weatherProviderTypeSet);
	}

	protected void onActionBootCompleted(JobParameters jobParameters) {
		//위젯 자동 업데이트 재 등록
		widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
			@Override
			public void onResultSuccessful(List<WidgetDto> list) {
				WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());

				long widgetRefreshInterval = widgetHelper.getRefreshInterval();

				if (widgetRefreshInterval > 0) {
					widgetHelper.onSelectedAutoRefreshInterval(widgetRefreshInterval);
				}
				jobFinished(jobParameters, false);
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	abstract Class<?> getWidgetProviderClass();

	abstract AbstractWidgetCreator createWidgetViewCreator(int appWidgetId, int jobId);

	abstract Set<WeatherDataType> getRequestWeatherDataTypeSet();

	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews,
	                              WidgetDto widgetDto, Set<WeatherProviderType> requestWeatherProviderTypeSet, @Nullable MultipleRestApiDownloader multipleRestApiDownloader,
	                              Set<WeatherDataType> weatherDataTypeSet, int jobId) {
		if (!widgetDto.isInitialized()) {
			widgetDto.setInitialized(true);
		}

		if (widgetDto.isLoadSuccessful()) {
			RemoteViewsUtil.onSuccessfulProcess(remoteViews);
		} else {
			RemoteViewsUtil.onErrorProcess(remoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
			setRefreshPendingIntent(remoteViews, appWidgetId, jobId);
		}

		widgetRepository.update(widgetDto, null);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		backgroundCallbackMap.get(jobId).onResult();
	}

	protected final Callback addBackgroundCallback(JobParameters jobParameters) {
		Callback callback = new Callback() {
			@Override
			public void onResult() {
				jobFinished(jobParameters, false);
			}
		};

		backgroundCallbackMap.put(jobParameters.getJobId(), callback);
		return callback;
	}
}
