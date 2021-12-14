package com.lifedawn.bestweather.widget.widgetprovider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractAppWidgetProvider extends AppWidgetProvider {
	protected static final String tag = "AppWidgetProvider";
	protected WidgetRepository widgetRepository;
	protected AppWidgetManager appWidgetManager;
	protected NetworkStatus networkStatus;


	abstract Class<?> getThis();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.e(tag, "onUpdate");
		if (widgetRepository == null) {
			widgetRepository = new WidgetRepository(context);
		}
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(context);
		}
		if (networkStatus == null) {
			networkStatus = NetworkStatus.getInstance(context);
		}
		ComponentName componentName = new ComponentName(context, getThis());

		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
		for (int widgetId : allWidgetIds) {
			reDrawWidget(context, widgetId);
		}
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.e(tag, "onEnabled");
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.e(tag, "onDisabled");
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		if (widgetRepository == null) {
			widgetRepository = new WidgetRepository(context);
		}
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(context);
		}
		for (int appWidgetId : appWidgetIds) {
			widgetRepository.delete(appWidgetId, null);
		}
		Log.e(tag, "onDeleted");
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (widgetRepository == null) {
			widgetRepository = new WidgetRepository(context);
		}
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(context);
		}
		if (networkStatus == null) {
			networkStatus = NetworkStatus.getInstance(context);
		}

		String action = intent.getAction();
		Log.e(tag, action);

		if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_INIT))) {
			init(context, intent.getExtras());
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			Bundle bundle = intent.getExtras();
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			RemoteViews remoteViews = bundle.getParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name());

			widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
				@Override
				public void onResultSuccessful(WidgetDto result) {
					loadWeatherData(context, remoteViews, appWidgetId, result);

				}

				@Override
				public void onResultNoData() {

				}
			});
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH_CURRENT_LOCATION))) {
			Bundle bundle = intent.getExtras();
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			RemoteViews remoteViews = bundle.getParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name());
			RemoteViewProcessor.onBeginProcess(remoteViews);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

			loadCurrentLocation(context, remoteViews, appWidgetId);
		}
	}

	public final void setRefreshPendingIntent(RemoteViews remoteViews, int appWidgetId, Context context) {
		Intent refreshIntent = new Intent(context, getThis());
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));

		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		bundle.putParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
		refreshIntent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, refreshIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, pendingIntent);
	}

	public void loadCurrentLocation(Context context, RemoteViews remoteViews, int appWidgetId) {
		Log.e(tag, "loadCurrentLocation");
		Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						WidgetRepository widgetRepository = new WidgetRepository(context);
						widgetRepository.get(appWidgetId, new DbQueryCallback<WidgetDto>() {
							@Override
							public void onResultSuccessful(WidgetDto result) {
								WidgetDto widgetDto = result;
								Address address = addressList.get(0);

								widgetDto.setAddressName(address.getAddressLine(0));
								widgetDto.setCountryCode(address.getCountryCode());
								widgetDto.setLatitude(address.getLatitude());
								widgetDto.setLongitude(address.getLongitude());

								widgetRepository.update(widgetDto, null);
								loadWeatherData(context, remoteViews, appWidgetId, widgetDto);
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
			}
		};

		Gps gps = new Gps(context, null, null, null);
		if (gps.checkPermissionAndGpsEnabled(context, locationCallback)) {
			gps.runGps(context, locationCallback);
		}
	}


	public void loadWeatherData(Context context, RemoteViews remoteViews, int appWidgetId, WidgetDto widgetDto) {
		if (networkStatus.networkAvailable()) {
			RemoteViewProcessor.onBeginProcess(remoteViews);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

			final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();
			WeatherSourceType weatherSourceType = WeatherSourceType.valueOf(widgetDto.getWeatherSourceType());

			if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
				weatherSourceType = WeatherSourceType.KMA;
			}

			ExecutorService executorService = Executors.newSingleThreadExecutor();
			WeatherSourceType finalWeatherSourceType = weatherSourceType;
			WeatherRequestUtil.loadWeatherData(context, executorService, widgetDto.getCountryCode(), widgetDto.getLatitude(),
					widgetDto.getLongitude(), requestWeatherDataTypeSet, new MultipleJsonDownloader() {
						@Override
						public void onResult() {
							setResultViews(context, appWidgetId, remoteViews, widgetDto, finalWeatherSourceType, this, requestWeatherDataTypeSet);
						}

						@Override
						public void onCanceled() {

						}
					}, weatherSourceType);

		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.UNAVAILABLE_NETWORK);
			setRefreshPendingIntent(remoteViews, appWidgetId, context);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}

	}

	abstract protected void reDrawWidget(Context context, int appWidgetId);

	abstract protected void init(Context context, Bundle bundle);

	abstract Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();

	abstract protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews,
	                                       WidgetDto widgetDto, WeatherSourceType requestWeatherSourceType, @Nullable MultipleJsonDownloader multipleJsonDownloader,
	                                       Set<RequestWeatherDataType> requestWeatherDataTypeSet);
}
