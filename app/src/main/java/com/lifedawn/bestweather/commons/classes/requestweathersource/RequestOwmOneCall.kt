package com.lifedawn.bestweather.commons.classes.requestweathersource

import com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall.OwmOneCallParameter.OneCallApis

class RequestOwmOneCall : RequestWeatherSource() {
    var excludeApis: Set<OneCallApis>? = null
}