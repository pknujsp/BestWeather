package com.lifedawn.bestweather.data.remote.nominatim.datasource

import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.callback.requestApiFlow
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery
import com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim.GeocodeParameterRest
import com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim.ReverseGeocodeParameterRest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NominatimDataSourceImpl @Inject constructor(
    private val nominatimRestApi: RestfulApiQuery
) : NominatimDataSource {

    override fun geocoding(query: String): Flow<ApiResponse<JsonElement>> = flow {
        requestApiFlow {
            nominatimRestApi.nominatimGeocode(
                GeocodeParameterRest(query).map, MyApplication.locale.toLanguageTag()
            )
        }
    }

    override fun reverseGeocoding(latitude: Double, longitude: Double): Flow<ApiResponse<JsonElement>> = flow {
        requestApiFlow {
            nominatimRestApi.nominatimReverseGeocode(
                ReverseGeocodeParameterRest(latitude, longitude).map, MyApplication.locale.toLanguageTag()
            )
        }
    }
}