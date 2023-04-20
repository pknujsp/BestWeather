package com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.timeseries.LocationForecastTimeSeriesItem

class LocationForecastProperties {
    @Expose @SerializedName("meta") var meta: LocationForecastMeta? = null
    @Expose @SerializedName("timeseries") var timeSeries: List<LocationForecastTimeSeriesItem>? = null
}