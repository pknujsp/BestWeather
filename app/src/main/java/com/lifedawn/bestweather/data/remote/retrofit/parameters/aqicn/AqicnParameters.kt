package com.lifedawn.bestweather.data.remote.retrofit.parameters.aqicn

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

data class AqicnParameters(
    var latitude: String = "",
    var longitude: String = ""
) : RestRequestParameter() {

    val map: Map<String, String>
        get() = mapOf("token" to RetrofitClient.AQICN_TOKEN)

}