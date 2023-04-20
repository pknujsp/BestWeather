package com.lifedawn.bestweather.data.remote.retrofit.client

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.*

interface RestfulApiQuery {
    // kma web --------------------------------------------------------------------------------------------
    @GET("current-weather.do")
    suspend fun getKmaCurrentConditions(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<String>

    @GET("digital-forecast.do")
    suspend fun getKmaHourlyAndDailyForecast(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<String>

    //met norway---------------------------------------------------------------------------------------------------------
    //https://api.met.no/weatherapi/locationforecast/2.0/complete?lat=35.235421&lon=128.868227
    //@Headers("User-Agent: Mozilla/5.0 (Macintosh Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 " +
    //"Safari/537.36")

    @Headers("User-Agent: BestWeatherApp https://github.com/pknujsp")
    @GET("locationforecast/2.0/complete")
    suspend fun getMetNorwayLocationForecast(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<JsonElement>

    //aqicn--------------------------------------------------------------------------------------------------------------
    //https://api.waqi.info/feed/geo:35.235421128.868227/?token=8538c6118653f6e4acbfd8ae5667bd07683a1cde
    @GET("feed/geo:{latitude}{longitude}/")
    suspend fun getAqiCnGeolocalizedFeed(
        @Path(value = "latitude", encoded = true) latitude: String,
        @Path(value = "longitude", encoded = true) longitude: String, @QueryMap(encoded = true) queryMap: Map<String, String>
    ): Response<JsonElement>

    //openweathermap---------------------------------------------------------------------------------------------------------
    //https://api.openweathermap.org/data/2.5/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}
    @GET("data/2.5/onecall")
    suspend fun getOneResponse(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<JsonElement>

    //flickr------------------------------------------------------------------------------------------------------------------------
    //https://api.flickr.com/services/rest/?method=flickr.galleries.getPhotos&api_key=2c887b8d73b8334ddb3b0809c387de1b&gallery_id=72157719980390655&format=json&nojsoncallback=1
    @GET("rest/")
    suspend fun getPhotosFromGallery(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<JsonElement>

    @GET("rest/")
    suspend fun getGetInfo(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<JsonElement>

    //Google place search
    @GET("json")
    suspend fun getGooglePlaceSearch(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<JsonElement>

    @GET("json")
    suspend fun getFindPlaceSearch(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<JsonElement>

    //FreeTimeApi
    // https://timeapi.io/api/TimeZone/coordinate?latitude=38.9&longitude=-77.03
    @GET("coordinate")
    suspend fun getTimeZone(@QueryMap(encoded = true) queryMap: Map<String, String>): Response<JsonElement>

    //nominatim reverse geocode
    //https://nominatim.openstreetmap.org/reverse?format=geojson&lat=44.50155&lon=11.33989
    @GET("reverse")
    suspend fun nominatimReverseGeocode(
        @QueryMap(encoded = true) queryMap: Map<String, String>,
        @Header("Accept-Language") lang: String
    ): Response<JsonElement>

    //nominatim geocode
    //https://nominatim.openstreetmap.org/search?q=%EB%82%B4%EB%8F%99&format=geojson&addressdetails=1
    @GET("search")
    suspend fun nominatimGeocode(
        @QueryMap(encoded = true) queryMap: Map<String, String>,
        @Header("Accept-Language") lang: String
    ): Response<JsonElement>

    // rainviewer
    @GET("weather-maps.json")
    suspend fun getRainViewer(): Response<JsonElement>
}