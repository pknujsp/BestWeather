package com.lifedawn.bestweather.data.remote.weather.metnorway

import android.content.Context
import com.google.gson.Gson
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.LocationForecastResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries.Data
import com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries.Details
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherUtil
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WindUtil
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.max
import kotlin.math.min

object MetNorwayResponseProcessor : WeatherResponseProcessor() {
    private val WEATHER_ICON_DESCRIPTION_MAP = mutableMapOf<String, String>()
    private val WEATHER_ICON_ID_MAP = mutableMapOf<String, Int>()
    private val FLICKR_MAP = mutableMapOf<String, String>()

    fun init(context: Context) {
        if (WEATHER_ICON_DESCRIPTION_MAP.isEmpty() || WEATHER_ICON_ID_MAP.isEmpty() || FLICKR_MAP.isEmpty()) {
            val codes = context.resources.getStringArray(R.array.MetNorwayWeatherIconSymbols)
            val descriptions = context.resources.getStringArray(R.array.MetNorwayWeatherIconDescriptionsForSymbol)
            val iconIds = context.resources.obtainTypedArray(R.array.MetNorwayWeatherIconForSymbol)
            WEATHER_ICON_DESCRIPTION_MAP.clear()

            for (i in codes.indices) {
                WEATHER_ICON_DESCRIPTION_MAP[codes[i]] = descriptions[i]
                WEATHER_ICON_ID_MAP[codes[i]] = iconIds.getResourceId(i, R.drawable.temp_icon)
            }

            val flickrGalleryNames = context.resources.getStringArray(R.array.MetNorwayFlickrGalleryNames)
            for (i in codes.indices)
                FLICKR_MAP[codes[i]] = flickrGalleryNames[i]
        }
    }

    fun getLocationForecastResponseObjFromJson(response: String): LocationForecastResponse =
        Gson().fromJson(response, LocationForecastResponse::class.java)


    private fun getWeatherIconDescription(symbolCode: String): String {
        return WEATHER_ICON_DESCRIPTION_MAP[symbolCode.replace("day", "").replace("night", "")
            .replace("_", "")] ?: ""
    }

    private fun getWeatherIconImg(symbolCode: String): Int {
        var _symbolCode = symbolCode
        val isNight = _symbolCode.contains("night")
        _symbolCode = _symbolCode.replace("day", "").replace("night", "")
            .replace("_", "")
        var iconId = WEATHER_ICON_ID_MAP[_symbolCode] ?: -1

        if (isNight) {
            if (iconId == R.drawable.day_clear) {
                iconId = R.drawable.night_clear
            } else if (iconId == R.drawable.day_partly_cloudy) {
                iconId = R.drawable.night_partly_cloudy
            }
        }

        return iconId
    }

