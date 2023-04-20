package com.lifedawn.bestweather.data.remote.weather.owm

import android.content.Context
import com.google.gson.Gson
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.onecall.OwmOneCallResponse
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WindUtil
import java.time.ZoneId
import java.util.*

object OwmResponseProcessor : WeatherResponseProcessor() {
    private val WEATHER_ICON_DESCRIPTION_MAP = mutableMapOf<String, String>()
    private val WEATHER_ICON_ID_MAP = mutableMapOf<String, Int>()
    private val FLICKR_MAP = mutableMapOf<String, String>()

    fun init(context: Context) {
        if (WEATHER_ICON_DESCRIPTION_MAP.isEmpty() || WEATHER_ICON_ID_MAP.isEmpty() || FLICKR_MAP.isEmpty()) {
            val codes = context.resources.getStringArray(R.array.OpenWeatherMapWeatherIconCodes)
            val descriptions = context.resources.getStringArray(R.array.OpenWeatherMapWeatherIconDescriptionsForCode)
            val iconIds = context.resources.obtainTypedArray(R.array.OpenWeatherMapWeatherIconForCode)
            WEATHER_ICON_DESCRIPTION_MAP.clear()
            for (i in codes.indices) {
                WEATHER_ICON_DESCRIPTION_MAP[codes[i]] = descriptions[i]
                WEATHER_ICON_ID_MAP[codes[i]] = iconIds.getResourceId(i, R.drawable.temp_icon)
            }

            val flickrGalleryNames = context.resources.getStringArray(R.array.OpenWeatherMapFlickrGalleryNames)
            FLICKR_MAP.clear()

            for (i in codes.indices)
                FLICKR_MAP[codes[i]] = flickrGalleryNames[i]
        }
    }

    private fun getWeatherIconImg(code: String, night: Boolean): Int = if (night) {
        when (code) {
            "800" -> R.drawable.night_clear
            "801" -> R.drawable.night_partly_cloudy
            "802" -> R.drawable.night_scattered_clouds
            "803" -> R.drawable.night_mostly_cloudy
            else -> WEATHER_ICON_ID_MAP[code] ?: -1
        }
    } else WEATHER_ICON_ID_MAP[code] ?: -1


    private fun getWeatherIconDescription(code: String): String {
        return WEATHER_ICON_DESCRIPTION_MAP[code] ?: ""
    }


