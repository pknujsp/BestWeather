package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.ArrayMap;

import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;

import java.util.Set;

public class MainProcessing {

	public static MultipleRestApiDownloader requestNewWeatherData(Context context, Double latitude, Double longitude,
	                                                              ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources,
	                                                              MultipleRestApiDownloader multipleRestApiDownloader) {
		int totalRequestCount = 0;
		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}

		if (requestWeatherSources.containsKey(WeatherProviderType.ACCU_WEATHER)) {
			//요청 좌표의 locationKey가 저장되어 있는지 확인
			if (AccuWeatherProcessing.getLocationKey(context, latitude, longitude).isEmpty()) {
				++totalRequestCount;
				requestWeatherSources.get(WeatherProviderType.ACCU_WEATHER).addRequestServiceType(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);
			}
		}

		multipleRestApiDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherProviderType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherProviderType.ACCU_WEATHER), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.KMA_WEB)) {
			KmaProcessing.requestWeatherDataAsWEB(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherProviderType.KMA_WEB),
					multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_ONECALL)) {
			OpenWeatherMapProcessing.requestWeatherDataOneCall(context, latitude, longitude,
					(RequestOwmOneCall) requestWeatherSources.get(WeatherProviderType.OWM_ONECALL), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
			OpenWeatherMapProcessing.requestWeatherDataIndividual(context, latitude, longitude,
					(RequestOwmIndividual) requestWeatherSources.get(WeatherProviderType.OWM_INDIVIDUAL), multipleRestApiDownloader);
		}

		if (requestWeatherSources.containsKey(WeatherProviderType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, multipleRestApiDownloader);
		}

		return multipleRestApiDownloader;
	}

	public static MultipleRestApiDownloader reRequestWeatherDataBySameWeatherSourceIfFailed(Context context, Double latitude, Double longitude,
	                                                                                        ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources,
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

		if (requestWeatherSources.containsKey(WeatherProviderType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherProviderType.ACCU_WEATHER), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.KMA_WEB)) {
			KmaProcessing.requestWeatherDataAsWEB(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherProviderType.KMA_WEB), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_ONECALL)) {
			OpenWeatherMapProcessing.requestWeatherDataOneCall(context, latitude, longitude,
					(RequestOwmOneCall) requestWeatherSources.get(WeatherProviderType.OWM_ONECALL), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
			OpenWeatherMapProcessing.requestWeatherDataIndividual(context, latitude, longitude,
					(RequestOwmIndividual) requestWeatherSources.get(WeatherProviderType.OWM_INDIVIDUAL), multipleRestApiDownloader);
		}

		return multipleRestApiDownloader;
	}

	public static MultipleRestApiDownloader reRequestWeatherDataByAnotherWeatherSourceIfFailed(Context context, Double latitude, Double longitude,
	                                                                                           ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources,
	                                                                                           MultipleRestApiDownloader multipleRestApiDownloader) {
		multipleRestApiDownloader.getResponseMap().clear();
		multipleRestApiDownloader.setResponseCount(0);

		int totalRequestCount = 0;

		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}

		multipleRestApiDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherProviderType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherProviderType.ACCU_WEATHER), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.KMA_WEB)) {
			KmaProcessing.requestWeatherDataAsWEB(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherProviderType.KMA_WEB), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_ONECALL)) {
			OpenWeatherMapProcessing.requestWeatherDataOneCall(context, latitude, longitude,
					(RequestOwmOneCall) requestWeatherSources.get(WeatherProviderType.OWM_ONECALL), multipleRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
			OpenWeatherMapProcessing.requestWeatherDataIndividual(context, latitude, longitude,
					(RequestOwmIndividual) requestWeatherSources.get(WeatherProviderType.OWM_INDIVIDUAL), multipleRestApiDownloader);
		}

		if (requestWeatherSources.containsKey(WeatherProviderType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, multipleRestApiDownloader);
		}

		return multipleRestApiDownloader;
	}


}