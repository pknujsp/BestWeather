package com.lifedawn.bestweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.ArrayMap;
import android.widget.RemoteViews;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;

import java.util.HashSet;
import java.util.Set;

public class CurrentFirst extends AppWidgetProvider {

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
	                            int appWidgetId) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.current_first);

		Intent intentSync = new Intent(context, CurrentFirst.class);
		intentSync.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		PendingIntent pendingSync = PendingIntent.getBroadcast(context, 0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.updated_datetime, pendingSync);

		retrieveCurrentConditions(context, views, appWidgetId);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onEnabled(Context context) {
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		// Enter relevant functionality for when the last widget is disabled
	}

	private static void retrieveCurrentConditions(Context context, RemoteViews remoteViews, int appWidgetId) {
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		RequestOwm requestOwm = new RequestOwm();
		Set<OneCallParameter.OneCallApis> excludeSet = new HashSet<>();
		excludeSet.add(OneCallParameter.OneCallApis.daily);
		excludeSet.add(OneCallParameter.OneCallApis.hourly);
		excludeSet.add(OneCallParameter.OneCallApis.minutely);
		excludeSet.add(OneCallParameter.OneCallApis.alerts);

		requestOwm.setExcludeApis(excludeSet);
		requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		requestWeatherSources.put(WeatherSourceType.OPEN_WEATHER_MAP, requestOwm);

		Double latitude = 35.235421;
		Double longitude = 128.868227;

		MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				ArrayMap<RetrofitClient.ServiceType, ResponseResult<JsonElement>> responseMap =
						getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP);

				ResponseResult<JsonElement> oneCallResponse = responseMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

				remoteViews.setTextViewText(R.id.updated_datetime, "업데이트 테스트");
				AppWidgetManager manager = AppWidgetManager.getInstance(context);
				manager.updateAppWidget(appWidgetId, remoteViews);
			}
		});
	}
}