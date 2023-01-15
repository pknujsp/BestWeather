package com.lifedawn.bestweather.data.remote.weather.aqicn

import android.content.Context
import android.util.ArrayMap
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast.Val
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse
import com.lifedawn.bestweather.ui.weathers.simplefragment.aqicn.AirQualityForecastObj
import retrofit2.Response
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object AqicnResponseProcessor {
    private val AQI_GRADES = IntArray(5)
    private val AQI_GRADE_COLORS = IntArray(6)
    private val AQI_GRADE_DESCRIPTIONS = arrayOfNulls<String>(6)

    @JvmStatic
    fun init(context: Context) {
        val aqiGrades = context.resources.getIntArray(R.array.AqiGrades)
        val aqiGradeColors = context.resources.getIntArray(R.array.AqiGradeColors)
        val aqiGradeDescriptions = context.resources.getStringArray(R.array.AqiGradeState)
        for (i in aqiGrades.indices) {
            AQI_GRADES[i] = aqiGrades[i]
        }
        for (i in aqiGradeColors.indices) {
            AQI_GRADE_COLORS[i] = aqiGradeColors[i]
        }
        for (i in aqiGradeDescriptions.indices) {
            AQI_GRADE_DESCRIPTIONS[i] = aqiGradeDescriptions[i]
        }
    }

    @JvmStatic
    fun getGradeColorId(grade: Int): Int {
        /*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
        for (i in AQI_GRADES.indices) {
            if (grade <= AQI_GRADES[i]) {
                return AQI_GRADE_COLORS[i]
            }
        }
        //if hazardous
        return AQI_GRADE_COLORS[5]
    }

    /**
     * grade가 -1이면 정보없음을 뜻함
     *
     * @param grade
     * @return
     */
    @JvmStatic
    fun getGradeDescription(grade: Int): String? {
        /*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
        if (grade == -1) {
            return "?"
        }
        for (i in AQI_GRADES.indices) {
            if (grade <= AQI_GRADES[i]) {
                return AQI_GRADE_DESCRIPTIONS[i]
            }
        }
        //if hazardous
        return AQI_GRADE_DESCRIPTIONS[5]
    }

    fun getAirQualityObjFromJson(response: String): AqiCnGeolocalizedFeedResponse? {
        return try {
            Gson().fromJson(response, AqiCnGeolocalizedFeedResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun getAirQualityForecastObjList(
        aqiCnGeolocalizedFeedResponse: AqiCnGeolocalizedFeedResponse,
        timeZone: ZoneId?
    ): List<AirQualityForecastObj> {
        val forecastObjMap = ArrayMap<String, AirQualityForecastObj>()
        val todayDate = LocalDate.now(timeZone)
        var date: LocalDate? = null
        val pm10Forecast = aqiCnGeolocalizedFeedResponse.data.forecast.daily.pm10
        for (valueMap in pm10Forecast) {
            date = getDate(valueMap.day)
            if (date.isBefore(todayDate)) {
                continue
            }
            if (!forecastObjMap.containsKey(valueMap.day)) {
                forecastObjMap[valueMap.day] = AirQualityForecastObj(date)
            }
            forecastObjMap[valueMap.day]!!.pm10 = valueMap.avg.toInt()
        }
        val pm25Forecast = aqiCnGeolocalizedFeedResponse.data.forecast.daily.pm25
        for (valueMap in pm25Forecast) {
            date = getDate(valueMap.day)
            if (date.isBefore(todayDate)) {
                continue
            }
            if (!forecastObjMap.containsKey(valueMap.day)) {
                forecastObjMap[valueMap.day] = AirQualityForecastObj(date)
            }
            forecastObjMap[valueMap.day]!!.pm25 = valueMap.avg.toInt()
        }
        val o3Forecast = aqiCnGeolocalizedFeedResponse.data.forecast.daily.o3
        for (valueMap in o3Forecast) {
            date = getDate(valueMap.day)
            if (date.isBefore(todayDate)) {
                continue
            }
            if (!forecastObjMap.containsKey(valueMap.day)) {
                forecastObjMap[valueMap.day] = AirQualityForecastObj(date)
            }
            forecastObjMap[valueMap.day]!!.o3 = valueMap.avg.toInt()
        }
        val forecastObjArr = arrayOfNulls<AirQualityForecastObj>(1)
        val forecastObjList = Arrays.asList(*forecastObjMap.values.toArray<AirQualityForecastObj>(forecastObjArr))
        Collections.sort(forecastObjList) { forecastObj, t1 -> forecastObj.date.compareTo(t1.date) }
        return forecastObjList
    }

    fun getAirQuality(context: Context, airQualityResponse: AqiCnGeolocalizedFeedResponse?): String? {
        return if (airQualityResponse != null) {
            if (airQualityResponse.status == "ok") {
                val iAqi = airQualityResponse.data.iaqi
                var `val` = Int.MIN_VALUE
                if (iAqi.o3 != null) {
                    `val` = Math.max(`val`, iAqi.o3.value.toDouble().toInt())
                }
                if (iAqi.pm25 != null) {
                    `val` = Math.max(`val`, iAqi.pm25.value.toDouble().toInt())
                }
                if (iAqi.pm10 != null) {
                    `val` = Math.max(`val`, iAqi.pm10.value.toDouble().toInt())
                }
                if (iAqi.no2 != null) {
                    `val` = Math.max(`val`, iAqi.no2.value.toDouble().toInt())
                }
                if (iAqi.so2 != null) {
                    `val` = Math.max(`val`, iAqi.so2.value.toDouble().toInt())
                }
                if (iAqi.co != null) {
                    `val` = Math.max(`val`, iAqi.co.value.toDouble().toInt())
                }
                if (iAqi.dew != null) {
                    `val` = Math.max(`val`, iAqi.dew.value.toDouble().toInt())
                }
                if (`val` == Int.MIN_VALUE) {
                    context.getString(R.string.noData)
                } else {
                    getGradeDescription(`val`)
                }
            } else {
                context.getString(R.string.noData)
            }
        } else {
            context.getString(R.string.noData)
        }
    }

    private fun getDate(day: String): LocalDate {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.parse(day, dateTimeFormatter)
    }

    fun successfulResponse(result: MultipleWeatherRestApiCallback.ResponseResult): Boolean {
        return if (result.getResponse() != null) {
            val response = result.getResponse() as Response<JsonElement>
            if (response.isSuccessful) {
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun makeAirQualityDto(aqiCnGeolocalizedFeedResponse: AqiCnGeolocalizedFeedResponse?, zoneId: ZoneId): AirQualityDto {
        var zoneOffset = zoneOffset
        val airQualityDto = AirQualityDto()
        if (aqiCnGeolocalizedFeedResponse == null) {
            airQualityDto.setAqi(-1).setSuccessful(false)
        } else {
            if (aqiCnGeolocalizedFeedResponse.status == "ok") {
                airQualityDto.setSuccessful(true)
                val data = aqiCnGeolocalizedFeedResponse.data
                if (zoneOffset == null) {
                    zoneOffset = ZoneOffset.of(data.time.tz)
                }
                //-----------------time----
                val time = AirQualityDto.Time()
                time.setS(data.time.s)
                time.setTz(data.time.tz)
                time.setV(data.time.v)
                time.setIso(data.time.iso)
                airQualityDto.setAqi(if (data.aqi == "-") -1 else data.aqi.toDouble().toInt())
                airQualityDto.setIdx(data.idx.toInt())
                airQualityDto.setTimeInfo(time)
                airQualityDto.setLatitude(data.city.geo[0].toDouble())
                airQualityDto.setLongitude(data.city.geo[1].toDouble())
                airQualityDto.setCityName(data.city.name)
                airQualityDto.setAqiCnUrl(data.city.url)
                airQualityDto.setTime(ZonedDateTime.parse(data.time.iso))

                //------------------Current------------------------------------------------------------------------------
                val current = AirQualityDto.Current()
                airQualityDto.setCurrent(current)
                val iAqi = data.iaqi
                current.setPm10(if (iAqi.pm10 != null) iAqi.pm10.value.toDouble().toInt() else -1)
                current.setPm25(if (iAqi.pm25 != null) iAqi.pm25.value.toDouble().toInt() else -1)
                current.setDew(if (iAqi.dew != null) iAqi.dew.value.toDouble().toInt() else -1)
                current.setCo(if (iAqi.co != null) iAqi.co.value.toDouble().toInt() else -1)
                current.setSo2(if (iAqi.so2 != null) iAqi.so2.value.toDouble().toInt() else -1)
                current.setNo2(if (iAqi.no2 != null) iAqi.no2.value.toDouble().toInt() else -1)
                current.setO3(if (iAqi.o3 != null) iAqi.o3.value.toDouble().toInt() else -1)

                //---------- dailyforecast-----------------------------------------------------------------------
                val forecastArrMap = ArrayMap<String, DailyForecast>()
                val todayDate = ZonedDateTime.now(zoneOffset)
                var date = ZonedDateTime.of(todayDate.toLocalDateTime(), zoneOffset)
                var localDate: LocalDate? = null
                val forecast = data.forecast
                val pm10Forecast = forecast.daily.pm10
                if (pm10Forecast != null) {
                    for (valueMap in pm10Forecast) {
                        localDate = getDate(valueMap.day)
                        date = date.withYear(localDate.year).withMonth(localDate.monthValue).withDayOfMonth(localDate.dayOfMonth)
                        if (date.isBefore(todayDate)) {
                            continue
                        }
                        if (!forecastArrMap.containsKey(valueMap.day)) {
                            val dailyForecast = DailyForecast()
                            dailyForecast.setDate(date)
                            forecastArrMap[valueMap.day] = dailyForecast
                        }
                        val pm10 = Val()
                        pm10.setAvg(valueMap.avg.toDouble().toInt())
                        pm10.setMax(valueMap.max.toDouble().toInt())
                        pm10.setMin(valueMap.min.toDouble().toInt())
                        forecastArrMap[valueMap.day].setPm10(pm10)
                    }
                }
                val pm25Forecast = forecast.daily.pm25
                if (pm25Forecast != null) {
                    for (valueMap in pm25Forecast) {
                        localDate = getDate(valueMap.day)
                        date = date.withYear(localDate.year).withMonth(localDate.monthValue).withDayOfMonth(localDate.dayOfMonth)
                        if (date.isBefore(todayDate)) {
                            continue
                        }
                        if (!forecastArrMap.containsKey(valueMap.day)) {
                            val dailyForecast = DailyForecast()
                            dailyForecast.setDate(date)
                            forecastArrMap[valueMap.day] = dailyForecast
                        }
                        val pm25 = Val()
                        pm25.setAvg(valueMap.avg.toDouble().toInt())
                        pm25.setMax(valueMap.max.toDouble().toInt())
                        pm25.setMin(valueMap.min.toDouble().toInt())
                        forecastArrMap[valueMap.day].setPm25(pm25)
                    }
                }
                val o3Forecast = forecast.daily.o3
                if (o3Forecast != null) {
                    for (valueMap in o3Forecast) {
                        localDate = getDate(valueMap.day)
                        date = date.withYear(localDate.year).withMonth(localDate.monthValue).withDayOfMonth(localDate.dayOfMonth)
                        if (date.isBefore(todayDate)) {
                            continue
                        }
                        if (!forecastArrMap.containsKey(valueMap.day)) {
                            val dailyForecast = DailyForecast()
                            dailyForecast.setDate(date)
                            forecastArrMap[valueMap.day] = dailyForecast
                        }
                        val o3 = Val()
                        o3.setAvg(valueMap.avg.toDouble().toInt())
                        o3.setMax(valueMap.max.toDouble().toInt())
                        o3.setMin(valueMap.min.toDouble().toInt())
                        forecastArrMap[valueMap.day].setO3(o3)
                    }
                }
                val uviForecast = aqiCnGeolocalizedFeedResponse.data.forecast.daily.uvi
                if (uviForecast != null) {
                    for (valueMap in uviForecast) {
                        localDate = getDate(valueMap.day)
                        date = date.withYear(localDate.year).withMonth(localDate.monthValue).withDayOfMonth(localDate.dayOfMonth)
                        if (date.isBefore(todayDate)) {
                            continue
                        }
                        if (!forecastArrMap.containsKey(valueMap.day)) {
                            val dailyForecast = DailyForecast()
                            dailyForecast.setDate(date)
                            forecastArrMap[valueMap.day] = dailyForecast
                        }
                        val uvi = Val()
                        uvi.setAvg(valueMap.avg.toDouble().toInt())
                        uvi.setMax(valueMap.max.toDouble().toInt())
                        uvi.setMin(valueMap.min.toDouble().toInt())
                        forecastArrMap[valueMap.day].setUvi(uvi)
                    }
                }
                var forecastObjArr = arrayOfNulls<DailyForecast>(1)
                forecastObjArr = forecastArrMap.values.toArray(forecastObjArr)
                val dailyForecastList: MutableList<DailyForecast?> = ArrayList()
                for (dailyForecast in forecastObjArr) {
                    dailyForecastList.add(dailyForecast)
                }
                Collections.sort(dailyForecastList) { (date1), (date2) -> date1.compareTo(date2) }
                airQualityDto.setDailyForecastList(dailyForecastList)
            } else {
                airQualityDto.setAqi(-1).setSuccessful(false)
            }
        }
        return airQualityDto
    }

    @JvmStatic
    fun parseTextToAirQualityDto(jsonObject: JsonObject): AirQualityDto? {
        var airQualityDto: AirQualityDto? = null
        if (jsonObject[WeatherProviderType.AQICN.name] != null && jsonObject[WeatherProviderType.AQICN.name].toString() != "{}") {
            val aqiCnObject = jsonObject.getAsJsonObject(WeatherProviderType.AQICN.name)
            val aqiCnGeolocalizedFeedResponse =
                getAirQualityObjFromJson(aqiCnObject[RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name].asString)
            airQualityDto = makeAirQualityDto(
                aqiCnGeolocalizedFeedResponse,
                ZoneOffset.of(jsonObject["zoneOffset"].asString)
            )
            return airQualityDto
        } else {
            airQualityDto = AirQualityDto()
            airQualityDto.setSuccessful(false)
        }
        return airQualityDto
    }
}