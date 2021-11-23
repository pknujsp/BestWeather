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
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.widget.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.widget.dto.DailyForecastObj;
import com.lifedawn.bestweather.widget.dto.HeaderObj;
import com.lifedawn.bestweather.widget.dto.HourlyForecastObj;
import com.lifedawn.bestweather.widget.dto.WeatherJsonObj;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public abstract class AbstractAppWidgetProvider extends AppWidgetProvider implements WidgetCreator.WidgetUpdateCallback {
	private static final String tag = "AppWidgetProvider";
	private static ExecutorService executorService = Executors.newFixedThreadPool(2);
	private MultipleJsonDownloader multipleJsonDownloader;

	abstract Class<?> getThis();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.e(tag, "onUpdate");
		ComponentName componentName = new ComponentName(context, getThis());

		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
		AqicnResponseProcessor.init(context);

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

		for (int appWidgetId : appWidgetIds) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				context.deleteSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId));
			} else {
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit().clear().apply();
			}
		}
	}

	@Override
	public void updateWidget() {

	}

	protected void reDrawWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.e(tag, "reDraw");
		if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null) {
			return;
		}

		SharedPreferences sharedPreferences = context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);

		if (sharedPreferences.getAll().isEmpty()) {
			return;
		}

		WidgetCreator widgetViewCreator = new WidgetCreator(context, this);
		widgetViewCreator.onSharedPreferenceChanged(sharedPreferences, null);

		WeatherJsonObj weatherJsonObj = WidgetCreator.getSavedWeatherData(appWidgetId, context);
		RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);

		if (weatherJsonObj.isSuccessful()) {
			widgetViewCreator.setHeaderViews(remoteViews, weatherJsonObj.getHeaderObj());
			widgetViewCreator.setCurrentConditionsViews(remoteViews, weatherJsonObj.getCurrentConditionsObj());
			widgetViewCreator.setHourlyForecastViews(remoteViews, weatherJsonObj.getHourlyForecasts());
			widgetViewCreator.setDailyForecastViews(remoteViews, weatherJsonObj.getDailyForecasts());
			widgetViewCreator.setClockTimeZone(remoteViews, ZoneId.of(weatherJsonObj.getCurrentConditionsObj().getZoneId()));
		} else {
			remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetViewCreator.getOnClickedPendingIntent(remoteViews, appWidgetId));
			onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
		}
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}


	protected final void onBeginProcess(RemoteViews remoteViews) {
		Log.e(tag, "onBeginProcess");
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
	}

	protected final void onSuccessfulProcess(RemoteViews remoteViews) {
		Log.e(tag, "onSuccessfulProcess");
		remoteViews.setViewVisibility(R.id.content_container, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
	}

	protected final void onErrorProcess(RemoteViews remoteViews, String errorMsg, String btnMsg) {
		Log.e(tag, "onErrorProcess");
		remoteViews.setViewVisibility(R.id.warning_layout, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
		remoteViews.setTextViewText(R.id.warning, errorMsg);
		remoteViews.setTextViewText(R.id.warning_process_btn, btnMsg);
	}

	public void init(Context context, Bundle bundle) {
		//초기화
		final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		final SharedPreferences sharedPreferences = context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId),
				Context.MODE_PRIVATE);
		WidgetCreator widgetCreator = new WidgetCreator(context, null);
		widgetCreator.onSharedPreferenceChanged(sharedPreferences, null);
		final RemoteViews remoteViews = widgetCreator.createRemoteViews(false);

		onBeginProcess(remoteViews);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		final LocationType locationType =
				LocationType.valueOf(sharedPreferences.getString(WidgetCreator.WidgetAttributes.LOCATION_TYPE.name(),
						LocationType.CurrentLocation.name()));

		if (locationType == LocationType.CurrentLocation) {
			loadCurrentLocation(context, remoteViews, appWidgetId);
		} else {
			if (sharedPreferences.getString(WidgetDataKeys.COUNTRY_CODE.name(), "").equals("KR")) {
				if (sharedPreferences.getBoolean(WidgetCreator.WidgetAttributes.TOP_PRIORITY_KMA.name(), false)) {
					sharedPreferences.edit().putString(WidgetCreator.WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
							WeatherSourceType.KMA.name()).apply();
				}
			}
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
			RemoteViews remoteViews = bundle.getParcelable(WidgetCreator.WidgetAttributes.REMOTE_VIEWS.name());
			loadWeatherData(context, AppWidgetManager.getInstance(context), remoteViews, appWidgetId);
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH_CURRENT_LOCATION))) {
			Bundle bundle = intent.getExtras();
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			RemoteViews remoteViews = bundle.getParcelable(WidgetCreator.WidgetAttributes.REMOTE_VIEWS.name());
			onBeginProcess(remoteViews);
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
						if (countryCode.equals("KR")) {
							if (widgetAttributes.getBoolean(WidgetCreator.WidgetAttributes.TOP_PRIORITY_KMA.name(), false)) {
								editor.putString(WidgetCreator.WidgetAttributes.WEATHER_SOURCE_TYPE.name(), WeatherSourceType.KMA.name());
							}
						}
						editor.putString(WidgetDataKeys.LATITUDE.name(), String.valueOf(addressList.get(0).getLatitude()))
								.putString(WidgetDataKeys.LONGITUDE.name(), String.valueOf(addressList.get(0).getLongitude()))
								.putString(WidgetDataKeys.COUNTRY_CODE.name(), countryCode)
								.putString(WidgetDataKeys.ADDRESS_NAME.name(), addressList.get(0).getAddressLine(0)).commit();

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
				onErrorProcess(remoteViews, errorMsg, btnMsg);
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}
		};

		Gps gps = new Gps(null, null, null);
		if (gps.checkPermissionAndGpsEnabled(context, locationCallback)) {
			gps.runGps(context, locationCallback);
		}
	}


	public void loadWeatherData(Context context, AppWidgetManager appWidgetManager, RemoteViews remoteViews, int appWidgetId) {
		onBeginProcess(remoteViews);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		SharedPreferences widgetAttributes =
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId),
						Context.MODE_PRIVATE);
		final WeatherSourceType weatherSourceType =
				WeatherSourceType.valueOf(widgetAttributes.getString(WidgetCreator.WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources =
				makeRequestWeatherSources(weatherSourceType);
		Set<RequestWeatherDataType> requestWeatherDataTypeSet = getRequestWeatherDataTypeSet();

		setRequestWeatherSources(weatherSourceType, requestWeatherSources, requestWeatherDataTypeSet);

		Double latitude = Double.parseDouble(widgetAttributes.getString(WidgetDataKeys.LATITUDE.name(), "0.0"));
		Double longitude = Double.parseDouble(widgetAttributes.getString(WidgetDataKeys.LONGITUDE.name(), "0.0"));

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				multipleJsonDownloader = MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources,
						new MultipleJsonDownloader() {
							@Override
							public void onResult() {
								Log.e(tag, "onResult");
								if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null) {
									return;
								}
								initWeatherSourceUniqueValues(context, weatherSourceType);
								setResultViews(context, appWidgetId, remoteViews, appWidgetManager, this, requestWeatherDataTypeSet);
							}

							@Override
							public void onCanceled() {
								Log.e(tag, "canceled");
							}
						});
			}
		});

	}

	public static void initWeatherSourceUniqueValues(Context context, WeatherSourceType weatherSourceType) {
		switch (weatherSourceType) {
			case KMA:
				KmaResponseProcessor.init(context);
				break;
			case ACCU_WEATHER:
				AccuWeatherResponseProcessor.init(context);
				break;
			case OPEN_WEATHER_MAP:
				OpenWeatherMapResponseProcessor.init(context);
				break;
		}
		AqicnResponseProcessor.init(context);
	}

	abstract Set<RequestWeatherDataType> getRequestWeatherDataTypeSet();

	public final ArrayMap<WeatherSourceType, RequestWeatherSource> makeRequestWeatherSources(WeatherSourceType weatherSourceType) {
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		RequestWeatherSource requestWeatherSource = null;

		if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			requestWeatherSource = new RequestAccu();
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			requestWeatherSource = new RequestOwm();
		} else if (weatherSourceType == WeatherSourceType.KMA) {
			requestWeatherSource = new RequestKma();
		}
		requestWeatherSources.put(weatherSourceType, requestWeatherSource);
		return requestWeatherSources;
	}

	protected final void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, AppWidgetManager appWidgetManager,
	                                    @Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		SharedPreferences widgetAttributes =
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);

		WidgetCreator widgetCreator = new WidgetCreator(context, this);
		widgetCreator.onSharedPreferenceChanged(widgetAttributes, null);

		final WeatherSourceType requestWeatherSourceType =
				WeatherSourceType.valueOf(widgetAttributes.getString(WidgetCreator.WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));

		CurrentConditionsObj currentConditionsObj = null;
		WeatherJsonObj.HourlyForecasts hourlyForecastObjs = null;
		WeatherJsonObj.DailyForecasts dailyForecasts = null;

		HeaderObj headerObj = getHeader(context, multipleJsonDownloader, appWidgetId);
		widgetCreator.setHeaderViews(remoteViews, headerObj);

		boolean successful = true;
		ZoneId zoneId = null;

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
			currentConditionsObj = getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
					appWidgetId);
			if (!currentConditionsObj.isSuccessful()) {
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetCreator.getOnClickedPendingIntent(remoteViews, appWidgetId));
				onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else {
				widgetCreator.setCurrentConditionsViews(remoteViews, currentConditionsObj);
				zoneId = ZoneId.of(currentConditionsObj.getZoneId());
			}
		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
			hourlyForecastObjs = getHourlyForecasts(context, requestWeatherSourceType, multipleJsonDownloader,
					appWidgetId);
			if (hourlyForecastObjs.getHourlyForecastObjs().isEmpty() && successful) {
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetCreator.getOnClickedPendingIntent(remoteViews, appWidgetId));
				onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else if (!hourlyForecastObjs.getHourlyForecastObjs().isEmpty()) {
				zoneId = ZoneId.of(hourlyForecastObjs.getZoneId());
				widgetCreator.setHourlyForecastViews(remoteViews, hourlyForecastObjs);
			}

		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
			dailyForecasts = getDailyForecasts(context, requestWeatherSourceType, multipleJsonDownloader,
					appWidgetId);
			if (dailyForecasts.getDailyForecastObjs().isEmpty() && successful) {
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetCreator.getOnClickedPendingIntent(remoteViews, appWidgetId));
				onErrorProcess(remoteViews, context.getString(R.string.update_failed), context.getString(R.string.again));
				successful = false;
			} else if (!dailyForecasts.getDailyForecastObjs().isEmpty()) {
				zoneId = ZoneId.of(dailyForecasts.getZoneId());
				widgetCreator.setDailyForecastViews(remoteViews, dailyForecasts);
			}
		}

		if (successful) {
			boolean displayLocalClock = widgetAttributes.getBoolean(WidgetCreator.WidgetAttributes.DISPLAY_LOCAL_CLOCK.name(), false);
			widgetCreator.setClockTimeZone(remoteViews, displayLocalClock ? zoneId : ZoneId.systemDefault());
			onSuccessfulProcess(remoteViews);
		}
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		WidgetCreator.saveWeatherData(appWidgetId, context, headerObj, currentConditionsObj, hourlyForecastObjs, dailyForecasts);
	}

	public final HeaderObj getHeader(Context context, MultipleJsonDownloader multipleJsonDownloader, int appWidgetId) {
		HeaderObj headerObj = new HeaderObj();
		headerObj.setAddress(context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE)
				.getString(WidgetDataKeys.ADDRESS_NAME.name(), ""));
		headerObj.setRefreshDateTime(ZonedDateTime.now().toString());

		return headerObj;
	}

	public final CurrentConditionsObj getCurrentConditions(Context context, WeatherSourceType weatherSourceType,
	                                                       MultipleJsonDownloader multipleJsonDownloader, int appWidgetId) {
		final ZonedDateTime updatedTime = ZonedDateTime.of(multipleJsonDownloader.getLocalDateTime().toLocalDate(),
				multipleJsonDownloader.getLocalDateTime().toLocalTime(), ZoneId.systemDefault());

		ZoneId zoneId = null;
		CurrentConditionsObj currentConditionsObj = new CurrentConditionsObj();
		boolean successfulResponse = true;

		switch (weatherSourceType) {
			case KMA:
				if (KmaResponseProcessor.successfulVilageResponse((Response<VilageFcstResponse>) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST).getResponse())) {
					FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditions(
							(VilageFcstResponse) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST).getResponse().body());
					zoneId = KmaResponseProcessor.getZoneId();

					currentConditionsObj.setTemp(finalCurrentConditions.getTemperature());
					currentConditionsObj.setPrecipitation(finalCurrentConditions.getPrecipitation1Hour().equals("0") ? null : finalCurrentConditions.getPrecipitation1Hour());
					currentConditionsObj.setRealFeelTemp(null);

					SharedPreferences sharedPreferences =
							context.getSharedPreferences(WidgetCreator.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId,
									Context.MODE_PRIVATE);

					final double latitude = Double.parseDouble(sharedPreferences.getString(WidgetDataKeys.LATITUDE.name(), "0.0"));
					final double longitude = Double.parseDouble(sharedPreferences.getString(WidgetDataKeys.LONGITUDE.name(), "0.0"));

					SunriseSunsetCalculator sunriseSunsetCalculator =
							new SunriseSunsetCalculator(new com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude),
									TimeZone.getTimeZone(zoneId.getId()));

					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
					calendar.setTimeInMillis(updatedTime.toInstant().toEpochMilli());

					Calendar sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar);
					Calendar sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar);
					boolean isNight = SunRiseSetUtil.isNight(calendar, sunRise, sunSet);

					currentConditionsObj.setWeatherIcon(KmaResponseProcessor.getWeatherPtyIconImg(finalCurrentConditions.getPrecipitationType(), isNight));
				} else {
					successfulResponse = false;
				}
				break;
			case ACCU_WEATHER:
				if (AccuWeatherResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS))) {
					CurrentConditionsResponse currentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
							(JsonElement) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponse().body());
					CurrentConditionsResponse.Item item = currentConditionsResponse.getItems().get(0);
					zoneId = ZonedDateTime.parse(item.getLocalObservationDateTime()).getZone();

					currentConditionsObj.setTemp(item.getTemperature().getMetric().getValue());
					currentConditionsObj.setRealFeelTemp(item.getRealFeelTemperature().getMetric().getValue());
					currentConditionsObj.setPrecipitation(item.getPrecip1hr() == null ? null : item.getPrecip1hr().getMetric().getValue());
					currentConditionsObj.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon()));

				} else {
					successfulResponse = false;
				}
				break;
			case OPEN_WEATHER_MAP:
				if (OpenWeatherMapResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL))) {
					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP)
									.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().body().toString());
					OneCallResponse.Current current = oneCallResponse.getCurrent();
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

					currentConditionsObj.setTemp(current.getTemp());
					currentConditionsObj.setRealFeelTemp(current.getFeelsLike());

					if (current.getRain() != null) {
						currentConditionsObj.setPrecipitation(current.getRain().getPrecipitation1Hour());
					} else if (current.getSnow() != null) {
						currentConditionsObj.setPrecipitation(current.getSnow().getPrecipitation1Hour());
					}
					currentConditionsObj.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(current.getWeather().get(0).getId()
							, current.getWeather().get(0).getIcon().contains("n")));
				} else {
					successfulResponse = false;
				}
				break;
		}
		String airQuality = null;
		if (AqicnResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN).get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED))) {
			GeolocalizedFeedResponse geolocalizedFeedResponse =
					AqicnResponseProcessor.getAirQualityObjFromJson((Response<JsonElement>) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN).get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED).getResponse());
			airQuality = geolocalizedFeedResponse.getData().getIaqi().getPm10() == null ? null :
					geolocalizedFeedResponse.getData().getIaqi().getPm10().getValue();
		} else {
			airQuality = null;
		}
		currentConditionsObj.setAirQuality(airQuality);
		currentConditionsObj.setSuccessful(successfulResponse);
		currentConditionsObj.setZoneId(successfulResponse ? zoneId.getId() : null);
		return currentConditionsObj;
	}

	public final WeatherJsonObj.HourlyForecasts getHourlyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                               MultipleJsonDownloader multipleJsonDownloader, int appWidgetId) {
		boolean successfulResponse = true;
		List<HourlyForecastObj> hourlyForecastObjList = new ArrayList<>();
		ZoneId zoneId = null;

		switch (weatherSourceType) {
			case KMA:
				if (KmaResponseProcessor.successfulVilageResponse((Response<VilageFcstResponse>) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST).getResponse()) &&
						KmaResponseProcessor.successfulVilageResponse((Response<VilageFcstResponse>) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST).getResponse())) {
					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
							(VilageFcstResponse) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST).getResponse().body(),
							(VilageFcstResponse) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST).getResponse().body());
					zoneId = KmaResponseProcessor.getZoneId();

					ZonedDateTime begin = ZonedDateTime.of(finalHourlyForecastList.get(0).getFcstDateTime().toLocalDateTime(), zoneId);
					ZonedDateTime end =
							ZonedDateTime.of(finalHourlyForecastList.get(finalHourlyForecastList.size() - 1).getFcstDateTime().toLocalDateTime(),
									zoneId);
					SharedPreferences sharedPreferences =
							context.getSharedPreferences(WidgetCreator.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId,
									Context.MODE_PRIVATE);

					final double latitude = Double.parseDouble(sharedPreferences.getString(WidgetDataKeys.LATITUDE.name(), "0.0"));
					final double longitude = Double.parseDouble(sharedPreferences.getString(WidgetDataKeys.LONGITUDE.name(), "0.0"));

					Map<Integer, SunRiseSetUtil.SunRiseSetObj> sunRiseSetObjMap = SunRiseSetUtil.getDailySunRiseSetMap(begin, end, latitude
							, longitude);
					ZonedDateTime fcstDateTime = null;
					int index = 0;
					Calendar itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));

					for (FinalHourlyForecast hourlyForecast : finalHourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);

						fcstDateTime = ZonedDateTime.of(hourlyForecast.getFcstDateTime().toLocalDateTime(), zoneId);
						hourlyForecastObj.setClock(fcstDateTime.toString());

						itemCalendar.setTimeInMillis(fcstDateTime.toInstant().toEpochMilli());
						Calendar sunRise = sunRiseSetObjMap.get(fcstDateTime.getDayOfMonth()).getSunrise();
						Calendar sunSet = sunRiseSetObjMap.get(fcstDateTime.getDayOfMonth()).getSunset();

						hourlyForecastObj.setWeatherIcon(KmaResponseProcessor.getWeatherSkyIconImg(hourlyForecast.getSky(),
								SunRiseSetUtil.isNight(itemCalendar, sunRise, sunSet)));
						hourlyForecastObj.setTemp(hourlyForecast.getTemp1Hour());

						hourlyForecastObjList.add(hourlyForecastObj);

						if (++index == 10) {
							break;
						}
					}
				} else {
					successfulResponse = false;
				}
				break;
			case ACCU_WEATHER:
				if (AccuWeatherResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_12_HOURLY))) {
					TwelveHoursOfHourlyForecastsResponse hourlyForecastResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
							(JsonElement) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_12_HOURLY).getResponse().body());
					List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyForecastList = hourlyForecastResponse.getItems();
					zoneId = ZonedDateTime.parse(hourlyForecastList.get(0).getDateTime()).getZone();

					int index = 0;

					for (TwelveHoursOfHourlyForecastsResponse.Item hourlyForecast : hourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);

						hourlyForecastObj.setClock(WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourlyForecast.getEpochDateTime()) * 1000L, zoneId).toString());
						hourlyForecastObj.setTemp(hourlyForecast.getTemperature().getValue());
						hourlyForecastObj.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(hourlyForecast.getWeatherIcon()));

						hourlyForecastObjList.add(hourlyForecastObj);
						if (++index == 10) {
							break;
						}
					}
				} else {
					successfulResponse = false;
				}
				break;

			case OPEN_WEATHER_MAP:
				if (OpenWeatherMapResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL))) {
					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().body().toString());
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

					List<OneCallResponse.Hourly> hourly = oneCallResponse.getHourly();
					int index = 0;

					for (OneCallResponse.Hourly item : hourly) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);

						hourlyForecastObj.setClock(WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(item.getDt()) * 1000L,
								zoneId).toString());
						hourlyForecastObj.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(),
								item.getWeather().get(0).getIcon().contains("n")));
						hourlyForecastObj.setTemp(item.getTemp());

						hourlyForecastObjList.add(hourlyForecastObj);
						if (++index == 10) {
							break;
						}
					}
				} else {
					successfulResponse = false;
				}
				break;

		}
		WeatherJsonObj.HourlyForecasts hourlyForecasts = new WeatherJsonObj.HourlyForecasts();
		hourlyForecasts.setHourlyForecastObjs(hourlyForecastObjList);
		hourlyForecasts.setZoneId(successfulResponse ? zoneId.getId() : null);

		return hourlyForecasts;
	}

	public final WeatherJsonObj.DailyForecasts getDailyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                             MultipleJsonDownloader multipleJsonDownloader, int appWidgetId) {
		boolean successfulResponse = true;
		List<DailyForecastObj> dailyForecastObjList = new ArrayList<>();
		ZoneId zoneId = null;

		switch (weatherSourceType) {
			case KMA:
				if (KmaResponseProcessor.successfulMidLandFcstResponse((Response<MidLandFcstResponse>) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_LAND_FCST).getResponse())
						&& KmaResponseProcessor.successfulMidTaFcstResponse((Response<MidTaResponse>) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_TA_FCST).getResponse())) {
					List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(
							(MidLandFcstResponse)
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_LAND_FCST).getResponse().body(),
							(MidTaResponse)
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_TA_FCST).getResponse().body(),
							Long.parseLong(multipleJsonDownloader.get("tmFc")));
					zoneId = KmaResponseProcessor.getZoneId();

					int index = 0;

					for (FinalDailyForecast finalDailyForecast : finalDailyForecastList) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(true, false);
						dailyForecastObj.setDate(finalDailyForecast.getDate().toString());
						dailyForecastObj.setMinTemp(finalDailyForecastList.get(index).getMinTemp());
						dailyForecastObj.setMaxTemp(finalDailyForecastList.get(index).getMaxTemp());
						dailyForecastObj.setLeftWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getAmSky(), false));
						dailyForecastObj.setRightWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getPmSky(), true));

						dailyForecastObjList.add(dailyForecastObj);
						if (++index == 4) {
							break;
						}
					}
				} else {
					successfulResponse = false;
				}
				break;
			case ACCU_WEATHER:
				if (AccuWeatherResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY))) {
					FiveDaysOfDailyForecastsResponse dailyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
							multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponse().body().toString());

					List<FiveDaysOfDailyForecastsResponse.DailyForecasts> dailyForecasts = dailyForecastsResponse.getDailyForecasts();
					zoneId = ZonedDateTime.parse(dailyForecasts.get(0).getDateTime()).getZone();
					int index = 0;

					for (FiveDaysOfDailyForecastsResponse.DailyForecasts item : dailyForecasts) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(true, false);

						dailyForecastObj.setDate(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getEpochDate()) * 1000L, zoneId).toString());

						dailyForecastObj.setMinTemp(item.getTemperature().getMinimum().getValue());
						dailyForecastObj.setMaxTemp(item.getTemperature().getMaximum().getValue());
						dailyForecastObj.setLeftWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getDay().getIcon()));
						dailyForecastObj.setRightWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getNight().getIcon()));

						dailyForecastObjList.add(dailyForecastObj);
						if (++index == 4) {
							break;
						}
					}
				} else {
					successfulResponse = false;
				}
				break;
			case OPEN_WEATHER_MAP:
				if (OpenWeatherMapResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL))) {
					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().body().toString());
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

					List<OneCallResponse.Daily> daily = oneCallResponse.getDaily();
					int index = 0;

					for (OneCallResponse.Daily item : daily) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(true, true);
						dailyForecastObj.setDate(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getDt()) * 1000L,
								zoneId).toString());
						dailyForecastObj.setMinTemp(item.getTemp().getMin());
						dailyForecastObj.setMaxTemp(item.getTemp().getMax());
						dailyForecastObj.setLeftWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(), false));

						dailyForecastObjList.add(dailyForecastObj);
						if (++index == 4) {
							break;
						}
					}
				} else {
					successfulResponse = false;
				}
				break;
		}

		WeatherJsonObj.DailyForecasts dailyForecasts = new WeatherJsonObj.DailyForecasts();
		dailyForecasts.setDailyForecastObjs(dailyForecastObjList);
		dailyForecasts.setZoneId(successfulResponse ? zoneId.getId() : null);

		return dailyForecasts;
	}


	protected final void setRequestWeatherSources(WeatherSourceType weatherSourceType,
	                                              ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                              Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		if (weatherSourceType == WeatherSourceType.KMA) {
			RequestKma requestKma = (RequestKma) requestWeatherSources.get(weatherSourceType);
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_FCST).addRequestServiceType(RetrofitClient.ServiceType.VILAGE_FCST);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.MID_LAND_FCST).addRequestServiceType(RetrofitClient.ServiceType.MID_TA_FCST);
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			RequestAccu requestAccu = (RequestAccu) requestWeatherSources.get(weatherSourceType);
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_12_HOURLY);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			RequestOwm requestOwm = (RequestOwm) requestWeatherSources.get(weatherSourceType);
			Set<OneCallParameter.OneCallApis> excludeSet = new HashSet<>();
			excludeSet.add(OneCallParameter.OneCallApis.daily);
			excludeSet.add(OneCallParameter.OneCallApis.hourly);
			excludeSet.add(OneCallParameter.OneCallApis.minutely);
			excludeSet.add(OneCallParameter.OneCallApis.alerts);
			excludeSet.add(OneCallParameter.OneCallApis.current);
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				excludeSet.remove(OneCallParameter.OneCallApis.current);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				excludeSet.remove(OneCallParameter.OneCallApis.hourly);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				excludeSet.remove(OneCallParameter.OneCallApis.daily);
			}
			requestOwm.setExcludeApis(excludeSet);
			requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		}

		RequestAqicn requestAqicn = new RequestAqicn();
		requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		requestWeatherSources.put(WeatherSourceType.AQICN, requestAqicn);
	}

	public enum WidgetDataKeys {
		ADDRESS_NAME, LATITUDE, LONGITUDE, COUNTRY_CODE, TIMEZONE_ID
	}
}
