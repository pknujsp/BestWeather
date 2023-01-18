package com.lifedawn.bestweather.data.remote.nominatim.usecase

import com.lifedawn.bestweather.data.remote.nominatim.model.GeocodingDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import kotlinx.coroutines.flow.Flow

interface GeocodingUseCase {
    fun geocoding(query: String): Flow<ApiResponse<List<GeocodingDto>>>
    fun reverseGeocoding(latitude: Double, longitude: Double): Flow<ApiResponse<GeocodingDto>>
}