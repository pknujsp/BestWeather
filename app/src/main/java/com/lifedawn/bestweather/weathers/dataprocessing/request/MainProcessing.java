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

import java.util.Set;

public class MainProcessing {

	public static MultipleJsonDownloader<JsonElement> requestNewWeatherData(Context context, Double latitude, Double longitude,
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

		return multipleJsonDownloader;
	}

	public static MultipleJsonDownloader<JsonElement> reRequestWeatherDataBySameWeatherSourceIfFailed(Context context, Double latitude, Double longitude,
	                                                                                                  ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                                                                                  MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		//실패한 데이터는 모두 삭제(aqicn 제외)
		Set<RetrofitClient.ServiceType> failedRequestServiceTypes = requestWeatherSources.valueAt(0).getRequestServiceTypes();
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> resultArrayMap =
				multipleJsonDownloader.getResponseMap().get(requestWeatherSources.keyAt(0));

		for (RetrofitClient.ServiceType fail : failedRequestServiceTypes) {
			if (resultArrayMap.containsKey(fail)) {
				resultArrayMap.remove(fail);
			}
		}

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
		return multipleJsonDownloader;
	}

	public static MultipleJsonDownloader<JsonElement> reRequestWeatherDataByAnotherWeatherSourceIfFailed(Context context, Double latitude, Double longitude,
	                                                                                                     WeatherSourceType lastWeatherSourceType,
	                                                                                                     ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                                                                                     MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		//aqicn빼고 모두 삭제
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> aqicnResult
				= multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN);
		multipleJsonDownloader.getResponseMap().clear();
		multipleJsonDownloader.getResponseMap().put(WeatherSourceType.AQICN, aqicnResult);

		int totalRequestCount = 1;

		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}

		multipleJsonDownloader.setResponseCount(1);
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
		return multipleJsonDownloader;
	}


}