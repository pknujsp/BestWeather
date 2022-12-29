package com.lifedawn.bestweather.data.remote.retrofit.client

import com.google.gson.JsonElement

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface RestfulApiQuery {
    // kma api xml-----------------------------------------------------------------------------------------
    @GET("getUltraSrtNcst")
    fun getUltraSrtNcstByXml(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<String>

    @GET("getUltraSrtNcst")
    fun getUltraSrtNcstByText(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<String>

    @GET("getUltraSrtFcst")
    fun getUltraSrtFcstByXml(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<String>

    @GET("getVilageFcst")
    fun getVilageFcstByXml(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<String>

    @GET("getMidLandFcst")
    fun getMidLandFcstByXml(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<String>

    @GET("getMidTa")
    fun getMidTaByXml(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<String>

    // kma web html --------------------------------------------------------------------------------------------
    @GET("current-weather.do")
    fun getKmaCurrentConditions(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<String>

    @GET("digital-forecast.do")
    fun getKmaHourlyAndDailyForecast(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<String>

    //accu weather------------------------------------------------------------------------------------------------------------------------
    /* http://dataservice.accuweather.com/locations/v1/cities/geoposition/
    search?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&q=35.235421%2C128.868227
    */
    @GET("locations/v1/cities/geoposition/search")
    fun geoAccuPositionSearch(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    //http://dataservice.accuweather.com/currentconditions/v1/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
    @GET("currentconditions/v1/{location_key}")
    fun getAccuCurrentConditions(
        @Path(value = "location_key", encoded = true) locationKey: String,
        @QueryMap(encoded = true) queryMap: Map<String, String>
    ): Call<JsonElement>

    //http://dataservice.accuweather.com/forecasts/v1/daily/5day/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
    @GET("forecasts/v1/daily/5day/{location_key}")
    fun getAccuDailyForecast(
        @Path(value = "location_key", encoded = true) locationKey: String,
        @QueryMap(encoded = true) queryMap: Map<String, String>
    ): Call<JsonElement>

    //http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/3430446?apikey=tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8&details=true&metric=true
    @GET("forecasts/v1/hourly/12hour/{location_key}")
    fun getAccuHourlyForecast(
        @Path(value = "location_key", encoded = true) locationKey: String,
        @QueryMap(encoded = true) queryMap: Map<String, String>
    ): Call<JsonElement>

    //met norway---------------------------------------------------------------------------------------------------------
    //https://api.met.no/weatherapi/locationforecast/2.0/complete?lat=35.235421&lon=128.868227
    //@Headers("User-Agent: Mozilla/5.0 (Macintosh Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 " +
//			"Safari/537.36")

    @Headers("User-Agent: BestWeatherApp https://github.com/pknujsp")
    @GET("locationforecast/2.0/complete")
    fun getMetNorwayLocationForecast(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    //aqicn--------------------------------------------------------------------------------------------------------------
    //https://api.waqi.info/feed/geo:35.235421128.868227/?token=8538c6118653f6e4acbfd8ae5667bd07683a1cde
    @GET("feed/geo:{latitude}{longitude}/")
    fun getAqiCnGeolocalizedFeed(
        @Path(value = "latitude", encoded = true) latitude: String,
        @Path(value = "longitude", encoded = true) longitude: String, @QueryMap(encoded = true) queryMap: Map<String, String>
    ): Call<JsonElement>

    //openweathermap---------------------------------------------------------------------------------------------------------
    //https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}
    @GET("data/2.5/weather")
    fun getOwmCurrentConditions(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    //https://pro.openweathermap.org/data/2.5/forecast/hourly?lat={lat}&lon={lon}&appid={API key}
    @GET("data/2.5/forecast/hourly")
    fun getOwmHourlyForecast(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    //https://api.openweathermap.org/data/2.5/forecast/daily?lat={lat}&lon={lon}&cnt={cnt}&appid={API key}
    @GET("data/2.5/forecast/daily")
    fun getOwmDailyForecast(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    //https://api.openweathermap.org/data/2.5/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}
    @GET("data/2.5/onecall")
    fun getOneCall(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    //flickr------------------------------------------------------------------------------------------------------------------------
    //https://api.flickr.com/services/rest/?method=flickr.galleries.getPhotos&api_key=2c887b8d73b8334ddb3b0809c387de1b&gallery_id=72157719980390655&format=json&nojsoncallback=1
    @GET("rest/")
    fun getPhotosFromGallery(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    @GET("rest/")
    fun getGetInfo(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>


    //Google place search
    @GET("json")
    fun getGooglePlaceSearch(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    @GET("json")
    fun getFindPlaceSearch(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>

    //FreeTimeApi
    // https://timeapi.io/api/TimeZone/coordinate?latitude=38.9&longitude=-77.03
    @GET("coordinate")
    fun getTimeZone(@QueryMap(encoded = true) queryMap: Map<String, String>): Call<JsonElement>


    //nominatim reverse geocode
    //https://nominatim.openstreetmap.org/reverse?format=geojson&lat=44.50155&lon=11.33989
    @GET("reverse")
    fun nominatimReverseGeocode(
        @QueryMap(encoded = true) queryMap: Map<String, String>,
        @Header("Accept-Language") lang: String
    ): Call<JsonElement>

    //nominatim geocode
    //https://nominatim.openstreetmap.org/search?q=%EB%82%B4%EB%8F%99&format=geojson&addressdetails=1
    @GET("search")
    fun nominatimGeocode(
        @QueryMap(encoded = true) queryMap: Map<String, String>,
        @Header("Accept-Language") lang: String
    ): Call<JsonElement>

    // rainviewer
    @GET("weather-maps.json")
    fun getRainViewer(): Call<JsonElement>
}