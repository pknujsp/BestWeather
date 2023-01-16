package com.lifedawn.bestweather.commons.interfaces

interface OnProgressViewListener {
    fun onSuccessful()
    fun onFailed(text: String?)
    fun onStarted()
}