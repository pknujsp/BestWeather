package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.ArrayMap;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;

public class MainProcessing {

	public static void requestWeatherData(Context context, Double latitude, Double longitude,
	                                      ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                      MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		int totalRequestCount = 0;
		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			//요청 좌표의 locationKey가 저장되어 있는지 확인
			if (AccuWeatherProcessing.getLocationKey(context, latitude, longitude).isEmpty()) {
				++totalRequestCount;
				requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER).addRequestServiceType(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);
			}
		}

		multipleJsonDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.KMA)) {
			KmaProcessing.requestWeatherData(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherSourceType.KMA), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.requestWeatherData(context, latitude, longitude,
					(RequestOwm) requestWeatherSources.get(WeatherSourceType.OPEN_WEATHER_MAP), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, multipleJsonDownloader);
		}
	}

	public static void reRequestWeatherDataBySameWeatherSource(Context context, Double latitude, Double longitude,
	                                                           ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                                           MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		int totalResponseCount = multipleJsonDownloader.getResponseCount();
		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalResponseCount -= requestWeatherSource.getRequestServiceTypes().size();
		}

		multipleJsonDownloader.setResponseCount(totalResponseCount);

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.KMA)) {
			KmaProcessing.requestWeatherData(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherSourceType.KMA), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.requestWeatherData(context, latitude, longitude,
					(RequestOwm) requestWeatherSources.get(WeatherSourceType.OPEN_WEATHER_MAP), multipleJsonDownloader);
		}
	}

	public static void reRequestWeatherDataByAnotherWeatherSource(Context context, Double latitude, Double longitude,
	                                                              WeatherSourceType lastWeatherSourceType,
	                                                              ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                                              MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		//이전 날씨 제공사 응답 삭제
		int totalResponseCount = multipleJsonDownloader.getResponseCount() - multipleJsonDownloader.getResponseMap().get(lastWeatherSourceType).size();
		int totalRequestCount =
				multipleJsonDownloader.getRequestCount() - multipleJsonDownloader.getResponseMap().get(lastWeatherSourceType).size();

		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}

		multipleJsonDownloader.setResponseCount(totalResponseCount);
		multipleJsonDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.KMA)) {
			KmaProcessing.requestWeatherData(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherSourceType.KMA), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.requestWeatherData(context, latitude, longitude,
					(RequestOwm) requestWeatherSources.get(WeatherSourceType.OPEN_WEATHER_MAP), multipleJsonDownloader);
		}
	}


}