package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Details {
    @Expose @SerializedName("air_pressure_at_sea_level") var airPressureAtSeaLevel: String? = null
    @Expose @SerializedName("air_temperature") var airTemperature: String? = null
    @Expose @SerializedName("air_temperature_max") var airTemperatureMax: String? = null
    @Expose @SerializedName("air_temperature_min") var airTemperatureMin: String? = null
    @Expose @SerializedName("cloud_area_fraction") var cloudAreaFraction: String? = null
    @Expose @SerializedName("cloud_area_fraction_high") var cloudAreaFractionHigh: String? = null
    @Expose @SerializedName("cloud_area_fraction_low") var cloudAreaFractionLow: String? = null
    @Expose @SerializedName("cloud_area_fraction_medium") var cloudAreaFractionMedium: String? = null
    @Expose @SerializedName("dew_point_temperature") var dewPointTemperature: String? = null
    @Expose @SerializedName("fog_area_fraction") var fogAreaFraction: String? = null
    @Expose @SerializedName("precipitation_amount") var precipitationAmount: String? = null
    @Expose @SerializedName("relative_humidity") var relativeHumidity: String? = null
    @Expose @SerializedName("ultraviolet_index_clear_sky") var ultravioletIndexClearSky: String? = null
    @Expose @SerializedName("wind_from_direction") var windFromDirection: String? = null
    @Expose @SerializedName("wind_speed") var windSpeed: String? = null
}