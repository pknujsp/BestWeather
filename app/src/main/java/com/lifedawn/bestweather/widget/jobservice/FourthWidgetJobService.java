package com.lifedawn.bestweather.widget.jobservice;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FourthWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.FourthWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class FourthWidgetJobService extends AbstractWidgetJobService {

	@Override
	Class<?> getWidgetProviderClass() {
		return FourthWidgetProvider.class;
	}

	@Override
	FourthWidgetCreator createWidgetViewCreator(int appWidgetId, int jobId) {
		FourthWidgetCreator fourthWidgetCreator = new FourthWidgetCreator(getApplicationContext(), null, appWidgetId);
		widgetCreatorMap.put(jobId, fourthWidgetCreator);
		return fourthWidgetCreator;
	}

	@Override
	Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.currentConditions);
		set.add(RequestWeatherDataType.dailyForecast);
		set.add(RequestWeatherDataType.airQuality);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherDataSourceType> requestWeatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet, int jobId) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		FourthWidgetCreator widgetCreator = (FourthWidgetCreator) widgetCreatorMap.get(jobId);
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		final WeatherDataSourceType weatherDataSourceType = WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherDataSourceTypeSet);


		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				weatherDataSourceType);
		final List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, multipleRestApiDownloader,
				weatherDataSourceType);

		final boolean successful = currentConditionsDto != null && !dailyForecastDtoList.isEmpty();
		AirQualityDto airQualityDto = null;

		if (successful) {
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());

			airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);


			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto,
					currentConditionsDto, dailyForecastDtoList, new OnDrawBitmapCallback() {
						@Override
						public void onCreatedBitmap(Bitmap bitmap) {

						}
					});
			widgetCreator.makeResponseTextToJson(multipleRestApiDownloader, requestWeatherDataTypeSet, requestWeatherDataSourceTypeSet, widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);


		super.setResultViews(context, appWidgetId, remoteViews, widgetDto, requestWeatherDataSourceTypeSet, multipleRestApiDownloader, requestWeatherDataTypeSet, jobId);
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
