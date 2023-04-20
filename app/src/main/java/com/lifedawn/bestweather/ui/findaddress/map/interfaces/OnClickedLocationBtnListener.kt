package com.lifedawn.bestweather.ui.findaddress.map.interfaces

interface OnClickedLocationBtnListener<T> {
    fun onSelected(e: T, remove: Boolean)
}