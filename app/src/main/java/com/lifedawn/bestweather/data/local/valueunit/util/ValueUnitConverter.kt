package com.lifedawn.bestweather.data.local.valueunit.util

import com.lifedawn.bestweather.data.local.valueunit.enums.ValueUnitType
import java.util.*
import kotlin.math.roundToInt

class ValueUnitConverter {
    /*
accu weather 기본 단위
바람 : km/h, 비 : mm, 눈 : cm, 기온 : C, 기압 : mb

owm
바람 : m/s, 비 : mm, 눈 : mm, 기온 : C, 기압 : mb

기상청
바람 : m/s, 비 : mm, 눈 : mm, 기온 : C
 */

    companion object {
        /**
         * valueUnitType값을 String으로 반환
         */
        fun toString(unitType: ValueUnitType) =
            when (unitType) {
                ValueUnitType.CELSIUS -> "℃"
                ValueUnitType.FAHRENHEIT -> "℉"
                ValueUnitType.M_PER_SEC -> "m/s"
                ValueUnitType.KM_PER_HOUR -> "km/h"
                ValueUnitType.MILE -> "mile"
                ValueUnitType.KM -> "km"
                else -> ""
            }

        fun convertTemperature(value: String, valueUnitType: ValueUnitType): Int {
            var floatTemp = 0f
            floatTemp = try {
                value.toFloat()
            } catch (e: Exception) {
                return 999
            }

            var convertedVal = floatTemp.roundToInt()
            if (valueUnitType == ValueUnitType.FAHRENHEIT) {
                //화씨 (1℃ × 9/5) + 32℉
                convertedVal = (convertedVal * (9.0 / 5.0) + 32).roundToInt().toInt()
            }
            return convertedVal
        }

        fun convertWindSpeed(value: String, valueUnitType: ValueUnitType): Double {
            var convertedVal = 0.0
            try {
                convertedVal = value.toDouble()
            } catch (e: java.lang.Exception) {
                return -1.0
            }
            if (valueUnitType == ValueUnitType.KM_PER_HOUR) {
                //m/s -> km/h n x 3.6 = c
                convertedVal *= 3.6
            }
            return (convertedVal * 10).roundToInt() / 10.0
        }

        fun convertVisibility(value: String, valueUnitType: ValueUnitType): String {
            var convertedVal = 0.0
            try {
                convertedVal = value.toDouble() / 1000.0
            } catch (e: java.lang.Exception) {
            }
            if (valueUnitType == ValueUnitType.MILE) {
                //km -> mile  n / 1.609 = c
                convertedVal /= 1.609
            }
            return String.format(Locale.getDefault(), "%.1f", convertedVal)
        }
    }
}