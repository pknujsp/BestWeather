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
	void createWidgetViewCreator(int appWidgetId) {
		widgetViewCreator = new NinthWidgetCreator(getApplicationContext(), null, appWidgetId);
	}

	@Override
	Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.hourlyForecast);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherDataSourceType> requestWeatherDataSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		NinthWidgetCreator widgetCreator = (NinthWidgetCreator) widgetViewCreator;
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherDataSourceTypeSet));
		final boolean successful = !hourlyForecastDtoList.isEmpty();

		if (successful) {
			zoneId = hourlyForecastDtoList.get(0).getHours().getZone();
			zoneOffset = hourlyForecastDtoList.get(0).getHours().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(),
					hourlyForecastDtoList, new OnDrawBitmapCallback() {
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
