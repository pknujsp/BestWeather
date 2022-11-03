package com.lifedawn.bestweather.commons.classes

import android.content.Context
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.enums.WeatherValueType

class WeatherValueLabels private constructor() {
    companion object {
        val weatherValueLabelsMap = HashMap<WeatherValueType, String>()

        fun load(context: Context) {
            // 돌풍, 기압, 이슬점, 운량, 시정, 자외선, 체감기온
            // 날씨, 기온, 강수량, 강수확률, 풍향, 풍속, 바람세기, 습도
            weatherValueLabelsMap[WeatherValueType.windGust] = context.getString(R.string.wind_gust);
            weatherValueLabelsMap[WeatherValueType.pressure] = context.getString(R.string.pressure);
            weatherValueLabelsMap[WeatherValueType.dewPoint] = context.getString(R.string.dew_point);
            weatherValueLabelsMap[WeatherValueType.cloudiness] = context.getString(R.string.cloud_cover);
            weatherValueLabelsMap[WeatherValueType.visibility] = context.getString(R.string.visibility);
            weatherValueLabelsMap[WeatherValueType.uvIndex] = context.getString(R.string.uv_index);
            weatherValueLabelsMap[WeatherValueType.feelsLikeTemp] = context.getString(R.string.feelsLike);
            weatherValueLabelsMap[WeatherValueType.weatherDescription] = context.getString(R.string.weather);
            weatherValueLabelsMap[WeatherValueType.temp] = context.getString(R.string.temperature);
            weatherValueLabelsMap[WeatherValueType.rainVolume] = context.getString(R.string.rain_volume);
            weatherValueLabelsMap[WeatherValueType.precipitationVolume] = context.getString(R.string.precipitation_volume);
            weatherValueLabelsMap[WeatherValueType.snowVolume] = context.getString(R.string.snow_volume);
            weatherValueLabelsMap[WeatherValueType.por] = context.getString(R.string.precipitation_of_rain);
            weatherValueLabelsMap[WeatherValueType.pos] = context.getString(R.string.precipitation_of_snow);
            weatherValueLabelsMap[WeatherValueType.pop] = context.getString(R.string.probability_of_precipitation);
            weatherValueLabelsMap[WeatherValueType.windDirection] = context.getString(R.string.wind_direction);
            weatherValueLabelsMap[WeatherValueType.windSpeed] = context.getString(R.string.wind_speed);
            weatherValueLabelsMap[WeatherValueType.windStrength] = context.getString(R.string.wind_strength);
            weatherValueLabelsMap[WeatherValueType.humidity] = context.getString(R.string.humidity);
            weatherValueLabelsMap[WeatherValueType.precipitationType] = context.getString(R.string.precipitation_type);
        }

        fun clear() = weatherValueLabelsMap.clear()
    }
}