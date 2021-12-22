package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.ArrayMap;

import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;

import java.util.Set;

public class MainProcessing {

	public static MultipleRestApiDownloader requestNewWeatherData(Context context, Double latitude, Double longitude,
	                                                              ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                                              MultipleRestApiDownloader multipleRestApiDownloader) {
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

		multipleRestApiDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.KMA)) {
			KmaProcessing.requestWeatherData(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherSourceType.KMA), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.requestWeatherData(context, latitude, longitude,
					(RequestOwm) requestWeatherSources.get(WeatherSourceType.OPEN_WEATHER_MAP), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, multipleRestApiDownloader);
		}

		return multipleRestApiDownloader;
	}

	public static MultipleRestApiDownloader reRequestWeatherDataBySameWeatherSourceIfFailed(Context context, Double latitude, Double longitude,
	                                                                                        ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                                                                        MultipleRestApiDownloader multipleRestApiDownloader) {
		//실패한 데이터는 모두 삭제(aqicn 제외)
		Set<RetrofitClient.ServiceType> failedRequestServiceTypes = requestWeatherSources.valueAt(0).getRequestServiceTypes();
		ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> resultArrayMap =
				multipleRestApiDownloader.getResponseMap().get(requestWeatherSources.keyAt(0));

		for (RetrofitClient.ServiceType fail : failedRequestServiceTypes) {
			if (resultArrayMap.containsKey(fail)) {
				resultArrayMap.remove(fail);
			}
		}

		int totalResponseCount = multipleRestApiDownloader.getResponseCount();
		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalResponseCount -= requestWeatherSource.getRequestServiceTypes().size();
		}

		multipleRestApiDownloader.setResponseCount(totalResponseCount);

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.KMA)) {
			KmaProcessing.requestWeatherData(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherSourceType.KMA), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.requestWeatherData(context, latitude, longitude,
					(RequestOwm) requestWeatherSources.get(WeatherSourceType.OPEN_WEATHER_MAP), multipleRestApiDownloader);
		}
		return multipleRestApiDownloader;
	}

	public static MultipleRestApiDownloader reRequestWeatherDataByAnotherWeatherSourceIfFailed(Context context, Double latitude, Double longitude,
	                                                                                           ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                                                                           MultipleRestApiDownloader multipleRestApiDownloader) {
		multipleRestApiDownloader.getResponseMap().clear();
		multipleRestApiDownloader.setResponseCount(0);

		int totalRequestCount = 0;

		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}

		multipleRestApiDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.KMA)) {
			KmaProcessing.requestWeatherData(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherSourceType.KMA), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.requestWeatherData(context, latitude, longitude,
					(RequestOwm) requestWeatherSources.get(WeatherSourceType.OPEN_WEATHER_MAP), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, multipleRestApiDownloader);
		}

		return multipleRestApiDownloader;
	}


}