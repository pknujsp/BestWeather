package com.lifedawn.bestweather.data.remote.weather.dataprocessing.response

import android.content.Context
import android.util.ArrayMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast.Val
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient.ServiceType
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaCurrentConditionsParameters
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaForecastsParameters
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.UltraSrtNcstParameter
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.VilageFcstParameter
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.currentconditions.AccuCurrentConditionsResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.dailyforecasts.AccuDailyForecastsResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.hourlyforecasts.AccuHourlyForecastsResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midtaresponse.MidTaResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.LocationForecastResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.currentweather.OwmCurrentConditionsResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.dailyforecast.OwmDailyForecastResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.hourlyforecast.OwmHourlyForecastResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.onecall.OwmOneCallResponse
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WindUtil
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.getDailyForecastListByXML
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.getFinalCurrentConditionsByXML
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.getFinalDailyForecastListByXML
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.getFinalHourlyForecastListByXML
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.makeCurrentConditionsDtoOfWEB
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.makeCurrentConditionsDtoOfXML
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.makeDailyForecastDtoListOfWEB
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.makeDailyForecastDtoListOfXML
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.makeHourlyForecastDtoListOfWEB
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor.makeHourlyForecastDtoListOfXML
import com.lifedawn.bestweather.data.remote.weather.kma.parser.KmaWebParser
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaCurrentConditions
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaDailyForecast
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaHourlyForecast
import com.tickaroo.tikxml.TikXml
import okio.Buffer
import org.jsoup.Jsoup
import us.dustinj.timezonemap.TimeZoneMap
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

open class WeatherResponseProcessor {
    companion object {
        val TIMEZONE_MAP: MutableMap<String, ZoneId> = HashMap()

        fun containsTimeZone(latitude: Double, longitude: Double): Boolean {
            return TIMEZONE_MAP.containsKey(latitude.toString() + longitude.toString())
        }

        fun getTimeZone(latitude: Double, longitude: Double): ZoneId? {
            return TIMEZONE_MAP[latitude.toString() + longitude.toString()]
        }

        fun putTimeZone(latitude: Double, longitude: Double, zoneId: ZoneId): ZoneId? {
            return TIMEZONE_MAP.put(latitude.toString() + longitude.toString(), zoneId)
        }

        fun convertDateTimeOfDailyForecast(millis: Long, zoneId: ZoneId?): ZonedDateTime {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId).withHour(0).withMinute(0).withSecond(0).withNano(0)
        }

