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
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasPrecipitationVolume
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.parseTextToAirQualityDto
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.initWeatherSourceUniqueValues
import com.lifedawn.bestweather.ui.weathers.customview.DetailSingleTemperatureView
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.SecondWidgetProvider
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SecondWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    private val cellCount = 7
    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.currentConditions)
            set.add(WeatherDataType.hourlyForecast)
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
            WeatherResponseProcessor.getTempHourlyForecastDtoList(context, cellCount),
            null,
            parentWidth,
            parentHeight
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return SecondWidgetProvider::class.java
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
        airQuality = if (airQualityDto.isSuccessful()) getGradeDescription(airQualityDto!!.aqi) else context.getString(R.string.noData)
        (view.findViewById<View>(R.id.airQuality) as TextView).text = airQuality
        return view
    }

    fun setDataViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?,
        hourlyForecastDtoList: List<HourlyForecastDto?>,
        onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(
            remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, hourlyForecastDtoList,
            onDrawBitmapCallback, null, null
        )
    }

    private fun drawViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?,
        hourlyForecastDtoList: List<HourlyForecastDto?>,
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
        val mm = "mm"
        val cm = "cm"
        var haveRain = false
        var haveSnow = false
        for (cell in 0 until cellCount) {
            if (hourlyForecastDtoList[cell]!!.isHasRain || hourlyForecastDtoList[cell]!!.isHasPrecipitation) {
                haveRain = true
            }
            if (hourlyForecastDtoList[cell]!!.isHasSnow) {
                haveSnow = true
            }
        }
        val tempList: MutableList<Int> = ArrayList()
        val degree = tempDegree
        val hour0Formatter = DateTimeFormatter.ofPattern("E 0")
        var rain: String? = null
        for (cell in 0 until cellCount) {
            val view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false)
            //hour, weatherIcon, pop
            if (hourlyForecastDtoList[cell]!!.hours.hour == 0) {
                (view.findViewById<View>(R.id.dateTime) as TextView).text = hourlyForecastDtoList[cell]!!.hours.format(hour0Formatter)
            } else {
                (view.findViewById<View>(R.id.dateTime) as TextView).text = hourlyForecastDtoList[cell]!!.hours.hour.toString()
            }
            (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                hourlyForecastDtoList[cell]!!.weatherIcon
            )
            tempList.add(hourlyForecastDtoList[cell]!!.temp.replace(degree, "").toInt())
            (view.findViewById<View>(R.id.pop) as TextView).text = hourlyForecastDtoList[cell]!!.pop
            if (haveRain) {
                if (hourlyForecastDtoList[cell]!!.isHasRain || hourlyForecastDtoList[cell]!!.isHasPrecipitation) {
                    rain =
                        if (hourlyForecastDtoList[cell]!!.isHasRain) hourlyForecastDtoList[cell]!!.rainVolume else hourlyForecastDtoList[cell]!!.precipitationVolume
                    (view.findViewById<View>(R.id.rainVolume) as TextView).text = rain.replace(
                        mm, ""
                    ).replace(cm, "")
                } else {
                    view.findViewById<View>(R.id.rainVolumeLayout).visibility = View.INVISIBLE
                }
            } else {
                view.findViewById<View>(R.id.rainVolumeLayout).visibility = View.GONE
            }
            if (haveSnow) {
                if (hourlyForecastDtoList[cell]!!.isHasSnow) {
                    (view.findViewById<View>(R.id.snowVolume) as TextView).text = hourlyForecastDtoList[cell]!!.snowVolume.replace(
                        mm, ""
                    ).replace(cm, "")
                } else {
                    view.findViewById<View>(R.id.snowVolumeLayout).visibility = View.INVISIBLE
                }
            } else {
                view.findViewById<View>(R.id.snowVolumeLayout).visibility = View.GONE
            }
            view.findViewById<View>(R.id.temperature).visibility = View.GONE
            view.findViewById<View>(R.id.rightIcon).visibility = View.GONE
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
        val detailSingleTemperatureView = DetailSingleTemperatureView(context, tempList)
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
        val remoteViews = createRemoteViews()
        zoneId = ZoneId.of(widgetDto.timeZoneId)
        val jsonObject = JsonParser.parseString(widgetDto.responseText) as JsonObject
        val airQualityDto = parseTextToAirQualityDto(jsonObject)
        val currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(
            context, jsonObject,
            weatherProviderType, widgetDto.latitude, widgetDto.longitude, zoneId
        )
        val hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(
            context, jsonObject,
            weatherProviderType, widgetDto.latitude, widgetDto.longitude, zoneId
        )
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime, airQualityDto, currentConditionsDto,
            hourlyForecastDtoList, null
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        onSuccessfulProcess(remoteViews!!)
        appWidgetManager.updateAppWidget(
            appWidgetId,
            remoteViews
        )
    }

    override fun setResultViews(appWidgetId: Int, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?, zoneId: ZoneId?) {
        this.zoneId = zoneId
        var zoneOffset: ZoneOffset? = null
        val weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet())
        val currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(
            context, multipleWeatherRestApiCallback,
            weatherProviderType, zoneId
        )
        val hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(
            context, multipleWeatherRestApiCallback,
            weatherProviderType, zoneId
        )
        val successful = currentConditionsDto != null && !hourlyForecastDtoList.isEmpty()
        if (successful) {
            zoneOffset = currentConditionsDto!!.currentTime.offset
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