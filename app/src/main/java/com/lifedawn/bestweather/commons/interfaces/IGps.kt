package com.lifedawn.bestweather.commons.interfaces

import java.io.Serializable

interface IGps : Serializable {
    fun requestCurrentLocation()
}