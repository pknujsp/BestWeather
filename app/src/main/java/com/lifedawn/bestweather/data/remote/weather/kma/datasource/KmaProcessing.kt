package com.lifedawn.bestweather.data.remote.weather.kma.datasource

import android.content.Context
import android.util.Log
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.local.room.dto.KmaAreaCodeDto
import com.lifedawn.bestweather.data.local.room.repository.KmaAreaCodesRepository
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient.ServiceType
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient.getApiService
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.*
import com.lifedawn.bestweather.data.remote.weather.kma.parser.KmaWebParser
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.LocationDistance
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object KmaProcessing {
    /**
     * 현재 날씨 web
     */
    fun getCurrentConditionsData(parameter: KmaCurrentConditionsParameters, callback: JsonDownloader): Call<String> {
        val restfulApiQuery = getApiService(ServiceType.KMA_WEB_CURRENT_CONDITIONS)
        val call: Call<String> = restfulApiQuery.getKmaCurrentConditions(parameter.parametersMap)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.body() != null) {
                    val currentConditionsDocument = Jsoup.parse(response.body())
                    val parsedKmaCurrentConditions = KmaWebParser.parseCurrentConditions(
                        currentConditionsDocument,
                        ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString()
                    )
                    if (parsedKmaCurrentConditions.temp == "자료없음" || parsedKmaCurrentConditions.temp!!.contains("999")) {
                        callback.onResponseResult(Exception())
                        Log.e(RetrofitClient.LOG_TAG, "kma current conditions 실패")
                    } else {
                        callback.onResponseResult(response, parsedKmaCurrentConditions, response.body())
                        Log.e(RetrofitClient.LOG_TAG, "kma current conditions 성공")
                    }
                } else {
                    callback.onResponseResult(Exception())
                    Log.e(RetrofitClient.LOG_TAG, "kma current conditions 실패")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                callback.onResponseResult(t)
                Log.e(RetrofitClient.LOG_TAG, "kma current conditions 실패")
            }
        })
        return call
    }

    /**
     * 시간별, 일별 web
     */
    fun getForecastsData(parameter: KmaForecastsParameters, callback: JsonDownloader): Call<String> {
        val restfulApiQuery = getApiService(ServiceType.KMA_WEB_FORECASTS)
        val call: Call<String> = restfulApiQuery.getKmaHourlyAndDailyForecast(parameter.parametersMap)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.body() != null) {
                    val forecastsDocument = Jsoup.parse(response.body())
                    val parsedKmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(forecastsDocument)
                    val parsedKmaDailyForecasts = KmaWebParser.parseDailyForecasts(forecastsDocument)
                    KmaWebParser.makeExtendedDailyForecasts(parsedKmaHourlyForecasts, parsedKmaDailyForecasts)
                    val lists = arrayOf<Any>(parsedKmaHourlyForecasts, parsedKmaDailyForecasts)
                    callback.onResponseResult(response, lists, response.body())
                    Log.e(RetrofitClient.LOG_TAG, "kma forecasts 성공")
                } else {
                    callback.onResponseResult(Exception())
                    Log.e(RetrofitClient.LOG_TAG, "kma forecasts 실패")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                callback.onResponseResult(t)
                Log.e(RetrofitClient.LOG_TAG, "kma forecasts 실패")
            }
        })
        return call
    }

    /**
     * 초단기 실황
     */
    fun getUltraSrtNcstData(
        parameter: UltraSrtNcstParameter, dateTime: ZonedDateTime,
        callback: JsonDownloader
    ): Call<String> {
        //basetime설정
        var dateTime = dateTime
        if (dateTime.minute < 40) {
            dateTime = dateTime.minusHours(1)
        }
        val yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")
        val HH = DateTimeFormatter.ofPattern("HH")
        parameter.baseDate = dateTime.toLocalDate().format(yyyyMMdd)
        parameter.baseTime = dateTime.toLocalTime().format(HH) + "00"
        val restfulApiQuery = getApiService(ServiceType.KMA_ULTRA_SRT_NCST)
        val call: Call<String> = restfulApiQuery.getUltraSrtNcstByXml(parameter.map)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                val vilageFcstResponse = KmaResponseProcessor.successfulVilageResponse(response)
                if (vilageFcstResponse != null) {
                    callback.onResponseResult(response, vilageFcstResponse, response.body())
                    Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 성공")
                } else {
                    callback.onResponseResult(Exception())
                    Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 실패")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                callback.onResponseResult(t)
                Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 실패")
            }
        })
        return call
    }

    /**
     * 1일전(정확히는 23시간 58분전) 초단기 실황
     */
    fun getYesterdayUltraSrtNcstData(
        parameter: UltraSrtNcstParameter, dateTime: ZonedDateTime,
        callback: JsonDownloader
    ): Call<String> {
        var dateTime = dateTime
        val yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")
        val HH = DateTimeFormatter.ofPattern("HHmm")
        dateTime = dateTime.minusHours(23).minusMinutes(58)
        parameter.baseDate = dateTime.format(yyyyMMdd)
        parameter.baseTime = dateTime.format(HH)
        val restfulApiQuery = getApiService(ServiceType.KMA_ULTRA_SRT_NCST)
        val call: Call<String> = restfulApiQuery.getUltraSrtNcstByXml(parameter.map)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                val vilageFcstResponse = KmaResponseProcessor.successfulVilageResponse(response)
                if (vilageFcstResponse != null) {
                    callback.onResponseResult(response, vilageFcstResponse, response.body())
                    Log.e(RetrofitClient.LOG_TAG, "yesterday kma ultra srt ncst 성공")
                } else {
                    callback.onResponseResult(Exception())
                    Log.e(RetrofitClient.LOG_TAG, "yesterday kma ultra srt ncst 실패")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                callback.onResponseResult(t)
                Log.e(RetrofitClient.LOG_TAG, "yesterday kma ultra srt ncst 실패")
            }
        })
        return call
    }

    /**
     * 초단기예보
     */
    fun getUltraSrtFcstData(
        parameter: UltraSrtFcstParameter, dateTime: ZonedDateTime,
        callback: JsonDownloader
    ): Call<String> {
        //basetime설정
        var dateTime = dateTime
        if (dateTime.minute < 45) {
            dateTime = dateTime.minusHours(1)
        }
        val yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")
        val HH = DateTimeFormatter.ofPattern("HH")
        parameter.baseDate = dateTime.toLocalDate().format(yyyyMMdd)
        parameter.baseTime = dateTime.toLocalTime().format(HH) + "30"
        val restfulApiQuery = getApiService(ServiceType.KMA_ULTRA_SRT_FCST)
        val xmlCall: Call<String> = restfulApiQuery.getUltraSrtFcstByXml(parameter.map)
        xmlCall.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                val vilageFcstResponse = KmaResponseProcessor.successfulVilageResponse(response)
                if (vilageFcstResponse != null) {
                    callback.onResponseResult(response, vilageFcstResponse, response.body())
                    Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 성공")
                } else {
                    callback.onResponseResult(Exception())
                    Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 실패")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                callback.onResponseResult(t)
                Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 실패")
            }
        })
        return xmlCall
    }

    /**
     * 동네예보
     *
     *
     * - Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
     * - API 제공 시간(~이후) : 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
     */
    fun getVilageFcstData(
        parameter: VilageFcstParameter, dateTime: ZonedDateTime,
        callback: JsonDownloader
    ): Call<String> {
        //basetime설정
        var dateTime = dateTime
        val currentHour = dateTime.hour
        val currentMinute = dateTime.minute
        var i = if (currentHour >= 0 && currentHour <= 2) 7 else currentHour / 3 - 1
        var baseHour = 0
        if (currentMinute > 10 && (currentHour - 2) % 3 == 0) {
            // ex)1411인 경우
            baseHour = 3 * ((currentHour - 2) / 3) + 2
            i = 0
        } else {
            baseHour = 3 * i + 2
        }
        dateTime = if (i == 7) {
            dateTime.minusDays(1).withHour(23)
        } else {
            dateTime.withHour(baseHour)
        }
        val yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")
        val HH = DateTimeFormatter.ofPattern("HH")
        parameter.baseDate = dateTime.toLocalDate().format(yyyyMMdd)
        parameter.baseTime = dateTime.toLocalTime().format(HH) + "00"
        val restfulApiQuery = getApiService(ServiceType.KMA_VILAGE_FCST)
        val call: Call<String> = restfulApiQuery.getVilageFcstByXml(parameter.map)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                val vilageFcstResponse = KmaResponseProcessor.successfulVilageResponse(response)
                if (vilageFcstResponse != null) {
                    callback.onResponseResult(response, vilageFcstResponse, response.body())
                    Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 성공")
                } else {
                    callback.onResponseResult(Exception())
                    Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 실패")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                callback.onResponseResult(t)
                Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 실패")
            }
        })
        return call
    }

    /**
     * 중기육상예보
     *
     * @param parameter
     */
    fun getMidLandFcstData(parameter: MidLandParameter, callback: JsonDownloader): Call<String> {
        val restfulApiQuery = getApiService(ServiceType.KMA_MID_LAND_FCST)
        val call: Call<String> = restfulApiQuery.getMidLandFcstByXml(parameter.map)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                val midLandFcstResponse = KmaResponseProcessor.successfulMidLandFcstResponse(response)
                if (midLandFcstResponse != null) {
                    callback.onResponseResult(response, midLandFcstResponse, response.body())
                    Log.e(RetrofitClient.LOG_TAG, "kma mid land 성공")
                } else {
                    callback.onResponseResult(Exception())
                    Log.e(RetrofitClient.LOG_TAG, "kma mid land 실패")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                callback.onResponseResult(t)
                Log.e(RetrofitClient.LOG_TAG, "kma mid land 실패")
            }
        })
        return call
    }

    /**
     * 중기기온조회
     *
     * @param parameter
     */
    fun getMidTaData(parameter: MidTaParameter, callback: JsonDownloader): Call<String> {
        val restfulApiQuery = getApiService(ServiceType.KMA_MID_TA_FCST)
        val call: Call<String> = restfulApiQuery.getMidTaByXml(parameter.map)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                val midTaResponse = KmaResponseProcessor.successfulMidTaFcstResponse(response)
                if (midTaResponse != null) {
                    callback.onResponseResult(response, midTaResponse, response.body())
                    Log.e(RetrofitClient.LOG_TAG, "kma mid ta 성공")
                } else {
                    callback.onResponseResult(Exception())
                    Log.e(RetrofitClient.LOG_TAG, "kma mid ta 실패")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                callback.onResponseResult(t)
                Log.e(RetrofitClient.LOG_TAG, "kma mid ta 실패")
            }
        })
        return call
    }

    fun getTmFc(dateTime: ZonedDateTime): String {
        var dateTime = dateTime
        val hour = dateTime.hour
        val minute = dateTime.minute
        val yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")
        return if (hour >= 18 && minute >= 1) {
            dateTime = dateTime.withHour(18)
            dateTime.format(yyyyMMdd) + "1800"
        } else if (hour >= 6 && minute >= 1) {
            dateTime = dateTime.withHour(6)
            dateTime.format(yyyyMMdd) + "0600"
        } else {
            dateTime = dateTime.minusDays(1).withHour(18)
            dateTime.format(yyyyMMdd) + "1800"
        }
    }

    fun requestWeatherDataAsXML(
        context: Context?, latitude: Double?, longitude: Double?,
        requestKma: RequestKma,
        multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback
    ) {
        val kmaAreaCodesRepository = KmaAreaCodesRepository.getINSTANCE()
        kmaAreaCodesRepository.getAreaCodes(
            latitude!!, longitude!!,
            object : DbQueryCallback<List<KmaAreaCodeDto?>?> {
                override fun onResultSuccessful(result: List<KmaAreaCodeDto>) {
                    val criteriaLatLng = doubleArrayOf(latitude, longitude)
                    var minDistance = Double.MAX_VALUE
                    var distance = 0.0
                    val compLatLng = DoubleArray(2)
                    var nearbyKmaAreaCodeDto: KmaAreaCodeDto? = null
                    for (weatherAreaCodeDTO in result) {
                        compLatLng[0] = weatherAreaCodeDTO.latitudeSecondsDivide100.toDouble()
                        compLatLng[1] = weatherAreaCodeDTO.longitudeSecondsDivide100.toDouble()
                        distance = LocationDistance.distance(
                            criteriaLatLng[0], criteriaLatLng[1], compLatLng[0], compLatLng[1],
                            LocationDistance.Unit.METER
                        )
                        if (distance < minDistance) {
                            minDistance = distance
                            nearbyKmaAreaCodeDto = weatherAreaCodeDTO
                        }
                    }
                    val koreaLocalDateTime = ZonedDateTime.now(KmaResponseProcessor.zoneId)
                    multipleWeatherRestApiCallback.putValue("koreaLocalDateTime", koreaLocalDateTime.toString())
                    val tmFc = getTmFc(koreaLocalDateTime)
                    multipleWeatherRestApiCallback.putValue("tmFc", tmFc)
                    val requestTypeSet = requestKma.requestServiceTypes
                    if (requestTypeSet.contains(ServiceType.KMA_ULTRA_SRT_NCST)) {
                        val ultraSrtNcstParameter = UltraSrtNcstParameter()
                        ultraSrtNcstParameter.setNx(nearbyKmaAreaCodeDto!!.x).setNy(nearbyKmaAreaCodeDto.y)
                            .setLatitude(latitude).longitude = longitude
                        val ultraSrtNcstCall = getUltraSrtNcstData(ultraSrtNcstParameter,
                            ZonedDateTime.of(koreaLocalDateTime.toLocalDateTime(), koreaLocalDateTime.zone),
                            object : JsonDownloader() {
                                fun onResponseResult(response: Response<*>?, responseObj: Any?, responseText: String?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, ultraSrtNcstParameter,
                                        ServiceType.KMA_ULTRA_SRT_NCST, response, responseObj, responseText
                                    )
                                }

                                fun onResponseResult(t: Throwable?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, ultraSrtNcstParameter,
                                        ServiceType.KMA_ULTRA_SRT_NCST, t
                                    )
                                }
                            })
                        multipleWeatherRestApiCallback.getCallMap()[ServiceType.KMA_ULTRA_SRT_NCST] = ultraSrtNcstCall
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST)) {
                        val ultraSrtNcstParameter = UltraSrtNcstParameter()
                        ultraSrtNcstParameter.setNx(nearbyKmaAreaCodeDto!!.x).setNy(nearbyKmaAreaCodeDto.y)
                            .setLatitude(latitude).longitude = longitude
                        val yesterdayUltraSrtNcstCall = getYesterdayUltraSrtNcstData(ultraSrtNcstParameter,
                            ZonedDateTime.of(koreaLocalDateTime.toLocalDateTime(), koreaLocalDateTime.zone),
                            object : JsonDownloader() {
                                fun onResponseResult(response: Response<*>?, responseObj: Any?, responseText: String?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, ultraSrtNcstParameter,
                                        ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST, response, responseObj, responseText
                                    )
                                }

                                fun onResponseResult(t: Throwable?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, ultraSrtNcstParameter,
                                        ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST, t
                                    )
                                }
                            })
                        multipleWeatherRestApiCallback.getCallMap()[ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST] = yesterdayUltraSrtNcstCall
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_ULTRA_SRT_FCST)) {
                        val ultraSrtFcstParameter = UltraSrtFcstParameter()
                        ultraSrtFcstParameter.setNx(nearbyKmaAreaCodeDto!!.x).setNy(nearbyKmaAreaCodeDto.y)
                            .setLatitude(latitude).longitude = longitude
                        val ultraSrtFcstCall = getUltraSrtFcstData(ultraSrtFcstParameter,
                            ZonedDateTime.of(koreaLocalDateTime.toLocalDateTime(), koreaLocalDateTime.zone),
                            object : JsonDownloader() {
                                fun onResponseResult(response: Response<*>?, responseObj: Any?, responseText: String?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, ultraSrtFcstParameter,
                                        ServiceType.KMA_ULTRA_SRT_FCST, response, responseObj, responseText
                                    )
                                }

                                fun onResponseResult(t: Throwable?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, ultraSrtFcstParameter,
                                        ServiceType.KMA_ULTRA_SRT_FCST, t
                                    )
                                }
                            })
                        multipleWeatherRestApiCallback.getCallMap()[ServiceType.KMA_ULTRA_SRT_FCST] = ultraSrtFcstCall
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_VILAGE_FCST)) {
                        val vilageFcstParameter = VilageFcstParameter()
                        vilageFcstParameter.setNx(nearbyKmaAreaCodeDto!!.x).setNy(nearbyKmaAreaCodeDto.y)
                            .setLatitude(latitude).longitude = longitude
                        val vilageFcstCall = getVilageFcstData(vilageFcstParameter,
                            ZonedDateTime.of(koreaLocalDateTime.toLocalDateTime(), koreaLocalDateTime.zone),
                            object : JsonDownloader() {
                                fun onResponseResult(response: Response<*>?, responseObj: Any?, responseText: String?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, vilageFcstParameter,
                                        ServiceType.KMA_VILAGE_FCST, response, responseObj, responseText
                                    )
                                }

                                fun onResponseResult(t: Throwable?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, vilageFcstParameter,
                                        ServiceType.KMA_VILAGE_FCST, t
                                    )
                                }
                            })
                        multipleWeatherRestApiCallback.getCallMap()[ServiceType.KMA_VILAGE_FCST] = vilageFcstCall
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_MID_LAND_FCST)) {
                        val midLandParameter = MidLandParameter()
                        midLandParameter.setRegId(nearbyKmaAreaCodeDto!!.midLandFcstCode).setTmFc(tmFc)
                            .setLatitude(latitude).longitude = longitude
                        val midLandFcstCall = getMidLandFcstData(midLandParameter, object : JsonDownloader() {
                            fun onResponseResult(response: Response<*>?, responseObj: Any?, responseText: String?) {
                                multipleWeatherRestApiCallback.processResult(
                                    WeatherProviderType.KMA_WEB, midLandParameter,
                                    ServiceType.KMA_MID_LAND_FCST, response, responseObj, responseText
                                )
                            }

                            fun onResponseResult(t: Throwable?) {
                                multipleWeatherRestApiCallback.processResult(
                                    WeatherProviderType.KMA_WEB, midLandParameter,
                                    ServiceType.KMA_MID_LAND_FCST, t
                                )
                            }
                        })
                        multipleWeatherRestApiCallback.getCallMap()[ServiceType.KMA_MID_LAND_FCST] = midLandFcstCall
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_MID_TA_FCST)) {
                        val midTaParameter = MidTaParameter()
                        midTaParameter.setRegId(nearbyKmaAreaCodeDto!!.midTaCode).setTmFc(tmFc)
                            .setLatitude(latitude).longitude = longitude
                        val midTaFcstCall = getMidTaData(midTaParameter, object : JsonDownloader() {
                            fun onResponseResult(response: Response<*>?, responseObj: Any?, responseText: String?) {
                                multipleWeatherRestApiCallback.processResult(
                                    WeatherProviderType.KMA_WEB, midTaParameter,
                                    ServiceType.KMA_MID_TA_FCST, response, responseObj, responseText
                                )
                            }

                            fun onResponseResult(t: Throwable?) {
                                multipleWeatherRestApiCallback.processResult(
                                    WeatherProviderType.KMA_WEB, midTaParameter,
                                    ServiceType.KMA_MID_TA_FCST, t
                                )
                            }
                        })
                        multipleWeatherRestApiCallback.getCallMap()[ServiceType.KMA_MID_TA_FCST] = midTaFcstCall
                    }
                }

                override fun onResultNoData() {
                    val exception = Exception("not found lat,lon")
                    val requestTypeSet = requestKma.requestServiceTypes
                    if (requestTypeSet.contains(ServiceType.KMA_ULTRA_SRT_NCST)) {
                        multipleWeatherRestApiCallback.processResult(
                            WeatherProviderType.KMA_WEB, null,
                            ServiceType.KMA_ULTRA_SRT_NCST, exception
                        )
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_ULTRA_SRT_FCST)) {
                        multipleWeatherRestApiCallback.processResult(
                            WeatherProviderType.KMA_WEB, null,
                            ServiceType.KMA_ULTRA_SRT_FCST, exception
                        )
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_VILAGE_FCST)) {
                        multipleWeatherRestApiCallback.processResult(
                            WeatherProviderType.KMA_WEB, null,
                            ServiceType.KMA_VILAGE_FCST, exception
                        )
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_MID_LAND_FCST)) {
                        multipleWeatherRestApiCallback.processResult(
                            WeatherProviderType.KMA_WEB, null,
                            ServiceType.KMA_MID_LAND_FCST, exception
                        )
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_MID_TA_FCST)) {
                        multipleWeatherRestApiCallback.processResult(
                            WeatherProviderType.KMA_WEB, null,
                            ServiceType.KMA_MID_TA_FCST, exception
                        )
                    }
                }
            })
    }

    @JvmStatic
    fun requestWeatherDataAsWEB(
        context: Context?, latitude: Double, longitude: Double,
        requestKma: RequestKma, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback
    ) {
        KmaResponseProcessor.init(context)
        val kmaAreaCodesRepository = KmaAreaCodesRepository.getINSTANCE()
        kmaAreaCodesRepository.getAreaCodes(
            latitude!!, longitude!!,
            object : DbQueryCallback<List<KmaAreaCodeDto?>?> {
                override fun onResultSuccessful(result: List<KmaAreaCodeDto>) {
                    val criteriaLatLng = doubleArrayOf(latitude, longitude)
                    var minDistance = Double.MAX_VALUE
                    var distance = 0.0
                    val compLatLng = DoubleArray(2)
                    var nearbyKmaAreaCodeDto: KmaAreaCodeDto? = null
                    for (weatherAreaCodeDTO in result) {
                        compLatLng[0] = weatherAreaCodeDTO.latitudeSecondsDivide100.toDouble()
                        compLatLng[1] = weatherAreaCodeDTO.longitudeSecondsDivide100.toDouble()
                        distance = LocationDistance.distance(
                            criteriaLatLng[0], criteriaLatLng[1], compLatLng[0], compLatLng[1],
                            LocationDistance.Unit.METER
                        )
                        if (distance < minDistance) {
                            minDistance = distance
                            nearbyKmaAreaCodeDto = weatherAreaCodeDTO
                        }
                    }
                    val koreaLocalDateTime = ZonedDateTime.now(KmaResponseProcessor.zoneId)
                    multipleWeatherRestApiCallback.putValue("koreaLocalDateTime", koreaLocalDateTime.toString())
                    val tmFc = getTmFc(koreaLocalDateTime)
                    multipleWeatherRestApiCallback.putValue("tmFc", tmFc)
                    val requestTypeSet = requestKma.requestServiceTypes
                    val code = nearbyKmaAreaCodeDto!!.administrativeAreaCode
                    if (requestTypeSet.contains(ServiceType.KMA_WEB_CURRENT_CONDITIONS)) {
                        val parameters = KmaCurrentConditionsParameters(code)
                        parameters.setLatitude(latitude).longitude = longitude
                        val currentConditionsCall = getCurrentConditionsData(parameters,
                            object : JsonDownloader() {
                                fun onResponseResult(response: Response<*>?, responseObj: Any?, responseText: String?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, parameters,
                                        ServiceType.KMA_WEB_CURRENT_CONDITIONS, response, responseObj, responseText
                                    )
                                }

                                fun onResponseResult(t: Throwable?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, parameters,
                                        ServiceType.KMA_WEB_CURRENT_CONDITIONS, t
                                    )
                                }
                            })
                        multipleWeatherRestApiCallback.getCallMap()[ServiceType.KMA_WEB_CURRENT_CONDITIONS] = currentConditionsCall
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_WEB_FORECASTS)) {
                        val parameters = KmaForecastsParameters(code)
                        parameters.setLatitude(latitude).longitude = longitude
                        val forecastsCall = getForecastsData(parameters,
                            object : JsonDownloader() {
                                fun onResponseResult(response: Response<*>?, responseObj: Any?, responseText: String?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, parameters,
                                        ServiceType.KMA_WEB_FORECASTS, response, responseObj, responseText
                                    )
                                }

                                fun onResponseResult(t: Throwable?) {
                                    multipleWeatherRestApiCallback.processResult(
                                        WeatherProviderType.KMA_WEB, parameters,
                                        ServiceType.KMA_WEB_FORECASTS, t
                                    )
                                }
                            })
                        multipleWeatherRestApiCallback.getCallMap()[ServiceType.KMA_WEB_FORECASTS] = forecastsCall
                    }
                }

                override fun onResultNoData() {
                    val exception = Exception("not found lat,lon")
                    val requestTypeSet = requestKma.requestServiceTypes
                    if (requestTypeSet.contains(ServiceType.KMA_WEB_CURRENT_CONDITIONS)) {
                        multipleWeatherRestApiCallback.processResult(
                            WeatherProviderType.KMA_WEB, null,
                            ServiceType.KMA_WEB_CURRENT_CONDITIONS, exception
                        )
                    }
                    if (requestTypeSet.contains(ServiceType.KMA_WEB_FORECASTS)) {
                        multipleWeatherRestApiCallback.processResult(
                            WeatherProviderType.KMA_WEB, null,
                            ServiceType.KMA_WEB_FORECASTS, exception
                        )
                    }
                }
            })
    }
}