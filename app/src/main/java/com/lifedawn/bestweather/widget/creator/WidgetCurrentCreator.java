package com.lifedawn.bestweather.widget.creator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.widget.DialogActivity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class WidgetCurrentCreator extends AbstractWidgetCreator {
	private int addressInHeaderTextSize;
	private int refreshInHeaderTextSize;

	private int dateInClockTextSize;
	private int timeInClockTextSize;

	private int tempInCurrentTextSize;
	private int realFeelTempInCurrentTextSize;
	private int airQualityInCurrentTextSize;
	private int precipitationInCurrentTextSize;

	public WidgetCurrentCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {
			setTempHeaderViews(remoteViews);
			setTempAirQualityViews(remoteViews);
			setTempCurrentConditionsViews(remoteViews);
		}
		remoteViews.setViewVisibility(R.id.watch, widgetDto.isDisplayClock() ? View.VISIBLE : View.GONE);

		remoteViews.setCharSequence(R.id.date, "setFormat24Hour", dateFormat);
		remoteViews.setCharSequence(R.id.date, "setFormat12Hour", dateFormat);
		remoteViews.setCharSequence(R.id.time, "setFormat24Hour", timeFormat);
		remoteViews.setCharSequence(R.id.time, "setFormat12Hour", timeFormat);

		remoteViews.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, dateInClockTextSize);
		remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, timeInClockTextSize);

		setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());
		remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews));

		setClockTimeZone(remoteViews, ZoneId.systemDefault());
		return remoteViews;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressInHeaderTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInHeader) + extraSize;
		refreshInHeaderTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshTextSizeInHeader) + extraSize;

		tempInCurrentTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrent) + extraSize;
		realFeelTempInCurrentTextSize = context.getResources().getDimensionPixelSize(R.dimen.realFeelTempTextSizeInCurrent) + extraSize;
		airQualityInCurrentTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrent) + extraSize;
		precipitationInCurrentTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrent) + extraSize;

		dateInClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTextSizeInClock) + extraSize;
		timeInClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.timeTextSizeInClock) + extraSize;
	}

	public void setClockTimeZone(RemoteViews remoteViews, ZoneId zoneId) {
		ZoneId tempZoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(zoneId.getId()) : ZoneId.systemDefault();

		remoteViews.setString(R.id.date, "setTimeZone", tempZoneId.getId());
		remoteViews.setString(R.id.time, "setTimeZone", tempZoneId.getId());
	}

	public void setHeaderViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime) {
		remoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressInHeaderTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshInHeaderTextSize);
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(dateTimeFormatter));
	}

	public void setTempHeaderViews(RemoteViews remoteViews) {
		remoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressInHeaderTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshInHeaderTextSize);
		remoteViews.setTextViewText(R.id.address, context.getString(R.string.address_name));
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.now().format(dateTimeFormatter));
	}

	public void setAirQualityViews(RemoteViews remoteViews, AirQualityObj airQualityObj) {
		remoteViews.setTextViewText(R.id.current_airquality,
				context.getString(R.string.air_quality) + " " + (airQualityObj.getAqi() == null ? context.getString(R.string.not_data)
						: airQualityObj.getAqi()));
		remoteViews.setTextViewTextSize(R.id.current_airquality, TypedValue.COMPLEX_UNIT_PX, airQualityInCurrentTextSize);
	}

	public void setTempAirQualityViews(RemoteViews remoteViews) {
		remoteViews.setTextViewText(R.id.current_airquality, context.getString(R.string.air_quality) + " " + context.getString(R.string.good));
		remoteViews.setTextViewTextSize(R.id.current_airquality, TypedValue.COMPLEX_UNIT_PX, airQualityInCurrentTextSize);
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsObj currentConditionsObj) {
		if (currentConditionsObj.getRealFeelTemp() == null) {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.GONE);
		} else {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.VISIBLE);
			remoteViews.setTextViewText(R.id.current_realfeel_temperature,
					context.getString(R.string.real_feel_temperature_simple) + " " + currentConditionsObj.getRealFeelTemp());
		}

		String precipitationType = currentConditionsObj.getPrecipitationType();
		String precipitationVolume = currentConditionsObj.getPrecipitationVolume();

		remoteViews.setTextViewText(R.id.current_precipitation,
				context.getString(R.string.precipitation) + " " + precipitationType + " " + precipitationVolume);
		remoteViews.setTextViewText(R.id.current_temperature, currentConditionsObj.getTemp());
		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsObj.getWeatherIcon());

		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_realfeel_temperature, TypedValue.COMPLEX_UNIT_PX, realFeelTempInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationInCurrentTextSize);
	}

	public void setTempCurrentConditionsViews(RemoteViews remoteViews) {
		remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.VISIBLE);
		remoteViews.setTextViewText(R.id.current_realfeel_temperature,
				context.getString(R.string.real_feel_temperature_simple) + " " + "20º");

		remoteViews.setTextViewText(R.id.current_precipitation, context.getString(R.string.precipitation) + " "
				+ context.getString(R.string.not_precipitation));
		remoteViews.setTextViewText(R.id.current_temperature, "20º");
		remoteViews.setImageViewResource(R.id.current_weather_icon, R.drawable.day_clear);

		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_realfeel_temperature, TypedValue.COMPLEX_UNIT_PX, realFeelTempInCurrentTextSize);
		remoteViews.setTextViewTextSize(R.id.current_precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationInCurrentTextSize);
	}

	/*
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

	 */

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	public static class AirQualityObj {
		private String aqi;

		public String getAqi() {
			return aqi;
		}

		public void setAqi(String aqi) {
			this.aqi = aqi;
		}
	}

	public static class CurrentConditionsObj {
		private int weatherIcon;
		private String temp;
		private String realFeelTemp;
		private String precipitationVolume;
		private String precipitationType;

		public int getWeatherIcon() {
			return weatherIcon;
		}

		public void setWeatherIcon(int weatherIcon) {
			this.weatherIcon = weatherIcon;
		}

		public String getTemp() {
			return temp;
		}

		public void setTemp(String temp) {
			this.temp = temp;
		}

		public String getRealFeelTemp() {
			return realFeelTemp;
		}

		public void setRealFeelTemp(String realFeelTemp) {
			this.realFeelTemp = realFeelTemp;
		}

		public String getPrecipitationVolume() {
			return precipitationVolume;
		}

		public void setPrecipitationVolume(String precipitationVolume) {
			this.precipitationVolume = precipitationVolume;
		}

		public String getPrecipitationType() {
			return precipitationType;
		}

		public void setPrecipitationType(String precipitationType) {
			this.precipitationType = precipitationType;
		}
	}
}
