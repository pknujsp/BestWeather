package com.lifedawn.bestweather.retrofit.client;

import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse.UltraSrtFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse.UltraSrtNcstRoot;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface Querys {
	// kma
	@GET("getUltraSrtNcst")
	Call<JsonObject> getUltraSrtNcst(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("getUltraSrtFcst")
	Call<JsonObject> getUltraSrtFcst(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("getVilageFcst")
	Call<JsonObject> getVilageFcst(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("getMidLandFcst")
	Call<JsonObject> getMidLandFcst(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("getMidTa")
	Call<JsonObject> getMidTa(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//accu weather
	@GET("currentconditions/v1")
	Call<JsonObject> getCurrentConditions(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("forecasts/v1/daily/5day")
	Call<JsonObject> get5Days(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("forecasts/v1/hourly/12hour")
	Call<JsonObject> get12Hourly(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//met norway
	@GET("locationforecast/2.0/complete")
	Call<JsonObject> getLocationForecast(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//aqicn
	@GET("feed")
	Call<JsonObject> getGeolocalizedFeed(@QueryMap(encoded = true) Map<String, String> queryMap);
}