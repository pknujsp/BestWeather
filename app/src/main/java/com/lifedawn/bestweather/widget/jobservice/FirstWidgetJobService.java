package com.lifedawn.bestweather.widget.jobservice;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.content.Context;
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
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FirstWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.FirstWidgetProvider;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class FirstWidgetJobService extends AbstractWidgetJobService {

	@Override
	AbstractWidgetCreator createWidgetViewCreator(int appWidgetId) {
		FirstWidgetCreator firstWidgetCreator = new FirstWidgetCreator(getApplicationContext(), null, appWidgetId);
		widgetViewCreator = firstWidgetCreator;
		return firstWidgetCreator;
	}

	@Override
	public boolean onStartJob(JobParameters params) {
		return super.onStartJob(params);
	}

	@Override
	Class<?> getWidgetProviderClass() {
		return FirstWidgetProvider.class;
	}

	@Override
	Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.currentConditions);
		set.add(RequestWeatherDataType.airQuality);
		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherDataSourceType> requestWeatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		FirstWidgetCreator widgetCreator = (FirstWidgetCreator) widgetViewCreator;
		widgetCreator.setWidgetDto(widgetDto);

		final WeatherDataSourceType weatherDataSourceType = WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherDataSourceTypeSet);
		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				weatherDataSourceType);
		AirQualityDto airQualityDto = null;
		boolean successful = currentConditionsDto != null;

		if (successful) {
			widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();

			widgetDto.setTimeZoneId(zoneId.getId());
			widgetCreator.setClockTimeZone(remoteViews);

			airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader,
					zoneOffset);
			if (airQualityDto == null) {
				airQualityDto = new AirQualityDto();
				airQualityDto.setAqi(-1);
			}

			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto,
					currentConditionsDto, null);

			widgetCreator.makeResponseTextToJson(multipleRestApiDownloader, requestWeatherDataTypeSet, requestWeatherDataSourceTypeSet, widgetDto, zoneOffset);
		}

		widgetDto.setLoadSuccessful(successful);

		if (successful) {
			RemoteViewsUtil.onSuccessfulProcess(remoteViews);
		} else {
			if (widgetDto.getBitmap() == null) {
				RemoteViewsUtil.onErrorProcess(remoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA);
				setRefreshPendingIntent(remoteViews, appWidgetId);
			} else {
				widgetCreator.drawBitmap(remoteViews, widgetDto.getBitmap());
			}
		}

		widgetCreator.updateSettings(widgetDto, null);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		super.setResultViews(context, appWidgetId, remoteViews, widgetDto, requestWeatherDataSourceTypeSet, multipleRestApiDownloader, requestWeatherDataTypeSet);
	}
}