    fun makeHourlyForecastDtoListOneCall(
        context: Context,
        owmOneCallResponse: OwmOneCallResponse, zoneId: ZoneId
    ): List<HourlyForecastDto> {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val visibilityUnit = MyApplication.VALUE_UNIT_OBJ.visibilityUnit
        val tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val percent = "%"
        val mm = "mm"
        val windUnitStr = MyApplication.VALUE_UNIT_OBJ.windUnitText
        val zeroSnowVolume = "0.0mm"
        val zeroRainVolume = "0.0mm"
        val pressureUnit = "hpa"
        val visibilityUnitStr = MyApplication.VALUE_UNIT_OBJ.visibilityUnitText
        var snowVolume: String
        var rainVolume: String
        var hasRain: Boolean
        var hasSnow: Boolean
        val hourlyForecastDtoList = mutableListOf<HourlyForecastDto>()

        for (hourly in owmOneCallResponse.hourly) {
            if (hourly.rain == null) {
                hasRain = false
                rainVolume = zeroRainVolume
            } else {
                hasRain = true
                rainVolume = hourly.rain.precipitation1Hour + mm
            }
            if (hourly.snow == null) {
                hasSnow = false
                snowVolume = zeroSnowVolume
            } else {
                hasSnow = true
                snowVolume = hourly.snow.precipitation1Hour + mm
            }
            hourlyForecastDtoList.add(
                HourlyForecastDto(
                    temp = ValueUnits.convertTemperature(hourly.temp, tempUnit).toString() + tempDegree,
                    pop = (hourly.pop.toDouble() * 100.0).toInt().toString() + percent,
                    isHasRain = hasRain, isHasSnow = hasSnow,
                    rainVolume = rainVolume, snowVolume = snowVolume,
                    weatherDescription = getWeatherIconDescription(hourly.weather[0].id),
                    feelsLikeTemp = ValueUnits.convertTemperature(hourly.feelsLike, tempUnit).toString() + tempDegree,
                    windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, hourly.wind_deg),
                    windDirectionVal = hourly.wind_deg.toInt(),
                    windSpeed = ValueUnits.convertWindSpeed(hourly.wind_speed, windUnit).toString() + windUnitStr,
                    windStrength = WindUtil.getSimpleWindSpeedDescription(hourly.wind_speed),
                    pressure = hourly.pressure + pressureUnit,
                    humidity = hourly.humidity + percent,
                    cloudiness = hourly.clouds + percent,
                    visibility = ValueUnits.convertVisibility(hourly.visibility, visibilityUnit) + visibilityUnitStr,
                    uvIndex = hourly.uvi,
                    windGust = hourly.windGust?.run { ValueUnits.convertWindSpeed(this, windUnit).toString() + windUnitStr } ?: "",
                    hours = convertDateTimeOfHourlyForecast(hourly.dt.toLong() * 1000L, zoneId),
                    weatherIcon = getWeatherIconImg(
                        hourly.weather[0].id,
                        hourly.weather[0].icon.contains("n")
                    ))
            )
        }
        return hourlyForecastDtoList.toList()
    }

    fun makeDailyForecastDtoListOneCall(
        context: Context,
        owmOneCallResponse: OwmOneCallResponse, zoneId: ZoneId
    ): List<DailyForecastDto> {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val mm = "mm"
        val percent = "%"
        val wind = MyApplication.VALUE_UNIT_OBJ.windUnitText
        val zeroSnowVolume = "0.0mm"
        val zeroRainVolume = "0.0mm"
        val hpa = "hpa"

        //순서 : 날짜, 날씨상태, 최저/최고 기온, 강수확률, 하루 강우량(nullable), 하루 강설량(nullable)
        //풍향, 풍속, 바람세기, 돌풍(nullable), 기압, 습도, 이슬점, 운량, 자외선최고치

        //아침/낮/저녁/밤 기온(체감) 제외
        val dailyForecastDtoList = mutableListOf<DailyForecastDto>()
        var rainVolume: String
        var snowVolume: String
        var hasRain: Boolean
        var hasSnow: Boolean

        for (daily in owmOneCallResponse.daily) {
            val dateTime = convertDateTimeOfDailyForecast(daily.dt.toLong() * 1000L, zoneId)
            val minTemp = ValueUnits.convertTemperature(daily.temp.min, tempUnit).toString() + tempDegree
            val maxTemp = ValueUnits.convertTemperature(daily.temp.max, tempUnit).toString() + tempDegree

            if (daily.rain == null) {
                hasRain = false
                rainVolume = zeroRainVolume
            } else {
                hasRain = true
                rainVolume = daily.rain + mm
            }
            if (daily.snow == null) {
                hasSnow = false
                snowVolume = zeroSnowVolume
            } else {
                hasSnow = true
                snowVolume = daily.snow + mm
            }

            val pop = (daily.pop.toDouble() * 100.0).toInt().toString() + percent
            val single = DailyForecastDto.Values(
                isHasRainVolume = hasRain,
                rainVolume = rainVolume,
                isHasSnowVolume = hasSnow,
                snowVolume = snowVolume,
                weatherIcon = getWeatherIconImg(daily.weather[0].id, false),
                weatherDescription = getWeatherIconDescription(daily.weather[0].id),
                windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, daily.windDeg),
                windDirectionVal = daily.windDeg.toInt(),
                windSpeed = ValueUnits.convertWindSpeed(daily.windSpeed, windUnit).toString() + wind,
                windStrength = WindUtil.getSimpleWindSpeedDescription(daily.windSpeed),
                windGust = ValueUnits.convertWindSpeed(daily.windGust, windUnit).toString() + wind,
                pressure = daily.pressure + hpa,
                humidity = daily.humidity + percent,
                dewPointTemp = ValueUnits.convertTemperature(daily.dew_point, tempUnit).toString() + tempDegree,
                cloudiness = daily.clouds + percent,
                uvIndex = daily.uvi, pop = pop
            )

            val dailyForecastDto = DailyForecastDto(
                date = dateTime,
                minTemp = minTemp, maxTemp = maxTemp,
                valuesList = listOf(single)
            )

            dailyForecastDtoList.add(dailyForecastDto)
        }
        return dailyForecastDtoList
    }

    fun makeCurrentConditionsDtoOneCall(
        context: Context,
        owmOneCallResponse: OwmOneCallResponse, zoneId: ZoneId
    ): CurrentConditionsDto {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val visibilityUnit = MyApplication.VALUE_UNIT_OBJ.visibilityUnit
        val tempUnitStr = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val percent = "%"
        val item = owmOneCallResponse.current
        var rainVolume = ""
        var snowVolume = ""
        var precipitationVolume = 0.0
        var precipitationVolumeStr = ""
        var precipitationType = ""

        if (item.rain != null) {
            precipitationVolume += item.rain.precipitation1Hour.toDouble()
            rainVolume = item.rain.precipitation1Hour + "mm"
        }
        if (item.snow != null) {
            precipitationVolume += item.snow.precipitation1Hour.toDouble()
            snowVolume = item.snow.precipitation1Hour + "mm"
        }
        if (precipitationVolume > 0.0)
            precipitationVolumeStr = String.format(Locale.getDefault(), "%.1fmm", precipitationVolume)

        if (precipitationVolumeStr.isNotEmpty()) {
            precipitationType = if (rainVolume.isNotEmpty() && snowVolume.isNotEmpty())
                context.getString(R.string.owm_icon_616_rain_and_snow)
            else if (rainVolume.isNotEmpty())
                context.getString(R.string.rain)
            else
                context.getString(R.string.snow)
        }

        val currentConditionsDto = CurrentConditionsDto(
            currentTime = convertDateTimeOfCurrentConditions(
                item.dt.toLong() * 1000L, zoneId
            ),
            weatherDescription = getWeatherIconDescription(item.weather[0].id),
            weatherIconId = getWeatherIconImg(
                item.weather[0].id,
                item.weather[0].icon.contains("n")
            ),
            temp = ValueUnits.convertTemperature(item.temp, tempUnit).toString() + tempUnitStr,
            feelsLikeTemp = ValueUnits.convertTemperature(item.feelsLike, tempUnit).toString() + tempUnitStr,
            humidity = item.humidity + percent,
            dewPoint = ValueUnits.convertTemperature(item.dewPoint, tempUnit).toString() + tempUnitStr,
            windDirectionDegree = item.wind_deg.toInt(),
            windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, item.wind_deg),
            windSpeed = ValueUnits.convertWindSpeed(item.wind_speed, windUnit).toString() + MyApplication.VALUE_UNIT_OBJ.windUnitText,
            windGust = item.windGust?.run {
                ValueUnits.convertWindSpeed(this, windUnit).toString() + MyApplication.VALUE_UNIT_OBJ.windUnitText
            } ?: "",
            simpleWindStrength = WindUtil.getSimpleWindSpeedDescription(item.wind_speed),
            windStrength = WindUtil.getWindSpeedDescription(item.wind_speed),
            pressure = item.pressure + "hpa",
            uvIndex = item.uvi,
            visibility = ValueUnits.convertVisibility(
                item.visibility,
                visibilityUnit
            ) + MyApplication.VALUE_UNIT_OBJ.visibilityUnitText,
            cloudiness = item.clouds + percent,
            precipitationType = precipitationType,
        )

        currentConditionsDto.rainVolume = rainVolume
        currentConditionsDto.snowVolume = snowVolume
        currentConditionsDto.precipitationVolume = precipitationVolumeStr

        return currentConditionsDto
    }

    fun getOneCallObjFromJson(response: String): OwmOneCallResponse = Gson().fromJson(response, OwmOneCallResponse::class.java)


    fun getFlickrGalleryName(code: String): String? = FLICKR_MAP[code] ?: ""

}