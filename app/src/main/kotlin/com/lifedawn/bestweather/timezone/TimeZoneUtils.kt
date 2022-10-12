package com.lifedawn.bestweather.timezone

import com.lifedawn.bestweather.model.timezone.TimeZoneIdDto
import com.lifedawn.bestweather.model.timezone.TimeZoneIdRepository
import com.lifedawn.bestweather.retrofit.responses.freetime.FreeTimeResponse
import com.lifedawn.bestweather.retrofit.util.JsonDownloader
import com.lifedawn.bestweather.room.callback.DbQueryCallback
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor
import retrofit2.Response
import java.time.ZoneId

class TimeZoneUtils {
    companion object {
        fun getTimeZone(latitude: Double, longitude: Double, callback: TimeZoneCallback) {
            val timeZoneIdRepository = TimeZoneIdRepository.INSTANCE!!

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
    }

    interface TimeZoneCallback {
        fun onResult(zoneId: ZoneId)
    }
}