package com.lifedawn.bestweather.data.remote.weather.aqicn.repository

import android.content.Context
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AqicnRepositoryImpl @Inject constructor(
    private val aqicnDataSource: AqicnDataSource,
    private val context: Context
) : AqicnRepository {
    override fun getAirQuality(latitude: Double, longitude: Double): Flow<ApiResponse<AirQualityDto>> {
        TODO("Not yet implemented")
    }
}