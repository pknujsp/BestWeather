package com.lifedawn.bestweather.retrofit.client;

import com.lifedawn.bestweather.main.MyApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
	//kma
	public static final String KMA_CURRENT_CONDITIONS_AND_HOURLY_AND_DAILY_FORECAST_URL = "https://www.weather.go.kr/w/wnuri-fct2021/main/";
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
	//google place search
	public static final String GOOGLE_PLACE_SEARCH_SERVICE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/";

	//service key, token
	public static final String VILAGE_FCST_INFO_SERVICE_SERVICE_KEY = "T2nJm9zlOA0Z7Dut%2BThT6Jp0Itn0zZw80AUP3uMdOWlZJR1gVPkx9p1t8etuSW1kWsSNrGGHKdxbwr1IUlt%2Baw%3D%3D";
	public static final String MID_FCST_INFO_SERVICE_SERVICE_KEY = "T2nJm9zlOA0Z7Dut%2BThT6Jp0Itn0zZw80AUP3uMdOWlZJR1gVPkx9p1t8etuSW1kWsSNrGGHKdxbwr1IUlt%2Baw%3D%3D";
	public static final String ACCU_WEATHER_SERVICE_KEY = "tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8";
	public static final String AQICN_TOKEN = "8538c6118653f6e4acbfd8ae5667bd07683a1cde";
	public static final String OWM_ONECALL_API_KEY = "4e3a18c58bdf77658cd11a9ee0cb51cc";
	public static final String OWM_INDIVIDUAL_API_KEY = "4e3a18c58bdf77658cd11a9ee0cb51cc";
	public static final String FLICKR_KEY = "2c887b8d73b8334ddb3b0809c387de1b";
	public static final String FLICKR_SECRET = "0112b42bf9b07200";

	public static final String XML_DATATYPE = "XML";
	public static final String LOG_TAG = "Retrofit Response";

	private static final OkHttpClient client = new OkHttpClient.Builder().readTimeout(4, TimeUnit.SECONDS)
			.connectTimeout(3, TimeUnit.SECONDS).build();

	public enum ServiceType {
		KMA_YESTERDAY_ULTRA_SRT_NCST, KMA_ULTRA_SRT_NCST, KMA_ULTRA_SRT_FCST, KMA_MID_LAND_FCST, KMA_MID_TA_FCST, KMA_VILAGE_FCST,
		ACCU_GEOPOSITION_SEARCH,
		ACCU_CURRENT_CONDITIONS, ACCU_DAILY_FORECAST, ACCU_HOURLY_FORECAST, MET_NORWAY_LOCATION_FORECAST, AQICN_GEOLOCALIZED_FEED,
		OWM_CURRENT_CONDITIONS,
		OWM_HOURLY_FORECAST, OWM_DAILY_FORECAST, OWM_ONE_CALL, FLICKR, KMA_WEB_CURRENT_CONDITIONS, KMA_WEB_FORECASTS,
		GOOGLE_PLACE_SEARCH
	}


	public static synchronized Queries getApiService(ServiceType serviceType) {

		switch (serviceType) {
			case KMA_WEB_CURRENT_CONDITIONS:
			case KMA_WEB_FORECASTS:
				Retrofit kmaHtmlInstance = new Retrofit.Builder().client(client).addConverterFactory(ScalarsConverterFactory.create())
						.callbackExecutor(Executors.newSingleThreadExecutor())
						.baseUrl(KMA_CURRENT_CONDITIONS_AND_HOURLY_AND_DAILY_FORECAST_URL).build();
				return kmaHtmlInstance.create(Queries.class);

			case KMA_MID_LAND_FCST:
			case KMA_MID_TA_FCST:
				Retrofit midFcstInstance = new Retrofit.Builder().client(client)
						.baseUrl(MID_FCST_INFO_SERVICE_URL).callbackExecutor(Executors.newSingleThreadExecutor())

						.addConverterFactory(ScalarsConverterFactory.create())
						.build();
				return midFcstInstance.create(Queries.class);

			case KMA_ULTRA_SRT_NCST:
			case KMA_YESTERDAY_ULTRA_SRT_NCST:
			case KMA_ULTRA_SRT_FCST:
			case KMA_VILAGE_FCST:
				Retrofit vilageFcstInstance = new Retrofit.Builder().client(client).baseUrl(VILAGE_FCST_INFO_SERVICE_URL).callbackExecutor(Executors.newSingleThreadExecutor())

						.addConverterFactory(ScalarsConverterFactory.create())
						.build();
				return vilageFcstInstance.create(Queries.class);

			case ACCU_GEOPOSITION_SEARCH:
			case ACCU_HOURLY_FORECAST:
			case ACCU_CURRENT_CONDITIONS:
			case ACCU_DAILY_FORECAST:
				Retrofit accuWeatherInstance = new Retrofit.Builder().client(client).addConverterFactory(
								GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
								ACCU_WEATHER_SERVICE_URL).callbackExecutor(Executors.newSingleThreadExecutor())
						.build();
				return accuWeatherInstance.create(Queries.class);

			case MET_NORWAY_LOCATION_FORECAST:
				Retrofit metNorwayLocationForecastsInstance = new Retrofit.Builder().client(client).addConverterFactory(
								GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
								MET_NORWAY_LOCATION_FORECAST_SERVICE_URL).callbackExecutor(Executors.newSingleThreadExecutor())
						.build();
				return metNorwayLocationForecastsInstance.create(Queries.class);

			case AQICN_GEOLOCALIZED_FEED:
				Retrofit aqiCnGeolocalizedInstance = new Retrofit.Builder().client(client).addConverterFactory(
								GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
								AQICN_GEOLOCALIZED_FEED_SERVICE_URL).callbackExecutor(Executors.newSingleThreadExecutor())
						.build();
				return aqiCnGeolocalizedInstance.create(Queries.class);

			case OWM_CURRENT_CONDITIONS:
			case OWM_DAILY_FORECAST:
			case OWM_ONE_CALL:
				Retrofit owmBeginApiInstance = new Retrofit.Builder().client(client).addConverterFactory(
								GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
								DEFAULT_OPEN_WEATHER_MAP_SERVICE_URL).callbackExecutor(Executors.newSingleThreadExecutor())
						.build();
				return owmBeginApiInstance.create(Queries.class);

			case OWM_HOURLY_FORECAST:
				Retrofit owmBeginProInstance = new Retrofit.Builder().client(client).addConverterFactory(
								GsonConverterFactory.create()).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
								PRO_OPEN_WEATHER_MAP_SERVICE_URL).callbackExecutor(Executors.newSingleThreadExecutor())
						.build();
				return owmBeginProInstance.create(Queries.class);

			case FLICKR:
				Retrofit flickrInstance = new Retrofit.Builder().client(client).addConverterFactory(
								GsonConverterFactory.create()).callbackExecutor(Executors.newSingleThreadExecutor())
						.addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
								FLICKR_SERVICE_URL).build();
				return flickrInstance.create(Queries.class);

			case GOOGLE_PLACE_SEARCH:
				Retrofit googlePlaceSearchInstance = new Retrofit.Builder().client(client).addConverterFactory(
								GsonConverterFactory.create()).callbackExecutor(Executors.newSingleThreadExecutor())
						.addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
								GOOGLE_PLACE_SEARCH_SERVICE_URL).build();
				return googlePlaceSearchInstance.create(Queries.class);

			default:
				return null;
		}
	}

}
