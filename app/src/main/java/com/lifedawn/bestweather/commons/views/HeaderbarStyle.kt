package com.lifedawn.bestweather.commons.views

import android.app.Activity
import android.view.View

object HeaderbarStyle {
    @JvmStatic
    fun setStyle(style: Style, activity: Activity?) {
        if (activity != null) {
            var newValue = 0
            newValue = if (style == Style.Black) {
                // 상단바 블랙으로
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // 상단바 하양으로
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
            if (activity.window.decorView.systemUiVisibility != newValue) activity.window.decorView.systemUiVisibility = newValue
        }
    }

    enum class Style {
        Black, White
    }
}