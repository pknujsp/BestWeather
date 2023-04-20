package com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.onecall

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.FeelsLike
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Temp
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.Weather

class OwmOneCallResponse {
    @Expose @SerializedName("lat") var latitude: String? = null
    @Expose @SerializedName("lon") var longitude: String? = null
    @Expose @SerializedName("timezone") var timezone: String? = null
    @Expose @SerializedName("timezone_offset") var timezoneOffset: String? = null
    @Expose @SerializedName("current") var current: Current? = null
    @Expose @SerializedName("minutely") var minutely: List<Minutely>? = null
    @Expose @SerializedName("hourly") var hourly: List<Hourly>? = null
    @Expose @SerializedName("daily") var daily: List<Daily>? = null

    open class Current {
        @Expose @SerializedName("dt") var dt: String? = null
        @Expose @SerializedName("temp") var temp: String? = null
        @Expose @SerializedName("feels_like") var feelsLike: String? = null
        @Expose @SerializedName("pressure") var pressure: String? = null
        @Expose @SerializedName("humidity") var humidity: String? = null
        @Expose @SerializedName("dew_point") var dewPoint: String? = null
        @Expose @SerializedName("uvi") var uvi: String? = null
        @Expose @SerializedName("clouds") var clouds: String? = null
        @Expose @SerializedName("visibility") var visibility: String? = null
        @Expose @SerializedName("wind_speed") var wind_speed: String? = null
        @Expose @SerializedName("wind_gust") var windGust: String? = null
        @Expose @SerializedName("wind_deg") var wind_deg: String? = null
        @Expose @SerializedName("rain") var rain: Rain? = null
        @Expose @SerializedName("snow") var snow: Snow? = null
        @Expose @SerializedName("weather") var weather: List<Weather>? = null
    }

    class Minutely {
        @Expose @SerializedName("dt") var dt: String? = null
        @Expose @SerializedName("precipitation") var precipitation: String? = null
    }

    class Hourly : Current() {
        @Expose @SerializedName("pop") var pop: String? = null
    }

    class Daily {
        @Expose @SerializedName("dt") var dt: String? = null
        @Expose @SerializedName("temp") var temp: Temp? = null
        @Expose @SerializedName("feels_like") var feelsLike: FeelsLike? = null
        @Expose @SerializedName("pressure") var pressure: String? = null
        @Expose @SerializedName("humidity") var humidity: String? = null
        @Expose @SerializedName("dew_point") var dew_point: String? = null
        @Expose @SerializedName("wind_speed") var windSpeed: String? = null
        @Expose @SerializedName("wind_deg") var windDeg: String? = null
        @Expose @SerializedName("wind_gust") var windGust: String? = null
        @Expose @SerializedName("weather") var weather: List<Weather>? = null
        @Expose @SerializedName("clouds") var clouds: String? = null
        @Expose @SerializedName("pop") var pop: String? = null
        @Expose @SerializedName("rain") var rain: String? = null
        @Expose @SerializedName("snow") var snow: String? = null
        @Expose @SerializedName("uvi") var uvi: String? = null
    }

    class Rain {
        @Expose @SerializedName("1h") var precipitation1Hour: String? = null
    }

    class Snow {
        @Expose @SerializedName("1h") var precipitation1Hour: String? = null
    }
}