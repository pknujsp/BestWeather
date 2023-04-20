package com.lifedawn.bestweather.commons.interfaces

interface OnCheckedSwitchInListListener<T> {
    fun onCheckedSwitch(t: T, isChecked: Boolean)
}