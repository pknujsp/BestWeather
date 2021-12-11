package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.widget.model.AirQualityObj;
import com.lifedawn.bestweather.widget.model.CurrentConditionsObj;
import com.lifedawn.bestweather.widget.model.DailyForecastObj;
import com.lifedawn.bestweather.widget.model.HourlyForecastObj;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WidgetCurrentHourlyDailyCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;
	private final String clockDateFormat;
	private final String clockTimeFormat;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int clockDateTextSize;
	private int clockTimeTextSize;
	private int tempTextSize;
	private int feelsLikeTempTextSize;
	private int aqiTextSize;
	private int precipitationTextSize;

	public WidgetCurrentHourlyDailyCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
		clockDateFormat = "M.d E";
		clockTimeFormat = clockUnit == ValueUnits.clock12 ? "a hh:mm" :
				"HH:mm";
		refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E " + clockTimeFormat);
	}


	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {
			setTempHourlyForecastViews(remoteViews);
			setTempDailyForecastViews(remoteViews);
		} else {
			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews));
		}

		remoteViews.setViewVisibility(R.id.watch, widgetDto.isDisplayClock() ? View.VISIBLE : View.GONE);
		remoteViews.setCharSequence(R.id.date, "setFormat24Hour", clockDateFormat);
		remoteViews.setCharSequence(R.id.date, "setFormat12Hour", clockDateFormat);
		remoteViews.setCharSequence(R.id.time, "setFormat24Hour", clockTimeFormat);
		remoteViews.setCharSequence(R.id.time, "setFormat12Hour", clockTimeFormat);

		remoteViews.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, clockDateTextSize);
		remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, clockTimeTextSize);

		setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());

		remoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempTextSize);
		remoteViews.setTextViewTextSize(R.id.current_realfeel_temperature, TypedValue.COMPLEX_UNIT_PX, feelsLikeTempTextSize);
		remoteViews.setTextViewTextSize(R.id.current_precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);

		remoteViews.setTextViewTextSize(R.id.current_airquality, TypedValue.COMPLEX_UNIT_PX, aqiTextSize);

		setClockTimeZone(remoteViews);
		return remoteViews;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInHeader) + extraSize;
		refreshDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshTextSizeInHeader) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrentHourlyDailyWidget) + extraSize;
		clockDateTextSize = context.getResources().getDimensionPixelSize(R.dimen.clockDateTextSizeInCurrentHourlyDailyWidget) + extraSize;
		clockTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.clockTimeTextSizeInCurrentHourlyDailyWidget) + extraSize;
		feelsLikeTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.feelsLikeTempTextSizeInCurrentHourlyDailyWidget) + extraSize;
		aqiTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrentHourlyDailyWidget) + extraSize;
		precipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrentHourlyDailyWidget) + extraSize;
	}

	public void setClockTimeZone(RemoteViews remoteViews) {
		ZoneId zoneId;
		if (widgetDto.getTimeZoneId() == null) {
			zoneId = ZoneId.systemDefault();
		} else {
			zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		}

		remoteViews.setString(R.id.time, "setTimeZone", zoneId.getId());
		remoteViews.setString(R.id.date, "setTimeZone", zoneId.getId());
	}

	public void setAirQualityViews(RemoteViews remoteViews, AirQualityObj airQualityObj) {
		remoteViews.setTextViewText(R.id.current_airquality, context.getString(R.string.air_quality) + " " + (airQualityObj.isSuccessful() ?
				airQualityObj.getAqi() :
				context.getString(R.string.noData)));
	}


	public void setHeaderViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));
	}


	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsObj currentConditionsObj) {
		remoteViews.setTextViewText(R.id.current_temperature, currentConditionsObj.getTemp());
		remoteViews.setTextViewText(R.id.current_realfeel_temperature, currentConditionsObj.getRealFeelTemp());

		String precipitation = currentConditionsObj.getPrecipitationType() + " " + currentConditionsObj.getPrecipitationVolume();
		remoteViews.setTextViewText(R.id.current_precipitation, precipitation);

		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsObj.getWeatherIcon());

		remoteViews.setViewVisibility(R.id.current_realfeel_temperature, currentConditionsObj.getRealFeelTemp() == null ?
				View.GONE : View.VISIBLE);
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, List<HourlyForecastObj> hourlyForecastObjList) {
		remoteViews.removeAllViews(R.id.hourly_forecast_row_1);
		remoteViews.removeAllViews(R.id.hourly_forecast_row_2);

		for (int i = 0; i < 12; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_linear);

			childRemoteViews.setTextViewText(R.id.hourly_clock, hourlyForecastObjList.get(i).getHours());
			childRemoteViews.setTextViewText(R.id.hourly_temperature, hourlyForecastObjList.get(i).getTemp());
			childRemoteViews.setImageViewResource(R.id.hourly_weather_icon, hourlyForecastObjList.get(i).getWeatherIcon());

			if (i >= 6) {
				remoteViews.addView(R.id.hourly_forecast_row_2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourly_forecast_row_1, childRemoteViews);
			}
		}
	}

	public void setDailyForecastViews(RemoteViews remoteViews, List<DailyForecastObj> dailyForecastObjList) {
		remoteViews.removeAllViews(R.id.daily_forecast_row);

		for (int day = 0; day < 4; day++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_forecast_item_in_linear);

			childRemoteViews.setTextViewText(R.id.daily_date, dailyForecastObjList.get(day).getDate());
			childRemoteViews.setTextViewText(R.id.daily_temperature, dailyForecastObjList.get(day).getMinTemp() + " / " + dailyForecastObjList.get(day).getMaxTemp());

			childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.VISIBLE);
			childRemoteViews.setViewVisibility(R.id.daily_right_weather_icon, View.VISIBLE);

			if (dailyForecastObjList.get(day).isSingle()) {
				childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastObjList.get(day).getSingleWeatherIcon());
				childRemoteViews.setViewVisibility(R.id.daily_right_weather_icon, View.GONE);
			} else {
				childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastObjList.get(day).getAmWeatherIcon());
				childRemoteViews.setImageViewResource(R.id.daily_right_weather_icon, dailyForecastObjList.get(day).getPmWeatherIcon());
			}

			remoteViews.addView(R.id.daily_forecast_row, childRemoteViews);
		}
	}


	public void setTempHourlyForecastViews(RemoteViews remoteViews) {
		remoteViews.removeAllViews(R.id.hourly_forecast_row_1);
		remoteViews.removeAllViews(R.id.hourly_forecast_row_2);

		for (int i = 0; i < 12; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_linear);

			if (i >= 6) {
				remoteViews.addView(R.id.hourly_forecast_row_2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourly_forecast_row_1, childRemoteViews);
			}
		}
	}

	public void setTempDailyForecastViews(RemoteViews remoteViews) {
		remoteViews.removeAllViews(R.id.daily_forecast_row);

		for (int day = 0; day < 4; day++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_forecast_item_in_linear);

			childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.VISIBLE);
			childRemoteViews.setViewVisibility(R.id.daily_right_weather_icon, View.VISIBLE);

			remoteViews.addView(R.id.daily_forecast_row, childRemoteViews);
		}
	}


	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}
}
