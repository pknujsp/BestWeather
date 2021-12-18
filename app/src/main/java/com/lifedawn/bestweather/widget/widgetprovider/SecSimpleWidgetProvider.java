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
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.widget.OnDrawBitmapCallback;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.SecSimpleWidgetCreator;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecSimpleWidgetProvider extends AbstractAppWidgetProvider {

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
		WidgetHelper widgetHelper = new WidgetHelper(context);
		for (int appWidgetId : appWidgetIds) {
			widgetHelper.cancelAutoRefresh(appWidgetId);
		}
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
		SecSimpleWidgetCreator widgetViewCreator = new SecSimpleWidgetCreator(context, null, appWidgetId);
		widgetViewCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto widgetDto) {
				if (widgetDto == null) {
					return;
				}

				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);

						if (widgetDto.isLoadSuccessful()) {
							loadCurrentLocation(context, remoteViews, appWidgetId);
						} else {
							remoteViews.setOnClickPendingIntent(R.id.warning_process_btn, widgetViewCreator.getOnClickedPendingIntent(remoteViews));
							RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
							appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
						}

					}
				});
			}

			@Override
			public void onResultNoData() {

			}
		});

	}

	@Override
	protected void init(Context context, Bundle bundle) {
		final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		SecSimpleWidgetCreator widgetViewCreator = new SecSimpleWidgetCreator(context, null, appWidgetId);
		widgetViewCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto widgetDto) {
				final RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);
				WidgetHelper widgetHelper = new WidgetHelper(context);
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
		set.add(RequestWeatherDataType.currentConditions);
		set.add(RequestWeatherDataType.dailyForecast);
		set.add(RequestWeatherDataType.airQuality);

		return set;
	}

	@Override
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, WeatherSourceType requestWeatherSourceType, @Nullable @org.jetbrains.annotations.Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		ZoneId zoneId = null;
		ZoneOffset zoneOffset = null;
		SecSimpleWidgetCreator widgetCreator = new SecSimpleWidgetCreator(context, null, appWidgetId);
		widgetCreator.setWidgetDto(widgetDto);
		widgetDto.setLastRefreshDateTime(multipleJsonDownloader.getRequestDateTime().toString());

		final CurrentConditionsDto currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(context, multipleJsonDownloader,
				requestWeatherSourceType);
		final List<DailyForecastDto> dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(context, multipleJsonDownloader,
				requestWeatherSourceType);

		final boolean successful = currentConditionsDto != null && !dailyForecastDtoList.isEmpty();
		AirQualityDto airQualityDto = null;

		if (successful) {
			zoneId = currentConditionsDto.getCurrentTime().getZone();
			zoneOffset = currentConditionsDto.getCurrentTime().getOffset();
			widgetDto.setTimeZoneId(zoneId.getId());

			airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleJsonDownloader,
					requestWeatherSourceType, zoneOffset);
			if (airQualityDto == null) {
				airQualityDto = new AirQualityDto();
				airQualityDto.setAqi(-1);
			}

			widgetCreator.setDataViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime(), airQualityDto,
					currentConditionsDto, dailyForecastDtoList, new OnDrawBitmapCallback() {
						@Override
						public void onCreatedBitmap(Bitmap bitmap) {
							widgetDto.setBitmap(bitmap);
						}
					});
			widgetCreator.makeResponseTextToJson(multipleJsonDownloader, requestWeatherDataTypeSet, requestWeatherSourceType, widgetDto, zoneOffset);


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
		return SecSimpleWidgetProvider.class;
	}
}