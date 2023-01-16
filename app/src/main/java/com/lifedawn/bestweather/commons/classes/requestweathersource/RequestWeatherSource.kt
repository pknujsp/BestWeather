package com.lifedawn.bestweather.commons.classes.requestweathersource

import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient.ServiceType

open class RequestWeatherSource {
    private val requestServiceTypes: MutableSet<ServiceType> = HashSet()
    fun getRequestServiceTypes(): Set<ServiceType> {
        return requestServiceTypes
    }

    fun addRequestServiceType(serviceType: ServiceType): RequestWeatherSource {
        requestServiceTypes.add(serviceType)
        return this
    }
}