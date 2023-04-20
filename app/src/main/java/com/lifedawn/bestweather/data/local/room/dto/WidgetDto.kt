package com.lifedawn.bestweather.data.local.room.dto

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.lifedawn.bestweather.commons.constants.LocationType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.ErrorType
import java.util.HashSet

@Entity(tableName = "widget_table")
class WidgetDto {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0
    @ColumnInfo(name = "appWidgetId") var appWidgetId = 0
    @ColumnInfo(name = "backgroundAlpha") var backgroundAlpha = 0
    @ColumnInfo(name = "displayClock") var isDisplayClock = false
    @ColumnInfo(name = "displayLocalClock") var isDisplayLocalClock = false
    @ColumnInfo(name = "locationType") var locationType: LocationType? = null

    @ColumnInfo(name = "weatherSourceTypes")
    private var weatherProviderTypeSet: MutableSet<WeatherProviderType>? = null
    @ColumnInfo(name = "topPriorityKma") var isTopPriorityKma = false
    @ColumnInfo(name = "selectedAddressDtoId") var selectedAddressDtoId = 0
    @ColumnInfo(name = "textSizeAmount") var textSizeAmount = 0
    @ColumnInfo(name = "addressName") var addressName: String? = null
    @ColumnInfo(name = "latitude") var latitude = 0.0
    @ColumnInfo(name = "longitude") var longitude = 0.0
    @ColumnInfo(name = "countryCode") var countryCode: String? = null
    @ColumnInfo(name = "timeZoneId") var timeZoneId: String? = null
    @ColumnInfo(name = "lastRefreshDateTime") var lastRefreshDateTime: String? = null
    @ColumnInfo(name = "loadSuccessful") var isLoadSuccessful = false
    @ColumnInfo(name = "responseText") var responseText: String? = null
    @ColumnInfo(name = "initialized") var isInitialized = false
    @ColumnInfo(name = "multipleWeatherDataSource") var isMultipleWeatherDataSource = false
    @ColumnInfo(name = "widgetProviderClassName") var widgetProviderClassName: String? = null
    @ColumnInfo(name = "lastErrorType") var lastErrorType: ErrorType? = null
    @ColumnInfo(name = "processing") var isProcessing = false
    fun addWeatherProviderType(newType: WeatherProviderType) {
        if (weatherProviderTypeSet == null) {
            weatherProviderTypeSet = HashSet()
        }
        weatherProviderTypeSet!!.add(newType)
    }

    fun removeWeatherSourceType(removeType: WeatherProviderType) {
        weatherProviderTypeSet!!.remove(removeType)
    }

    fun getWeatherProviderTypeSet(): Set<WeatherProviderType>? {
        if (countryCode != null) {
            if (isTopPriorityKma && countryCode == "KR") {
                if (!isMultipleWeatherDataSource) {
                    weatherProviderTypeSet!!.remove(WeatherProviderType.OWM_ONECALL)
                    weatherProviderTypeSet!!.remove(WeatherProviderType.MET_NORWAY)
                }
                weatherProviderTypeSet!!.add(WeatherProviderType.KMA_WEB)
            }
            if (countryCode != "KR" && isMultipleWeatherDataSource) {
                weatherProviderTypeSet!!.clear()
                weatherProviderTypeSet!!.add(WeatherProviderType.OWM_ONECALL)
                weatherProviderTypeSet!!.add(WeatherProviderType.MET_NORWAY)
            }
        }
        return weatherProviderTypeSet
    }

    fun setWeatherProviderTypeSet(weatherProviderTypeSet: MutableSet<WeatherProviderType>?) {
        this.weatherProviderTypeSet = weatherProviderTypeSet
    }
}