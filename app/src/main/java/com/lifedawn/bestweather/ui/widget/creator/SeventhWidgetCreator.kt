package com.lifedawn.bestweather.ui.widget.creator

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.gridlayout.widget.GridLayout
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onErrorProcess
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onSuccessfulProcess
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.room.dto.WidgetDto
import com.lifedawn.bestweather.data.local.room.repository.WidgetRepository.add
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto.DailyForecast.Val
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.init
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.parseTextToAirQualityDto
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.SeventhWidgetProvider
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SeventhWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    private val forecastDateFormatter = DateTimeFormatter.ofPattern("E")
    override fun loadDefaultSettings(): WidgetDto? {
        widgetDto = super.loadDefaultSettings()
        widgetDto.getWeatherProviderTypeSet().clear()
        widgetDto.getWeatherProviderTypeSet().add(WeatherProviderType.AQICN)
        return widgetDto
    }

    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
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
            null,
            parentWidth,
            parentHeight
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return SeventhWidgetProvider::class.java
    }

    fun setDataViews(
        remoteViews: RemoteViews?, addressName: String, lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?, onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto, onDrawBitmapCallback, null, null)
    }

    private fun drawViews(
        remoteViews: RemoteViews?, addressName: String, lastRefreshDateTime: String,
        airQualityDto: AirQualityDto?, onDrawBitmapCallback: OnDrawBitmapCallback?, parentWidth: Int?,
        parentHeight: Int?
    ) {
        if (!airQualityDto.isSuccessful()) {
            onErrorProcess(remoteViews!!, context, RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA)
            setRefreshPendingIntent(remoteViews)
            return
        }
        val rootLayout = RelativeLayout(context)
        val layoutInflater = LayoutInflater.from(context)
        val headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime)
        headerView!!.id = R.id.header
        val seventhView = layoutInflater.inflate(R.layout.view_seventh_widget, null, false) as ViewGroup
        val stationName = context.getString(R.string.measuring_station_name) + ": " + airQualityDto!!.cityName
        (seventhView.findViewById<View>(R.id.measuring_station_name) as TextView).text = stationName
        val airQuality = context.getString(R.string.air_quality) + ": " + getGradeDescription(
            airQualityDto.aqi
        )
        (seventhView.findViewById<View>(R.id.airQuality) as TextView).text = airQuality
        val gridLayout = seventhView.findViewById<GridLayout>(R.id.airQualityGrid)

        //co, so2, no2 순서로
        val particleNames = arrayOf(
            context.getString(R.string.co_str), context.getString(R.string.so2_str),
            context.getString(R.string.no2_str)
        )
        val gradeValueList: MutableList<String> = ArrayList()
        val gradeDescriptionList: MutableList<String> = ArrayList()
        gradeValueList.add(airQualityDto.current!!.co.toString())
        gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.co))
        gradeValueList.add(airQualityDto.current!!.so2.toString())
        gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.so2))
        gradeValueList.add(airQualityDto.current!!.no2.toString())
        gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.no2))
        for (i in 0..2) {
            addAirQualityGridItem(layoutInflater, gridLayout, particleNames[i], gradeValueList[i], gradeDescriptionList[i])
        }
        val forecastLayout = seventhView.findViewById<LinearLayout>(R.id.airQualityForecast)
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt()
        val noData = context.getString(R.string.noData)
        val forecastItemLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        forecastItemLayoutParams.bottomMargin = margin
        var forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null)
        forecastItemView.setPadding(0, 0, 0, 0)
        var dateTextView = forecastItemView.findViewById<TextView>(R.id.date)
        var pm10TextView = forecastItemView.findViewById<TextView>(R.id.pm10)
        var pm25TextView = forecastItemView.findViewById<TextView>(R.id.pm25)
        var o3TextView = forecastItemView.findViewById<TextView>(R.id.o3)
        dateTextView.text = null
        pm10TextView.text = context.getString(R.string.pm10_str)
        pm25TextView.text = context.getString(R.string.pm25_str)
        o3TextView.text = context.getString(R.string.o3_str)
        forecastLayout.addView(forecastItemView, forecastItemLayoutParams)
        val current = DailyForecast()
        current.setDate(null).setPm10(Val().setAvg(airQualityDto.current!!.pm10))
            .setPm25(Val().setAvg(airQualityDto.current!!.pm25))
            .setO3(Val().setAvg(airQualityDto.current!!.o3))
        val dailyForecastList: MutableList<DailyForecast> = ArrayList()
        dailyForecastList.add(current)
        dailyForecastList.addAll(airQualityDto.dailyForecastList)
        for (item in dailyForecastList) {
            forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null)
            forecastItemView.setPadding(0, 0, 0, 0)
            dateTextView = forecastItemView.findViewById(R.id.date)
            pm10TextView = forecastItemView.findViewById(R.id.pm10)
            pm25TextView = forecastItemView.findViewById(R.id.pm25)
            o3TextView = forecastItemView.findViewById(R.id.o3)
            dateTextView.text = if (item.date == null) context.getString(R.string.current) else item.date.format(forecastDateFormatter)
            if (item.isHasPm10()) {
                pm10TextView.text = getGradeDescription(item.pm10!!.avg)
            } else {
                pm10TextView.text = noData
            }
            if (item.isHasPm25()) {
                pm25TextView.text = getGradeDescription(item.pm25!!.avg)
            } else {
                pm25TextView.text = noData
            }
            if (item.isHasO3()) {
                o3TextView.text = getGradeDescription(item.o3!!.avg)
            } else {
                o3TextView.text = noData
            }
            forecastLayout.addView(forecastItemView, forecastItemLayoutParams)
        }
        val headerViewLayoutParams = headerViewLayoutParams
        val seventhWidgetViewLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        headerViewLayoutParams!!.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        seventhWidgetViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header)
        seventhWidgetViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        rootLayout.addView(headerView, headerViewLayoutParams)
        rootLayout.addView(seventhView, seventhWidgetViewLayoutParams)
        drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews!!, parentWidth, parentHeight)
    }

    private fun addAirQualityGridItem(
        layoutInflater: LayoutInflater, gridLayout: GridLayout, label: String, gradeValue: String,
        gradeDescription: String
    ) {
        val view = layoutInflater.inflate(R.layout.view_simple_air_quality_item, null, false)
        val labelTextView = view.findViewById<TextView>(R.id.label)
        val gradeValueTextView = view.findViewById<TextView>(R.id.gradeValue)
        val gradeDescriptionTextView = view.findViewById<TextView>(R.id.gradeDescription)
        labelTextView.text = label
        gradeValueTextView.text = gradeValue
        gradeDescriptionTextView.text = gradeDescription
        val cellCount = gridLayout.childCount
        val row = cellCount / gridLayout.columnCount
        val column = cellCount % gridLayout.columnCount
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1f)
        layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1f)
        gridLayout.addView(view, layoutParams)
    }

    override fun setDisplayClock(displayClock: Boolean) {
        widgetDto.isDisplayClock = displayClock
    }

    override fun setDataViewsOfSavedData() {
        val remoteViews = createRemoteViews()
        val jsonObject = JsonParser.parseString(widgetDto.responseText) as JsonObject
        val airQualityDto = parseTextToAirQualityDto(jsonObject)
        init(context)
        zoneId = ZoneId.of(widgetDto.timeZoneId)
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime,
            airQualityDto, null
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        onSuccessfulProcess(remoteViews!!)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    override fun setResultViews(appWidgetId: Int, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?, zoneId: ZoneId?) {
        this.zoneId = zoneId
        val airQualityDto = WeatherResponseProcessor.getAirQualityDto(multipleWeatherRestApiCallback, null)
        val successful = airQualityDto != null && airQualityDto.isSuccessful()
        if (successful) {
            val zoneOffset = ZoneOffset.of(airQualityDto!!.timeInfo!!.tz)
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