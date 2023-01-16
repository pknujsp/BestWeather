package com.lifedawn.bestweather.commons.classes

import android.os.Handler
import android.os.Looper

object MainThreadWorker {
    private val handler = Handler(Looper.getMainLooper())
    @JvmStatic
    fun runOnUiThread(action: Runnable) {
        if (Thread.currentThread() === Looper.getMainLooper().thread) action.run() else handler.post(action)
    }
}