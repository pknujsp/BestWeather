package com.lifedawn.bestweather.ui.theme

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import com.lifedawn.bestweather.ui.weathers.enums.WeatherDataType

object AppTheme {
    fun getColor(context: Context, id: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(id, value, true)
        return value.data
    }

    @JvmStatic
    fun getTextColor(weatherDataType: WeatherDataType?): Int {
        return when (weatherDataType) {
            WeatherDataType.Detail, WeatherDataType.Comparison -> Color.BLACK
            else -> Color.WHITE
        }
    }
}