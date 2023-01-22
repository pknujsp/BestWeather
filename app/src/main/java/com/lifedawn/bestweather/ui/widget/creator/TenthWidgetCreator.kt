package com.lifedawn.bestweather.ui.widget.creator

import android.appwidget.AppWidgetManager
import android.content.Context
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
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.initWeatherSourceUniqueValues
import com.lifedawn.bestweather.ui.weathers.view.DetailDoubleTemperatureViewForRemoteViews
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.TenthWidgetProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TenthWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    private val DAY_LENGTH = 8
    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.dailyForecast)
            return set
        }

    override fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews? {
        val remoteViews = createBaseRemoteViews()
        onSuccessfulProcess(remoteViews!!)
        drawViews(
            remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempDailyForecastDtoList(context, DAY_LENGTH), null, parentWidth, parentHeight
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return TenthWidgetProvider::class.java
    }

    fun setDataViews(
        remoteViews: RemoteViews?, addressName: String, lastRefreshDateTime: String,
        dailyForecastDtoList: List<DailyForecastDto?>, onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, dailyForecastDtoList, onDrawBitmapCallback, null, null)
    }

    private fun drawViews(
        remoteViews: RemoteViews?, addressName: String, lastRefreshDateTime: String,
        dailyForecastDtoList: List<DailyForecastDto?>, onDrawBitmapCallback: OnDrawBitmapCallback?, parentWidth: Int?,
        parentHeight: Int?
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime)
        headerView!!.id = R.id.header
        val hourAndIconLinearLayout = LinearLayout(context)
        hourAndIconLinearLayout.id = R.id.hourAndIconView
        hourAndIconLinearLayout.orientation = LinearLayout.HORIZONTAL
        val hourAndIconCellLayoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        hourAndIconCellLayoutParams.weight = 1f
        val minTempList: MutableList<Int> = ArrayList()
        val maxTempList: MutableList<Int> = ArrayList()
        val dateFormatter = DateTimeFormatter.ofPattern("M.d\nE")
        for (cell in 0 until DAY_LENGTH) {
            if (!dailyForecastDtoList[cell]!!.isAvailable_toMakeMinMaxTemp) break
            val view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false)
            (view.findViewById<View>(R.id.dateTime) as TextView).text =
                dailyForecastDtoList[cell]!!.date.format(dateFormatter)
            if (dailyForecastDtoList[cell]!!.valuesList.size == 1) {
                (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[0].weatherIcon
                )
                view.findViewById<View>(R.id.rightIcon).visibility = View.GONE
            } else if (dailyForecastDtoList[cell]!!.valuesList.size == 2) {
                (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[0].weatherIcon
                )
                (view.findViewById<View>(R.id.rightIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[1].weatherIcon
                )
            } else if (dailyForecastDtoList[cell]!!.valuesList.size == 4) {
                (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[1].weatherIcon
                )
                (view.findViewById<View>(R.id.rightIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[cell]!!.valuesList[2].weatherIcon
                )
            }
            view.findViewById<View>(R.id.temperature).visibility = View.GONE
            view.findViewById<View>(R.id.popLayout).visibility = View.GONE
            view.findViewById<View>(R.id.rainVolumeLayout).visibility = View.GONE
            view.findViewById<View>(R.id.snowVolumeLayout).visibility = View.GONE
            minTempList.add(dailyForecastDtoList[cell]!!.minTemp.replace(tempDegree, "").toInt())
            maxTempList.add(dailyForecastDtoList[cell]!!.maxTemp.replace(tempDegree, "").toInt())
            hourAndIconLinearLayout.addView(view, hourAndIconCellLayoutParams)
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
        val dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(
            context, jsonObject,
            weatherProviderType, zoneId
        )
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime,
            dailyForecastDtoList, null
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
        val dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(
            context, multipleWeatherRestApiCallback,
            WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet()), zoneId
        )
        val successful = !dailyForecastDtoList.isEmpty()
        if (successful) {
            val zoneOffset = dailyForecastDtoList[0]!!.date.offset
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