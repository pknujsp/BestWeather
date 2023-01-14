package com.lifedawn.bestweather.data.remote.weather.kma

import android.content.Context
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.SunRiseSetUtil
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherUtil
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WindUtil
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaCurrentConditions
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaDailyForecast
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaHourlyForecast
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object KmaResponseProcessor : WeatherResponseProcessor() {
    private val WEATHER_MID_ICON_DESCRIPTION_MAP = mutableMapOf<String, String>()
    private val WEATHER_WEB_ICON_DESCRIPTION_MAP = mutableMapOf<String, String>()
    private val WEATHER_MID_ICON_ID_MAP = mutableMapOf<String, Int>()
    private val WEATHER_WEB_ICON_ID_MAP = mutableMapOf<String, Int>()
    private val PTY_FLICKR_MAP = mutableMapOf<String, String>()
    private val SKY_FLICKR_MAP = mutableMapOf<String, String>()
    private val HOURLY_TO_DAILY_DESCRIPTION_MAP = mutableMapOf<String, String>()
    private val zoneId = ZoneId.of("Asia/Seoul")

    private const val POP = "POP"
    private const val PTY = "PTY"
    private const val PCP = "PCP"
    private const val REH = "REH"
    private const val SNO = "SNO"
    private const val SKY = "SKY"
    private const val TMP = "TMP"
    private const val TMN = "TMN"
    private const val TMX = "TMX"
    private const val VEC = "VEC"
    private const val WSD = "WSD"
    private const val T1H = "T1H"
    private const val RN1 = "RN1"
    private const val LGT = "LGT"

    fun init(context: Context) {
        if ((WEATHER_MID_ICON_DESCRIPTION_MAP.isEmpty() || WEATHER_MID_ICON_ID_MAP.isEmpty() || PTY_FLICKR_MAP.isEmpty() || SKY_FLICKR_MAP.isEmpty() || HOURLY_TO_DAILY_DESCRIPTION_MAP.isEmpty())) {
            val midCodes = context.resources.getStringArray(R.array.KmaMidIconCodes)
            val webIconCodes = context.resources.getStringArray(R.array.KmaWeatherDescriptionCodes)
            val midDescriptions = context.resources.getStringArray(R.array.KmaMidIconDescriptionsForCode)
            val webIconDescriptions = context.resources.getStringArray(R.array.KmaWeatherDescriptions)
            val midIconIds = context.resources.obtainTypedArray(R.array.KmaMidWeatherIconForCode)
            val webIconIds = context.resources.obtainTypedArray(R.array.KmaWeatherIconForDescriptionCode)
            WEATHER_MID_ICON_DESCRIPTION_MAP.clear()

            for (i in midCodes.indices) {
                WEATHER_MID_ICON_DESCRIPTION_MAP[midCodes[i]] = midDescriptions[i]
                WEATHER_MID_ICON_ID_MAP[midCodes[i]] = midIconIds.getResourceId(i, R.drawable.temp_icon)
            }

            WEATHER_WEB_ICON_DESCRIPTION_MAP.clear()
            for (i in webIconCodes.indices) {
                WEATHER_WEB_ICON_DESCRIPTION_MAP[webIconCodes[i]] = webIconDescriptions[i]
                WEATHER_WEB_ICON_ID_MAP[webIconCodes[i]] = webIconIds.getResourceId(i, R.drawable.temp_icon)
            }

            val ptyFlickrGalleryNames = context.resources.getStringArray(R.array.KmaPtyFlickrGalleryNames)
            val skyFlickrGalleryNames = context.resources.getStringArray(R.array.KmaSkyFlickrGalleryNames)
            val skyCodes = context.resources.getStringArray(R.array.KmaSkyIconCodes)
            val ptyCodes = context.resources.getStringArray(R.array.KmaPtyIconCodes)

            PTY_FLICKR_MAP.clear()
            for (i in ptyCodes.indices) {
                PTY_FLICKR_MAP[ptyCodes[i]] = ptyFlickrGalleryNames.get(i)
            }
            SKY_FLICKR_MAP.clear()
            for (i in skyCodes.indices) {
                SKY_FLICKR_MAP[skyCodes[i]] = skyFlickrGalleryNames.get(i)
            }
            HOURLY_TO_DAILY_DESCRIPTION_MAP["비"] = "흐리고 비"
            HOURLY_TO_DAILY_DESCRIPTION_MAP["비/눈"] = "흐리고 비/눈"
            HOURLY_TO_DAILY_DESCRIPTION_MAP["눈"] = "흐리고 눈"
            HOURLY_TO_DAILY_DESCRIPTION_MAP["빗방울"] = "흐리고 비"
            HOURLY_TO_DAILY_DESCRIPTION_MAP["빗방울/눈날림"] = "흐리고 비/눈"
            HOURLY_TO_DAILY_DESCRIPTION_MAP["눈날림"] = "흐리고 눈"
            HOURLY_TO_DAILY_DESCRIPTION_MAP["구름 많음"] = "구름많음"
        }
    }

    /*
		sky
		<item>맑음</item>
        <item>구름 많음</item>
        <item>흐림</item>

        pty
        <item>비</item>
        <item>비/눈</item>
        <item>눈</item>
        <item>소나기</item>
        <item>빗방울</item>
        <item>빗방울/눈날림</item>
        <item>눈날림</item>
		 */
    fun convertSkyTextToCode(text: String?): String? = when (text) {
        "맑음" -> "1"
        "구름 많음" -> "3"
        "흐림" -> "4"
        else -> null
    }


    private fun convertPtyTextToCode(text: String): String = when (text) {
        "비" -> "1"
        "비/눈" -> "2"
        "눈" -> "3"
        "소나기" -> "4"
        "빗방울" -> "5"
        "빗방울/눈날림" -> "6"
        "눈날림" -> "7"
        else -> "0"
    }


    private fun getWeatherDescriptionWeb(weatherDescriptionKr: String): String =
        WEATHER_WEB_ICON_DESCRIPTION_MAP[weatherDescriptionKr] ?: ""


    private fun getWeatherIconImgWeb(weatherDescriptionKr: String, night: Boolean): Int = if (night) {
        if ((weatherDescriptionKr == "맑음")) {
            R.drawable.night_clear
        } else if ((weatherDescriptionKr == "구름 많음")) {
            R.drawable.night_mostly_cloudy
        } else {
            WEATHER_WEB_ICON_ID_MAP[weatherDescriptionKr] ?: -1
        }
    } else {
        WEATHER_WEB_ICON_ID_MAP[weatherDescriptionKr] ?: -1
    }


    private fun getWeatherIconImgWeb(weatherDescriptionKr: String, night: Boolean, thunder: Boolean): Int = if (thunder) {
        R.drawable.thunderstorm
    } else {
        getWeatherIconImgWeb(weatherDescriptionKr, night)
    }


    private fun getWeatherMidIconDescription(code: String): String = WEATHER_MID_ICON_DESCRIPTION_MAP[code] ?: ""


    private fun getWeatherMidIconImg(code: String, night: Boolean): Int = if (night) {
        when (code) {
            "맑음" -> R.drawable.night_clear
            "구름많음" -> R.drawable.night_mostly_cloudy
            else -> WEATHER_MID_ICON_ID_MAP[code] ?: -1
        }
    } else {
        WEATHER_MID_ICON_ID_MAP[code] ?: -1
    }


    fun convertSkyPtyToMid(sky: String?, pty: String): String = if (pty == "0") {
        when (sky) {
            "1" -> "맑음"
            "3" -> "구름많음"
            else -> "흐림"
        }
    } else {
        when (pty) {
            "1", "5" -> "흐리고 비"
            "2", "6" -> "흐리고 비/눈"
            "3", "7" -> "흐리고 눈"
            else -> "흐리고 소나기"
        }
    }


    fun convertHourlyWeatherDescriptionToMid(description: String): String {
        /*
		hourly -
		<item>맑음</item>
        <item>구름 많음</item>
        <item>흐림</item>

        <item>비</item>
        <item>비/눈</item>
        <item>눈</item>
        <item>소나기</item>
        <item>빗방울</item>
        <item>빗방울/눈날림</item>
        <item>눈날림</item>

		mid -
		<item>맑음</item>
        <item>구름많음</item>
        <item>구름많고 비</item>
        <item>구름많고 눈</item>
        <item>구름많고 비/눈</item>
        <item>구름많고 소나기</item>
        <item>흐림</item>
        <item>흐리고 비</item>
        <item>흐리고 눈</item>
        <item>흐리고 비/눈</item>
        <item>흐리고 소나기</item>
        <item>소나기</item>
		 */
        return if (HOURLY_TO_DAILY_DESCRIPTION_MAP.containsKey(description)) {
            HOURLY_TO_DAILY_DESCRIPTION_MAP[description] ?: ""
        } else {
            description
        }
    }

    fun getPtyFlickrGalleryName(code: String): String? = PTY_FLICKR_MAP[code] ?: ""


    fun getSkyFlickrGalleryName(code: String): String? = SKY_FLICKR_MAP[code] ?: ""


    fun makeHourlyForecastDtoListOfWEB(
        context: Context, parsedKmaHourlyForecasts: List<ParsedKmaHourlyForecast>, latitude: Double, longitude: Double
    ): List<HourlyForecastDto> {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val mPerSec = "m/s"
        val zeroRainVolume = "0.0mm"
        val zeroSnowVolume = "0.0cm"
        val percent = "%"
        val zoneId = ZoneId.of("Asia/Seoul")
        val sunSetRiseDataMap = SunRiseSetUtil.getDailySunRiseSetMap(
            ZonedDateTime.parse(parsedKmaHourlyForecasts[0].hourISO8601), ZonedDateTime.parse(
                parsedKmaHourlyForecasts[parsedKmaHourlyForecasts.size - 1].hourISO8601
            ), latitude, longitude
        )
        val poong = "풍"

        var isNight = false
        val itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.id))
        var sunRise: Calendar? = null
        var sunSet: Calendar? = null
        val hourlyForecastDtoList = mutableListOf<HourlyForecastDto>()
        var hours = ZonedDateTime.now()

        for (parsedKmaHourlyForecast in parsedKmaHourlyForecasts) {
            hours = ZonedDateTime.parse(parsedKmaHourlyForecast.hourISO8601)

            val hasRain = parsedKmaHourlyForecast.isHasRain
            val hasSnow = parsedKmaHourlyForecast.isHasSnow
            val hasThunder = parsedKmaHourlyForecast.isHasThunder
            var snowVolume = if (!hasSnow) {
                zeroSnowVolume
            } else {
                parsedKmaHourlyForecast.snowVolume
            }
            var rainVolume = if (!hasRain) {
                zeroRainVolume
            } else {
                parsedKmaHourlyForecast.rainVolume
            }

            var feelsLikeTemp = ""
            var windDirectionInt = -1
            val temp = ValueUnits.convertTemperature(parsedKmaHourlyForecast.temp, tempUnit).toString() + tempDegree
            val weatherIcon = getWeatherIconImgWeb(
                parsedKmaHourlyForecast.weatherDescription, isNight, hasThunder
            )
            val weatherDescription = getWeatherDescriptionWeb(parsedKmaHourlyForecast.weatherDescription)
            val pop = if (!parsedKmaHourlyForecast.pop.contains("%")) "" else parsedKmaHourlyForecast.pop
            var windDirection = ""
            var windStrength = ""
            var windSpeed = ""
            val humidity = parsedKmaHourlyForecast.humidity
            val humidityVal = parsedKmaHourlyForecast.humidity.replace(percent, "").toInt()

            itemCalendar.timeInMillis = hours.toInstant().toEpochMilli()
            sunRise = sunSetRiseDataMap[hours.dayOfYear]?.sunrise
            sunSet = sunSetRiseDataMap[hours.dayOfYear]?.sunset
            isNight = SunRiseSetUtil.isNight(itemCalendar, sunRise, sunSet)

            if (parsedKmaHourlyForecast.windDirection.isNotEmpty()) {
                val windSpeedVal = parsedKmaHourlyForecast.windSpeed.replace(mPerSec, "")
                val windDirectionStr = parsedKmaHourlyForecast.windDirection.replace(poong, "")

                windDirectionInt = WindUtil.parseWindDirectionStrAsInt(windDirectionStr)
                windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, windDirectionInt.toString())
                windStrength = WindUtil.getSimpleWindSpeedDescription(windSpeedVal)
                windSpeed = ValueUnits.convertWindSpeed(windSpeedVal, windUnit).toString() + MyApplication.VALUE_UNIT_OBJ.windUnitText

                feelsLikeTemp = ValueUnits.convertTemperature(
                    WeatherUtil.calcFeelsLikeTemperature(
                        parsedKmaHourlyForecast.temp.toDouble(),
                        ValueUnits.convertWindSpeed(windSpeed, ValueUnits.kmPerHour),
                        humidityVal.toDouble()
                    ).toString(), tempUnit
                ).toString() + tempDegree
            }

            hourlyForecastDtoList.add(
                HourlyForecastDto(
                    hours = hours,
                    temp = temp,
                    rainVolume = rainVolume,
                    isHasRain = hasRain,
                    isHasSnow = hasSnow,
                    isHasThunder = hasThunder,
                    weatherIcon = weatherIcon,
                    weatherDescription = weatherDescription,
                    humidity = humidity,
                    pop = pop,
                    windDirectionVal = windDirectionInt,
                    windDirection = windDirection,
                    windStrength = windStrength,
                    windSpeed = windSpeed,
                    feelsLikeTemp = feelsLikeTemp
                )
            )
        }

        return hourlyForecastDtoList.toList()
    }

    fun makeDailyForecastDtoListOfWEB(parsedKmaDailyForecasts: List<ParsedKmaDailyForecast>): List<DailyForecastDto> {
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val dailyForecastDtoList = mutableListOf<DailyForecastDto>()

        for (parsedKmaDailyForecast in parsedKmaDailyForecasts) {
            val valueList = mutableListOf<DailyForecastDto.Values>()

            if (parsedKmaDailyForecast.isSingle) {
                parsedKmaDailyForecast.singleValues?.also { singleValues ->
                    valueList.add(
                        DailyForecastDto.Values(
                            pop = singleValues.pop, weatherIcon = getWeatherMidIconImg(
                                singleValues.weatherDescription, false
                            ), weatherDescription = getWeatherMidIconDescription(
                                singleValues.weatherDescription
                            )
                        )
                    )
                }

            } else {
                parsedKmaDailyForecast.amValues?.also { amValues ->
                    valueList.add(
                        DailyForecastDto.Values(
                            pop = amValues.pop,
                            weatherIcon = getWeatherMidIconImg(amValues.weatherDescription, false),
                            weatherDescription = getWeatherMidIconDescription(
                                amValues.weatherDescription
                            )
                        )
                    )
                }

                parsedKmaDailyForecast.pmValues?.also { pmValues ->
                    valueList.add(
                        DailyForecastDto.Values(
                            pop = pmValues.pop,
                            weatherIcon = getWeatherMidIconImg(pmValues.weatherDescription, false),
                            weatherDescription = getWeatherMidIconDescription(
                                pmValues.weatherDescription
                            )
                        )
                    )
                }
            }

            dailyForecastDtoList.add(
                DailyForecastDto(
                    date = ZonedDateTime.parse(parsedKmaDailyForecast.dateISO8601),
                    minTemp = ValueUnits.convertTemperature(parsedKmaDailyForecast.minTemp, tempUnit).toString() + tempDegree,
                    maxTemp = ValueUnits.convertTemperature(parsedKmaDailyForecast.maxTemp, tempUnit).toString() + tempDegree,
                    valuesList = valueList.toList()
                )
            )
        }
        return dailyForecastDtoList.toList()
    }

    fun makeCurrentConditionsDtoOfWEB(
        context: Context,
        parsedKmaCurrentConditions: ParsedKmaCurrentConditions,
        parsedKmaHourlyForecast: ParsedKmaHourlyForecast,
        latitude: Double,
        longitude: Double
    ): CurrentConditionsDto {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempUnitStr = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val currentTime = ZonedDateTime.parse(parsedKmaCurrentConditions.baseDateTimeISO8601)
        val currentPtyCode = convertPtyTextToCode(parsedKmaCurrentConditions.pty)
        val hourlyForecastDescription = parsedKmaHourlyForecast.weatherDescription
        val koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul")
        val sunriseSunsetCalculator = SunriseSunsetCalculator(
            Location(latitude, longitude), koreaTimeZone
        )

        val calendar = Calendar.getInstance(koreaTimeZone)
        calendar[currentTime.year, currentTime.monthValue - 1, currentTime.dayOfMonth, currentTime.hour] = currentTime.minute
        val sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar)
        val sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar)

        val windDirectionDegree = WindUtil.parseWindDirectionStrAsInt(parsedKmaCurrentConditions.windDirection)
        val windDirection = WindUtil.parseWindDirectionDegreeAsStr(context, windDirectionDegree.toString())
        var windSpeed = ""
        var simpleWindStrength = ""
        var windStrength = ""

        if (parsedKmaCurrentConditions.windSpeed.isNotEmpty()) {
            val windSpeedVal = parsedKmaCurrentConditions.windSpeed.toDouble()
            windSpeed =
                ValueUnits.convertWindSpeed(windSpeedVal.toString(), windUnit).toString() + MyApplication.VALUE_UNIT_OBJ.windUnitText

            simpleWindStrength = WindUtil.getSimpleWindSpeedDescription(windSpeedVal.toString())
            windStrength = WindUtil.getWindSpeedDescription(windSpeedVal.toString())
        }

        val precipitationVolume =
            if (!parsedKmaCurrentConditions.precipitationVolume.contains("-") && !parsedKmaCurrentConditions.precipitationVolume.contains("0.0")) {
                parsedKmaCurrentConditions.precipitationVolume
            } else ""

        val currentConditionsDto = CurrentConditionsDto(
            currentTime = currentTime,
            weatherDescription = getWeatherDescriptionWeb(currentPtyCode.ifEmpty { hourlyForecastDescription }),
            weatherIconId = getWeatherIconImgWeb(
                (currentPtyCode.ifEmpty { hourlyForecastDescription }), SunRiseSetUtil.isNight(calendar, sunRise, sunSet)
            ),
            temp = ValueUnits.convertTemperature(parsedKmaCurrentConditions.temp, tempUnit).toString() + tempUnitStr,
            feelsLikeTemp = ValueUnits.convertTemperature(parsedKmaCurrentConditions.feelsLikeTemp, tempUnit).toString() + tempUnitStr,
            humidity = parsedKmaCurrentConditions.humidity,
            tempYesterday = parsedKmaCurrentConditions.yesterdayTemp,
            windDirection = windDirection,
            windDirectionDegree = windDirectionDegree,
            windSpeed = windSpeed,
            windStrength = windStrength,
            simpleWindStrength = simpleWindStrength,
            precipitationType = currentPtyCode
        )

        currentConditionsDto.precipitationVolume = precipitationVolume
        return currentConditionsDto
    }

}