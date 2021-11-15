package com.lifedawn.bestweather.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.ArrayMap;
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
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;

import java.util.HashSet;
import java.util.Set;

public class WidgetCurrent extends RootAppWidget {

	@Override
	Class<?> getThisClass() {
		return getClass();
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	@Override
	public void onTimeTick(Context context) {

	}

	@Override
	public void loadWeatherData(Context context, AppWidgetManager appWidgetManager, RemoteViews remoteViews, int appWidgetId) {
		super.loadWeatherData(context, appWidgetManager, remoteViews, appWidgetId);
		SharedPreferences widgetAttributes =
				context.getSharedPreferences(ConfigureWidgetActivity.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId,
						Context.MODE_PRIVATE);
		final WeatherSourceType weatherSourceType =
				WeatherSourceType.valueOf(widgetAttributes.getString(ConfigureWidgetActivity.WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));
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

		Double latitude = Double.parseDouble(widgetAttributes.getString(WidgetDataKeys.LATITUDE.name(), "0.0"));
		Double longitude = Double.parseDouble(widgetAttributes.getString(WidgetDataKeys.LONGITUDE.name(), "0.0"));

		MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				initWeatherSourceUniqueValues(context, weatherSourceType);
				setResultViews(context, appWidgetId, remoteViews, appWidgetManager, this);
			}
		});
	}

	@Override
	void setResultViews(Context context, int appWidgetId, RemoteViews remoteViews, AppWidgetManager appWidgetManager, @Nullable @org.jetbrains.annotations.Nullable MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		SharedPreferences widgetAttributes =
				context.getSharedPreferences(ConfigureWidgetActivity.WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId,
						Context.MODE_PRIVATE);
		final WeatherSourceType requestWeatherSourceType =
				WeatherSourceType.valueOf(widgetAttributes.getString(ConfigureWidgetActivity.WidgetAttributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));

		CurrentConditionsObj currentConditionsObj = getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
				appWidgetId);
		HeaderObj headerObj = getHeader(context, multipleJsonDownloader, appWidgetId, currentConditionsObj.zoneId);

		remoteViews.setTextViewText(R.id.address, headerObj.address);
		remoteViews.setTextViewText(R.id.refresh, headerObj.refreshDateTime);
		remoteViews.setTextViewText(R.id.current_temperature, currentConditionsObj.temp);

		if (currentConditionsObj.realFeelTemp == null) {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.GONE);
		} else {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.VISIBLE);
			remoteViews.setTextViewText(R.id.current_realfeel_temperature,
					context.getString(R.string.real_feel_temperature_simple) + " : " + currentConditionsObj.realFeelTemp);
		}
		remoteViews.setTextViewText(R.id.current_airquality, currentConditionsObj.airQuality);
		remoteViews.setTextViewText(R.id.current_precipitation, currentConditionsObj.precipitation);
		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsObj.weatherIcon);

		setWatch(context, remoteViews, currentConditionsObj.zoneId);

		onSuccessfulProcess(remoteViews);
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}
}