package com.lifedawn.bestweather.data.remote.retrofit.client;


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    //kma
    const val KMA_CURRENT_CONDITIONS_AND_HOURLY_AND_DAILY_FORECAST_URL = "https://www.weather.go.kr/w/wnuri-fct2021/main/"
    const val MID_FCST_INFO_SERVICE_URL = "http://apis.data.go.kr/1360000/MidFcstInfoService/"
    const val VILAGE_FCST_INFO_SERVICE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"

    //met norway
    const val MET_NORWAY_LOCATION_FORECAST_SERVICE_URL = "https://api.met.no/weatherapi/"

    //aqicn
    const val AQICN_GEOLOCALIZED_FEED_SERVICE_URL = "https://api.waqi.info/"

    //openweathermap
    const val DEFAULT_OPEN_WEATHER_MAP_SERVICE_URL = "https://api.openweathermap.org/"
    const val PRO_OPEN_WEATHER_MAP_SERVICE_URL = "https://pro.openweathermap.org/"

    //flickr
    const val FLICKR_SERVICE_URL = "https://www.flickr.com/services/"

    //freeTimeApi
    const val FREE_TIME_SERVICE_URL = "https://timeapi.io/api/TimeZone/"

    //google place search
    const val GOOGLE_PLACE_SEARCH_SERVICE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/"
    const val GOOGLE_FIND_PLACE_SERVICE_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/"

    const val NOMINATIM_GEOCODE_URL = "https://nominatim.openstreetmap.org/"
    const val RAIN_VIEWER_URL = "https://api.rainviewer.com/public/"

    //service key, token
    const val VILAGE_FCST_INFO_SERVICE_SERVICE_KEY =
        "T2nJm9zlOA0Z7Dut%2BThT6Jp0Itn0zZw80AUP3uMdOWlZJR1gVPkx9p1t8etuSW1kWsSNrGGHKdxbwr1IUlt%2Baw%3D%3D"
    const val MID_FCST_INFO_SERVICE_SERVICE_KEY =
        "T2nJm9zlOA0Z7Dut%2BThT6Jp0Itn0zZw80AUP3uMdOWlZJR1gVPkx9p1t8etuSW1kWsSNrGGHKdxbwr1IUlt%2Baw%3D%3D"
    const val ACCU_WEATHER_SERVICE_KEY = "tUnqAFCcGWIyhZf4zSVlKgQb1wsbJOo8"
    const val AQICN_TOKEN = "8538c6118653f6e4acbfd8ae5667bd07683a1cde"
    const val OWM_ONECALL_API_KEY = "4e3a18c58bdf77658cd11a9ee0cb51cc"
    const val FLICKR_KEY = "2c887b8d73b8334ddb3b0809c387de1b"

    const val XML = "XML"
    const val LOG_TAG = "Retrofit Response"

    private val client = OkHttpClient.Builder().readTimeout(4, TimeUnit.SECONDS)
        .connectTimeout(3, TimeUnit.SECONDS).build()

    enum class ServiceType {
        KMA_YESTERDAY_ULTRA_SRT_NCST, KMA_ULTRA_SRT_NCST, KMA_ULTRA_SRT_FCST, KMA_MID_LAND_FCST, KMA_MID_TA_FCST, KMA_VILAGE_FCST,
        ACCU_GEOPOSITION_SEARCH,
        ACCU_CURRENT_CONDITIONS, ACCU_DAILY_FORECAST, ACCU_HOURLY_FORECAST, MET_NORWAY_LOCATION_FORECAST, AQICN_GEOLOCALIZED_FEED,
        OWM_CURRENT_CONDITIONS,
        OWM_HOURLY_FORECAST, OWM_DAILY_FORECAST, OWM_ONE_CALL, FLICKR, KMA_WEB_CURRENT_CONDITIONS, KMA_WEB_FORECASTS,
        GOOGLE_PLACE_SEARCH, FREE_TIME, NOMINATIM, RAIN_VIEWER
    }

    fun getApiService(serviceType: ServiceType): RestfulApiQuery {
        when (serviceType) {
            ServiceType.KMA_WEB_CURRENT_CONDITIONS,
            ServiceType.KMA_WEB_FORECASTS -> {
                val kmaHtmlInstance = Retrofit.Builder().client(client).addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(KMA_CURRENT_CONDITIONS_AND_HOURLY_AND_DAILY_FORECAST_URL).build()
                return kmaHtmlInstance.create(RestfulApiQuery::class.java)
            }
            ServiceType.KMA_MID_LAND_FCST,
            ServiceType.KMA_MID_TA_FCST -> {
                val midFcstInstance = Retrofit.Builder().client(client)
                    .baseUrl(MID_FCST_INFO_SERVICE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                return midFcstInstance.create(RestfulApiQuery::class.java)
            }
            ServiceType.KMA_ULTRA_SRT_NCST,
            ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST,
            ServiceType.KMA_ULTRA_SRT_FCST,
            ServiceType.KMA_VILAGE_FCST -> {
                val vilageFcstInstance = Retrofit.Builder().client(client).baseUrl(VILAGE_FCST_INFO_SERVICE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                return vilageFcstInstance.create(RestfulApiQuery::class.java)
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
            ServiceType.OWM_CURRENT_CONDITIONS,
            ServiceType.OWM_DAILY_FORECAST,
            ServiceType.OWM_ONE_CALL
            -> {
                val owmBeginApiInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    DEFAULT_OPEN_WEATHER_MAP_SERVICE_URL
                ).build()
                return owmBeginApiInstance.create(RestfulApiQuery::class.java)
            }
            ServiceType.OWM_HOURLY_FORECAST -> {
                val owmBeginProInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    PRO_OPEN_WEATHER_MAP_SERVICE_URL
                )
                    .build()
                return owmBeginProInstance.create(RestfulApiQuery::class.java)
            }

            ServiceType.FLICKR -> {

                val flickrInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    FLICKR_SERVICE_URL
                ).build()
                return flickrInstance.create(RestfulApiQuery::class.java)
            }
            ServiceType.GOOGLE_PLACE_SEARCH -> {
                val googlePlaceSearchInstance = Retrofit.Builder().client(client).addConverterFactory(
                    GsonConverterFactory.create()
                ).addConverterFactory(ScalarsConverterFactory.create()).baseUrl(
                    GOOGLE_PLACE_SEARCH_SERVICE_URL
                ).build()
                return googlePlaceSearchInstance.create(RestfulApiQuery::class.java)
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
