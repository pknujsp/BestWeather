package com.lifedawn.bestweather.utils

import android.content.Context
import android.os.IBinder
import android.os.PowerManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import java.security.AccessController.getContext


class DeviceUtils {
    companion object {

        fun isScreenOn(context: Context): Boolean {
            val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isInteractive
        }

        fun showKeyboard(context: Context, view: View) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)


        }

        fun hideKeyboard(context: Context, windowToken: IBinder?) {
            if (windowToken != null) {
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }
}