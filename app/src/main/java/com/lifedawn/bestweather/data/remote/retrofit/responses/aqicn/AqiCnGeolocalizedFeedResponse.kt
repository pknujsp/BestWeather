package com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AqiCnGeolocalizedFeedResponse : Serializable {
    @Expose @SerializedName("status") var status: String? = null
    @Expose @SerializedName("data") var data: Data? = null

    class Data : Serializable {
        @Expose @SerializedName("aqi") var aqi: String? = null
            private set
        @Expose @SerializedName("idx") var idx: String? = null
            private set
        @Expose @SerializedName("dominentpol") var dominentPol: String? = null
            private set
        @Expose @SerializedName("city") var city: City? = null
            private set
        @Expose @SerializedName("iaqi") var iaqi: IAqi? = null
            private set
        @Expose @SerializedName("time") var time: Time? = null
            private set
        @Expose @SerializedName("forecast") var forecast: Forecast? = null
            private set

        fun setCity(city: City?): Data {
            this.city = city
            return this
        }

        fun setIaqi(iaqi: IAqi?): Data {
            this.iaqi = iaqi
            return this
        }

        fun setTime(time: Time?): Data {
            this.time = time
            return this
        }

        fun setForecast(forecast: Forecast?): Data {
            this.forecast = forecast
            return this
        }

        fun setAqi(aqi: String?): Data {
            this.aqi = aqi
            return this
        }

        fun setIdx(idx: String?): Data {
            this.idx = idx
            return this
        }

        fun setDominentPol(dominentPol: String?): Data {
            this.dominentPol = dominentPol
            return this
        }

        class City : Serializable {
            @Expose @SerializedName("geo") var geo: List<String>? = null
            @Expose @SerializedName("name") var name: String? = null
            @Expose @SerializedName("url") var url: String? = null
        }

        class IAqi : Serializable {
            @Expose @SerializedName("co") var co: ValueMap? = null
            @Expose @SerializedName("dew") var dew: ValueMap? = null
            @Expose @SerializedName("no2") var no2: ValueMap? = null
            @Expose @SerializedName("o3") var o3: ValueMap? = null
            @Expose @SerializedName("pm10") var pm10: ValueMap? = null
            @Expose @SerializedName("pm25") var pm25: ValueMap? = null
            @Expose @SerializedName("so2") var so2: ValueMap? = null

            class ValueMap : Serializable {
                @Expose @SerializedName("v") var value: String? = null
            }
        }

        class Time : Serializable {
            @Expose @SerializedName("s") var s: String? = null
            @Expose @SerializedName("tz") var tz: String? = null
            @Expose @SerializedName("v") var v: String? = null
            @Expose @SerializedName("iso") var iso: String? = null
        }

        class Forecast : Serializable {
            @Expose @SerializedName("daily") var daily: Daily? = null
                private set

            fun setDaily(daily: Daily?): Forecast {
                this.daily = daily
                return this
            }

            class Daily : Serializable {
                @Expose @SerializedName("o3") var o3: List<ValueMap>? = null
                    private set
                @Expose @SerializedName("pm10") var pm10: List<ValueMap>? = null
                    private set
                @Expose @SerializedName("pm25") var pm25: List<ValueMap>? = null
                    private set
                @Expose @SerializedName("uvi") var uvi: List<ValueMap>? = null
                    private set

                fun setO3(o3: List<ValueMap>?): Daily {
                    this.o3 = o3
                    return this
                }

                fun setPm10(pm10: List<ValueMap>?): Daily {
                    this.pm10 = pm10
                    return this
                }

                fun setPm25(pm25: List<ValueMap>?): Daily {
                    this.pm25 = pm25
                    return this
                }

                fun setUvi(uvi: List<ValueMap>?): Daily {
                    this.uvi = uvi
                    return this
                }

                class ValueMap : Serializable {
                    @Expose @SerializedName("avg") var avg: String? = null
                    @Expose @SerializedName("day") var day: String? = null
                    @Expose @SerializedName("max") var max: String? = null
                    @Expose @SerializedName("min") var min: String? = null
                }
            }
        }
    }
}