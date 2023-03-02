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
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.initWeatherSourceUniqueValues
import com.lifedawn.bestweather.ui.weathers.customview.DetailSingleTemperatureView
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.NinthWidgetProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NinthWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    private val hourGap = 3
    private val maxHoursCount = 13
    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.hourlyForecast)
            return set
        }

    override fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews? {
        val remoteViews = createBaseRemoteViews()
        onSuccessfulProcess(remoteViews!!)
        drawViews(
            remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempHourlyForecastDtoList(context, 24),
            null, parentWidth, parentHeight
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return NinthWidgetProvider::class.java
    }

    fun setDataViews(
        remoteViews: RemoteViews?, addressName: String, lastRefreshDateTime: String,
        hourlyForecastDtoList: List<HourlyForecastDto?>, onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, hourlyForecastDtoList, onDrawBitmapCallback, null, null)
    }

    private fun drawViews(
        remoteViews: RemoteViews?, addressName: String, lastRefreshDateTime: String,
        hourlyForecastDtoList: List<HourlyForecastDto?>, onDrawBitmapCallback: OnDrawBitmapCallback?, parentWidth: Int?,
        parentHeight: Int?
    ) {
        val rootLayout = RelativeLayout(context)
        val hourAndIconLinearLayout = LinearLayout(context)
        hourAndIconLinearLayout.id = R.id.hourAndIconView
        hourAndIconLinearLayout.orientation = LinearLayout.HORIZONTAL
        val hourAndIconCellLayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        hourAndIconCellLayoutParams.gravity = Gravity.CENTER
        hourAndIconCellLayoutParams.weight = 1f
        val layoutInflater = LayoutInflater.from(context)
        val headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime)
        headerView!!.id = R.id.header
        val tempList: MutableList<Int> = ArrayList()
        val degree = tempDegree
        val hour0Formatter = DateTimeFormatter.ofPattern("E\n0")
        val hourFormatter = DateTimeFormatter.ofPattern("E\nH")

        //강우, 강설 여부 확인
        var haveSnowVolume = false
        var haveRainVolume = false
        var count = 1
        var cell = 0
        while (cell < hourlyForecastDtoList.size) {
            if (count > maxHoursCount) {
                break
            }
            if (hourlyForecastDtoList[cell]!!.isHasSnow) {
                haveSnowVolume = true
            }
            if (hourlyForecastDtoList[cell]!!.isHasRain || hourlyForecastDtoList[cell]!!.isHasPrecipitation) {
                haveRainVolume = true
            }
            count++
            cell = cell + hourGap
        }
        count = 1
        cell = 0
        while (cell < hourlyForecastDtoList.size) {
            if (count++ > maxHoursCount) {
                break
            }
            val view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false)
            if (hourlyForecastDtoList[cell]!!.hours.hour == 0) {
                (view.findViewById<View>(R.id.dateTime) as TextView).text =
                    hourlyForecastDtoList[cell]!!.hours.format(hour0Formatter)
            } else {
                (view.findViewById<View>(R.id.dateTime) as TextView).text =
                    hourlyForecastDtoList[cell]!!.hours.format(hourFormatter)
            }
            (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                hourlyForecastDtoList[cell]!!.weatherIcon
            )
            view.findViewById<View>(R.id.temperature).visibility = View.GONE
            view.findViewById<View>(R.id.rightIcon).visibility = View.GONE
            view.findViewById<View>(R.id.popLayout).visibility = View.GONE
            view.findViewById<View>(R.id.rainVolumeLayout).visibility = View.GONE
            view.findViewById<View>(R.id.snowVolumeLayout).visibility = View.GONE
            tempList.add(hourlyForecastDtoList[cell]!!.temp.replace(degree, "").toInt())
            hourAndIconLinearLayout.addView(view, hourAndIconCellLayoutParams)
            cell = cell + hourGap
        }
        val headerViewLayoutParams = headerViewLayoutParams
        val hourAndIconRowLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tempRowLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        headerViewLayoutParams!!.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        hourAndIconRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.header)
        tempRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.hourAndIconView)
        tempRowLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        val detailSingleTemperatureView = DetailSingleTemperatureView(context, tempList)
        rootLayout.addView(headerView, headerViewLayoutParams)
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
        initWeatherSourceUniqueValues(weatherProviderType, false, context)
        val hourlyForecastDtoList = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(
            context, jsonObject,
            weatherProviderType, widgetDto.latitude, widgetDto.longitude, zoneId
        )
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime,
            hourlyForecastDtoList, null
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        onSuccessfulProcess(remoteViews!!)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    override fun setResultViews(appWidgetId: Int, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?, zoneId: ZoneId?) {
        this.zoneId = zoneId
        val hourlyForecastDtoList = WeatherResponseProcessor.getHourlyForecastDtoList(
            context, multipleWeatherRestApiCallback,
            WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet()), zoneId
        )
        val successful = !hourlyForecastDtoList.isEmpty()
        if (successful) {
            val zoneOffset = hourlyForecastDtoList[0]!!.hours.offset
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