package com.lifedawn.bestweather.data.local.datastore.interfaces

import androidx.datastore.preferences.core.edit

interface IAppSettings {
    suspend fun saveDefaultProviderAsOwm(on: Boolean)

    suspend fun saveDefaultProviderAsMetNorway(on: Boolean)

    suspend fun saveKmaTopPriority(on: Boolean)

    suspend fun saveEnableCurrentLocation(on: Boolean)

    suspend fun saveNeverAskAgainAccessLocationPermission(on: Boolean)

    suspend fun saveEnabledBackgroundAnimation(on: Boolean)

    suspend fun saveAppIntro(on: Boolean)

    suspend fun saveWidgetRefreshInterval(interval: Long)

    suspend fun saveLastCoordinates(latitude: String, longitude: String)
}