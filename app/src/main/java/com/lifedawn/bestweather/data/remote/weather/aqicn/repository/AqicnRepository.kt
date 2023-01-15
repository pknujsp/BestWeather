package com.lifedawn.bestweather.data.remote.weather.aqicn.repository

import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

interface AqicnRepository {
    fun getAirQuality(
        latitude: Double,
        longitude: Double,
        zoneId: ZoneId
    ): Flow<AirQualityDto?>
}