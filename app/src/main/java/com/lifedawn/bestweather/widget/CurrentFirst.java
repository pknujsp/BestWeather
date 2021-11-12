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
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import retrofit2.Response;

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

		MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				final WeatherSourceType requestWeatherSourceType = Objects.requireNonNull(widgetDataObjArrayMap.get(appWidgetId)).weatherSourceType;
				switch (requestWeatherSourceType) {
					case KMA:
						KmaResponseProcessor.init(context);
						break;
					case ACCU_WEATHER:
						AccuWeatherResponseProcessor.init(context);
						break;
					case OPEN_WEATHER_MAP:
						OpenWeatherMapResponseProcessor.init(context);
						break;
				}
				AqicnResponseProcessor.init(context);
				setResultViews(context, AppWidgetManager.getInstance(context), appWidgetId, remoteViews, this);
			}
		});
	}

	@Override
	public void setResultViews(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews remoteViews, @Nullable @org.jetbrains.annotations.Nullable MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		super.setResultViews(context, appWidgetManager, appWidgetId, remoteViews, multipleJsonDownloader);
		WeatherSourceType requestWeatherSourceType = Objects.requireNonNull(widgetDataObjArrayMap.get(appWidgetId)).weatherSourceType;

		CurrentConditionsObj currentConditionsObj = getCurrentConditions(context, requestWeatherSourceType, multipleJsonDownloader,
				appWidgetId);
		HeaderObj headerObj = getHeader(context, multipleJsonDownloader, appWidgetId, currentConditionsObj.timeZone);

		remoteViews.setTextViewText(R.id.address, headerObj.address);
		remoteViews.setTextViewText(R.id.refresh, headerObj.refreshDateTime);
		remoteViews.setTextViewText(R.id.current_temperature, currentConditionsObj.temp);
		if (currentConditionsObj.realFeelTemp == null) {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.GONE);
		} else {
			remoteViews.setViewVisibility(R.id.current_realfeel_temperature, View.VISIBLE);
			remoteViews.setTextViewText(R.id.current_realfeel_temperature,
					context.getString(R.string.real_feel_temperature) + ": " + currentConditionsObj.realFeelTemp);
		}
		remoteViews.setTextViewText(R.id.current_airquality, currentConditionsObj.airQuality);
		remoteViews.setTextViewText(R.id.current_precipitation, currentConditionsObj.precipitation);
		remoteViews.setImageViewResource(R.id.current_weather_icon, currentConditionsObj.weatherIcon);

		remoteViews.setViewVisibility(R.id.progressbar, View.GONE);
		remoteViews.setViewVisibility(R.id.content_container, View.VISIBLE);

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}
}