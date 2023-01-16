package com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Wind {
    @Expose @SerializedName("Speed") var speed: ValuesUnit? = null
        private set
    @Expose @SerializedName("Direction") var direction: Direction? = null
    fun setSpeed(speed: ValuesUnit?): Wind {
        this.speed = speed
        return this
    }
}