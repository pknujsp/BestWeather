package com.lifedawn.bestweather.ui.theme

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import com.lifedawn.bestweather.ui.weathers.FragmentType

object AppTheme {
    fun getColor(context: Context, id: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(id, value, true)
        return value.data
    }

    @JvmStatic
    fun getTextColor(fragmentType: FragmentType?): Int {
        return when (fragmentType) {
            FragmentType.Detail, FragmentType.Comparison -> Color.BLACK
            else -> Color.WHITE
        }
    }
}