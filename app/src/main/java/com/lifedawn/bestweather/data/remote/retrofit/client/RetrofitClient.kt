package com.lifedawn.bestweather.data.remote.retrofit.client;


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    //kma
    const val KMA_CURRENT_CONDITIONS_AND_HOURLY_AND_DAILY_FORECAST_URL = "https://www.weather.go.kr/w/wnuri-fct2021/main/"

    //met norway
    const val MET_NORWAY_LOCATION_FORECAST_SERVICE_URL = "https://api.met.no/weatherapi/"

    //aqicn
    const val AQICN_GEOLOCALIZED_FEED_SERVICE_URL = "https://api.waqi.info/"

    //openweathermap
    const val DEFAULT_OPEN_WEATHER_MAP_SERVICE_URL = "https://api.openweathermap.org/"

    //flickr
    const val FLICKR_SERVICE_URL = "https://www.flickr.com/services/"

    //freeTimeApi
    const val FREE_TIME_SERVICE_URL = "https://timeapi.io/api/TimeZone/

    const val NOMINATIM_GEOCODE_URL = "https://nominatim.openstreetmap.org/"
    const val RAIN_VIEWER_URL = "https://api.rainviewer.com/public/"

    //service key, token
    const val AQICN_TOKEN = "8538c6118653f6e4acbfd8ae5667bd07683a1cde"
    const val OWM_ONECALL_API_KEY = "4e3a18c58bdf77658cd11a9ee0cb51cc"
    const val FLICKR_KEY = "2c887b8d73b8334ddb3b0809c387de1b"

    const val XML = "XML"
    const val LOG_TAG = "Retrofit Response"

    private val client = OkHttpClient.Builder().readTimeout(4, TimeUnit.SECONDS)
        .connectTimeout(3, TimeUnit.SECONDS).build()

    enum class ServiceType {
        MET_NORWAY_LOCATION_FORECAST, AQICN_GEOLOCALIZED_FEED,
        OWM_ONE_CALL, FLICKR, KMA_WEB_CURRENT_CONDITIONS, KMA_WEB_FORECASTS,
        FREE_TIME, NOMINATIM, RAIN_VIEWER
    }

    fun getApiService(serviceType: ServiceType): RestfulApiQuery {
        when (serviceType) {
            ServiceType.KMA_WEB_CURRENT_CONDITIONS,
            ServiceType.KMA_WEB_FORECASTS -> {
                val kmaHtmlInstance = Retrofit.Builder().client(client).addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(KMA_CURRENT_CONDITIONS_AND_HOURLY_AND_DAILY_FORECAST_URL).build()
                return kmaHtmlInstance.create(RestfulApiQuery::class.java)
            }

            ServiceType.MET_NORWAY_LOCATION_FORECAST -> {
                val metNorwayLocationForecastsInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    MET_NORWAY_LOCATION_FORECAST_SERVICE_URL
                ).build()
                return metNorwayLocationForecastsInstance.create(RestfulApiQuery::class.java)
            }
            ServiceType.AQICN_GEOLOCALIZED_FEED -> {
                val aqiCnGeolocalizedInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    AQICN_GEOLOCALIZED_FEED_SERVICE_URL
                ).build()
                return aqiCnGeolocalizedInstance.create(RestfulApiQuery::class.java)
            }
            ServiceType.OWM_ONE_CALL
            -> {
                val owmBeginApiInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    DEFAULT_OPEN_WEATHER_MAP_SERVICE_URL
                ).build()
                return owmBeginApiInstance.create(RestfulApiQuery::class.java)
            }


            ServiceType.FLICKR -> {

                val flickrInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    FLICKR_SERVICE_URL
                ).build()
                return flickrInstance.create(RestfulApiQuery::class.java)
            }


            ServiceType.FREE_TIME -> {
                val freeTimeInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    FREE_TIME_SERVICE_URL
                ).build()
                return freeTimeInstance.create(RestfulApiQuery::class.java)
            }
            ServiceType.NOMINATIM -> {
                val nominatim = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    NOMINATIM_GEOCODE_URL
                ).build()
                return nominatim.create(RestfulApiQuery::class.java)
            }

            else -> {
                val rainViewer = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    RAIN_VIEWER_URL
                ).build()
                return rainViewer.create(RestfulApiQuery::class.java)
            }
        }
    }

}
