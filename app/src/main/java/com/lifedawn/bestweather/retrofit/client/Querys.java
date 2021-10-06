package com.lifedawn.bestweather.retrofit.client;

import com.google.gson.JsonElement;
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
	/* http://dataservice.accuweather.com/locations/v1/cities/geoposition/
	search?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&q=35.235421%2C128.868227
	*/
	@GET("locations/v1/cities/geoposition/search")
	Call<JsonObject> geoPositionSearch(@QueryMap(encoded = true) Map<String, String> queryMap);

	//http://dataservice.accuweather.com/currentconditions/v1/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
	@GET("currentconditions/v1/{location_key}")
	Call<JsonElement> getCurrentConditions(@Path(value = "location_key", encoded = true) String locationKey,
	                                       @QueryMap(encoded = true) Map<String, String> queryMap);

	//http://dataservice.accuweather.com/forecasts/v1/daily/5day/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
	@GET("forecasts/v1/daily/5day/{location_key}")
	Call<JsonElement> get5Days(@Path(value = "location_key", encoded = true) String locationKey,
	                           @QueryMap(encoded = true) Map<String, String> queryMap);

	//http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
	@GET("forecasts/v1/hourly/12hour/{location_key}")
	Call<JsonElement> get12Hourly(@Path(value = "location_key", encoded = true) String locationKey,
	                              @QueryMap(encoded = true) Map<String, String> queryMap);

	//met norway
	//https://api.met.no/weatherapi/locationforecast/2.0/complete?lat=35.235421&lon=128.868227
	@Headers("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36")
	@GET("locationforecast/2.0/complete")
	Call<JsonObject> getLocationForecast(@QueryMap(encoded = true) Map<String, String> queryMap);

	//aqicn
	//https://api.waqi.info/feed/geo:35.235421;128.868227/?token=8538c6118653f6e4acbfd8ae5667bd07683a1cde
	@GET("feed/geo:{latitude};{longitude}/")
	Call<JsonObject> getGeolocalizedFeed(@Path(value = "latitude", encoded = true) String latitude,
	                                     @Path(value = "longitude", encoded = true) String longitude, @QueryMap(encoded = true) Map<String, String> queryMap);

	//openweathermap
	//https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}
	@GET("data/2.5/weather")
	Call<JsonObject> getCurrentWeather(@QueryMap(encoded = true) Map<String, String> queryMap);

	//https://pro.openweathermap.org/data/2.5/forecast/hourly?lat={lat}&lon={lon}&appid={API key}
	@GET("data/2.5/forecast/hourly")
	Call<JsonObject> getHourlyForecast(@QueryMap(encoded = true) Map<String, String> queryMap);

	//https://api.openweathermap.org/data/2.5/forecast/daily?lat={lat}&lon={lon}&cnt={cnt}&appid={API key}
	@GET("data/2.5/forecast/daily")
	Call<JsonObject> getDailyForecast(@QueryMap(encoded = true) Map<String, String> queryMap);

	//https://api.openweathermap.org/data/2.5/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}
	@GET("data/2.5/onecall")
	Call<JsonObject> getOneCall(@QueryMap(encoded = true) Map<String, String> queryMap);
}