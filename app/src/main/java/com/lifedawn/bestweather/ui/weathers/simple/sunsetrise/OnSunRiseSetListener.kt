package com.lifedawn.bestweather.ui.weathers.simple.sunsetrise

interface OnSunRiseSetListener {
    fun onCalcResult(calcSuccessful: Boolean, night: Boolean)
}