package com.lifedawn.bestweather.ui.widget.creator

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onSuccessfulProcess
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasPrecipitationVolume
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.parseTextToAirQualityDto
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.initWeatherSourceUniqueValues
import com.lifedawn.bestweather.ui.weathers.customview.DetailDoubleTemperatureViewForRemoteViews
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.FourthWidgetProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FourthWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    private val cellCount = 5
    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.currentConditions)
            set.add(WeatherDataType.dailyForecast)
            set.add(WeatherDataType.airQuality)
            return set
        }

    override fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews? {
        val remoteViews = createBaseRemoteViews()
        onSuccessfulProcess(remoteViews!!)
        drawViews(
            remoteViews,
            context.getString(R.string.address_name),
            ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempAirQualityDto(),
            WeatherResponseProcessor.getTempCurrentConditionsDto(context),
            WeatherResponseProcessor.getTempDailyForecastDtoList(context, cellCount),
            null,
            parentWidth,
            parentHeight
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return FourthWidgetProvider::class.java
    }

    fun makeCurrentConditionsViews(
        layoutInflater: LayoutInflater, currentConditionsDto: CurrentConditionsDto?,
        airQualityDto: AirQualityDto?
    ): View {
        val view = layoutInflater.inflate(R.layout.view_current_conditions_for_simple_widget, null, false)
        (view.findViewById<View>(R.id.temperature) as TextView).text = currentConditionsDto!!.temp.replace(tempDegree, "°")
        (view.findViewById<View>(R.id.weatherIcon) as ImageView).setImageResource(currentConditionsDto.getWeatherIcon())
        var precipitation = ""
        if (currentConditionsDto.isHasPrecipitationVolume()) {
            precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.precipitationVolume
        } else {
            precipitation = context.getString(R.string.not_precipitation)
        }
        (view.findViewById<View>(R.id.precipitation) as TextView).text = precipitation
        var airQuality: String? = null
        airQuality = if (airQualityDto.isSuccessful()) {
            getGradeDescription(airQualityDto!!.aqi)
        } else {
            context.getString(R.string.noData)
        }
        (view.findViewById<View>(R.id.airQuality) as TextView).text = airQuality
        return view
    }

    fun setDataViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?,
        dailyForecastDtoList: List<DailyForecastDto?>,
        onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(
            remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, dailyForecastDtoList,
            onDrawBitmapCallback, null, null
        )
    }

    private fun drawViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?,
        dailyForecastDtoList: List<DailyForecastDto?>,
        onDrawBitmapCallback: OnDrawBitmapCallback?,
        parentWidth: Int?,
        parentHeight: Int?
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime)
        headerView!!.id = R.id.header
        val currentConditionsView = makeCurrentConditionsViews(layoutInflater, currentConditionsDto, airQualityDto)
        currentConditionsView.id = R.id.currentConditions
        val hourAndIconLinearLayout = LinearLayout(context)
        hourAndIconLinearLayout.id = R.id.hourAndIconView
        hourAndIconLinearLayout.orientation = LinearLayout.HORIZONTAL
        val hourAndIconCellLayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        hourAndIconCellLayoutParams.gravity = Gravity.CENTER
        hourAndIconCellLayoutParams.weight = 1f
        val minTempList: MutableList<Int> = ArrayList()
        val maxTempList: MutableList<Int> = ArrayList()
        val dateFormatter = DateTimeFormatter.ofPattern("E")
        var pop: String? = null
        var haveRain = false
        var haveSnow = false
        for (cell in 0 until cellCount) {
            if (dailyForecastDtoList[cell]!!.valuesList.size == 1) {
                if (dailyForecastDtoList[cell]!!.valuesList[0].isHasRainVolume) {
                    haveRain = true
                }
                if (dailyForecastDtoList[cell]!!.valuesList[0].isHasSnowVolume) {
                    haveSnow = true
                }
            } else if (dailyForecastDtoList[cell]!!.valuesList.size == 2) {
                if (dailyForecastDtoList[cell]!!.valuesList[0].isHasRainVolume ||
                    dailyForecastDtoList[cell]!!.valuesList[1].isHasRainVolume
                ) {
                    haveRain = true
                }
                if (dailyForecastDtoList[cell]!!.valuesList[0].isHasSnowVolume ||
                    dailyForecastDtoList[cell]!!.valuesList[1].isHasSnowVolume
                ) {
                    haveSnow = true
                }
            } else if (dailyForecastDtoList[cell]!!.valuesList.size == 4) {
                if (dailyForecastDtoList[cell]!!.valuesList[0].isHasPrecipitationVolume ||
                    dailyForecastDtoList[cell]!!.valuesList[1].isHasPrecipitationVolume ||
                    dailyForecastDtoList[cell]!!.valuesList[2].isHasPrecipitationVolume ||
                    dailyForecastDtoList[cell]!!.valuesList[3].isHasPrecipitationVolume
                ) {
                    haveRain = true
                }
            }
        }
        val mm = "mm"
        val cm = "cm"
        var rainVolume = 0.0
        var snowVolume = 0.0
        for (cell in 0 until cellCount) {
            rainVolume = 0.0
            snowVolume = 0.0
            val view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false)
            //hour, weatherIcon
            (view.findViewById<View>(R.id.dateTime) as TextView).text = dailyForecastDtoList[cell]!!.date.format(dateFormatter)
            if (dailyForecastDtoList[cell]!!.valuesList.size == 1) {
                (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[0].weatherIcon
                )
                pop = dailyForecastDtoList[cell]!!.valuesList[0].pop
                view.findViewById<View>(R.id.rightIcon).visibility = View.GONE
            } else if (dailyForecastDtoList[cell]!!.valuesList.size == 2) {
                (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[0].weatherIcon
                )
                (view.findViewById<View>(R.id.rightIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[1].weatherIcon
                )
                pop = dailyForecastDtoList[cell]!!.valuesList[0].pop + "/" + dailyForecastDtoList[cell]!!.valuesList[1].pop
            } else if (dailyForecastDtoList[cell]!!.valuesList.size == 4) {
                (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[1].weatherIcon
                )
                (view.findViewById<View>(R.id.rightIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[2].weatherIcon
                )
                pop = "-/-"
            }
            (view.findViewById<View>(R.id.pop) as TextView).text = pop
            if (haveRain) {
                if (dailyForecastDtoList[cell]!!.valuesList.size == 1) {
                    if (dailyForecastDtoList[cell]!!.valuesList[0].isHasRainVolume) {
                        rainVolume += dailyForecastDtoList[cell]!!.valuesList[0].rainVolume.replace(mm, "")
                            .replace(cm, "").toDouble()
                    }
                } else if (dailyForecastDtoList[cell]!!.valuesList.size == 2) {
                    if (dailyForecastDtoList[cell]!!.valuesList[0].isHasRainVolume) {
                        rainVolume += dailyForecastDtoList[cell]!!.valuesList[0].rainVolume.replace(mm, "")
                            .replace(cm, "").toDouble()
                    }
                    if (dailyForecastDtoList[cell]!!.valuesList[1].isHasRainVolume) {
                        rainVolume += dailyForecastDtoList[cell]!!.valuesList[1].rainVolume.replace(mm, "")
                            .replace(cm, "").toDouble()
                    }
                } else if (dailyForecastDtoList[cell]!!.valuesList.size == 4) {
                    if (dailyForecastDtoList[cell]!!.valuesList[0].isHasPrecipitationVolume ||
                        dailyForecastDtoList[cell]!!.valuesList[1].isHasPrecipitationVolume
                    ) {
                        rainVolume = rainVolume + dailyForecastDtoList[cell]!!.valuesList[0].precipitationVolume.replace(
                            mm, ""
                        ).toDouble() + dailyForecastDtoList[cell]!!.valuesList[1].precipitationVolume.replace(
                            mm, ""
                        ).toDouble()
                    }
                    if (dailyForecastDtoList[cell]!!.valuesList[2].isHasPrecipitationVolume ||
                        dailyForecastDtoList[cell]!!.valuesList[3].isHasPrecipitationVolume
                    ) {
                        rainVolume = rainVolume + dailyForecastDtoList[cell]!!.valuesList[2].precipitationVolume.replace(
                            mm, ""
                        ).toDouble() + dailyForecastDtoList[cell]!!.valuesList[3].precipitationVolume.replace(
                            mm, ""
                        ).toDouble()
                    }
                }
                if (rainVolume == 0.0) {
                    view.findViewById<View>(R.id.rainVolumeLayout).visibility = View.INVISIBLE
                } else {
                    (view.findViewById<View>(R.id.rainVolume) as TextView).text = String.format(Locale.getDefault(), "%.1f", rainVolume)
                }
            } else {
                view.findViewById<View>(R.id.rainVolumeLayout).visibility = View.GONE
            }
            if (haveSnow) {
                if (dailyForecastDtoList[cell]!!.valuesList.size == 1) {
                    if (dailyForecastDtoList[cell]!!.valuesList[0].isHasSnowVolume) {
                        snowVolume += dailyForecastDtoList[cell]!!.valuesList[0].snowVolume.replace(mm, "")
                            .replace(cm, "").toDouble()
                    }
                } else {
                    if (dailyForecastDtoList[cell]!!.valuesList[0].isHasSnowVolume) {
                        snowVolume += dailyForecastDtoList[cell]!!.valuesList[0].snowVolume.replace(mm, "")
                            .replace(cm, "").toDouble()
                    }
                    if (dailyForecastDtoList[cell]!!.valuesList[1].isHasSnowVolume) {
                        snowVolume += dailyForecastDtoList[cell]!!.valuesList[1].snowVolume.replace(mm, "")
                            .replace(cm, "").toDouble()
                    }
                }
                if (snowVolume == 0.0) {
                    view.findViewById<View>(R.id.snowVolumeLayout).visibility = View.INVISIBLE
                } else {
                    (view.findViewById<View>(R.id.snowVolume) as TextView).text = String.format(Locale.getDefault(), "%.1f", snowVolume)
                }
            } else {
                view.findViewById<View>(R.id.snowVolumeLayout).visibility = View.GONE
            }
            view.findViewById<View>(R.id.temperature).visibility = View.GONE
            minTempList.add(dailyForecastDtoList[cell]!!.minTemp.replace(tempDegree, "").toInt())
            maxTempList.add(dailyForecastDtoList[cell]!!.maxTemp.replace(tempDegree, "").toInt())
            hourAndIconLinearLayout.addView(view, hourAndIconCellLayoutParams)
        }
        val headerViewLayoutParams = headerViewLayoutParams
        val currentConditionsViewLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val hourAndIconRowLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tempRowLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        headerViewLayoutParams!!.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        currentConditionsViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        currentConditionsViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header)
        hourAndIconRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.header)
        hourAndIconRowLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.currentConditions)
        tempRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.hourAndIconView)
        tempRowLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.currentConditions)
        tempRowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        val detailSingleTemperatureView = DetailDoubleTemperatureViewForRemoteViews(
            context,
            minTempList, maxTempList
        )
        val rootLayout = RelativeLayout(context)
        rootLayout.addView(headerView, headerViewLayoutParams)
        rootLayout.addView(currentConditionsView, currentConditionsViewLayoutParams)
        rootLayout.addView(hourAndIconLinearLayout, hourAndIconRowLayoutParams)
        rootLayout.addView(detailSingleTemperatureView, tempRowLayoutParams)
        drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews!!, parentWidth, parentHeight)
    }

    override fun setDisplayClock(displayClock: Boolean) {
        widgetDto.isDisplayClock = displayClock
    }

    override fun setDataViewsOfSavedData() {
        var weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet())
        if (widgetDto.isTopPriorityKma && widgetDto.countryCode == "KR") {
            weatherProviderType = WeatherProviderType.KMA_WEB
        }
        initWeatherSourceUniqueValues(weatherProviderType, true, context)
        zoneId = ZoneId.of(widgetDto.timeZoneId)
        val remoteViews = createRemoteViews()
        val jsonObject = JsonParser.parseString(widgetDto.responseText) as JsonObject
        val airQualityDto = parseTextToAirQualityDto(jsonObject)
        val currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(
            context, jsonObject,
            weatherProviderType, widgetDto.latitude, widgetDto.longitude, zoneId
        )
        val dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(
            context, jsonObject,
            weatherProviderType, zoneId
        )
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime, airQualityDto, currentConditionsDto,
            dailyForecastDtoList, null
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        onSuccessfulProcess(remoteViews!!)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    override fun setResultViews(appWidgetId: Int, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?, zoneId: ZoneId?) {
        this.zoneId = zoneId
        val weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet())
        val currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(
            context, multipleWeatherRestApiCallback,
            weatherProviderType, zoneId
        )
        val dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(
            context, multipleWeatherRestApiCallback,
            weatherProviderType, zoneId
        )
        val successful = currentConditionsDto != null && !dailyForecastDtoList.isEmpty()
        if (successful) {
            val zoneOffset = currentConditionsDto!!.currentTime.offset
            widgetDto.timeZoneId = zoneId!!.id
            widgetDto.lastRefreshDateTime = multipleWeatherRestApiCallback.getRequestDateTime().toString()
            makeResponseTextToJson(
                multipleWeatherRestApiCallback,
                requestWeatherDataTypeSet,
                widgetDto.getWeatherProviderTypeSet(),
                widgetDto,
                zoneOffset
            )
        }
        widgetDto.isLoadSuccessful = successful
        super.setResultViews(appWidgetId, multipleWeatherRestApiCallback, zoneId)
    }
}