    fun makeCurrentConditionsDto(
        context: Context, locationForecastResponse: LocationForecastResponse,
        zoneId: ZoneId
    ): CurrentConditionsDto {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempUnitStr = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val percent = "%"
        var time = ZonedDateTime.parse(locationForecastResponse.properties.timeSeries[0].time)
        time = time.withZoneSameInstant(zoneId)
        val data = locationForecastResponse.properties.timeSeries[0].data
        val feelsLikeTemp = WeatherUtil.calcFeelsLikeTemperature(
            data.instant.details.airTemperature.toDouble(),
            ValueUnits.convertWindSpeed(data.instant.details.windSpeed, ValueUnits.kmPerHour),
            data.instant.details.relativeHumidity.toDouble()
        )

        var precipitationVolume = 0.0
        var precipitationVolumeStr = ""
        precipitationVolume += data.next_1_hours.details.precipitationAmount.toDouble()
        if (precipitationVolume > 0.0) {
            precipitationVolumeStr = String.format(Locale.getDefault(), "%.1fmm", precipitationVolume)
        }

        val currentConditionsDto = CurrentConditionsDto(
            currentTime = time,
            weatherDescription = getWeatherIconDescription(data.next_1_hours.summary.symbolCode),
            weatherIconId = getWeatherIconImg(data.next_1_hours.summary.symbolCode),
            temp = ValueUnits.convertTemperature(data.instant.details.airTemperature, tempUnit).toString() + tempUnitStr,
            feelsLikeTemp = ValueUnits.convertTemperature(feelsLikeTemp.toString(), tempUnit).toString() + tempUnitStr,
            humidity = data.instant.details.relativeHumidity.toInt().toString() + percent,
            dewPoint = ValueUnits.convertTemperature(data.instant.details.dewPointTemperature, tempUnit).toString() +
                    tempUnitStr,
            windDirectionDegree = data.instant.details.windFromDirection.toDouble().toInt(),
            windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, data.instant.details.windFromDirection),
            windSpeed = ValueUnits.convertWindSpeed(data.instant.details.windSpeed, windUnit)
                .toString() + MyApplication.VALUE_UNIT_OBJ.windUnitText,
            simpleWindStrength = WindUtil.getSimpleWindSpeedDescription(data.instant.details.windSpeed),
            windStrength = WindUtil.getWindSpeedDescription(data.instant.details.windSpeed),
            pressure = data.instant.details.airPressureAtSeaLevel + "hpa",
            uvIndex = data.instant.details.ultravioletIndexClearSky,
            cloudiness = data.instant.details.cloudAreaFraction + percent
        )

