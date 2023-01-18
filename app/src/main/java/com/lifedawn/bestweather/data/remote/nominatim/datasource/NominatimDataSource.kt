package com.lifedawn.bestweather.data.remote.nominatim.datasource

import android.content.Context
import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import kotlinx.coroutines.flow.Flow

interface NominatimDataSource {
    fun geocoding(query: String) : Flow<ApiResponse<JsonElement>>
    fun reverseGeocoding(latitude: Double, longitude: Double) : Flow<ApiResponse<JsonElement>>
}