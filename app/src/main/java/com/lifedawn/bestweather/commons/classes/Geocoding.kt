package com.lifedawn.bestweather.commons.classes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient.getApiService
import com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim.GeocodeParameterRest
import com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim.ReverseGeocodeParameterRest
import com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim.GeocodeResponse
import com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim.ReverseGeocodeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

object Geocoding {

    fun nominatimGeocoding(context: Context, query: String) {
        val parameter = GeocodeParameterRest(query)
        val call: Call<JsonElement> = getApiService(RetrofitClient.ServiceType.NOMINATIM).nominatimGeocode(
            parameter.map,
            MyApplication.locale.toLanguageTag()
        )
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful) {
                    val geocodeResponse = Gson().fromJson(
                        response.body(),
                        GeocodeResponse::class.java
                    )
                    val addressDtoList: MutableList<AddressDto?> = ArrayList()
                    for (features in geocodeResponse.features) {
                        val properties = features.properties
                        val editedDisplayName = convertDisplayName(properties.displayName)
                        addressDtoList.add(
                            AddressDto(
                                features.geometry.coordinates[1],
                                features.geometry.coordinates[0],
                                editedDisplayName, properties.address.country,
                                properties.address.countryCode.uppercase(Locale.getDefault())
                            )
                        )
                    }
                    callback.onGeocodingResult(addressDtoList)
                } else {
                    androidGeocoding(context, query, callback)
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                androidGeocoding(context, query, callback)
            }
        })
    }

    fun nominatimReverseGeocoding(context: Context?, latitude: Double?, longitude: Double?, callback: ReverseGeocodingCallback) {
        val parameter = ReverseGeocodeParameterRest(latitude, longitude)
        val call: Call<JsonElement> = getApiService(RetrofitClient.ServiceType.NOMINATIM).nominatimReverseGeocode(
            parameter.map,
            MyApplication.locale.toLanguageTag()
        )
        call.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    val reverseGeocodeResponse = Gson().fromJson(
                        response.body().toString(),
                        ReverseGeocodeResponse::class.java
                    )
                    val properties = reverseGeocodeResponse.features[0].properties
                    val editedDisplayName = convertDisplayName(properties.displayName)
                    val addressDto = AddressDto(
                        latitude, longitude,
                        editedDisplayName, properties.address.country,
                        properties.address.countryCode.uppercase(Locale.getDefault())
                    )
                    callback.onReverseGeocodingResult(addressDto)
                } else {
                    androidReverseGeocoding(context, latitude, longitude, callback)
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                androidReverseGeocoding(context, latitude, longitude, callback)
            }
        })
    }

    private fun convertDisplayName(originalDisplayName: String): String {
        val separatedDisplayNames = originalDisplayName.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (separatedDisplayNames.size > 2) {
            val stringBuilder = StringBuilder()
            val lastIdx = separatedDisplayNames.size - 2
            for (i in 0..lastIdx) {
                stringBuilder.append(separatedDisplayNames[i])
                if (i < lastIdx) {
                    stringBuilder.append(", ")
                }
            }
            stringBuilder.toString()
        } else {
            originalDisplayName
        }
    }

    fun interface ReverseGeocodingCallback {
        fun onReverseGeocodingResult(addressDto: AddressDto?)
    }

    fun interface GeocodingCallback {
        fun onGeocodingResult(addressList: List<AddressDto>)
    }

    data class AddressDto(
        val latitude: Double, val longitude: Double,
        val displayName: String, val country: String, val countryCode: String
    )
}