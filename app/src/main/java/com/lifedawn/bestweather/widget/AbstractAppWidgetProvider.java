package com.lifedawn.bestweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.forremoteviews.WeatherDataRequestForRemote;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

public abstract class AbstractAppWidgetProvider extends AppWidgetProvider {
	private static final String tag = "AppWidgetProvider";
	private MultipleJsonDownloader multipleJsonDownloader;

	abstract Class<?> getThis();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.e(tag, "onUpdate");
		ComponentName componentName = new ComponentName(context, getThis());

		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
		for (int widgetId : allWidgetIds) {
			reDrawWidget(context, appWidgetManager, widgetId);
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
		Log.e(tag, "onDeleted");

		if (multipleJsonDownloader != null) {
			multipleJsonDownloader.cancel();
		}

	}

	abstract protected void reDrawWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId);

	abstract protected void init(Context context, Bundle bundle);


	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		Log.e(tag, action);

		if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_INIT))) {
			init(context, intent.getExtras());
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			Bundle bundle = intent.getExtras();
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			RemoteViews remoteViews = bundle.getParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name());
			loadWeatherData(context, AppWidgetManager.getInstance(context), remoteViews, appWidgetId);
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH_CURRENT_LOCATION))) {
			Bundle bundle = intent.getExtras();
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			RemoteViews remoteViews = bundle.getParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name());
			RemoteViewProcessor.onBeginProcess(remoteViews);

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
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

								loadWeatherData(context, AppWidgetManager.getInstance(context), remoteViews, appWidgetId);
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
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}
		};

		Gps gps = new Gps(context, null, null, null);
		if (gps.checkPermissionAndGpsEnabled(context, locationCallback)) {
			gps.runGps(context, locationCallback);
		}
	}


	public void loadWeatherData(Context context, AppWidgetManager appWidgetManager, RemoteViews remoteViews, int appWidgetId) {
		NetworkStatus networkStatus = NetworkStatus.getInstance(context);
		if (networkStatus.networkAvailable()) {

			RemoteViewProcessor.onBeginProcess(remoteViews);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

			WeatherDataRequestForRemote weatherDataRequestForRemote = new WeatherDataRequestForRemote(context);
			final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();

			multipleJsonDownloader = new MultipleJsonDownloader() {
				@Override
				public void onResult() {
					setResultViews(context, appWidgetId, remoteViews, appWidgetManager, this, requestWeatherDataTypeSet);
				}

				@Override
				public void onCanceled() {
					Log.e(tag, "canceled");
				}
			};
			weatherDataRequestForRemote.loadWeatherData(context, AbstractWidgetCreator.getSharedPreferenceName(appWidgetId),
					requestWeatherDataTypeSet, multipleJsonDownloader);
		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.UNAVAILABLE_NETWORK);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
	}


	abstract Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();


	protected final void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, AppWidgetManager appWidgetManager,
	                                    @Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		SharedPreferences widgetAttributes =
				context.getSharedPreferences(AbstractWidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);

		AbstractWidgetCreator abstractWidgetCreator = new AbstractWidgetCreator(context, this, appWidgetId);
		abstractWidgetCreator.loadSavedPreferences();

		WeatherSourceType requestWeatherSourceType =
				WeatherSourceType.valueOf(widgetAttributes.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));
		String countryCode = widgetAttributes.getString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), "");
		if (widgetAttributes.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), true) &&
				countryCode.equals("KR")) {
			requestWeatherSourceType = WeatherSourceType.KMA;
		}

		CurrentConditionsObj currentConditionsObj = null;
		WeatherJsonObj.HourlyForecasts hourlyForecastObjs = null;
		WeatherJsonObj.DailyForecasts dailyForecasts = null;

		WeatherDataRequestForRemote weatherDataRequestForRemote = new WeatherDataRequestForRemote(context);

		HeaderObj headerObj = weatherDataRequestForRemote.getHeader(context, AbstractWidgetCreator.getSharedPreferenceName(appWidgetId));
		abstractWidgetCreator.setHeaderViews(remoteViews, headerObj);

		boolean successful = true;
		ZoneId zoneId = null;

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
			currentConditionsObj = weatherDataRequestForRemote.getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
					AbstractWidgetCreator.getSharedPreferenceName(appWidgetId));
			if (currentConditionsObj.isSuccessful()) {
				abstractWidgetCreator.setCurrentConditionsViews(remoteViews, currentConditionsObj);
				zoneId = ZoneId.of(currentConditionsObj.getZoneId());
			} else {
				successful = false;
			}
		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
			hourlyForecastObjs = weatherDataRequestForRemote.getHourlyForecasts(context, requestWeatherSourceType, multipleJsonDownloader,
					AbstractWidgetCreator.getSharedPreferenceName(appWidgetId));
			if (hourlyForecastObjs.isSuccessful()) {
				zoneId = ZoneId.of(hourlyForecastObjs.getZoneId());
				abstractWidgetCreator.setHourlyForecastViews(remoteViews, hourlyForecastObjs);
			} else {
				successful = false;
			}

		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
			dailyForecasts = weatherDataRequestForRemote.getDailyForecasts(requestWeatherSourceType, multipleJsonDownloader);
			if (dailyForecasts.isSuccessful()) {
				zoneId = ZoneId.of(dailyForecasts.getZoneId());
				abstractWidgetCreator.setDailyForecastViews(remoteViews, dailyForecasts);
			} else {
				successful = false;
			}
		}

		if (successful) {
			boolean displayLocalClock = widgetAttributes.getBoolean(WidgetNotiConstants.WidgetAttributes.DISPLAY_LOCAL_CLOCK.name(), false);
			abstractWidgetCreator.setClockTimeZone(remoteViews, displayLocalClock ? zoneId : ZoneId.systemDefault());
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
			setRefreshPendingIntent(remoteViews, appWidgetId, context);
		}
		JsonDataSaver.saveWeatherData(AbstractWidgetCreator.getSharedPreferenceName(appWidgetId), context, headerObj, currentConditionsObj, hourlyForecastObjs,
				dailyForecasts);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}


}
