package com.lifedawn.bestweather.retrofit.client;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
	//kma
	public static final String MID_FCST_INFO_SERVICE_URL = "http://apis.data.go.kr/1360000/MidFcstInfoService/";
	public static final String VILAGE_FCST_INFO_SERVICE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/";
	//accu weather
	public static final String ACCU_WEATHER_SERVICE_URL = "http://dataservice.accuweather.com/";
	//met norway
	public static final String MET_NORWAY_LOCATION_FORECAST_SERVICE_URL = "https://api.met.no/weatherapi/";
	//aqicn
	public static final String AQICN_GEOLOCALIZED_FEED_SERVICE_URL = "https://api.waqi.info/";
	
	//service key, token
	public static final String VILAGE_FCST_INFO_SERVICE_SERVICE_KEY = "T2nJm9zlOA0Z7Dut%2BThT6Jp0Itn0zZw80AUP3uMdOWlZJR1gVPkx9p1t8etuSW1kWsSNrGGHKdxbwr1IUlt%2Baw%3D%3D";
	public static final String MID_FCST_INFO_SERVICE_SERVICE_KEY = "T2nJm9zlOA0Z7Dut%2BThT6Jp0Itn0zZw80AUP3uMdOWlZJR1gVPkx9p1t8etuSW1kWsSNrGGHKdxbwr1IUlt%2Baw%3D%3D";
	public static final String ACCU_WEATHER_SERVICE_KEY = "tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8";
	public static final String AQICN_TOKEN = "8538c6118653f6e4acbfd8ae5667bd07683a1cde";
	
	public static final String DATATYPE = "JSON";
	
	public enum ServiceType {
		ULTRA_SRT_NCST, ULTRA_SRT_FCST, MID_LAND_FCST, MID_TA_FCST, VILAGE_FCST, ACCU_GEOPOSITION_SEARCH, ACCU_CURRENT_CONDITIONS, ACCU_5_DAYS_OF_DAILY, ACCU_12_HOURLY, MET_NORWAY_LOCATION_FORECAST, AQICN_GEOLOCALIZED_FEED
	}
	
	
	public static synchronized Querys getApiService(ServiceType serviceType) {
		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
		
		switch (serviceType) {
			case MID_LAND_FCST:
			case MID_TA_FCST:
				Retrofit midFcstInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
						MID_FCST_INFO_SERVICE_URL).build();
				return midFcstInstance.create(Querys.class);
			
			case ULTRA_SRT_NCST:
			case ULTRA_SRT_FCST:
			case VILAGE_FCST:
				Retrofit vilageFcstInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
						VILAGE_FCST_INFO_SERVICE_URL).build();
				return vilageFcstInstance.create(Querys.class);
			
			case ACCU_GEOPOSITION_SEARCH:
			case ACCU_12_HOURLY:
			case ACCU_CURRENT_CONDITIONS:
			case ACCU_5_DAYS_OF_DAILY:
				Retrofit accuWeatherInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
						ACCU_WEATHER_SERVICE_URL).build();
				return accuWeatherInstance.create(Querys.class);
			
			case MET_NORWAY_LOCATION_FORECAST:
				Retrofit metNorwayLocationForecastsInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
						MET_NORWAY_LOCATION_FORECAST_SERVICE_URL).build();
				return metNorwayLocationForecastsInstance.create(Querys.class);
			
			case AQICN_GEOLOCALIZED_FEED:
				Retrofit aqicnGeolocalizedInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
						AQICN_GEOLOCALIZED_FEED_SERVICE_URL).build();
				return aqicnGeolocalizedInstance.create(Querys.class);
			
			default:
				return null;
		}
	}
	
}
