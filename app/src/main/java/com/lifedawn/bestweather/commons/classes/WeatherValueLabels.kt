package com.lifedawn.bestweather.commons.classes

import android.content.Context
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.WeatherValueType
import javax.inject.Inject

class WeatherValueLabels @Inject constructor(context: Context) {
    private val _weatherValueLabelsMap = mutableMapOf<WeatherValueType, String>()
    val weatherValueLabelsMap get() = _weatherValueLabelsMap.toMap()

    init {
        // 돌풍, 기압, 이슬점, 운량, 시정, 자외선, 체감기온
        // 날씨, 기온, 강수량, 강수확률, 풍향, 풍속, 바람세기, 습도
        _weatherValueLabelsMap[WeatherValueType.windGust] = context.getString(R.string.wind_gust)
        _weatherValueLabelsMap[WeatherValueType.pressure] = context.getString(R.string.pressure)
        _weatherValueLabelsMap[WeatherValueType.dewPoint] = context.getString(R.string.dew_point)
        _weatherValueLabelsMap[WeatherValueType.cloudiness] = context.getString(R.string.cloud_cover)
        _weatherValueLabelsMap[WeatherValueType.visibility] = context.getString(R.string.visibility)
        _weatherValueLabelsMap[WeatherValueType.uvIndex] = context.getString(R.string.uv_index)
        _weatherValueLabelsMap[WeatherValueType.feelsLikeTemp] = context.getString(R.string.feelsLike)
        _weatherValueLabelsMap[WeatherValueType.weatherDescription] = context.getString(R.string.weather)
        _weatherValueLabelsMap[WeatherValueType.temp] = context.getString(R.string.temperature)
        _weatherValueLabelsMap[WeatherValueType.rainVolume] = context.getString(R.string.rain_volume)
        _weatherValueLabelsMap[WeatherValueType.precipitationVolume] = context.getString(R.string.precipitation_volume)
        _weatherValueLabelsMap[WeatherValueType.snowVolume] = context.getString(R.string.snow_volume)
        _weatherValueLabelsMap[WeatherValueType.por] = context.getString(R.string.precipitation_of_rain)
        _weatherValueLabelsMap[WeatherValueType.pos] = context.getString(R.string.precipitation_of_snow)
        _weatherValueLabelsMap[WeatherValueType.pop] = context.getString(R.string.probability_of_precipitation)
        _weatherValueLabelsMap[WeatherValueType.windDirection] = context.getString(R.string.wind_direction)
        _weatherValueLabelsMap[WeatherValueType.windSpeed] = context.getString(R.string.wind_speed)
        _weatherValueLabelsMap[WeatherValueType.windStrength] = context.getString(R.string.wind_strength)
        _weatherValueLabelsMap[WeatherValueType.humidity] = context.getString(R.string.humidity)
        _weatherValueLabelsMap[WeatherValueType.precipitationType] = context.getString(R.string.precipitation_type)
    }

    fun clear() = _weatherValueLabelsMap.clear()
}