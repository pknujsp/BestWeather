package com.lifedawn.bestweather.data.remote.retrofit.parameters.kma

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

class KmaForecastsParameters(val code: String) : RestRequestParameter() {
    private val unit = "m%2Fs"
    private val hr1 = "Y"
    private val ext = "N"
    var latitude = 0.0
    var longitude = 0.0
    val parametersMap: Map<String, String>
        get() {
            return mapOf(
                "unit" to unit, "hr1" to hr1,
                "ext" to ext, "code" to code
            )
        }


}