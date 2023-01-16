package com.lifedawn.bestweather.commons.interfaces

interface OnClickedPopupMenuItemListener<T> {
    fun onClickedItem(e: T, position: Int)
}