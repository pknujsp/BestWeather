package com.lifedawn.bestweather.data.remote.retrofit.parameters.nominatim

import android.util.ArrayMap
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

class ReverseGeocodeParameterRest(// format=geojson&lat=44.50155&lon=11.33989
    private val latitude: Double, private val longitude: Double
) : RestRequestParameter() {
    val map: Map<String, String>
        get() {
            val map: MutableMap<String, String> = ArrayMap()
            map["format"] = "geojson"
            map["lat"] = latitude.toString()
            map["lon"] = longitude.toString()
            map["zoom"] = "14"
            return map
        }
}