package com.lifedawn.bestweather.commons.classes

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.github.matteobattilana.weather.PrecipType
import com.github.matteobattilana.weather.WeatherView

class WeatherViewController(private var weatherView: WeatherView?) : DefaultLifecycleObserver {
    fun setWeatherView(precipType: PrecipType, volume: String?) {
        weatherView!!.setWeatherData(precipType)
        if (precipType === PrecipType.CLEAR) {
        } else {
            // mm단위
            var volumeValue = 0
            if (volume == null) {
            } else {
                if (volume.contains("mm") || volume.contains("cm")) {
                    val intStr = volume.replace("[^0-9]".toRegex(), "")
                    volumeValue = intStr.toInt()
                    if (volume.contains("cm")) {
                        volumeValue = volumeValue * 100 / 10
                    }
                }
            }
            val originalEr = weatherView!!.emissionRate
            var amount = 0f
            if (precipType === PrecipType.RAIN) {
                /*
			‘약한 비’는 1시간에 3㎜ 미만
			‘(보통) 비’는 1시간에 3∼15㎜
			‘강한 비’는 1시간에 15㎜ 이상
			‘매우 강한 비’는 1시간에 30㎜ 이상
				 */
                amount = if (volumeValue >= 30) {
                    1.3f
                } else if (volumeValue >= 15) {
                    1.2f
                } else if (volumeValue >= 3) {
                    0.9f
                } else {
                    0.7f
                }
                weatherView!!.emissionRate = originalEr * amount
            } else if (precipType === PrecipType.SNOW) {
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onCreate(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onStart(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onDestroy(owner)
        weatherView = null
    }
}