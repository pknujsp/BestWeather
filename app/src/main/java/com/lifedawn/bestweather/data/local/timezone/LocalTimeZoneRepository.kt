package com.lifedawn.bestweather.data.local.timezone

import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDto
import kotlinx.coroutines.flow.Flow

interface LocalTimeZoneRepository {
    fun get(lat: Double, lon: Double): Flow<TimeZoneIdDto>

    suspend fun insert(timeZoneDto: TimeZoneIdDto)

    suspend fun delete(timeZoneDto: TimeZoneIdDto)

    fun reset(): Flow<Boolean>
}