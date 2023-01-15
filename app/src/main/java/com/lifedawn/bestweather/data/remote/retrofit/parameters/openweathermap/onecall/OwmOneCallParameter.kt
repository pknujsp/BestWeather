package com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RestRequestParameter

data class OwmOneCallParameter(
    val latitude: String = "",
    var longitude: String = "",
    val oneCallApis: MutableSet<OneCallApis> = mutableSetOf<OneCallApis>(OneCallApis.daily, OneCallApis.alerts)
) : RestRequestParameter() {


    enum class OneCallApis {
        current, minutely, hourly, daily, alerts
    }

    val map: Map<String, String>
        get() {
            val map = mutableMapOf<String, String>()

            map["lat"] = latitude
            map["lon"] = longitude
            map["units"] = "metric"

            val stringBuilder = StringBuilder()
            for (exclude in oneCallApis) {
                stringBuilder.append(exclude.toString()).append(",")
            }
            stringBuilder.deleteCharAt(stringBuilder.length - 1)
            map["exclude"] = stringBuilder.toString()

            map["appid"] = RetrofitClient.OWM_ONECALL_API_KEY
            return map.toMap()
        }


}