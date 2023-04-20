package com.lifedawn.bestweather.data.remote.nominatim.repository

import com.lifedawn.bestweather.data.remote.nominatim.model.GeocodingDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import kotlinx.coroutines.flow.Flow

interface NominatimRepository {
    fun geocoding(query: String): Flow<ApiResponse<List<GeocodingDto>>>
    fun reverseGeocoding(latitude: Double, longitude: Double): Flow<ApiResponse<GeocodingDto>>
}