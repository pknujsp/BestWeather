package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util

import android.content.Context
import com.lifedawn.bestweather.R
import javax.inject.Inject

class WindUtil @Inject constructor(context: Context) {
    private val windStrengthDescriptionMap = mutableMapOf<String, String>()
    private val windStrengthDescriptionSimpleMap = mutableMapOf<String, String>()

    init {
        if (windStrengthDescriptionMap.isEmpty() || windStrengthDescriptionSimpleMap.isEmpty()) {
            windStrengthDescriptionMap["1"] = context.getString(R.string.wind_strength_1)
            windStrengthDescriptionMap["2"] = context.getString(R.string.wind_strength_2)
            windStrengthDescriptionMap["3"] = context.getString(R.string.wind_strength_3)
            windStrengthDescriptionMap["4"] = context.getString(R.string.wind_strength_4)
            windStrengthDescriptionSimpleMap["1"] = context.getString(R.string.wind_strength_1_simple)
            windStrengthDescriptionSimpleMap["2"] = context.getString(R.string.wind_strength_2_simple)
            windStrengthDescriptionSimpleMap["3"] = context.getString(R.string.wind_strength_3_simple)
            windStrengthDescriptionSimpleMap["4"] = context.getString(R.string.wind_strength_4_simple)
        }
    }

    fun getWindSpeedDescription(windSpeed: String): String? {
        val speed = windSpeed.toDouble()
        return if (speed >= 14) {
            windStrengthDescriptionMap["4"]
        } else if (speed >= 9) {
            windStrengthDescriptionMap["3"]
        } else if (speed >= 4) {
            windStrengthDescriptionMap["2"]
        } else {
            windStrengthDescriptionMap["1"]
        }
    }

    fun getSimpleWindSpeedDescription(windSpeed: String): String? {
        val speed = windSpeed.toDouble()
        return if (speed >= 14) {
            windStrengthDescriptionSimpleMap["4"]
        } else if (speed >= 9) {
            windStrengthDescriptionSimpleMap["3"]
        } else if (speed >= 4) {
            windStrengthDescriptionSimpleMap["2"]
        } else {
            windStrengthDescriptionSimpleMap["1"]
        }
    }

    fun parseWindDirectionDegreeAsStr(context: Context, degree: String): String = when (((degree.toDouble() + 22.5 * 0.5) / 22.5).toInt()) {
        1 -> context.getString(R.string.wind_direction_NNE)
        2 -> context.getString(R.string.wind_direction_NE)
        3 -> context.getString(R.string.wind_direction_ENE)
        4 -> context.getString(R.string.wind_direction_E)
        5 -> context.getString(R.string.wind_direction_ESE)
        6 -> context.getString(R.string.wind_direction_SE)
        7 -> context.getString(R.string.wind_direction_SSE)
        8 -> context.getString(R.string.wind_direction_S)
        9 -> context.getString(R.string.wind_direction_SSW)
        10 -> context.getString(R.string.wind_direction_SW)
        11 -> context.getString(R.string.wind_direction_WSW)
        12 -> context.getString(R.string.wind_direction_W)
        13 -> context.getString(R.string.wind_direction_WNW)
        14 -> context.getString(R.string.wind_direction_NW)
        15 -> context.getString(R.string.wind_direction_NNW)
        16 -> context.getString(R.string.wind_direction_N)
        else -> ""
    }


    fun parseWindDirectionStrAsStr(context: Context, degree: String): String = when (degree) {
        "북북동" -> context.getString(R.string.wind_direction_NNE)
        "북동" -> context.getString(R.string.wind_direction_NE)
        "동북동" -> context.getString(R.string.wind_direction_ENE)
        "동" -> context.getString(R.string.wind_direction_E)
        "동남동" -> context.getString(R.string.wind_direction_ESE)
        "남동" -> context.getString(R.string.wind_direction_SE)
        "남남동" -> context.getString(R.string.wind_direction_SSE)
        "남" -> context.getString(R.string.wind_direction_S)
        "남남서" -> context.getString(R.string.wind_direction_SSW)
        "남서" -> context.getString(R.string.wind_direction_SW)
        "서남서" -> context.getString(R.string.wind_direction_WSW)
        "서" -> context.getString(R.string.wind_direction_W)
        "서북서" -> context.getString(R.string.wind_direction_WNW)
        "북서" -> context.getString(R.string.wind_direction_NW)
        "북북서" -> context.getString(R.string.wind_direction_NNW)
        else -> context.getString(R.string.wind_direction_N)
    }


    fun parseWindDirectionStrAsInt(degree: String): Int = when (degree) {
        "북북동" -> 25
        "북동" -> 45
        "동북동" -> 67
        "동" -> 90
        "동남동" -> 112
        "남동" -> 135
        "남남동" -> 157
        "남" -> 180
        "남남서" -> 202
        "남서" -> 225
        "서남서" -> 247
        "서" -> 270
        "서북서" -> 292
        "북서" -> 315
        "북북서" -> 337
        else -> -1
    }

}