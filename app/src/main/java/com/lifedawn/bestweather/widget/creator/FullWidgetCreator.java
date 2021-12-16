package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FullWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;
	private final String timeClockFormat;
	private final String dateClockFormat;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int clockDateTextSize;
	private int clockTimeTextSize;
	private int tempTextSize;
	private int aqiTextSize;
	private int precipitationTextSize;

	public FullWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
		timeClockFormat = "HH:mm";
		dateClockFormat = "M.d E";
		refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a hh:mm");
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

		remoteViews.setViewVisibility(R.id.clockLayout, widgetDto.isDisplayClock() ? View.VISIBLE : View.GONE);
		remoteViews.setCharSequence(R.id.dateClock, "setFormat24Hour", dateClockFormat);
		remoteViews.setCharSequence(R.id.dateClock, "setFormat12Hour", dateClockFormat);
		remoteViews.setCharSequence(R.id.timeClock, "setFormat24Hour", timeClockFormat);
		remoteViews.setCharSequence(R.id.timeClock, "setFormat12Hour", timeClockFormat);

		remoteViews.setTextViewTextSize(R.id.dateClock, TypedValue.COMPLEX_UNIT_PX, clockDateTextSize);
		remoteViews.setTextViewTextSize(R.id.timeClock, TypedValue.COMPLEX_UNIT_PX, clockTimeTextSize);

		//setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());

		remoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		remoteViews.setTextViewTextSize(R.id.temperature, TypedValue.COMPLEX_UNIT_PX, tempTextSize);
		remoteViews.setTextViewTextSize(R.id.precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);
		remoteViews.setTextViewTextSize(R.id.airQuality, TypedValue.COMPLEX_UNIT_PX, aqiTextSize);

		setClockTimeZone(remoteViews);
		return remoteViews;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInCommonWidgetHeader) + extraSize;
		refreshDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshDateTimeTextSizeInCommonWidgetHeader) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInFullWidget) + extraSize;
		clockDateTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateClockTextSizeInFullWidget) + extraSize;
		clockTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.timeClockTextSizeInFullWidget) + extraSize;
		aqiTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInFullWidget) + extraSize;
		precipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInFullWidget) + extraSize;
	}

	public void setClockTimeZone(RemoteViews remoteViews) {
		ZoneId zoneId;
		if (widgetDto.getTimeZoneId() == null) {
			zoneId = ZoneId.systemDefault();
		} else {
			zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		}

		remoteViews.setString(R.id.dateClock, "setTimeZone", zoneId.getId());
		remoteViews.setString(R.id.timeClock, "setTimeZone", zoneId.getId());
	}

	public void setAirQualityViews(RemoteViews remoteViews, AirQualityDto airQualityDto) {
		remoteViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));
	}

	public void setAirQualityViews(RemoteViews remoteViews, String value) {
		remoteViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.air_quality) + ": " + value);
	}


	public void setHeaderViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));
	}


	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsDto currentConditionsDto) {
		remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		remoteViews.setTextViewText(R.id.precipitation, precipitation);
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());
	}

	public void setHourlyForecastViews(RemoteViews remoteViews, List<HourlyForecastDto> hourlyForecastDtoList) {
		remoteViews.removeAllViews(R.id.hourly_forecast_row_1);
		remoteViews.removeAllViews(R.id.hourly_forecast_row_2);

		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");
		String hours = "";

		for (int i = 0; i < 12; i++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_hourly_forecast_item_in_linear);

			if (hourlyForecastDtoList.get(i).getHours().getHour() == 0) {
				hours = hourlyForecastDtoList.get(i).getHours().format(hour0Formatter);
			} else {
				hours = String.valueOf(hourlyForecastDtoList.get(i).getHours().getHour());
			}

			childRemoteViews.setTextViewText(R.id.hourly_clock, hours);
			childRemoteViews.setTextViewText(R.id.hourly_temperature, hourlyForecastDtoList.get(i).getTemp());
			childRemoteViews.setImageViewResource(R.id.hourly_weather_icon, hourlyForecastDtoList.get(i).getWeatherIcon());

			if (i >= 6) {
				remoteViews.addView(R.id.hourly_forecast_row_2, childRemoteViews);
			} else {
				remoteViews.addView(R.id.hourly_forecast_row_1, childRemoteViews);
			}
		}

	}

	public void setDailyForecastViews(RemoteViews remoteViews, List<DailyForecastDto> dailyForecastDtoList) {
		remoteViews.removeAllViews(R.id.daily_forecast_row);
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E");

		for (int day = 0; day < 4; day++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_forecast_item_in_linear);

			childRemoteViews.setTextViewText(R.id.daily_date, dailyForecastDtoList.get(day).getDate().format(dateFormatter));
			childRemoteViews.setTextViewText(R.id.daily_temperature, dailyForecastDtoList.get(day).getMinTemp() + " / " + dailyForecastDtoList.get(day).getMaxTemp());

			childRemoteViews.setViewVisibility(R.id.daily_left_weather_icon, View.VISIBLE);
			childRemoteViews.setViewVisibility(R.id.daily_right_weather_icon, View.VISIBLE);

			if (dailyForecastDtoList.get(day).isSingle()) {
				childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastDtoList.get(day).getSingleValues().getWeatherIcon());
				childRemoteViews.setViewVisibility(R.id.daily_right_weather_icon, View.GONE);
			} else {
				childRemoteViews.setImageViewResource(R.id.daily_left_weather_icon, dailyForecastDtoList.get(day).getAmValues().getWeatherIcon());
				childRemoteViews.setImageViewResource(R.id.daily_right_weather_icon, dailyForecastDtoList.get(day).getPmValues().getWeatherIcon());
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
