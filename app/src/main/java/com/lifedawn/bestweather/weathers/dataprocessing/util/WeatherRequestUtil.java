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
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestMet;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.onecall.OneCallParameter;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.MetNorwayResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class WeatherRequestUtil {
	public static void loadWeatherData(Context context, ExecutorService executorService, Double latitude, Double longitude,
	                                   Set<WeatherDataType> weatherDataTypeSet,
	                                   MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherProviderType> weatherProviderTypeSet) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				final ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
				setRequestWeatherSourceWithSourceType(weatherProviderTypeSet, requestWeatherSources, weatherDataTypeSet);
				MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, multipleRestApiDownloader);
			}
		});
	}

	public static void initWeatherSourceUniqueValues(WeatherProviderType weatherProviderType, boolean aqi, Context context) {
		switch (weatherProviderType) {
			case KMA_WEB:
				KmaResponseProcessor.init(context);
				break;
			case ACCU_WEATHER:
				AccuWeatherResponseProcessor.init(context);
				break;
			case OWM_ONECALL:
				OpenWeatherMapResponseProcessor.init(context);
				break;
			case MET_NORWAY:
				MetNorwayResponseProcessor.init(context);
				break;
		}
		if (aqi) {
			AqicnResponseProcessor.init(context);
		}
	}

	public static void setRequestWeatherSourceWithSourceType(Set<WeatherProviderType> weatherProviderTypeSet,
	                                                         ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources,
	                                                         Set<WeatherDataType> weatherDataTypeSet) {
		if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_API)) {
			RequestKma requestKma = new RequestKma();
			requestWeatherSources.put(WeatherProviderType.KMA_API, requestKma);

			if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST);
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).addRequestServiceType(RetrofitClient.ServiceType.KMA_VILAGE_FCST);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_LAND_FCST).addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_TA_FCST)
						.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).addRequestServiceType(RetrofitClient.ServiceType.KMA_VILAGE_FCST);
			}
		} else if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			RequestKma requestKma = new RequestKma();
			requestWeatherSources.put(WeatherProviderType.KMA_WEB, requestKma);

			if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS);
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast) || weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
			}
		}

		if (weatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
			RequestAccu requestAccu = new RequestAccu();
			requestWeatherSources.put(WeatherProviderType.ACCU_WEATHER, requestAccu);
			if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);
			}
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
			RequestOwmOneCall requestOwmOneCall = new RequestOwmOneCall();
			requestWeatherSources.put(WeatherProviderType.OWM_ONECALL, requestOwmOneCall);

			Set<OneCallParameter.OneCallApis> excludeSet = new HashSet<>();
			excludeSet.add(OneCallParameter.OneCallApis.daily);
			excludeSet.add(OneCallParameter.OneCallApis.hourly);
			excludeSet.add(OneCallParameter.OneCallApis.minutely);
			excludeSet.add(OneCallParameter.OneCallApis.alerts);
			excludeSet.add(OneCallParameter.OneCallApis.current);
			if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
				excludeSet.remove(OneCallParameter.OneCallApis.current);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
				excludeSet.remove(OneCallParameter.OneCallApis.hourly);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
				excludeSet.remove(OneCallParameter.OneCallApis.daily);
			}
			requestOwmOneCall.setExcludeApis(excludeSet);
			requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		}

		if (weatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
			RequestMet requestMet = new RequestMet();
			requestWeatherSources.put(WeatherProviderType.MET_NORWAY, requestMet);

			requestMet.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		}

		if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_INDIVIDUAL)) {
			RequestOwmIndividual requestOwmIndividual = new RequestOwmIndividual();
			requestWeatherSources.put(WeatherProviderType.OWM_INDIVIDUAL, requestOwmIndividual);
			if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
				requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
				requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST);
			}
			if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
				requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);
			}
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.AQICN)) {
			RequestAqicn requestAqicn = new RequestAqicn();
			requestWeatherSources.put(WeatherProviderType.AQICN, requestAqicn);

			if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
				requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
			}
		}
	}

	public static WeatherProviderType getMainWeatherSourceType(Context context, @Nullable String countryCode) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		WeatherProviderType mainWeatherProviderType = null;

		if (sharedPreferences.getBoolean(context.getString(R.string.pref_key_met), true)) {
			mainWeatherProviderType = WeatherProviderType.MET_NORWAY;
		} else {
			mainWeatherProviderType = WeatherProviderType.OWM_ONECALL;
		}

		if (countryCode != null) {
			if (countryCode.equals("KR")) {
				boolean kmaIsTopPriority = sharedPreferences.getBoolean(context.getString(R.string.pref_key_kma_top_priority), true);
				if (kmaIsTopPriority) {
					mainWeatherProviderType = WeatherProviderType.KMA_WEB;
				}
			}
		}

		return mainWeatherProviderType;
	}

}
