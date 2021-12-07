package com.lifedawn.bestweather.weathers.dataprocessing.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class WeatherRequestUtil {
	public static void loadWeatherData(Context context, ExecutorService executorService, String countryCode, Double latitude, Double longitude,
	                                   Set<RequestWeatherDataType> requestWeatherDataTypeSet, MultipleJsonDownloader multipleJsonDownloader) {
		final WeatherSourceType requestWeatherSourceType = getMainWeatherSourceType(context, countryCode);
		final ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		setRequestWeatherSourceWithSourceType(requestWeatherSourceType, requestWeatherSources, requestWeatherDataTypeSet);

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, multipleJsonDownloader);
			}
		});
	}


	public static void setRequestWeatherSourceWithSourceType(WeatherSourceType weatherSourceType,
	                                                         ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                                         Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		if (weatherSourceType == WeatherSourceType.KMA) {
			RequestKma requestKma = new RequestKma();
			requestWeatherSources.put(weatherSourceType, requestKma);

			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_FCST).addRequestServiceType(RetrofitClient.ServiceType.VILAGE_FCST);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.MID_LAND_FCST).addRequestServiceType(RetrofitClient.ServiceType.MID_TA_FCST)
						.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_FCST).addRequestServiceType(RetrofitClient.ServiceType.VILAGE_FCST);
			}
		}
		if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			RequestAccu requestAccu = new RequestAccu();
			requestWeatherSources.put(weatherSourceType, requestAccu);
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.currentConditions)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.hourlyForecast)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_12_HOURLY);
			}
			if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.dailyForecast)) {
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);
			}
		}
		if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			RequestOwm requestOwm = new RequestOwm();
			requestWeatherSources.put(weatherSourceType, requestOwm);

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
			requestOwm.setExcludeApis(excludeSet);
			requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		}
		if (requestWeatherDataTypeSet.contains(RequestWeatherDataType.airQuality)) {
			RequestAqicn requestAqicn = new RequestAqicn();
			requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
			requestWeatherSources.put(WeatherSourceType.AQICN, requestAqicn);
		}
	}

	public static WeatherSourceType getMainWeatherSourceType(Context context, @NonNull String countryCode) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		WeatherSourceType mainWeatherSourceType = null;

		if (sharedPreferences.getBoolean(context.getString(R.string.pref_key_accu_weather), true)) {
			mainWeatherSourceType = WeatherSourceType.ACCU_WEATHER;
		} else {
			mainWeatherSourceType = WeatherSourceType.OPEN_WEATHER_MAP;
		}

		if (countryCode.equals("KR")) {
			boolean kmaIsTopPriority = sharedPreferences.getBoolean(context.getString(R.string.pref_key_kma_top_priority), true);
			if (kmaIsTopPriority) {
				mainWeatherSourceType = WeatherSourceType.KMA;
			}
		}

		return mainWeatherSourceType;
	}

}
