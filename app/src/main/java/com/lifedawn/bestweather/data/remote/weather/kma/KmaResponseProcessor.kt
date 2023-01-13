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
    private val WEATHER_MID_ICON_DESCRIPTION_MAP = HashMap<String, String>()
    private val WEATHER_WEB_ICON_DESCRIPTION_MAP = HashMap<String, String>()
    private val WEATHER_MID_ICON_ID_MAP = HashMap<String, String>()
    private val WEATHER_WEB_ICON_ID_MAP = HashMap<String, String>()
    private val PTY_FLICKR_MAP = HashMap<String, String>()
    private val SKY_FLICKR_MAP = HashMap<String, String>()
    private val HOURLY_TO_DAILY_DESCRIPTION_MAP = HashMap<String, String>()

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
        if ((WEATHER_MID_ICON_DESCRIPTION_MAP.isEmpty()
                    || WEATHER_MID_ICON_ID_MAP.isEmpty() || PTY_FLICKR_MAP.isEmpty() ||
                    SKY_FLICKR_MAP.isEmpty() || HOURLY_TO_DAILY_DESCRIPTION_MAP.isEmpty())
        ) {
            val midCodes = context.resources.getStringArray(R.array.KmaMidIconCodes)
            val webIconCodes = context.resources.getStringArray(R.array.KmaWeatherDescriptionCodes)
            val midDescriptions = context.resources.getStringArray(R.array.KmaMidIconDescriptionsForCode)
            val webIconDescriptions = context.resources.getStringArray(R.array.KmaWeatherDescriptions)
            val midIconIds = context.resources.obtainTypedArray(R.array.KmaMidWeatherIconForCode)
            val webIconIds = context.resources.obtainTypedArray(R.array.KmaWeatherIconForDescriptionCode)
            WEATHER_MID_ICON_DESCRIPTION_MAP.clear()
            for (i in midCodes.indices) {
                WEATHER_MID_ICON_DESCRIPTION_MAP[midCodes.get(i)] = midDescriptions.get(i)
                WEATHER_MID_ICON_ID_MAP[midCodes.get(i)] = midIconIds.getResourceId(i, R.drawable.temp_icon)
            }
            WEATHER_WEB_ICON_DESCRIPTION_MAP.clear()
            for (i in webIconCodes.indices) {
                WEATHER_WEB_ICON_DESCRIPTION_MAP[webIconCodes.get(i)] = webIconDescriptions.get(i)
                WEATHER_WEB_ICON_ID_MAP[webIconCodes.get(i)] = webIconIds.getResourceId(i, R.drawable.temp_icon)
            }
            val ptyFlickrGalleryNames = context.resources.getStringArray(R.array.KmaPtyFlickrGalleryNames)
            val skyFlickrGalleryNames = context.resources.getStringArray(R.array.KmaSkyFlickrGalleryNames)
            val skyCodes = context.resources.getStringArray(R.array.KmaSkyIconCodes)
            val ptyCodes = context.resources.getStringArray(R.array.KmaPtyIconCodes)
            PTY_FLICKR_MAP.clear()
            for (i in ptyCodes.indices) {
                PTY_FLICKR_MAP[ptyCodes.get(i)] = ptyFlickrGalleryNames.get(i)
            }
            SKY_FLICKR_MAP.clear()
            for (i in skyCodes.indices) {
                SKY_FLICKR_MAP[skyCodes.get(i)] = skyFlickrGalleryNames.get(i)
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


    fun convertPtyTextToCode(text: String?): String? = when (text) {
        "없음" -> "0"
        "비" -> "1"
        "비/눈" -> "2"
        "눈" -> "3"
        "소나기" -> "4"
        "빗방울" -> "5"
        "빗방울/눈날림" -> "6"
        "눈날림" -> "7"
        else -> null
    }


    fun getWeatherSkyIconDescription(code: String?): String? {
        //return WEATHER_SKY_ICON_DESCRIPTION_MAP.get(code);
        return null
    }

    fun getWeatherDescription(pty: String, sky: String?): String? {
        return if ((pty == "0")) {
            getWeatherSkyIconDescription(sky)
        } else {
            getWeatherPtyIconDescription(pty)
        }
    }

    fun getWeatherDescriptionWeb(weatherDescriptionKr: String): String? {
        return WEATHER_WEB_ICON_DESCRIPTION_MAP[weatherDescriptionKr]
    }

    fun getWeatherIconImgWeb(weatherDescriptionKr: String, night: Boolean): Int {
        if (night) {
            if ((weatherDescriptionKr == "맑음")) {
                return R.drawable.night_clear
            } else return if ((weatherDescriptionKr == "구름 많음")) {
                R.drawable.night_mostly_cloudy
            } else {
                (WEATHER_WEB_ICON_ID_MAP.get(weatherDescriptionKr))!!
            }
        } else {
            return (WEATHER_WEB_ICON_ID_MAP[weatherDescriptionKr])!!
        }
    }

    fun getWeatherIconImgWeb(weatherDescriptionKr: String, night: Boolean, thunder: Boolean): Int {
        return if (thunder) {
            R.drawable.thunderstorm
        } else {
            getWeatherIconImgWeb(weatherDescriptionKr, night)
        }
    }

    fun getWeatherMidIconDescription(code: String): String? {
        return WEATHER_MID_ICON_DESCRIPTION_MAP[code]
    }

    fun getWeatherMidIconImg(code: String, night: Boolean): Int {
        if (night) {
            if ((code == "맑음")) {
                return R.drawable.night_clear
            } else return if ((code == "구름많음")) {
                R.drawable.night_mostly_cloudy
            } else {
                (WEATHER_MID_ICON_ID_MAP.get(code))!!
            }
        }
        return (WEATHER_MID_ICON_ID_MAP[code])!!
    }

    fun getWeatherPtyIconDescription(code: String?): String? {
        //return WEATHER_PTY_ICON_DESCRIPTION_MAP.get(code);
        return null
    }

    fun convertSkyPtyToMid(sky: String?, pty: String): String {
        return if ((pty == "0")) {
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
    }

    fun convertHourlyWeatherDescriptionToMid(description: String): String? {
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
            HOURLY_TO_DAILY_DESCRIPTION_MAP.get(description)
        } else {
            description
        }
    }

    fun getPtyFlickrGalleryName(code: String): String? {
        return PTY_FLICKR_MAP[code]
    }

    fun getSkyFlickrGalleryName(code: String): String? {
        return SKY_FLICKR_MAP[code]
    }

    @JvmStatic
    fun makeHourlyForecastDtoListOfWEB(
        context: Context?,
        hourlyForecastList: List<ParsedKmaHourlyForecast>, latitude: Double, longitude: Double
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
            ZonedDateTime.of(hourlyForecastList[0].getHour().toLocalDateTime(), zoneId),
            ZonedDateTime.of(
                hourlyForecastList[hourlyForecastList.size - 1].getHour().toLocalDateTime(),
                zoneId
            ), latitude, longitude
        )
        var isNight = false
        val itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.id))
        var sunRise: Calendar? = null
        var sunSet: Calendar? = null
        val hourlyForecastDtoList: MutableList<HourlyForecastDto> = ArrayList()
        var snowVolume: String
        var rainVolume: String
        var hasRain: Boolean
        var hasSnow: Boolean
        var hasThunder: Boolean
        var windSpeed: String? = null
        var humidity = 0
        var feelsLikeTemp = 0.0
        var windDirectionStr: String? = null
        var windDirectionInt = 0
        val poong = "풍"
        for (finalHourlyForecast: ParsedKmaHourlyForecast in hourlyForecastList) {
            val hourlyForecastDto = HourlyForecastDto()
            hasRain = finalHourlyForecast.isHasRain
            if (!hasRain) {
                rainVolume = zeroRainVolume
            } else {
                rainVolume = finalHourlyForecast.rainVolume
            }
            hasSnow = finalHourlyForecast.isHasSnow
            if (!hasSnow) {
                snowVolume = zeroSnowVolume
            } else {
                snowVolume = finalHourlyForecast.snowVolume
            }
            hasThunder = finalHourlyForecast.isHasThunder
            itemCalendar.timeInMillis = finalHourlyForecast.getHour().toInstant().toEpochMilli()
            sunRise = sunSetRiseDataMap[finalHourlyForecast.getHour().getDayOfYear()]!!.sunrise
            sunSet = sunSetRiseDataMap[finalHourlyForecast.getHour().getDayOfYear()]!!.sunset
            isNight = SunRiseSetUtil.isNight(itemCalendar, sunRise, sunSet)
            humidity = finalHourlyForecast.humidity.replace(percent, "").toInt()
            hourlyForecastDto.setHours(finalHourlyForecast.getHour())
                .setTemp(ValueUnits.convertTemperature(finalHourlyForecast.temp, tempUnit).toString() + tempDegree)
                .setRainVolume(rainVolume)
                .setHasRain(hasRain)
                .setHasSnow(hasSnow)
                .setSnowVolume(snowVolume)
                .setHasThunder(hasThunder)
                .setWeatherIcon(
                    getWeatherIconImgWeb(
                        finalHourlyForecast.weatherDescription,
                        isNight, hasThunder
                    )
                )
                .setWeatherDescription(getWeatherDescriptionWeb(finalHourlyForecast.weatherDescription))
                .setHumidity(finalHourlyForecast.humidity).pop =
                if (!finalHourlyForecast.pop.contains("%")) "-" else finalHourlyForecast.pop
            if (finalHourlyForecast.windDirection != null) {
                windSpeed = finalHourlyForecast.windSpeed.replace(mPerSec, "")
                windDirectionStr = finalHourlyForecast.windDirection.replace(poong, "")
                windDirectionInt = WindUtil.parseWindDirectionStrAsInt(windDirectionStr)
                hourlyForecastDto.setWindDirectionVal(windDirectionInt)
                    .setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, windDirectionInt.toString()))
                    .setWindStrength(WindUtil.getSimpleWindSpeedDescription(windSpeed)).windSpeed =
                    ValueUnits.convertWindSpeed(windSpeed, windUnit)
                        .toString() + MyApplication.VALUE_UNIT_OBJ.windUnitText
                feelsLikeTemp = WeatherUtil.calcFeelsLikeTemperature(
                    finalHourlyForecast.temp.toDouble(),
                    ValueUnits.convertWindSpeed(windSpeed, ValueUnits.kmPerHour), humidity.toDouble()
                )
                hourlyForecastDto.feelsLikeTemp = ValueUnits.convertTemperature(
                    feelsLikeTemp.toString(),
                    tempUnit
                ).toString() + tempDegree
            }
            hourlyForecastDtoList.add(hourlyForecastDto)
        }
        return hourlyForecastDtoList
    }

    @JvmStatic
    fun makeDailyForecastDtoListOfWEB(dailyForecastList: List<ParsedKmaDailyForecast>): List<DailyForecastDto> {
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempDegree = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val dailyForecastDtoList: MutableList<DailyForecastDto> = ArrayList()
        for (finalDailyForecast: ParsedKmaDailyForecast in dailyForecastList) {
            val dailyForecastDto = DailyForecastDto()
            dailyForecastDtoList.add(dailyForecastDto)
            dailyForecastDto.setDate(finalDailyForecast.getDate())
                .setMinTemp(ValueUnits.convertTemperature(finalDailyForecast.minTemp, tempUnit).toString() + tempDegree).maxTemp =
                ValueUnits.convertTemperature(finalDailyForecast.maxTemp, tempUnit).toString() + tempDegree
            if (finalDailyForecast.isSingle) {
                val single = DailyForecastDto.Values()
                single.setPop(finalDailyForecast.singleValues!!.pop)
                    .setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.singleValues.weatherDescription, false)).weatherDescription =
                    getWeatherMidIconDescription(
                        finalDailyForecast.singleValues.weatherDescription
                    )
                dailyForecastDto.valuesList.add(single)
            } else {
                val am = DailyForecastDto.Values()
                val pm = DailyForecastDto.Values()
                dailyForecastDto.valuesList.add(am)
                dailyForecastDto.valuesList.add(pm)
                am.setPop(finalDailyForecast.amValues!!.pop)
                    .setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.amValues.weatherDescription, false)).weatherDescription =
                    getWeatherMidIconDescription(
                        finalDailyForecast.amValues.weatherDescription
                    )
                pm.setPop(finalDailyForecast.pmValues!!.pop)
                    .setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.pmValues.weatherDescription, false)).weatherDescription =
                    getWeatherMidIconDescription(
                        finalDailyForecast.pmValues.weatherDescription
                    )
            }
        }
        return dailyForecastDtoList
    }

    @JvmStatic
    fun makeCurrentConditionsDtoOfWEB(
        context: Context?,
        parsedKmaCurrentConditions: ParsedKmaCurrentConditions,
        parsedKmaHourlyForecast: ParsedKmaHourlyForecast,
        latitude: Double?,
        longitude: Double?
    ): CurrentConditionsDto {
        val windUnit = MyApplication.VALUE_UNIT_OBJ.windUnit
        val tempUnit = MyApplication.VALUE_UNIT_OBJ.tempUnit
        val tempUnitStr = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val currentConditionsDto = CurrentConditionsDto()
        val currentTime = ZonedDateTime.parse(parsedKmaCurrentConditions.baseDateTimeISO8601)
        var currentPtyCode = parsedKmaCurrentConditions.pty
        val hourlyForecastDescription = parsedKmaHourlyForecast.weatherDescription
        val koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul")
        val sunriseSunsetCalculator = SunriseSunsetCalculator(
            Location((latitude)!!, (longitude)!!),
            koreaTimeZone
        )
        val calendar = Calendar.getInstance(koreaTimeZone)
        calendar[currentTime.year, currentTime.monthValue - 1, currentTime.dayOfMonth, currentTime.hour] = currentTime.minute
        val sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar)
        val sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar)
        currentConditionsDto.setCurrentTime(currentTime)
        currentConditionsDto.setWeatherDescription(getWeatherDescriptionWeb((if (currentPtyCode!!.isEmpty()) hourlyForecastDescription else currentPtyCode)))
        currentConditionsDto.setWeatherIcon(
            getWeatherIconImgWeb(
                (if (currentPtyCode!!.isEmpty()) hourlyForecastDescription else currentPtyCode),
                SunRiseSetUtil.isNight(calendar, sunRise, sunSet)
            )
        )
        currentConditionsDto.setTemp(ValueUnits.convertTemperature(parsedKmaCurrentConditions.temp, tempUnit).toString() + tempUnitStr)
        currentConditionsDto.setFeelsLikeTemp(
            ValueUnits.convertTemperature(parsedKmaCurrentConditions.feelsLikeTemp, tempUnit).toString() + tempUnitStr
        )
        currentConditionsDto.setHumidity(parsedKmaCurrentConditions.humidity)
        currentConditionsDto.setYesterdayTemp(parsedKmaCurrentConditions.yesterdayTemp)
        if (parsedKmaCurrentConditions.windDirection != null) {
            val windDirectionDegree = WindUtil.parseWindDirectionStrAsInt(parsedKmaCurrentConditions.windDirection)
            currentConditionsDto.setWindDirectionDegree(windDirectionDegree)
            currentConditionsDto.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, windDirectionDegree.toString()))
        }
        if (parsedKmaCurrentConditions.windSpeed != null) {
            val windSpeed = parsedKmaCurrentConditions.windSpeed.toDouble()
            currentConditionsDto.setWindSpeed(
                ValueUnits.convertWindSpeed(windSpeed.toString(), windUnit).toString() + MyApplication.VALUE_UNIT_OBJ.windUnitText
            )
            currentConditionsDto.setSimpleWindStrength(WindUtil.getSimpleWindSpeedDescription(windSpeed.toString()))
            currentConditionsDto.setWindStrength(WindUtil.getWindSpeedDescription(windSpeed.toString()))
        }
        if (currentPtyCode!!.isEmpty()) {
            currentPtyCode = "0"
        } else {
            currentPtyCode = convertPtyTextToCode(currentPtyCode)
        }
        currentConditionsDto.setPrecipitationType(getWeatherPtyIconDescription(currentPtyCode))
        if (!parsedKmaCurrentConditions.precipitationVolume!!.contains("-") && !parsedKmaCurrentConditions.precipitationVolume.contains("0.0")) {
            currentConditionsDto.precipitationVolume = (parsedKmaCurrentConditions.precipitationVolume)
        }
        return currentConditionsDto
    }

    val zoneId: ZoneId
        get() = ZoneId.of("Asia/Seoul")
}