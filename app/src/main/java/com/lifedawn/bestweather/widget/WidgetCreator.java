package com.lifedawn.bestweather.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.theme.AppTheme;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WidgetCreator implements SharedPreferences.OnSharedPreferenceChangeListener {
	private String tag = "WidgetCreator";

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
	private int tempInCurrentTextSize;
	private int realFeelTempInCurrentTextSize;
	private int airQualityInCurrentTextSize;
	private int precipitationInCurrentTextSize;
	private int clockInHourlyTextSize;
	private int tempInHourlyTextSize;
	private int dateInDailyTextSize;
	private int tempInDailyTextSize;

	private Context context;
	private WidgetUpdateCallback widgetUpdateCallback;

	private String dateFormat;
	private String timeFormat;

	public static String getSharedPreferenceName(int appWidgetId) {
		return WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId;
	}

	public enum WidgetAttributes {
		WIDGET_ATTRIBUTES_ID, APP_WIDGET_ID, BACKGROUND_ALPHA, LOCATION_TYPE, WEATHER_SOURCE_TYPE, TOP_PRIORITY_KMA,
		UPDATE_INTERVAL, DISPLAY_CLOCK, DISPLAY_LOCAL_CLOCK, SELECTED_ADDRESS_DTO_ID, WIDGET_CLASS, REMOTE_VIEWS;
	}

	public static class WidgetTextViews {
		public enum Header {
			ADDRESS_TEXT_IN_HEADER, REFRESH_TEXT_IN_HEADER;
		}

		public enum Current {
			TEMP_TEXT_IN_CURRENT, REAL_FEEL_TEMP_TEXT_IN_CURRENT, AIR_QUALITY_TEXT_IN_CURRENT, PRECIPITATION_TEXT_IN_CURRENT;
		}

		public enum Hourly {
			CLOCK_TEXT_IN_HOURLY, TEMP_TEXT_IN_HOURLY;
		}

		public enum Daily {
			DATE_TEXT_IN_DAILY, TEMP_TEXT_IN_DAILY;
		}
	}

	public WidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback) {
		this.context = context;
		this.widgetUpdateCallback = widgetUpdateCallback;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits clockUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_clock),
				ValueUnits.clock12.name()));

		dateFormat = context.getString(R.string.date_pattern);
		timeFormat = clockUnit == ValueUnits.clock12 ? context.getString(R.string.clock_12_pattern) : context.getString(R.string.clock_24_pattern);
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

		editor.commit();
	}


	public void init(SharedPreferences sharedPreferences) {
		/*
		appWidgetId = sharedPreferences.getInt(WidgetAttributes.APP_WIDGET_ID.name(), appWidgetId);
		backgroundAlpha = sharedPreferences.getInt(WidgetAttributes.BACKGROUND_ALPHA.name(), backgroundAlpha);
		locationType = LocationType.valueOf(sharedPreferences.getString(WidgetAttributes.LOCATION_TYPE.name(),
				locationType.name()));
		weatherSourceType = WeatherSourceType.valueOf(sharedPreferences.getString(WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
				weatherSourceType.name()));
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

		 */
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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

		widgetUpdateCallback.updateWidget();
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

		float opacity = backgroundAlpha / 100f;
		int newBackgroundColor = (int) (opacity * 0xFF) << 24 | AppTheme.getColor(context, R.attr.appWidgetBackgroundColor);
		remoteViews.setInt(R.id.root_layout, "setBackgroundColor", newBackgroundColor);

		setClockTimeZone(remoteViews, ZoneId.systemDefault());
		Log.e(tag, "background opacity : " + opacity);
		return remoteViews;
	}

	public void setClockTimeZone(RemoteViews remoteViews, ZoneId zoneId) {
		Log.e(tag, "zoneid : " + zoneId.getId());
		remoteViews.setString(R.id.date, "setTimeZone", zoneId.getId());
		remoteViews.setString(R.id.time, "setTimeZone", zoneId.getId());
	}

	public void setHeaderViews(RemoteViews remoteViews, RootAppWidget.HeaderObj headerObj) {
		remoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressInHeaderTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshInHeaderTextSize);
		remoteViews.setTextViewText(R.id.address, headerObj.address);
		remoteViews.setTextViewText(R.id.refresh, headerObj.refreshDateTime);
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, RootAppWidget.CurrentConditionsObj currentConditionsObj) {
		if (currentConditionsObj.realFeelTemp == null) {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.GONE);
		} else {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.VISIBLE);
			remoteViews.setTextViewText(R.id.current_realfeel_temperature,
					context.getString(R.string.real_feel_temperature_simple) + " : " + currentConditionsObj.realFeelTemp);
		}
		remoteViews.setTextViewText(R.id.current_airquality, currentConditionsObj.airQuality);
		remoteViews.setTextViewText(R.id.current_precipitation, currentConditionsObj.precipitation);
		remoteViews.setTextViewText(R.id.current_temperature, currentConditionsObj.temp);
		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsObj.weatherIcon);

		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_realfeel_temperature, TypedValue.COMPLEX_UNIT_PX, realFeelTempInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_airquality, TypedValue.COMPLEX_UNIT_PX, airQualityInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationInCurrentTextSize);
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, List<RootAppWidget.HourlyForecastObj> hourlyForecastObjList) {
		remoteViews.removeAllViews(R.id.hourly_forecast_row_1);
		remoteViews.removeAllViews(R.id.hourly_forecast_row_2);

		for (int i = 0; i < 10; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_widget);
			childRemoteViews.setTextViewText(R.id.hourly_clock, hourlyForecastObjList.get(i).clock);
			childRemoteViews.setTextViewText(R.id.hourly_temperature, hourlyForecastObjList.get(i).temp);
			childRemoteViews.setImageViewResource(R.id.hourly_weather_icon, hourlyForecastObjList.get(i).weatherIcon);

			childRemoteViews.setTextViewTextSize(R.id.hourly_clock, TypedValue.COMPLEX_UNIT_PX, clockInHourlyTextSize);
			childRemoteViews.setTextViewTextSize(R.id.hourly_temperature, TypedValue.COMPLEX_UNIT_PX, tempInHourlyTextSize);

			if (i >= 5) {
				remoteViews.addView(R.id.hourly_forecast_row_2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourly_forecast_row_1, childRemoteViews);
			}
		}
	}

	public void setDailyForecastViews(RemoteViews remoteViews, List<RootAppWidget.DailyForecastObj> dailyForecastObjList) {
		remoteViews.removeAllViews(R.id.daily_forecast_row);

		for (int day = 0; day < 4; day++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_forecast_item_in_widget);
			childRemoteViews.setTextViewText(R.id.daily_date, dailyForecastObjList.get(day).date);
			childRemoteViews.setTextViewText(R.id.daily_temperature, dailyForecastObjList.get(day).temp);
			childRemoteViews.setTextViewTextSize(R.id.daily_date, TypedValue.COMPLEX_UNIT_PX, dateInDailyTextSize);
			childRemoteViews.setTextViewTextSize(R.id.daily_temperature, TypedValue.COMPLEX_UNIT_PX, tempInDailyTextSize);

			childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.VISIBLE);
			childRemoteViews.setViewVisibility(R.id.daily_right_weather_icon, View.VISIBLE);

			if (dailyForecastObjList.get(day).isSingle) {
				childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastObjList.get(day).weatherIcon);
				childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.GONE);
			} else {
				childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastObjList.get(day).dayWeatherIcon);
				childRemoteViews.setImageViewResource(R.id.daily_right_weather_icon, dailyForecastObjList.get(day).nightWeatherIcon);
			}

			remoteViews.addView(R.id.daily_forecast_row, childRemoteViews);
		}
	}

	public RootAppWidget.HeaderObj getTempHeaderObj() {
		RootAppWidget.HeaderObj tempHeaderObj = new RootAppWidget.HeaderObj(true);
		tempHeaderObj.address = context.getString(R.string.address_name);
		tempHeaderObj.refreshDateTime = context.getString(R.string.updated_time);
		return tempHeaderObj;
	}

	public RootAppWidget.CurrentConditionsObj getTempCurrentConditionsObj() {
		RootAppWidget.CurrentConditionsObj currentConditionsObj = new RootAppWidget.CurrentConditionsObj(true);
		currentConditionsObj.weatherIcon = R.drawable.day_clear;
		currentConditionsObj.temp = "20º";
		currentConditionsObj.realFeelTemp = "20º";
		currentConditionsObj.airQuality = context.getString(R.string.good);
		currentConditionsObj.precipitation = context.getString(R.string.not_precipitation);
		return currentConditionsObj;
	}

	public List<RootAppWidget.HourlyForecastObj> getTempHourlyForecastObjs() {
		List<RootAppWidget.HourlyForecastObj> tempHourlyForecastObjs = new ArrayList<>();
		ZonedDateTime now = ZonedDateTime.now();

		for (int i = 0; i < 10; i++) {
			RootAppWidget.HourlyForecastObj hourlyForecastObj = new RootAppWidget.HourlyForecastObj(true);
			hourlyForecastObj.weatherIcon = R.drawable.day_clear;
			if (now.getHour() == 0) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(context.getString(R.string.time_pattern_if_hours_0_of_hourly_forecast_in_widget));
				hourlyForecastObj.clock = now.format(formatter);
			} else {
				hourlyForecastObj.clock = String.valueOf(now.getHour());
			}
			hourlyForecastObj.temp = "20º";
			tempHourlyForecastObjs.add(hourlyForecastObj);

			now = now.plusHours(1);
		}
		return tempHourlyForecastObjs;
	}

	public List<RootAppWidget.DailyForecastObj> getTempDailyForecastObjs() {
		List<RootAppWidget.DailyForecastObj> tempDailyForecastObjs = new ArrayList<>();
		ZonedDateTime now = ZonedDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(context.getString(R.string.date_pattern_of_daily_forecast_in_widget));
		String temperature = "20º / 20º";

		for (int i = 0; i < 5; i++) {
			RootAppWidget.DailyForecastObj dailyForecastObj = new RootAppWidget.DailyForecastObj(true, false);
			dailyForecastObj.dayWeatherIcon = R.drawable.day_clear;
			dailyForecastObj.nightWeatherIcon = R.drawable.night_clear;
			dailyForecastObj.date = now.format(formatter);
			dailyForecastObj.temp = temperature;
			tempDailyForecastObjs.add(dailyForecastObj);

			now = now.plusDays(1);
		}
		return tempDailyForecastObjs;
	}

	public interface WidgetUpdateCallback {
		void updateWidget();
	}
}
