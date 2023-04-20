package com.lifedawn.bestweather.commons.utils

import com.lifedawn.bestweather.data.local.timezone.LocalTimeZoneRepository
import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDto
import com.lifedawn.bestweather.data.remote.retrofit.responses.freetime.FreeTimeResponse
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import retrofit2.Response
import java.time.ZoneId

class TimeZoneUtils {
    fun getTimeZone(timeZoneIdRepository: LocalTimeZoneRepository, free, latitude: Double, longitude: Double, callback: TimeZoneCallback) {
        timeZoneIdRepository.get(latitude, longitude, object : DbQueryCallback<TimeZoneIdDto?> {
            override fun onResultSuccessful(result: TimeZoneIdDto?) {
                val zoneId = ZoneId.of(result!!.timeZoneId)
                callback.onResult(zoneId)
            }

            override fun onResultNoData() {
                FreeTimeZoneApi.getTimeZone(latitude, longitude, object : JsonDownloader() {
                    override fun onResponseResult(response: Response<*>?, responseObj: Any, responseText: String) {
                        val freeTimeDto = responseObj as FreeTimeResponse
                        val zoneId = ZoneId.of(freeTimeDto.timezone)

                        timeZoneIdRepository.insert(TimeZoneIdDto(latitude, longitude, zoneId.id))
                        callback.onResult(zoneId)
                    }

                    override fun onResponseResult(t: Throwable) {
                        val zoneId = WeatherResponseProcessor.getZoneId(latitude, longitude)
                        timeZoneIdRepository.insert(TimeZoneIdDto(latitude, longitude, zoneId.id))
                        callback.onResult(zoneId)
                    }
                })
            }
        })


    }

    interface TimeZoneCallback {
        fun onResult(zoneId: ZoneId)
    }
}