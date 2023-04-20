package com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim

import android.util.ArrayMap
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

class GeocodeParameterRest(private val query: String) : RestRequestParameter() {
    val map: Map<String, String>
        get() {
            val map: MutableMap<String, String> = ArrayMap()
            map["q"] = query
            map["format"] = "geojson"
            map["addressdetails"] = "1"
            return map
        }
}