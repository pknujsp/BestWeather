package com.lifedawn.bestweather.ui.main

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class AppApplication : Application() {
    companion object {
        var statusBarHeight = 0
        lateinit var locale: Locale
        lateinit var localeCountryCode: String
    }

    override fun onCreate() {
        super.onCreate()

        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (id > 0)
            statusBarHeight = resources.getDimensionPixelSize(id)

        locale = resources.configuration.locales[0]
        localeCountryCode = locale.country

        val context = applicationContext
    }

    fun loadValueUnitTypes() {
        //datastore쓰기
    }
}