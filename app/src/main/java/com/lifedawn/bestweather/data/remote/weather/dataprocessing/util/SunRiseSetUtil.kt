package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

object SunRiseSetUtil {
    fun getDailySunRiseSetMap(
        begin: ZonedDateTime, end: ZonedDateTime, latitude: Double,
        longitude: Double
    ): Map<Int, SunRiseSetObj> {
        val zoneId = begin.zone
        val utc0TimeZone = ZoneId.of(TimeZone.getTimeZone("UTC").id)
        val realTimeZone = TimeZone.getTimeZone(zoneId.id)
        var beginUtc0ZonedDateTime = ZonedDateTime.of(begin.toLocalDateTime(), zoneId)
        var beginRealZonedDateTime = ZonedDateTime.of(begin.toLocalDateTime(), zoneId)
        var endUtc0ZonedDateTime = ZonedDateTime.of(end.toLocalDateTime(), zoneId)
        beginUtc0ZonedDateTime = beginUtc0ZonedDateTime.withZoneSameLocal(utc0TimeZone)
        endUtc0ZonedDateTime = endUtc0ZonedDateTime.withZoneSameLocal(utc0TimeZone)
        var beginDay: Long
        val endDay = TimeUnit.MILLISECONDS.toDays(endUtc0ZonedDateTime.toInstant().toEpochMilli())
        val map: MutableMap<Int, SunRiseSetObj> = HashMap()
        val calculator = SunriseSunsetCalculator(Location(latitude, longitude), realTimeZone)
        val calendar = Calendar.getInstance(realTimeZone)
        do {
            calendar.timeInMillis = beginRealZonedDateTime.toInstant().toEpochMilli()
            map[beginRealZonedDateTime.dayOfYear] = SunRiseSetObj(
                calculator.getOfficialSunriseCalendarForDate(calendar),
                calculator.getOfficialSunsetCalendarForDate(calendar)
            )
            beginUtc0ZonedDateTime = beginUtc0ZonedDateTime.plusDays(1)
            beginRealZonedDateTime = beginRealZonedDateTime.plusDays(1)
            beginDay = TimeUnit.MILLISECONDS.toDays(beginUtc0ZonedDateTime.toInstant().toEpochMilli())
        } while (beginDay <= endDay)
        return map
    }

    fun isNight(compDate: Calendar, sunRiseDate: Calendar?, sunSetDate: Calendar?): Boolean {
        if (sunRiseDate == null || sunSetDate == null)
            return false

        val compH = TimeUnit.MILLISECONDS.toHours(compDate.timeInMillis)
        val sunRiseH = TimeUnit.MILLISECONDS.toHours(sunRiseDate.timeInMillis)
        val sunSetH = TimeUnit.MILLISECONDS.toHours(sunSetDate.timeInMillis)
        return compH > sunSetH || compH < sunRiseH
    }

    class SunRiseSetObj {
        var zonedDateTime: ZonedDateTime? = null
        val sunrise: Calendar
        val sunset: Calendar

        constructor(sunrise: Calendar, sunset: Calendar) {
            this.sunrise = sunrise
            this.sunset = sunset
        }

        constructor(zonedDateTime: ZonedDateTime?, sunrise: Calendar, sunset: Calendar) {
            this.zonedDateTime = zonedDateTime
            this.sunrise = sunrise
            this.sunset = sunset
        }
    }
}