        currentConditionsDto.precipitationVolume = precipitationVolumeStr
        return currentConditionsDto
    }

    fun makeHourlyForecastDtoList(
        context: Context,
        locationForecastResponse: LocationForecastResponse,
        zoneId: ZoneId
    ): List<HourlyForecastDto> {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val percent = "%"
        val mm = "mm"
        val windUnitStr = MyApplication.VALUE_UNIT_OBJ.windUnitText
        val zeroPrecipitationVolume = "0.0mm"
        val zero = "0.0"
        val pressureUnit = "hpa"
        var precipitationVolume: String
        var hasPrecipitation = false
        val hourlyForecastDtoList = mutableListOf<HourlyForecastDto>()
        val hourlyList = locationForecastResponse.properties.timeSeries
        var instantDetails: Details? = null
        var feelsLikeTemp: Double? = null
        var time: ZonedDateTime? = null

        for (hourly in hourlyList) {
            if (hourly.data.next_1_hours == null && hourly.data.next_6_hours == null) {
                break
            }

            time = ZonedDateTime.parse(hourly.time)
            time = time.withZoneSameInstant(zoneId)
            time = time.withMinute(0).withSecond(0).withNano(0)
            instantDetails = hourly.data.instant.details
            feelsLikeTemp = WeatherUtil.calcFeelsLikeTemperature(
                instantDetails.airTemperature.toDouble(),
                ValueUnits.convertWindSpeed(instantDetails.windSpeed, ValueUnits.kmPerHour), instantDetails.relativeHumidity.toDouble()
            )
            if (hourly.data.next_1_hours == null) {
                //이후 6시간 강수량 표기
                if (hourly.data.next_6_hours.details.precipitationAmount == zero) {
                    precipitationVolume = zeroPrecipitationVolume
                    hasPrecipitation = false
                } else {
                    precipitationVolume = hourly.data.next_6_hours.details.precipitationAmount + mm
                    hasPrecipitation = true
                }
            } else {
                if (hourly.data.next_1_hours.details.precipitationAmount == zero) {
                    precipitationVolume = zeroPrecipitationVolume
                    hasPrecipitation = false
                } else {
                    precipitationVolume = hourly.data.next_1_hours.details.precipitationAmount + mm
                    hasPrecipitation = true
                }
            }
            var weatherIcon = 0
            var weatherDescription: String? = null
            if (hourly.data.next_1_hours != null) {
                weatherIcon = getWeatherIconImg(hourly.data.next_1_hours.summary.symbolCode)
                weatherDescription = getWeatherIconDescription(hourly.data.next_1_hours.summary.symbolCode)
            } else {
                weatherIcon = getWeatherIconImg(hourly.data.next_6_hours.summary.symbolCode)
                weatherDescription = getWeatherIconDescription(hourly.data.next_6_hours.summary.symbolCode)
            }

            hourlyForecastDtoList.add(
                HourlyForecastDto(
                    hours = time,
                    weatherIcon = weatherIcon,
                    temp = ValueUnits.convertTemperature(instantDetails.airTemperature, tempUnit).toString() + tempDegree,
                    isHasNext6HoursPrecipitation = hourly.data.next_1_hours == null,
                    isHasPrecipitation = hasPrecipitation,
                    weatherDescription = weatherDescription,
                    feelsLikeTemp = ValueUnits.convertTemperature(feelsLikeTemp.toString(), tempUnit).toString() + tempDegree,
                    windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, instantDetails.windFromDirection),
                    windDirectionVal = instantDetails.windFromDirection.toDouble().toInt(),
                    windSpeed = ValueUnits.convertWindSpeed(instantDetails.windSpeed, windUnit).toString() + windUnitStr,
                    windStrength = WindUtil.getSimpleWindSpeedDescription(instantDetails.windSpeed),
                    pressure = instantDetails.airPressureAtSeaLevel + pressureUnit,
                    humidity = instantDetails.relativeHumidity.toInt().toString() + percent,
                    cloudiness = instantDetails.cloudAreaFraction + percent,
                    uvIndex = instantDetails.ultravioletIndexClearSky,
                    pop = "-",
                    precipitationVolume = precipitationVolume
                )
            )
        }

        return hourlyForecastDtoList.toList()
    }

    fun makeDailyForecastDtoList(
        context: Context,
        locationForecastResponse: LocationForecastResponse,
        zoneId: ZoneId
    ): List<DailyForecastDto> {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val mm = "mm"
        val wind = MyApplication.VALUE_UNIT_OBJ.windUnitText
        val zeroPrecipitationVolume = "0.0mm"
        val zero = "0.0"

        val arr = idxHasNotNext1Hours(locationForecastResponse, zoneId)

        val hasNotNext1HoursIdx = arr[0]
        val dayHasNotNext1HoursStartIdx = arr[1]
        val tomorrowIdx = arr[2]
        var minTemp = Double.MAX_VALUE
        var maxTemp = Double.MIN_VALUE

        // 강수량, 날씨 아이콘, 날씨 설명, 풍향, 풍속
        lateinit var data: Data
        lateinit var date: ZonedDateTime
        val forecastDtoSortedMap: SortedMap<String, DailyForecastDto> = TreeMap()
        var idx = tomorrowIdx
        var precipitation = ""
        var hasPrecipitation = false


        val hourlyList = locationForecastResponse.properties.timeSeries

        while (idx < hasNotNext1HoursIdx) {
            data = hourlyList[idx].data
            date = ZonedDateTime.parse(hourlyList[idx].time)
            date = date.withZoneSameInstant(zoneId)
            date = date.withMinute(0).withSecond(0).withNano(0)

            val key = date.toLocalDate().toString()

            if (!forecastDtoSortedMap.containsKey(key)) {
                val dailyForecastDto = DailyForecastDto()
                dailyForecastDto.date = date
                forecastDtoSortedMap[date.toLocalDate().toString()] = dailyForecastDto
            }

            if (data.next_1_hours.details.precipitationAmount != zero) {
                precipitation = data.next_1_hours.details.precipitationAmount + mm
                hasPrecipitation = true
            } else {
                precipitation = zeroPrecipitationVolume
                hasPrecipitation = false
            }

            forecastDtoSortedMap[key]?.apply {
                this.valuesList.toMutableList().add(
                    DailyForecastDto.Values(
                        temp = ValueUnits.convertTemperature(data.instant.details.airTemperature, tempUnit).toString() + tempDegree,
                        weatherIcon = getWeatherIconImg(data.next_1_hours.summary.symbolCode),
                        weatherDescription = getWeatherIconDescription(data.next_1_hours.summary.symbolCode),
                        windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, data.instant.details.windFromDirection),
                        windDirectionVal = data.instant.details.windFromDirection.toDouble().toInt(),
                        windSpeed = ValueUnits.convertWindSpeed(data.instant.details.windSpeed, windUnit).toString() + wind,
                        windStrength = WindUtil.getSimpleWindSpeedDescription(data.instant.details.windSpeed),
                        isHasPrecipitationNextHoursAmount = false,
                        pop = "-",
                        dateTime = date,
                        isHasPrecipitationVolume = hasPrecipitation,
                        precipitationVolume = precipitation
                    )
                )
                if (date.hour == 23)
                    isHaveOnly1HoursForecast = true
            }

            idx++
            hasPrecipitation = false
        }

        idx = hasNotNext1HoursIdx

        while (idx < hourlyList.size) {
            data = hourlyList[idx].data
            if (data.next_6_hours == null)
                break

            date = ZonedDateTime.parse(hourlyList[idx].time)
            date = date.withZoneSameInstant(zoneId)
            date = date.withMinute(0).withSecond(0).withNano(0)
            val key = date.toLocalDate().toString()

            if (data.next_6_hours.details.precipitationAmount != zero) {
                precipitation = data.next_6_hours.details.precipitationAmount + mm
                hasPrecipitation = true
            } else {
                precipitation = zeroPrecipitationVolume
                hasPrecipitation = false
            }

            val values = DailyForecastDto.Values(
                minTemp = ValueUnits.convertTemperature(data.next_6_hours.details.airTemperatureMin, tempUnit).toString() + tempDegree,
                maxTemp = ValueUnits.convertTemperature(data.next_6_hours.details.airTemperatureMax, tempUnit).toString() + tempDegree,
                weatherIcon = getWeatherIconImg(data.next_6_hours.summary.symbolCode),
                weatherDescription = getWeatherIconDescription(data.next_6_hours.summary.symbolCode),
                windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, data.instant.details.windFromDirection),
                windDirectionVal = data.instant.details.windFromDirection.toDouble().toInt(),
                windSpeed = ValueUnits.convertWindSpeed(data.instant.details.windSpeed, windUnit).toString() + wind,
                windStrength = WindUtil.getSimpleWindSpeedDescription(data.instant.details.windSpeed),
                isHasPrecipitationNextHoursAmount = true,
                precipitationNextHoursAmount = 6, pop = "-", dateTime = date,
                isHasPrecipitationVolume = hasPrecipitation,
                precipitationVolume = precipitation
            )

            idx++

            if (!forecastDtoSortedMap.containsKey(key)) {
                val dailyForecastDto = DailyForecastDto(date = date)
                dailyForecastDto.valuesList.toMutableList().add(values)
                forecastDtoSortedMap[date.toLocalDate().toString()] = dailyForecastDto
            }
        }

        val dailyForecastDtos = mutableListOf<DailyForecastDto>()
        dailyForecastDtos.addAll(forecastDtoSortedMap.values)

        for (dto in dailyForecastDtos) {
            var hours = 0
            minTemp = Double.MAX_VALUE
            maxTemp = Double.MIN_VALUE

            for (values in dto.valuesList) {
                values.apply {
                    hours = if (this.precipitationNextHoursAmount == 0) hours + 1 else hours + this.precipitationNextHoursAmount
                    if (this.isHasPrecipitationNextHoursAmount) {
                        // 6 hours
                        minTemp = minOf(this.minTemp.replace(tempDegree, "").toDouble(), minTemp)
                        maxTemp = maxOf(this.maxTemp.replace(tempDegree, "").toDouble(), maxTemp)
                    } else {
                        // 1 hours
                        val temp = this.temp.replace(tempDegree, "").toInt()
                        minTemp = minOf(temp.toDouble(), minTemp)
                        maxTemp = maxOf(temp.toDouble(), maxTemp)
                    }
                }
            }

            if (hours < 23)
                dto.isAvailable_toMakeMinMaxTemp = false
            else {
                // available
                ValueUnits.convertTemperature(minTemp.toString(), tempUnit).toString() + tempDegree.also {
                    dto.minTemp = it
                }

                dto.maxTemp = ValueUnits.convertTemperature(maxTemp.toString(), tempUnit).toString() + tempDegree
                val newValues: MutableList<DailyForecastDto.Values> = ArrayList()
                lateinit var values: DailyForecastDto.Values
                var precipitationVolume = 0.0

                if (dto.isHaveOnly1HoursForecast) {
                    //1시간별 예보만 포함
                    for (idx in 0..23) {
                        val temp = dto.valuesList[idx].temp.replace(tempDegree, "").toInt()
                        minTemp = min(temp.toDouble(), minTemp)
                        maxTemp = max(temp.toDouble(), maxTemp)
                        precipitationVolume += dto.valuesList[idx].precipitationVolume.replace(mm, "").toDouble()

                        if (idx == 5 || idx == 11 || idx == 17 || idx == 23) {
                            dto.valuesList[idx].apply {
                                newValues.add(
                                    DailyForecastDto.Values(
                                        isHasPrecipitationNextHoursAmount = true,
                                        precipitationNextHoursAmount = 6,
                                        minTemp = ValueUnits.convertTemperature(minTemp.toString(), tempUnit).toString() + tempDegree,
                                        maxTemp = ValueUnits.convertTemperature(maxTemp.toString(), tempUnit).toString() + tempDegree,
                                        weatherIcon = weatherIcon,
                                        weatherDescription = weatherDescription,
                                        windDirection = windDirection,
                                        windDirectionVal = windDirectionVal,
                                        windSpeed = windSpeed,
                                        windStrength = windStrength,
                                        precipitationVolume = if (precipitationVolume == 0.0) zeroPrecipitationVolume else String.format(
                                            Locale.getDefault(),
                                            "%.1f",
                                            precipitationVolume.toFloat()
                                        ) + mm,
                                        isHasPrecipitationVolume = precipitationVolume != 0.0,
                                        pop = "-",
                                        dateTime = dateTime
                                    )
                                )
                            }

                            minTemp = Double.MAX_VALUE
                            maxTemp = Double.MIN_VALUE
                            precipitationVolume = 0.0
                        }
                    }
                    dto.valuesList.toMutableList().clear()
                    dto.valuesList.toMutableList().addAll(newValues)
                } else if (dto.valuesList.size > 4) {
                    //1시간, 6시간별 예보 혼합
                    var startIdx_6HoursForecast = 0
                    var hours_start6HoursForecast = 0
                    var count_6HoursForecast = 0

                    for (idx in dto.valuesList.indices) {
                        if (dto.valuesList[idx].isHasPrecipitationNextHoursAmount) {
                            startIdx_6HoursForecast = idx
                            hours_start6HoursForecast = dto.valuesList[idx].dateTime.hour
                            count_6HoursForecast = dto.valuesList.size - startIdx_6HoursForecast
                            break
                        }
                    }
                    if (count_6HoursForecast == 4) {
                        for (count in 0 until startIdx_6HoursForecast) {
                            dto.valuesList.toMutableList().removeAt(0)
                        }
                    } else {
                        for (idx in startIdx_6HoursForecast - 1 downTo 0) {
                            val temp = dto.valuesList[idx].temp.replace(tempDegree, "").toInt()
                            minTemp = min(temp.toDouble(), minTemp)
                            maxTemp = max(temp.toDouble(), maxTemp)
                            precipitationVolume += dto.valuesList[idx].precipitationVolume.replace(mm, "").toDouble()

                            if (idx == startIdx_6HoursForecast - 6 || idx == startIdx_6HoursForecast - 12 || idx == startIdx_6HoursForecast - 18 || idx == startIdx_6HoursForecast - 24) {
                                dto.valuesList[idx].apply {
                                    newValues.add(
                                        DailyForecastDto.Values(
                                            isHasPrecipitationNextHoursAmount = true,
                                            precipitationNextHoursAmount = 6,
                                            minTemp = ValueUnits.convertTemperature(minTemp.toString(), tempUnit).toString() + tempDegree,
                                            maxTemp = ValueUnits.convertTemperature(maxTemp.toString(), tempUnit).toString() + tempDegree,
                                            weatherIcon = weatherIcon,
                                            weatherDescription = weatherDescription,
                                            windDirection = windDirection,
                                            windDirectionVal = windDirectionVal,
                                            windSpeed = windSpeed,
                                            windStrength = windStrength,
                                            precipitationVolume = if (precipitationVolume == 0.0) zeroPrecipitationVolume else String.format(
                                                Locale.getDefault(),
                                                "%.1f",
                                                precipitationVolume.toFloat()
                                            ) + mm,
                                            isHasPrecipitationVolume = precipitationVolume != 0.0,
                                            pop = "-",
                                            dateTime = dateTime
                                        )
                                    )
                                }

                                minTemp = Double.MAX_VALUE
                                maxTemp = Double.MIN_VALUE
                                precipitationVolume = 0.0
                            }
                        }
                        for (count in 0 until startIdx_6HoursForecast) {
                            dto.valuesList.toMutableList().removeAt(0)
                        }
                        dto.valuesList.toMutableList().addAll(newValues)
                    }
                }
            }
        }

        var noAvailableDayCount = 0
        idx = 0

        while (idx < dailyForecastDtos.size) {
            if (!dailyForecastDtos[idx].isAvailable_toMakeMinMaxTemp) {
                noAvailableDayCount = dailyForecastDtos.size - idx
                break
            }
            idx++
        }

        idx = 0
        while (idx < noAvailableDayCount) {
            dailyForecastDtos.removeAt(dailyForecastDtos.size - 1)
            idx++
        }

        return dailyForecastDtos.toList()
    }

    private fun idxHasNotNext1Hours(response: LocationForecastResponse, zoneId: ZoneId): Array<Int> {
        var time = ZonedDateTime.parse(response.properties.timeSeries[0].time)
        time = time.withZoneSameInstant(zoneId)
        time = time.withMinute(0).withSecond(0).withNano(0)
        val hourlyList = response.properties.timeSeries

        var tomorrowIdx = -1
        var hasNotNext1HoursDayOfYear = -1
        var hasNotNext1HoursIdx = -1
        var dayHasNotNext1HoursStartIdx = -1
        val now = ZonedDateTime.now(zoneId)

        for (i in hourlyList.indices) {
            if (tomorrowIdx == -1) {
                if (time.dayOfMonth != now.dayOfMonth)
                    tomorrowIdx = i
            }
            if (hasNotNext1HoursDayOfYear == -1) {
                if (hourlyList[i].data.next_1_hours == null) {
                    hasNotNext1HoursDayOfYear = time.dayOfYear
                    hasNotNext1HoursIdx = i
                    dayHasNotNext1HoursStartIdx = hasNotNext1HoursIdx - time.hour
                }
            }
            if (hasNotNext1HoursDayOfYear != -1 && tomorrowIdx != -1)
                break

            time = time.plusHours(1)
        }

        return arrayOf(hasNotNext1HoursIdx, dayHasNotNext1HoursStartIdx, tomorrowIdx)
    }

    fun getFlickrGalleryName(code: String): String = FLICKR_MAP[code] ?: ""

}