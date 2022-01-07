package com.lifedawn.bestweather.widget.jobservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.creator.EighthWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.EighthWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class EighthWidgetJobService extends AbstractWidgetJobService {

	@Override
	Class<?> getWidgetProviderClass() {
		return EighthWidgetProvider.class;
	}

	@Override
	void createWidgetViewCreator(int appWidgetId) {
		widgetViewCreator = new EighthWidgetCreator(getApplicationContext(), null, appWidgetId);
	}

	@Override
	Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.currentConditions);
		set.add(RequestWeatherDataType.hourlyForecast);
		set.add(RequestWeatherDataType.dailyForecast);
		set.add(RequestWeatherDataType.airQuality);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherDataSourceType> requestWeatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		EighthWidgetCreator widgetCreator = (EighthWidgetCreator) widgetViewCreator;
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		final WeatherDataSourceType mainWeatherDataSourceType = WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherDataSourceTypeSet);

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				mainWeatherDataSourceType);
		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				mainWeatherDataSourceType);
		final List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, multipleRestApiDownloader,
				mainWeatherDataSourceType);
		AirQualityDto airQualityDto = null;
		final boolean successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty() && !dailyForecastDtoList.isEmpty();

		if (successful) {
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());

			airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);
			if (airQualityDto == null) {
				airQualityDto = new AirQualityDto();
				airQualityDto.setAqi(-1);
			}

			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
					hourlyForecastDtoList, dailyForecastDtoList, airQualityDto, new OnDrawBitmapCallback() {
						@Override
						public void onCreatedBitmap(Bitmap bitmap) {
							widgetDto.setBitmap(bitmap);
						}
					});
			widgetCreator.makeResponseTextToJson(multipleRestApiDownloader, requestWeatherDataTypeSet, requestWeatherDataSourceTypeSet, widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);

		if (successful) {
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		} else {
			if (widgetDto.getBitmap() == null) {
				RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
				setRefreshPendingIntent(remoteViews, appWidgetId, context);
			} else {
				widgetCreator.drawBitmap(remoteViews, widgetDto.getBitmap());
			}
		}
		widgetCreator.updateSettings(widgetDto, null);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		super.setResultViews(context, appWidgetId, remoteViews, widgetDto, requestWeatherDataSourceTypeSet, multipleRestApiDownloader, requestWeatherDataTypeSet);
	}
}
