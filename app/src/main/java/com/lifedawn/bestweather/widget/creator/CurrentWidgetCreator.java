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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CurrentWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;
	private final String timeClockFormat;
	private final String dateClockFormat;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int dateClockTextSize;
	private int timeClockTextSize;
	private int tempTextSize;
	private int precipitationTextSize;
	private int airQualityTextSize;


	public CurrentWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
		timeClockFormat = "HH:mm";
		dateClockFormat = "M.d E";
		refreshDateTimeFormatter = DateTimeFormatter.ofPattern("a h:mm");
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {

		} else {
			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews));
		}

		remoteViews.setViewVisibility(R.id.clockLayout, widgetDto.isDisplayClock() ? View.VISIBLE : View.GONE);
		remoteViews.setCharSequence(R.id.dateClock, "setFormat24Hour", dateClockFormat);
		remoteViews.setCharSequence(R.id.dateClock, "setFormat12Hour", dateClockFormat);
		remoteViews.setCharSequence(R.id.timeClock, "setFormat24Hour", timeClockFormat);
		remoteViews.setCharSequence(R.id.timeClock, "setFormat12Hour", timeClockFormat);

		remoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		remoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		remoteViews.setTextViewTextSize(R.id.dateClock, TypedValue.COMPLEX_UNIT_PX, dateClockTextSize);
		remoteViews.setTextViewTextSize(R.id.timeClock, TypedValue.COMPLEX_UNIT_PX, timeClockTextSize);
		remoteViews.setTextViewTextSize(R.id.airQuality, TypedValue.COMPLEX_UNIT_PX, airQualityTextSize);
		remoteViews.setTextViewTextSize(R.id.precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);
		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempTextSize);

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
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrentWidget) + extraSize;
		dateClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateClockTextSizeInCurrentWidget) + extraSize;
		timeClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.timeClockTextSizeInCurrentWidget) + extraSize;
		precipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrentWidget) + extraSize;
		airQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrentWidget) + extraSize;
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

	public void setHeaderViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime) {
		remoteViews.setTextViewText(R.id.address, addressName);
		remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));
	}


	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsDto currentConditionsDto) {
		final String celsius = "C";
		final String fahrenheit = "F";
		remoteViews.setTextViewText(R.id.current_temperature, currentConditionsDto.getTemp().replace(celsius, "").replace(fahrenheit, ""));
		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsDto.getWeatherIcon());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		remoteViews.setTextViewText(R.id.precipitation, precipitation);

		remoteViews.setTextViewTextSize(R.id.precipitation, TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);
		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempTextSize);
	}

	public void setAirQualityViews(RemoteViews remoteViews, String value) {
		remoteViews.setTextViewText(R.id.airQuality,
				context.getString(R.string.air_quality) + ": " + value);
	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

}
