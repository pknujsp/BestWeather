package com.lifedawn.bestweather.widget.widgetprovider;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.forremoteviews.SimpleWeatherDataProcessor;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.widget.WidgetHelper;
import com.lifedawn.bestweather.widget.creator.CurrentWidgetCreator;
import com.lifedawn.bestweather.widget.model.CurrentConditionsObj;
import com.lifedawn.bestweather.widget.model.WeatherDataObj;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CurrentWidgetProvider extends AbstractAppWidgetProvider {
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
			widgetHelper.cancelAutoRefresh(appWidgetId, CurrentWidgetProvider.class);
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
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			//위젯 자동 업데이트 재 등록
			widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
				@Override
				public void onResultSuccessful(List<WidgetDto> list) {
					WidgetHelper widgetHelper = new WidgetHelper(context);
					for (WidgetDto widgetDto : list) {
						if (widgetDto.getUpdateIntervalMillis() > 0) {
							widgetHelper.onSelectedAutoRefreshInterval(widgetDto.getUpdateIntervalMillis(), widgetDto.getAppWidgetId(),
									CurrentWidgetProvider.class);
						}
					}
				}

				@Override
				public void onResultNoData() {

				}
			});
		}
	}

	@Override
	protected void reDrawWidget(Context context, int appWidgetId) {
		CurrentWidgetCreator widgetViewCreator = new CurrentWidgetCreator(context, null, appWidgetId);
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
		CurrentWidgetCreator widgetViewCreator = new CurrentWidgetCreator(context, null, appWidgetId);
		widgetViewCreator.loadSavedSettings(new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto widgetDto) {
				final RemoteViews remoteViews = widgetViewCreator.createRemoteViews(false);

				WidgetHelper widgetHelper = new WidgetHelper(context);
				if (widgetDto.getUpdateIntervalMillis() > 0) {
					widgetHelper.onSelectedAutoRefreshInterval(widgetDto.getUpdateIntervalMillis(), appWidgetId, CurrentWidgetProvider.class);
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
	protected void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, WidgetDto widgetDto, WeatherSourceType requestWeatherSourceType, @Nullable @org.jetbrains.annotations.Nullable MultipleJsonDownloader multipleJsonDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		CurrentConditionsObj currentConditionsObj = null;

		ZoneId zoneId = null;
		CurrentWidgetCreator currentWidgetCreator = new CurrentWidgetCreator(context, null, appWidgetId);
		currentWidgetCreator.setWidgetDto(widgetDto);

		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
			currentConditionsObj = SimpleWeatherDataProcessor.getCurrentConditionsObj(context, requestWeatherSourceType,
					multipleJsonDownloader);
			if (currentConditionsObj.isSuccessful()) {
				widgetDto.setLastRefreshDateTime(multipleJsonDownloader.getLocalDateTime().toString());
				currentWidgetCreator.setCurrentConditionsViews(remoteViews, currentConditionsObj);
				currentWidgetCreator.setHeaderViews(remoteViews, widgetDto.getAddressName(), widgetDto.getLastRefreshDateTime());
				zoneId = ZoneId.of(currentConditionsObj.getTimeZoneId());

				currentWidgetCreator.setClockTimeZone(remoteViews);
				widgetDto.setTimeZoneId(zoneId.getId());
			}
		}
		boolean successful = currentConditionsObj.isSuccessful();

		WeatherDataObj weatherDataObj = new WeatherDataObj();
		weatherDataObj.setSuccessful(successful);
		widgetDto.setLoadSuccessful(successful);

		if (successful) {
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);
		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
			setRefreshPendingIntent(remoteViews, appWidgetId, context);
		}
		currentWidgetCreator.updateSettings(widgetDto, null);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	@Override
	Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.currentConditions);
		return set;
	}

	@Override
	Class<?> getThis() {
		return CurrentWidgetProvider.class;
	}
}
