package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.dailyforecast

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Coord
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.FeelsLike
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Temp
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Weather

class OwmDailyForecastResponse {
    @Expose @SerializedName("city") var city: City? = null
    @Expose @SerializedName("cod") var cod: String? = null
    @Expose @SerializedName("message") var message: String? = null
    @Expose @SerializedName("cnt") var cnt: String? = null
    @Expose @SerializedName("list") var list: List<Item>? = null

    class City {
        @Expose @SerializedName("id") var id: String? = null
        @Expose @SerializedName("name") var name: String? = null
        @Expose @SerializedName("country") var country: String? = null
        @Expose @SerializedName("population") var population: String? = null
        @Expose @SerializedName("timezone") var timezone: String? = null
        @Expose @SerializedName("coord") var coord: Coord? = null
    }

    class Item {
        @Expose @SerializedName("dt") var dt: String? = null
        @Expose @SerializedName("pressure") var pressure: String? = null
        @Expose @SerializedName("humidity") var humidity: String? = null
        @Expose @SerializedName("speed") var speed: String? = null
        @Expose @SerializedName("deg") var deg: String? = null
        @Expose @SerializedName("gust") var gust: String? = null
        @Expose @SerializedName("clouds") var clouds: String? = null
        @Expose @SerializedName("pop") var pop: String? = null
        @Expose @SerializedName("rain") var rain: String? = null
        @Expose @SerializedName("snow") var snow: String? = null
        @Expose @SerializedName("temp") var temp: Temp? = null
        @Expose @SerializedName("feels_like") var feelsLike: FeelsLike? = null
        @Expose @SerializedName("weather") var weather: List<Weather>? = null
    }
}