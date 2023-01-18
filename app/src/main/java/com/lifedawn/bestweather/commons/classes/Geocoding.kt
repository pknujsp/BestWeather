package com.lifedawn.bestweather.commons.classes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient.getApiService
import com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim.ReverseGeocodeParameterRest
import com.lifedawn.bestweather.data.remote.retrofit.responses.nominatim.ReverseGeocodeResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

object Geocoding {

    fun nominatimReverseGeocoding(context: Context?, latitude: Double?, longitude: Double?, callback: ReverseGeocodingCallback) {
        val parameter = ReverseGeocodeParameterRest(latitude, longitude)
        val call: Call<JsonElement> = getApiService(RetrofitClient.ServiceType.NOMINATIM).nominatimReverseGeocode(
            parameter.map,
            MyApplication.locale.toLanguageTag()
        )
        call.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    val reverseGeocodeResponseDto = Gson().fromJson(
                        response.body().toString(),
                        ReverseGeocodeResponseDto::class.java
                    )
                    val properties = reverseGeocodeResponseDto.features[0].properties
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

     fun convertDisplayName(originalDisplayName: String): String {
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


}