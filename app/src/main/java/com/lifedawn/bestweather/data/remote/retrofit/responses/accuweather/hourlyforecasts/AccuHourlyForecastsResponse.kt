package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.hourlyforecasts

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.Direction
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.ValueUnit

class AccuHourlyForecastsResponse {
    private var items: MutableList<Item>? = null
    fun getItems(): List<Item>? {
        return items
    }

    fun setItems(responseJsonElement: JsonElement) {
        val jsonArray = responseJsonElement.asJsonArray
        val gson = Gson()
        items = ArrayList()
        for (jsonElement in jsonArray) {
            val item = gson.fromJson(jsonElement.toString(), Item::class.java)
            items.add(item)
        }
    }

    class Item {
        @Expose @SerializedName("DateTime") var dateTime: String? = null
        @Expose @SerializedName("EpochDateTime") var epochDateTime: String? = null
        @Expose @SerializedName("WeatherIcon") var weatherIcon: String? = null
        @Expose @SerializedName("IconPhrase") var iconPhrase: String? = null
        @Expose @SerializedName("HasPrecipitation") var hasPrecipitation: String? = null
        @Expose @SerializedName("PrecipitationType") var precipitationType: String? = null
        @Expose @SerializedName("PrecipitationIntensity") var precipitationIntensity: String? = null
        @Expose @SerializedName("IsDaylight") var isDaylight: String? = null
        @Expose @SerializedName("RelativeHumidity") var relativeHumidity: String? = null
        @Expose @SerializedName("IndoorRelativeHumidity") var indoorRelativeHumidity: String? = null

        @Expose @SerializedName("UVIndex")
        private var uVIndex: String? = null

        @Expose @SerializedName("UVIndexText")
        private var uVIndexText: String? = null
        @Expose @SerializedName("PrecipitationProbability") var precipitationProbability: String? = null
        @Expose @SerializedName("ThunderstormProbability") var thunderstormProbability: String? = null
        @Expose @SerializedName("RainProbability") var rainProbability: String? = null
        @Expose @SerializedName("SnowProbability") var snowProbability: String? = null
        @Expose @SerializedName("IceProbability") var iceProbability: String? = null
        @Expose @SerializedName("CloudCover") var cloudCover: String? = null
        @Expose @SerializedName("Wind") var wind: Wind? = null
        @Expose @SerializedName("WindGust") var windGust: WindGust? = null
        @Expose @SerializedName("TotalLiquid") var totalLiquid: ValueUnit? = null
        @Expose @SerializedName("Rain") var rain: ValueUnit? = null
        @Expose @SerializedName("Snow") var snow: ValueUnit? = null
        @Expose @SerializedName("Ice") var ice: ValueUnit? = null
        @Expose @SerializedName("Temperature") var temperature: ValueUnit? = null
        @Expose @SerializedName("RealFeelTemperature") var realFeelTemperature: ValueUnit? = null
        @Expose @SerializedName("RealFeelTemperatureShade") var realFeelTemperatureShade: ValueUnit? = null
        @Expose @SerializedName("WetBulbTemperature") var wetBulbTemperature: ValueUnit? = null
        @Expose @SerializedName("DewPoint") var dewPoint: ValueUnit? = null
        @Expose @SerializedName("Visibility") var visibility: ValueUnit? = null
        @Expose @SerializedName("Ceiling") var ceiling: ValueUnit? = null
        fun getuVIndex(): String? {
            return uVIndex
        }

        fun setuVIndex(uVIndex: String?) {
            this.uVIndex = uVIndex
        }

        fun getuVIndexText(): String? {
            return uVIndexText
        }

        fun setuVIndexText(uVIndexText: String?) {
            this.uVIndexText = uVIndexText
        }

        class Wind {
            @Expose @SerializedName("Speed")
            var speed: ValueUnit? = null

            @Expose @SerializedName("Direction")
            var direction: Direction? = null
        }

        class WindGust {
            @Expose @SerializedName("Speed")
            var speed: ValueUnit? = null
        }
    }
}