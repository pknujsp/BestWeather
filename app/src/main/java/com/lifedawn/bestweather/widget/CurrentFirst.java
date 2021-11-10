package com.lifedawn.bestweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class CurrentFirst extends AppWidgetProvider {
	static final String tag = "appWidget";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			setViews(context, appWidgetId, null);
		}
	}

	@Override
	public void onEnabled(Context context) {
	}

	@Override
	public void onDisabled(Context context) {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName widget = new ComponentName(context.getPackageName(), CurrentFirst.class.getName());
		int[] widgetIds = appWidgetManager.getAppWidgetIds(widget);

		if (action.equals(context.getString(R.string.ACTION_REFRESH))) {
			onUpdate(context, appWidgetManager, widgetIds);
		} else if (action.equals(context.getString(R.string.ACTION_SHOW_DIALOG))) {
			Intent i = new Intent(context, DialogActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

	private static void retrieveCurrentConditions(Context context, int appWidgetId) {
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
				setViews(context, appWidgetId, this);
			}
		});
	}

	public static PendingIntent getClickedPendingIntent(Context context) {
		Intent intent = new Intent(context, CurrentFirst.class);
		intent.setAction(context.getString(R.string.ACTION_SHOW_DIALOG));

		return PendingIntent.getBroadcast(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private static void setViews(Context context,
	                             int appWidgetId, @Nullable MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.current_first);

		/*
		Intent intentSync = new Intent(context, CurrentFirst.class);
		intentSync.setAction(context.getString(R.string.ACTION_REFRESH));
		PendingIntent pendingSync = PendingIntent.getBroadcast(context, 0, intentSync, 0);
		remoteViews.setOnClickPendingIntent(R.id.refresh, pendingSync);

		 */

		remoteViews.setOnClickPendingIntent(R.id.content_container,
				getClickedPendingIntent(context));

		if (multipleJsonDownloader == null) {
			Log.e(tag, "데이터 요청");
			remoteViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.content_container, View.GONE);
			retrieveCurrentConditions(context, appWidgetId);
		} else {
			Log.e(tag, "데이터 응답완료");
			OpenWeatherMapResponseProcessor.init(context);

			OneCallResponse oneCallResponse =
					OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP)
							.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().body().toString());

			LocalDateTime updatedTime = multipleJsonDownloader.getLocalDateTime();
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E HH:mm");

			remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
			remoteViews.setViewVisibility(R.id.content_container, View.VISIBLE);

			remoteViews.setTextViewText(R.id.refresh, updatedTime.format(dateTimeFormatter));
			remoteViews.setTextViewText(R.id.temperature, oneCallResponse.getCurrent().getTemp());
			remoteViews.setTextViewText(R.id.realfeel_temperature, oneCallResponse.getCurrent().getFeelsLike());
			remoteViews.setTextViewText(R.id.precipitation, oneCallResponse.getCurrent().getRain() != null
					? oneCallResponse.getCurrent().getRain().getPrecipitation1Hour() : context.getString(R.string.not_precipitation));
			remoteViews.setImageViewResource(R.id.weather_icon, OpenWeatherMapResponseProcessor.getWeatherIconImg(
					oneCallResponse.getCurrent().getWeather().get(0).getId(), false));
			OpenWeatherMapResponseProcessor.init(context);

		}

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}
}