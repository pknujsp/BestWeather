package com.lifedawn.bestweather.ui.weathers.detail.currentconditions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasPrecipitationVolume
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasRainVolume
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto.Values.isHasSnowVolume
import com.lifedawn.bestweather.ui.weathers.detail.base.BaseDetailCurrentConditionsFragment

class DetailCurrentConditionsFragment : BaseDetailCurrentConditionsFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentConditionsDto = bundle!!.getSerializable(WeatherDataType.currentConditions.name) as CurrentConditionsDto?
        setValuesToViews()
    }

    override fun onResume() {
        super.onResume()
        weatherFragmentViewModel!!.onResumeWithAsync(this)
    }

    override fun setValuesToViews() {
        binding!!.conditionsGrid.removeAllViews()
        if (currentConditionsDto!!.weatherDescription != null) {
            addGridItem(
                R.string.weather, currentConditionsDto!!.weatherDescription,
                currentConditionsDto.getWeatherIcon(), true
            )
        }
        if (currentConditionsDto!!.precipitationType != null) {
            addGridItem(R.string.precipitation_type, currentConditionsDto!!.precipitationType, null)
        }
        if (currentConditionsDto.isHasPrecipitationVolume()) {
            addGridItem(R.string.precipitation_volume_of_grid, currentConditionsDto!!.precipitationVolume, null)
        }
        if (currentConditionsDto.isHasRainVolume()) {
            addGridItem(R.string.rain_volume_of_grid, currentConditionsDto!!.rainVolume, null)
        }
        if (currentConditionsDto.isHasSnowVolume()) {
            addGridItem(R.string.snow_volume_of_grid, currentConditionsDto!!.snowVolume, null)
        }
        if (currentConditionsDto!!.temp != null) {
            addGridItem(
                R.string.temperature, currentConditionsDto!!.temp,
                null
            )
        }
        if (currentConditionsDto!!.feelsLikeTemp != null) {
            addGridItem(
                R.string.wind_chill_temperature_of_grid, currentConditionsDto!!.feelsLikeTemp,
                null
            )
        }
        if (currentConditionsDto!!.humidity != null) {
            addGridItem(R.string.humidity, currentConditionsDto!!.humidity, null)
        }
        if (currentConditionsDto!!.windDirection != null) {
            val windDirectionView = addGridItem(
                R.string.wind_direction, currentConditionsDto!!.windDirection,
                R.drawable.arrow, false
            )
            (windDirectionView.findViewById<View>(R.id.label_icon) as ImageView).rotation =
                (currentConditionsDto!!.windDirectionDegree + 180).toFloat()
        }
        if (currentConditionsDto!!.windSpeed != null) {
            addGridItem(
                R.string.wind_speed,
                currentConditionsDto!!.windSpeed, null
            )
        }
        if (currentConditionsDto!!.windGust != null) {
            addGridItem(
                R.string.wind_gust,
                if (currentConditionsDto!!.windGust == null) getString(R.string.not_available) else currentConditionsDto!!.windGust, null
            )
        }
        if (currentConditionsDto!!.windStrength != null) {
            addGridItem(R.string.wind_strength, currentConditionsDto!!.simpleWindStrength, null)
        }
        if (currentConditionsDto!!.pressure != null) {
            addGridItem(R.string.pressure, currentConditionsDto!!.pressure, null)
        }
        if (currentConditionsDto!!.visibility != null) {
            addGridItem(R.string.visibility, currentConditionsDto!!.visibility, null)
        }
        if (currentConditionsDto!!.cloudiness != null) {
            addGridItem(R.string.cloud_cover, currentConditionsDto!!.cloudiness, null)
        }
        if (currentConditionsDto!!.dewPoint != null) {
            addGridItem(R.string.dew_point, currentConditionsDto!!.dewPoint, null)
        }
        if (currentConditionsDto!!.uvIndex != null) {
            addGridItem(R.string.uv_index, currentConditionsDto!!.uvIndex, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentConditionsDto = null
    }

    fun setCurrentConditionsDto(currentConditionsDto: CurrentConditionsDto) {
        this.currentConditionsDto = currentConditionsDto
    }

    companion object {
        private var currentConditionsDto: CurrentConditionsDto? = null
    }
}