package com.lifedawn.bestweather.ui.notification.daily.viewcreator

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasPrecipitationVolume
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherUtil.makeTempCompareToYesterdayText
import java.time.ZoneOffset
import java.time.ZonedDateTime

class SecondDailyNotificationViewCreator constructor(context: Context) : AbstractDailyNotiViewCreator(context) {
    private val cellCount: Int = 6
    public override fun createRemoteViews(needTempData: Boolean): RemoteViews {
        val remoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.second_daily_noti_view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            remoteViews.setViewPadding(R.id.root_layout, 0, 0, 0, 0)
        }
        if (needTempData) {
            setTempDataViews(remoteViews)
        }
        return remoteViews
    }

    fun setDataViews(
        remoteViews: RemoteViews,
        addressName: String?,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto)
    }

    public override fun setTempDataViews(remoteViews: RemoteViews) {
        drawViews(
            remoteViews,
            context.getString(R.string.address_name),
            ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempAirQualityDto(),
            WeatherResponseProcessor.getTempCurrentConditionsDto(context)
        )
    }

    private fun drawViews(
        remoteViews: RemoteViews, addressName: String?, lastRefreshDateTime: String, airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?
    ) {
        var precipitation: String? = ""
        if (currentConditionsDto.isHasPrecipitationVolume()) {
            precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto!!.precipitationVolume
        } else {
            precipitation = context.getString(R.string.not_precipitation)
        }
        val humidity: String = context.getString(R.string.humidity) + ": " + currentConditionsDto!!.humidity
        remoteViews.setTextViewText(R.id.temperature, currentConditionsDto.temp)
        remoteViews.setTextViewText(R.id.humidity, humidity)
        remoteViews.setTextViewText(R.id.precipitation, precipitation)
        remoteViews.setImageViewResource(R.id.weatherIcon, currentConditionsDto.getWeatherIcon())
        if (currentConditionsDto.getYesterdayTemp() != null) {
            val tempUnit: ValueUnits = ValueUnits.valueOf(
                (PreferenceManager.getDefaultSharedPreferences(context).getString(
                    context.getString(
                        R.string.pref_key_unit_temp
                    ), ValueUnits.celsius.name
                ))!!
            )
            val yesterdayCompText: String = makeTempCompareToYesterdayText(
                currentConditionsDto.temp,
                currentConditionsDto.getYesterdayTemp(), tempUnit, context
            )
            remoteViews.setTextViewText(R.id.yesterdayTemperature, yesterdayCompText)
        } else {
            remoteViews.setViewVisibility(R.id.yesterdayTemperature, View.GONE)
        }
        if (currentConditionsDto.windDirection != null) {
            remoteViews.setTextViewText(R.id.windDirection, currentConditionsDto.windDirection)
            remoteViews.setTextViewText(R.id.windSpeed, currentConditionsDto.windSpeed)
            remoteViews.setTextViewText(R.id.windStrength, currentConditionsDto.windStrength)
        } else {
            remoteViews.setViewVisibility(R.id.windDirection, View.GONE)
            remoteViews.setViewVisibility(R.id.windSpeed, View.GONE)
            remoteViews.setTextViewText(R.id.windStrength, context.getString(R.string.noWindData))
        }
        if (currentConditionsDto.feelsLikeTemp != null) {
            val feelsLikeTemp: String = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.feelsLikeTemp
            remoteViews.setTextViewText(R.id.feelsLikeTemp, feelsLikeTemp)
        } else {
            remoteViews.setViewVisibility(R.id.feelsLikeTemp, View.GONE)
        }
        remoteViews.setTextViewText(
            R.id.airQuality, (context.getString(R.string.air_quality) + ": " +
                    getGradeDescription(airQualityDto!!.aqi))
        )
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
        val currentConditionsDto: CurrentConditionsDto? = WeatherResponseProcessor.getCurrentConditionsDto(
            context, multipleWeatherRestApiCallback,
            weatherProviderType, zoneId
        )
        val successful: Boolean = currentConditionsDto != null
        if (successful) {
            val zoneOffset: ZoneOffset = currentConditionsDto!!.currentTime.getOffset()
            val airQualityDto: AirQualityDto? = WeatherResponseProcessor.getAirQualityDto(
                multipleWeatherRestApiCallback,
                zoneOffset
            )
            setDataViews(remoteViews, dailyPushNotificationDto.addressName, refreshDateTime, airQualityDto, currentConditionsDto)
            makeNotification(remoteViews, dailyPushNotificationDto.id)
        } else {
            makeFailedNotification(dailyPushNotificationDto.id, context.getString(R.string.msg_failed_update))
        }
    }

    override val requestWeatherDataTypeSet: Set<WeatherDataType>
        get() {
            val set: MutableSet<WeatherDataType> = HashSet()
            set.add(WeatherDataType.currentConditions)
            set.add(WeatherDataType.airQuality)
            return set
        }
}