package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.currentconditions

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.ValuesUnit
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.Wind
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.WindGust

class AccuCurrentConditionsResponse {
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
        @Expose @SerializedName("LocalObservationDateTime") var localObservationDateTime: String? = null
        @Expose @SerializedName("EpochTime") var epochTime: String? = null
        @Expose @SerializedName("WeatherText") var weatherText: String? = null
        @Expose @SerializedName("WeatherIcon") var weatherIcon: String? = null
        @Expose @SerializedName("HasPrecipitation") var hasPrecipitation: String? = null
        @Expose @SerializedName("PrecipitationType") var precipitationType: String? = null
        @Expose @SerializedName("IsDayTime") var isDayTime: String? = null
        @Expose @SerializedName("RelativeHumidity") var relativeHumidity: String? = null
        @Expose @SerializedName("IndoorRelativeHumidity") var indoorRelativeHumidity: String? = null

        @Expose @SerializedName("UVIndex")
        private var uVIndex: String? = null

        @Expose @SerializedName("UVIndexText")
        private var uVIndexText: String? = null
        @Expose @SerializedName("ObstructionsToVisibility") var obstructionsToVisibility: String? = null
        @Expose @SerializedName("CloudCover") var cloudCover: String? = null
        @Expose @SerializedName("Temperature") var temperature: ValuesUnit? = null
        @Expose @SerializedName("RealFeelTemperature") var realFeelTemperature: ValuesUnit? = null
        @Expose @SerializedName("RealFeelTemperatureShade") var realFeelTemperatureShade: ValuesUnit? = null
        @Expose @SerializedName("DewPoint") var dewPoint: ValuesUnit? = null
        @Expose @SerializedName("Wind") var wind: Wind? = null
        @Expose @SerializedName("WindGust") var windGust: WindGust? = null
        @Expose @SerializedName("Visibility") var visibility: ValuesUnit? = null
        @Expose @SerializedName("Ceiling") var ceiling: ValuesUnit? = null
        @Expose @SerializedName("Pressure") var pressure: ValuesUnit? = null
        @Expose @SerializedName("PressureTendency") var pressureTendency: PressureTendency? = null
        @Expose @SerializedName("Past24HourTemperatureDeparture") var past24HourTemperatureDeparture: ValuesUnit? = null
        @Expose @SerializedName("ApparentTemperature") var apparentTemperature: ValuesUnit? = null
        @Expose @SerializedName("WindChillTemperature") var windChillTemperature: ValuesUnit? = null
        @Expose @SerializedName("WetBulbTemperature") var wetBulbTemperature: ValuesUnit? = null
        @Expose @SerializedName("Precip1hr") var precip1hr: ValuesUnit? = null
        @Expose @SerializedName("PrecipitationSummary") var precipitationSummary: PrecipitationSummary? = null
        @Expose @SerializedName("TemperatureSummary") var temperatureSummary: TemperatureSummary? = null
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

        class PressureTendency {
            @Expose @SerializedName("LocalizedText") var localizedText: String? = null
            @Expose @SerializedName("Code") var code: String? = null
        }

        class PrecipitationSummary {
            @Expose @SerializedName("Precipitation") var precipitation: ValuesUnit? = null
                private set
            @Expose @SerializedName("PastHour") var pastHour: ValuesUnit? = null
                private set
            @Expose @SerializedName("Past3Hours") var past3Hours: ValuesUnit? = null
                private set
            @Expose @SerializedName("Past6Hours") var past6Hours: ValuesUnit? = null
                private set
            @Expose @SerializedName("Past9Hours") var past9Hours: ValuesUnit? = null
                private set
            @Expose @SerializedName("Past12Hours") var past12Hours: ValuesUnit? = null
                private set
            @Expose @SerializedName("Past18Hours") var past18Hours: ValuesUnit? = null
                private set
            @Expose @SerializedName("Past24Hours") var past24Hours: ValuesUnit? = null
                private set

            fun setPrecipitation(precipitation: ValuesUnit?): PrecipitationSummary {
                this.precipitation = precipitation
                return this
            }

            fun setPastHour(pastHour: ValuesUnit?): PrecipitationSummary {
                this.pastHour = pastHour
                return this
            }

            fun setPast3Hours(past3Hours: ValuesUnit?): PrecipitationSummary {
                this.past3Hours = past3Hours
                return this
            }

            fun setPast6Hours(past6Hours: ValuesUnit?): PrecipitationSummary {
                this.past6Hours = past6Hours
                return this
            }

            fun setPast9Hours(past9Hours: ValuesUnit?): PrecipitationSummary {
                this.past9Hours = past9Hours
                return this
            }

            fun setPast12Hours(past12Hours: ValuesUnit?): PrecipitationSummary {
                this.past12Hours = past12Hours
                return this
            }

            fun setPast18Hours(past18Hours: ValuesUnit?): PrecipitationSummary {
                this.past18Hours = past18Hours
                return this
            }

            fun setPast24Hours(past24Hours: ValuesUnit?): PrecipitationSummary {
                this.past24Hours = past24Hours
                return this
            }
        }

        class TemperatureSummary {
            @Expose @SerializedName("Past6HourRange")
            private val pastHourRange: PastHourRange? = null

            @Expose @SerializedName("Past12HourRange")
            private val past12HourRange: PastHourRange? = null

            @Expose @SerializedName("Past24HourRange")
            private val past24HourRange: PastHourRange? = null

            class PastHourRange {
                @Expose @SerializedName("Minimum") var minimum: ValuesUnit? = null
                    private set
                @Expose @SerializedName("Maximum") var maximum: ValuesUnit? = null
                    private set

                fun setMinimum(minimum: ValuesUnit?): PastHourRange {
                    this.minimum = minimum
                    return this
                }

                fun setMaximum(maximum: ValuesUnit?): PastHourRange {
                    this.maximum = maximum
                    return this
                }
            }
        }
    }
}