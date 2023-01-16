package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util

import android.content.Context
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.commons.constants.ValueUnits.Companion.convertTemperature
import com.lifedawn.bestweather.data.MyApplication

object WeatherUtil {
    fun calcFeelsLikeTemperature(celsiusTemperature: Double, kmPerHWindSpeed: Double, humidity: Double): Double {
        return if (celsiusTemperature < 11.0) {
            /*
			- 겨울 체감온도 = 13.12 + 0.6215T - 11.37 V0.16 + 0.3965 V0.16T
			* T : 기온(℃), V : 풍속(km/h)
			 */
            if (kmPerHWindSpeed <= 4.68) {
                celsiusTemperature
            } else {
                13.12 + 0.6215 * celsiusTemperature - 11.37 * Math.pow(kmPerHWindSpeed, 0.16) + 0.3965 * Math.pow(
                    kmPerHWindSpeed, 0.16
                ) * celsiusTemperature
            }
        } else {
            /*
			- 여름 체감온도 = -0.2442 + 0.55399Tw + 0.45535Ta – 0.0022Tw2 + 0.00278TwTa + 3.5
			* Tw = Ta * ATAN[0.151977(RH+8.313659)1/2] + ATAN(Ta+RH) - ATAN(RH-1.67633) + 0.00391838 * RH * 3/2 * ATAN(0.023101RH) - 4.686035
			** Ta : 기온(℃), Tw : 습구온도(Stull의 추정식** 이용), RH : 상대습도(%)
			 */
            val tw = celsiusTemperature * Math.atan(
                Math.abs(
                    0.151977 * Math.pow(
                        humidity + 8.313659,
                        0.5
                    )
                )
            ) + Math.atan(celsiusTemperature + humidity)
            -Math.atan(humidity - 1.67633) + 0.00391838 * Math.pow(
                humidity,
                1.5
            ) * Math.atan(0.023101 * humidity) - 4.686035
            -0.2442 + 0.55399 * tw + 0.45535 * celsiusTemperature - 0.0022 * Math.pow(
                tw,
                2.0
            ) + 0.00278 * tw * celsiusTemperature + 3.5
        }
    }

    @JvmStatic
    fun makeTempCompareToYesterdayText(currentTempText: String, yesterdayTemp: String, tempUnit: ValueUnits?, context: Context): String {
        val tempUnitStr = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val yesterdayTempVal = convertTemperature(
            yesterdayTemp.replace(
                tempUnitStr,
                ""
            ), tempUnit!!
        )
        val todayTempVal = currentTempText.replace(tempUnitStr, "").toInt()
        return if (yesterdayTempVal == todayTempVal) {
            context.getString(R.string.TheTemperatureIsTheSameAsYesterday)
        } else {
            var text: String? = null
            text = if (todayTempVal > yesterdayTempVal) {
                if (MyApplication.getLocaleCountryCode() == "KR") {
                    (context.getString(R.string.thanYesterday) + " " + (todayTempVal - yesterdayTempVal) + tempUnitStr
                            + " " + context.getString(R.string.higherTemperature))
                } else {
                    ((todayTempVal - yesterdayTempVal).toString() + tempUnitStr
                            + " " + context.getString(R.string.higherTemperature) + " " + context.getString(R.string.thanYesterday))
                }
            } else {
                if (MyApplication.getLocaleCountryCode() == "KR") {
                    (context.getString(R.string.thanYesterday) + " " + (yesterdayTempVal - todayTempVal) + tempUnitStr
                            + " " + context.getString(R.string.lowerTemperature))
                } else {
                    ((todayTempVal - yesterdayTempVal).toString() + tempUnitStr
                            + " " + context.getString(R.string.lowerTemperature) + " " + context.getString(R.string.thanYesterday))
                }
            }
            text
        }
    }
}