package com.lifedawn.bestweather.commons.classes

import java.time.ZonedDateTime
import java.util.*

object ClockUtil {
    fun areSameDate(dt1: Long, dt2: Long): Boolean {
        val dt1Calendar = GregorianCalendar()
        dt1Calendar.timeInMillis = dt1
        val dt2Calendar = GregorianCalendar()
        dt2Calendar.timeInMillis = dt2
        return if (dt1Calendar[Calendar.YEAR] == dt2Calendar[Calendar.YEAR] &&
            dt1Calendar[Calendar.DAY_OF_YEAR] == dt2Calendar[Calendar.DAY_OF_YEAR]
        ) {
            true
        } else {
            false
        }
    }

    fun areSameDate(calendar1: Calendar, calendar2: Calendar): Boolean {
        return if (calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR] &&
            calendar1[Calendar.DAY_OF_YEAR] == calendar2[Calendar.DAY_OF_YEAR]
        ) {
            true
        } else {
            false
        }
    }

    fun convertISO8061Format(dateTime: String?): ZonedDateTime {
        //2021-10-22T13:31:00+09:00
        return ZonedDateTime.parse(dateTime)
    }
}