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
        private var _statusBarHeight = 0
        val statusBarHeight get() = _statusBarHeight

        private lateinit var _locale: Locale
        val locale get() = _locale
        private lateinit var _localeCountryCode: String
        val localeCountryCode get() = _localeCountryCode
    }

    override fun onCreate() {
        super.onCreate()

        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (id > 0)
            _statusBarHeight = resources.getDimensionPixelSize(id)

        _locale = resources.configuration.locales[0]
        _localeCountryCode = _locale.country

    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
}