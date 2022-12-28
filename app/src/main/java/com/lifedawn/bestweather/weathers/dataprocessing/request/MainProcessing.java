package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.ArrayMap;

import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestMet;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;

public class MainProcessing {

	public static WeatherRestApiDownloader requestNewWeatherData(Context context, Double latitude, Double longitude,
	                                                             ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources,
	                                                             WeatherRestApiDownloader weatherRestApiDownloader) {
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

		weatherRestApiDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherProviderType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherProviderType.ACCU_WEATHER), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.KMA_WEB)) {
			KmaProcessing.requestWeatherDataAsWEB(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherProviderType.KMA_WEB),
					weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.MET_NORWAY)) {
			MetNorwayProcessing.getMetNorwayForecasts(latitude.toString(), longitude.toString(),
					(RequestMet) requestWeatherSources.get(WeatherProviderType.MET_NORWAY), weatherRestApiDownloader, context);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_ONECALL)) {
			OpenWeatherMapProcessing.requestWeatherDataOneCall(context, latitude, longitude,
					(RequestOwmOneCall) requestWeatherSources.get(WeatherProviderType.OWM_ONECALL), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
			OpenWeatherMapProcessing.requestWeatherDataIndividual(context, latitude, longitude,
					(RequestOwmIndividual) requestWeatherSources.get(WeatherProviderType.OWM_INDIVIDUAL), weatherRestApiDownloader);
		}

		if (requestWeatherSources.containsKey(WeatherProviderType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, weatherRestApiDownloader, context);
		}

		return weatherRestApiDownloader;
	}

	public static WeatherRestApiDownloader reRequestWeatherDataBySameWeatherSourceIfFailed(Context context, Double latitude, Double longitude,
	                                                                                       ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources,
	                                                                                       WeatherRestApiDownloader weatherRestApiDownloader) {
		//실패한 데이터는 모두 삭제(aqicn 제외)
		weatherRestApiDownloader.getResponseMap().clear();
		weatherRestApiDownloader.setResponseCompleted(false);
		weatherRestApiDownloader.getCallMap().clear();
		weatherRestApiDownloader.setResponseCount(0);

		if (requestWeatherSources.containsKey(WeatherProviderType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherProviderType.ACCU_WEATHER), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.KMA_WEB)) {
			KmaProcessing.requestWeatherDataAsWEB(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherProviderType.KMA_WEB), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_ONECALL)) {
			OpenWeatherMapProcessing.requestWeatherDataOneCall(context, latitude, longitude,
					(RequestOwmOneCall) requestWeatherSources.get(WeatherProviderType.OWM_ONECALL), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
			OpenWeatherMapProcessing.requestWeatherDataIndividual(context, latitude, longitude,
					(RequestOwmIndividual) requestWeatherSources.get(WeatherProviderType.OWM_INDIVIDUAL), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.MET_NORWAY)) {
			MetNorwayProcessing.getMetNorwayForecasts(latitude.toString(), longitude.toString(),
					(RequestMet) requestWeatherSources.get(WeatherProviderType.MET_NORWAY), weatherRestApiDownloader, context);
		}

		if (requestWeatherSources.containsKey(WeatherProviderType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, weatherRestApiDownloader, context);
		}


		return weatherRestApiDownloader;
	}

	public static WeatherRestApiDownloader reRequestWeatherDataByAnotherWeatherSourceIfFailed(Context context, Double latitude, Double longitude,
	                                                                                          ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources,
	                                                                                          WeatherRestApiDownloader weatherRestApiDownloader) {
		weatherRestApiDownloader.getResponseMap().clear();
		weatherRestApiDownloader.setResponseCount(0);

		int totalRequestCount = 0;

		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}

		weatherRestApiDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherProviderType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherProviderType.ACCU_WEATHER), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.KMA_WEB)) {
			KmaProcessing.requestWeatherDataAsWEB(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherProviderType.KMA_WEB), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_ONECALL)) {
			OpenWeatherMapProcessing.requestWeatherDataOneCall(context, latitude, longitude,
					(RequestOwmOneCall) requestWeatherSources.get(WeatherProviderType.OWM_ONECALL), weatherRestApiDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
			OpenWeatherMapProcessing.requestWeatherDataIndividual(context, latitude, longitude,
					(RequestOwmIndividual) requestWeatherSources.get(WeatherProviderType.OWM_INDIVIDUAL), weatherRestApiDownloader);
		}

		if (requestWeatherSources.containsKey(WeatherProviderType.MET_NORWAY)) {
			MetNorwayProcessing.getMetNorwayForecasts(latitude.toString(), longitude.toString(),
					(RequestMet) requestWeatherSources.get(WeatherProviderType.MET_NORWAY), weatherRestApiDownloader, context);
		}

		if (requestWeatherSources.containsKey(WeatherProviderType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, weatherRestApiDownloader, context);
		}

		return weatherRestApiDownloader;
	}


}