package com.lifedawn.bestweather.ui.widget.creator

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.ArrayMap
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onSuccessfulProcess
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.local.room.dto.WidgetDto
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.WeatherResponseProcessor
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.initWeatherSourceUniqueValues
import com.lifedawn.bestweather.ui.widget.OnDrawBitmapCallback
import com.lifedawn.bestweather.ui.widget.widgetprovider.EleventhWidgetProvider
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class EleventhWidgetCreator(context: Context, widgetUpdateCallback: WidgetUpdateCallback?, appWidgetId: Int) :
    AbstractWidgetCreator(context, widgetUpdateCallback, appWidgetId) {
    private val cellCount = 9
    override val requestWeatherDataTypeSet: Set<Any?>
        get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.hourlyForecast)
            return set
        }

    override fun loadDefaultSettings(): WidgetDto? {
        val widgetDto = super.loadDefaultSettings()
        widgetDto!!.isMultipleWeatherDataSource = true
        return widgetDto
    }

    override fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews? {
        val remoteViews = createBaseRemoteViews()
        onSuccessfulProcess(remoteViews!!)
        val hourlyForecastDtoListMap = ArrayMap<WeatherProviderType, List<HourlyForecastDto?>>()
        hourlyForecastDtoListMap[WeatherProviderType.KMA_WEB] = WeatherResponseProcessor.getTempHourlyForecastDtoList(
            context,
            cellCount
        )
        hourlyForecastDtoListMap[WeatherProviderType.OWM_ONECALL] = WeatherResponseProcessor.getTempHourlyForecastDtoList(
            context,
            cellCount
        )
        drawViews(
            remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
            hourlyForecastDtoListMap,
            null, parentWidth, parentHeight
        )
        return remoteViews
    }

    override fun widgetProviderClass(): Class<*> {
        return EleventhWidgetProvider::class.java
    }

    fun setDataViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        hourlyForecastDtoListMap: ArrayMap<WeatherProviderType, List<HourlyForecastDto?>>,
        onDrawBitmapCallback: OnDrawBitmapCallback?
    ) {
        drawViews(remoteViews, addressName, lastRefreshDateTime, hourlyForecastDtoListMap, onDrawBitmapCallback, null, null)
    }

    private fun drawViews(
        remoteViews: RemoteViews?,
        addressName: String,
        lastRefreshDateTime: String,
        hourlyForecastDtoListMap: ArrayMap<WeatherProviderType, List<HourlyForecastDto?>>,
        onDrawBitmapCallback: OnDrawBitmapCallback?,
        parentWidth: Int?,
        parentHeight: Int?
    ) {
        val rootLayout = RelativeLayout(context)
        val layoutInflater = LayoutInflater.from(context)
        val headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime)
        headerView!!.id = R.id.header
        val hour0Formatter = DateTimeFormatter.ofPattern("E 0")

        //첫번째로 일치하는 시각을 찾는다. 첫 시각이 kma가 12시, owm이 13시 이면 13시를 첫 시작으로 하여 화면을 표시
        val weatherProviderTypeSet: Set<WeatherProviderType> = hourlyForecastDtoListMap.keys
        var firstDateTime: ZonedDateTime? = null
        for (hourlyForecastDtoList in hourlyForecastDtoListMap.values) {
            if (firstDateTime == null) {
                firstDateTime = ZonedDateTime.of(
                    hourlyForecastDtoList[0]!!.hours.toLocalDateTime(),
                    hourlyForecastDtoList[0]!!.hours.zone
                )
            } else if (firstDateTime.isBefore(hourlyForecastDtoList[0]!!.hours)) {
                firstDateTime = ZonedDateTime.of(
                    hourlyForecastDtoList[0]!!.hours.toLocalDateTime(),
                    hourlyForecastDtoList[0]!!.hours.zone
                )
            }
        }
        val firstHours = TimeUnit.SECONDS.toHours(firstDateTime!!.toEpochSecond())
        var hours: Long = 0
        val firstBeginIdxMap: MutableMap<WeatherProviderType, Int> = HashMap()
        for (weatherProviderType in hourlyForecastDtoListMap.keys) {
            hours = TimeUnit.SECONDS.toHours(hourlyForecastDtoListMap[weatherProviderType]!![0]!!.hours.toEpochSecond())
            firstBeginIdxMap[weatherProviderType] = (firstHours - hours).toInt()
        }

        //시각을 먼저 표시
        var hour: String? = null
        val hoursRow = LinearLayout(context)
        hoursRow.id = R.id.hoursRow
        hoursRow.orientation = LinearLayout.HORIZONTAL
        val hourTextViewLayoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        hourTextViewLayoutParams.weight = 1f
        val textColor = ContextCompat.getColor(context, R.color.widgetTextColor)
        for (i in 0 until cellCount) {
            hour = if (firstDateTime!!.hour == 0) firstDateTime.format(hour0Formatter) else firstDateTime.hour.toString()
            firstDateTime = firstDateTime.plusHours(1)
            val textView = TextView(context)
            textView.text = hour
            textView.setTextColor(textColor)
            textView.gravity = Gravity.CENTER
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            hoursRow.addView(textView, hourTextViewLayoutParams)
        }
        val forecastTable = LinearLayout(context)
        forecastTable.orientation = LinearLayout.VERTICAL
        val forecastRowLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        forecastRowLayoutParams.weight = 1f
        val mm = "mm"
        val cm = "cm"
        for (weatherProviderType in weatherProviderTypeSet) {
            val hourlyForecastDtoList = hourlyForecastDtoListMap[weatherProviderType]!!
            val row = LinearLayout(context)
            row.orientation = LinearLayout.VERTICAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.layoutParams = forecastRowLayoutParams
            val hourlyForecastListView = LinearLayout(context)
            hourlyForecastListView.orientation = LinearLayout.HORIZONTAL
            val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER
            layoutParams.weight = 1f
            var haveRain = false
            var haveSnow = false
            val count = cellCount + firstBeginIdxMap[weatherProviderType]!!
            for (cell in firstBeginIdxMap[weatherProviderType]!! until count) {
                if (hourlyForecastDtoList[cell]!!.isHasRain || hourlyForecastDtoList[cell]!!.isHasPrecipitation) {
                    if (!haveRain) {
                        haveRain = true
                    }
                }
                if (hourlyForecastDtoList[cell]!!.isHasSnow) {
                    if (!haveSnow) {
                        haveSnow = true
                    }
                }
            }
            var rain: String? = null
            for (cell in firstBeginIdxMap[weatherProviderType]!! until count) {
                val view = layoutInflater.inflate(R.layout.view_forecast_item_in_linear, null, false)
                (view.findViewById<View>(R.id.leftIcon) as ImageView).setImageResource(
                    hourlyForecastDtoList[cell]!!.weatherIcon
                )
                if (hourlyForecastDtoList[cell]!!.pop != "-") {
                    (view.findViewById<View>(R.id.pop) as TextView).text = hourlyForecastDtoList[cell]!!.pop
                } else {
                    view.findViewById<View>(R.id.popLayout).visibility = View.INVISIBLE
                }
                if (haveRain) {
                    if (hourlyForecastDtoList[cell]!!.isHasRain || hourlyForecastDtoList[cell]!!.isHasPrecipitation) {
                        rain =
                            if (hourlyForecastDtoList[cell]!!.isHasRain) hourlyForecastDtoList[cell]!!.rainVolume else hourlyForecastDtoList[cell]!!.precipitationVolume
                        (view.findViewById<View>(R.id.rainVolume) as TextView).text = rain.replace(mm, "").replace(cm, "")
                    } else {
                        view.findViewById<View>(R.id.rainVolumeLayout).visibility = View.INVISIBLE
                    }
                } else {
                    view.findViewById<View>(R.id.rainVolumeLayout).visibility = View.GONE
                }
                if (haveSnow) {
                    if (hourlyForecastDtoList[cell]!!.isHasSnow) {
                        (view.findViewById<View>(R.id.snowVolume) as TextView).text = hourlyForecastDtoList[cell]!!.snowVolume.replace(
                            mm,
                            ""
                        ).replace(cm, "")
                    } else {
                        view.findViewById<View>(R.id.snowVolumeLayout).visibility = View.INVISIBLE
                    }
                } else {
                    view.findViewById<View>(R.id.snowVolumeLayout).visibility = View.GONE
                }
                (view.findViewById<View>(R.id.temperature) as TextView).text = hourlyForecastDtoList[cell]!!.temp
                view.findViewById<View>(R.id.dateTime).visibility = View.GONE
                view.findViewById<View>(R.id.rightIcon).visibility = View.GONE
                hourlyForecastListView.addView(view, layoutParams)
            }
            var weatherSource: String? = null
            var icon = 0
            if (weatherProviderType === WeatherProviderType.KMA_WEB) {
                weatherSource = context.getString(R.string.kma)
                icon = R.drawable.kmaicon
            } else if (weatherProviderType === WeatherProviderType.ACCU_WEATHER) {
                weatherSource = context.getString(R.string.accu_weather)
                icon = R.drawable.accuicon
            } else if (weatherProviderType === WeatherProviderType.OWM_ONECALL
                || weatherProviderType === WeatherProviderType.OWM_INDIVIDUAL
            ) {
                weatherSource = context.getString(R.string.owm)
                icon = R.drawable.owmicon
            } else {
                weatherSource = context.getString(R.string.met)
                icon = R.drawable.metlogo
            }
            val weatherSourceView = layoutInflater.inflate(R.layout.weather_data_source_view, null)
            (weatherSourceView.findViewById<View>(R.id.source) as TextView).text = weatherSource
            (weatherSourceView.findViewById<View>(R.id.icon) as ImageView).setImageResource(icon)
            val listViewLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
            listViewLayoutParams.weight = 1f
            row.addView(weatherSourceView)
            row.addView(hourlyForecastListView, listViewLayoutParams)
            forecastTable.addView(row)
        }
        val headerViewLayoutParams = headerViewLayoutParams
        val hoursRowLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val forecastViewsLayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        headerViewLayoutParams!!.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        hoursRowLayoutParams.addRule(RelativeLayout.BELOW, R.id.header)
        forecastViewsLayoutParams.addRule(RelativeLayout.BELOW, R.id.hoursRow)
        forecastViewsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        rootLayout.addView(headerView, headerViewLayoutParams)
        rootLayout.addView(hoursRow, hoursRowLayoutParams)
        rootLayout.addView(forecastTable, forecastViewsLayoutParams)
        drawBitmap(rootLayout, onDrawBitmapCallback, remoteViews!!, parentWidth, parentHeight)
    }

    override fun setDisplayClock(displayClock: Boolean) {
        widgetDto.isDisplayClock = displayClock
    }

    override fun setDataViewsOfSavedData() {
        val remoteViews = createRemoteViews()
        zoneId = ZoneId.of(widgetDto.timeZoneId)
        val jsonObject = JsonParser.parseString(widgetDto.responseText) as JsonObject
        val weatherSourceTypeListArrayMap = ArrayMap<WeatherProviderType, List<HourlyForecastDto?>>()
        val weatherProviderTypeSet: Set<WeatherProviderType> = widgetDto.getWeatherProviderTypeSet()
        for (weatherProviderType in weatherProviderTypeSet) {
            initWeatherSourceUniqueValues(weatherProviderType, false, context)
            weatherSourceTypeListArrayMap[weatherProviderType] = WeatherResponseProcessor.parseTextToHourlyForecastDtoList(
                context, jsonObject, weatherProviderType, widgetDto.latitude,
                widgetDto.longitude, zoneId
            )
        }
        setDataViews(
            remoteViews, widgetDto.addressName, widgetDto.lastRefreshDateTime,
            weatherSourceTypeListArrayMap, null
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        onSuccessfulProcess(remoteViews!!)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    override fun setResultViews(appWidgetId: Int, multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?, zoneId: ZoneId?) {
        this.zoneId = zoneId
        val weatherSourceTypeListArrayMap = ArrayMap<WeatherProviderType?, List<HourlyForecastDto?>>()
        val requestWeatherProviderTypeSet: Set<WeatherProviderType?> = widgetDto.getWeatherProviderTypeSet()
        var successful = true
        for (weatherProviderType in requestWeatherProviderTypeSet) {
            weatherSourceTypeListArrayMap[weatherProviderType] = WeatherResponseProcessor.getHourlyForecastDtoList(
                context, multipleWeatherRestApiCallback,
                weatherProviderType!!, zoneId
            )
            if (weatherSourceTypeListArrayMap[weatherProviderType]!!.isEmpty()) {
                successful = false
                break
            }
        }
        if (successful) {
            val zoneOffset = weatherSourceTypeListArrayMap.valueAt(0)[0]!!.hours.offset
            widgetDto.timeZoneId = zoneId!!.id
            widgetDto.lastRefreshDateTime = multipleWeatherRestApiCallback.getRequestDateTime().toString()
            makeResponseTextToJson(
                multipleWeatherRestApiCallback,
                requestWeatherDataTypeSet,
                requestWeatherProviderTypeSet,
                widgetDto,
                zoneOffset
            )
        }
        widgetDto.isLoadSuccessful = successful
        super.setResultViews(appWidgetId, multipleWeatherRestApiCallback, zoneId)
    }
}