package com.lifedawn.bestweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.JsonDataSaver;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.forremoteviews.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.forremoteviews.dto.DailyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.HeaderObj;
import com.lifedawn.bestweather.forremoteviews.dto.HourlyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WidgetCreator {
	private final int appWidgetId;
	private final ValueUnits tempUnit;
	private final String tempDegree;

	private final String dateFormat;
	private final String timeFormat;
	private final DateTimeFormatter dateTimeFormatter;
	private final ValueUnits clockUnit;
	private final Context context;


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

	private WidgetUpdateCallback widgetUpdateCallback;


	private JsonDataSaver jsonDataSaver = new JsonDataSaver();

	public static String getSharedPreferenceName(int appWidgetId) {
		return WidgetNotiConstants.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId;
	}

	public void removeWidgetUpdateCallback() {
		widgetUpdateCallback = null;
	}

	public WidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		this.context = context;
		this.widgetUpdateCallback = widgetUpdateCallback;
		this.appWidgetId = appWidgetId;

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


	public void loadDefaultPreferences() {
		backgroundAlpha = 100;
		locationType = LocationType.CurrentLocation;
		weatherSourceType =
				PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_accu_weather), false)
						? WeatherSourceType.ACCU_WEATHER : WeatherSourceType.OPEN_WEATHER_MAP;
		kmaTopPriority = false;
		updateInterval = 0L;
		displayClock = true;
		displayLocalClock = false;
		selectedAddressDtoId = 0;

		addressInHeaderTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInHeader);
		refreshInHeaderTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshTextSizeInHeader);

		tempInCurrentTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrent);
		realFeelTempInCurrentTextSize = context.getResources().getDimensionPixelSize(R.dimen.realFeelTempTextSizeInCurrent);
		airQualityInCurrentTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrent);
		precipitationInCurrentTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrent);

		dateInClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTextSizeInClock);
		timeInClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.timeTextSizeInClock);
	}

	public void loadSavedPreferences() {
		SharedPreferences sharedPreferences = context.getSharedPreferences(getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE);

		backgroundAlpha = sharedPreferences.getInt(WidgetNotiConstants.WidgetAttributes.BACKGROUND_ALPHA.name(), backgroundAlpha);
		locationType = LocationType.valueOf(sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(),
				LocationType.CurrentLocation.name()));
		weatherSourceType = WeatherSourceType.valueOf(sharedPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
				WeatherSourceType.OPEN_WEATHER_MAP.name()));
		kmaTopPriority = sharedPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), kmaTopPriority);
		updateInterval = sharedPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), updateInterval);
		displayClock = sharedPreferences.getBoolean(WidgetNotiConstants.WidgetAttributes.DISPLAY_CLOCK.name(), displayClock);
		displayLocalClock = sharedPreferences.getBoolean(WidgetNotiConstants.WidgetAttributes.DISPLAY_LOCAL_CLOCK.name(), displayLocalClock);
		selectedAddressDtoId = sharedPreferences.getInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0);

		addressInHeaderTextSize = sharedPreferences.getInt(WidgetNotiConstants.WidgetTextViews.Header.ADDRESS_TEXT_IN_HEADER.name(),
				addressInHeaderTextSize);
		refreshInHeaderTextSize = sharedPreferences.getInt(WidgetNotiConstants.WidgetTextViews.Header.REFRESH_TEXT_IN_HEADER.name(),
				refreshInHeaderTextSize);

		tempInCurrentTextSize = sharedPreferences.getInt(WidgetNotiConstants.WidgetTextViews.Current.TEMP_TEXT_IN_CURRENT.name(),
				tempInCurrentTextSize);
		realFeelTempInCurrentTextSize = sharedPreferences.getInt(WidgetNotiConstants.WidgetTextViews.Current.REAL_FEEL_TEMP_TEXT_IN_CURRENT.name(),
				realFeelTempInCurrentTextSize);
		airQualityInCurrentTextSize = sharedPreferences.getInt(WidgetNotiConstants.WidgetTextViews.Current.AIR_QUALITY_TEXT_IN_CURRENT.name(),
				airQualityInCurrentTextSize);
		precipitationInCurrentTextSize = sharedPreferences.getInt(WidgetNotiConstants.WidgetTextViews.Current.PRECIPITATION_TEXT_IN_CURRENT.name(),
				precipitationInCurrentTextSize);

		dateInClockTextSize = sharedPreferences.getInt(WidgetNotiConstants.WidgetTextViews.Clock.DATE_TEXT_IN_CLOCK.name(), dateInClockTextSize);
		timeInClockTextSize = sharedPreferences.getInt(WidgetNotiConstants.WidgetTextViews.Clock.TIME_TEXT_IN_CLOCK.name(), timeInClockTextSize);
	}

	public void saveFinalWidgetPreferences() {
		SharedPreferences.Editor editor =
				context.getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit();

		editor.putInt(WidgetNotiConstants.WidgetAttributes.APP_WIDGET_ID.name(), appWidgetId);
		editor.putInt(WidgetNotiConstants.WidgetAttributes.BACKGROUND_ALPHA.name(), backgroundAlpha);
		editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), locationType.name());
		editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), weatherSourceType.name());

		editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), kmaTopPriority);
		editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), updateInterval);
		editor.putBoolean(WidgetNotiConstants.WidgetAttributes.DISPLAY_CLOCK.name(), displayClock);
		editor.putBoolean(WidgetNotiConstants.WidgetAttributes.DISPLAY_LOCAL_CLOCK.name(), displayLocalClock);
		editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), selectedAddressDtoId);

		editor.putInt(WidgetNotiConstants.WidgetTextViews.Header.ADDRESS_TEXT_IN_HEADER.name(),
				addressInHeaderTextSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Header.REFRESH_TEXT_IN_HEADER.name(), refreshInHeaderTextSize);

		editor.putInt(WidgetNotiConstants.WidgetTextViews.Current.TEMP_TEXT_IN_CURRENT.name(), tempInCurrentTextSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Current.REAL_FEEL_TEMP_TEXT_IN_CURRENT.name(), realFeelTempInCurrentTextSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Current.AIR_QUALITY_TEXT_IN_CURRENT.name(), airQualityInCurrentTextSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Current.PRECIPITATION_TEXT_IN_CURRENT.name(), precipitationInCurrentTextSize);

		editor.putInt(WidgetNotiConstants.WidgetTextViews.Clock.DATE_TEXT_IN_CLOCK.name(), dateInClockTextSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Clock.TIME_TEXT_IN_CLOCK.name(), timeInClockTextSize);

		editor.commit();
	}


	private void changeTextSize(RemoteViews remoteViews, int layoutId) {
		if (layoutId == R.layout.widget_current) {
			setHeaderViews(remoteViews, getTempHeaderObj());
			setCurrentConditionsViews(remoteViews, jsonDataSaver.getTempCurrentConditionsObj());
		} else if (layoutId == R.layout.widget_current_hourly) {
			setHeaderViews(remoteViews, getTempHeaderObj());
			setCurrentConditionsViews(remoteViews, jsonDataSaver.getTempCurrentConditionsObj());
			setHourlyForecastViews(remoteViews, jsonDataSaver.getTempHourlyForecastObjs(12));
		} else if (layoutId == R.layout.widget_current_daily) {
			setHeaderViews(remoteViews, getTempHeaderObj());
			setCurrentConditionsViews(remoteViews, jsonDataSaver.getTempCurrentConditionsObj());
			setDailyForecastViews(remoteViews, jsonDataSaver.getTempDailyForecastObjs(5));
		} else if (layoutId == R.layout.widget_current_hourly_daily) {
			setHeaderViews(remoteViews, getTempHeaderObj());
			setCurrentConditionsViews(remoteViews, jsonDataSaver.getTempCurrentConditionsObj());
			setHourlyForecastViews(remoteViews, jsonDataSaver.getTempHourlyForecastObjs(12));
			setDailyForecastViews(remoteViews, jsonDataSaver.getTempDailyForecastObjs(5));
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
		bundle.putParcelable(WidgetNotiConstants.WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
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
		remoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressInHeaderTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshInHeaderTextSize);
		remoteViews.setTextViewText(R.id.address, headerObj.getAddress());
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(headerObj.getRefreshDateTime()).format(dateTimeFormatter));
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsObj currentConditionsObj) {

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
		remoteViews.removeAllViews(R.id.hourly_forecast_row_1);
		remoteViews.removeAllViews(R.id.hourly_forecast_row_2);
		String clock = null;
		ZonedDateTime zonedDateTime = null;

		List<HourlyForecastObj> hourlyForecastObjList = hourlyForecasts.getHourlyForecastObjs();

		for (int i = 0; i < 12; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_linear);

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

			if (i >= 6) {
				remoteViews.addView(R.id.hourly_forecast_row_2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourly_forecast_row_1, childRemoteViews);
			}
		}
	}

	public void setDailyForecastViews(RemoteViews remoteViews, WeatherJsonObj.DailyForecasts dailyForecasts) {
		remoteViews.removeAllViews(R.id.daily_forecast_row);
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.date_pattern_of_daily_forecast_in_widget));
		List<DailyForecastObj> dailyForecastObjList = dailyForecasts.getDailyForecastObjs();

		for (int day = 0; day < 4; day++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_forecast_item_in_linear);

			childRemoteViews.setTextViewText(R.id.daily_date, ZonedDateTime.parse(dailyForecastObjList.get(day).getDate()).format(dateFormatter));
			childRemoteViews.setTextViewText(R.id.daily_temperature, ValueUnits.convertTemperature(dailyForecastObjList.get(day).getMinTemp(),
					tempUnit) + tempDegree + " / " + ValueUnits.convertTemperature(dailyForecastObjList.get(day).getMaxTemp(),
					tempUnit) + tempDegree);

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

	public int getBackgroundAlpha() {
		return backgroundAlpha;
	}

	public void setBackgroundAlpha(int backgroundAlpha) {
		this.backgroundAlpha = backgroundAlpha;
	}

	public LocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}

	public WeatherSourceType getWeatherSourceType() {
		return weatherSourceType;
	}

	public void setWeatherSourceType(WeatherSourceType weatherSourceType) {
		this.weatherSourceType = weatherSourceType;
	}

	public boolean isKmaTopPriority() {
		return kmaTopPriority;
	}

	public void setKmaTopPriority(boolean kmaTopPriority) {
		this.kmaTopPriority = kmaTopPriority;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}

	public boolean isDisplayClock() {
		return displayClock;
	}

	public void setDisplayClock(boolean displayClock) {
		this.displayClock = displayClock;
	}

	public boolean isDisplayLocalClock() {
		return displayLocalClock;
	}

	public void setDisplayLocalClock(boolean displayLocalClock) {
		this.displayLocalClock = displayLocalClock;
	}

	public int getSelectedAddressDtoId() {
		return selectedAddressDtoId;
	}

	public void setSelectedAddressDtoId(int selectedAddressDtoId) {
		this.selectedAddressDtoId = selectedAddressDtoId;
	}

	public int getAddressInHeaderTextSize() {
		return addressInHeaderTextSize;
	}

	public void setAddressInHeaderTextSize(int addressInHeaderTextSize) {
		this.addressInHeaderTextSize = addressInHeaderTextSize;
	}

	public int getRefreshInHeaderTextSize() {
		return refreshInHeaderTextSize;
	}

	public void setRefreshInHeaderTextSize(int refreshInHeaderTextSize) {
		this.refreshInHeaderTextSize = refreshInHeaderTextSize;
	}

	public int getDateInClockTextSize() {
		return dateInClockTextSize;
	}

	public void setDateInClockTextSize(int dateInClockTextSize) {
		this.dateInClockTextSize = dateInClockTextSize;
	}

	public int getTimeInClockTextSize() {
		return timeInClockTextSize;
	}

	public void setTimeInClockTextSize(int timeInClockTextSize) {
		this.timeInClockTextSize = timeInClockTextSize;
	}

	public int getTempInCurrentTextSize() {
		return tempInCurrentTextSize;
	}

	public void setTempInCurrentTextSize(int tempInCurrentTextSize) {
		this.tempInCurrentTextSize = tempInCurrentTextSize;
	}

	public int getRealFeelTempInCurrentTextSize() {
		return realFeelTempInCurrentTextSize;
	}

	public void setRealFeelTempInCurrentTextSize(int realFeelTempInCurrentTextSize) {
		this.realFeelTempInCurrentTextSize = realFeelTempInCurrentTextSize;
	}

	public int getAirQualityInCurrentTextSize() {
		return airQualityInCurrentTextSize;
	}

	public void setAirQualityInCurrentTextSize(int airQualityInCurrentTextSize) {
		this.airQualityInCurrentTextSize = airQualityInCurrentTextSize;
	}

	public int getPrecipitationInCurrentTextSize() {
		return precipitationInCurrentTextSize;
	}

	public void setPrecipitationInCurrentTextSize(int precipitationInCurrentTextSize) {
		this.precipitationInCurrentTextSize = precipitationInCurrentTextSize;
	}

	public interface WidgetUpdateCallback {
		void updatePreview();
	}
}
