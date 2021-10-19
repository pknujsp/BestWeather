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
	Call<JsonElement> getUltraSrtNcst(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("getUltraSrtFcst")
	Call<JsonElement> getUltraSrtFcst(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("getVilageFcst")
	Call<JsonElement> getVilageFcst(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("getMidLandFcst")
	Call<JsonElement> getMidLandFcst(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	@GET("getMidTa")
	Call<JsonElement> getMidTa(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//accu weather
	/* http://dataservice.accuweather.com/locations/v1/cities/geoposition/
	search?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&q=35.235421%2C128.868227
	*/
	@GET("locations/v1/cities/geoposition/search")
	Call<JsonElement> geoPositionSearch(@QueryMap(encoded = true) Map<String, String> queryMap);
	
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
	Call<JsonElement> getLocationForecast(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//aqicn
	//https://api.waqi.info/feed/geo:35.235421;128.868227/?token=8538c6118653f6e4acbfd8ae5667bd07683a1cde
	@GET("feed/geo:{latitude};{longitude}/")
	Call<JsonElement> getGeolocalizedFeed(@Path(value = "latitude", encoded = true) String latitude,
			@Path(value = "longitude", encoded = true) String longitude, @QueryMap(encoded = true) Map<String, String> queryMap);
	
	//openweathermap
	//https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}
	@GET("data/2.5/weather")
	Call<JsonElement> getCurrentWeather(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//https://pro.openweathermap.org/data/2.5/forecast/hourly?lat={lat}&lon={lon}&appid={API key}
	@GET("data/2.5/forecast/hourly")
	Call<JsonElement> getHourlyForecast(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//https://api.openweathermap.org/data/2.5/forecast/daily?lat={lat}&lon={lon}&cnt={cnt}&appid={API key}
	@GET("data/2.5/forecast/daily")
	Call<JsonElement> getDailyForecast(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//https://api.openweathermap.org/data/2.5/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}
	@GET("data/2.5/onecall")
	Call<JsonElement> getOneCall(@QueryMap(encoded = true) Map<String, String> queryMap);
	
	//flickr
	//https://api.flickr.com/services/rest/?method=flickr.galleries.getPhotos&api_key=2c887b8d73b8334ddb3b0809c387de1b&gallery_id=72157719980390655&format=json&nojsoncallback=1
	@GET("rest/")
	Call<JsonElement> getPhotosFromGallery(@QueryMap(encoded = true) Map<String, String> queryMap);
}