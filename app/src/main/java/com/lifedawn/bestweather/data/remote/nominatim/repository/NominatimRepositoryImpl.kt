package com.lifedawn.bestweather.data.remote.nominatim.repository

import com.google.gson.Gson
import com.lifedawn.bestweather.commons.classes.Geocoding
import com.lifedawn.bestweather.data.remote.nominatim.datasource.NominatimDataSource
import com.lifedawn.bestweather.data.remote.nominatim.model.GeocodingDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.ApiResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim.GeocodeResponseDto
import com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim.ReverseGeocodeResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject

class NominatimRepositoryImpl @Inject constructor(
    private val nominatimDataSource: NominatimDataSource,
) : NominatimRepository {
    override fun geocoding(query: String): Flow<ApiResponse<List<GeocodingDto>>> = flow {
        val response = nominatimDataSource.geocoding(query)
        response.collect {
            if (it is ApiResponse.Success) {
                try {
                    val geocodeResponseDto = Gson().fromJson(
                        it.data,
                        GeocodeResponseDto::class.java
                    )

                    geocodeResponseDto?.apply {
                        if (features.isEmpty()) {
                            emit(ApiResponse.Failure(Exception("fail")))
                            return@collect
                        }

                        val addressDtoList = mutableListOf<GeocodingDto>()
                        for (features in this.features) {
                            if (features.properties == null || features.geometry == null) {
                                emit(ApiResponse.Failure(Exception("fail")))
                                return@collect
                            }

                            features.properties?.also { property ->
                                if (property.address == null) {
                                    emit(ApiResponse.Failure(Exception("fail")))
                                    return@collect
                                }

                                val editedDisplayName =
                                    Geocoding.convertDisplayName(property.displayName)

                                features.geometry?.also { geometry ->
                                    property.address?.also { address ->
                                        addressDtoList.add(
                                            GeocodingDto(
                                                geometry.coordinates[1],
                                                geometry.coordinates[0],
                                                editedDisplayName, address.country,
                                                address.countryCode.uppercase(Locale.getDefault())
                                            )
                                        )
                                    }
                                }
                            }


                        }
                    } ?: emit(ApiResponse.Failure(Exception("fail")))

                } catch (e: Exception) {
                    emit(ApiResponse.Failure(e))
                }
            } else {
                emit(ApiResponse.Failure(Exception("fail")))
            }
        }
    }

    override fun reverseGeocoding(latitude: Double, longitude: Double): Flow<ApiResponse<GeocodingDto>> = flow {

        val response = nominatimDataSource.reverseGeocoding(latitude, longitude)
        response.collect {
            if (it is ApiResponse.Success) {
                try {
                    val reverseGeocodeResponseDto = Gson().fromJson(
                        it.data,
                        ReverseGeocodeResponseDto::class.java
                    )

                    reverseGeocodeResponseDto.features[0].properties?.also { property ->
                        property.address?.also { address ->
                            val editedDisplayName = Geocoding.convertDisplayName(property.displayName)
                            val addressDto = GeocodingDto(
                                latitude, longitude,
                                editedDisplayName, address.country,
                                address.countryCode.uppercase(Locale.getDefault())
                            )
                        } ?: emit(ApiResponse.Failure(Exception("fail")))

                    } ?: emit(ApiResponse.Failure(Exception("fail")))

                } catch (e: Exception) {
                    emit(ApiResponse.Failure(e))
                }
            } else {
                emit(ApiResponse.Failure(Exception("fail")))
            }
        }
    }
}