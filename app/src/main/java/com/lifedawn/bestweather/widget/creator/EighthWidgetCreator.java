package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
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

public class EighthWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int dateClockTextSize;
	private int timeClockTextSize;
	private int hourTextSize;
	private int tempTextSize;
	private int currentTempTextSize;
	private int precipitationTextSize;
	private int airQualityTextSize;
	private int popTextSize;

	private final int hourlyForecastCount = 5;

	public EighthWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
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

		//setBackgroundAlpha(remoteViews, widgetDto.getBackgroundAlpha());

		return remoteViews;
	}

	@Override
	public void setTextSize(int amount) {
		final int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(amount),
				context.getResources().getDisplayMetrics());
		final int extraSize = amount >= 0 ? absSize : absSize * -1;

		addressTextSize = context.getResources().getDimensionPixelSize(R.dimen.addressTextSizeInCommonWidgetHeader) + extraSize;
		refreshDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.refreshDateTimeTextSizeInCommonWidgetHeader) + extraSize;

		hourTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTimeTextSizeInSimpleWidgetForecastItem) + extraSize;
		tempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSimpleWidgetForecastItem) + extraSize;
		popTextSize = context.getResources().getDimensionPixelSize(R.dimen.popTextSizeInSimpleWidgetForecastItem) + extraSize;

		dateClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateClockTextSizeInEighthWidget) + extraSize;
		timeClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.timeClockTextSizeInEighthWidget) + extraSize;

		currentTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInEighthWidget) + extraSize;
		precipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInEighthWidget) + extraSize;
		airQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInEighthWidget) + extraSize;
	}


	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, CurrentConditionsDto currentConditionsDto,
	                         List<HourlyForecastDto> hourlyForecastDtoList, AirQualityDto airQualityDto, OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, currentConditionsDto, hourlyForecastDtoList, airQualityDto, onDrawBitmapCallback);
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(), null, null, null, null);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, CurrentConditionsDto currentConditionsDto,
	                       List<HourlyForecastDto> hourlyForecastDtoList, AirQualityDto airQualityDto,
	                       @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		final RemoteViews clockRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_eighth_widget_clock);

		clockRemoteViews.setTextViewText(R.id.address, addressName);
		clockRemoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		clockRemoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		clockRemoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		ZoneId zoneId;
		if (widgetDto.getTimeZoneId() == null) {
			zoneId = ZoneId.systemDefault();
		} else {
			zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		}

		remoteViews.setString(R.id.timeClock, "setTimeZone", zoneId.getId());
		remoteViews.setString(R.id.dateClock, "setTimeZone", zoneId.getId());
		remoteViews.setTextViewTextSize(R.id.timeClock, TypedValue.COMPLEX_UNIT_PX, timeClockTextSize);
		remoteViews.setTextViewTextSize(R.id.dateClock, TypedValue.COMPLEX_UNIT_PX, dateClockTextSize);

		remoteViews.addView(R.id.noBitmapValuesView, clockRemoteViews);
		remoteViews.setViewVisibility(R.id.noBitmapValuesView, View.VISIBLE);

		final ViewGroup eighthValuesView = (ViewGroup) layoutInflater.inflate(R.layout.view_eighth_widget_values, null, false);

		((TextView) eighthValuesView.findViewById(R.id.temperature)).setTextSize(TypedValue.COMPLEX_UNIT_PX, tempTextSize);
		((TextView) eighthValuesView.findViewById(R.id.precipitation)).setTextSize(TypedValue.COMPLEX_UNIT_PX, precipitationTextSize);
		((TextView) eighthValuesView.findViewById(R.id.airQuality)).setTextSize(TypedValue.COMPLEX_UNIT_PX, airQualityTextSize);

		((TextView) eighthValuesView.findViewById(R.id.temperature)).setText(currentConditionsDto.getTemp());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}

		((TextView) eighthValuesView.findViewById(R.id.precipitation)).setText(precipitation);
		String simpleAirQuality = context.getString(R.string.air_quality) + ": " +
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		((TextView) eighthValuesView.findViewById(R.id.airQuality)).setText(simpleAirQuality);

		final LinearLayout hourlyForecastLayout = eighthValuesView.findViewById(R.id.hourlyForecast);

		LinearLayout hourlyForecastHourAndIconLinearLayout = new LinearLayout(context);
		hourlyForecastHourAndIconLinearLayout.setId(R.id.hourAndIconView);
		hourlyForecastHourAndIconLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams hourAndIconCellLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		hourAndIconCellLayoutParams.gravity = Gravity.CENTER;
		hourAndIconCellLayoutParams.weight = 1;

		List<Integer> tempList = new ArrayList<>();
		final String degree = "°";
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E 0");

		for (int cell = 0; cell < hourlyForecastCount; cell++) {
			View view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false);

			if (cell == 0) {
				((TextView) view.findViewById(R.id.dateTime)).setText(context.getString(R.string.current));
			} else {
				//hour, weatherIcon, pop
				if (hourlyForecastDtoList.get(cell).getHours().getHour() == 0) {
					((TextView) view.findViewById(R.id.dateTime)).setText(hourlyForecastDtoList.get(cell).getHours().format(hour0Formatter));
				} else {
					((TextView) view.findViewById(R.id.dateTime)).setText(String.valueOf(hourlyForecastDtoList.get(cell).getHours().getHour()));
				}
			}
			((ImageView) view.findViewById(R.id.leftIcon)).setImageResource(hourlyForecastDtoList.get(cell).getWeatherIcon());
			tempList.add(Integer.parseInt(hourlyForecastDtoList.get(cell).getTemp().replace(degree, "")));

			if (cell != 0) {
				((TextView) view.findViewById(R.id.pop)).setText(hourlyForecastDtoList.get(cell).getPop());
			} else {
				view.findViewById(R.id.popLayout).setVisibility(View.INVISIBLE);
			}

			((TextView) view.findViewById(R.id.dateTime)).setTextSize(TypedValue.COMPLEX_UNIT_PX, hourTextSize);
			((TextView) view.findViewById(R.id.pop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, popTextSize);

			view.findViewById(R.id.temperature).setVisibility(View.GONE);
			view.findViewById(R.id.rightIcon).setVisibility(View.GONE);

			hourlyForecastHourAndIconLinearLayout.addView(view, hourAndIconCellLayoutParams);
		}

		DetailSingleTemperatureView detailSingleTemperatureView = new DetailSingleTemperatureView(context, tempList);
		detailSingleTemperatureView.setTempTextSizePx(tempTextSize);

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
		tempRowLayoutParams.weight = 1;

		hourlyForecastLayout.addView(hourlyForecastHourAndIconLinearLayout,
				new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		hourlyForecastLayout.addView(detailSingleTemperatureView, tempRowLayoutParams);

		drawBitmap(eighthValuesView, onDrawBitmapCallback, remoteViews);
	}


	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		WeatherSourceType weatherSourceType = WeatherSourceType.valueOf(widgetDto.getWeatherSourceType());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherSourceType = WeatherSourceType.KMA;
		}

		RemoteViews remoteViews = createRemoteViews(false);
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());
		List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(context, jsonObject,
				weatherSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());
		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(context, jsonObject);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
				hourlyForecastDtoList, airQualityDto, null);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

}
