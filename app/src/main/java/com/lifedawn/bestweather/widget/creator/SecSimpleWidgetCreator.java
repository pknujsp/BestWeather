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

public class SecSimpleWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;
	private final String clockFormat = "HH:mm";

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int clockTextSize;
	private int tempTextSize;
	private int precipitationTextSize;
	private int airQualityTextSize;

	public SecSimpleWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
		refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a hh:mm");
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {
			setTempDailyForecastViews(remoteViews);
		} else {
			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews));
		}

		remoteViews.setViewVisibility(R.id.clock, widgetDto.isDisplayClock() ? View.VISIBLE : View.GONE);
		remoteViews.setCharSequence(R.id.clock, "setFormat24Hour", clockFormat);
		remoteViews.setCharSequence(R.id.clock, "setFormat12Hour", clockFormat);

		remoteViews.setTextViewTextSize(R.id.addressName, TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		remoteViews.setTextViewTextSize(R.id.clock, TypedValue.COMPLEX_UNIT_PX, clockTextSize);
		remoteViews.setTextViewTextSize(R.id.precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);
		remoteViews.setTextViewTextSize(R.id.temperature, TypedValue.COMPLEX_UNIT_PX, tempTextSize);
		remoteViews.setTextViewTextSize(R.id.airQuality, TypedValue.COMPLEX_UNIT_PX, airQualityTextSize);

		//setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());

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
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSimple1Widget) + extraSize;
		clockTextSize = context.getResources().getDimensionPixelSize(R.dimen.clockTextSizeInSimple1Widget) + extraSize;
		precipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInSimple1Widget) + extraSize;
		airQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInSimple1Widget) + extraSize;
	}

	public void setClockTimeZone(RemoteViews remoteViews) {
		ZoneId zoneId;
		if (widgetDto.getTimeZoneId() == null) {
			zoneId = ZoneId.systemDefault();
		} else {
			zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		}

		remoteViews.setString(R.id.clock, "setTimeZone", zoneId.getId());
	}

	public void setHeaderViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));
	}

	public void setAirQualityViews(RemoteViews remoteViews, AirQualityDto airQualityDto) {
		remoteViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi()));
	}

	public void setAirQualityViews(RemoteViews remoteViews, String value) {
		remoteViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.air_quality) + ": " + value);
	}

	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsDto currentConditionsDto) {
		remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		remoteViews.setTextViewText(R.id.precipitation, precipitation);
	}

	public void setDailyForecastViews(RemoteViews remoteViews, List<DailyForecastDto> dailyForecastDtoList) {
		remoteViews.removeAllViews(R.id.dailyForecastView);
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E");

		for (int day = 0; day < 3; day++) {
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

			remoteViews.addView(R.id.dailyForecastView, childRemoteViews);
		}
	}

	public void setTempDailyForecastViews(RemoteViews remoteViews) {
		for (int day = 0; day < 3; day++) {
			RemoteViews childRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_daily_forecast_item_in_linear);
			remoteViews.addView(R.id.daily_forecast_row, childRemoteViews);
		}
	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

}
