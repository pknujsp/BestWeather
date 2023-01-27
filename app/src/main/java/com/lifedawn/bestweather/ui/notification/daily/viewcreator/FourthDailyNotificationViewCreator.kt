package com.lifedawn.bestweather.ui.notification.daily.viewcreator

import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import java.time.ZonedDateTime

class FourthDailyNotificationViewCreator constructor(context: Context) : AbstractDailyNotiViewCreator(context) {
    fun setDataViews(
        remoteViews: RemoteViews, addressName: String?, lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto)
    }

    public override fun createRemoteViews(needTempData: Boolean): RemoteViews {
        val remoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.fourth_daily_noti_view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            remoteViews.setViewPadding(R.id.root_layout, 0, 0, 0, 0)
        }
        if (needTempData) {
            setTempDataViews(remoteViews)
        }
        return remoteViews
    }

    public override fun setTempDataViews(remoteViews: RemoteViews) {
        drawViews(
            remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempAirQualityDto()
        )
    }

    private fun drawViews(remoteViews: RemoteViews, addressName: String?, lastRefreshDateTime: String, airQualityDto: AirQualityDto?) {
        remoteViews.setTextViewText(R.id.address, addressName)
        remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter))
        val noData: String = "-"
        remoteViews.setTextViewText(
            R.id.measuring_station_name,
            context.getString(R.string.measuring_station_name) + ": " + (if (airQualityDto!!.cityName == null) noData else airQualityDto.cityName)
        )
        remoteViews.setTextViewText(
            R.id.airQuality,
            context.getString(R.string.currentAirQuality) + "\n" + getGradeDescription(
                airQualityDto.aqi
            )
        )
        remoteViews.setTextViewText(
            R.id.pm10, if (!airQualityDto.current!!.isHasPm10) noData else getGradeDescription(
                airQualityDto.current!!.pm10
            )
        )
        remoteViews.setTextViewText(
            R.id.pm25, if (!airQualityDto.current!!.isHasPm25) noData else getGradeDescription(
                airQualityDto.current!!.pm25
            )
        )
        remoteViews.setTextViewText(
            R.id.so2, if (!airQualityDto.current!!.isHasSo2) noData else getGradeDescription(
                airQualityDto.current!!.so2
            )
        )
        remoteViews.setTextViewText(
            R.id.co, if (!airQualityDto.current!!.isHasCo) noData else getGradeDescription(
                airQualityDto.current!!.co
            )
        )
        remoteViews.setTextViewText(
            R.id.o3, if (!airQualityDto.current!!.isHasO3) noData else getGradeDescription(
                airQualityDto.current!!.o3
            )
        )
        remoteViews.setTextViewText(
            R.id.no2, if (!airQualityDto.current!!.isHasNo2) noData else getGradeDescription(
                airQualityDto.current!!.no2
            )
        )
    }

    public override fun setResultViews(
        remoteViews: RemoteViews,
        dailyPushNotificationDto: DailyPushNotificationDto,
        weatherProviderTypeSet: Set<WeatherProviderType?>?,
        multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
        weatherDataTypeSet: Set<WeatherDataType?>?
    ) {
        val refreshDateTime: String = multipleWeatherRestApiCallback.getRequestDateTime().toString()
        zoneId = multipleWeatherRestApiCallback!!.zoneId
        val airQualityDto: AirQualityDto? = WeatherResponseProcessor.getAirQualityDto(multipleWeatherRestApiCallback, null)
        val successful: Boolean = airQualityDto.isSuccessful()
        if (successful) {
            setDataViews(remoteViews, dailyPushNotificationDto.addressName, refreshDateTime, airQualityDto)
            makeNotification(remoteViews, dailyPushNotificationDto.id)
        } else {
            makeFailedNotification(dailyPushNotificationDto.id, context.getString(R.string.msg_failed_update))
        }
    }

    override val requestWeatherDataTypeSet: Set<WeatherDataType>
        get() {
            val set: MutableSet<WeatherDataType> = HashSet()
            set.add(WeatherDataType.airQuality)
            return set
        }
}