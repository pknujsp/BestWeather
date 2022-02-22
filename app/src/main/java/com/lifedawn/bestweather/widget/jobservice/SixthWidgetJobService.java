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
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.creator.SixthWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.SixthWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class SixthWidgetJobService extends AbstractWidgetJobService {

	@Override
	Class<?> getWidgetProviderClass() {
		return SixthWidgetProvider.class;
	}

	@Override
	SixthWidgetCreator createWidgetViewCreator(int appWidgetId, int jobId) {
		SixthWidgetCreator sixthWidgetCreator = new SixthWidgetCreator(getApplicationContext(), null, appWidgetId);
		widgetCreatorMap.put(jobId, sixthWidgetCreator);
		return sixthWidgetCreator;
	}

	@Override
	Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.currentConditions);
		set.add(WeatherDataType.airQuality);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherProviderType> requestWeatherProviderTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet, int jobId) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		SixthWidgetCreator widgetCreator = (SixthWidgetCreator) widgetCreatorMap.get(jobId);
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherProviderTypeSet));
		final AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader, null);
		final boolean successful = currentConditionsDto != null && airQualityDto.isSuccessful();

		if (successful) {
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
					airQualityDto, new OnDrawBitmapCallback() {
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
