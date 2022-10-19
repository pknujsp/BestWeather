package com.lifedawn.bestweather.retrofit.client;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.tickaroo.tikxml.TikXml;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface Queries {
	// kma api xml-----------------------------------------------------------------------------------------
	@GET("getUltraSrtNcst")
	Call<String> getUltraSrtNcstByXml(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("getUltraSrtNcst")
	Call<String> getUltraSrtNcstByText(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("getUltraSrtFcst")
	Call<String> getUltraSrtFcstByXml(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("getVilageFcst")
	Call<String> getVilageFcstByXml(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("getMidLandFcst")
	Call<String> getMidLandFcstByXml(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("getMidTa")
	Call<String> getMidTaByXml(@QueryMap(encoded = true) Map<String, String> queryMap);

	// kma web html --------------------------------------------------------------------------------------------
	@GET("current-weather.do")
	Call<String> getKmaCurrentConditions(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("digital-forecast.do")
	Call<String> getKmaHourlyAndDailyForecast(@QueryMap(encoded = true) Map<String, String> queryMap);

	//accu weather------------------------------------------------------------------------------------------------------------------------
	/* http://dataservice.accuweather.com/locations/v1/cities/geoposition/
	search?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&q=35.235421%2C128.868227
	*/
	@GET("locations/v1/cities/geoposition/search")
	Call<JsonElement> geoAccuPositionSearch(@QueryMap(encoded = true) Map<String, String> queryMap);

	//http://dataservice.accuweather.com/currentconditions/v1/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
	@GET("currentconditions/v1/{location_key}")
	Call<JsonElement> getAccuCurrentConditions(@Path(value = "location_key", encoded = true) String locationKey,
	                                           @QueryMap(encoded = true) Map<String, String> queryMap);

	//http://dataservice.accuweather.com/forecasts/v1/daily/5day/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
	@GET("forecasts/v1/daily/5day/{location_key}")
	Call<JsonElement> getAccuDailyForecast(@Path(value = "location_key", encoded = true) String locationKey,
	                                       @QueryMap(encoded = true) Map<String, String> queryMap);

	//http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
	@GET("forecasts/v1/hourly/12hour/{location_key}")
	Call<JsonElement> getAccuHourlyForecast(@Path(value = "location_key", encoded = true) String locationKey,
	                                        @QueryMap(encoded = true) Map<String, String> queryMap);

	//met norway---------------------------------------------------------------------------------------------------------
	//https://api.met.no/weatherapi/locationforecast/2.0/complete?lat=35.235421&lon=128.868227
	//@Headers("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 " +
//			"Safari/537.36")

	@Headers("User-Agent: BestWeatherApp https://github.com/pknujsp")
	@GET("locationforecast/2.0/complete")
	Call<JsonElement> getMetNorwayLocationForecast(@QueryMap(encoded = true) Map<String, String> queryMap);

	//aqicn--------------------------------------------------------------------------------------------------------------
	//https://api.waqi.info/feed/geo:35.235421;128.868227/?token=8538c6118653f6e4acbfd8ae5667bd07683a1cde
	@GET("feed/geo:{latitude};{longitude}/")
	Call<JsonElement> getAqiCnGeolocalizedFeed(@Path(value = "latitude", encoded = true) String latitude,
	                                           @Path(value = "longitude", encoded = true) String longitude, @QueryMap(encoded = true) Map<String, String> queryMap);

	//openweathermap---------------------------------------------------------------------------------------------------------
	//https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}
	@GET("data/2.5/weather")
	Call<JsonElement> getOwmCurrentConditions(@QueryMap(encoded = true) Map<String, String> queryMap);

	//https://pro.openweathermap.org/data/2.5/forecast/hourly?lat={lat}&lon={lon}&appid={API key}
	@GET("data/2.5/forecast/hourly")
	Call<JsonElement> getOwmHourlyForecast(@QueryMap(encoded = true) Map<String, String> queryMap);

	//https://api.openweathermap.org/data/2.5/forecast/daily?lat={lat}&lon={lon}&cnt={cnt}&appid={API key}
	@GET("data/2.5/forecast/daily")
	Call<JsonElement> getOwmDailyForecast(@QueryMap(encoded = true) Map<String, String> queryMap);

	//https://api.openweathermap.org/data/2.5/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}
	@GET("data/2.5/onecall")
	Call<JsonElement> getOneCall(@QueryMap(encoded = true) Map<String, String> queryMap);

	//flickr------------------------------------------------------------------------------------------------------------------------
	//https://api.flickr.com/services/rest/?method=flickr.galleries.getPhotos&api_key=2c887b8d73b8334ddb3b0809c387de1b&gallery_id=72157719980390655&format=json&nojsoncallback=1
	@GET("rest/")
	Call<JsonElement> getPhotosFromGallery(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("rest/")
	Call<JsonElement> getGetInfo(@QueryMap(encoded = true) Map<String, String> queryMap);


	//Google place search
	@GET("json")
	Call<JsonElement> getGooglePlaceSearch(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("json")
	Call<JsonElement> getFindPlaceSearch(@QueryMap(encoded = true) Map<String, String> queryMap);

	//FreeTimeApi
	// https://timeapi.io/api/TimeZone/coordinate?latitude=38.9&longitude=-77.03
	@GET("coordinate")
	Call<JsonElement> getTimeZone(@QueryMap(encoded = true) Map<String, String> queryMap);


	//nominatim reverse geocode
	//https://nominatim.openstreetmap.org/reverse?format=geojson&lat=44.50155&lon=11.33989
	@GET("reverse")
	Call<JsonElement> nominatimReverseGeocode(@QueryMap(encoded = true) Map<String, String> queryMap,
	                                          @Header("Accept-Language") String lang);

	// rainviewer
	@GET("weather-maps.json")
	Call<JsonElement> getRainViewer();
}