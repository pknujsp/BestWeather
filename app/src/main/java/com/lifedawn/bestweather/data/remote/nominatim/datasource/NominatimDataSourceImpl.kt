package com.lifedawn.bestweather.data.remote.nominatim.datasource

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NominatimDataSourceImpl @Inject constructor(
    private val nominatimRestApi: RestfulApiQuery
) :
    NominatimDataSource {

    override fun geocoding(query: String): Flow<ApiResponse<JsonElement>> {
        TODO("Not yet implemented")
    }

    override fun reverseGeocoding(latitude: Double, longitude: Double): Flow<ApiResponse<JsonElement>> {
        TODO("Not yet implemented")
    }
}