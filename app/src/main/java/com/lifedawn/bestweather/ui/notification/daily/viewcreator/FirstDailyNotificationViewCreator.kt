package com.lifedawn.bestweather.ui.notification.daily.viewcreator

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FirstDailyNotificationViewCreator constructor(context: Context) : AbstractDailyNotiViewCreator(context) {
    private val cellCount: Int = 9
    fun setDataViews(
        remoteViews: RemoteViews, addressName: String?, lastRefreshDateTime: String,
        hourlyForecastDtoList: List<HourlyForecastDto?>
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, hourlyForecastDtoList)
    }

    public override fun createRemoteViews(needTempData: Boolean): RemoteViews {
        val remoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.first_daily_noti_view)
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
            WeatherResponseProcessor.getTempHourlyForecastDtoList(context, cellCount)
        )
    }

    private fun drawViews(
        remoteViews: RemoteViews, addressName: String?, lastRefreshDateTime: String,
        hourlyForecastDtoList: List<HourlyForecastDto?>
    ) {
        val hours0Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E H")
        val hoursFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H")
        val mm: String = "mm"
        val cm: String = "cm"
        var haveRain: Boolean = false
        var haveSnow: Boolean = false
        for (cell in 0 until cellCount) {
            if (hourlyForecastDtoList.get(cell)!!.isHasRain || hourlyForecastDtoList.get(cell)!!.isHasPrecipitation) {
                haveRain = true
            }
            if (hourlyForecastDtoList.get(cell)!!.isHasSnow) {
                haveSnow = true
            }
        }
        var rain: String? = null
        for (cell in 0 until cellCount) {
            val forecastRemoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear)
            forecastRemoteViews.setTextViewText(
                R.id.dateTime,
                if (hourlyForecastDtoList.get(cell)!!.hours.getHour() == 0) hourlyForecastDtoList.get(cell)!!.hours.format(hours0Formatter) else hourlyForecastDtoList.get(
                    cell
                )!!.hours.format(hoursFormatter)
            )
            forecastRemoteViews.setImageViewResource(R.id.leftIcon, hourlyForecastDtoList.get(cell)!!.weatherIcon)
            forecastRemoteViews.setTextViewText(R.id.pop, hourlyForecastDtoList.get(cell)!!.pop)
            if (hourlyForecastDtoList.get(cell)!!.isHasRain || hourlyForecastDtoList.get(cell)!!.isHasPrecipitation) {
                rain =
                    if (hourlyForecastDtoList.get(cell)!!.isHasRain) hourlyForecastDtoList.get(cell)!!.rainVolume else hourlyForecastDtoList.get(
                        cell
                    )!!.precipitationVolume
                forecastRemoteViews.setTextViewText(R.id.rainVolume, rain.replace(mm, "").replace(cm, ""))
            } else {
                forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, if (haveRain) View.INVISIBLE else View.GONE)
            }
            if (hourlyForecastDtoList.get(cell)!!.isHasSnow) {
                forecastRemoteViews.setTextViewText(
                    R.id.snowVolume,
                    hourlyForecastDtoList.get(cell)!!.snowVolume.replace(mm, "").replace(cm, "")
                )
            } else {
                forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, if (haveSnow) View.INVISIBLE else View.GONE)
            }
            forecastRemoteViews.setTextViewText(R.id.temperature, hourlyForecastDtoList.get(cell)!!.temp)
            forecastRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE)
            remoteViews.addView(R.id.hourlyForecast, forecastRemoteViews)
        }
        remoteViews.setTextViewText(R.id.address, addressName)
        remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter))
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
        val weatherProviderType: WeatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType((weatherProviderTypeSet)!!)
        val hourlyForecastDtoList: List<HourlyForecastDto?> = WeatherResponseProcessor.getHourlyForecastDtoList(
            context,
            multipleWeatherRestApiCallback, weatherProviderType, zoneId
        )
        if (!hourlyForecastDtoList.isEmpty()) {
            setDataViews(remoteViews, dailyPushNotificationDto.addressName, refreshDateTime, hourlyForecastDtoList)
            makeNotification(remoteViews, dailyPushNotificationDto.id)
        } else {
            makeFailedNotification(dailyPushNotificationDto.id, context.getString(R.string.msg_failed_update))
        }
    }

    override val requestWeatherDataTypeSet: Set<WeatherDataType>
        get() {
            val set: MutableSet<WeatherDataType> = HashSet()
            set.add(WeatherDataType.hourlyForecast)
            return set
        }
}