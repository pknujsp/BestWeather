package com.lifedawn.bestweather.data.remote.retrofit.parameters.metnorway

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

data class LocationForecastParameters(
    var latitude: String = "",
    var longitude: String = ""
) : RestRequestParameter() {

    val map: Map<String, String>
        get() {
            return mapOf(
                "lat" to latitude,
                "lon" to longitude
            )
        }


}