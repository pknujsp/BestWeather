package com.lifedawn.bestweather.data.local.timezone

import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDto
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback

interface LocalTimeZoneRepository {
    fun get(lat: Double, lon: Double, callback: DbQueryCallback<TimeZoneIdDto?>)

    fun insert(timeZoneDto: TimeZoneIdDto)

    fun delete(timeZoneDto: TimeZoneIdDto)

    fun reset(callback: DbQueryCallback<Boolean>)
}