package com.lifedawn.bestweather.data.remote.nominatim.repository

import com.lifedawn.bestweather.data.remote.nominatim.datasource.NominatimDataSource
import com.lifedawn.bestweather.data.remote.nominatim.model.GeocodingDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NominatimRepositoryImpl @Inject constructor(
    private val nominatimDataSource: NominatimDataSource,
) : NominatimRepository {
    override fun geocoding(query: String): Flow<ApiResponse<List<GeocodingDto>>> {
        TODO("Not yet implemented")
    }

    override fun reverseGeocoding(latitude: Double, longitude: Double): Flow<ApiResponse<GeocodingDto>> {
        TODO("Not yet implemented")
    }
}