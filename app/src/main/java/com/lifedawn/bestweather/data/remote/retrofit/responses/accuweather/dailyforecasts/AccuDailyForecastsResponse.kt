package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.dailyforecasts

import android.graphics.drawable.Drawable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.Direction
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.ValueUnit

class AccuDailyForecastsResponse {
    @Expose @SerializedName("Headline") var headline: Headline? = null
    @Expose @SerializedName("DailyForecasts") var dailyForecasts: List<DailyForecasts>? = null

    class Headline {
        @Expose @SerializedName("EffectiveDate") var effectiveDate: String? = null
        @Expose @SerializedName("EffectiveEpochDate") var effectiveEpochDate: String? = null
        @Expose @SerializedName("Severity") var severity: String? = null
        @Expose @SerializedName("Text") var text: String? = null
        @Expose @SerializedName("Category") var category: String? = null
        @Expose @SerializedName("EndDate") var endDate: String? = null
        @Expose @SerializedName("EndEpochDate") var endEpochDate: String? = null
    }

    class DailyForecasts {
        @Expose @SerializedName("Date") var dateTime: String? = null
        @Expose @SerializedName("EpochDate") var epochDate: String? = null
        @Expose @SerializedName("Temperature") var temperature: Temperature? = null
        @Expose @SerializedName("RealFeelTemperature") var realFeelTemperature: RealFeelTemperature? = null
        @Expose @SerializedName("RealFeelTemperatureShade") var realFeelTemperatureShade: RealFeelTemperatureShade? = null
        @Expose @SerializedName("Day") var day: DayOrNightValues? = null
        @Expose @SerializedName("Night") var night: DayOrNightValues? = null

        class Temperature {
            @Expose @SerializedName("Minimum") var minimum: ValueUnit? = null
            @Expose @SerializedName("Maximum") var maximum: ValueUnit? = null
        }

        class RealFeelTemperature {
            @Expose @SerializedName("Minimum") var minimum: ValueUnit? = null
            @Expose @SerializedName("Maximum") var maximum: ValueUnit? = null
        }

        class RealFeelTemperatureShade {
            @Expose @SerializedName("Minimum") var minimum: ValueUnit? = null
            @Expose @SerializedName("Maximum") var maximum: ValueUnit? = null
        }

        class DayOrNightValues {
            @Expose @SerializedName("Icon") var icon: String? = null
            var weatherImg: Drawable? = null
            @Expose @SerializedName("IconPhrase") var iconPhrase: String? = null
            @Expose @SerializedName("HasPrecipitation") var hasPrecipitation: String? = null
            @Expose @SerializedName("PrecipitationProbability") var precipitationProbability: String? = null
            @Expose @SerializedName("ThunderstormProbability") var thunderstormProbability: String? = null
            @Expose @SerializedName("RainProbability") var rainProbability: String? = null
            @Expose @SerializedName("SnowProbability") var snowProbability: String? = null
            @Expose @SerializedName("IceProbability") var iceProbability: String? = null
            @Expose @SerializedName("HoursOfPrecipitation") var hoursOfPrecipitation: String? = null
            @Expose @SerializedName("HoursOfRain") var hoursOfRain: String? = null
            @Expose @SerializedName("HoursOfSnow") var hoursOfSnow: String? = null
            @Expose @SerializedName("HoursOfIce") var hoursOfIce: String? = null
            @Expose @SerializedName("CloudCover") var cloudCover: String? = null
            @Expose @SerializedName("Wind") var wind: Wind? = null
            @Expose @SerializedName("WindGust") var windGust: Wind? = null
            @Expose @SerializedName("TotalLiquid") var totalLiquid: ValueUnit? = null
            @Expose @SerializedName("Rain") var rain: ValueUnit? = null
            @Expose @SerializedName("Snow") var snow: ValueUnit? = null
            @Expose @SerializedName("Ice") var ice: ValueUnit? = null

            class Wind {
                @Expose @SerializedName("Speed") var speed: ValueUnit? = null
                @Expose @SerializedName("Direction") var direction: Direction? = null
            }
        }
    }
}