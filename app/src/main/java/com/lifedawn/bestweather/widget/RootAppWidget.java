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
import android.os.Bundle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

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
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
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
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunsetriseUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Response;

public abstract class RootAppWidget extends AppWidgetProvider implements WidgetCreator.WidgetUpdateCallback {
	private static final String tag = "RootAppWidget";

	abstract Class<?> getThis();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.e("APP WIDGET TAG : ", "onUpdate");

		/*
		ComponentName thisWidget = new ComponentName(context,
				getThis());

		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			reDrawWidget(context, appWidgetManager, widgetId);
		}
		 */
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.e("APP WIDGET TAG : ", "onEnabled");
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);

	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		for (int appWidgetId : appWidgetIds) {
			context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit().clear().apply();
		}
	}

	@Override
	public void updateWidget() {

	}

	protected void reDrawWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.e("APP WIDGET TAG : ", "reDraw");

		WidgetCreator widgetViewCreator = new WidgetCreator(context, this);
		SharedPreferences sharedPreferences = context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);
		widgetViewCreator.onSharedPreferenceChanged(sharedPreferences, null);

		RemoteViews remoteViews = widgetViewCreator.createRemoteViews(true);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}


	protected final void onBeginProcess(RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
	}

	protected final void onSuccessfulProcess(RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.content_container, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
	}

	protected final void onErrorProcess(RemoteViews remoteViews, String errorMsg, String btnMsg) {
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
		remoteViews.setTextViewText(R.id.warning, errorMsg);
		remoteViews.setTextViewText(R.id.warning_process_btn, btnMsg);
	}

	public void init(Context context, Bundle bundle) {
		//초기화
		Class<?> widgetProviderClass = getThis();
		RemoteViews remoteViews = bundle.getParcelable(WidgetCreator.WidgetAttributes.REMOTE_VIEWS.name());
		int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

		final SharedPreferences sharedPreferences =
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId),
						Context.MODE_PRIVATE);
		LocationType locationType =
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

			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(context, remoteViews,
					widgetProviderClass, appWidgetId));
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			loadWeatherData(context, appWidgetManager, remoteViews, appWidgetId);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		Log.e("APP WIDGET TAG : ", action);

		if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_INIT))) {
			init(context, intent.getExtras());
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			Bundle bundle = intent.getExtras();
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			RemoteViews remoteViews = bundle.getParcelable(WidgetCreator.WidgetAttributes.REMOTE_VIEWS.name());
			loadWeatherData(context, AppWidgetManager.getInstance(context), remoteViews, appWidgetId);
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_SHOW_DIALOG))) {
			Intent i = new Intent(context, DialogActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtras(intent.getExtras());
			context.startActivity(i);
		} else if (action.equals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) {
			context.startActivity(intent);
		} else if (action.equals(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) {
			context.startActivity(intent);
		} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH_CURRENT_LOCATION))) {
			Bundle bundle = intent.getExtras();
			int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			RemoteViews remoteViews = bundle.getParcelable(WidgetCreator.WidgetAttributes.REMOTE_VIEWS.name());
			loadCurrentLocation(context, remoteViews, appWidgetId);
		}
	}


	public static RemoteViews createRemoteViews(Context context, int appWidgetId, int layoutId) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
		SharedPreferences widgetAttributes =
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);
		Map<String, ?> map = widgetAttributes.getAll();
		Set<? extends Map.Entry<String, ?>> entrySet = map.entrySet();

		for (Map.Entry<String, ?> entry : entrySet) {
			try {
				remoteViews.setTextViewTextSize(Integer.parseInt(entry.getKey()), TypedValue.COMPLEX_UNIT_PX, (Float) entry.getValue());
			} catch (NumberFormatException e) {

			}
		}

		remoteViews.setViewVisibility(R.id.watch,
				widgetAttributes.getBoolean(WidgetCreator.WidgetAttributes.DISPLAY_CLOCK.name(), true) ?
						View.VISIBLE : View.GONE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);

		return remoteViews;
	}


	public static PendingIntent getOnClickedPendingIntent(Context context, RemoteViews remoteViews, Class<?> className, int appWidgetId) {
		Intent intent = new Intent(context, className);
		intent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_SHOW_DIALOG));
		Bundle bundle = new Bundle();
		bundle.putSerializable(WidgetCreator.WidgetAttributes.WIDGET_CLASS.name(), className);
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		bundle.putParcelable(WidgetCreator.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
		intent.putExtras(bundle);

		return PendingIntent.getBroadcast(
				context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	public void loadCurrentLocation(Context context, RemoteViews remoteViews, int appWidgetId) {
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		final SharedPreferences widgetAttributes =
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId),
						Context.MODE_PRIVATE);

		onBeginProcess(remoteViews);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {
				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						SharedPreferences.Editor editor = widgetAttributes.edit();
						editor.putString(WidgetDataKeys.ADDRESS_NAME.name(), addressList.get(0).getAddressLine(0));

						String countryCode = addressList.get(0).getCountryCode();
						if (countryCode.equals("KR")) {
							if (widgetAttributes.getBoolean(WidgetCreator.WidgetAttributes.TOP_PRIORITY_KMA.name(), false)) {
								editor.putString(WidgetCreator.WidgetAttributes.WEATHER_SOURCE_TYPE.name(), WeatherSourceType.KMA.name());
							}
						}
						editor.putString(WidgetDataKeys.LATITUDE.name(), String.valueOf(addressList.get(0).getLatitude()))
								.putString(WidgetDataKeys.LONGITUDE.name(), String.valueOf(addressList.get(0).getLongitude()))
								.putString(WidgetDataKeys.COUNTRY_CODE.name(), countryCode).apply();

						remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(context, remoteViews,
								getThis(), appWidgetId));

						appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
						loadWeatherData(context, appWidgetManager, remoteViews, appWidgetId);
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

				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, pendingIntent);

				onErrorProcess(remoteViews, errorMsg, btnMsg);
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

		MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				initWeatherSourceUniqueValues(context, weatherSourceType);
				setResultViews(context, appWidgetId, remoteViews, appWidgetManager, this, requestWeatherDataTypeSet);
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
	                                    @Nullable MultipleJsonDownloader<JsonElement> multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		SharedPreferences widgetAttributes =
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);
		final WeatherSourceType requestWeatherSourceType =
				WeatherSourceType.valueOf(widgetAttributes.getString(WidgetCreator.WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));

		CurrentConditionsObj currentConditionsObj = null;
		List<HourlyForecastObj> hourlyForecastObjList = null;
		List<DailyForecastObj> dailyForecastObjList = null;

		WidgetCreator widgetCreator = new WidgetCreator(context, this);
		widgetCreator.onSharedPreferenceChanged(widgetAttributes, null);

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
			currentConditionsObj = getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
					appWidgetId);
			widgetCreator.setCurrentConditionsViews(remoteViews, currentConditionsObj);
		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
			hourlyForecastObjList = getHourlyForecasts(context, requestWeatherSourceType, multipleJsonDownloader,
					appWidgetId);
			widgetCreator.setHourlyForecastViews(remoteViews, hourlyForecastObjList);

		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
			dailyForecastObjList = getDailyForecasts(context, requestWeatherSourceType, multipleJsonDownloader,
					appWidgetId);
			widgetCreator.setDailyForecastViews(remoteViews, dailyForecastObjList);

		}
		HeaderObj headerObj = getHeader(context, multipleJsonDownloader, appWidgetId, currentConditionsObj.zoneId);

		widgetCreator.setHeaderViews(remoteViews, headerObj);

		boolean displayLocalClock = widgetAttributes.getBoolean(WidgetCreator.WidgetAttributes.DISPLAY_LOCAL_CLOCK.name(), false);

		widgetCreator.setClockTimeZone(remoteViews, displayLocalClock ? currentConditionsObj.zoneId : ZoneId.systemDefault());
		onSuccessfulProcess(remoteViews);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	public final HeaderObj getHeader(Context context, MultipleJsonDownloader<JsonElement> multipleJsonDownloader, int appWidgetId,
	                                 ZoneId zoneId) {
		HeaderObj headerObj = new HeaderObj(zoneId != null);
		headerObj.address = context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE)
				.getString(WidgetDataKeys.ADDRESS_NAME.name(), "");

		if (zoneId == null) {
			headerObj.refreshDateTime = context.getString(R.string.error);
		} else {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			ValueUnits clockUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_clock),
					ValueUnits.clock12.name()));

			LocalDateTime updatedTime = LocalDateTime.now();
			headerObj.refreshDateTime =
					updatedTime.format(DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ?
							context.getString(R.string.datetime_pattern_clock12) : context.getString(R.string.datetime_pattern_clock24)));

			Log.e("timezone", zoneId.getId());
		}

		return headerObj;
	}

	public final CurrentConditionsObj getCurrentConditions(Context context, WeatherSourceType weatherSourceType,
	                                                       MultipleJsonDownloader<JsonElement> multipleJsonDownloader, int appWidgetId) {
		final LocalDateTime updatedTime = LocalDateTime.of(multipleJsonDownloader.getLocalDateTime().toLocalDate(),
				multipleJsonDownloader.getLocalDateTime().toLocalTime());
		String temp = null;
		String realFeelTemp = null;
		String precipitation = null;
		String airQuality = null;
		int weatherIcon = 0;
		ZoneId zoneId = ZoneId.systemDefault();

		String precipitationUnitStr = "mm";
		boolean successfulResponse = true;

		ValueUnits tempUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		String tempUnitStr = ValueUnits.convertToStr(context, tempUnit);

		switch (weatherSourceType) {
			case KMA:
				if (KmaResponseProcessor.successfulVilageResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST))) {
					FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditions(
							KmaResponseProcessor.getUltraSrtNcstObjFromJson(
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST).getResponse().body().toString()));
					temp = ValueUnits.convertTemperature(finalCurrentConditions.getTemperature(), tempUnit).toString() + tempUnitStr;
					precipitation = finalCurrentConditions.getPrecipitation1Hour().equals("0") ?
							context.getString(R.string.not_precipitation) :
							KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType()) + ", " + finalCurrentConditions.getPrecipitation1Hour() + precipitationUnitStr;

					SharedPreferences sharedPreferences =
							context.getSharedPreferences(WidgetCreator.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId,
									Context.MODE_PRIVATE);

					ZonedDateTime begin = ZonedDateTime.of(updatedTime, KmaResponseProcessor.getZoneId());
					ZonedDateTime end = ZonedDateTime.of(updatedTime, KmaResponseProcessor.getZoneId());
					Double latitude = Double.parseDouble(sharedPreferences.getString(WidgetDataKeys.LATITUDE.name(), "0.0"));
					Double longitude = Double.parseDouble(sharedPreferences.getString(WidgetDataKeys.LONGITUDE.name(), "0.0"));

					Map<String, SunsetriseUtil.SunSetRiseData> sunSetRiseDataMap = SunsetriseUtil.getSunSetRiseMap(begin, end, latitude, longitude);

					Date sunRiseDate = sunSetRiseDataMap.get(updatedTime.toString()).getSunrise();
					Date sunSetDate = sunSetRiseDataMap.get(updatedTime.toString()).getSunset();
					Date itemDate = new Date(updatedTime.toInstant(ZoneOffset.UTC).toEpochMilli());
					boolean isNight = SunsetriseUtil.isNight(itemDate, sunRiseDate, sunSetDate);

					weatherIcon = KmaResponseProcessor.getWeatherPtyIconImg(finalCurrentConditions.getPrecipitationType(), isNight);
				} else {
					successfulResponse = false;
				}
				break;
			case ACCU_WEATHER:
				if (AccuWeatherResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS))) {
					CurrentConditionsResponse currentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
							multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponse().body());
					CurrentConditionsResponse.Item item = currentConditionsResponse.getItems().get(0);
					temp = ValueUnits.convertTemperature(item.getTemperature().getMetric().getValue(),
							tempUnit).toString() + tempUnitStr;
					realFeelTemp = ValueUnits.convertTemperature(item.getRealFeelTemperature().getMetric().getValue(),
							tempUnit).toString() + tempUnitStr;
					precipitation = item.getPrecip1hr() == null ?
							context.getString(R.string.not_precipitation) :
							AccuWeatherResponseProcessor.getPty(item.getPrecipitationType()) + ", " + item.getPrecip1hr().getMetric().getValue() +
									precipitationUnitStr;
					weatherIcon =
							AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon());

					zoneId = ZonedDateTime.parse(item.getLocalObservationDateTime()).getZone();
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
					temp = ValueUnits.convertTemperature(current.getTemp(), tempUnit).toString() + tempUnitStr;
					realFeelTemp = ValueUnits.convertTemperature(current.getFeelsLike(), tempUnit).toString() + tempUnitStr;

					if (current.getRain() != null && current.getSnow() != null) {
						precipitation = context.getString(
								R.string.owm_icon_616_rain_and_snow) + ", " + current.getRain().getPrecipitation1Hour() + precipitationUnitStr + ", " + current.getSnow().getPrecipitation1Hour() + precipitationUnitStr;
					} else if (current.getRain() != null) {
						precipitation = context.getString(R.string.rain) + ", " + current.getRain().getPrecipitation1Hour() + precipitationUnitStr;
					} else if (current.getSnow() != null) {
						precipitation = context.getString(R.string.snow) + ", " + current.getSnow().getPrecipitation1Hour() + precipitationUnitStr;
					} else {
						precipitation = context.getString(R.string.not_precipitation);
					}
					weatherIcon = OpenWeatherMapResponseProcessor.getWeatherIconImg(current.getWeather().get(0).getId(), false);
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);
				} else {
					successfulResponse = false;
				}
				break;
		}
		final String notData = context.getString(R.string.not_data);

		if (AqicnResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN).get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED))) {
			GeolocalizedFeedResponse geolocalizedFeedResponse =
					AqicnResponseProcessor.getAirQualityObjFromJson((Response<JsonElement>) multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN).get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED).getResponse());
			airQuality =
					geolocalizedFeedResponse.getData().getIaqi().getPm10() == null ? notData :
							AqicnResponseProcessor.getGradeDescription(Integer.parseInt(geolocalizedFeedResponse.getData().getIaqi().getPm10().getValue()));
		} else {
			airQuality = notData;
		}

		CurrentConditionsObj currentConditionsObj = new CurrentConditionsObj(successfulResponse);
		if (successfulResponse) {
			currentConditionsObj.temp = temp;
			currentConditionsObj.realFeelTemp = realFeelTemp;
			currentConditionsObj.precipitation = precipitation;
			currentConditionsObj.weatherIcon = weatherIcon;
			context.getSharedPreferences(WidgetCreator.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId, Context.MODE_PRIVATE)
					.edit().putString(WidgetDataKeys.TIMEZONE_ID.name(), zoneId.getId()).apply();
		} else {
			currentConditionsObj.temp = notData;
			currentConditionsObj.realFeelTemp = notData;
			currentConditionsObj.precipitation = notData;
			currentConditionsObj.weatherIcon = R.drawable.error;
		}
		currentConditionsObj.airQuality = airQuality;
		currentConditionsObj.zoneId = zoneId;
		return currentConditionsObj;
	}

	public final List<HourlyForecastObj> getHourlyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                        MultipleJsonDownloader<JsonElement> multipleJsonDownloader, int appWidgetId) {
		boolean successfulResponse = true;
		List<HourlyForecastObj> hourlyForecastObjList = new ArrayList<>();
		ZoneId zoneId = null;

		ValueUnits tempUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		String tempUnitStr = context.getString(R.string.degree_symbol);

		DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.time_pattern_of_hourly_forecast_in_widget));
		DateTimeFormatter hoursIf0HourFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.time_pattern_if_hours_0_of_hourly_forecast_in_widget));

		switch (weatherSourceType) {
			case KMA:
				if (KmaResponseProcessor.successfulVilageResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST)) &&
						KmaResponseProcessor.successfulVilageResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST))) {
					zoneId = KmaResponseProcessor.getZoneId();
					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
							KmaResponseProcessor.getUltraSrtFcstObjFromJson(
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST).getResponse().body().toString()),
							KmaResponseProcessor.getVilageFcstObjFromJson(
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST).getResponse().body().toString()));

					ZonedDateTime begin = ZonedDateTime.of(finalHourlyForecastList.get(0).getFcstDateTime(), zoneId);
					ZonedDateTime end = ZonedDateTime.of(finalHourlyForecastList.get(finalHourlyForecastList.size() - 1).getFcstDateTime(),
							zoneId);
					SharedPreferences sharedPreferences =
							context.getSharedPreferences(WidgetCreator.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId,
									Context.MODE_PRIVATE);

					Double latitude = Double.parseDouble(sharedPreferences.getString(WidgetDataKeys.LATITUDE.name(), "0.0"));
					Double longitude = Double.parseDouble(sharedPreferences.getString(WidgetDataKeys.LONGITUDE.name(), "0.0"));

					Map<String, SunsetriseUtil.SunSetRiseData> sunSetRiseDataMap = SunsetriseUtil.getSunSetRiseMap(begin, end, latitude, longitude);
					int index = 0;
					for (FinalHourlyForecast hourlyForecast : finalHourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);

						LocalDateTime dateTime = hourlyForecast.getFcstDateTime();
						hourlyForecastObj.clock = dateTime.format(dateTime.getHour() == 0 ? hoursIf0HourFormatter : hoursFormatter);

						Date sunRiseDate = sunSetRiseDataMap.get(dateTime.toString()).getSunrise();
						Date sunSetDate = sunSetRiseDataMap.get(dateTime.toString()).getSunset();
						Date itemDate = new Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli());

						hourlyForecastObj.weatherIcon = KmaResponseProcessor.getWeatherSkyIconImg(hourlyForecast.getSky(), SunsetriseUtil.isNight(itemDate, sunRiseDate, sunSetDate));
						hourlyForecastObj.temp = ValueUnits.convertTemperature(hourlyForecast.getTemp1Hour(), tempUnit) + tempUnitStr;

						hourlyForecastObjList.add(hourlyForecastObj);

						if (++index == 10) {
							break;
						}
					}
				} else {

				}
				break;
			case ACCU_WEATHER:
				if (AccuWeatherResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_12_HOURLY))) {
					TwelveHoursOfHourlyForecastsResponse hourlyForecastResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
							multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_12_HOURLY).getResponse().body());

					zoneId = ZonedDateTime.parse(hourlyForecastResponse.getItems().get(0).getDateTime()).getZone();
					List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyForecastList = hourlyForecastResponse.getItems();
					int index = 0;

					for (TwelveHoursOfHourlyForecastsResponse.Item hourlyForecast : hourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);

						LocalDateTime dateTime =
								WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourlyForecast.getEpochDateTime()) * 1000L, TimeZone.getTimeZone(zoneId.getId()));
						hourlyForecastObj.clock = dateTime.format(dateTime.getHour() == 0 ? hoursIf0HourFormatter : hoursFormatter);
						hourlyForecastObj.temp =
								ValueUnits.convertTemperature(hourlyForecast.getTemperature().getValue(), tempUnit) + tempUnitStr;
						hourlyForecastObj.weatherIcon = AccuWeatherResponseProcessor.getWeatherIconImg(hourlyForecast.getWeatherIcon());

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

						LocalDateTime dateTime =
								WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(item.getDt()) * 1000L,
										TimeZone.getTimeZone(zoneId.getId()));
						hourlyForecastObj.clock = dateTime.format(dateTime.getHour() == 0 ? hoursIf0HourFormatter : hoursFormatter);
						hourlyForecastObj.weatherIcon = OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(),
								item.getWeather().get(0).getIcon().contains("n"));
						hourlyForecastObj.temp = ValueUnits.convertTemperature(item.getTemp(), tempUnit) + tempUnitStr;

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
		if (successfulResponse) {
			context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE)
					.edit().putString(WidgetDataKeys.TIMEZONE_ID.name(), zoneId.getId()).apply();
			hourlyForecastObjList.get(0).zoneId = zoneId;
		}
		return hourlyForecastObjList;
	}

	public final List<DailyForecastObj> getDailyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                      MultipleJsonDownloader<JsonElement> multipleJsonDownloader, int appWidgetId) {
		boolean successfulResponse = true;
		List<DailyForecastObj> dailyForecastObjList = new ArrayList<>();
		ZoneId zoneId = null;

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.date_pattern_of_daily_forecast_in_widget));
		ValueUnits tempUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		String tempUnitStr = context.getString(R.string.degree_symbol);

		switch (weatherSourceType) {
			case KMA:
				if (KmaResponseProcessor.successfulMidResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_LAND_FCST))
						&& KmaResponseProcessor.successfulMidResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_TA_FCST))) {
					List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(
							KmaResponseProcessor.getMidLandObjFromJson(
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_LAND_FCST).getResponse().body().toString()),
							KmaResponseProcessor.getMidTaObjFromJson(
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_TA_FCST).getResponse().body().toString()),
							Long.parseLong(multipleJsonDownloader.get("tmFc")));

					int index = 0;

					for (FinalDailyForecast finalDailyForecast : finalDailyForecastList) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(true, false);
						dailyForecastObj.date = finalDailyForecast.getDate().format(dateFormatter);
						dailyForecastObj.temp =
								ValueUnits.convertTemperature(finalDailyForecastList.get(index).getMinTemp(), tempUnit).toString() + tempUnitStr
										+ " / " + ValueUnits.convertTemperature(finalDailyForecastList.get(index).getMaxTemp(), tempUnit).toString() + tempUnitStr;
						dailyForecastObj.dayWeatherIcon =
								KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getAmSky(), false);
						dailyForecastObj.nightWeatherIcon =
								KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getPmSky(), false);

						dailyForecastObjList.add(dailyForecastObj);
						if (++index == 4) {
							break;
						}
					}
					zoneId = KmaResponseProcessor.getZoneId();
				} else {
					successfulResponse = false;
				}
				break;
			case ACCU_WEATHER:
				if (AccuWeatherResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY))) {
					FiveDaysOfDailyForecastsResponse dailyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
							multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponse().body().toString());

					int index = 0;
					List<FiveDaysOfDailyForecastsResponse.DailyForecasts> dailyForecasts = dailyForecastsResponse.getDailyForecasts();
					zoneId = ZonedDateTime.parse(dailyForecasts.get(0).getDateTime()).getZone();

					for (FiveDaysOfDailyForecastsResponse.DailyForecasts item : dailyForecasts) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(true, false);

						dailyForecastObj.date =
								WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getEpochDate()) * 1000L,
										TimeZone.getTimeZone(zoneId.getId())).format(dateFormatter);
						dailyForecastObj.temp =
								ValueUnits.convertTemperature(item.getTemperature().getMinimum().getValue(), tempUnit).toString() + tempUnitStr + " / " + ValueUnits.convertTemperature(item.getTemperature().getMaximum().getValue(), tempUnit).toString() + tempUnitStr;
						dailyForecastObj.dayWeatherIcon = AccuWeatherResponseProcessor.getWeatherIconImg(item.getDay().getIcon());
						dailyForecastObj.nightWeatherIcon = AccuWeatherResponseProcessor.getWeatherIconImg(item.getNight().getIcon());
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
						dailyForecastObj.date =
								(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getDt()) * 1000L,
										TimeZone.getTimeZone(zoneId.getId())).format(
										dateFormatter));
						dailyForecastObj.temp =
								ValueUnits.convertTemperature(item.getTemp().getMin(), tempUnit) + tempUnitStr + " / " + ValueUnits.convertTemperature(item.getTemp().getMax(), tempUnit) + tempUnitStr;
						dailyForecastObj.weatherIcon = OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId()
								, false);
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

		if (successfulResponse) {
			dailyForecastObjList.get(0).zoneId = zoneId;
			context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE)
					.edit().putString(WidgetDataKeys.TIMEZONE_ID.name(), zoneId.getId()).apply();
		}
		return dailyForecastObjList;
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

	public static class HeaderObj {
		final boolean successful;
		String address;
		String refreshDateTime;

		public HeaderObj(boolean successful) {
			this.successful = successful;
		}

	}

	public static class CurrentConditionsObj {
		final boolean successful;
		String temp;
		String realFeelTemp;
		String precipitation;
		String airQuality;
		int weatherIcon;
		ZoneId zoneId;

		public CurrentConditionsObj(boolean successful) {
			this.successful = successful;
		}

	}

	public static class HourlyForecastObj {
		final boolean successful;
		String clock;
		String temp;
		int weatherIcon;
		ZoneId zoneId;

		public HourlyForecastObj(boolean successful) {
			this.successful = successful;
		}

	}

	public static class DailyForecastObj {
		final boolean successful;
		final boolean isSingle;
		String date;
		String temp;
		int dayWeatherIcon;
		int nightWeatherIcon;
		int weatherIcon;
		ZoneId zoneId;

		public DailyForecastObj(boolean successful, boolean isSingle) {
			this.successful = successful;
			this.isSingle = isSingle;
		}

	}

	public enum WidgetDataKeys {
		ADDRESS_NAME, LATITUDE, LONGITUDE, COUNTRY_CODE, TIMEZONE_ID
	}
}
