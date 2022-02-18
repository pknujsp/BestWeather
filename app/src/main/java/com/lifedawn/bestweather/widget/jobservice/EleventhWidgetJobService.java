package com.lifedawn.bestweather.widget.jobservice;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.ArrayMap;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.creator.EleventhWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.EleventhWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class EleventhWidgetJobService extends AbstractWidgetJobService {

	@Override
	Class<?> getWidgetProviderClass() {
		return EleventhWidgetProvider.class;
	}

	@Override
	EleventhWidgetCreator createWidgetViewCreator(int appWidgetId, int jobId) {
		EleventhWidgetCreator eleventhWidgetCreator = new EleventhWidgetCreator(getApplicationContext(), null, appWidgetId);
		widgetCreatorMap.put(jobId, eleventhWidgetCreator);
		return eleventhWidgetCreator;
	}

	@Override
	Set<WeatherDataType> getRequestWeatherDataTypeSet() {
		Set<WeatherDataType> set = new HashSet<>();
		set.add(WeatherDataType.hourlyForecast);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherDataSourceType> requestWeatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataType> weatherDataTypeSet, int jobId) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		EleventhWidgetCreator widgetCreator = (EleventhWidgetCreator) widgetCreatorMap.get(jobId);
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		ArrayMap<WeatherDataSourceType, List<HourlyForecastDto>> weatherSourceTypeListArrayMap = new ArrayMap<>();
		boolean successful = true;

		for (WeatherDataSourceType weatherDataSourceType : requestWeatherDataSourceTypeSet) {
			weatherSourceTypeListArrayMap.put(weatherDataSourceType, WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
					weatherDataSourceType));

			if (weatherSourceTypeListArrayMap.get(weatherDataSourceType).isEmpty()) {
				successful = false;
			}
		}

		if (successful) {
			zoneId = weatherSourceTypeListArrayMap.valueAt(0).get(0).getHours().getZone();
			zoneOffset = weatherSourceTypeListArrayMap.valueAt(0).get(0).getHours().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), weatherSourceTypeListArrayMap,
					new OnDrawBitmapCallback() {
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
