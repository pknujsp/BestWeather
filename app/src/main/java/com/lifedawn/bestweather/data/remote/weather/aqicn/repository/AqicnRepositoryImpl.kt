package com.lifedawn.bestweather.data.remote.weather.aqicn.repository

import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.parameters.aqicn.AqicnParameters
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getAirQualityObjFromJson
import com.lifedawn.bestweather.data.remote.weather.aqicn.datasource.AqicnDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZoneId
import javax.inject.Inject

class AqicnRepositoryImpl @Inject constructor(
    private val aqicnDataSource: AqicnDataSource,
) : AqicnRepository {
    override fun getAirQuality(
        latitude: Double, longitude: Double, zoneId: ZoneId
    ): Flow<AirQualityDto?> = flow {
        val response = aqicnDataSource.getAqicn(AqicnParameters(latitude.toString(), longitude.toString()))
        response.collect {
            if (it is ApiResponse.Success) {
                val aqicnResponse = getAirQualityObjFromJson(it.data.toString())

                if (aqicnResponse == null)
                    emit(null)
                else {
                    val airQualityDto = AqicnResponseProcessor.makeAirQualityDto(aqicnResponse, zoneId)
                    emit(airQualityDto)
                }
            } else {
                emit(null)
            }
        }
    }
}