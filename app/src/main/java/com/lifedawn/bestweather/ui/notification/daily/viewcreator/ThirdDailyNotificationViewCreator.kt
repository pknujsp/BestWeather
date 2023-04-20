package com.lifedawn.bestweather.ui.notification.daily.viewcreator

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ThirdDailyNotificationViewCreator constructor(context: Context) : AbstractDailyNotiViewCreator(context) {
    private val cellCount: Int = 6
    public override fun createRemoteViews(needTempData: Boolean): RemoteViews {
        val remoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.third_daily_noti_view)
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
        dailyForecastDtoList: List<DailyForecastDto?>
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, dailyForecastDtoList)
    }

    public override fun setTempDataViews(remoteViews: RemoteViews) {
        drawViews(
            remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempDailyForecastDtoList(context, cellCount)
        )
    }

    private fun drawViews(
        remoteViews: RemoteViews, addressName: String?, lastRefreshDateTime: String,
        dailyForecastDtoList: List<DailyForecastDto?>
    ) {
        val mm: String = "mm"
        val cm: String = "cm"
        var haveRain: Boolean = false
        var haveSnow: Boolean = false
        for (cell in 0 until cellCount) {
            if (dailyForecastDtoList.get(cell)!!.valuesList.size == 1) {
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasRainVolume) {
                    haveRain = true
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasSnowVolume) {
                    haveSnow = true
                }
            } else if (dailyForecastDtoList.get(cell)!!.valuesList.size == 2) {
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasRainVolume ||
                    dailyForecastDtoList.get(cell)!!.valuesList.get(1).isHasRainVolume
                ) {
                    haveRain = true
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasSnowVolume ||
                    dailyForecastDtoList.get(cell)!!.valuesList.get(1).isHasSnowVolume
                ) {
                    haveSnow = true
                }
            } else if (dailyForecastDtoList.get(cell)!!.valuesList.size == 4) {
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(1).isHasPrecipitationVolume ||
                    dailyForecastDtoList.get(cell)!!.valuesList.get(2).isHasPrecipitationVolume
                ) {
                    haveRain = true
                }
            }
        }
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E")
        var pop: String? = null
        var rainVolume: Float = 0f
        var snowVolume: Float = 0f
        for (cell in 0 until cellCount) {
            val forecastRemoteViews: RemoteViews = RemoteViews(context.getPackageName(), R.layout.view_forecast_item_in_linear)
            forecastRemoteViews.setTextViewText(R.id.dateTime, dailyForecastDtoList.get(cell)!!.date.format(dateTimeFormatter))
            rainVolume = 0f
            snowVolume = 0f
            if (dailyForecastDtoList.get(cell)!!.valuesList.size == 1) {
                forecastRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell)!!.valuesList.get(0).weatherIcon)
                forecastRemoteViews.setViewVisibility(R.id.rightIcon, View.GONE)
                pop = dailyForecastDtoList.get(cell)!!.valuesList.get(0).pop
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasRainVolume) {
                    (rainVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(0).rainVolume.replace(cm, "").replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasSnowVolume) {
                    (snowVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(0).snowVolume.replace(cm, "").replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
            } else if (dailyForecastDtoList.get(cell)!!.valuesList.size == 2) {
                forecastRemoteViews.setImageViewResource(R.id.leftIcon, dailyForecastDtoList.get(cell)!!.valuesList.get(0).weatherIcon)
                forecastRemoteViews.setImageViewResource(R.id.rightIcon, dailyForecastDtoList.get(cell)!!.valuesList.get(1).weatherIcon)
                pop = dailyForecastDtoList.get(cell)!!.valuesList.get(0).pop + "/" + dailyForecastDtoList.get(cell)!!.valuesList.get(1).pop
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasRainVolume) {
                    (rainVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(0).rainVolume.replace(cm, "").replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(1).isHasRainVolume) {
                    (rainVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(1).rainVolume.replace(cm, "").replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasSnowVolume) {
                    (snowVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(0).snowVolume.replace(cm, "").replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(1).isHasSnowVolume) {
                    (snowVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(1).snowVolume.replace(cm, "").replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
            } else if (dailyForecastDtoList.get(cell)!!.valuesList.size == 4) {
                forecastRemoteViews.setImageViewResource(
                    R.id.leftIcon,
                    dailyForecastDtoList.get(cell)!!.valuesList.get(1).weatherIcon
                )
                forecastRemoteViews.setImageViewResource(
                    R.id.rightIcon,
                    dailyForecastDtoList.get(cell)!!.valuesList.get(2).weatherIcon
                )
                pop = "-"
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(0).isHasPrecipitationVolume) {
                    (rainVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(0).precipitationVolume.replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(1).isHasPrecipitationVolume) {
                    (rainVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(1).precipitationVolume.replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(2).isHasPrecipitationVolume) {
                    (rainVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(2).precipitationVolume.replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
                if (dailyForecastDtoList.get(cell)!!.valuesList.get(3).isHasPrecipitationVolume) {
                    (rainVolume += dailyForecastDtoList.get(cell)!!.valuesList.get(3).precipitationVolume.replace(
                        mm,
                        ""
                    ).toDouble()).toFloat()
                }
            }
            if (haveRain) {
                if (rainVolume > 0f) {
                    forecastRemoteViews.setTextViewText(R.id.rainVolume, String.format(Locale.getDefault(), "%.1f", rainVolume))
                } else {
                    forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.INVISIBLE)
                }
            } else {
                forecastRemoteViews.setViewVisibility(R.id.rainVolumeLayout, View.GONE)
            }
            if (haveSnow) {
                if (snowVolume > 0f) {
                    forecastRemoteViews.setTextViewText(R.id.snowVolume, String.format(Locale.getDefault(), "%.1f", snowVolume))
                } else {
                    forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.INVISIBLE)
                }
            } else {
                forecastRemoteViews.setViewVisibility(R.id.snowVolumeLayout, View.GONE)
            }
            forecastRemoteViews.setTextViewText(
                R.id.temperature, (dailyForecastDtoList.get(cell)!!.minTemp + "/" +
                        dailyForecastDtoList.get(cell)!!.maxTemp)
            )
            forecastRemoteViews.setTextViewText(R.id.pop, pop)
            remoteViews.addView(R.id.dailyForecast, forecastRemoteViews)
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
        val dailyForecastDtoList: List<DailyForecastDto?> = WeatherResponseProcessor.getDailyForecastDtoList(
            context, multipleWeatherRestApiCallback, weatherProviderType, zoneId
        )
        val successful: Boolean = !dailyForecastDtoList.isEmpty()
        if (successful) {
            setDataViews(remoteViews, dailyPushNotificationDto.addressName, refreshDateTime, dailyForecastDtoList)
            makeNotification(remoteViews, dailyPushNotificationDto.id)
        } else {
            makeFailedNotification(dailyPushNotificationDto.id, context.getString(R.string.msg_failed_update))
        }
    }

    override val requestWeatherDataTypeSet: Set<WeatherDataType>
        get() {
            val set: MutableSet<WeatherDataType> = HashSet()
            set.add(WeatherDataType.dailyForecast)
            return set
        }
}