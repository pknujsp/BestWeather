package com.lifedawn.bestweather.ui.widget.creator

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.ArrayMap
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractWidgetCreator(
    var context: Context,
    protected var widgetUpdateCallback: WidgetUpdateCallback?,
    val appWidgetId: Int
) {
    protected val refreshDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a h:mm")
    protected val tempUnit: ValueUnits
    protected val clockUnit: ValueUnits
    protected val tempDegree: String
    protected var zoneId: ZoneId? = null
    protected var widgetDto: WidgetDto? = null
    protected var widgetRepository: WidgetRepository
    protected var appWidgetManager: AppWidgetManager
    protected var remoteViewsCallback: RemoteViewsCallback? = null
    fun setRemoteViewsCallback(remoteViewsCallback: RemoteViewsCallback?): AbstractWidgetCreator {
        this.remoteViewsCallback = remoteViewsCallback
        return this
    }

    fun setWidgetDto(widgetDto: WidgetDto?): AbstractWidgetCreator {
        this.widgetDto = widgetDto
        return this
    }

    abstract fun createTempViews(parentWidth: Int?, parentHeight: Int?): RemoteViews?

    init {
        tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit()
        clockUnit = MyApplication.VALUE_UNIT_OBJ.getClockUnit()
        tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText()
        widgetRepository = WidgetRepository.getINSTANCE()
        appWidgetManager = AppWidgetManager.getInstance(context)
    }

    fun setRefreshPendingIntent(remoteViews: RemoteViews) {
        remoteViews.setOnClickPendingIntent(R.id.refreshBtn, refreshPendingIntent)
    }

    val refreshPendingIntent: PendingIntent
        get() {
            val refreshIntent = Intent(context, FirstWidgetProvider::class.java)
            refreshIntent.action = context.getString(R.string.com_lifedawn_bestweather_action_REFRESH)
            return PendingIntent.getBroadcast(
                context, IntentRequestCodes.WIDGET_MANUALLY_REFRESH.requestCode, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

    open fun loadDefaultSettings(): WidgetDto? {
        widgetDto = WidgetDto()
        widgetDto.appWidgetId = appWidgetId
        widgetDto.isMultipleWeatherDataSource = false
        widgetDto.widgetProviderClassName = widgetProviderClass().name
        widgetDto.backgroundAlpha = 100
        widgetDto.isDisplayClock = true
        widgetDto.isDisplayLocalClock = false
        widgetDto.locationType = LocationType.CurrentLocation
        widgetDto.addWeatherProviderType(
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    context.getString(R.string.pref_key_met), true
                )
            ) WeatherProviderType.MET_NORWAY else WeatherProviderType.OWM_ONECALL
        )
        widgetDto.textSizeAmount = 0
        widgetDto.isTopPriorityKma = true
        if (requestWeatherDataTypeSet.contains(WeatherDataType.airQuality)) {
            widgetDto.getWeatherProviderTypeSet().add(WeatherProviderType.AQICN)
        }
        return widgetDto
    }

    fun loadSavedSettings(callback: DbQueryCallback<WidgetDto?>?) {
        widgetRepository.get(appWidgetId, object : DbQueryCallback<WidgetDto?>() {
            fun onResultSuccessful(result: WidgetDto?) {
                widgetDto = result
                if (callback != null) {
                    callback.onResultSuccessful(widgetDto)
                }
            }

            fun onResultNoData() {}
        })
    }

    fun saveSettings(widgetDto: WidgetDto?, callback: DbQueryCallback<WidgetDto?>?) {
        widgetRepository.add(widgetDto, object : DbQueryCallback<WidgetDto?>() {
            fun onResultSuccessful(result: WidgetDto?) {
                if (callback != null) {
                    callback.onResultSuccessful(result)
                }
            }

            fun onResultNoData() {}
        })
    }

    fun updateSettings(widgetDto: WidgetDto?, callback: DbQueryCallback<WidgetDto?>?) {
        widgetRepository.update(widgetDto, object : DbQueryCallback<WidgetDto?>() {
            fun onResultSuccessful(result: WidgetDto?) {
                if (callback != null) {
                    callback.onResultSuccessful(result)
                }
            }

            fun onResultNoData() {}
        })
    }

    protected fun setBackgroundAlpha(remoteViews: RemoteViews, backgroundAlpha: Int) {
        val opacity: Float = widgetDto.backgroundAlpha / 100f
        val newBackgroundColor = (opacity * 0xFF).toInt() shl 24 or AppTheme.getColor(context, R.color.widgetBackgroundColor)
        remoteViews.setInt(R.id.root_layout, "setBackgroundColor", newBackgroundColor)
    }

    val onClickedPendingIntent: PendingIntent
        get() {
            val intent = Intent(context, DialogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val bundle = Bundle()
            bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.putExtras(bundle)
            return PendingIntent.getActivity(
                context, IntentRequestCodes.CLICK_WIDGET.requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

    protected fun getWidgetSizeInDp(appWidgetManager: AppWidgetManager, key: String?): Int {
        return appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(key, 0)
    }

    /**
     * @param
     * @return {widgetWidthPx, widgetHeightPx} 반환
     */
    fun getWidgetExactSizeInPx(appWidgetManager: AppWidgetManager): IntArray {
        var widgetWidth = 0
        var widgetHeight = 0
        val portrait = context.resources.configuration.orientation
        if (portrait == Configuration.ORIENTATION_PORTRAIT) {
            widgetWidth = getWidgetSizeInDp(appWidgetManager, AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            widgetHeight = getWidgetSizeInDp(appWidgetManager, AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        } else {
            widgetWidth = getWidgetSizeInDp(appWidgetManager, AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            widgetHeight = getWidgetSizeInDp(appWidgetManager, AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        }
        val density = context.resources.displayMetrics.density
        val widgetWidthPx = widgetWidth * density
        val widgetHeightPx = widgetHeight * density
        return intArrayOf(widgetWidthPx.toInt(), widgetHeightPx.toInt())
    }

    fun makeResponseTextToJson(
        multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback, weatherDataTypeSet: Set<WeatherDataType?>,
        weatherProviderTypeSet: Set<WeatherProviderType?>, widgetDto: WidgetDto, zoneOffset: ZoneOffset?
    ) {
        //json형태로 저장
        /*
		examples :
		{
			"KMA":{
				"ULTRA_SRT_NCST": ~~json text~~,
				"ULTRA_SRT_FCST": ~~json text~~
			},
			"OPEN_WEATHER_MAP":{
				"OWM_ONE_CALL": ~~json text~~
			},
			"zoneOffset": "09:00"
		}
		 */
        val arrayMap: Map<WeatherProviderType, ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult>> =
            multipleWeatherRestApiCallback.responseMap
        val rootJsonObject = JsonObject()
        var text: String? = null

        //owm이면 onecall이므로 한번만 수행
        for (weatherProviderType in weatherProviderTypeSet) {
            val requestWeatherSourceArr: ArrayMap<ServiceType, MultipleWeatherRestApiCallback.ResponseResult> =
                arrayMap[weatherProviderType]!!
            if (weatherProviderType === WeatherProviderType.OWM_ONECALL) {
                val owmJsonObject = JsonObject()
                text = requestWeatherSourceArr[RetrofitClient.ServiceType.OWM_ONE_CALL].getResponseText()
                owmJsonObject.addProperty(RetrofitClient.ServiceType.OWM_ONE_CALL.name, text)
                rootJsonObject.add(weatherProviderType.name, owmJsonObject)
            } else if (weatherProviderType === WeatherProviderType.MET_NORWAY) {
                val metNorwayJsonObject = JsonObject()
                text = requestWeatherSourceArr[RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST].getResponseText()
                metNorwayJsonObject.addProperty(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST.name, text)
                rootJsonObject.add(weatherProviderType.name, metNorwayJsonObject)
            } else if (weatherProviderType === WeatherProviderType.OWM_INDIVIDUAL) {
                val owmIndividualJsonObject = JsonObject()
                if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS].getResponseText()
                    owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS.name(), text)
                }
                if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.OWM_HOURLY_FORECAST].getResponseText()
                    owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST.name(), text)
                }
                if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.OWM_DAILY_FORECAST].getResponseText()
                    owmIndividualJsonObject.addProperty(RetrofitClient.ServiceType.OWM_DAILY_FORECAST.name(), text)
                }
                rootJsonObject.add(weatherProviderType.name, owmIndividualJsonObject)
            } else if (weatherProviderType === WeatherProviderType.KMA_API) {
                val kmaJsonObject = JsonObject()
                if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST].getResponseText()
                    kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST.name(), text)
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST].getResponseText()
                    kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name(), text)
                }
                if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
                    if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name())) {
                        text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST].getResponseText()
                        kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name(), text)
                    }
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_VILAGE_FCST].getResponseText()
                    kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name(), text)
                }
                if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_MID_TA_FCST].getResponseText()
                    kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_MID_TA_FCST.name(), text)
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_MID_LAND_FCST].getResponseText()
                    kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_MID_LAND_FCST.name(), text)
                    if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name())) {
                        text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST].getResponseText()
                        kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name(), text)
                    }
                    if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name())) {
                        text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_VILAGE_FCST].getResponseText()
                        kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name(), text)
                    }
                    val tmFc: Long = multipleWeatherRestApiCallback.getValue("tmFc").toLong()
                    kmaJsonObject.addProperty("tmFc", tmFc)
                }
                rootJsonObject.add(weatherProviderType.name, kmaJsonObject)
            } else if (weatherProviderType === WeatherProviderType.KMA_WEB) {
                val kmaJsonObject = JsonObject()
                if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS].getResponseText()
                    kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS.name, text)
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_WEB_FORECASTS].getResponseText()
                    kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name, text)
                }
                if (!kmaJsonObject.has(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name)) {
                    if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast) || weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                        text = requestWeatherSourceArr[RetrofitClient.ServiceType.KMA_WEB_FORECASTS].getResponseText()
                        kmaJsonObject.addProperty(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name, text)
                    }
                }
                rootJsonObject.add(weatherProviderType.name, kmaJsonObject)
            } else if (weatherProviderType === WeatherProviderType.ACCU_WEATHER) {
                val accuJsonObject = JsonObject()
                if (weatherDataTypeSet.contains(WeatherDataType.currentConditions)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS].getResponseText()
                    accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS.name(), text)
                }
                if (weatherDataTypeSet.contains(WeatherDataType.hourlyForecast)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST].getResponseText()
                    accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST.name(), text)
                }
                if (weatherDataTypeSet.contains(WeatherDataType.dailyForecast)) {
                    text = requestWeatherSourceArr[RetrofitClient.ServiceType.ACCU_DAILY_FORECAST].getResponseText()
                    accuJsonObject.addProperty(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST.name(), text)
                }
                rootJsonObject.add(weatherProviderType.name, accuJsonObject)
            } else if (weatherProviderType === WeatherProviderType.AQICN) {
                val aqiCnJsonObject = JsonObject()
                if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
                    text = arrayMap[WeatherProviderType.AQICN]!![RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED].getResponseText()
                    if (text != null && text != "{}") {
                        aqiCnJsonObject.addProperty(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name, text)
                    }
                }
                rootJsonObject.add(weatherProviderType.name, aqiCnJsonObject)
            }
        }
        if (zoneOffset != null) {
            rootJsonObject.addProperty("zoneOffset", zoneOffset.id)
        }
        rootJsonObject.addProperty("lastUpdatedDateTime", multipleWeatherRestApiCallback.getRequestDateTime().toString())
        widgetDto.responseText = rootJsonObject.toString()
    }

    protected fun drawBitmap(
        rootLayout: ViewGroup, onDrawBitmapCallback: OnDrawBitmapCallback?, remoteViews: RemoteViews,
        parentWidth: Int?, parentHeight: Int?
    ) {
        var widthSize = 0
        var heightSize = 0
        if (parentWidth == null || parentHeight == null) {
            val widgetSize = getWidgetExactSizeInPx(AppWidgetManager.getInstance(context))
            widthSize = widgetSize[0]
            heightSize = widgetSize[1]
        } else {
            widthSize = parentWidth
            heightSize = parentHeight
        }
        val widgetPadding = context.resources.getDimension(R.dimen.widget_padding)
        val widthSpec = View.MeasureSpec.makeMeasureSpec((widthSize - widgetPadding * 2).toInt(), View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec((heightSize - widgetPadding * 2).toInt(), View.MeasureSpec.EXACTLY)
        rootLayout.measure(widthSpec, heightSpec)
        val viewBmp = drawBitmap(rootLayout, remoteViews)
        if (onDrawBitmapCallback != null) {
            onDrawBitmapCallback.onCreatedBitmap(viewBmp)
        }
    }

    protected fun drawBitmap(rootLayout: ViewGroup, remoteViews: RemoteViews): Bitmap {
        rootLayout.layout(0, 0, rootLayout.measuredWidth, rootLayout.measuredHeight)
        rootLayout.isDrawingCacheEnabled = false
        rootLayout.destroyDrawingCache()
        rootLayout.isDrawingCacheEnabled = true
        rootLayout.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        rootLayout.buildDrawingCache()
        val viewBmp = rootLayout.drawingCache
        remoteViews.setImageViewBitmap(R.id.bitmapValuesView, viewBmp)
        return viewBmp
    }

    protected fun createBaseRemoteViews(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.view_widget)
    }

    open fun setResultViews(
        appWidgetId: Int,
        multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback?,
        zoneId: ZoneId?
    ) {
        if (!widgetDto.isInitialized) widgetDto.isInitialized = true
        if (widgetDto.isLoadSuccessful) {
            widgetDto.lastErrorType = null
        } else {
            if (widgetDto.lastErrorType == null) widgetDto.lastErrorType = RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA
        }
        widgetRepository.update(widgetDto, object : DbQueryCallback<WidgetDto?>() {
            fun onResultSuccessful(result: WidgetDto) {
                remoteViewsCallback!!.onResult(result.appWidgetId, createRemoteViews())
            }

            fun onResultNoData() {}
        })
    }

    val headerViewLayoutParams: RelativeLayout.LayoutParams
        get() {
            val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.bottomMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f,
                context.resources.displayMetrics
            ).toInt()
            return layoutParams
        }

    protected fun makeHeaderViews(layoutInflater: LayoutInflater, addressName: String?, lastRefreshDateTime: String?): View {
        val view = layoutInflater.inflate(R.layout.header_view_in_widget, null, false)
        (view.findViewById<View>(R.id.address) as TextView).text = addressName
        (view.findViewById<View>(R.id.refresh) as TextView).text =
            ZonedDateTime.parse(lastRefreshDateTime).format(refreshDateTimeFormatter)
        return view
    }

    fun createRemoteViews(): RemoteViews {
        val remoteViews = createBaseRemoteViews()
        remoteViews.setOnClickPendingIntent(R.id.root_layout, onClickedPendingIntent)
        return remoteViews
    }

    abstract fun widgetProviderClass(): Class<*>
    abstract fun setDisplayClock(displayClock: Boolean)
    abstract fun setDataViewsOfSavedData()
    abstract val requestWeatherDataTypeSet: Set<Any?>
    fun getWidgetDto(): WidgetDto? {
        return widgetDto
    }

    interface WidgetUpdateCallback {
        fun updatePreview()
    }

    abstract class RemoteViewsCallback(private val requestCount: Int) {
        private val responseCount = AtomicInteger(0)
        private val remoteViewsMap: MutableMap<Int, RemoteViews?> = ConcurrentHashMap()
        fun onResult(id: Int, remoteViews: RemoteViews) {
            remoteViewsMap[id] = remoteViews
            if (responseCount.incrementAndGet() == requestCount) onFinished(remoteViewsMap)
        }

        protected abstract fun onFinished(remoteViewsMap: Map<Int, RemoteViews?>)
    }

    companion object {
        fun getInstance(appWidgetManager: AppWidgetManager, context: Context, appWidgetId: Int): AbstractWidgetCreator? {
            val appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val providerClassName = appWidgetProviderInfo.provider.className
            return if (providerClassName == FirstWidgetProvider::class.java.getName()) {
                FirstWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == SecondWidgetProvider::class.java.getName()) {
                SecondWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == ThirdWidgetProvider::class.java.getName()) {
                ThirdWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == FourthWidgetProvider::class.java.getName()) {
                FourthWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == FifthWidgetProvider::class.java.getName()) {
                FifthWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == SixthWidgetProvider::class.java.getName()) {
                SixthWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == SeventhWidgetProvider::class.java.getName()) {
                SeventhWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == EighthWidgetProvider::class.java.getName()) {
                EighthWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == NinthWidgetProvider::class.java.getName()) {
                NinthWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == TenthWidgetProvider::class.java.getName()) {
                TenthWidgetCreator(context, null, appWidgetId)
            } else if (providerClassName == EleventhWidgetProvider::class.java.getName()) {
                EleventhWidgetCreator(context, null, appWidgetId)
            } else {
                null
            }
        }
    }
}