        fun convertDateTimeOfCurrentConditions(millis: Long, zoneId: ZoneId?): ZonedDateTime {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId)
        }

        fun convertDateTimeOfHourlyForecast(millis: Long, zoneId: ZoneId?): ZonedDateTime {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId).withMinute(0).withSecond(0).withNano(0)
        }

        fun getZoneId(latitude: Double, longitude: Double): ZoneId? {
            val key = latitude.toString() + longitude.toString()
            if (TIMEZONE_MAP.containsKey(key)) {
                return TIMEZONE_MAP[key]
            }
            val map = TimeZoneMap.forRegion(
                latitude - 4.0,
                longitude - 4.0, latitude + 4.0, longitude + 4.0
            )
            val zoneId = ZoneId.of(map.getOverlappingTimeZone(latitude, longitude)!!.zoneId)
            TIMEZONE_MAP[key] = zoneId
            return zoneId
        }

        fun getAirQualityDto(
            multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
            zoneOffset: ZoneOffset?
        ): AirQualityDto? {
            if (multipleWeatherRestApiCallback == null) {
                return null
            }
            val aqiCnResponseResult: MultipleWeatherRestApiCallback.ResponseResult =
                multipleWeatherRestApiCallback.responseMap[WeatherProviderType.AQICN]!![ServiceType.AQICN_GEOLOCALIZED_FEED]
            return AqicnResponseProcessor.makeAirQualityDto(
                aqiCnResponseResult.getResponseObj() as AqiCnGeolocalizedFeedResponse,
                zoneOffset
            )
        }

        fun parseTextToCurrentConditionsDto(
            context: Context?, jsonObject: JsonObject,
            weatherProviderType: WeatherProviderType, latitude: Double?,
            longitude: Double?, zoneId: ZoneId?
        ): CurrentConditionsDto? {
            var currentConditionsDto: CurrentConditionsDto? = null
            val weatherSourceElement = jsonObject.getAsJsonObject(weatherProviderType.name)
            if (weatherProviderType === WeatherProviderType.KMA_API) {
                if (weatherSourceElement[ServiceType.KMA_ULTRA_SRT_NCST.name()] != null &&
                    weatherSourceElement[ServiceType.KMA_ULTRA_SRT_FCST.name()] != null
                ) {
                    try {
                        val tikXml = TikXml.Builder().exceptionOnUnreadXml(false).build()
                        val ultraSrtNcstResponse = tikXml.read<VilageFcstResponse>(
                            Buffer().writeUtf8(weatherSourceElement[ServiceType.KMA_ULTRA_SRT_NCST.name()].asString),
                            VilageFcstResponse::class.java
                        )
                        val ultraSrtFcstResponse = tikXml.read<VilageFcstResponse>(
                            Buffer().writeUtf8(weatherSourceElement[ServiceType.KMA_ULTRA_SRT_FCST.name()].asString),
                            VilageFcstResponse::class.java
                        )
                        val finalCurrentConditions = getFinalCurrentConditionsByXML(ultraSrtNcstResponse)
                        val finalHourlyForecastList = getFinalHourlyForecastListByXML(ultraSrtFcstResponse, null)
                        currentConditionsDto = makeCurrentConditionsDtoOfXML(
                            context, finalCurrentConditions,
                            finalHourlyForecastList[0],
                            latitude, longitude
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (weatherProviderType === WeatherProviderType.KMA_WEB) {
                if (weatherSourceElement[ServiceType.KMA_WEB_CURRENT_CONDITIONS.name] != null) {
                    val lastUpdatedDateTime = jsonObject["lastUpdatedDateTime"].asString
                    val currentConditionsDocument = Jsoup.parse(weatherSourceElement[ServiceType.KMA_WEB_CURRENT_CONDITIONS.name].asString)
                    val hourlyForecastDocument = Jsoup.parse(weatherSourceElement[ServiceType.KMA_WEB_FORECASTS.name].asString)
                    val parsedKmaCurrentConditions = KmaWebParser.parseCurrentConditions(currentConditionsDocument, lastUpdatedDateTime)
                    val parsedKmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(hourlyForecastDocument)
                    currentConditionsDto = makeCurrentConditionsDtoOfWEB(
                        context, parsedKmaCurrentConditions,
                        parsedKmaHourlyForecasts[0], latitude, longitude
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.ACCU_WEATHER) {
                val jsonElement = weatherSourceElement[ServiceType.ACCU_CURRENT_CONDITIONS.name()]
                val accuCurrentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(jsonElement)
                currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(
                    context,
                    accuCurrentConditionsResponse.items[0]
                )
            } else if (weatherProviderType === WeatherProviderType.OWM_ONECALL) {
                val owmOneCallResponse =
                    OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement[ServiceType.OWM_ONE_CALL.name].asString)
                currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoOneCall(
                    context, owmOneCallResponse, zoneId
                )
            } else if (weatherProviderType === WeatherProviderType.MET_NORWAY) {
                val metNorwayResponse =
                    MetNorwayResponseProcessor.getLocationForecastResponseObjFromJson(weatherSourceElement[ServiceType.MET_NORWAY_LOCATION_FORECAST.name].asString)
                currentConditionsDto = MetNorwayResponseProcessor.makeCurrentConditionsDto(
                    context, metNorwayResponse,
                    zoneId
                )
            } else if (weatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
                val owmCurrentConditionsResponse = OpenWeatherMapResponseProcessor.getOwmCurrentConditionsResponseFromJson(
                    weatherSourceElement[ServiceType.OWM_CURRENT_CONDITIONS.name()].asString
                )
                currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoIndividual(
                    context,
                    owmCurrentConditionsResponse, zoneId
                )
            }
            return currentConditionsDto
        }

        fun getCurrentConditionsDto(
            context: Context?, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
            weatherProviderType: WeatherProviderType, zoneId: ZoneId?
        ): CurrentConditionsDto? {
            if (multipleWeatherRestApiCallback == null) {
                return null
            }
            var currentConditionsDto: CurrentConditionsDto? = null
            if (weatherProviderType === WeatherProviderType.KMA_API) {
                val ultraSrtNcstResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[WeatherProviderType.KMA_API]!![ServiceType.KMA_ULTRA_SRT_NCST]
                val ultraSrtFcstResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[WeatherProviderType.KMA_API]!![ServiceType.KMA_ULTRA_SRT_FCST]
                if (ultraSrtNcstResponseResult != null && ultraSrtFcstResponseResult != null && ultraSrtNcstResponseResult.isSuccessful()) {
                    val finalCurrentConditions =
                        getFinalCurrentConditionsByXML((ultraSrtNcstResponseResult.getResponseObj() as VilageFcstResponse))
                    val finalHourlyForecastList = getFinalHourlyForecastListByXML(
                        (ultraSrtFcstResponseResult.getResponseObj() as VilageFcstResponse), null
                    )
                    val ultraSrtNcstParameter = ultraSrtNcstResponseResult.getRequestParameter() as UltraSrtNcstParameter
                    currentConditionsDto = makeCurrentConditionsDtoOfXML(
                        context, finalCurrentConditions,
                        finalHourlyForecastList[0],
                        ultraSrtNcstParameter.latitude, ultraSrtNcstParameter.longitude
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.KMA_WEB) {
                val currentConditionsResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[WeatherProviderType.KMA_WEB]!![ServiceType.KMA_WEB_CURRENT_CONDITIONS]
                val hourlyForecastsResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[WeatherProviderType.KMA_WEB]!![ServiceType.KMA_WEB_FORECASTS]
                if (currentConditionsResponseResult != null && hourlyForecastsResponseResult != null &&
                    currentConditionsResponseResult.isSuccessful() && hourlyForecastsResponseResult.isSuccessful()
                ) {
                    val parsedKmaCurrentConditions = currentConditionsResponseResult.getResponseObj() as ParsedKmaCurrentConditions
                    val forecasts = hourlyForecastsResponseResult.getResponseObj() as Array<Any>
                    val parsedKmaHourlyForecasts = forecasts[0] as ArrayList<ParsedKmaHourlyForecast>
                    val parameters = currentConditionsResponseResult.getRequestParameter() as KmaCurrentConditionsParameters
                    currentConditionsDto = makeCurrentConditionsDtoOfWEB(
                        context, parsedKmaCurrentConditions,
                        parsedKmaHourlyForecasts[0],
                        parameters.latitude, parameters.longitude
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.ACCU_WEATHER) {
                val currentConditionsResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[WeatherProviderType.ACCU_WEATHER]
                        .get(ServiceType.ACCU_CURRENT_CONDITIONS)
                if (currentConditionsResponseResult != null && currentConditionsResponseResult.isSuccessful()) {
                    val accuCurrentConditionsResponse = currentConditionsResponseResult.getResponseObj() as AccuCurrentConditionsResponse
                    currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(
                        context,
                        accuCurrentConditionsResponse.items[0]
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_ONECALL) {
                val owmResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[WeatherProviderType.OWM_ONECALL]
                        .get(ServiceType.OWM_ONE_CALL)
                if (owmResponseResult != null && owmResponseResult.isSuccessful()) {
                    val owmOneCallResponse = owmResponseResult.getResponseObj() as OwmOneCallResponse
                    currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoOneCall(
                        context, owmOneCallResponse, zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
                val owmCurrentConditionsResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[weatherProviderType]
                        .get(ServiceType.OWM_CURRENT_CONDITIONS)
                if (owmCurrentConditionsResponseResult != null && owmCurrentConditionsResponseResult.isSuccessful()) {
                    val owmCurrentConditionsResponse = owmCurrentConditionsResponseResult.getResponseObj() as OwmCurrentConditionsResponse
                    currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoIndividual(
                        context,
                        owmCurrentConditionsResponse, zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.MET_NORWAY) {
                val metResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[WeatherProviderType.MET_NORWAY]
                        .get(ServiceType.MET_NORWAY_LOCATION_FORECAST)
                if (metResponseResult != null && metResponseResult.isSuccessful()) {
                    val metResponse = metResponseResult.getResponseObj() as LocationForecastResponse
                    currentConditionsDto = MetNorwayResponseProcessor.makeCurrentConditionsDto(context, metResponse, zoneId)
                }
            }
            return currentConditionsDto
        }

        fun parseTextToHourlyForecastDtoList(
            context: Context?, jsonObject: JsonObject,
            weatherProviderType: WeatherProviderType, latitude: Double?,
            longitude: Double?, zoneId: ZoneId?
        ): List<HourlyForecastDto?> {
            var hourlyForecastDtoList: List<HourlyForecastDto?> = ArrayList()
            val weatherSourceElement = jsonObject.getAsJsonObject(weatherProviderType.name)
            if (weatherProviderType === WeatherProviderType.KMA_API) {
                if (weatherSourceElement[ServiceType.KMA_ULTRA_SRT_FCST.name()] != null &&
                    weatherSourceElement[ServiceType.KMA_VILAGE_FCST.name()] != null
                ) {
                    val tikXml = TikXml.Builder().exceptionOnUnreadXml(false).build()
                    try {
                        val ultraSrtFcstResponse = tikXml.read<VilageFcstResponse>(
                            Buffer().writeUtf8(weatherSourceElement[ServiceType.KMA_ULTRA_SRT_FCST.name()].asString),
                            VilageFcstResponse::class.java
                        )
                        val vilageFcstResponse = tikXml.read<VilageFcstResponse>(
                            Buffer().writeUtf8(weatherSourceElement[ServiceType.KMA_VILAGE_FCST.name()].asString),
                            VilageFcstResponse::class.java
                        )
                        val finalHourlyForecastList = getFinalHourlyForecastListByXML(
                            ultraSrtFcstResponse,
                            vilageFcstResponse
                        )
                        hourlyForecastDtoList = makeHourlyForecastDtoListOfXML(
                            context, finalHourlyForecastList,
                            latitude!!, longitude!!
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (weatherProviderType === WeatherProviderType.KMA_WEB) {
                val hourlyForecastDocument = Jsoup.parse(weatherSourceElement[ServiceType.KMA_WEB_FORECASTS.name].asString)
                val parsedKmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(hourlyForecastDocument)
                hourlyForecastDtoList = makeHourlyForecastDtoListOfWEB(
                    context, parsedKmaHourlyForecasts, latitude!!, longitude!!
                )
            } else if (weatherProviderType === WeatherProviderType.ACCU_WEATHER) {
                if (weatherSourceElement[ServiceType.ACCU_HOURLY_FORECAST.name()] != null) {
                    val jsonText = weatherSourceElement[ServiceType.ACCU_HOURLY_FORECAST.name()].asString
                    val accuJsonArr = JsonParser.parseString(jsonText) as JsonArray
                    val hourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
                        accuJsonArr
                    )
                    hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.items)
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_ONECALL) {
                if (weatherSourceElement[ServiceType.OWM_ONE_CALL.name] != null) {
                    val owmOneCallResponse =
                        OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement[ServiceType.OWM_ONE_CALL.name].asString)
                    hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListOneCall(
                        context, owmOneCallResponse, zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
                val owmHourlyForecastResponse =
                    OpenWeatherMapResponseProcessor.getOwmHourlyForecastResponseFromJson(weatherSourceElement[ServiceType.OWM_HOURLY_FORECAST.name()].asString)
                hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListIndividual(
                    context, owmHourlyForecastResponse, zoneId
                )
            } else if (weatherProviderType === WeatherProviderType.MET_NORWAY) {
                if (weatherSourceElement[ServiceType.MET_NORWAY_LOCATION_FORECAST.name] != null) {
                    val metNorwayResponse =
                        MetNorwayResponseProcessor.getLocationForecastResponseObjFromJson(weatherSourceElement[ServiceType.MET_NORWAY_LOCATION_FORECAST.name].asString)
                    hourlyForecastDtoList = MetNorwayResponseProcessor.makeHourlyForecastDtoList(
                        context, metNorwayResponse,
                        zoneId
                    )
                }
            }
            return hourlyForecastDtoList
        }

        fun getHourlyForecastDtoList(
            context: Context?,
            multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
            weatherProviderType: WeatherProviderType,
            zoneId: ZoneId?
        ): List<HourlyForecastDto?> {
            var hourlyForecastDtoList: List<HourlyForecastDto?> = ArrayList()
            if (multipleWeatherRestApiCallback == null) {
                return hourlyForecastDtoList
            }
            val responseMap: Map<WeatherProviderType, ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>> =
                multipleWeatherRestApiCallback.responseMap
            if (weatherProviderType === WeatherProviderType.KMA_API) {
                val ultraSrtFcstResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.KMA_API]!![ServiceType.KMA_ULTRA_SRT_FCST]
                val vilageFcstResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.KMA_API]!![ServiceType.KMA_VILAGE_FCST]
                if (ultraSrtFcstResponseResult != null && vilageFcstResponseResult != null && ultraSrtFcstResponseResult.isSuccessful() && vilageFcstResponseResult.isSuccessful()) {
                    val ultraSrtFcstRoot = ultraSrtFcstResponseResult.getResponseObj() as VilageFcstResponse
                    val vilageFcstRoot = vilageFcstResponseResult.getResponseObj() as VilageFcstResponse
                    val vilageFcstParameter = vilageFcstResponseResult.getRequestParameter() as VilageFcstParameter
                    val finalHourlyForecastList = getFinalHourlyForecastListByXML(
                        ultraSrtFcstRoot,
                        vilageFcstRoot
                    )
                    hourlyForecastDtoList = makeHourlyForecastDtoListOfXML(
                        context, finalHourlyForecastList,
                        vilageFcstParameter.latitude, vilageFcstParameter.longitude
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.KMA_WEB) {
                val hourlyForecastsResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[weatherProviderType]!![ServiceType.KMA_WEB_FORECASTS]
                if (hourlyForecastsResponseResult != null && hourlyForecastsResponseResult.isSuccessful()) {
                    val forecasts = hourlyForecastsResponseResult.getResponseObj() as Array<Any>
                    val parsedKmaHourlyForecasts = forecasts[0] as ArrayList<ParsedKmaHourlyForecast>
                    val parameters = hourlyForecastsResponseResult.getRequestParameter() as KmaForecastsParameters
                    hourlyForecastDtoList = makeHourlyForecastDtoListOfWEB(
                        context, parsedKmaHourlyForecasts,
                        parameters.latitude, parameters.longitude
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.ACCU_WEATHER) {
                val hourlyForecastResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[weatherProviderType]!![ServiceType.ACCU_HOURLY_FORECAST]
                if (hourlyForecastResponseResult != null && hourlyForecastResponseResult.isSuccessful()) {
                    val hourlyForecastsResponse = hourlyForecastResponseResult.getResponseObj() as AccuHourlyForecastsResponse
                    hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.items)
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_ONECALL) {
                val responseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[weatherProviderType]!![ServiceType.OWM_ONE_CALL]
                if (responseResult != null && responseResult.isSuccessful()) {
                    val owmOneCallResponse = responseResult.getResponseObj() as OwmOneCallResponse
                    hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListOneCall(
                        context, owmOneCallResponse,
                        zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
                val owmHourlyForecastResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    multipleWeatherRestApiCallback.responseMap[weatherProviderType]
                        .get(ServiceType.OWM_HOURLY_FORECAST)
                if (owmHourlyForecastResponseResult != null && owmHourlyForecastResponseResult.isSuccessful()) {
                    val owmHourlyForecastResponse = owmHourlyForecastResponseResult.getResponseObj() as OwmHourlyForecastResponse
                    hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListIndividual(
                        context,
                        owmHourlyForecastResponse, zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.MET_NORWAY) {
                val responseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[weatherProviderType]!![ServiceType.MET_NORWAY_LOCATION_FORECAST]
                if (responseResult != null && responseResult.isSuccessful()) {
                    val metNorwayResponse = responseResult.getResponseObj() as LocationForecastResponse
                    hourlyForecastDtoList = MetNorwayResponseProcessor.makeHourlyForecastDtoList(context, metNorwayResponse, zoneId)
                }
            }
            return hourlyForecastDtoList
        }

        fun parseTextToDailyForecastDtoList(
            context: Context?, jsonObject: JsonObject,
            weatherProviderType: WeatherProviderType, zoneId: ZoneId?
        ): List<DailyForecastDto?> {
            var dailyForecastDtoList: List<DailyForecastDto?> = ArrayList()
            val weatherSourceElement = jsonObject.getAsJsonObject(weatherProviderType.name)
            if (weatherProviderType === WeatherProviderType.KMA_API) {
                if (weatherSourceElement[ServiceType.KMA_VILAGE_FCST.name()] != null && weatherSourceElement[ServiceType.KMA_ULTRA_SRT_FCST.name()] != null && weatherSourceElement[ServiceType.KMA_MID_TA_FCST.name()] != null && weatherSourceElement[ServiceType.KMA_MID_LAND_FCST.name()] != null && weatherSourceElement["tmFc"] != null) {
                    val tikXml = TikXml.Builder().exceptionOnUnreadXml(false).build()
                    try {
                        val ultraSrtFcstResponse = tikXml.read<VilageFcstResponse>(
                            Buffer().writeUtf8(weatherSourceElement[ServiceType.KMA_ULTRA_SRT_FCST.name()].asString),
                            VilageFcstResponse::class.java
                        )
                        val vilageFcstResponse = tikXml.read<VilageFcstResponse>(
                            Buffer().writeUtf8(weatherSourceElement[ServiceType.KMA_VILAGE_FCST.name()].asString),
                            VilageFcstResponse::class.java
                        )
                        val midLandFcstResponse = tikXml.read<MidLandFcstResponse>(
                            Buffer().writeUtf8(weatherSourceElement[ServiceType.KMA_MID_LAND_FCST.name()].asString),
                            MidLandFcstResponse::class.java
                        )
                        val midTaFcstResponse = tikXml.read<MidTaResponse>(
                            Buffer().writeUtf8(weatherSourceElement[ServiceType.KMA_MID_TA_FCST.name()].asString),
                            MidTaResponse::class.java
                        )
                        val finalHourlyForecastList = getFinalHourlyForecastListByXML(
                            ultraSrtFcstResponse,
                            vilageFcstResponse
                        )
                        val finalDailyForecastList = getFinalDailyForecastListByXML(
                            midLandFcstResponse, midTaFcstResponse,
                            weatherSourceElement["tmFc"].asLong
                        )
                        dailyForecastDtoList = makeDailyForecastDtoListOfXML(
                            getDailyForecastListByXML(finalDailyForecastList, finalHourlyForecastList)
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (weatherProviderType === WeatherProviderType.KMA_WEB) {
                val forecastDocument = Jsoup.parse(weatherSourceElement[ServiceType.KMA_WEB_FORECASTS.name].asString)
                val parsedKmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(forecastDocument)
                val parsedKmaDailyForecasts = KmaWebParser.parseDailyForecasts(forecastDocument)
                KmaWebParser.makeExtendedDailyForecasts(parsedKmaHourlyForecasts, parsedKmaDailyForecasts)
                dailyForecastDtoList = makeDailyForecastDtoListOfWEB(parsedKmaDailyForecasts)
            } else if (weatherProviderType === WeatherProviderType.ACCU_WEATHER) {
                if (weatherSourceElement[ServiceType.ACCU_DAILY_FORECAST.name()] != null) {
                    val hourlyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
                        weatherSourceElement[ServiceType.ACCU_DAILY_FORECAST.name()].asString
                    )
                    dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(
                        context, hourlyForecastsResponse.dailyForecasts
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_ONECALL) {
                if (weatherSourceElement[ServiceType.OWM_ONE_CALL.name] != null) {
                    val owmOneCallResponse =
                        OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement[ServiceType.OWM_ONE_CALL.name].asString)
                    dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListOneCall(
                        context, owmOneCallResponse,
                        zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
                if (weatherSourceElement[ServiceType.OWM_DAILY_FORECAST.name()] != null) {
                    val owmDailyForecastResponse = OpenWeatherMapResponseProcessor.getOwmDailyForecastResponseFromJson(
                        weatherSourceElement[ServiceType.OWM_DAILY_FORECAST.name()].asString
                    )
                    dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListIndividual(
                        context, owmDailyForecastResponse,
                        zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.MET_NORWAY) {
                if (weatherSourceElement[ServiceType.MET_NORWAY_LOCATION_FORECAST.name] != null) {
                    val metNorwayResponse =
                        MetNorwayResponseProcessor.getLocationForecastResponseObjFromJson(weatherSourceElement[ServiceType.MET_NORWAY_LOCATION_FORECAST.name].asString)
                    dailyForecastDtoList = MetNorwayResponseProcessor.makeDailyForecastDtoList(context, metNorwayResponse, zoneId)
                }
            }
            return dailyForecastDtoList
        }

        fun getDailyForecastDtoList(
            context: Context?, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
            weatherProviderType: WeatherProviderType, zoneId: ZoneId?
        ): List<DailyForecastDto?> {
            var dailyForecastDtoList: List<DailyForecastDto?> = ArrayList()
            if (multipleWeatherRestApiCallback == null) {
                return dailyForecastDtoList
            }
            val responseMap: Map<WeatherProviderType, ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>> =
                multipleWeatherRestApiCallback.responseMap
            if (weatherProviderType === WeatherProviderType.KMA_API) {
                val midLandFcstResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.KMA_API]!![ServiceType.KMA_MID_LAND_FCST]
                val midTaFcstResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.KMA_API]!![ServiceType.KMA_MID_TA_FCST]
                val vilageFcstResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.KMA_API]!![ServiceType.KMA_VILAGE_FCST]
                val ultraSrtFcstResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.KMA_API]!![ServiceType.KMA_ULTRA_SRT_FCST]
                if (midLandFcstResponseResult.isSuccessful() && midTaFcstResponseResult.isSuccessful() &&
                    vilageFcstResponseResult.isSuccessful() && ultraSrtFcstResponseResult.isSuccessful()
                ) {
                    val midLandFcstRoot = midLandFcstResponseResult.getResponseObj() as MidLandFcstResponse
                    val midTaRoot = midTaFcstResponseResult.getResponseObj() as MidTaResponse
                    val vilageFcstRoot = vilageFcstResponseResult.getResponseObj() as VilageFcstResponse
                    val ultraSrtFcstRoot = ultraSrtFcstResponseResult.getResponseObj() as VilageFcstResponse
                    val finalHourlyForecasts = getFinalHourlyForecastListByXML(
                        ultraSrtFcstRoot,
                        vilageFcstRoot
                    )
                    val finalDailyForecasts = getFinalDailyForecastListByXML(
                        midLandFcstRoot, midTaRoot, multipleWeatherRestApiCallback.getValue("tmFc")!!
                            .toLong()
                    )
                    dailyForecastDtoList = makeDailyForecastDtoListOfXML(
                        getDailyForecastListByXML(finalDailyForecasts, finalHourlyForecasts)
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.KMA_WEB) {
                val dailyForecastsResponseResult: MultipleWeatherRestApiCallback.ResponseResult =
                    multipleWeatherRestApiCallback.responseMap[WeatherProviderType.KMA_WEB]!![ServiceType.KMA_WEB_FORECASTS]
                if (dailyForecastsResponseResult.isSuccessful()) {
                    val forecasts = dailyForecastsResponseResult.getResponseObj() as Array<Any>
                    val parsedKmaDailyForecasts = forecasts[1] as ArrayList<ParsedKmaDailyForecast>
                    dailyForecastDtoList = makeDailyForecastDtoListOfWEB(parsedKmaDailyForecasts)
                }
            } else if (weatherProviderType === WeatherProviderType.ACCU_WEATHER) {
                val dailyForecastResponseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.ACCU_WEATHER]!![ServiceType.ACCU_DAILY_FORECAST]
                if (dailyForecastResponseResult.isSuccessful()) {
                    val dailyForecastResponse = dailyForecastResponseResult.getResponseObj() as AccuDailyForecastsResponse
                    dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(
                        context, dailyForecastResponse.dailyForecasts
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_ONECALL) {
                val responseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.OWM_ONECALL]!![ServiceType.OWM_ONE_CALL]
                if (responseResult.isSuccessful()) {
                    val owmOneCallResponse = responseResult.getResponseObj() as OwmOneCallResponse
                    dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListOneCall(
                        context, owmOneCallResponse, zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
                val responseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[weatherProviderType]!![ServiceType.OWM_DAILY_FORECAST]
                if (responseResult.isSuccessful()) {
                    val owmDailyForecastResponse = responseResult.getResponseObj() as OwmDailyForecastResponse
                    dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListIndividual(
                        context,
                        owmDailyForecastResponse, zoneId
                    )
                }
            } else if (weatherProviderType === WeatherProviderType.MET_NORWAY) {
                val responseResult: MultipleWeatherRestApiCallback.ResponseResult? =
                    responseMap[WeatherProviderType.MET_NORWAY]!![ServiceType.MET_NORWAY_LOCATION_FORECAST]
                if (responseResult.isSuccessful()) {
                    val metResponse = responseResult.getResponseObj() as LocationForecastResponse
                    dailyForecastDtoList = MetNorwayResponseProcessor.makeDailyForecastDtoList(
                        context, metResponse,
                        zoneId
                    )
                }
            }
            return dailyForecastDtoList
        }

        fun getTempCurrentConditionsDto(context: Context): CurrentConditionsDto {
            val tempCurrentConditions = CurrentConditionsDto()
            tempCurrentConditions.setTemp(context.getString(R.string.temp_temperature))
                .setFeelsLikeTemp(context.getString(R.string.temp_temperature)).setWeatherIcon(
                R.drawable.day_clear
            ).setWindDirectionDegree(context.getString(R.string.temp_tempWindDirectionDegree).toInt())
                .setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, tempCurrentConditions.windDirectionDegree.toString()))
                .setWindSpeed(context.getString(R.string.temp_windSpeed))
                .setHumidity(context.getString(R.string.temp_humidity))
                .setWindStrength(context.getString(R.string.temp_simpleWindStrength))
            return tempCurrentConditions
        }

        fun getTempHourlyForecastDtoList(context: Context, count: Int): List<HourlyForecastDto> {
            val tempDegree = context.getString(R.string.temp_temperature)
            val zeroSnowVolume = context.getString(R.string.temp_snowVolume)
            val zeroRainVolume = context.getString(R.string.temp_rainVolume)
            val hourlyForecastDtoList: MutableList<HourlyForecastDto> = ArrayList()
            var zonedDateTime = ZonedDateTime.now()
            for (i in 0 until count) {
                val hourlyForecastDto = HourlyForecastDto()
                hourlyForecastDto.setHours(zonedDateTime)
                    .setWeatherIcon(R.drawable.day_clear)
                    .setTemp(tempDegree)
                    .setPop(context.getString(R.string.temp_pop))
                    .setHasRain(false)
                    .setHasSnow(false)
                    .setRainVolume(zeroRainVolume)
                    .setSnowVolume(zeroSnowVolume).feelsLikeTemp = tempDegree
                hourlyForecastDtoList.add(hourlyForecastDto)
                zonedDateTime = zonedDateTime.plusHours(1)
            }
            return hourlyForecastDtoList
        }

        fun getTempDailyForecastDtoList(context: Context, count: Int): List<DailyForecastDto> {
            val minTemp = context.getString(R.string.temp_minTemperature)
            val maxTemp = context.getString(R.string.temp_maxTemperature)
            val zeroSnowVolume = context.getString(R.string.temp_snowVolume)
            val zeroRainVolume = context.getString(R.string.temp_rainVolume)
            val pop = context.getString(R.string.temp_pop)
            val dailyForecastDtoList: MutableList<DailyForecastDto> = ArrayList()
            var zonedDateTime = ZonedDateTime.now()
            for (i in 0 until count) {
                val dailyForecastDto = DailyForecastDto()
                dailyForecastDto.valuesList.add(DailyForecastDto.Values())
                dailyForecastDto.valuesList.add(DailyForecastDto.Values())
                dailyForecastDto.setMinTemp(minTemp).setMaxTemp(maxTemp).setDate(zonedDateTime).valuesList[0]
                    .setWeatherIcon(R.drawable.day_clear).setPop(pop)
                    .setRainVolume(zeroRainVolume).snowVolume = zeroSnowVolume
                dailyForecastDto.valuesList[1].setWeatherIcon(R.drawable.day_clear).setPop(pop)
                    .setRainVolume(zeroRainVolume).snowVolume = zeroSnowVolume
                dailyForecastDtoList.add(dailyForecastDto)
                zonedDateTime = zonedDateTime.plusDays(1)
            }
            return dailyForecastDtoList
        }

       val tempAirQualityDto: AirQualityDto
            get() {
                val airQualityDto = AirQualityDto()
                airQualityDto.isSuccessful = true
                airQualityDto.setAqi(160).cityName = "CityName"
                val current = AirQualityDto.Current()
                airQualityDto.current = current
                current.o3 = 10
                current.no2 = 20
                current.co = 30
                current.so2 = 40
                current.pm25 = 100
                current.pm10 = 200
                current.dew = 70
                var zonedDateTime = ZonedDateTime.now()
                val dailyForecastList: MutableList<DailyForecast> = ArrayList()
                airQualityDto.dailyForecastList = dailyForecastList
                val `val` = Val()
                `val`.setMin(10).setMax(20).avg = 15
                for (i in 0..6) {
                    val dailyForecast = DailyForecast()
                    dailyForecast.setDate(zonedDateTime).setUvi(`val`).setO3(`val`).setPm25(`val`).pm10 = `val`
                    dailyForecastList.add(dailyForecast)
                    zonedDateTime = zonedDateTime.plusDays(1)
                }
                return airQualityDto
            }

        fun getMainWeatherSourceType(requestWeatherProviderTypeSet: Set<WeatherProviderType?>): WeatherProviderType {
            return if (requestWeatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
                WeatherProviderType.KMA_WEB
            } else if (requestWeatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
                WeatherProviderType.ACCU_WEATHER
            } else if (requestWeatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
                WeatherProviderType.OWM_ONECALL
            } else if (requestWeatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
                WeatherProviderType.MET_NORWAY
            } else {
                WeatherProviderType.OWM_INDIVIDUAL
            }
        }
    }
}