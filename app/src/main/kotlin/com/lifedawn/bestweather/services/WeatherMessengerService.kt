package com.lifedawn.bestweather.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Messenger
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WeatherMessengerService constructor(val clients: ArrayList<Messenger> = ArrayList<Messenger>(),
                                          val backgroundExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) :
        Service
        () {
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        backgroundExecutor.schedule({

        }, 0, TimeUnit.SECONDS)
        return super.onStartCommand(intent, flags, startId)
    }

}