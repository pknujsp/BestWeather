package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindDirectionConverter;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.MeasureSpec.EXACTLY;

public class CurrentWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("E a h:mm");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int tempTextSize;
	private int precipitationTextSize;
	private int airQualityTextSize;
	private int humidityTextSize;
	private int windDirectionTextSize;
	private int windSpeedTextSize;
	private int windStrengthTextSize;


	public CurrentWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}

	@Override
	public RemoteViews createRemoteViews(boolean needTempData) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		if (needTempData) {
			setTempDataViews(remoteViews);
		} else {
			remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent(remoteViews));
		}

		/*
		remoteViews.setViewVisibility(R.id.clockLayout, widgetDto.isDisplayClock() ? View.VISIBLE : View.GONE);
		remoteViews.setCharSequence(R.id.dateClock, "setFormat24Hour", dateClockFormat);
		remoteViews.setCharSequence(R.id.dateClock, "setFormat12Hour", dateClockFormat);
		remoteViews.setCharSequence(R.id.timeClock, "setFormat24Hour", timeClockFormat);
		remoteViews.setCharSequence(R.id.timeClock, "setFormat12Hour", timeClockFormat);

		setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());
		setClockTimeZone(remoteViews);
		 */
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
		precipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrentWidget) + extraSize;
		airQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrentWidget) + extraSize;
		humidityTextSize = context.getResources().getDimensionPixelSize(R.dimen.humidityTextSizeInCurrentWidget) + extraSize;
		windDirectionTextSize = context.getResources().getDimensionPixelSize(R.dimen.windDirectionTextSizeInCurrentWidget) + extraSize;
		windSpeedTextSize = context.getResources().getDimensionPixelSize(R.dimen.windSpeedTextSizeInCurrentWidget) + extraSize;
		windStrengthTextSize = context.getResources().getDimensionPixelSize(R.dimen.windStrengthTextSizeInCurrentWidget) + extraSize;
	}

	public void setClockTimeZone(RemoteViews remoteViews) {
		ZoneId zoneId;
		if (widgetDto.getTimeZoneId() == null) {
			zoneId = ZoneId.systemDefault();
		} else {
			zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		}

		//remoteViews.setString(R.id.dateClock, "setTimeZone", zoneId.getId());
		//remoteViews.setString(R.id.timeClock, "setTimeZone", zoneId.getId());
	}

	public View makeHeaderViews(LayoutInflater layoutInflater, String addressName, String lastRefreshDateTime) {
		View view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false);
		((TextView) view.findViewById(R.id.address)).setText(addressName);
		((TextView) view.findViewById(R.id.refresh)).setText(ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		((TextView) view.findViewById(R.id.address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		((TextView) view.findViewById(R.id.refresh)).setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		return view;
	}

	public View makeCurrentConditionsViews(LayoutInflater layoutInflater, CurrentConditionsDto currentConditionsDto,
	                                       AirQualityDto airQualityDto) {
		View view = layoutInflater.inflate(R.layout.view_widget_current_values, null, false);

		((TextView) view.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp());

		String airQuality = context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		((TextView) view.findViewById(R.id.airQuality)).setText(airQuality);

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		((TextView) view.findViewById(R.id.precipitation)).setText(precipitation);
		((TextView) view.findViewById(R.id.humidity)).setText(currentConditionsDto.getHumidity());
		((TextView) view.findViewById(R.id.windDirection)).setText(currentConditionsDto.getWindDirection());
		((TextView) view.findViewById(R.id.windSpeed)).setText(currentConditionsDto.getWindSpeed());
		((TextView) view.findViewById(R.id.windStrength)).setText(currentConditionsDto.getWindStrength());
		view.findViewById(R.id.windDirectionArrow).setRotation(currentConditionsDto.getWindDirectionDegree() + 180);

		((TextView) view.findViewById(R.id.temperature)).setTextSize(TypedValue.COMPLEX_UNIT_PX, tempTextSize);
		((TextView) view.findViewById(R.id.airQuality)).setTextSize(TypedValue.COMPLEX_UNIT_PX, airQualityTextSize);
		((TextView) view.findViewById(R.id.precipitation)).setTextSize(TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);
		((TextView) view.findViewById(R.id.humidity)).setTextSize(TypedValue.COMPLEX_UNIT_PX, humidityTextSize);
		((TextView) view.findViewById(R.id.windDirection)).setTextSize(TypedValue.COMPLEX_UNIT_PX, windDirectionTextSize);
		((TextView) view.findViewById(R.id.windSpeed)).setTextSize(TypedValue.COMPLEX_UNIT_PX, windSpeedTextSize);
		((TextView) view.findViewById(R.id.windStrength)).setTextSize(TypedValue.COMPLEX_UNIT_PX, windStrengthTextSize);

		return view;
	}

	public void setTempDataViews(RemoteViews remoteViews) {

		CurrentConditionsDto tempCurrentConditions = new CurrentConditionsDto();
		tempCurrentConditions.setTemp("10°").setWeatherIcon(R.drawable.day_clear).setWindDirectionDegree(120)
				.setWindDirection(WindDirectionConverter.windDirection(context, String.valueOf(tempCurrentConditions.getWindDirectionDegree())))
				.setWindSpeed(ValueUnits.convertWindSpeed("2.6", ValueUnits.mPerSec) + "m/s")
				.setWindStrength(WeatherResponseProcessor.getWindSpeedDescription("2.6"));

		AirQualityDto tempAirQualityDto = new AirQualityDto();
		tempAirQualityDto.setAqi(10);

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), tempAirQualityDto, tempCurrentConditions,
				null);
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                         OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, onDrawBitmapCallback);
	}


	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto, CurrentConditionsDto currentConditionsDto,
	                       @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		View currentConditionsView = makeCurrentConditionsViews(layoutInflater, currentConditionsDto, airQualityDto);
		currentConditionsView.setId(R.id.currentConditions);

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams currentConditionsViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		currentConditionsViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		currentConditionsViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);

		RelativeLayout rootLayout = new RelativeLayout(context);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(currentConditionsView, currentConditionsViewLayoutParams);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		final int[] widgetSize = getWidgetExactSizeInPx(appWidgetManager);
		final float widgetPadding = context.getResources().getDimension(R.dimen.widget_padding);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec((int) (widgetSize[0] - widgetPadding * 2), EXACTLY);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec((int) (widgetSize[1] - widgetPadding * 2), EXACTLY);

		rootLayout.measure(widthSpec, heightSpec);
		rootLayout.layout(0, 0, rootLayout.getMeasuredWidth(), rootLayout.getMeasuredHeight());

		rootLayout.setDrawingCacheEnabled(true);
		rootLayout.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		Bitmap viewBmp = rootLayout.getDrawingCache();
		if (onDrawBitmapCallback != null) {
			onDrawBitmapCallback.onCreatedBitmap(viewBmp);
		}
		remoteViews.setImageViewBitmap(R.id.valuesView, viewBmp);
	}


	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

}
