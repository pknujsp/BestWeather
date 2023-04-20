package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util

import android.content.Context
import android.util.ArrayMap
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.requestweathersource.*
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall.OwmOneCallParameter.OneCallApis
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.init
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.request.MainProcessing
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.init
import com.lifedawn.bestweather.data.remote.weather.metnorway.MetNorwayResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.metnorway.MetNorwayResponseProcessor.init
import com.lifedawn.bestweather.data.remote.weather.owm.OwmResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.owm.OwmResponseProcessor.init
import java.time.ZoneId
import java.util.concurrent.ExecutorService

object WeatherRequestUtil {
    @JvmStatic
    fun loadWeatherData(
        context: Context?, executorService: ExecutorService, latitude: Double?, longitude: Double?,
        weatherDataTypeSet: Set<WeatherDataType?>,
        multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback, weatherProviderTypeSet: Set<WeatherProviderType?>, zoneId: ZoneId?
    ) {
        executorService.submit {
            multipleWeatherRestApiCallback.setZoneId(zoneId)
            val requestWeatherSources = ArrayMap<WeatherProviderType, RequestWeatherSource>()
            setRequestWeatherSourceWithSourceType(weatherProviderTypeSet, requestWeatherSources, weatherDataTypeSet)
            MainProcessing.requestNewWeatherData(context, latitude, longitude, requestWeatherSources, multipleWeatherRestApiCallback)
        }
    }

    @JvmStatic
    fun initWeatherSourceUniqueValues(weatherProviderType: WeatherProviderType?, aqi: Boolean, context: Context?) {
        when (weatherProviderType) {
            WeatherProviderType.KMA_WEB -> KmaResponseProcessor.init(context!!)
            ACCU_WEATHER -> AccuWeatherResponseProcessor.init(context)
            WeatherProviderType.OWM_ONECALL -> OwmResponseProcessor.init(context!!)
            WeatherProviderType.MET_NORWAY -> MetNorwayResponseProcessor.init(context!!)
        }
        if (aqi) AqicnResponseProcessor.init(context!!)
    }

    fun setRequestWeatherSourceWithSourceType(
        weatherProviderTypeSet: Set<WeatherProviderType?>,
        requestWeatherSources: ArrayMap<WeatherProviderType, RequestWeatherSource>,
        weatherDataTypeSet: Set<WeatherDataType?>
    ) {
        if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_API)) {
            val requestKma = RequestKma()
            requestWeatherSources[WeatherProviderType.KMA_API] = requestKma
            if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST)
                requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
                requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST)
                    .addRequestServiceType(RetrofitClient.ServiceType.KMA_VILAGE_FCST)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_LAND_FCST)
                    .addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_TA_FCST)
                    .addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST)
                    .addRequestServiceType(RetrofitClient.ServiceType.KMA_VILAGE_FCST)
            }
        } else if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
            val requestKma = RequestKma()
            requestWeatherSources[WeatherProviderType.KMA_WEB] = requestKma
            if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS)
                requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast) || weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)
            }
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
            val requestAccu = RequestAccu()
            requestWeatherSources[WeatherProviderType.ACCU_WEATHER] = requestAccu
            if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
                requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST)
            }
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
            val requestOwmOneCall = RequestOwmOneCall()
            requestWeatherSources[WeatherProviderType.OWM_ONECALL] = requestOwmOneCall
            val excludeSet: MutableSet<OneCallApis> = HashSet()
            excludeSet.add(OneCallApis.daily)
            excludeSet.add(OneCallApis.hourly)
            excludeSet.add(OneCallApis.minutely)
            excludeSet.add(OneCallApis.alerts)
            excludeSet.add(OneCallApis.current)
            if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                excludeSet.remove(OneCallApis.current)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
                excludeSet.remove(OneCallApis.hourly)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                excludeSet.remove(OneCallApis.daily)
            }
            requestOwmOneCall.excludeApis = excludeSet
            requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL)
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
            val requestMet = RequestMet()
            requestWeatherSources[WeatherProviderType.MET_NORWAY] = requestMet
            requestMet.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL)
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_INDIVIDUAL)) {
            val requestOwmIndividual = RequestOwmIndividual()
            requestWeatherSources[WeatherProviderType.OWM_INDIVIDUAL] = requestOwmIndividual
            if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
                requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST)
            }
            if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_DAILY_FORECAST)
            }
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.AQICN)) {
            val requestAqicn = RequestAqicn()
            requestWeatherSources[WeatherProviderType.AQICN] = requestAqicn
            if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
                requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED)
            }
        }
    }

    @JvmStatic
    fun getMainWeatherSourceType(context: Context, countryCode: String?): WeatherProviderType {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var mainWeatherProviderType: WeatherProviderType? = null
        mainWeatherProviderType = if (sharedPreferences.getBoolean(context.getString(R.string.pref_key_met), true)) {
            WeatherProviderType.MET_NORWAY
        } else {
            WeatherProviderType.OWM_ONECALL
        }
        if (countryCode != null) {
            if (countryCode == "KR") {
                val kmaIsTopPriority = sharedPreferences.getBoolean(context.getString(R.string.pref_key_kma_top_priority), true)
                if (kmaIsTopPriority) {
                    mainWeatherProviderType = WeatherProviderType.KMA_WEB
                }
            }
        }
        return mainWeatherProviderType
    }
}