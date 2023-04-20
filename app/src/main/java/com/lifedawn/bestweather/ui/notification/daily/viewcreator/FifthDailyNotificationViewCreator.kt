package com.lifedawn.bestweather.ui.notification.daily.viewcreator

import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.Current.isHasO3
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.Current.isHasPm10
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.Current.isHasPm25
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast.Val
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeColorId
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FifthDailyNotificationViewCreator constructor(context: Context) : AbstractDailyNotiViewCreator(context) {
    fun setDataViews(
        remoteViews: RemoteViews, addressName: String?, lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto)
    }

    public override fun createRemoteViews(needTempData: Boolean): RemoteViews {
        val remoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.fifth_daily_noti_view)
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
            remoteViews,
            context.getString(R.string.address_name),
            ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempAirQualityDto()
        )
    }

    private fun drawViews(
        remoteViews: RemoteViews, addressName: String?, lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?
    ) {
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
        val current: DailyForecast = DailyForecast()
        current.setDate(null).setPm10(Val().setAvg(airQualityDto.current!!.pm10))
            .setPm25(Val().setAvg(airQualityDto.current!!.pm25))
            .setO3(Val().setAvg(airQualityDto.current!!.o3))
        val dailyForecastList: MutableList<DailyForecast> = ArrayList()
        dailyForecastList.add(current)
        dailyForecastList.addAll(airQualityDto.dailyForecastList)
        val forecastDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E")
        val packageName: String = context.getPackageName()
        val maxCount: Int = 7
        var count: Int = dailyForecastList.size
        if (maxCount < count) {
            count = maxCount
        }
        var i: Int = 1
        for (item: DailyForecast in dailyForecastList) {
            if (i++ > count) {
                break
            }
            val forecastItemView: RemoteViews = RemoteViews(packageName, R.layout.item_view_color_airquality)
            forecastItemView.setTextViewText(
                R.id.date,
                if (item.date == null) context.getString(R.string.current) else item.date.format(forecastDateFormatter)
            )
            if (item.isHasPm10()) {
                forecastItemView.setTextViewText(R.id.pm10, item.pm10!!.avg.toString())
                forecastItemView.setTextColor(R.id.pm10, getGradeColorId(item.pm10!!.avg))
            } else {
                forecastItemView.setTextViewText(R.id.pm10, "?")
            }
            if (item.isHasPm25()) {
                forecastItemView.setTextViewText(R.id.pm25, item.pm25!!.avg.toString())
                forecastItemView.setTextColor(R.id.pm25, getGradeColorId(item.pm25!!.avg))
            } else {
                forecastItemView.setTextViewText(R.id.pm25, "?")
            }
            if (item.isHasO3()) {
                forecastItemView.setTextViewText(R.id.o3, item.o3!!.avg.toString())
                forecastItemView.setTextColor(R.id.o3, getGradeColorId(item.o3!!.avg))
            } else {
                forecastItemView.setTextViewText(R.id.o3, "?")
            }
            remoteViews.addView(R.id.forecast, forecastItemView)
        }
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