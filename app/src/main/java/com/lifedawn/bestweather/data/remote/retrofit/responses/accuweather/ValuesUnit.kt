package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ValuesUnit {
    @Expose @SerializedName("Metric") var metric: ValueUnit? = null
        private set
    @Expose @SerializedName("Imperial") var imperial: ValueUnit? = null
        private set

    fun setMetric(metric: ValueUnit?): ValuesUnit {
        this.metric = metric
        return this
    }

    fun setImperial(imperial: ValueUnit?): ValuesUnit {
        this.imperial = imperial
        return this
    }
}