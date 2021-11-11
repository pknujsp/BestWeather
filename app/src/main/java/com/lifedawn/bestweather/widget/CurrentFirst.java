package com.lifedawn.bestweather.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class CurrentFirst extends RootAppWidget {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

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
		final String action = intent.getAction();

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName componentName = new ComponentName(context.getPackageName(), CurrentFirst.class.getName());
		int[] widgetIds = appWidgetManager.getAppWidgetIds(componentName);

		if (action.equals(context.getString(R.string.ACTION_REFRESH))) {
			for (int appWidgetId : widgetIds) {
				RemoteViews remoteViews = createViews(context, R.layout.current_first);
				remoteViews.setOnClickPendingIntent(R.id.content_container, getOnClickedPendingIntent(context, appWidgetId,
						CurrentFirst.class));

				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
				loadWeatherData(context, appWidgetId, remoteViews);
			}
		} else if (action.equals(context.getString(R.string.ACTION_SHOW_DIALOG))) {
			Log.e(tag, "show dialog");
			Intent i = new Intent(context, DialogActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtras(intent.getExtras());
			context.startActivity(i);
		} else if (action.equals(context.getString(R.string.ACTION_INIT))) {

		}
	}

	@Override
	public void loadWeatherData(Context context, int appWidgetId, RemoteViews remoteViews) {
		super.loadWeatherData(context, appWidgetId, remoteViews);
		WeatherSourceType weatherSourceType = widgetDataObjArrayMap.get(appWidgetId).weatherSourceType;
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources =
				makeRequestWeatherSources(weatherSourceType);

		if (weatherSourceType == WeatherSourceType.KMA) {
			RequestKma requestKma = (RequestKma) requestWeatherSources.get(weatherSourceType);
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			RequestAccu requestAccu = (RequestAccu) requestWeatherSources.get(weatherSourceType);
			requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			RequestOwm requestOwm = (RequestOwm) requestWeatherSources.get(weatherSourceType);
			Set<OneCallParameter.OneCallApis> excludeSet = new HashSet<>();
			excludeSet.add(OneCallParameter.OneCallApis.daily);
			excludeSet.add(OneCallParameter.OneCallApis.hourly);
			excludeSet.add(OneCallParameter.OneCallApis.minutely);
			excludeSet.add(OneCallParameter.OneCallApis.alerts);

			requestOwm.setExcludeApis(excludeSet);
			requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		}

		RequestAqicn requestAqicn = new RequestAqicn();
		requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		requestWeatherSources.put(WeatherSourceType.AQICN, requestAqicn);

		Double latitude = null;
		Double longitude = null;

		if (widgetDataObjArrayMap.get(appWidgetId).locationType == LocationType.CurrentLocation) {
			latitude = widgetDataObjArrayMap.get(appWidgetId).latitude;
			longitude = widgetDataObjArrayMap.get(appWidgetId).longitude;
		} else {
			FavoriteAddressDto favoriteAddressDto = widgetDataObjArrayMap.get(appWidgetId).selectedAddressDto;
			latitude = Double.parseDouble(favoriteAddressDto.getLatitude());
			longitude = Double.parseDouble(favoriteAddressDto.getLongitude());
		}

		Log.e(tag, "데이터 요청");
		MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				setResultViews(context, AppWidgetManager.getInstance(context), appWidgetId, remoteViews, this);
			}
		});
	}

	@Override
	public void setResultViews(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews remoteViews, @Nullable @org.jetbrains.annotations.Nullable MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		super.setResultViews(context, appWidgetManager, appWidgetId, remoteViews, multipleJsonDownloader);
		OpenWeatherMapResponseProcessor.init(context);

		OneCallResponse oneCallResponse =
				OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP)
						.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().body().toString());

		LocalDateTime updatedTime = multipleJsonDownloader.getLocalDateTime();

		remoteViews.setTextViewText(R.id.address, widgetDataObjArrayMap.get(appWidgetId).addressName);
		remoteViews.setTextViewText(R.id.refresh, updatedTime.format(DATE_TIME_FORMATTER));
		remoteViews.setTextViewText(R.id.current_temperature, oneCallResponse.getCurrent().getTemp());
		remoteViews.setTextViewText(R.id.current_realfeel_temperature, oneCallResponse.getCurrent().getFeelsLike());
		remoteViews.setTextViewText(R.id.current_precipitation, oneCallResponse.getCurrent().getRain() != null
				? oneCallResponse.getCurrent().getRain().getPrecipitation1Hour() : context.getString(R.string.not_precipitation));
		remoteViews.setImageViewResource(R.id.current_weather_icon, OpenWeatherMapResponseProcessor.getWeatherIconImg(
				oneCallResponse.getCurrent().getWeather().get(0).getId(), false));
		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
		remoteViews.setViewVisibility(R.id.content_container, View.VISIBLE);

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}
}