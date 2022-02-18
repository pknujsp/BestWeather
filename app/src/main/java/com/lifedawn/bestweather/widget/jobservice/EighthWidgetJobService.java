package com.lifedawn.bestweather.widget.jobservice;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
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
	EighthWidgetCreator createWidgetViewCreator(int appWidgetId, int jobId) {
		EighthWidgetCreator eighthWidgetCreator = new EighthWidgetCreator(getApplicationContext(), null, appWidgetId);
		widgetCreatorMap.put(jobId, eighthWidgetCreator);

		return eighthWidgetCreator;
	}

	@Override
	Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.hourlyForecast);
		set.add(WeatherDataType.airQuality);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherDataSourceType> requestWeatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet, int jobId) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		EighthWidgetCreator widgetCreator = (EighthWidgetCreator) widgetCreatorMap.get(jobId);
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		final WeatherDataSourceType mainWeatherDataSourceType = WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherDataSourceTypeSet);

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				mainWeatherDataSourceType);
		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				mainWeatherDataSourceType);
		AirQualityDto airQualityDto = null;
		final boolean successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty();

		if (successful) {
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());

			airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);

			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
					hourlyForecastDtoList, null, airQualityDto, new OnDrawBitmapCallback() {
						@Override
						public void onCreatedBitmap(Bitmap bitmap) {

						}
					});
			widgetCreator.makeResponseTextToJson(multipleRestApiDownloader, weatherDataTypeSet, requestWeatherDataSourceTypeSet, widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);


		super.setResultViews(context, appWidgetId, remoteViews, widgetDto, requestWeatherDataSourceTypeSet, multipleRestApiDownloader, weatherDataTypeSet, jobId);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onStartJob(JobParameters params) {
		return super.onStartJob(params);
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		return super.onStopJob(params);
	}

	@Override
	public void loadCurrentLocation(Context context, int appWidgetId, RemoteViews remoteViews, int jobId) {
		super.loadCurrentLocation(context, appWidgetId, remoteViews, jobId);
	}

	@Override
	public void loadWeatherData(Context context, RemoteViews remoteViews, int appWidgetId, WidgetDto widgetDto, int jobId) {
		super.loadWeatherData(context, remoteViews, appWidgetId, widgetDto, jobId);
	}

	@Override
	protected void onActionBootCompleted(JobParameters jobParameters) {
		super.onActionBootCompleted(jobParameters);
	}
}
