package com.lifedawn.bestweather.data.remote.weather.aqicn.usecase

import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.remote.weather.aqicn.repository.AqicnRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZoneId
import javax.inject.Inject

class GetAqicnUseCaseImpl @Inject constructor(
    private val aqicnRepository: AqicnRepository
) : GetAqicnUseCase {

    override suspend fun getAirQuality(latitude: Double, longitude: Double, zoneId: ZoneId): Flow<AirQualityDto?> = flow {
        val airQualityDto = aqicnRepository.getAirQuality(latitude, longitude, zoneId)
        airQualityDto.collect {
            emit(it)
        }
    }
}