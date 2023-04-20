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
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.parseTextToAirQualityDto
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.initWeatherSourceUniqueValues
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherUtil.makeTempCompareToYesterdayText
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.ThirdWidgetProvider
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ThirdWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    private val hourlyForecastCount = 12
    private val dailyForecastCount = 5
    override fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews? {
        val remoteViews = createBaseRemoteViews()
        onSuccessfulProcess(remoteViews!!)
        drawViews(
            remoteViews,
            context.getString(R.string.address_name),
            ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempAirQualityDto(),
            WeatherResponseProcessor.getTempCurrentConditionsDto(context),
            WeatherResponseProcessor.getTempHourlyForecastDtoList(context, hourlyForecastCount),
            WeatherResponseProcessor.getTempDailyForecastDtoList(context, dailyForecastCount),
            null,
            parentWidth,
            parentHeight
        )
        return remoteViews
    }

    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.currentConditions)
            set.add(WeatherDataType.hourlyForecast)
            set.add(WeatherDataType.dailyForecast)
            set.add(WeatherDataType.airQuality)
            return set
        }

    override fun widgetProviderClass(): Class<*> {
        return ThirdWidgetProvider::class.java
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
        view: View, currentConditionsDto: CurrentConditionsDto?,
        airQualityDto: AirQualityDto?
    ) {
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
        (view.findViewById<View>(R.id.precipitation) as TextView).text = precipitation
        val feelsLikeTemp = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.feelsLikeTemp
        (view.findViewById<View>(R.id.feelsLikeTemp) as TextView).text = feelsLikeTemp
        if (currentConditionsDto.getYesterdayTemp() != null) {
            val yesterdayCompText = makeTempCompareToYesterdayText(
                currentConditionsDto.temp,
                currentConditionsDto.getYesterdayTemp(), tempUnit, context
            )
            (view.findViewById<View>(R.id.yesterdayTemperature) as TextView).text = yesterdayCompText
        } else {
            view.findViewById<View>(R.id.yesterdayTemperature).visibility = View.GONE
        }
    }

    fun setHourlyForecastViews(view: View, layoutInflater: LayoutInflater, hourlyForecastDtoList: List<HourlyForecastDto?>) {
        val hour0Formatter = DateTimeFormatter.ofPattern("E 0")
        var hours: String? = ""
        val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.weight = 1f
        layoutParams.gravity = Gravity.CENTER
        val padding = context.resources.getDimension(R.dimen.forecastItemViewInLinearPadding).toInt()
        for (i in 0 until hourlyForecastCount) {
            val itemView = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false)
            itemView.layoutParams = layoutParams
            itemView.setPadding(0, padding, 0, padding)
            hours = if (hourlyForecastDtoList[i]!!.hours.hour == 0) {
                hourlyForecastDtoList[i]!!.hours.format(hour0Formatter)
            } else {
                hourlyForecastDtoList[i]!!.hours.hour.toString()
            }
            (itemView.findViewById<View>(R.id.dateTime) as TextView).text = hours
            (itemView.findViewById<View>(R.id.temperature) as TextView).text = hourlyForecastDtoList[i]!!.temp
            (itemView.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                hourlyForecastDtoList[i]!!.weatherIcon
            )
            itemView.findViewById<View>(R.id.rightIcon).visibility = View.GONE
            itemView.findViewById<View>(R.id.popLayout).visibility = View.GONE
            itemView.findViewById<View>(R.id.rainVolumeLayout).visibility = View.GONE
            itemView.findViewById<View>(R.id.snowVolumeLayout).visibility = View.GONE
            if (i >= hourlyForecastCount / 2) {
                (view.findViewById<View>(R.id.hourly_forecast_row_2) as ViewGroup).addView(itemView)
            } else {
                (view.findViewById<View>(R.id.hourly_forecast_row_1) as ViewGroup).addView(itemView)
            }
        }
    }

    fun setDailyForecastViews(view: View, layoutInflater: LayoutInflater, dailyForecastDtoList: List<DailyForecastDto?>) {
        val dateFormatter = DateTimeFormatter.ofPattern("E")
        var temp = ""
        val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.weight = 1f
        layoutParams.gravity = Gravity.CENTER
        val padding = context.resources.getDimension(R.dimen.forecastItemViewInLinearPadding).toInt()
        for (day in 0 until dailyForecastCount) {
            val itemView = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false)
            itemView.layoutParams = layoutParams
            itemView.setPadding(0, padding, 0, padding)
            (itemView.findViewById<View>(R.id.dateTime) as TextView).text = dailyForecastDtoList[day]!!.date.format(dateFormatter)
            temp = dailyForecastDtoList[day]!!.minTemp + "/" + dailyForecastDtoList[day]!!.maxTemp
            (itemView.findViewById<View>(R.id.temperature) as TextView).text = temp
            if (dailyForecastDtoList[day]!!.valuesList.size == 1) {
                (itemView.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[day]!!.valuesList[0].weatherIcon
                )
                itemView.findViewById<View>(R.id.rightIcon).visibility = View.GONE
            } else if (dailyForecastDtoList[day]!!.valuesList.size == 2) {
                (itemView.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[day]!!.valuesList[0].weatherIcon
                )
                (itemView.findViewById<View>(R.id.rightIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[day]!!.valuesList[1].weatherIcon
                )
            } else if (dailyForecastDtoList[day]!!.valuesList.size == 4) {
                (itemView.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[day]!!.valuesList[1].weatherIcon
                )
                (itemView.findViewById<View>(R.id.rightIcon) as ImageView).setImageResource(
                    dailyForecastDtoList[day]!!.valuesList[2].weatherIcon
                )
            }
            itemView.findViewById<View>(R.id.popLayout).visibility = View.GONE
            itemView.findViewById<View>(R.id.rainVolumeLayout).visibility = View.GONE
            itemView.findViewById<View>(R.id.snowVolumeLayout).visibility = View.GONE
            (view.findViewById<View>(R.id.daily_forecast_row) as ViewGroup).addView(itemView)
        }
    }

    fun setDataViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?,
        hourlyForecastDtoList: List<HourlyForecastDto?>,
        dailyForecastDtoList: List<DailyForecastDto?>,
        onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(
            remoteViews, addressName, lastRefreshDateTime, airQualityDto, currentConditionsDto, hourlyForecastDtoList,
            dailyForecastDtoList, onDrawBitmapCallback, null, null
        )
    }

    private fun drawViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?,
        currentConditionsDto: CurrentConditionsDto?,
        hourlyForecastDtoList: List<HourlyForecastDto?>,
        dailyForecastDtoList: List<DailyForecastDto?>,
        onDrawBitmapCallback: OnDrawBitmapCallback?,
        parentWidth: Int?,
        parentHeight: Int?
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime)
        headerView!!.id = R.id.header
        val valuesView = layoutInflater.inflate(R.layout.view_third_widget, null, false)
        makeCurrentConditionsViews(valuesView, currentConditionsDto, airQualityDto)
        setHourlyForecastViews(valuesView, layoutInflater, hourlyForecastDtoList)
        setDailyForecastViews(valuesView, layoutInflater, dailyForecastDtoList)
        val headerViewLayoutParams = headerViewLayoutParams
        val valuesViewLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        headerViewLayoutParams!!.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        valuesViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        valuesViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header)
        val rootLayout = RelativeLayout(context)
        rootLayout.addView(headerView, headerViewLayoutParams)
        rootLayout.addView(valuesView, valuesViewLayoutParams)
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
        val dailyForecastDtoList = WeatherResponseProcessor.parseTextToDailyForecastDtoList(
            context, jsonObject,
            weatherProviderType, zoneId
        )
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime, airQualityDto, currentConditionsDto,
            hourlyForecastDtoList, dailyForecastDtoList, null
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        onSuccessfulProcess(remoteViews!!)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
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
        val dailyForecastDtoList = WeatherResponseProcessor.getDailyForecastDtoList(
            context, multipleWeatherRestApiCallback,
            weatherProviderType, zoneId
        )
        val successful = (currentConditionsDto != null && !hourlyForecastDtoList.isEmpty()
                && !dailyForecastDtoList.isEmpty())
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