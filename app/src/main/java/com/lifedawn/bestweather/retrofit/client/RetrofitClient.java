package com.lifedawn.bestweather.retrofit.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tickaroo.tikxml.TikXml;
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	//openweathermap
	public static final String DEFAULT_OPEN_WEATHER_MAP_SERVICE_URL = "https://api.openweathermap.org/";
	public static final String PRO_OPEN_WEATHER_MAP_SERVICE_URL = "https://pro.openweathermap.org/";
	//flickr
	public static final String FLICKR_SERVICE_URL = "https://www.flickr.com/services/";

	//service key, token
	public static final String VILAGE_FCST_INFO_SERVICE_SERVICE_KEY = "T2nJm9zlOA0Z7Dut%2BThT6Jp0Itn0zZw80AUP3uMdOWlZJR1gVPkx9p1t8etuSW1kWsSNrGGHKdxbwr1IUlt%2Baw%3D%3D";
	public static final String MID_FCST_INFO_SERVICE_SERVICE_KEY = "T2nJm9zlOA0Z7Dut%2BThT6Jp0Itn0zZw80AUP3uMdOWlZJR1gVPkx9p1t8etuSW1kWsSNrGGHKdxbwr1IUlt%2Baw%3D%3D";
	public static final String ACCU_WEATHER_SERVICE_KEY = "tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8";
	public static final String AQICN_TOKEN = "8538c6118653f6e4acbfd8ae5667bd07683a1cde";
	public static final String OWM_API_KEY = "4e3a18c58bdf77658cd11a9ee0cb51cc";
	public static final String FLICKR_KEY = "2c887b8d73b8334ddb3b0809c387de1b";
	public static final String FLICKR_SECRET = "0112b42bf9b07200";

	public static final String XML_DATATYPE = "XML";
	public static final String LOG_TAG = "Retrofit Response";

	private static final OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();

	public enum ServiceType {
		YESTERDAY_ULTRA_SRT_NCST, ULTRA_SRT_NCST, ULTRA_SRT_FCST, MID_LAND_FCST, MID_TA_FCST, VILAGE_FCST, ACCU_GEOPOSITION_SEARCH,
		ACCU_CURRENT_CONDITIONS,
		ACCU_5_DAYS_OF_DAILY, ACCU_12_HOURLY, MET_NORWAY_LOCATION_FORECAST, AQICN_GEOLOCALIZED_FEED, OWM_CURRENT_WEATHER,
		OWM_HOURLY_FORECAST, OWM_DAILY_FORECAST, OWM_ONE_CALL, FLICKR
	}


	public static synchronized Querys getApiService(ServiceType serviceType) {

		switch (serviceType) {
			case MID_LAND_FCST:
			case MID_TA_FCST:
				Retrofit midFcstInstance = new Retrofit.Builder().client(client)
						.baseUrl(MID_FCST_INFO_SERVICE_URL)
						.addConverterFactory(ScalarsConverterFactory.create())
						.build();
				return midFcstInstance.create(Querys.class);

			case ULTRA_SRT_NCST:
			case YESTERDAY_ULTRA_SRT_NCST:
			case ULTRA_SRT_FCST:
			case VILAGE_FCST:
				Retrofit vilageFcstInstance = new Retrofit.Builder().client(client).baseUrl(VILAGE_FCST_INFO_SERVICE_URL)
						.addConverterFactory(ScalarsConverterFactory.create())
						.build();
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

			case OWM_CURRENT_WEATHER:
			case OWM_DAILY_FORECAST:
			case OWM_ONE_CALL:
				Retrofit owmDefaultInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
						DEFAULT_OPEN_WEATHER_MAP_SERVICE_URL).build();
				return owmDefaultInstance.create(Querys.class);

			case OWM_HOURLY_FORECAST:
				Retrofit owmProInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
						PRO_OPEN_WEATHER_MAP_SERVICE_URL).build();
				return owmProInstance.create(Querys.class);

			case FLICKR:
				Retrofit flickrInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
						FLICKR_SERVICE_URL).build();
				return flickrInstance.create(Querys.class);

			default:
				return null;
		}
	}

}
