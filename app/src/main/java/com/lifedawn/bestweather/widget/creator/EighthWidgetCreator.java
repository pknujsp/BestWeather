package com.lifedawn.bestweather.widget.creator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.widgetprovider.EighthWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private final int hourlyForecastCount = 7;
	private final int dailyForecastCount = 3;

	public EighthWidgetCreator(Context context, WidgetUpdateCallback widgetUpdateCallback, int appWidgetId) {
		super(context, widgetUpdateCallback, appWidgetId);
	}

	@Override
	public Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.hourlyForecast);
		set.add(WeatherDataType.airQuality);

		return set;
	}

	@Override
	public RemoteViews createTempViews(Integer parentWidth, Integer parentHeight) {
		RemoteViews remoteViews = createBaseRemoteViews();

		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempCurrentConditionsDto(context),
				WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount),
				null
				, WeatherResponseProcessor.getTempAirQualityDto(), null);
		return remoteViews;
	}

	@Override
	public RemoteViews createRemoteViews() {
		RemoteViews remoteViews = createBaseRemoteViews();
		remoteViews.setOnClickPendingIntent(R.id.root_layout, getOnClickedPendingIntent());

		return remoteViews;
	}

	@Override
	public Class<?> widgetProviderClass() {
		return EighthWidgetProvider.class;
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
		valuesRemoteViews.setTextViewText(R.id.temperature, currentConditionsDto.getTemp().replace(tempDegree, "°"));
		valuesRemoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon());

		String precipitation = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = context.getString(R.string.not_precipitation);
		}
		valuesRemoteViews.setTextViewText(R.id.precipitation, precipitation);

		String airQuality = null;
		if (airQualityDto.isSuccessful()) {
			airQuality = AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		} else {
			airQuality = context.getString(R.string.noData);
		}

		valuesRemoteViews.setTextViewText(R.id.airQuality, airQuality);
		valuesRemoteViews.setTextViewTextSize(R.id.temperature, TypedValue.COMPLEX_UNIT_PX, currentTempTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.precipitation, TypedValue.COMPLEX_UNIT_PX, currentPrecipitationTextSize);
		valuesRemoteViews.setTextViewTextSize(R.id.airQuality, TypedValue.COMPLEX_UNIT_PX, currentAirQualityTextSize);

		/*
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
		 */

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
		WeatherProviderType weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		if (widgetDto.isTopPriorityKma() && widgetDto.getCountryCode().equals("KR")) {
			weatherProviderType = WeatherProviderType.KMA_WEB;
		}

		RemoteViews remoteViews = createRemoteViews();
		RemoteViewsUtil.onSuccessfulProcess(remoteViews);

		JsonObject jsonObject = (JsonObject) JsonParser.parseString(widgetDto.getResponseText());

		WeatherRequestUtil.initWeatherSourceUniqueValues(weatherProviderType, true, context);

		CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude());
		List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(context, jsonObject,
				weatherProviderType, widgetDto.getLatitude(), widgetDto.getLongitude());
		AirQualityDto airQualityDto = AqicnResponseProcessor.parseTextToAirQualityDto(jsonObject);

		setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
				hourlyForecastDtoList, null, airQualityDto, null);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	@Override
	public void setResultViews(int appWidgetId, RemoteViews remoteViews, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader) {

		final WeatherProviderType mainWeatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet());

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				mainWeatherProviderType);
		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				mainWeatherProviderType);

		AirQualityDto airQualityDto = null;
		final boolean successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty();

		if (successful) {
			ZoneId zoneId = currentConditionsDto.getCurrentTime().getZone();
			ZoneOffset zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

			airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);

			setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
					hourlyForecastDtoList, null, airQualityDto, new OnDrawBitmapCallback() {

						@Override
						public void onCreatedBitmap(Bitmap bitmap) {

						}
					});
			makeResponseTextToJson(multipleRestApiDownloader, getRequestWeatherDataTypeSet(), widgetDto.getWeatherProviderTypeSet(), widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);
		super.setResultViews(appWidgetId, remoteViews, multipleRestApiDownloader);
	}

}
