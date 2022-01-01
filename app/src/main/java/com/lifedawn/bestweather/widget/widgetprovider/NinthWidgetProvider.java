package com.lifedawn.bestweather.widget.widgetprovider;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.NinthWidgetCreator;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NinthWidgetProvider extends AbstractAppWidgetProvider {
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);

	}

	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		NinthWidgetCreator widgetCreator = new NinthWidgetCreator(context, null, appWidgetId);
		widgetCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				if (result.getResponseText() != null) {
					widgetCreator.setDataViewsOfSavedData();
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@SuppressLint("UnsafeProtectedBroadcastReceiver")
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	@Override
	protected void reDrawWidget(Context context, int appWidgetId) {
		NinthWidgetCreator widgetViewCreator = new NinthWidgetCreator(context, null, appWidgetId);
		reDrawWidget(widgetViewCreator);

	}

	@Override
	protected void init(Context context, Bundle bundle) {
		final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		NinthWidgetCreator widgetViewCreator = new NinthWidgetCreator(context, null, appWidgetId);
		widgetViewCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto widgetDto) {
				final RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);
				WidgetHelper widgetHelper = new WidgetHelper(context, getClass());
				if (widgetDto.getUpdateIntervalMillis() > 0) {
					widgetHelper.onSelectedAutoRefreshInterval(widgetDto.getUpdateIntervalMillis(), appWidgetId);
				}

				if (networkStatus.networkAvailable()) {
					RemoteViewProcessor.onBeginProcess(remoteViews);
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

					final LocationType locationType = LocationType.valueOf(widgetDto.getLocationType());

					if (locationType == LocationType.CurrentLocation) {
						loadCurrentLocation(context, remoteViews, appWidgetId);
					} else {
						loadWeatherData(context, remoteViews, appWidgetId, widgetDto);
					}
				} else {
					RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.UNAVAILABLE_NETWORK);
					setRefreshPendingIntent(remoteViews, appWidgetId, context);
					appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
				}
			}

			@Override
			public void onResultNoData() {

			}
		});

	}

	@Override
	Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.hourlyForecast);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, Set<WeatherSourceType> requestWeatherSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		NinthWidgetCreator widgetCreator = new NinthWidgetCreator(context, null, appWidgetId);
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleRestApiDownloader.getRequestDateTime().toString());

		final List<HourlyForecastDto> hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(context, multipleRestApiDownloader,
				WeatherResponseProcessor.getMainWeatherSourceType(requestWeatherSourceTypeSet));
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
			widgetCreator.makeResponseTextToJson(multipleRestApiDownloader, requestWeatherDataTypeSet, requestWeatherSourceTypeSet, widgetDto, zoneOffset);
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
	}

	@Override
	Class<?> getThis() {
		return NinthWidgetProvider.class;
	}
}