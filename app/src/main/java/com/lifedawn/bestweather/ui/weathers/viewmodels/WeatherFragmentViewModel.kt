package com.lifedawn.bestweather.ui.weathers.viewmodels

import android.app.Application
import android.os.Bundle
import android.util.ArrayMap
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WeatherFragmentViewModel(application: Application) : AndroidViewModel(application), OnResumeFragment {
    val resumedFragmentObserver = MutableLiveData<Boolean>()
    var dateTimeFormatter: DateTimeFormatter? = null
    var selectedFavoriteAddressDto: FavoriteAddressDto? = null
    var locationType: LocationType? = null
    var iTextColor: ITextColor? = null
    var mainWeatherProviderType: WeatherProviderType? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var countryCode: String? = null
    var addressName: String? = null
    var zoneId: ZoneId? = null
    var favoriteAddressDto: FavoriteAddressDto? = null
    var multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback? = null
    var arguments: Bundle? = null
    private val FRAGMENT_TOTAL_COUNTS = 7
    private val needDrawFragments = AtomicBoolean(true)
    private val resumedFragmentCount = AtomicInteger(0)
    val weatherDataResponse: MutableLiveData<ResponseResultObj> = MutableLiveData<ResponseResultObj>()
    override fun onCleared() {
        super.onCleared()
        if (multipleWeatherRestApiCallback != null) multipleWeatherRestApiCallback.clear()
        multipleWeatherRestApiCallback = null
        dateTimeFormatter = null
        selectedFavoriteAddressDto = null
        locationType = null
        iTextColor = null
        mainWeatherProviderType = null
        latitude = null
        longitude = null
        countryCode = null
        addressName = null
        zoneId = null
        favoriteAddressDto = null
        arguments = null
    }

    fun requestNewData() {
        needDrawFragments.set(true)
        MyApplication.getExecutorService().submit(Runnable { //메인 날씨 제공사만 요청
            val weatherProviderTypeSet: MutableSet<WeatherProviderType?> = HashSet<WeatherProviderType?>()
            weatherProviderTypeSet.add(mainWeatherProviderType)
            weatherProviderTypeSet.add(WeatherProviderType.AQICN)
            val requestWeatherSources: ArrayMap<WeatherProviderType, RequestWeatherSource> =
                ArrayMap<WeatherProviderType, RequestWeatherSource>()
            setRequestWeatherSourceWithSourceTypes(weatherProviderTypeSet, requestWeatherSources)
            val responseResultObj = ResponseResultObj(weatherProviderTypeSet, requestWeatherSources, mainWeatherProviderType)
            multipleWeatherRestApiCallback = object : MultipleWeatherRestApiCallback() {
                override fun onResult() {
                    multipleWeatherRestApiCallback = this
                    responseResultObj.multipleWeatherRestApiCallback = this
                    weatherDataResponse.postValue(responseResultObj)
                }

                override fun onCanceled() {}
            }
            multipleWeatherRestApiCallback.setZoneId(zoneId)
            MainProcessing.requestNewWeatherData(
                getApplication<Application>().applicationContext, latitude,
                longitude,
                requestWeatherSources, multipleWeatherRestApiCallback
            )
        })
    }

    fun requestNewDataWithAnotherWeatherSource(newWeatherProviderType: WeatherProviderType?) {
        needDrawFragments.set(true)
        MyApplication.getExecutorService().submit(Runnable {
            val requestWeatherSources: ArrayMap<WeatherProviderType, RequestWeatherSource> =
                ArrayMap<WeatherProviderType, RequestWeatherSource>()
            //메인 날씨 제공사만 요청
            val newWeatherProviderTypeSet: MutableSet<WeatherProviderType?> = HashSet<WeatherProviderType?>()
            newWeatherProviderTypeSet.add(newWeatherProviderType)
            newWeatherProviderTypeSet.add(WeatherProviderType.AQICN)
            setRequestWeatherSourceWithSourceTypes(newWeatherProviderTypeSet, requestWeatherSources)
            val responseResultObj = ResponseResultObj(newWeatherProviderTypeSet, requestWeatherSources, newWeatherProviderType)
            multipleWeatherRestApiCallback = object : MultipleWeatherRestApiCallback() {
                override fun onResult() {
                    multipleWeatherRestApiCallback = this
                    responseResultObj.multipleWeatherRestApiCallback = this
                    weatherDataResponse.postValue(responseResultObj)
                }

                override fun onCanceled() {}
            }
            multipleWeatherRestApiCallback.setZoneId(zoneId)
            MainProcessing.requestNewWeatherData(
                getApplication<Application>().applicationContext, latitude,
                longitude, requestWeatherSources, multipleWeatherRestApiCallback
            )
        })
    }

    private fun setRequestWeatherSourceWithSourceTypes(
        weatherProviderTypeSet: Set<WeatherProviderType?>,
        newRequestWeatherSources: ArrayMap<WeatherProviderType, RequestWeatherSource>
    ) {
        if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
            val requestKma = RequestKma()
            requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST).addRequestServiceType(
                RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST
            ).addRequestServiceType(
                RetrofitClient.ServiceType.KMA_VILAGE_FCST
            ).addRequestServiceType(
                RetrofitClient.ServiceType.KMA_MID_LAND_FCST
            ).addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_TA_FCST)
                .addRequestServiceType(RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST)
            newRequestWeatherSources[WeatherProviderType.KMA_WEB] = requestKma
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
            val requestKma = RequestKma()
            requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).addRequestServiceType(
                RetrofitClient.ServiceType.KMA_WEB_FORECASTS
            )
            newRequestWeatherSources[WeatherProviderType.KMA_WEB] = requestKma
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
            val requestOwmOneCall = RequestOwmOneCall()
            requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL)
            val excludes: MutableSet<OneCallApis> = HashSet<OneCallApis>()
            excludes.add(OwmOneCallParameter.OneCallApis.minutely)
            excludes.add(OwmOneCallParameter.OneCallApis.alerts)
            requestOwmOneCall.setExcludeApis(excludes)
            newRequestWeatherSources[WeatherProviderType.OWM_ONECALL] = requestOwmOneCall
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
            val requestMet = RequestMet()
            requestMet.addRequestServiceType(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST)
            newRequestWeatherSources[WeatherProviderType.MET_NORWAY] = requestMet
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
            val requestAccu = RequestAccu()
            requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).addRequestServiceType(
                RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST
            ).addRequestServiceType(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST)
            newRequestWeatherSources[WeatherProviderType.ACCU_WEATHER] = requestAccu
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_INDIVIDUAL)) {
            val requestOwmIndividual = RequestOwmIndividual()
            requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS).addRequestServiceType(
                RetrofitClient.ServiceType.OWM_HOURLY_FORECAST
            ).addRequestServiceType(RetrofitClient.ServiceType.OWM_DAILY_FORECAST)
            newRequestWeatherSources[WeatherProviderType.OWM_INDIVIDUAL] = requestOwmIndividual
        }
        if (weatherProviderTypeSet.contains(WeatherProviderType.AQICN)) {
            val requestAqicn = RequestAqicn()
            requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED)
            newRequestWeatherSources[WeatherProviderType.AQICN] = requestAqicn
        }
    }

    /**
     * kma, accu, owm
     * 요청 : kma, 현재 : owm ->  accu
     * 요청 : kma, 현재 : accu ->  owm
     * 요청 : kma, 현재 : kma ->  owm, accu
     *
     *
     * 요청 : accu, 현재 : accu ->  owm
     * 요청 : accu, 현재 : accu ->  owm, kma (only kr)
     * 요청 : accu, 현재 : owm ->  미 표시
     * 요청 : accu, 현재 : owm ->  kma (only kr)
     * 요청 : accu, 현재 : kma ->  owm
     *
     *
     * 요청 : owm, 현재 : owm ->  accu
     * 요청 : owm, 현재 : owm ->  accu, kma (only kr)
     * 요청 : owm, 현재 : accu ->  미 표시
     * 요청 : owm, 현재 : accu ->  kma (only kr)
     * 요청 : owm, 현재 : kma ->  accu
     */
    fun getOtherWeatherSourceTypes(
        requestWeatherProviderType: WeatherProviderType,
        lastWeatherProviderType: WeatherProviderType
    ): Set<WeatherProviderType> {
        val others: MutableSet<WeatherProviderType> = HashSet<WeatherProviderType>()
        if (requestWeatherProviderType === WeatherProviderType.KMA_WEB) {
            if (lastWeatherProviderType === WeatherProviderType.OWM_ONECALL) {
                others.add(WeatherProviderType.MET_NORWAY)
            } else if (lastWeatherProviderType === WeatherProviderType.MET_NORWAY) {
                others.add(WeatherProviderType.OWM_ONECALL)
            } else {
                others.add(WeatherProviderType.OWM_ONECALL)
                others.add(WeatherProviderType.MET_NORWAY)
            }
        } else if (requestWeatherProviderType === WeatherProviderType.KMA_API) {
            if (lastWeatherProviderType === WeatherProviderType.OWM_ONECALL) {
                others.add(WeatherProviderType.MET_NORWAY)
            } else if (lastWeatherProviderType === WeatherProviderType.MET_NORWAY) {
                others.add(WeatherProviderType.OWM_ONECALL)
            } else {
                others.add(WeatherProviderType.OWM_ONECALL)
                others.add(WeatherProviderType.MET_NORWAY)
            }
        } else if (requestWeatherProviderType === WeatherProviderType.MET_NORWAY) {
            if (lastWeatherProviderType === WeatherProviderType.MET_NORWAY) {
                if (countryCode != null && countryCode == "KR") {
                    others.add(WeatherProviderType.OWM_ONECALL)
                    others.add(WeatherProviderType.KMA_WEB)
                } else {
                    others.add(WeatherProviderType.OWM_ONECALL)
                }
            } else if (lastWeatherProviderType === WeatherProviderType.OWM_ONECALL) {
                if (countryCode != null && countryCode == "KR") {
                    others.add(WeatherProviderType.KMA_WEB)
                }
            } else {
                others.add(WeatherProviderType.OWM_ONECALL)
            }
        } else if (requestWeatherProviderType === WeatherProviderType.OWM_ONECALL) {
            if (lastWeatherProviderType === WeatherProviderType.OWM_ONECALL) {
                if (countryCode != null && countryCode == "KR") {
                    others.add(WeatherProviderType.MET_NORWAY)
                    others.add(WeatherProviderType.KMA_WEB)
                } else {
                    others.add(WeatherProviderType.MET_NORWAY)
                }
            } else if (lastWeatherProviderType === WeatherProviderType.MET_NORWAY) {
                if (countryCode != null && countryCode == "KR") {
                    others.add(WeatherProviderType.KMA_WEB)
                }
            } else {
                others.add(WeatherProviderType.MET_NORWAY)
            }
        } else if (requestWeatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
            if (lastWeatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
                if (countryCode != null && countryCode == "KR") {
                    others.add(WeatherProviderType.MET_NORWAY)
                    others.add(WeatherProviderType.KMA_WEB)
                } else {
                    others.add(WeatherProviderType.MET_NORWAY)
                }
            } else if (lastWeatherProviderType === WeatherProviderType.MET_NORWAY) {
                if (countryCode != null && countryCode == "KR") {
                    others.add(WeatherProviderType.KMA_WEB)
                }
            } else {
                others.add(WeatherProviderType.MET_NORWAY)
            }
        }
        return others
    }

    fun getMainWeatherSourceType(countryCode: String): WeatherProviderType? {
        if (arguments!!.containsKey("anotherProvider")) {
            val weatherProviderType: WeatherProviderType? = arguments!!.getSerializable("anotherProvider") as WeatherProviderType?
            arguments!!.remove("anotherProvider")
            return weatherProviderType
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication<Application>().applicationContext)
        var mainWeatherProviderType: WeatherProviderType = if (sharedPreferences.getBoolean(
                getApplication<Application>().getString(R.string.pref_key_met),
                true
            )
        ) WeatherProviderType.MET_NORWAY else WeatherProviderType.OWM_ONECALL
        if (countryCode == "KR") {
            val kmaIsTopPriority =
                sharedPreferences.getBoolean(getApplication<Application>().getString(R.string.pref_key_kma_top_priority), true)
            if (kmaIsTopPriority) {
                mainWeatherProviderType = WeatherProviderType.KMA_WEB
            }
        }
        return mainWeatherProviderType
    }

    fun createWeatherFragments(
        weatherProviderTypeSet: Set<WeatherProviderType?>, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback,
        latitude: Double?, longitude: Double?
    ): WeatherDataDto {
        val responseMap: Map<WeatherProviderType, ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>> =
            multipleWeatherRestApiCallback.responseMap
        val arrayMap: ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>
        var currentConditionsDto: CurrentConditionsDto? = null
        var hourlyForecastDtoList: List<HourlyForecastDto?>? = null
        var dailyForecastDtoList: List<DailyForecastDto?>? = null
        var currentConditionsWeatherVal: String? = null
        val context = getApplication<Application>().applicationContext
        if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_API)) {
            arrayMap = responseMap[WeatherProviderType.KMA_API]!!
            val finalCurrentConditions: FinalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditionsByXML(
                arrayMap[RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST].getResponseObj() as VilageFcstResponse
            )
            val yesterDayFinalCurrentConditions: FinalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditionsByXML(
                arrayMap[RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST].getResponseObj() as VilageFcstResponse
            )
            val finalHourlyForecastList: List<FinalHourlyForecast> = KmaResponseProcessor.getFinalHourlyForecastListByXML(
                arrayMap[RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST].getResponseObj() as VilageFcstResponse,
                arrayMap[RetrofitClient.ServiceType.KMA_VILAGE_FCST].getResponseObj() as VilageFcstResponse
            )
            var finalDailyForecastList: List<FinalDailyForecast?> = KmaResponseProcessor.getFinalDailyForecastListByXML(
                arrayMap[RetrofitClient.ServiceType.KMA_MID_LAND_FCST].getResponseObj() as MidLandFcstResponse,
                arrayMap[RetrofitClient.ServiceType.KMA_MID_TA_FCST].getResponseObj() as MidTaResponse,
                multipleWeatherRestApiCallback.getValue("tmFc").toLong()
            )
            finalDailyForecastList = KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecastList, finalHourlyForecastList)
            currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfXML(
                context, finalCurrentConditions,
                finalHourlyForecastList[0], latitude, longitude
            )
            currentConditionsDto.setYesterdayTemp(
                ValueUnits.convertTemperature(
                    yesterDayFinalCurrentConditions.getTemperature(),
                    MyApplication.VALUE_UNIT_OBJ.getTempUnit()
                ).toString() + MyApplication.VALUE_UNIT_OBJ.getTempUnitText()
            )
            hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfXML(
                context,
                finalHourlyForecastList, latitude, longitude
            )
            dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfXML(finalDailyForecastList)
            val sky: String = finalHourlyForecastList[0].getSky()
            val pty: String = finalCurrentConditions.getPrecipitationType()
            currentConditionsWeatherVal = if (pty == "0") sky + "_sky" else pty + "_pty"
            mainWeatherProviderType = WeatherProviderType.KMA_API
        } else if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
            arrayMap = responseMap[WeatherProviderType.KMA_WEB]!!
            val parsedKmaCurrentConditions: ParsedKmaCurrentConditions =
                arrayMap[RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS].getResponseObj() as ParsedKmaCurrentConditions
            val forecasts = arrayMap[RetrofitClient.ServiceType.KMA_WEB_FORECASTS].getResponseObj() as Array<Any>
            val parsedKmaHourlyForecasts: ArrayList<ParsedKmaHourlyForecast> = forecasts[0] as ArrayList<ParsedKmaHourlyForecast>
            val parsedKmaDailyForecasts: ArrayList<ParsedKmaDailyForecast> = forecasts[1] as ArrayList<ParsedKmaDailyForecast>
            currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(
                context,
                parsedKmaCurrentConditions, parsedKmaHourlyForecasts[0], latitude, longitude
            )
            hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(
                context,
                parsedKmaHourlyForecasts, latitude, longitude
            )
            dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(parsedKmaDailyForecasts)
            val pty: String = parsedKmaCurrentConditions.pty
            currentConditionsWeatherVal = if (pty.isEmpty()) parsedKmaHourlyForecasts[0].weatherDescription else pty
            mainWeatherProviderType = WeatherProviderType.KMA_WEB
        } else if (weatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
            arrayMap = responseMap[WeatherProviderType.ACCU_WEATHER]!!
            val accuCurrentConditionsResponse: AccuCurrentConditionsResponse =
                arrayMap[RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS].getResponseObj() as AccuCurrentConditionsResponse
            val accuHourlyForecastsResponse: AccuHourlyForecastsResponse =
                arrayMap[RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST].getResponseObj() as AccuHourlyForecastsResponse
            val accuDailyForecastsResponse: AccuDailyForecastsResponse =
                arrayMap[RetrofitClient.ServiceType.ACCU_DAILY_FORECAST].getResponseObj() as AccuDailyForecastsResponse
            currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(
                context, accuCurrentConditionsResponse.getItems().get(0)
            )
            hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(
                context, accuHourlyForecastsResponse.getItems()
            )
            dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(
                context,
                accuDailyForecastsResponse.getDailyForecasts()
            )
            currentConditionsWeatherVal = accuCurrentConditionsResponse.getItems().get(0).getWeatherIcon()
            mainWeatherProviderType = WeatherProviderType.ACCU_WEATHER
        } else if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
            arrayMap = responseMap[WeatherProviderType.OWM_ONECALL]!!
            val owmOneCallResponse: OwmOneCallResponse =
                arrayMap[RetrofitClient.ServiceType.OWM_ONE_CALL].getResponseObj() as OwmOneCallResponse
            currentConditionsDto = OwmResponseProcessor.makeCurrentConditionsDtoOneCall(
                context, owmOneCallResponse, zoneId
            )
            hourlyForecastDtoList = OwmResponseProcessor.makeHourlyForecastDtoListOneCall(
                context, owmOneCallResponse, zoneId
            )
            dailyForecastDtoList = OwmResponseProcessor.makeDailyForecastDtoListOneCall(
                context, owmOneCallResponse, zoneId
            )
            currentConditionsWeatherVal = owmOneCallResponse.current.weather.get(0).id
            mainWeatherProviderType = WeatherProviderType.OWM_ONECALL
        } else if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_INDIVIDUAL)) {
            arrayMap = responseMap[WeatherProviderType.OWM_INDIVIDUAL]!!
            val owmCurrentConditionsResponse: OwmCurrentConditionsResponse =
                arrayMap[RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS].getResponseObj() as OwmCurrentConditionsResponse
            val owmHourlyForecastResponse: OwmHourlyForecastResponse =
                arrayMap[RetrofitClient.ServiceType.OWM_HOURLY_FORECAST].getResponseObj() as OwmHourlyForecastResponse
            val owmDailyForecastResponse: OwmDailyForecastResponse =
                arrayMap[RetrofitClient.ServiceType.OWM_DAILY_FORECAST].getResponseObj() as OwmDailyForecastResponse
            currentConditionsDto = OwmResponseProcessor.makeCurrentConditionsDtoIndividual(
                context, owmCurrentConditionsResponse, zoneId
            )
            hourlyForecastDtoList = OwmResponseProcessor.makeHourlyForecastDtoListIndividual(
                context,
                owmHourlyForecastResponse, zoneId
            )
            dailyForecastDtoList = OwmResponseProcessor.makeDailyForecastDtoListIndividual(
                context,
                owmDailyForecastResponse, zoneId
            )
            currentConditionsWeatherVal = owmCurrentConditionsResponse.weather.get(0).id
            mainWeatherProviderType = WeatherProviderType.OWM_INDIVIDUAL
        } else if (weatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
            arrayMap = responseMap[WeatherProviderType.MET_NORWAY]!!
            val locationForecastResponse: LocationForecastResponse =
                arrayMap[RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST].getResponseObj() as LocationForecastResponse
            currentConditionsDto = MetNorwayResponseProcessor.makeCurrentConditionsDto(
                context, locationForecastResponse, zoneId
            )
            hourlyForecastDtoList = MetNorwayResponseProcessor.makeHourlyForecastDtoList(context, locationForecastResponse, zoneId)
            dailyForecastDtoList = MetNorwayResponseProcessor.makeDailyForecastDtoList(context, locationForecastResponse, zoneId)
            currentConditionsWeatherVal = locationForecastResponse.properties.timeSeries.get(0)
                .data.next_1_hours.summary.symbolCode.replace("day", "").replace("night", "")
                .replace("_", "")
            mainWeatherProviderType = WeatherProviderType.MET_NORWAY
        }
        val aqicnResponse: MultipleWeatherRestApiCallback.ResponseResult? =
            responseMap[WeatherProviderType.AQICN]!![RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED]
        var airQualityResponse: AqiCnGeolocalizedFeedResponse? = null
        if (aqicnResponse != null && aqicnResponse.isSuccessful()) {
            airQualityResponse = aqicnResponse.getResponseObj() as AqiCnGeolocalizedFeedResponse
        }
        val airQualityDto: AirQualityDto = AqicnResponseProcessor.makeAirQualityDto(
            airQualityResponse,
            ZonedDateTime.now(zoneId).offset
        )
        var precipitationVolume = ""
        if (currentConditionsDto.isHasPrecipitationVolume()) {
            precipitationVolume = currentConditionsDto.precipitationVolume
        } else if (currentConditionsDto.isHasRainVolume()) {
            precipitationVolume = currentConditionsDto.rainVolume
        } else if (currentConditionsDto.isHasSnowVolume()) {
            precipitationVolume = currentConditionsDto.snowVolume
        }
        val weatherDataDTO = WeatherDataDto(
            currentConditionsDto,
            hourlyForecastDtoList as ArrayList<HourlyForecastDto?>?, dailyForecastDtoList as ArrayList<DailyForecastDto?>?,
            airQualityDto,
            currentConditionsWeatherVal, latitude, longitude, addressName, countryCode, mainWeatherProviderType, zoneId,
            precipitationVolume,
            airQualityResponse
        )
        dateTimeFormatter = DateTimeFormatter.ofPattern(
            if (MyApplication.VALUE_UNIT_OBJ.getClockUnit() === ValueUnits.clock12) context.getString(R.string.datetime_pattern_clock12) else context.getString(
                R.string.datetime_pattern_clock24
            ), Locale.getDefault()
        )
        resumedFragmentCount.set(0)
        needDrawFragments.set(true)
        return weatherDataDTO
    }

    fun containWeatherData(latitude: Double, longitude: Double): Boolean {
        return FINAL_RESPONSE_MAP.containsKey(latitude.toString() + longitude.toString())
    }

    fun removeOldDownloadedData(latitude: Double, longitude: Double) {
        FINAL_RESPONSE_MAP.remove(latitude.toString() + longitude.toString())
    }

    fun isOldDownloadedData(latitude: Double, longitude: Double): Boolean {
        if (!FINAL_RESPONSE_MAP.containsKey(latitude.toString() + longitude.toString())) return false
        val diff = ChronoUnit.MINUTES.between(
            FINAL_RESPONSE_MAP[latitude.toString() + longitude].dataDownloadedDateTime,
            LocalDateTime.now()
        )
        return diff >= 30
    }

    override fun onResumeWithAsync(fragment: Fragment?) {
        if (needDrawFragments.get() && resumedFragmentCount.incrementAndGet() == FRAGMENT_TOTAL_COUNTS) {
            resumedFragmentCount.set(0)
            needDrawFragments.set(false)
            resumedFragmentObserver.value = true
        }
    }

    companion object {
        val FINAL_RESPONSE_MAP: ConcurrentHashMap<String, WeatherResponseObj> = ConcurrentHashMap<String, WeatherResponseObj>()
        fun clear() {
            for (v in FINAL_RESPONSE_MAP.values) {
                v.dataDownloadedDateTime = null
                v.requestMainWeatherProviderType = null
                v.multipleWeatherRestApiCallback.responseMap.clear()
                v.multipleWeatherRestApiCallback.callMap.clear()
                v.multipleWeatherRestApiCallback.valueMap.clear()
                v.requestWeatherProviderTypeSet.clear()
                v.multipleWeatherRestApiCallback = null
            }
            FINAL_RESPONSE_MAP.clear()
        }
    }
}