package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.hourlyforecast

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Coord
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Weather

class OwmHourlyForecastResponse {
    @Expose @SerializedName("cod") var cod: String? = null
    @Expose @SerializedName("message") var message: String? = null
    @Expose @SerializedName("cnt") var cnt: String? = null
    @Expose @SerializedName("list") var list: List<Item>? = null
    @Expose @SerializedName("city") var city: City? = null

    class Item {
        @Expose @SerializedName("visibility") var visibility: String? = null
        @Expose @SerializedName("dt") var dt: String? = null
        @Expose @SerializedName("pop") var pop: String? = null
        @Expose @SerializedName("weather") var weather: List<Weather>? = null
        @Expose @SerializedName("main") var main: Main? = null
        @Expose @SerializedName("wind") var wind: Wind? = null
        @Expose @SerializedName("clouds") var clouds: Clouds? = null
        @Expose @SerializedName("rain") var rain: Rain? = null
        @Expose @SerializedName("snow") var snow: Snow? = null
        @Expose @SerializedName("sys") var sys: Sys? = null
        @Expose @SerializedName("dt_txt") var dtTxt: String? = null

        class Rain {
            @Expose @SerializedName("1h") var rainVolumeOneHour: String? = null
        }

        class Snow {
            @Expose @SerializedName("1h") var snowVolumeOneHour: String? = null
        }

        class Main {
            @Expose @SerializedName("temp") var temp: String? = null
            @Expose @SerializedName("feels_like") var feels_like: String? = null
            @Expose @SerializedName("temp_min") var temp_min: String? = null
            @Expose @SerializedName("temp_max") var temp_max: String? = null
            @Expose @SerializedName("pressure") var pressure: String? = null
            @Expose @SerializedName("sea_level") var seaLevel: String? = null
            @Expose @SerializedName("grnd_level") var grndLevel: String? = null
            @Expose @SerializedName("humidity") var humidity: String? = null
            @Expose @SerializedName("temp_kf") var tempKf: String? = null
        }

        class Wind {
            @Expose @SerializedName("speed") var speed: String? = null
            @Expose @SerializedName("deg") var deg: String? = null
            @Expose @SerializedName("gust") var gust: String? = null
        }

        class Clouds {
            @Expose @SerializedName("all") var all: String? = null
        }

        class Sys {
            @Expose @SerializedName("pod") var pod: String? = null
        }
    }

    class City {
        @Expose @SerializedName("id") var id: String? = null
        @Expose @SerializedName("name") var name: String? = null
        @Expose @SerializedName("country") var country: String? = null
        @Expose @SerializedName("timezone") var timezone: String? = null
        @Expose @SerializedName("coord") var coord: Coord? = null
    }
}