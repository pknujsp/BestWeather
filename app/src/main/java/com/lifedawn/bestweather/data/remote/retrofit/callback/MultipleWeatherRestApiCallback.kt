package com.lifedawn.bestweather.data.remote.retrofit.callback

import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient.ServiceType
import com.lifedawn.bestweather.data.remote.retrofit.parameters.RequestParameter
import retrofit2.Call
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

abstract class MultipleWeatherRestApiCallback(private val requestCount: Int = 0) {
    val requestDateTimeISO8601: String = ZonedDateTime.now().toString()
    private val responseCount = AtomicInteger(0)

    var isResponseCompleted = false
        private set
    var zoneId: ZoneId? = null
        private set

    val valueMap = ConcurrentHashMap<String, String>()
    val callMap = ConcurrentHashMap<ServiceType, Call<Any>>()
    val responseMap = ConcurrentHashMap<WeatherProviderType, ArrayMap<ServiceType, Result<Any>>>()

    fun setResponseCompleted(responseCompleted: Boolean): MultipleWeatherRestApiCallback {
        isResponseCompleted = responseCompleted
        return this
    }


    fun setResponseCount(responseCount: Int) {
        this.responseCount.set(responseCount)
    }

    fun getValue(key: String): String? {
        return valueMap[key]
    }

    fun putValue(key: String, value: String) {
        valueMap[key] = value
    }

    abstract fun onResult()
    abstract fun onCanceled()

    fun cancel() {
        responseCount.set(10000)

        if (!callMap.isEmpty())
            for (call in callMap.values)
                call.cancel()
    }

    fun clear() {
        valueMap.clear()
        callMap.clear()
        responseMap.clear()
    }

    fun processResult(
        weatherProviderType: WeatherProviderType, serviceType: ServiceType, result: Result<Any>
    ) {
        responseCount.incrementAndGet()
        if (!responseMap.containsKey(weatherProviderType)) {
            responseMap[weatherProviderType] = arrayMapOf<ServiceType, Result<Any>>()
        }
        responseMap[weatherProviderType]?.set(serviceType, result)

        if (requestCount == responseCount.get()) {
            isResponseCompleted = true
            onResult()
        }
    }

}