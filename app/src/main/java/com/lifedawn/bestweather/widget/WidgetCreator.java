package com.lifedawn.bestweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.widget.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.widget.dto.DailyForecastObj;
import com.lifedawn.bestweather.widget.dto.HeaderObj;
import com.lifedawn.bestweather.widget.dto.HourlyForecastObj;
import com.lifedawn.bestweather.widget.dto.WeatherJsonObj;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WidgetCreator implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String tag = "WidgetCreator";

	private int appWidgetId;
	private int backgroundAlpha;
	private LocationType locationType;
	private WeatherSourceType weatherSourceType;
	private boolean kmaTopPriority;
	private long updateInterval;
	private boolean displayClock;
	private boolean displayLocalClock;
	private int selectedAddressDtoId;

	private int addressInHeaderTextSize;
	private int refreshInHeaderTextSize;

	private int dateInClockTextSize;
	private int timeInClockTextSize;

	private int tempInCurrentTextSize;
	private int realFeelTempInCurrentTextSize;
	private int airQualityInCurrentTextSize;
	private int precipitationInCurrentTextSize;

	private int clockInHourlyTextSize;
	private int tempInHourlyTextSize;

	private int dateInDailyTextSize;
	private int tempInDailyTextSize;

	private final Context context;
	private WidgetUpdateCallback widgetUpdateCallback;

	private final ValueUnits tempUnit;
	private final String tempDegree;

	private final String dateFormat;
	private final String timeFormat;
	private final DateTimeFormatter dateTimeFormatter;
	private final ValueUnits clockUnit;

	public static String getSharedPreferenceName(int appWidgetId) {
		return WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId;
	}

	public enum WidgetAttributes {
		WIDGET_ATTRIBUTES_ID, APP_WIDGET_ID, BACKGROUND_ALPHA, LOCATION_TYPE, WEATHER_SOURCE_TYPE, TOP_PRIORITY_KMA,
		UPDATE_INTERVAL, DISPLAY_CLOCK, DISPLAY_LOCAL_CLOCK, SELECTED_ADDRESS_DTO_ID, WIDGET_CLASS, REMOTE_VIEWS
	}

	public static class WidgetTextViews {
		public enum Header {
			ADDRESS_TEXT_IN_HEADER, REFRESH_TEXT_IN_HEADER
		}

		public enum Clock {
			DATE_TEXT_IN_CLOCK, TIME_TEXT_IN_CLOCK
		}

		public enum Current {
			TEMP_TEXT_IN_CURRENT, REAL_FEEL_TEMP_TEXT_IN_CURRENT, AIR_QUALITY_TEXT_IN_CURRENT, PRECIPITATION_TEXT_IN_CURRENT
		}

		public enum Hourly {
			CLOCK_TEXT_IN_HOURLY, TEMP_TEXT_IN_HOURLY
		}

		public enum Daily {
			DATE_TEXT_IN_DAILY, TEMP_TEXT_IN_DAILY
		}
	}

	public static class WidgetJsonKey {

		public enum Root {
			successful
		}

		public enum Type {
			ForecastJson, Header, Current, Hourly, Daily, zoneId
		}

		public enum Header {
			address, refreshDateTime
		}

		public enum Current {
			weatherIcon, temp, realFeelTemp, airQuality, precipitation
		}

		public enum Hourly {
			forecasts, clock, temp, weatherIcon
		}

		public enum Daily {
			forecasts, date, minTemp, maxTemp, leftWeatherIcon, rightWeatherIcon, isSingle
		}
	}

	public void removeWidgetUpdateCallback() {
		widgetUpdateCallback = null;
	}

	public WidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback) {
		this.context = context;
		this.widgetUpdateCallback = widgetUpdateCallback;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		clockUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_clock),
				ValueUnits.clock12.name()));

		dateFormat = context.getString(R.string.date_pattern);
		timeFormat = clockUnit == ValueUnits.clock12 ? context.getString(R.string.clock_12_pattern) : context.getString(R.string.clock_24_pattern);
		dateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? context.getString(R.string.datetime_pattern_clock12) :
				context.getString(R.string.datetime_pattern_clock24));
		tempDegree = context.getString(R.string.degree_symbol);
	}

	public void setIntialValues(int appWidgetId, SharedPreferences sharedPreferences) {
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt(WidgetAttributes.APP_WIDGET_ID.name(), appWidgetId);
		editor.putInt(WidgetAttributes.BACKGROUND_ALPHA.name(), 100);
		editor.putString(WidgetAttributes.LOCATION_TYPE.name(), LocationType.CurrentLocation.name());
		editor.putString(WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
				PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_accu_weather),
						true) ? WeatherSourceType.ACCU_WEATHER.name() : WeatherSourceType.OPEN_WEATHER_MAP.name());

		editor.putBoolean(WidgetAttributes.TOP_PRIORITY_KMA.name(), false);
		editor.putLong(WidgetAttributes.UPDATE_INTERVAL.name(), 0);
		editor.putBoolean(WidgetAttributes.DISPLAY_CLOCK.name(), true);
		editor.putBoolean(WidgetAttributes.DISPLAY_LOCAL_CLOCK.name(), false);
		editor.putInt(WidgetAttributes.SELECTED_ADDRESS_DTO_ID.name(), 0);

		editor.putInt(WidgetTextViews.Header.ADDRESS_TEXT_IN_HEADER.name(), context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInHeader));
		editor.putInt(WidgetTextViews.Header.REFRESH_TEXT_IN_HEADER.name(), context.getResources().getDimensionPixelSize(R.dimen.refreshTextSizeInHeader));

		editor.putInt(WidgetTextViews.Current.TEMP_TEXT_IN_CURRENT.name(), context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrent));
		editor.putInt(WidgetTextViews.Current.REAL_FEEL_TEMP_TEXT_IN_CURRENT.name(), context.getResources().getDimensionPixelSize(R.dimen.realFeelTempTextSizeInCurrent));
		editor.putInt(WidgetTextViews.Current.AIR_QUALITY_TEXT_IN_CURRENT.name(), context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrent));
		editor.putInt(WidgetTextViews.Current.PRECIPITATION_TEXT_IN_CURRENT.name(), context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrent));

		editor.putInt(WidgetTextViews.Hourly.CLOCK_TEXT_IN_HOURLY.name(), context.getResources().getDimensionPixelSize(R.dimen.clockTextSizeInHourly));
		editor.putInt(WidgetTextViews.Hourly.TEMP_TEXT_IN_HOURLY.name(), context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInHourly));

		editor.putInt(WidgetTextViews.Daily.DATE_TEXT_IN_DAILY.name(), context.getResources().getDimensionPixelSize(R.dimen.dateTextSizeInDaily));
		editor.putInt(WidgetTextViews.Daily.TEMP_TEXT_IN_DAILY.name(), context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInDaily));

		editor.putInt(WidgetTextViews.Clock.DATE_TEXT_IN_CLOCK.name(),
				context.getResources().getDimensionPixelSize(R.dimen.dateTextSizeInClock));
		editor.putInt(WidgetTextViews.Clock.TIME_TEXT_IN_CLOCK.name(), context.getResources().getDimensionPixelSize(R.dimen.timeTextSizeInClock));

		editor.commit();
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.e(tag, "onSharedPreferenceChanged In WidgetCreator");
		appWidgetId = sharedPreferences.getInt(WidgetAttributes.APP_WIDGET_ID.name(), appWidgetId);
		backgroundAlpha = sharedPreferences.getInt(WidgetAttributes.BACKGROUND_ALPHA.name(), backgroundAlpha);
		locationType = LocationType.valueOf(sharedPreferences.getString(WidgetAttributes.LOCATION_TYPE.name(),
				LocationType.CurrentLocation.name()));
		weatherSourceType = WeatherSourceType.valueOf(sharedPreferences.getString(WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name()));
		kmaTopPriority = sharedPreferences.getBoolean(WidgetAttributes.TOP_PRIORITY_KMA.name(), kmaTopPriority);
		updateInterval = sharedPreferences.getLong(WidgetAttributes.UPDATE_INTERVAL.name(), updateInterval);
		displayClock = sharedPreferences.getBoolean(WidgetAttributes.DISPLAY_CLOCK.name(), displayClock);
		displayLocalClock = sharedPreferences.getBoolean(WidgetAttributes.DISPLAY_LOCAL_CLOCK.name(), displayLocalClock);
		selectedAddressDtoId = sharedPreferences.getInt(WidgetAttributes.SELECTED_ADDRESS_DTO_ID.name(), 0);

		addressInHeaderTextSize = sharedPreferences.getInt(WidgetTextViews.Header.ADDRESS_TEXT_IN_HEADER.name(),
				addressInHeaderTextSize);
		refreshInHeaderTextSize = sharedPreferences.getInt(WidgetTextViews.Header.REFRESH_TEXT_IN_HEADER.name(),
				refreshInHeaderTextSize);

		tempInCurrentTextSize = sharedPreferences.getInt(WidgetTextViews.Current.TEMP_TEXT_IN_CURRENT.name(),
				tempInCurrentTextSize);
		realFeelTempInCurrentTextSize = sharedPreferences.getInt(WidgetTextViews.Current.REAL_FEEL_TEMP_TEXT_IN_CURRENT.name(),
				realFeelTempInCurrentTextSize);
		airQualityInCurrentTextSize = sharedPreferences.getInt(WidgetTextViews.Current.AIR_QUALITY_TEXT_IN_CURRENT.name(),
				airQualityInCurrentTextSize);
		precipitationInCurrentTextSize = sharedPreferences.getInt(WidgetTextViews.Current.PRECIPITATION_TEXT_IN_CURRENT.name(),
				precipitationInCurrentTextSize);

		clockInHourlyTextSize = sharedPreferences.getInt(WidgetTextViews.Hourly.CLOCK_TEXT_IN_HOURLY.name(),
				clockInHourlyTextSize);
		tempInHourlyTextSize = sharedPreferences.getInt(WidgetTextViews.Hourly.TEMP_TEXT_IN_HOURLY.name(),
				tempInHourlyTextSize);

		dateInDailyTextSize = sharedPreferences.getInt(WidgetTextViews.Daily.DATE_TEXT_IN_DAILY.name(),
				dateInDailyTextSize);
		tempInDailyTextSize = sharedPreferences.getInt(WidgetTextViews.Daily.TEMP_TEXT_IN_DAILY.name(),
				tempInDailyTextSize);

		dateInClockTextSize = sharedPreferences.getInt(WidgetTextViews.Clock.DATE_TEXT_IN_CLOCK.name(), dateInClockTextSize);
		timeInClockTextSize = sharedPreferences.getInt(WidgetTextViews.Clock.TIME_TEXT_IN_CLOCK.name(), timeInClockTextSize);

		if (widgetUpdateCallback != null) {
			widgetUpdateCallback.updateWidget();
		}
	}

	private void changeTextSize(RemoteViews remoteViews, int layoutId) {
		if (layoutId == R.layout.widget_current) {
			setHeaderViews(remoteViews, getTempHeaderObj());
			setCurrentConditionsViews(remoteViews, getTempCurrentConditionsObj());
		} else if (layoutId == R.layout.widget_current_hourly) {
			setHeaderViews(remoteViews, getTempHeaderObj());
			setCurrentConditionsViews(remoteViews, getTempCurrentConditionsObj());
			setHourlyForecastViews(remoteViews, getTempHourlyForecastObjs());
		} else if (layoutId == R.layout.widget_current_daily) {
			setHeaderViews(remoteViews, getTempHeaderObj());
			setCurrentConditionsViews(remoteViews, getTempCurrentConditionsObj());
			setDailyForecastViews(remoteViews, getTempDailyForecastObjs());
		} else if (layoutId == R.layout.widget_current_hourly_daily) {
			setHeaderViews(remoteViews, getTempHeaderObj());
			setCurrentConditionsViews(remoteViews, getTempCurrentConditionsObj());
			setHourlyForecastViews(remoteViews, getTempHourlyForecastObjs());
			setDailyForecastViews(remoteViews, getTempDailyForecastObjs());
		}
	}

	public RemoteViews createRemoteViews(boolean needTempData) {
		Log.e(tag, "createRemoteViews");

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {
			changeTextSize(remoteViews, layoutId);
		}
		remoteViews.setViewVisibility(R.id.watch, displayClock ? View.VISIBLE : View.GONE);

		remoteViews.setCharSequence(R.id.date, "setFormat24Hour", dateFormat);
		remoteViews.setCharSequence(R.id.date, "setFormat12Hour", dateFormat);
		remoteViews.setCharSequence(R.id.time, "setFormat24Hour", timeFormat);
		remoteViews.setCharSequence(R.id.time, "setFormat12Hour", timeFormat);

		remoteViews.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, dateInClockTextSize);
		remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, timeInClockTextSize);

		float opacity = backgroundAlpha / 100f;
		int newBackgroundColor = (int) (opacity * 0xFF) << 24 | AppTheme.getColor(context, R.attr.appWidgetBackgroundColor);
		remoteViews.setInt(R.id.root_layout, "setBackgroundColor", newBackgroundColor);
		remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews, appWidgetId));

		setClockTimeZone(remoteViews, ZoneId.systemDefault());

		return remoteViews;
	}

	public PendingIntent getOnClickedPendingIntent(RemoteViews remoteViews, int appWidgetId) {
		Intent intent = new Intent(context, DialogActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Bundle bundle = new Bundle();
		bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		bundle.putParcelable(WidgetCreator.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
		intent.putExtras(bundle);

		return PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void setClockTimeZone(RemoteViews remoteViews, ZoneId zoneId) {
		ZoneId tempZoneId = null;
		if (displayLocalClock) {
			tempZoneId = ZoneId.of(zoneId.getId());
		} else {
			tempZoneId = ZoneId.systemDefault();
		}
		remoteViews.setString(R.id.date, "setTimeZone", tempZoneId.getId());
		remoteViews.setString(R.id.time, "setTimeZone", tempZoneId.getId());
	}

	public void setHeaderViews(RemoteViews remoteViews, HeaderObj headerObj) {
		if (headerObj == null) {
			return;
		}

		remoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressInHeaderTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshInHeaderTextSize);
		remoteViews.setTextViewText(R.id.address, headerObj.getAddress());
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(headerObj.getRefreshDateTime()).format(dateTimeFormatter));
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsObj currentConditionsObj) {
		if (currentConditionsObj == null) {
			return;
		}

		if (currentConditionsObj.getRealFeelTemp() == null) {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.GONE);
		} else {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.VISIBLE);
			remoteViews.setTextViewText(R.id.current_realfeel_temperature,
					context.getString(R.string.real_feel_temperature_simple) + " : " + ValueUnits.convertTemperature(currentConditionsObj.getRealFeelTemp(),
							tempUnit) + tempDegree);
		}
		remoteViews.setTextViewText(R.id.current_airquality, currentConditionsObj.getAirQuality() == null ? context.getString(R.string.not_data)
				: AqicnResponseProcessor.getGradeDescription((int) Double.parseDouble(currentConditionsObj.getAirQuality())));
		remoteViews.setTextViewText(R.id.current_precipitation, currentConditionsObj.getPrecipitation() == null ?
				context.getString(R.string.not_precipitation) : currentConditionsObj.getPrecipitation() + "mm");
		remoteViews.setTextViewText(R.id.current_temperature, ValueUnits.convertTemperature(currentConditionsObj.getTemp(),
				tempUnit) + tempDegree);
		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsObj.getWeatherIcon());

		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_realfeel_temperature, TypedValue.COMPLEX_UNIT_PX, realFeelTempInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_airquality, TypedValue.COMPLEX_UNIT_PX, airQualityInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationInCurrentTextSize);
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, WeatherJsonObj.HourlyForecasts hourlyForecasts) {
		if (hourlyForecasts == null) {
			return;
		}

		remoteViews.removeAllViews(R.id.hourly_forecast_row_1);
		remoteViews.removeAllViews(R.id.hourly_forecast_row_2);
		String clock = null;
		ZonedDateTime zonedDateTime = null;

		List<HourlyForecastObj> hourlyForecastObjList = hourlyForecasts.getHourlyForecastObjs();

		for (int i = 0; i < 10; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_widget);

			zonedDateTime = ZonedDateTime.parse(hourlyForecastObjList.get(i).getClock());
			if (zonedDateTime.getHour() == 0) {
				clock = zonedDateTime.format(DateTimeFormatter.ofPattern(context.getString(R.string.time_pattern_if_hours_0_of_hourly_forecast_in_widget)));
			} else {
				clock = String.valueOf(zonedDateTime.getHour());
			}

			childRemoteViews.setTextViewText(R.id.hourly_clock, clock);
			childRemoteViews.setTextViewText(R.id.hourly_temperature, ValueUnits.convertTemperature(hourlyForecastObjList.get(i).getTemp(),
					tempUnit) + tempDegree);
			childRemoteViews.setImageViewResource(R.id.hourly_weather_icon, hourlyForecastObjList.get(i).getWeatherIcon());

			childRemoteViews.setTextViewTextSize(R.id.hourly_clock, TypedValue.COMPLEX_UNIT_PX, clockInHourlyTextSize);
			childRemoteViews.setTextViewTextSize(R.id.hourly_temperature, TypedValue.COMPLEX_UNIT_PX, tempInHourlyTextSize);

			if (i >= 5) {
				remoteViews.addView(R.id.hourly_forecast_row_2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourly_forecast_row_1, childRemoteViews);
			}
		}
	}

	public void setDailyForecastViews(RemoteViews remoteViews, WeatherJsonObj.DailyForecasts dailyForecasts) {
		if (dailyForecasts == null) {
			return;
		}
		remoteViews.removeAllViews(R.id.daily_forecast_row);
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.date_pattern_of_daily_forecast_in_widget));
		List<DailyForecastObj> dailyForecastObjList = dailyForecasts.getDailyForecastObjs();

		for (int day = 0; day < 4; day++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_forecast_item_in_widget);

			childRemoteViews.setTextViewText(R.id.daily_date, ZonedDateTime.parse(dailyForecastObjList.get(day).getDate()).format(dateFormatter));
			childRemoteViews.setTextViewText(R.id.daily_temperature, ValueUnits.convertTemperature(dailyForecastObjList.get(day).getMinTemp(),
					tempUnit) + tempDegree + " / " + ValueUnits.convertTemperature(dailyForecastObjList.get(day).getMaxTemp(),
					tempUnit) + tempDegree);

			childRemoteViews.setTextViewTextSize(R.id.daily_date, TypedValue.COMPLEX_UNIT_PX, dateInDailyTextSize);
			childRemoteViews.setTextViewTextSize(R.id.daily_temperature, TypedValue.COMPLEX_UNIT_PX, tempInDailyTextSize);

			childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.VISIBLE);
			childRemoteViews.setViewVisibility(R.id.daily_right_weather_icon, View.VISIBLE);

			if (dailyForecastObjList.get(day).isSingle()) {
				childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastObjList.get(day).getLeftWeatherIcon());
				childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.GONE);
			} else {
				childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastObjList.get(day).getLeftWeatherIcon());
				childRemoteViews.setImageViewResource(R.id.daily_right_weather_icon, dailyForecastObjList.get(day).getRightWeatherIcon());
			}

			remoteViews.addView(R.id.daily_forecast_row, childRemoteViews);
		}
	}

	public HeaderObj getTempHeaderObj() {
		HeaderObj tempHeaderObj = new HeaderObj();
		tempHeaderObj.setAddress(context.getString(R.string.address_name));
		tempHeaderObj.setRefreshDateTime(ZonedDateTime.now().toString());
		return tempHeaderObj;
	}

	public CurrentConditionsObj getTempCurrentConditionsObj() {
		CurrentConditionsObj currentConditionsObj = new CurrentConditionsObj(true);
		currentConditionsObj.setWeatherIcon(R.drawable.day_clear);

		String temp = "20";
		currentConditionsObj.setTemp(temp);
		currentConditionsObj.setRealFeelTemp(temp);
		currentConditionsObj.setAirQuality("10");
		currentConditionsObj.setPrecipitation(null);
		currentConditionsObj.setZoneId(ZoneId.systemDefault().getId());
		return currentConditionsObj;
	}

	public WeatherJsonObj.HourlyForecasts getTempHourlyForecastObjs() {
		WeatherJsonObj.HourlyForecasts hourlyForecasts = new WeatherJsonObj.HourlyForecasts();
		List<HourlyForecastObj> tempHourlyForecastObjs = new ArrayList<>();
		hourlyForecasts.setHourlyForecastObjs(tempHourlyForecastObjs);
		ZonedDateTime now = ZonedDateTime.now();
		hourlyForecasts.setZoneId(now.getZone().getId());

		String temp = "20";

		for (int i = 0; i < 10; i++) {
			HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);
			hourlyForecastObj.setWeatherIcon(R.drawable.day_clear);
			hourlyForecastObj.setClock(now.toString());
			hourlyForecastObj.setTemp(temp);
			tempHourlyForecastObjs.add(hourlyForecastObj);

			now = now.plusHours(1);
		}
		return hourlyForecasts;
	}

	public WeatherJsonObj.DailyForecasts getTempDailyForecastObjs() {
		WeatherJsonObj.DailyForecasts dailyForecasts = new WeatherJsonObj.DailyForecasts();
		List<DailyForecastObj> tempDailyForecastObjs = new ArrayList<>();
		dailyForecasts.setDailyForecastObjs(tempDailyForecastObjs);
		ZonedDateTime now = ZonedDateTime.now();
		dailyForecasts.setZoneId(now.getZone().getId());

		String temp = "20";

		for (int i = 0; i < 5; i++) {
			DailyForecastObj dailyForecastObj = new DailyForecastObj(true, false);
			dailyForecastObj.setLeftWeatherIcon(R.drawable.day_clear);
			dailyForecastObj.setRightWeatherIcon(R.drawable.night_clear);
			dailyForecastObj.setDate(now.toString());
			dailyForecastObj.setMinTemp(temp);
			dailyForecastObj.setMaxTemp(temp);
			tempDailyForecastObjs.add(dailyForecastObj);

			now = now.plusDays(1);
		}
		return dailyForecasts;
	}

	public static WeatherJsonObj getSavedWeatherData(int appWidgetId, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);
		WeatherJsonObj weatherJsonObj = new Gson().fromJson(sharedPreferences.getString(WidgetJsonKey.Type.ForecastJson.name(), ""),
				WeatherJsonObj.class);

		return weatherJsonObj;
	}


	public static void saveWeatherData(int appWidgetId, Context context, @Nullable HeaderObj headerObj,
	                                   @Nullable CurrentConditionsObj currentConditionsObj,
	                                   @Nullable WeatherJsonObj.HourlyForecasts hourlyForecastObjs,
	                                   @Nullable WeatherJsonObj.DailyForecasts dailyForecastObjs) {
		JsonObject weatherDataJsonObject = new JsonObject();

		if (headerObj != null) {
			JsonObject rootObject = new JsonObject();
			rootObject.addProperty(WidgetJsonKey.Header.address.name(), headerObj.getAddress());
			rootObject.addProperty(WidgetJsonKey.Header.refreshDateTime.name(), headerObj.getRefreshDateTime());

			weatherDataJsonObject.add(WidgetJsonKey.Type.Header.name(), rootObject);
		}
		if (currentConditionsObj != null) {
			if (currentConditionsObj.isSuccessful()) {
				JsonObject rootObject = new JsonObject();
				rootObject.addProperty(WidgetJsonKey.Current.weatherIcon.name(), currentConditionsObj.getWeatherIcon());
				rootObject.addProperty(WidgetJsonKey.Current.temp.name(), currentConditionsObj.getTemp());
				rootObject.addProperty(WidgetJsonKey.Current.realFeelTemp.name(), currentConditionsObj.getRealFeelTemp());
				rootObject.addProperty(WidgetJsonKey.Current.airQuality.name(), currentConditionsObj.getAirQuality());
				rootObject.addProperty(WidgetJsonKey.Current.precipitation.name(), currentConditionsObj.getPrecipitation());
				rootObject.addProperty(WidgetJsonKey.Type.zoneId.name(), currentConditionsObj.getZoneId());

				weatherDataJsonObject.add(WidgetJsonKey.Type.Current.name(), rootObject);
			}
		}
		if (hourlyForecastObjs != null) {
			JsonArray forecasts = new JsonArray();

			for (HourlyForecastObj hourlyForecastObj : hourlyForecastObjs.getHourlyForecastObjs()) {
				JsonObject forecastObject = new JsonObject();
				forecastObject.addProperty(WidgetJsonKey.Hourly.clock.name(), hourlyForecastObj.getClock());
				forecastObject.addProperty(WidgetJsonKey.Hourly.weatherIcon.name(), hourlyForecastObj.getWeatherIcon());
				forecastObject.addProperty(WidgetJsonKey.Hourly.temp.name(), hourlyForecastObj.getTemp());

				forecasts.add(forecastObject);
			}

			if (!forecasts.isEmpty()) {
				JsonObject rootObject = new JsonObject();
				rootObject.add(WidgetJsonKey.Hourly.forecasts.name(), forecasts);
				rootObject.addProperty(WidgetJsonKey.Type.zoneId.name(), hourlyForecastObjs.getZoneId());

				weatherDataJsonObject.add(WidgetJsonKey.Type.Hourly.name(), rootObject);
			}
		}
		if (dailyForecastObjs != null) {
			JsonArray forecasts = new JsonArray();

			for (DailyForecastObj dailyForecastObj : dailyForecastObjs.getDailyForecastObjs()) {
				JsonObject forecastObject = new JsonObject();
				forecastObject.addProperty(WidgetJsonKey.Daily.date.name(), dailyForecastObj.getDate());
				forecastObject.addProperty(WidgetJsonKey.Daily.isSingle.name(), dailyForecastObj.isSingle());
				forecastObject.addProperty(WidgetJsonKey.Daily.leftWeatherIcon.name(), dailyForecastObj.getLeftWeatherIcon());
				forecastObject.addProperty(WidgetJsonKey.Daily.rightWeatherIcon.name(), dailyForecastObj.getRightWeatherIcon());
				forecastObject.addProperty(WidgetJsonKey.Daily.minTemp.name(), dailyForecastObj.getMinTemp());
				forecastObject.addProperty(WidgetJsonKey.Daily.maxTemp.name(), dailyForecastObj.getMaxTemp());

				forecasts.add(forecastObject);
			}

			if (!forecasts.isEmpty()) {
				JsonObject rootObject = new JsonObject();
				rootObject.add(WidgetJsonKey.Daily.forecasts.name(), forecasts);
				rootObject.addProperty(WidgetJsonKey.Type.zoneId.name(), dailyForecastObjs.getZoneId());

				weatherDataJsonObject.add(WidgetJsonKey.Type.Daily.name(), rootObject);
			}
		}

		if (weatherDataJsonObject.size() <= 1) {
			weatherDataJsonObject.addProperty(WidgetJsonKey.Root.successful.name(), false);
		} else {
			weatherDataJsonObject.addProperty(WidgetJsonKey.Root.successful.name(), true);
		}

		SharedPreferences.Editor editor = context.getSharedPreferences(getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit();
		editor.putString(WidgetJsonKey.Type.ForecastJson.name(), weatherDataJsonObject.toString()).apply();
		Log.e(tag, "new saved json : " + weatherDataJsonObject.toString());
	}

	public interface WidgetUpdateCallback {
		void updateWidget();
	}
}