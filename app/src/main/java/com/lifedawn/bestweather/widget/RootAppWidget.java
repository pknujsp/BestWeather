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
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunsetriseUtil;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Response;

public class RootAppWidget extends AppWidgetProvider {
	public static final String tag = "appWidget";
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M.d E HH:mm");
	public static final DateTimeFormatter WATCH_DATE_FORMATTER = DateTimeFormatter.ofPattern("M.d E");
	public static DateTimeFormatter WATCH_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	protected Gps gps = new Gps(null, null, null);
	protected ValueUnits tempUnit;
	protected String tempUnitStr;
	protected static ArrayMap<Integer, ConfigureWidgetActivity.CustomAttributeObj> attributeArrayMap = new ArrayMap<>();
	protected static ArrayMap<Integer, WidgetDataObj> widgetDataObjArrayMap = new ArrayMap<>();

	public void onTimeTick(Context context) {

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.e(tag, intent.getAction());
		String action = intent.getAction();

		if (action.equals(context.getString(R.string.ACTION_INIT))) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			Bundle bundle = intent.getExtras();
			final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			final int layoutId = bundle.getInt(context.getString(R.string.bundle_key_widget_layout_id));
			final Class<?> widgetClass = (Class<?>) bundle.getSerializable(context.getString(R.string.bundle_key_widgetname));

			ConfigureWidgetActivity.CustomAttributeObj customAttributeObj =
					(ConfigureWidgetActivity.CustomAttributeObj) bundle.getSerializable(context.getString(R.string.bundle_key_widget_custom_attributes));
			attributeArrayMap.put(appWidgetId, customAttributeObj);

			WidgetDataObj widgetDataObj = new WidgetDataObj();
			widgetDataObj.locationType = customAttributeObj.locationType;
			widgetDataObj.selectedAddressDto = customAttributeObj.selectedAddressDto;
			widgetDataObjArrayMap.put(appWidgetId, widgetDataObj);


			RemoteViews remoteViews = createViews(context, appWidgetId, layoutId);
			if (customAttributeObj.displayDateTime) {
				setWatch(remoteViews, TimeZone.getDefault());
			}
			remoteViews.setViewVisibility(R.id.watch, customAttributeObj.displayDateTime ? View.VISIBLE : View.GONE);

			widgetDataObj.remoteViews = remoteViews;
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			init(context, appWidgetId, remoteViews, widgetClass);
		} else if (action.equals(context.getString(R.string.ACTION_REFRESH))) {
			Bundle bundle = intent.getExtras();
			final Class<?> widgetClass = (Class<?>) bundle.getSerializable(context.getString(R.string.bundle_key_widgetname));
			final int layoutId = bundle.getInt(context.getString(R.string.bundle_key_widget_layout_id));

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName componentName = new ComponentName(context.getPackageName(), widgetClass.getName());
			int[] widgetIds = appWidgetManager.getAppWidgetIds(componentName);

			for (int appWidgetId : widgetIds) {
				loadWeatherData(context, appWidgetId, widgetDataObjArrayMap.get(appWidgetId).remoteViews);
			}
		} else if (action.equals(context.getString(R.string.ACTION_SHOW_DIALOG))) {
			Intent i = new Intent(context, DialogActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtras(intent.getExtras());
			context.startActivity(i);
		} else if (action.equals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) {
			context.startActivity(intent);
		} else if (action.equals(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) {
			context.startActivity(intent);
		} else if (action.equals(context.getString(R.string.ACTION_REFRESH_CURRENT_LOCATION))) {
			Bundle bundle = intent.getExtras();
			final Class<?> widgetClass = (Class<?>) bundle.getSerializable(context.getString(R.string.bundle_key_widgetname));

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName componentName = new ComponentName(context.getPackageName(), widgetClass.getName());
			int[] widgetIds = appWidgetManager.getAppWidgetIds(componentName);

			for (int appWidgetId : widgetIds) {
				if (widgetDataObjArrayMap.get(appWidgetId).locationType == LocationType.CurrentLocation) {
					loadCurrentLocation(context, appWidgetId, widgetDataObjArrayMap.get(appWidgetId).remoteViews, widgetClass);
				}
			}
		} else if (action.equals(Intent.ACTION_TIME_TICK)) {
			onTimeTick(context);
		}

		if (tempUnit == null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			tempUnit = ValueUnits.enumOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
					ValueUnits.celsius.name()));
			tempUnitStr = ValueUnits.convertToStr(context, tempUnit);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		ValueUnits clockUnit =
				ValueUnits.enumOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name()));
		WATCH_TIME_FORMATTER = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? "a h:mm" : "HH:mm");
	}

	public static RemoteViews createViews(Context context, int appWidgetId, int layoutId) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
		ConfigureWidgetActivity.CustomAttributeObj customAttributeObj = attributeArrayMap.get(appWidgetId);
		List<ConfigureWidgetActivity.TextSizeObj> textSizeObjList = customAttributeObj.textSizeObjList;

		for (ConfigureWidgetActivity.TextSizeObj textSizeObj : textSizeObjList) {
			remoteViews.setTextViewTextSize(textSizeObj.viewId, TypedValue.COMPLEX_UNIT_PX, textSizeObj.textSize);
		}

		return remoteViews;
	}

	public void setWatch(RemoteViews remoteViews, TimeZone timeZone) {
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone.getID()));
		remoteViews.setTextViewText(R.id.date, now.format(WATCH_DATE_FORMATTER));
		remoteViews.setTextViewText(R.id.time, now.format(WATCH_TIME_FORMATTER));
	}

	public static PendingIntent getOnClickedPendingIntent(Context context, int appWidgetId, int widgetLayoutId, Class<?> className) {
		Intent intent = new Intent(context, className);
		intent.setAction(context.getString(R.string.ACTION_SHOW_DIALOG));

		Bundle bundle = new Bundle();
		bundle.putSerializable(context.getString(R.string.bundle_key_widgetname), className);
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		bundle.putInt(context.getString(R.string.bundle_key_widget_layout_id), widgetLayoutId);
		bundle.putSerializable(context.getString(R.string.bundle_key_location_type), Objects.requireNonNull(widgetDataObjArrayMap.get(appWidgetId)).locationType);

		intent.putExtras(bundle);
		return PendingIntent.getBroadcast(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void loadCurrentLocation(Context context, int appWidgetId, RemoteViews remoteViews, Class<?> className) {
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {
				widgetDataObjArrayMap.get(appWidgetId).location = location;
				widgetDataObjArrayMap.get(appWidgetId).latitude = location.getLatitude();
				widgetDataObjArrayMap.get(appWidgetId).longitude = location.getLongitude();

				Geocoding.geocoding(context, location.getLatitude(), location.getLongitude(), new Geocoding.GeocodingCallback() {
					@Override
					public void onGeocodingResult(List<Address> addressList) {
						widgetDataObjArrayMap.get(appWidgetId).addressName = addressList.get(0).getAddressLine(0);
						widgetDataObjArrayMap.get(appWidgetId).weatherSourceType =
								attributeArrayMap.get(appWidgetId).weatherSourceType;

						String countryCode = addressList.get(0).getCountryCode();
						if (countryCode.equals("KR")) {
							if (attributeArrayMap.get(appWidgetId).topPriorityKma) {
								widgetDataObjArrayMap.get(appWidgetId).weatherSourceType = WeatherSourceType.KMA;
							}
						}

						remoteViews.setOnClickPendingIntent(R.id.content_container, getOnClickedPendingIntent(context, appWidgetId,
								attributeArrayMap.get(appWidgetId).layoutId, className));
						AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
						appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

						Log.e(tag, countryCode);
						Log.e(tag, addressList.get(0).getAddressLine(0));
						loadWeatherData(context, appWidgetId, remoteViews);
					}
				});
			}

			@Override
			public void onFailed(Fail fail) {
				Intent intent = new Intent(context, className);

				if (fail == Fail.REJECT_PERMISSION) {
					remoteViews.setTextViewText(R.id.warning, context.getString(R.string.message_needs_location_permission));
					remoteViews.setTextViewText(R.id.warning_process_btn, context.getString(R.string.check_permission));
					intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					intent.setData(Uri.fromParts("package", context.getPackageName(), null));
				} else {
					remoteViews.setTextViewText(R.id.warning, context.getString(R.string.request_to_make_gps_on));
					remoteViews.setTextViewText(R.id.warning_process_btn, context.getString(R.string.enable_gps));
					intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				}

				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, pendingIntent);

				remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
				remoteViews.setViewVisibility(R.id.content_container, View.GONE);
				remoteViews.setViewVisibility(R.id.warning_layout, View.VISIBLE);
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}
		};

		if (gps.checkPermissionAndGpsEnabled(context, locationCallback)) {
			gps.runGps(context, locationCallback);
		}
	}

	public void init(Context context, int appWidgetId, RemoteViews remoteViews, Class<?> className) {
		if (widgetDataObjArrayMap.get(appWidgetId).locationType == LocationType.CurrentLocation) {
			loadCurrentLocation(context, appWidgetId, remoteViews, className);
		} else {
			widgetDataObjArrayMap.get(appWidgetId).addressName = widgetDataObjArrayMap.get(appWidgetId).selectedAddressDto.getAddress();
			widgetDataObjArrayMap.get(appWidgetId).weatherSourceType = attributeArrayMap.get(appWidgetId).weatherSourceType;
			widgetDataObjArrayMap.get(appWidgetId).latitude =
					Double.parseDouble(attributeArrayMap.get(appWidgetId).selectedAddressDto.getLatitude());
			widgetDataObjArrayMap.get(appWidgetId).longitude =
					Double.parseDouble(attributeArrayMap.get(appWidgetId).selectedAddressDto.getLongitude());
			if (attributeArrayMap.get(appWidgetId).selectedAddressDto.getCountryCode().equals("KR")) {
				if (attributeArrayMap.get(appWidgetId).topPriorityKma) {
					widgetDataObjArrayMap.get(appWidgetId).weatherSourceType = WeatherSourceType.KMA;
				}
			}
			remoteViews.setOnClickPendingIntent(R.id.content_container, getOnClickedPendingIntent(context, appWidgetId,
					attributeArrayMap.get(appWidgetId).layoutId, className));
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			loadWeatherData(context,
					appWidgetId, remoteViews);
		}
	}

	public void loadWeatherData(Context context, int appWidgetId, RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.content_container, View.GONE);
		remoteViews.setViewVisibility(R.id.warning_layout, View.GONE);
		AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteViews);
	}

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

	public void setResultViews(Context context, AppWidgetManager appWidgetManager,
	                           int appWidgetId, RemoteViews remoteViews, @Nullable MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		Log.e(tag, "데이터 응답완료");
	}

	public final HeaderObj getHeader(Context context, MultipleJsonDownloader<JsonElement> multipleJsonDownloader, int appWidgetId, TimeZone timeZone) {
		if (timeZone == null) {
			HeaderObj headerObj = new HeaderObj(false);
			headerObj.address = context.getString(R.string.error);
			headerObj.refreshDateTime = context.getString(R.string.error);
			return headerObj;
		} else {
			ZonedDateTime updatedTime = ZonedDateTime.now(ZoneId.of(timeZone.getID()));

			HeaderObj headerObj = new HeaderObj(true);
			headerObj.address = Objects.requireNonNull(widgetDataObjArrayMap.get(appWidgetId)).addressName;
			headerObj.refreshDateTime = updatedTime.format(DATE_TIME_FORMATTER);
			return headerObj;
		}
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
		TimeZone timeZone = TimeZone.getDefault();

		String precipitationUnitStr = "mm";
		boolean successfulResponse = true;

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

					timeZone = TimeZone.getTimeZone("Asia/Seoul");
					ZonedDateTime begin = ZonedDateTime.of(updatedTime, ZoneId.of(timeZone.getID()));
					ZonedDateTime end = ZonedDateTime.of(updatedTime, ZoneId.of(timeZone.getID()));
					Double latitude = Objects.requireNonNull(widgetDataObjArrayMap.get(appWidgetId)).latitude;
					Double longitude = Objects.requireNonNull(widgetDataObjArrayMap.get(appWidgetId)).longitude;

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
					temp = ValueUnits.convertTemperature(currentConditionsResponse.getItems().get(0).getTemperature().getMetric().getValue(),
							tempUnit).toString() + tempUnitStr;
					realFeelTemp = ValueUnits.convertTemperature(currentConditionsResponse.getItems().get(0).getRealFeelTemperature().getMetric().getValue(),
							tempUnit).toString() + tempUnitStr;
					precipitation = currentConditionsResponse.getItems().get(0).getPrecip1hr() == null ?
							context.getString(R.string.not_precipitation) :
							AccuWeatherResponseProcessor.getPty(currentConditionsResponse.getItems().get(0).getPrecipitationType()) + ", " + currentConditionsResponse.getItems().get(0).getPrecip1hr().getMetric().getValue() +
									precipitationUnitStr;
					weatherIcon =
							AccuWeatherResponseProcessor.getWeatherIconImg(currentConditionsResponse.getItems().get(0).getWeatherIcon());

					try {
						ZoneId zoneId = AccuWeatherResponseProcessor.getTimeZone(
								currentConditionsResponse.getItems().get(0).getLocalObservationDateTime());
						timeZone = TimeZone.getDefault();
						timeZone.setID(zoneId.toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
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
					timeZone = OpenWeatherMapResponseProcessor.getTimeZone(oneCallResponse);
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
			widgetDataObjArrayMap.get(appWidgetId).timeZone = timeZone;
		} else {
			currentConditionsObj.temp = notData;
			currentConditionsObj.realFeelTemp = notData;
			currentConditionsObj.precipitation = notData;
			currentConditionsObj.weatherIcon = R.drawable.error;
		}
		currentConditionsObj.airQuality = airQuality;
		currentConditionsObj.timeZone = timeZone;
		return currentConditionsObj;
	}

	public final List<HourlyForecastObj> getHourlyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                        MultipleJsonDownloader<JsonElement> multipleJsonDownloader, int appWidgetId) {
		boolean successfulResponse = true;
		List<HourlyForecastObj> hourlyForecastObjList = new ArrayList<>();
		TimeZone timeZone = TimeZone.getDefault();

		DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("H");

		switch (weatherSourceType) {
			case KMA:
				if (KmaResponseProcessor.successfulVilageResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST)) &&
						KmaResponseProcessor.successfulVilageResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST))) {
					timeZone = TimeZone.getTimeZone("Asia/Seoul");
					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
							KmaResponseProcessor.getUltraSrtFcstObjFromJson(
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST).getResponse().body().toString()),
							KmaResponseProcessor.getVilageFcstObjFromJson(
									multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST).getResponse().body().toString()));

					ZonedDateTime begin = ZonedDateTime.of(finalHourlyForecastList.get(0).getFcstDateTime(), ZoneId.of(timeZone.getID()));
					ZonedDateTime end = ZonedDateTime.of(finalHourlyForecastList.get(finalHourlyForecastList.size() - 1).getFcstDateTime(),
							ZoneId.of(timeZone.getID()));
					Double latitude = Objects.requireNonNull(widgetDataObjArrayMap.get(appWidgetId)).latitude;
					Double longitude = Objects.requireNonNull(widgetDataObjArrayMap.get(appWidgetId)).longitude;

					Map<String, SunsetriseUtil.SunSetRiseData> sunSetRiseDataMap = SunsetriseUtil.getSunSetRiseMap(begin, end, latitude, longitude);
					int index = 0;
					for (FinalHourlyForecast hourlyForecast : finalHourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);

						LocalDateTime dateTime = hourlyForecast.getFcstDateTime();
						hourlyForecastObj.clock = dateTime.format(clockFormatter);

						Date sunRiseDate = sunSetRiseDataMap.get(dateTime.toString()).getSunrise();
						Date sunSetDate = sunSetRiseDataMap.get(dateTime.toString()).getSunset();
						Date itemDate = new Date(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli());

						hourlyForecastObj.weatherIcon = KmaResponseProcessor.getWeatherSkyIconImg(hourlyForecast.getSky(), SunsetriseUtil.isNight(itemDate, sunRiseDate, sunSetDate));
						hourlyForecastObj.temp = ValueUnits.convertTemperature(hourlyForecast.getTemp1Hour(), tempUnit).toString() + tempUnitStr;

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

					try {
						ZoneId zoneId = AccuWeatherResponseProcessor.getTimeZone(
								hourlyForecastResponse.getItems().get(0).getDateTime());
						timeZone = TimeZone.getDefault();
						timeZone.setID(zoneId.toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}

					List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyForecastList = hourlyForecastResponse.getItems();
					int index = 0;

					for (TwelveHoursOfHourlyForecastsResponse.Item hourlyForecast : hourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);

						LocalDateTime dateTime =
								WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourlyForecast.getEpochDateTime()) * 1000L, timeZone);
						hourlyForecastObj.clock = dateTime.format(clockFormatter);
						hourlyForecastObj.temp =
								ValueUnits.convertTemperature(hourlyForecast.getTemperature().getValue(), tempUnit).toString() + tempUnitStr;
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
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().toString());
					timeZone = OpenWeatherMapResponseProcessor.getTimeZone(oneCallResponse);

					List<OneCallResponse.Hourly> hourly = oneCallResponse.getHourly();
					int index = 0;

					for (OneCallResponse.Hourly item : hourly) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);

						LocalDateTime dateTime =
								WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(item.getDt()) * 1000L, timeZone);
						hourlyForecastObj.clock = dateTime.format(clockFormatter);
						hourlyForecastObj.weatherIcon = OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(),
								item.getWeather().get(0).getIcon().contains("n"));
						hourlyForecastObj.temp = ValueUnits.convertTemperature(item.getTemp(), tempUnit).toString() + tempUnitStr;

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
			widgetDataObjArrayMap.get(appWidgetId).timeZone = timeZone;
			hourlyForecastObjList.get(0).timeZone = timeZone;
		}
		return hourlyForecastObjList;
	}

	public final List<DailyForecastObj> getDailyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                      MultipleJsonDownloader<JsonElement> multipleJsonDownloader, int appWidgetId) {
		boolean successfulResponse = true;
		List<DailyForecastObj> dailyForecastObjList = new ArrayList<>();
		TimeZone timeZone = TimeZone.getDefault();

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d");

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
								ValueUnits.convertTemperature(finalDailyForecastList.get(index).getMinTemp(), tempUnit).toString()
										+ " / " + ValueUnits.convertTemperature(finalDailyForecastList.get(index).getMaxTemp(), tempUnit).toString();
						dailyForecastObj.dayWeatherIcon =
								KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getAmSky(), false);
						dailyForecastObj.nightWeatherIcon =
								KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getPmSky(), false);

						dailyForecastObjList.add(dailyForecastObj);
						if (++index == 4) {
							break;
						}
					}
					timeZone = TimeZone.getTimeZone("Asia/Seoul");
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
					try {
						ZoneId zoneId = AccuWeatherResponseProcessor.getTimeZone(
								dailyForecasts.get(0).getDateTime());
						timeZone = TimeZone.getDefault();
						timeZone.setID(zoneId.toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}

					for (FiveDaysOfDailyForecastsResponse.DailyForecasts item : dailyForecasts) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(true, false);

						dailyForecastObj.date =
								WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getEpochDate()) * 1000L,
										timeZone).format(dateFormatter);
						dailyForecastObj.temp =
								ValueUnits.convertTemperature(item.getTemperature().getMinimum().getValue(), tempUnit).toString() + " / " + ValueUnits.convertTemperature(item.getTemperature().getMaximum().getValue(), tempUnit).toString();
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
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().toString());
					timeZone = OpenWeatherMapResponseProcessor.getTimeZone(oneCallResponse);

					List<OneCallResponse.Daily> daily = oneCallResponse.getDaily();
					int index = 0;

					for (OneCallResponse.Daily item : daily) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(true, true);
						dailyForecastObj.date = (WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getDt()) * 1000L, timeZone).format(
								dateFormatter));
						dailyForecastObj.temp =
								ValueUnits.convertTemperature(item.getTemp().getMin(), tempUnit) + " / " + ValueUnits.convertTemperature(item.getTemp().getMax(), tempUnit);
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
			dailyForecastObjList.get(0).timeZone = timeZone;
			widgetDataObjArrayMap.get(appWidgetId).timeZone = timeZone;
		}
		return dailyForecastObjList;
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
		TimeZone timeZone;

		public CurrentConditionsObj(boolean successful) {
			this.successful = successful;
		}

	}

	public static class HourlyForecastObj {
		final boolean successful;
		String clock;
		String temp;
		int weatherIcon;
		TimeZone timeZone;

		public HourlyForecastObj(boolean successful) {
			this.successful = successful;
		}

	}

	public static class DailyForecastObj {
		private final boolean successful;
		private final boolean isSingle;
		private String date;
		private String temp;
		private int dayWeatherIcon;
		private int nightWeatherIcon;
		private int weatherIcon;
		private TimeZone timeZone;

		public DailyForecastObj(boolean successful, boolean isSingle) {
			this.successful = successful;
			this.isSingle = isSingle;
		}

	}

	public static class WidgetDataObj {
		LocationType locationType;
		String addressName;
		Double latitude;
		Double longitude;
		TimeZone timeZone;
		Location location;
		WeatherSourceType weatherSourceType;
		FavoriteAddressDto selectedAddressDto;
		LocalDateTime updatedDateTime;
		RemoteViews remoteViews;
	}
}
