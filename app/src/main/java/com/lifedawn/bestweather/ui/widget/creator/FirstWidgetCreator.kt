package com.lifedawn.bestweather.ui.widget.creator

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.RemoteViews
import android.widget.TextView
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onSuccessfulProcess
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasPrecipitationVolume
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.parseTextToAirQualityDto
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.initWeatherSourceUniqueValues
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherUtil.makeTempCompareToYesterdayText
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.FirstWidgetProvider
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class FirstWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.currentConditions)
            set.add(WeatherDataType.airQuality)
            return set
        }

    override fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews? {
        val remoteViews = createBaseRemoteViews()
        onSuccessfulProcess(remoteViews!!)
        drawViews(
            remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempAirQualityDto(), WeatherResponseProcessor.getTempCurrentConditionsDto(context),
            null, parentWidth, parentHeight
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return FirstWidgetProvider::class.java
    }

    fun setClockTimeZone(remoteViews: RemoteViews?) {
        val zoneId: ZoneId
        zoneId = if (widgetDto.timeZoneId == null) {
            ZoneId.systemDefault()
        } else {
            if (widgetDto.isDisplayLocalClock) ZoneId.of(widgetDto.timeZoneId) else ZoneId.systemDefault()
        }

        //remoteViews.setString(R.id.dateClock, "setTimeZone", zoneId.getId());
        //remoteViews.setString(R.id.timeClock, "setTimeZone", zoneId.getId());
    }

    fun makeCurrentConditionsViews(
        layoutInflater: LayoutInflater, currentConditionsDto: CurrentConditionsDto?,
        airQualityDto: AirQualityDto?
    ): View {
        val view = layoutInflater.inflate(R.layout.view_first_widget, null, false)
        (view.findViewById<View>(R.id.temperature) as TextView).text = currentConditionsDto!!.temp
        (view.findViewById<View>(R.id.weatherIcon) as ImageView).setImageResource(currentConditionsDto.getWeatherIcon())
        var airQuality = context.getString(R.string.air_quality) + ": "
        airQuality += if (airQualityDto.isSuccessful()) {
            getGradeDescription(airQualityDto!!.aqi)
        } else {
            context.getString(R.string.noData)
        }
        (view.findViewById<View>(R.id.airQuality) as TextView).text = airQuality
        var precipitation = ""
        if (currentConditionsDto.isHasPrecipitationVolume()) {
            precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.precipitationVolume
        } else {
            precipitation = context.getString(R.string.not_precipitation)
        }
        val feelsLikeTemp = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.feelsLikeTemp
        (view.findViewById<View>(R.id.feelsLikeTemp) as TextView).text = feelsLikeTemp
        (view.findViewById<View>(R.id.precipitation) as TextView).text = precipitation
        val humidity = context.getString(R.string.humidity) + ": " + currentConditionsDto.humidity
        (view.findViewById<View>(R.id.humidity) as TextView).text = humidity
        (view.findViewById<View>(R.id.windDirection) as TextView).text =
            currentConditionsDto.windDirection
        (view.findViewById<View>(R.id.windSpeed) as TextView).text =
            currentConditionsDto.windSpeed
        (view.findViewById<View>(R.id.windStrength) as TextView).text = currentConditionsDto.windStrength
        view.findViewById<View>(R.id.windDirectionArrow).rotation = (currentConditionsDto.windDirectionDegree + 180).toFloat()
        if (currentConditionsDto.getYesterdayTemp() != null) {
            val yesterdayCompText = makeTempCompareToYesterdayText(
                currentConditionsDto.temp,
                currentConditionsDto.getYesterdayTemp(), tempUnit, context
            )
            (view.findViewById<View>(R.id.yesterdayTemperature) as TextView).text = yesterdayCompText
        } else {
            view.findViewById<View>(R.id.yesterdayTemperature).visibility = View.GONE
        }
        return view
    }

    fun setDataViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?,
        onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, onDrawBitmapCallback, null, null)
    }

    private fun drawViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?,
        onDrawBitmapCallback: OnDrawBitmapCallback?,
        parentWidth: Int?,
        parentHeight: Int?
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime)
        headerView!!.id = R.id.header
        val currentConditionsView = makeCurrentConditionsViews(layoutInflater, currentConditionsDto, airQualityDto)
        currentConditionsView.id = R.id.currentConditions
        val headerViewLayoutParams = headerViewLayoutParams
        val currentConditionsViewLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        headerViewLayoutParams!!.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        currentConditionsViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        currentConditionsViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header)
        val rootLayout = RelativeLayout(context)
        rootLayout.addView(headerView, headerViewLayoutParams)
        rootLayout.addView(currentConditionsView, currentConditionsViewLayoutParams)
        drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews!!, parentWidth, parentHeight)
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
        setDataViews(remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime, airQualityDto, currentConditionsDto, null)
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
        val successful = currentConditionsDto != null
        if (successful) {
            widgetDto.lastRefreshDateTime = multipleWeatherRestApiCallback.getRequestDateTime().toString()
            zoneOffset = currentConditionsDto!!.currentTime.offset
            widgetDto.timeZoneId = zoneId!!.id
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

    override fun setDisplayClock(displayClock: Boolean) {
        widgetDto.isDisplayClock = displayClock
    }
}