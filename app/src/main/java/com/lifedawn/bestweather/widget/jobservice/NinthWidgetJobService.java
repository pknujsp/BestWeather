package com.lifedawn.bestweather.widget.jobservice;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.creator.NinthWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.NinthWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class NinthWidgetJobService extends AbstractWidgetJobService {

	@Override
	Class<?> getWidgetProviderClass() {
		return NinthWidgetProvider.class;
	}

	@Override
	NinthWidgetCreator createWidgetViewCreator(int appWidgetId, int jobId) {
		NinthWidgetCreator ninthWidgetCreator = new NinthWidgetCreator(getApplicationContext(), null, appWidgetId);
		widgetCreatorMap.put(jobId, ninthWidgetCreator);
		return ninthWidgetCreator;
	}

	@Override
	Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.hourlyForecast);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherProviderType> requestWeatherProviderTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet, int jobId) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		NinthWidgetCreator widgetCreator = (NinthWidgetCreator) widgetCreatorMap.get(jobId);
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherProviderTypeSet));
		final boolean successful = !hourlyForecastDtoList.isEmpty();

		if (successful) {
			zoneId = hourlyForecastDtoList.get(0).getHours().getZone();
			zoneOffset = hourlyForecastDtoList.get(0).getHours().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(),
					hourlyForecastDtoList, new OnDrawBitmapCallback() {
						@Override
						public void onCreatedBitmap(Bitmap bitmap) {

						}
					});
			widgetCreator.makeResponseTextToJson(multipleRestApiDownloader, weatherDataTypeSet, requestWeatherProviderTypeSet, widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);

		super.setResultViews(context, appWidgetId, remoteViews, widgetDto, requestWeatherProviderTypeSet, multipleRestApiDownloader, weatherDataTypeSet, jobId);
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
