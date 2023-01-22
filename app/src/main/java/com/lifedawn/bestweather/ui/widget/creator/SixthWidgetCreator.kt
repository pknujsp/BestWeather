package com.lifedawn.bestweather.ui.widget.creator

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.gridlayout.widget.GridLayout
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
import com.lifedawn.bestweather.ui.widget.widgetprovider.SixthWidgetProvider
import java.time.ZoneId
import java.time.ZonedDateTime

class SixthWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    override fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews? {
        val remoteViews = createBaseRemoteViews()
        onSuccessfulProcess(remoteViews!!)
        drawViews(
            remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
            WeatherResponseProcessor.getTempCurrentConditionsDto(context),
            WeatherResponseProcessor.getTempAirQualityDto(), null, parentWidth, parentHeight
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return SixthWidgetProvider::class.java
    }

    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.currentConditions)
            set.add(WeatherDataType.airQuality)
            return set
        }

    fun setDataViews(
        remoteViews: RemoteViews?, addressName: String, lastRefreshDateTime: String, currentConditionsDto: CurrentConditionsDto?,
        airQualityDto: AirQualityDto?, onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, currentConditionsDto, airQualityDto, onDrawBitmapCallback, null, null)
    }

    private fun drawViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        currentConditionsDto: CurrentConditionsDto?,
        airQualityDto: AirQualityDto?,
        onDrawBitmapCallback: OnDrawBitmapCallback?,
        parentWidth: Int?,
        parentHeight: Int?
    ) {
        val rootLayout = RelativeLayout(context)
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime)
        headerView!!.id = R.id.header
        val sixWidgetView = layoutInflater.inflate(R.layout.view_sixth_widget, null, false) as ViewGroup
        val stationName = context.getString(R.string.measuring_station_name) + ": " + airQualityDto!!.cityName
        (sixWidgetView.findViewById<View>(R.id.measuring_station_name) as TextView).text =
            stationName
        (sixWidgetView.findViewById<View>(R.id.temperature) as TextView).text = currentConditionsDto!!.temp
        (sixWidgetView.findViewById<View>(R.id.weatherIcon) as ImageView).setImageResource(currentConditionsDto.getWeatherIcon())
        if (currentConditionsDto.getYesterdayTemp() != null) {
            val yesterdayCompText = makeTempCompareToYesterdayText(
                currentConditionsDto.temp,
                currentConditionsDto.getYesterdayTemp(), tempUnit, context
            )
            (sixWidgetView.findViewById<View>(R.id.yesterdayTemperature) as TextView).text = yesterdayCompText
        } else {
            sixWidgetView.findViewById<View>(R.id.yesterdayTemperature).visibility = View.GONE
        }
        val feelsLikeTemp = context.getString(R.string.feelsLike) + ": " + currentConditionsDto.feelsLikeTemp
        (sixWidgetView.findViewById<View>(R.id.feelsLikeTemp) as TextView).text = feelsLikeTemp
        var precipitation = ""
        if (currentConditionsDto.isHasPrecipitationVolume()) {
            precipitation += context.getString(R.string.precipitation) + ": " + currentConditionsDto.precipitationVolume
        } else {
            precipitation = context.getString(R.string.not_precipitation)
        }
        (sixWidgetView.findViewById<View>(R.id.precipitation) as TextView).text = precipitation
        val simpleAirQuality = context.getString(R.string.air_quality) + ": " + getGradeDescription(
            airQualityDto.aqi
        )
        var airQuality = context.getString(R.string.air_quality) + ": "
        airQuality += if (airQualityDto.isSuccessful()) {
            getGradeDescription(airQualityDto.aqi)
        } else {
            context.getString(R.string.noData)
        }
        (sixWidgetView.findViewById<View>(R.id.airQuality) as TextView).text = airQuality
        val airQualityGridLayout = sixWidgetView.findViewById<GridLayout>(R.id.airQualityGrid)
        if (airQualityDto.isSuccessful()) {

            //pm10, pm2.5, o3, co, so2, no2 순서로
            val particleNames = arrayOf(
                context.getString(R.string.pm10_str), context.getString(R.string.pm25_str),
                context.getString(R.string.o3_str), context.getString(R.string.co_str), context.getString(R.string.so2_str),
                context.getString(R.string.no2_str)
            )
            val iconIds = intArrayOf(R.drawable.pm10, R.drawable.pm25, R.drawable.o3, R.drawable.co, R.drawable.so2, R.drawable.no2)
            val gradeValueList: MutableList<String> = ArrayList()
            val gradeDescriptionList: MutableList<String> = ArrayList()
            gradeValueList.add(airQualityDto.current!!.pm10.toString())
            gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.pm10))
            gradeValueList.add(airQualityDto.current!!.pm25.toString())
            gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.pm25))
            gradeValueList.add(airQualityDto.current!!.o3.toString())
            gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.o3))
            gradeValueList.add(airQualityDto.current!!.co.toString())
            gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.co))
            gradeValueList.add(airQualityDto.current!!.so2.toString())
            gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.so2))
            gradeValueList.add(airQualityDto.current!!.no2.toString())
            gradeDescriptionList.add(getGradeDescription(airQualityDto.current!!.no2))
            for (i in 0..5) {
                addAirQualityGridItem(
                    layoutInflater, airQualityGridLayout, particleNames[i], gradeValueList[i], gradeDescriptionList[i],
                    iconIds[i]
                )
            }
        } else {
            airQualityGridLayout.visibility = View.GONE
        }
        val headerViewLayoutParams = headerViewLayoutParams
        val sixWidgetViewLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        headerViewLayoutParams!!.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        sixWidgetViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header)
        sixWidgetViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        rootLayout.addView(headerView, headerViewLayoutParams)
        rootLayout.addView(sixWidgetView, sixWidgetViewLayoutParams)
        drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews!!, parentWidth, parentHeight)
    }

    private fun addAirQualityGridItem(
        layoutInflater: LayoutInflater, gridLayout: GridLayout, label: String, gradeValue: String,
        gradeDescription: String, iconId: Int
    ) {
        val view = layoutInflater.inflate(R.layout.air_quality_item, null) as LinearLayout
        (view.findViewById<View>(R.id.label_icon) as ImageView).setImageResource(iconId)
        val labelTextView = view.findViewById<TextView>(R.id.label)
        val gradeValueTextView = view.findViewById<TextView>(R.id.value_int)
        val gradeDescriptionTextView = view.findViewById<TextView>(R.id.value_str)
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
        var weatherProviderType = WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet())
        if (widgetDto.isTopPriorityKma && widgetDto.countryCode == "KR") {
            weatherProviderType = WeatherProviderType.KMA_WEB
        }
        initWeatherSourceUniqueValues(weatherProviderType, true, context)
        zoneId = ZoneId.of(widgetDto.timeZoneId)
        val remoteViews = createRemoteViews()
        val jsonObject = JsonParser.parseString(widgetDto.responseText) as JsonObject
        val currentConditionsDto = WeatherResponseProcessor.parseTextToCurrentConditionsDto(
            context, jsonObject,
            weatherProviderType, widgetDto.latitude, widgetDto.longitude, zoneId
        )
        val airQualityDto = parseTextToAirQualityDto(jsonObject)
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime, currentConditionsDto,
            airQualityDto, null
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
        val currentConditionsDto = WeatherResponseProcessor.getCurrentConditionsDto(
            context, multipleWeatherRestApiCallback,
            WeatherResponseProcessor.getMainWeatherSourceType(widgetDto.getWeatherProviderTypeSet()), zoneId
        )
        val airQualityDto = WeatherResponseProcessor.getAirQualityDto(multipleWeatherRestApiCallback, null)
        val successful = currentConditionsDto != null && airQualityDto.isSuccessful()
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