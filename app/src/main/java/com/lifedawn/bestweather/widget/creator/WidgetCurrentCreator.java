package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.widget.model.CurrentConditionsObj;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class WidgetCurrentCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;
	private final DateTimeFormatter clockFormatter;
	private final String clockFormat;
	private final String refreshDateTimeFormat;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int clockTextSize;
	private int tempTextSize;


	public WidgetCurrentCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
		clockFormat = refreshDateTimeFormat = clockUnit == ValueUnits.clock12 ? "E a hh:mm" :
				"E HH:mm";
		refreshDateTimeFormatter = clockFormatter = DateTimeFormatter.ofPattern(clockFormat);
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {
			setTempHeaderViews(remoteViews);
			setTempCurrentConditionsViews(remoteViews);
		}else{
			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews));
		}

		remoteViews.setViewVisibility(R.id.clock, widgetDto.isDisplayClock() ? View.VISIBLE : View.GONE);
		remoteViews.setCharSequence(R.id.clock, "setFormat24Hour", clockFormat);
		remoteViews.setCharSequence(R.id.clock, "setFormat12Hour", clockFormat);

		remoteViews.setTextViewTextSize(R.id.clock, TypedValue.COMPLEX_UNIT_PX, clockTextSize);

		setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());

		setClockTimeZone(remoteViews);
		return remoteViews;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInWidgetCurrent) + extraSize;
		refreshDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshDateTimeTextSizeInWidgetCurrent) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInWidgetCurrent) + extraSize;
		clockTextSize = context.getResources().getDimensionPixelSize(R.dimen.clockTextSizeInWidgetCurrent) + extraSize;
	}

	public void setClockTimeZone(RemoteViews remoteViews) {
		ZoneId zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		remoteViews.setString(R.id.clock, "setTimeZone", zoneId.getId());
	}

	public void setHeaderViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime) {
		remoteViews.setTextViewTextSize(R.id.addressName, TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		remoteViews.setTextViewTextSize(R.id.refreshDateTime, TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);
		remoteViews.setTextViewText(R.id.addressName, addressName);
		remoteViews.setTextViewText(R.id.refreshDateTime, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));
	}

	public void setTempHeaderViews(RemoteViews remoteViews) {
		remoteViews.setTextViewTextSize(R.id.addressName, TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		remoteViews.setTextViewTextSize(R.id.refreshDateTime, TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);
		remoteViews.setTextViewText(R.id.addressName, context.getString(R.string.address_name));
		remoteViews.setTextViewText(R.id.refreshDateTime, ZonedDateTime.now().format(refreshDateTimeFormatter));
	}


	public void setCurrentConditionsViews(RemoteViews remoteViews, CurrentConditionsObj currentConditionsObj) {
		remoteViews.setTextViewText(R.id.current_temperature, currentConditionsObj.getTemp());
		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsObj.getWeatherIcon());
		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempTextSize);
	}

	public void setTempCurrentConditionsViews(RemoteViews remoteViews) {
		remoteViews.setTextViewText(R.id.current_temperature, "20°");
		remoteViews.setImageViewResource(R.id.current_weather_icon, R.drawable.day_clear);

		remoteViews.setTextViewTextSize(R.id.current_temperature, TypedValue.COMPLEX_UNIT_PX, tempTextSize);
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

}
