package com.lifedawn.bestweather.ui.main

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*

@HiltAndroidApp
class AppApplication : Application() {
    companion object {
        lateinit var locale: Locale
        lateinit var localeCountryCode: String
    }

    override fun onCreate() {
        super.onCreate()
        locale = resources.configuration.locales[0]
        localeCountryCode = locale.country
    }

}