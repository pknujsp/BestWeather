package com.lifedawn.bestweather.data.remote.retrofit.parameters.kma

import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

class KmaCurrentConditionsParameters(val code: String) : RestRequestParameter() {
    private val unit = "m%2Fs"
    private val aws = "N"
    var latitude = 0.0
    var longitude = 0.0

    val parametersMap: Map<String, String>
        get() {
            return mapOf(
                "unit" to unit, "aws" to aws, "code" to code
            )
        }

}