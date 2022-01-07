package com.lifedawn.bestweather.weathers.dataprocessing.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.onecall.OneCallParameter;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class WeatherRequestUtil {
	public static void loadWeatherData(Context context, ExecutorService executorService, String countryCode, Double latitude, Double longitude,
	                                   Set<RequestWeatherDataType> requestWeatherDataTypeSet,
	                                   MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherDataSourceType> weatherDataSourceTypeSet) {
		final ArrayMap<WeatherDataSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		setRequestWeatherSourceWithSourceType(weatherDataSourceTypeSet, requestWeatherSources, requestWeatherDataTypeSet);

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, multipleRestApiDownloader);
			}
		});
	}

	public static void initWeatherSourceUniqueValues(WeatherDataSourceType weatherDataSourceType, boolean aqi, Context context) {
		switch (weatherDataSourceType) {
			case KMA_WEB:
				KmaResponseProcessor.init(context);
				break;
			case ACCU_WEATHER:
				AccuWeatherResponseProcessor.init(context);
				break;
			case OWM_ONECALL:
				OpenWeatherMapResponseProcessor.init(context);
				break;
		}
		if (aqi) {
			AqicnResponseProcessor.init(context);
		}
	}

	public static void setRequestWeatherSourceWithSourceType(Set<WeatherDataSourceType> weatherDataSourceTypeSet,
	                                                         ArrayMap<WeatherDataSourceType, RequestWeatherSource> requestWeatherSources,
	                                                         Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		if (weatherDataSourceTypeSet.contains(WeatherDataSourceType.KMA_API)) {
			RequestKma requestKma = new RequestKma();
			requestWeatherSources.put(WeatherDataSourceType.KMA_API, requestKma);

			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST);
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).addRequestServiceType(RetrofitClient.ServiceType.KMA_VILAGE_FCST);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_LAND_FCST).addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_TA_FCST)
						.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).addRequestServiceType(RetrofitClient.ServiceType.KMA_VILAGE_FCST);
			}
		} else if (weatherDataSourceTypeSet.contains(WeatherDataSourceType.KMA_WEB)) {
			RequestKma requestKma = new RequestKma();
			requestWeatherSources.put(WeatherDataSourceType.KMA_WEB, requestKma);

			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS);
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast) || requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
			}
		}

		if (weatherDataSourceTypeSet.contains(WeatherDataSourceType.ACCU_WEATHER)) {
			RequestAccu requestAccu = new RequestAccu();
			requestWeatherSources.put(WeatherDataSourceType.ACCU_WEATHER, requestAccu);
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);
			}
		}
		if (weatherDataSourceTypeSet.contains(WeatherDataSourceType.OWM_ONECALL)) {
			RequestOwmOneCall requestOwmOneCall = new RequestOwmOneCall();
			requestWeatherSources.put(WeatherDataSourceType.OWM_ONECALL, requestOwmOneCall);

			Set<OneCallParameter.OneCallApis> excludeSet = new HashSet<>();
			excludeSet.add(OneCallParameter.OneCallApis.daily);
			excludeSet.add(OneCallParameter.OneCallApis.hourly);
			excludeSet.add(OneCallParameter.OneCallApis.minutely);
			excludeSet.add(OneCallParameter.OneCallApis.alerts);
			excludeSet.add(OneCallParameter.OneCallApis.current);
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				excludeSet.remove(OneCallParameter.OneCallApis.current);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				excludeSet.remove(OneCallParameter.OneCallApis.hourly);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				excludeSet.remove(OneCallParameter.OneCallApis.daily);
			}
			requestOwmOneCall.setExcludeApis(excludeSet);
			requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		}
		if (weatherDataSourceTypeSet.contains(WeatherDataSourceType.OWM_INDIVIDUAL)) {
			RequestOwmIndividual requestOwmIndividual = new RequestOwmIndividual();
			requestWeatherSources.put(WeatherDataSourceType.OWM_INDIVIDUAL, requestOwmIndividual);
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);
			}
		}
		if (weatherDataSourceTypeSet.contains(WeatherDataSourceType.AQICN)) {
			RequestAqicn requestAqicn = new RequestAqicn();
			requestWeatherSources.put(WeatherDataSourceType.AQICN, requestAqicn);

			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.airQuality)) {
				requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
			}
		}
	}

	public static WeatherDataSourceType getMainWeatherSourceType(Context context, @Nullable String countryCode) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		WeatherDataSourceType mainWeatherDataSourceType = null;

		if (sharedPreferences.getBoolean(context.getString(R.string.pref_key_accu_weather), true)) {
			mainWeatherDataSourceType = WeatherDataSourceType.ACCU_WEATHER;
		} else {
			mainWeatherDataSourceType = WeatherDataSourceType.OWM_ONECALL;
		}

		if (countryCode != null) {
			if (countryCode.equals("KR")) {
				boolean kmaIsTopPriority = sharedPreferences.getBoolean(context.getString(R.string.pref_key_kma_top_priority), true);
				if (kmaIsTopPriority) {
					mainWeatherDataSourceType = WeatherDataSourceType.KMA_WEB;
				}
			}
		}

		return mainWeatherDataSourceType;
	}

}
