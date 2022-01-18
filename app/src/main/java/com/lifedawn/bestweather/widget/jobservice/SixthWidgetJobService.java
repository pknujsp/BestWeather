package com.lifedawn.bestweather.widget.jobservice;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
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
	AbstractWidgetCreator createWidgetViewCreator(int appWidgetId) {
		SixthWidgetCreator sixthWidgetCreator = new SixthWidgetCreator(getApplicationContext(), null, appWidgetId);
		widgetViewCreator = sixthWidgetCreator;
		return sixthWidgetCreator;
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
		SixthWidgetCreator widgetCreator = (SixthWidgetCreator) widgetViewCreator;
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleRestApiDownloader,
				WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherDataSourceTypeSet));
		final AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader, null);
		final boolean successful = currentConditionsDto != null && airQualityDto != null;

		if (successful) {
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());
			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), currentConditionsDto,
					airQualityDto, new OnDrawBitmapCallback() {
						@Override
						public void onCreatedBitmap(Bitmap bitmap) {
							widgetDto.setBitmap(bitmap);
						}
					});
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
