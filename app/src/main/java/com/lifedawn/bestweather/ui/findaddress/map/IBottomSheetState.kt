package com.lifedawn.bestweather.ui.findaddress.map

interface IBottomSheetState {
    fun setStateOfBottomSheet(bottomSheetType: BottomSheetType, state: Int)
    fun getStateOfBottomSheet(bottomSheetType: BottomSheetType): Int
    fun collapseAllExpandedBottomSheets()
}