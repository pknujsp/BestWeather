package com.lifedawn.bestweather.commons.constants

import java.util.*

enum class ValueUnits(
    val text: String
) {
    celsius("℃"), fahrenheit("℉"), mPerSec("m/s"), kmPerHour("km/h"), km("km"), mile("mile"), clock12("3:00 PM"), clock24("15:00"),
    undefined("-");


    companion object {
        /*
	accu weather 기본 단위
	바람 : km/h, 비 : mm, 눈 : cm, 기온 : C, 기압 : mb

	owm
	바람 : m/s, 비 : mm, 눈 : mm, 기온 : C, 기압 : mb

	기상청
	바람 : m/s, 비 : mm, 눈 : mm, 기온 : C
	 */


        @JvmStatic
        fun convertTemperature(`val`: String, unit: ValueUnits): Int {
            var floatTemp = 0f
            floatTemp = try {
                `val`.toFloat()
            } catch (e: Exception) {
                return 999
            }
            var convertedVal = Math.round(floatTemp)
            if (unit == fahrenheit) {
                //화씨 (1℃ × 9/5) + 32℉
                convertedVal = Math.round(convertedVal * (9.0 / 5.0) + 32).toInt()
            }
            return convertedVal
        }

        fun convertWindSpeed(value: String, unit: ValueUnits): Double {
            var convertedVal = 0f
            try {
                convertedVal = value.toFloat()
            } catch (e: Exception) {
            }
            if (unit == kmPerHour) {
                //m/s -> km/h n x 3.6 = c
                convertedVal = convertedVal * 3.6f
            }
            return Math.round(convertedVal * 10) / 10.0
        }

        fun convertWindSpeedForAccu(value: String, unit: ValueUnits): Double {
            var convertedVal = 0f
            try {
                convertedVal = value.toFloat()
            } catch (e: Exception) {
            }
            if (unit == mPerSec) {
                //m/s -> km/h n x 3.6 = c
                convertedVal = convertedVal / 3.6f
            }
            return Math.round(convertedVal * 10) / 10.0
        }

        fun convertVisibility(value: String, unit: ValueUnits): String {
            var convertedVal = 0f
            try {
                convertedVal = value.toFloat() / 1000f
            } catch (e: Exception) {
            }
            if (unit == mile) {
                //km -> mile  n / 1.609 = c
                convertedVal = convertedVal / 1.609f
            }
            return String.format(Locale.getDefault(), "%.1f", convertedVal)
        }

        fun CMToMM(`val`: String): Double {
            return `val`.toDouble() * 100 / 10.0
        }

        fun MMToCM(`val`: String): Double {
            return `val`.toDouble() / 10.0
        }
    }
}