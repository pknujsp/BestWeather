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
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.JsonDataSaver;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.forremoteviews.WeatherDataRequest;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.forremoteviews.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.forremoteviews.dto.HeaderObj;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

public abstract class AbstractAppWidgetProvider extends AppWidgetProvider implements WidgetCreator.WidgetUpdateCallback {
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			for (int appWidgetId : appWidgetIds) {
				context.deleteSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId));
			}
		} else {
			for (int appWidgetId : appWidgetIds) {
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit().clear().apply();
			}
		}
	}

	@Override
	public void updateWidget() {

	}

	protected void reDrawWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.e(tag, "reDraw appWidgetId : " + appWidgetId);
		if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null) {
			return;
		}
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);
		if (sharedPreferences.getAll().isEmpty()) {
			return;
		}

		WidgetCreator widgetViewCreator = new WidgetCreator(context, this);
		widgetViewCreator.onSharedPreferenceChanged(sharedPreferences, null);

		WeatherJsonObj weatherJsonObj = JsonDataSaver.getSavedWeatherData(context, WidgetCreator.getSharedPreferenceName(appWidgetId));
		RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);

		if (weatherJsonObj != null) {
			if (weatherJsonObj.isSuccessful()) {
				widgetViewCreator.setHeaderViews(remoteViews, weatherJsonObj.getHeaderObj());
				widgetViewCreator.setCurrentConditionsViews(remoteViews, weatherJsonObj.getCurrentConditionsObj());
				widgetViewCreator.setHourlyForecastViews(remoteViews, weatherJsonObj.getHourlyForecasts());
				widgetViewCreator.setDailyForecastViews(remoteViews, weatherJsonObj.getDailyForecasts());
				widgetViewCreator.setClockTimeZone(remoteViews, ZoneId.of(weatherJsonObj.getCurrentConditionsObj().getZoneId()));
			}
		} else {
			remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetViewCreator.getOnClickedPendingIntent(remoteViews, appWidgetId));
			RemoteViewProcessor.onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
		}

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}


	public void init(Context context, Bundle bundle) {
		//초기화
		final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		final SharedPreferences sharedPreferences = context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId),
				Context.MODE_PRIVATE);
		WidgetCreator widgetCreator = new WidgetCreator(context, null);
		widgetCreator.onSharedPreferenceChanged(sharedPreferences, null);
		final RemoteViews remoteViews = widgetCreator.createRemoteViews(false);

		RemoteViewProcessor.onBeginProcess(remoteViews);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		final LocationType locationType =
				LocationType.valueOf(sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(),
						LocationType.CurrentLocation.name()));

		if (locationType == LocationType.CurrentLocation) {
			loadCurrentLocation(context, remoteViews, appWidgetId);
		} else {
			loadWeatherData(context, AppWidgetManager.getInstance(context), remoteViews, appWidgetId);
		}
	}

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

	public void loadCurrentLocation(Context context, RemoteViews remoteViews, int appWidgetId) {
		Log.e(tag, "loadCurrentLocation");
		Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						final SharedPreferences widgetAttributes =
								context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = widgetAttributes.edit();

						String countryCode = addressList.get(0).getCountryCode();
						editor.putString(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), String.valueOf(addressList.get(0).getLatitude()))
								.putString(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), String.valueOf(addressList.get(0).getLongitude()))
								.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), countryCode)
								.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), addressList.get(0).getAddressLine(0)).commit();

						loadWeatherData(context, AppWidgetManager.getInstance(context), remoteViews, appWidgetId);
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				Intent intent = null;
				String errorMsg = null;
				String btnMsg = null;

				if (fail == Fail.REJECT_PERMISSION) {
					errorMsg = context.getString(R.string.message_needs_location_permission);
					btnMsg = context.getString(R.string.check_permission);
					intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					intent.setData(Uri.fromParts("package", context.getPackageName(), null));
				} else {
					errorMsg = context.getString(R.string.request_to_make_gps_on);
					btnMsg = context.getString(R.string.enable_gps);
					intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				}

				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT));
				RemoteViewProcessor.onErrorProcess(remoteViews, errorMsg, btnMsg);
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
		RemoteViewProcessor.onBeginProcess(remoteViews);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		WeatherDataRequest weatherDataRequest = new WeatherDataRequest(context);
		final Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();

		multipleJsonDownloader = new MultipleJsonDownloader() {
			@Override
			public void onResult() {
				Log.e(tag, "onResult");
				if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null) {
					return;
				}
				setResultViews(context, appWidgetId, remoteViews, appWidgetManager, this, requestWeatherDataTypeSet);
			}

			@Override
			public void onCanceled() {
				Log.e(tag, "canceled");
			}
		};
		weatherDataRequest.loadWeatherData(context, WidgetCreator.getSharedPreferenceName(appWidgetId),
				requestWeatherDataTypeSet, multipleJsonDownloader);

	}


	abstract Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();


	protected final void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, AppWidgetManager appWidgetManager,
	                                    @Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		SharedPreferences widgetAttributes =
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);

		WidgetCreator widgetCreator = new WidgetCreator(context, this);
		widgetCreator.onSharedPreferenceChanged(widgetAttributes, null);

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

		WeatherDataRequest weatherDataRequest = new WeatherDataRequest(context);

		HeaderObj headerObj = weatherDataRequest.getHeader(context, WidgetCreator.getSharedPreferenceName(appWidgetId));
		widgetCreator.setHeaderViews(remoteViews, headerObj);

		boolean successful = true;
		ZoneId zoneId = null;

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
			currentConditionsObj = weatherDataRequest.getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
					WidgetCreator.getSharedPreferenceName(appWidgetId));
			if (!currentConditionsObj.isSuccessful()) {
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetCreator.getOnClickedPendingIntent(remoteViews, appWidgetId));
				RemoteViewProcessor.onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else {
				widgetCreator.setCurrentConditionsViews(remoteViews, currentConditionsObj);
				zoneId = ZoneId.of(currentConditionsObj.getZoneId());
			}
		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
			hourlyForecastObjs = weatherDataRequest.getHourlyForecasts(context, requestWeatherSourceType, multipleJsonDownloader,
					WidgetCreator.getSharedPreferenceName(appWidgetId));
			if (hourlyForecastObjs.getHourlyForecastObjs().isEmpty() && successful) {
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetCreator.getOnClickedPendingIntent(remoteViews, appWidgetId));
				RemoteViewProcessor.onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else if (!hourlyForecastObjs.getHourlyForecastObjs().isEmpty()) {
				zoneId = ZoneId.of(hourlyForecastObjs.getZoneId());
				widgetCreator.setHourlyForecastViews(remoteViews, hourlyForecastObjs);
			}

		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
			dailyForecasts = weatherDataRequest.getDailyForecasts(requestWeatherSourceType, multipleJsonDownloader);
			if (dailyForecasts.getDailyForecastObjs().isEmpty() && successful) {
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetCreator.getOnClickedPendingIntent(remoteViews, appWidgetId));
				RemoteViewProcessor.onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else if (!dailyForecasts.getDailyForecastObjs().isEmpty()) {
				zoneId = ZoneId.of(dailyForecasts.getZoneId());
				widgetCreator.setDailyForecastViews(remoteViews, dailyForecasts);
			}
		}

		if (successful) {
			boolean displayLocalClock = widgetAttributes.getBoolean(WidgetNotiConstants.WidgetAttributes.DISPLAY_LOCAL_CLOCK.name(), false);
			widgetCreator.setClockTimeZone(remoteViews, displayLocalClock ? zoneId : ZoneId.systemDefault());
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		}
		JsonDataSaver.saveWeatherData(WidgetCreator.getSharedPreferenceName(appWidgetId), context, headerObj, currentConditionsObj, hourlyForecastObjs,
				dailyForecasts);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}


}
