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

public class CurrentWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter;
	private final DateTimeFormatter clockFormatter;
	private final String clockFormat;
	private final String refreshDateTimeFormat;

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int clockTextSize;
	private int tempTextSize;


	public CurrentWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
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

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

}
