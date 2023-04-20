package com.lifedawn.bestweather.data.remote.nominatim.usecase

import com.lifedawn.bestweather.data.remote.nominatim.model.GeocodingDto
import com.lifedawn.bestweather.data.remote.nominatim.repository.NominatimRepository
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GeocodingUseCaseImpl @Inject constructor(
    private val nominatimRepository: NominatimRepository
) : GeocodingUseCase {
    override fun geocoding(query: String): Flow<ApiResponse<List<GeocodingDto>>> = flow {
        nominatimRepository.geocoding(query).collect {
            emit(it)
        }
    }

    override fun reverseGeocoding(latitude: Double, longitude: Double): Flow<ApiResponse<GeocodingDto>> = flow {
        nominatimRepository.reverseGeocoding(latitude, longitude).collect {
            emit(it)
        }
    }
}