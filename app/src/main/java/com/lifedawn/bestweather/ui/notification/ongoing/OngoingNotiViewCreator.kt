package com.lifedawn.bestweather.ui.notification.ongoing

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onErrorProcess
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onSuccessfulProcess
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasPrecipitationVolume
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherUtil.makeTempCompareToYesterdayText
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class OngoingNotiViewCreator constructor(private val context: Context, private val ongoingNotificationDto: OngoingNotificationDto?) {
    private val hourlyForecastCount: Int = 8
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm")
    private val tempUnit: ValueUnits
    private val ongoingNotificationHelper: OngoingNotificationHelper

    init {
        tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit()
        ongoingNotificationHelper = OngoingNotificationHelper(context)
    }

    fun createRemoteViews(temp: Boolean): Array<RemoteViews> {
        val collapsedRemoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.view_ongoing_notification_collapsed)
        val expandedRemoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.view_ongoing_notification_expanded)
        if (temp) {
            setHourlyForecastViews(expandedRemoteViews, WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount))
        } else {
            collapsedRemoteViews.setOnClickPendingIntent(R.id.refreshLayout, ongoingNotificationHelper.getRefreshPendingIntent())
            expandedRemoteViews.setOnClickPendingIntent(R.id.refreshLayout, ongoingNotificationHelper.getRefreshPendingIntent())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            expandedRemoteViews.setViewPadding(R.id.root_layout, 0, 0, 0, 0)
        }
        return arrayOf(collapsedRemoteViews, expandedRemoteViews)
    }

    fun setResultViews(
        collapsedRemoteViews: RemoteViews, expandedRemoteViews: RemoteViews,
        requestWeatherProviderType: WeatherProviderType?, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
        onRemoteViewsCallback: OnRemoteViewsCallback
    ) {
        var zoneOffset: ZoneOffset? = null
        setHeaderViews(
            collapsedRemoteViews,
            ongoingNotificationDto.getDisplayName(),
            multipleWeatherRestApiCallback.getRequestDateTime().toString()
        )
        setHeaderViews(
            expandedRemoteViews,
            ongoingNotificationDto.getDisplayName(),
            multipleWeatherRestApiCallback.getRequestDateTime().toString()
        )
        val zoneId: ZoneId = ZoneId.of(ongoingNotificationDto.getZoneId())
        var icon: Int = R.mipmap.ic_launcher_round
        var temperature: String? = null
        val currentConditionsDto: CurrentConditionsDto? = WeatherResponseProcessor.getCurrentConditionsDto(
            context, multipleWeatherRestApiCallback,
            (requestWeatherProviderType)!!, zoneId
        )
        val hourlyForecastDtoList: List<HourlyForecastDto?> = WeatherResponseProcessor.getHourlyForecastDtoList(
            context, multipleWeatherRestApiCallback,
            (requestWeatherProviderType)!!, zoneId
        )
        val successful: Boolean = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty()
        if (successful) {
            zoneOffset = currentConditionsDto!!.currentTime.getOffset()
            setCurrentConditionsViews(expandedRemoteViews, currentConditionsDto)
            setCollapsedCurrentConditionsViews(collapsedRemoteViews, currentConditionsDto)
            icon = currentConditionsDto.getWeatherIcon()
            temperature = currentConditionsDto.temp.replace(MyApplication.VALUE_UNIT_OBJ.getTempUnitText(), "°")
            val airQualityDto: AirQualityDto? = WeatherResponseProcessor.getAirQualityDto(multipleWeatherRestApiCallback, zoneOffset)
            if (airQualityDto.isSuccessful()) {
                setAirQualityViews(expandedRemoteViews, getGradeDescription(airQualityDto!!.aqi))
            } else {
                setAirQualityViews(expandedRemoteViews, context.getString(R.string.noData))
            }
            setHourlyForecastViews(expandedRemoteViews, hourlyForecastDtoList)
            onSuccessfulProcess(collapsedRemoteViews)
            onSuccessfulProcess(expandedRemoteViews)
        } else {
            setAirQualityViews(expandedRemoteViews, context.getString(R.string.noData))
            expandedRemoteViews.setOnClickPendingIntent(R.id.refreshBtn, ongoingNotificationHelper.getRefreshPendingIntent())
            collapsedRemoteViews.setOnClickPendingIntent(R.id.refreshBtn, ongoingNotificationHelper.getRefreshPendingIntent())
            onErrorProcess(collapsedRemoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA)
            onErrorProcess(expandedRemoteViews, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA)
        }
        onRemoteViewsCallback.onCreateFinished(collapsedRemoteViews, expandedRemoteViews, icon, temperature, true)
    }

    fun setHeaderViews(remoteViews: RemoteViews, addressName: String?, dateTime: String?) {
        remoteViews.setTextViewText(R.id.address, addressName)
        remoteViews.setTextViewText(R.id.refresh, ZonedDateTime.parse(dateTime).format(dateTimeFormatter))
    }

    fun setAirQualityViews(remoteViews: RemoteViews, value: String) {
        val airQuality: String = context.getString(R.string.air_quality) + ": " + value
        remoteViews.setTextViewText(R.id.airQuality, airQuality)
    }

    fun setCurrentConditionsViews(remoteViews: RemoteViews, currentConditionsDto: CurrentConditionsDto?) {
        remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon())
        var precipitation: String? = ""
        if (currentConditionsDto.isHasPrecipitationVolume()) {
            precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto!!.precipitationVolume
        } else {
            precipitation = context.getString(R.string.not_precipitation)
        }
        remoteViews.setTextViewText(R.id.precipitation, precipitation)
        remoteViews.setTextViewText(R.id.temperature, currentConditionsDto!!.temp)
        remoteViews.setTextViewText(
            R.id.feelsLikeTemp,
            String(context.getString(R.string.feelsLike) + ": " + currentConditionsDto.feelsLikeTemp)
        )
        if (currentConditionsDto.getYesterdayTemp() != null) {
            val yesterdayCompText: String = makeTempCompareToYesterdayText(
                currentConditionsDto.temp,
                currentConditionsDto.getYesterdayTemp(), tempUnit, context
            )
            remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText)
            remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE)
        }
    }

    fun setCollapsedCurrentConditionsViews(remoteViews: RemoteViews, currentConditionsDto: CurrentConditionsDto?) {
        remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon())
        remoteViews.setTextViewText(R.id.temperature, currentConditionsDto!!.temp)
        remoteViews.setTextViewText(
            R.id.feelsLikeTemp,
            String(context.getString(R.string.feelsLike) + ": " + currentConditionsDto.feelsLikeTemp)
        )
        if (currentConditionsDto.getYesterdayTemp() != null) {
            val yesterdayCompText: String = makeTempCompareToYesterdayText(
                currentConditionsDto.temp,
                currentConditionsDto.getYesterdayTemp(), tempUnit, context
            )
            remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText)
            remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE)
        }
    }

    fun setHourlyForecastViews(remoteViews: RemoteViews, hourlyForecastDtoList: List<HourlyForecastDto?>) {
        remoteViews.removeAllViews(R.id.hourlyForecast)
        val textColor: Int = ContextCompat.getColor(context, R.color.textColorInNotification)
        val hour0Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E 0")
        var hours: String? = null
        var haveRain: Boolean = false
        var haveSnow: Boolean = false
        for (i in 0 until hourlyForecastCount) {
            if (hourlyForecastDtoList.get(i)!!.isHasRain) {
                haveRain = true
            }
            if (hourlyForecastDtoList.get(i)!!.isHasSnow) {
                haveSnow = true
            }
        }
        val mm: String = "mm"
        val cm: String = "cm"
        for (i in 0 until hourlyForecastCount) {
            val childRemoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear)
            if (haveRain) {
                if (hourlyForecastDtoList.get(i)!!.isHasRain) {
                    childRemoteViews.setTextViewText(
                        R.id.rainVolume, hourlyForecastDtoList.get(i)!!.rainVolume
                            .replace(mm, "").replace(cm, "")
                    )
                    childRemoteViews.setTextColor(R.id.rainVolume, textColor)
                } else {
                    childRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.INVISIBLE)
                }
            } else {
                childRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE)
            }
            if (haveSnow) {
                if (hourlyForecastDtoList.get(i)!!.isHasSnow) {
                    childRemoteViews.setTextViewText(
                        R.id.snowVolume, hourlyForecastDtoList.get(i)!!.snowVolume
                            .replace(mm, "").replace(cm, "")
                    )
                    childRemoteViews.setTextColor(R.id.snowVolume, textColor)
                } else {
                    childRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.INVISIBLE)
                }
            } else {
                childRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE)
            }
            if (hourlyForecastDtoList.get(i)!!.hours.getHour() == 0) {
                hours = hourlyForecastDtoList.get(i)!!.hours.format(hour0Formatter)
            } else {
                hours = hourlyForecastDtoList.get(i)!!.hours.getHour().toString()
            }
            childRemoteViews.setTextViewText(R.id.dateTime, hours)
            childRemoteViews.setTextViewText(R.id.pop, hourlyForecastDtoList.get(i)!!.pop)
            childRemoteViews.setTextViewText(R.id.temperature, hourlyForecastDtoList.get(i)!!.temp)
            childRemoteViews.setImageViewResource(R.id.leftIcon, hourlyForecastDtoList.get(i)!!.weatherIcon)
            childRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE)
            childRemoteViews.setTextColor(R.id.dateTime, textColor)
            childRemoteViews.setTextColor(R.id.temperature, textColor)
            childRemoteViews.setTextColor(R.id.pop, textColor)
            remoteViews.addView(R.id.hourlyForecast, childRemoteViews)
        }
    }

    fun createFailedNotification(errorType: RemoteViewsUtil.ErrorType?): Array<RemoteViews> {
        val remoteViews: Array<RemoteViews> = createRemoteViews(false)
        remoteViews.get(0).setOnClickPendingIntent(R.id.refreshBtn, ongoingNotificationHelper.getRefreshPendingIntent())
        remoteViews.get(1).setOnClickPendingIntent(R.id.refreshBtn, ongoingNotificationHelper.getRefreshPendingIntent())
        onErrorProcess(remoteViews.get(0), context, (errorType)!!)
        onErrorProcess(remoteViews.get(1), context, (errorType)!!)
        return remoteViews
    }

    open interface OnRemoteViewsCallback {
        fun onCreateFinished(
            collapsedRemoteViews: RemoteViews?, expandedRemoteViews: RemoteViews?, icon: Int, temperature: String?,
            isFinished: Boolean
        )
    }
}