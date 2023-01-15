package com.lifedawn.bestweather.data.remote.weather.aqicn.repository

import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import kotlinx.coroutines.flow.Flow

interface AqicnRepository {
    fun getAirQuality(
        latitude: Double,
        longitude: Double
    ): Flow<ApiResponse<AirQualityDto>>
}