package com.lifedawn.bestweather.utils

import android.content.Context
import android.os.PowerManager
import androidx.core.content.ContextCompat.getSystemService


class DeviceUtils {
    companion object{

        fun isScreenOn(context: Context) : Boolean{
            val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isInteractive
        }
    }
}