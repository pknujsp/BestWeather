package com.lifedawn.bestweather.ui.weathers.simple.currentconditions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasPrecipitationVolume
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor.getGradeDescription
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.LocationDistance
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.LocationDistance.distance
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherUtil.makeTempCompareToYesterdayText
import com.lifedawn.bestweather.ui.weathers.simple.base.BaseSimpleCurrentConditionsFragment

class SimpleCurrentConditionsFragment : BaseSimpleCurrentConditionsFragment() {
    fun setAirQualityDto(airQualityDto: AirQualityDto): SimpleCurrentConditionsFragment {
        this.airQualityDto = airQualityDto
        return this
    }

    fun setCurrentConditionsDto(currentConditionsDto: CurrentConditionsDto): SimpleCurrentConditionsFragment {
        this.currentConditionsDto = currentConditionsDto
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onInflateFinished(view: View, resid: Int, parent: ViewGroup?) {
        super.onInflateFinished(view, resid, parent)
        setValuesToViews()
    }

    override fun setValuesToViews() {
        if (currentConditionsDto.isHasPrecipitationVolume()) {
            val precipitation = getString(R.string.precipitation_volume) + " : " + currentConditionsDto!!.precipitationVolume
            binding!!.precipitation.text = precipitation
        } else {
            binding!!.precipitation.visibility = View.GONE
        }
        if (currentConditionsDto!!.windDirection != null) {
            binding!!.windDirectionArrow.rotation =
                (currentConditionsDto.windDirectionDegree + 180).toFloat()
        }
        binding!!.windDirectionArrow.visibility =
            if (currentConditionsDto.windDirection == null) View.GONE else View.VISIBLE
        binding!!.windDirection.visibility =
            if (currentConditionsDto.windDirection == null) View.GONE else View.VISIBLE
        Glide.with(binding!!.weatherIcon).load(currentConditionsDto.getWeatherIcon()).into(binding!!.weatherIcon)
        binding!!.sky.text = currentConditionsDto.weatherDescription
        binding!!.wind.text =
            if (currentConditionsDto.windStrength != null) currentConditionsDto.windStrength else getString(
                R.string.noWindData
            )
        binding!!.windDirection.text = currentConditionsDto.windDirection
        binding!!.humidity.text = String.format(
            "%s %s",
            getString(R.string.humidity),
            currentConditionsDto.humidity
        )
        val tempUnitStr = MyApplication.VALUE_UNIT_OBJ.tempUnitText
        val currentTempText = currentConditionsDto.temp.replace(tempUnitStr, "")
        binding!!.temperature.text = currentTempText
        binding!!.tempUnit.text = tempUnitStr
        binding!!.feelsLikeTemp.text = currentConditionsDto.feelsLikeTemp.replace(tempUnitStr, "")
        binding!!.feelsLikeTempUnit.text = tempUnitStr
        if (currentConditionsDto.getYesterdayTemp() != null) {
            binding!!.tempDescription.text = makeTempCompareToYesterdayText(
                currentConditionsDto.temp,
                currentConditionsDto.getYesterdayTemp(), tempUnit, requireContext().applicationContext
            )
            binding!!.tempDescription.visibility = View.VISIBLE
        } else {
            binding!!.tempDescription.visibility = View.GONE
        }
        var airQuality: String? = null
        airQuality = if (airQualityDto.isSuccessful()) {
            val distance = distance(
                latitude!!,
                longitude!!,
                airQualityDto!!.latitude,
                airQualityDto.longitude,
                LocationDistance.Unit.KM
            )
            if (distance > 100.0) {
                getString(R.string.noData)
            } else {
                getGradeDescription(airQualityDto.aqi)
            }
        } else {
            getString(R.string.noData)
        }
        binding!!.airQuality.text = airQuality
    }

    companion object {
        private val currentConditionsDto: CurrentConditionsDto? = null
        private val airQualityDto: AirQualityDto? = null
    }
}