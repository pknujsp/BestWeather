package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EighthWidgetCreator extends AbstractWidgetCreator {
	private final DateTimeFormatter refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm");

	private int addressTextSize;
	private int refreshDateTimeTextSize;
	private int dateClockTextSize;
	private int timeClockTextSize;
	private int forecastDateTimeTextSize;
	private int forecastTempTextSize;
	private int currentWindDirectionTextSize;
	private int currentWindSpeedTextSize;
	private int currentTempTextSize;
	private int currentPrecipitationTextSize;
	private int currentAirQualityTextSize;
	private int currentFeelsLikeTempTextSize;
	private int forecastPopTextSize;

	private final int hourlyForecastCount = 5;
	private final int dailyForecastCount = 3;

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

		forecastDateTimeTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateTimeTextSizeInSimpleWidgetForecastItem) + extraSize;
		forecastTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInSimpleWidgetForecastItem) + extraSize;
		forecastPopTextSize = context.getResources().getDimensionPixelSize(R.dimen.popTextSizeInSimpleWidgetForecastItem) + extraSize;

		dateClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.dateClockTextSizeInEighthWidget) + extraSize;
		timeClockTextSize = context.getResources().getDimensionPixelSize(R.dimen.timeClockTextSizeInEighthWidget) + extraSize;

		currentTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.tempTextSizeInEighthWidget) + extraSize;
		currentFeelsLikeTempTextSize = context.getResources().getDimensionPixelSize(R.dimen.feelsLikeTempTextSizeInEighthWidget) + extraSize;
		currentPrecipitationTextSize = context.getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInEighthWidget) + extraSize;
		currentAirQualityTextSize = context.getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInEighthWidget) + extraSize;
		currentWindDirectionTextSize = context.getResources().getDimensionPixelSize(R.dimen.windDirectionTextSizeInEighthWidget) + extraSize;
		currentWindSpeedTextSize = context.getResources().getDimensionPixelSize(R.dimen.windSpeedTextSizeInEighthWidget) + extraSize;
	}


	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, CurrentConditionsDto currentConditionsDto,
	                         List<HourlyForecastDto> hourlyForecastDtoList, List<DailyForecastDto> dailyForecastDtoList, AirQualityDto airQualityDto, OnDrawBitmapCallback onDrawBitmapCallback) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, currentConditionsDto, hourlyForecastDtoList, dailyForecastDtoList, airQualityDto,
				onDrawBitmapCallback);
	}

	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount),
				WeatherResponseProcessor.getTempDailyForecastDtoList(context, dailyForecastCount)
				, WeatherResponseProcessor.getTempAirQualityDto(), null);
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, CurrentConditionsDto currentConditionsDto,
	                       List<HourlyForecastDto> hourlyForecastDtoList, List<DailyForecastDto> dailyForecastDtoList, AirQualityDto airQualityDto,
	                       @Nullable OnDrawBitmapCallback onDrawBitmapCallback) {
		final RemoteViews valuesRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_eighth_widget_values);

		valuesRemoteViews.setTextViewText(R.id.address, addressName);
		valuesRemoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter));

		valuesRemoteViews.setTextViewTextSize(R.id.address, TypedValue.COMPLEX_UNIT_PX, addressTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.refresh, TypedValue.COMPLEX_UNIT_PX, refreshDateTimeTextSize);

		ZoneId zoneId;
		if (widgetDto.getTimeZoneId() == null) {
			zoneId = ZoneId.systemDefault();
		} else {
			zoneId = widgetDto.isDisplayLocalClock() ? ZoneId.of(widgetDto.getTimeZoneId()) : ZoneId.systemDefault();
		}

		valuesRemoteViews.setString(R.id.timeClock, "setTimeZone", zoneId.getId());
		valuesRemoteViews.setString(R.id.dateClock, "setTimeZone", zoneId.getId());
		valuesRemoteViews.setTextViewTextSize(R.id.timeClock, TypedValue.COMPLEX_UNIT_PX, timeClockTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.dateClock, TypedValue.COMPLEX_UNIT_PX, dateClockTextSize);

		//현재 날씨------------------------------------------------------
		valuesRemoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp());
		String feelsLikeTemp = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.getFeelsLikeTemp();
		valuesRemoteViews.setTextViewText(R.id.feelsLikeTemp, feelsLikeTemp);
		valuesRemoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		valuesRemoteViews.setTextViewText(R.id.precipitation, precipitation);
		String airQuality = context.getString(R.string.air_quality) + ": " + AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		valuesRemoteViews.setTextViewText(R.id.airQuality, airQuality);
		valuesRemoteViews.setTextViewText(R.id.windDirection,
				context.getString(R.string.wind_direction) + ": " + currentConditionsDto.getWindDirection());
		String windSpeed = currentConditionsDto.getWindSpeed() + ", " + currentConditionsDto.getWindStrength();
		valuesRemoteViews.setTextViewText(R.id.windSpeed, windSpeed);

		valuesRemoteViews.setTextViewTextSize(R.id.temperature, TypedValue.COMPLEX_UNIT_PX, currentTempTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.feelsLikeTemp, TypedValue.COMPLEX_UNIT_PX, currentFeelsLikeTempTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.precipitation, TypedValue.COMPLEX_UNIT_PX, currentPrecipitationTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.windDirection, TypedValue.COMPLEX_UNIT_PX, currentWindDirectionTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.windSpeed, TypedValue.COMPLEX_UNIT_PX, currentWindSpeedTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.airQuality, TypedValue.COMPLEX_UNIT_PX, currentAirQualityTextSize);

		//시간별 예보-------------------------------------------------------------------------------------
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E\n0");

		for (int cell = 0; cell < hourlyForecastCount; cell++) {
			RemoteViews hourlyRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear);

			if (hourlyForecastDtoList.get(cell).getHours().getHour() == 0) {
				hourlyRemoteViews.setTextViewText(R.id.dateTime, hourlyForecastDtoList.get(cell).getHours().format(hour0Formatter));
			} else {
				hourlyRemoteViews.setTextViewText(R.id.dateTime, String.valueOf(hourlyForecastDtoList.get(cell).getHours().getHour()));
			}
			hourlyRemoteViews.setImageViewResource(R.id.leftIcon, hourlyForecastDtoList.get(cell).getWeatherIcon());
			hourlyRemoteViews.setTextViewText(R.id.temperature, hourlyForecastDtoList.get(cell).getTemp());
			hourlyRemoteViews.setTextViewText(R.id.pop, hourlyForecastDtoList.get(cell).getPop());

			hourlyRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			hourlyRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);
			hourlyRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

			valuesRemoteViews.addView(R.id.hourlyForecast, hourlyRemoteViews);
		}

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E");

		//----------------daily---------------------------------------------------------------
		for (int cell = 0; cell < dailyForecastCount; cell++) {
			RemoteViews dailyRemoteViews = new RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear);

			dailyRemoteViews.setTextViewText(R.id.dateTime, dailyForecastDtoList.get(cell).getDate().format(dateFormatter));

			if (dailyForecastDtoList.get(cell).isSingle()) {
				dailyRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell).getSingleValues().getWeatherIcon());
				dailyRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE);

				dailyRemoteViews.setTextViewText(R.id.pop, dailyForecastDtoList.get(cell).getSingleValues().getPop());
			} else {
				dailyRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell).getAmValues().getWeatherIcon());
				dailyRemoteViews.setImageViewResource(R.id.rightIcon, dailyForecastDtoList.get(cell).getPmValues().getWeatherIcon());
				dailyRemoteViews.setTextViewText(R.id.pop, dailyForecastDtoList.get(cell).getAmValues().getPop() + "/" +
						dailyForecastDtoList.get(cell).getPmValues().getPop());
			}

			dailyRemoteViews.setTextViewText(R.id.temperature,
					dailyForecastDtoList.get(cell).getMinTemp() + "/" + dailyForecastDtoList.get(cell).getMaxTemp());

			dailyRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE);
			dailyRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE);

			valuesRemoteViews.addView(R.id.dailyForecast, dailyRemoteViews);
		}

		remoteViews.removeAllViews(R.id.noBitmapValuesView);
		remoteViews.addView(R.id.noBitmapValuesView, valuesRemoteViews);
		remoteViews.setViewVisibility(R.id.noBitmapValuesView, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.valuesView, View.GONE);
	}

	@Override
	public void setDisplayClock(boolean displayClock) {
		widgetDto.setDisplayClock(displayClock);
	}

	@Override
	public void setDataViewsOfSavedData() {
		WeatherSourceType weatherSourceType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherSourceTypeSet());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherSourceType = WeatherSourceType.KMA_WEB;
		}

		RemoteViews remoteViews = createRemoteViews(false);
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());
		List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(context, jsonObject,
				weatherSourceType, widgetDto.getLatitude(), widgetDto.getLongitude());
		List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(context, jsonObject,
				weatherSourceType);
		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(context, jsonObject);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
				hourlyForecastDtoList, dailyForecastDtoList, airQualityDto, null);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId,
				remoteViews);
	}

}
