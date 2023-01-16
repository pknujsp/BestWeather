package com.lifedawn.bestweather.data.remote.weather.aqicn

import android.content.Context
import androidx.collection.arrayMapOf
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast.Val
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

object AqicnResponseProcessor {
    private val AQI_GRADES = IntArray(5)
    private val AQI_GRADE_COLORS = IntArray(6)
    private val AQI_GRADE_DESCRIPTIONS = arrayOfNulls<String>(6)

    fun init(context: Context) {
        val aqiGrades = context.resources.getIntArray(R.array.AqiGrades)
        val aqiGradeColors = context.resources.getIntArray(R.array.AqiGradeColors)
        val aqiGradeDescriptions = context.resources.getStringArray(R.array.AqiGradeState)

        for (i in aqiGrades.indices)
            AQI_GRADES[i] = aqiGrades[i]
        for (i in aqiGradeColors.indices)
            AQI_GRADE_COLORS[i] = aqiGradeColors[i]
        for (i in aqiGradeDescriptions.indices)
            AQI_GRADE_DESCRIPTIONS[i] = aqiGradeDescriptions[i]
    }

    fun getGradeColorId(grade: Int): Int {
        /*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
        for (i in AQI_GRADES.indices)
            if (grade <= AQI_GRADES[i])
                return AQI_GRADE_COLORS[i]

        //if hazardous
        return AQI_GRADE_COLORS[5]
    }

    /**
     * grade가 -1이면 정보 없음을 뜻함
     * @param grade
     * @return
     */
    fun getGradeDescription(grade: Int): String {
        /*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
        if (grade == -1)
            return "?"

        for (i in AQI_GRADES.indices)
            if (grade <= AQI_GRADES[i])
                return AQI_GRADE_DESCRIPTIONS[i] ?: "?"


        //if hazardous
        return AQI_GRADE_DESCRIPTIONS[5] ?: "?"
    }

    fun getAirQualityObjFromJson(response: String): AqiCnGeolocalizedFeedResponse? = try {
        Gson().fromJson(response, AqiCnGeolocalizedFeedResponse::class.java)
    } catch (e: Exception) {
        null
    }


    fun getAirQualityDailyForecastList(
        forecast: AqiCnGeolocalizedFeedResponse.Data.Forecast,
        zoneId: ZoneId
    ): List<DailyForecast> {
        val forecastObjMap = arrayMapOf<String, AirQualityDto.DailyForecast>()
        val todayDate = LocalDate.now(zoneId)
        var date: LocalDate? = null

        val pm10Forecast = forecast.daily.pm10

        for (valueMap in pm10Forecast) {
            date = getDate(valueMap.day)
            if (date.isBefore(todayDate))
                continue

            if (!forecastObjMap.containsKey(valueMap.day))
                forecastObjMap[valueMap.day] = DailyForecast(date)

            forecastObjMap[valueMap.day]?.pm10 = Val(
                min = valueMap.min.toInt(),
                avg = valueMap.avg.toInt(),
                max = valueMap.max.toInt()
            )
        }
        val pm25Forecast = forecast.daily.pm25
        for (valueMap in pm25Forecast) {
            date = getDate(valueMap.day)
            if (date.isBefore(todayDate))
                continue

            if (!forecastObjMap.containsKey(valueMap.day))
                forecastObjMap[valueMap.day] = DailyForecast(date)

            forecastObjMap[valueMap.day]?.pm25 = Val(
                min = valueMap.min.toInt(),
                avg = valueMap.avg.toInt(),
                max = valueMap.max.toInt()
            )
        }
        val o3Forecast = forecast.daily.o3
        for (valueMap in o3Forecast) {
            date = getDate(valueMap.day)
            if (date.isBefore(todayDate)) {
                continue
            }
            if (!forecastObjMap.containsKey(valueMap.day))
                forecastObjMap[valueMap.day] = DailyForecast(date)

            forecastObjMap[valueMap.day]?.o3 = Val(
                min = valueMap.min.toInt(),
                avg = valueMap.avg.toInt(),
                max = valueMap.max.toInt()
            )
        }

        val forecastObjList = forecastObjMap.values.toMutableList()
        forecastObjList.sortWith(Comparator<DailyForecast> { o1, o2 ->
            o1.date.compareTo(o2.date)
        })

        return forecastObjList.toList()
    }

    fun getAirQuality(context: Context, airQualityResponse: AqiCnGeolocalizedFeedResponse?): String = if (airQualityResponse != null) {
        if (airQualityResponse.status == "ok") {
            val iAqi = airQualityResponse.data.iaqi
            var `val` = Int.MIN_VALUE

            if (iAqi.o3 != null) {
                `val` = max(`val`, iAqi.o3.value.toDouble().toInt())
            }
            if (iAqi.pm25 != null) {
                `val` = max(`val`, iAqi.pm25.value.toDouble().toInt())
            }
            if (iAqi.pm10 != null) {
                `val` = max(`val`, iAqi.pm10.value.toDouble().toInt())
            }
            if (iAqi.no2 != null) {
                `val` = max(`val`, iAqi.no2.value.toDouble().toInt())
            }
            if (iAqi.so2 != null) {
                `val` = max(`val`, iAqi.so2.value.toDouble().toInt())
            }
            if (iAqi.co != null) {
                `val` = max(`val`, iAqi.co.value.toDouble().toInt())
            }
            if (iAqi.dew != null) {
                `val` = max(`val`, iAqi.dew.value.toDouble().toInt())
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


    private fun getDate(day: String): LocalDate = LocalDate.parse(day, DateTimeFormatter.ofPattern("yyyy-MM-dd"))


    fun makeAirQualityDto(aqiCnGeolocalizedFeedResponse: AqiCnGeolocalizedFeedResponse, zoneId: ZoneId): AirQualityDto? {
        if (aqiCnGeolocalizedFeedResponse.status != "ok")
            return null

        return aqiCnGeolocalizedFeedResponse.run {
            data.run {
                //-----------------time----
                val time = AirQualityDto.Time(
                    v = time.v,
                    tz = time.tz,
                    s = time.s,
                    iso = time.iso
                )

                //------------------Current------------------------------------------------------------------------------
                val current = AirQualityDto.Current(
                    pm10 = if (iaqi.pm10 != null) iaqi.pm10.value.toDouble().toInt() else -1,
                    pm25 = if (iaqi.pm25 != null) iaqi.pm25.value.toDouble().toInt() else -1,
                    dew = if (iaqi.dew != null) iaqi.dew.value.toDouble().toInt() else -1,
                    co = if (iaqi.co != null) iaqi.co.value.toDouble().toInt() else -1,
                    so2 = if (iaqi.so2 != null) iaqi.so2.value.toDouble().toInt() else -1,
                    no2 = if (iaqi.no2 != null) iaqi.no2.value.toDouble().toInt() else -1,
                    o3 = if (iaqi.o3 != null) iaqi.o3.value.toDouble().toInt() else -1
                )

                //---------- dailyforecast-----------------------------------------------------------------------
                val dailyForecastList = getAirQualityDailyForecastList(forecast, zoneId)

                AirQualityDto(
                    aqi = if (data.aqi == "-") -1 else data.aqi.toDouble().toInt(),
                    idx = idx.toInt(),
                    timeInfo = time,
                    latitude = city.geo[0].toDouble(),
                    longitude = city.geo[1].toDouble(),
                    cityName = city.name,
                    aqiCnUrl = city.url,
                    time = ZonedDateTime.parse(time.iso),
                    current = current,
                    dailyForecastList = dailyForecastList
                )
            }


        }

    }

    fun parseTextToAirQualityDto(jsonObject: JsonObject): AirQualityDto? = if (jsonObject[WeatherProviderType.AQICN.name] != null &&
        jsonObject[WeatherProviderType.AQICN.name].toString() != "{}"
    ) {
        val aqiCnObject = jsonObject.getAsJsonObject(WeatherProviderType.AQICN.name)
        val aqiCnGeolocalizedFeedResponse =
            getAirQualityObjFromJson(aqiCnObject[RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name].asString)

        aqiCnGeolocalizedFeedResponse?.run {
            makeAirQualityDto(
                this,
                ZoneOffset.of(jsonObject["zoneOffset"].asString)
            )
        }
    } else {
        null
    }


}