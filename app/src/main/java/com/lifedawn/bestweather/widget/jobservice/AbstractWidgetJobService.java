package com.lifedawn.bestweather.widget.jobservice;

import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Configuration;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FirstWidgetCreator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public abstract class AbstractWidgetJobService extends JobService {
	protected WidgetRepository widgetRepository;
	protected AppWidgetManager appWidgetManager;
	protected AbstractWidgetCreator widgetViewCreator;
	protected WidgetHelper widgetHelper;
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

					final RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);

					RemoteViewProcessor.onBeginProcess(remoteViews);
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

					if (widgetDto.getLocationType() == LocationType.CurrentLocation) {
						loadCurrentLocation(getApplicationContext(), remoteViews, appWidgetId);
					} else {
						loadWeatherData(getApplicationContext(), remoteViews, appWidgetId, widgetDto);
					}

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
					RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);
					if (result.getLocationType() == LocationType.CurrentLocation) {
						loadCurrentLocation(getApplicationContext(), remoteViews, appWidgetId);
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
		} else if (action.equals(getString(R.string.com_lifedawn_bestweather_action_REDRAW))) {
			int[] appWidgetIds = bundle.getIntArray("appWidgetIds");
			for (int id : appWidgetIds) {
				onReDraw(id);
			}
			jobFinished(params, false);

		} else if (action.equals(getString(R.string.com_lifedawn_bestweather_action_ON_APP_WIDGET_OPTIONS_CHANGED))) {
			createWidgetViewCreator(appWidgetId);
			widgetViewCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto result) {
					if (result.getResponseText() != null) {
						widgetViewCreator.setDataViewsOfSavedData();
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

	public final void setRefreshPendingIntent(RemoteViews remoteViews, int appWidgetId, Context context) {
		Intent refreshIntent = new Intent(context, getWidgetProviderClass());
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		refreshIntent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, refreshIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, pendingIntent);
		remoteViews.setOnClickPendingIntent(R.id.refreshLayout, pendingIntent);
	}

	public void loadCurrentLocation(Context context, RemoteViews remoteViews, int appWidgetId) {
		FusedLocation.MyLocationCallback locationCallback = new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				final Location location = locationResult.getLocations().get(0);
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						WidgetRepository widgetRepository = new WidgetRepository(context);
						widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
							@Override
							public void onResultSuccessful(WidgetDto result) {
								Address address = addressList.get(0);

								result.setAddressName(address.getAddressLine(0));
								result.setCountryCode(address.getCountryCode());
								result.setLatitude(address.getLatitude());
								result.setLongitude(address.getLongitude());

								widgetRepository.update(result, null);
								loadWeatherData(context, remoteViews, appWidgetId, result);
							}

							@Override
							public void onResultNoData() {

							}
						});


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

				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT));
				RemoteViewProcessor.onErrorProcess(remoteViews, context, errorType);
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

				Message message = handler.obtainMessage();
				message.obj = "finished";
				handler.sendMessage(message);
			}
		};

		FusedLocation.getInstance(context).startLocationUpdates(locationCallback);
	}


	public void loadWeatherData(Context context, RemoteViews remoteViews, int appWidgetId, WidgetDto widgetDto) {
		RemoteViewProcessor.onBeginProcess(remoteViews);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();
		final Set<WeatherSourceType> weatherSourceTypeSet = widgetDto.getWeatherSourceTypeSet();

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			if (weatherSourceTypeSet.size() == 1) {
				weatherSourceTypeSet.clear();
				weatherSourceTypeSet.add(WeatherSourceType.KMA_WEB);
			}
		}

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.airQuality)) {
			weatherSourceTypeSet.add(WeatherSourceType.AQICN);
		}

		WeatherRequestUtil.loadWeatherData(context, Executors.newSingleThreadExecutor(), widgetDto.getCountryCode(), widgetDto.getLatitude(),
				widgetDto.getLongitude(), requestWeatherDataTypeSet, new MultipleRestApiDownloader() {
					@Override
					public void onResult() {
						setResultViews(context, appWidgetId, remoteViews, widgetDto, weatherSourceTypeSet, this, requestWeatherDataTypeSet);
					}

					@Override
					public void onCanceled() {

					}
				}, weatherSourceTypeSet);

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

	protected void onReDraw(int appWidgetId) {
		createWidgetViewCreator(appWidgetId);
		widgetViewCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto widgetDto) {
				if (widgetDto == null) {
					return;
				}

				RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);

				if (widgetDto.isLoadSuccessful()) {
					widgetViewCreator.setDataViewsOfSavedData();
				} else {
					remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetViewCreator.getOnClickedPendingIntent(remoteViews));
					RemoteViewProcessor.onErrorProcess(remoteViews, widgetViewCreator.getContext(), RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
					appWidgetManager.updateAppWidget(widgetViewCreator.getAppWidgetId(), remoteViews);
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	abstract Class<?> getWidgetProviderClass();

	abstract void createWidgetViewCreator(int appWidgetId);

	abstract Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();

	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews,
	                              WidgetDto widgetDto, Set<WeatherSourceType> requestWeatherSourceTypeSet, @Nullable MultipleRestApiDownloader multipleRestApiDownloader,
	                              Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		Message message = handler.obtainMessage();
		message.obj = "finished";
		handler.sendMessage(message);
	}
}
