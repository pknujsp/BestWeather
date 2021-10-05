package com.lifedawn.bestweather.weathers.dataprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.CurrentConditionsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.FiveDaysOfDailyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.GeoPositionSearchParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.TwelveHoursOfHourlyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.retrofit.util.RetrofitCallListManager;
import com.lifedawn.bestweather.weathers.dataprocessing.callback.KmaVilageFcstCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccuWeatherProcessing {
	/**
	 * Current Conditions
	 */
	public Call<JsonObject> getCurrentConditions(CurrentConditionsParameter currentConditionsParameter, JsonDownloader callback) {
		return null;
	}
	
	/**
	 * 5 Days Of Daily Forecast
	 */
	public Call<JsonObject> get5DaysOfDailyForecast(FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter,
			JsonDownloader callback) {
		return null;
	}
	
	/**
	 * 12 Hours of Hourly Forecasts
	 */
	public Call<JsonObject> get12HoursOfHourlyForecasts(TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter,
			JsonDownloader callback) {
		return null;
	}
	
	
	/**
	 * GeoPosition Search
	 */
	public Call<JsonObject> getGeoPositionSearch(GeoPositionSearchParameter geoPositionSearchParameter, JsonDownloader callback) {
		return null;
	}
	

}
