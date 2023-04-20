package com.lifedawn.bestweather.data.remote.retrofit.parameters.freetime

import android.util.ArrayMap
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

class FreeTimeParameterRest(private val latitude: Double, private val longitude: Double) : RestRequestParameter() {
    val map: Map<String, String>
        get() {
            val map: MutableMap<String, String> = ArrayMap()
            map["latitude"] = latitude.toString()
            map["longitude"] = longitude.toString()
            return map
        }
}