package com.lifedawn.bestweather.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lifedawn.bestweather.data.local.datastore.interfaces.IAppSettings
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppSettingsDataStore @Inject constructor(
    private val context: Context
) : IAppSettings {

    private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

    private val DEFAULT_OWM_KEY = booleanPreferencesKey("default_provider_as_owm")
    private val DEFAULT_MET_NORWAY_KEY = booleanPreferencesKey("default_provider_as_met_norway")
    private val KMA_TOP_PRIORITY_KEY = booleanPreferencesKey("kma_top_priority")
    private val ENABLED_CURRENT_LOCATION_KEY = booleanPreferencesKey("enabled_current_location")
    private val NEVER_ASK_AGAIN_ACCESS_LOCATION_PERMISSION_KEY = booleanPreferencesKey("never_ask_again_access_location_permission")
    private val ENABLED_BACKGROUND_ANIMATION_KEY = booleanPreferencesKey("enabled_background_animation")
    private val APP_INTRO_KEY = booleanPreferencesKey("app_intro")
    private val WIDGET_REFRESH_INTERVAL_KEY = longPreferencesKey("widget_refresh_interval")

    private val LAST_LATITUDE_KEY = stringPreferencesKey("last_latitude")
    private val LAST_LONGITUDE_KEY = stringPreferencesKey("last_longitude")

    val defaultProviderAsOwm = context.appSettingsDataStore.data
        .map {
            it[DEFAULT_OWM_KEY] ?: false
        }

    val defaultProviderAsMetNorway = context.appSettingsDataStore.data
        .map {
            it[DEFAULT_MET_NORWAY_KEY] ?: true
        }

    val kmaTopPriority = context.appSettingsDataStore.data
        .map {
            it[KMA_TOP_PRIORITY_KEY] ?: true
        }

    val enableCurrentLocation = context.appSettingsDataStore.data
        .map {
            it[ENABLED_CURRENT_LOCATION_KEY] ?: true
        }

    val neverAskAgainAccessLocationPermission = context.appSettingsDataStore.data
        .map {
            it[NEVER_ASK_AGAIN_ACCESS_LOCATION_PERMISSION_KEY] ?: false
        }

    val enabledBackgroundAnimation = context.appSettingsDataStore.data
        .map {
            it[ENABLED_BACKGROUND_ANIMATION_KEY] ?: true
        }

    val appIntro = context.appSettingsDataStore.data
        .map {
            it[APP_INTRO_KEY] ?: true
        }

    val widgetRefreshInterval = context.appSettingsDataStore.data
        .map {
            it[WIDGET_REFRESH_INTERVAL_KEY] ?: 0L
        }

    val lastLatitude = context.appSettingsDataStore.data
        .map {
            it[LAST_LATITUDE_KEY] ?: ""
        }

    val lastLongitude = context.appSettingsDataStore.data
        .map {
            it[LAST_LONGITUDE_KEY] ?: ""
        }

    override suspend fun saveDefaultProviderAsOwm(on: Boolean) {
        context.appSettingsDataStore.edit {
            it[DEFAULT_OWM_KEY] = on
        }
    }

    override suspend fun saveDefaultProviderAsMetNorway(on: Boolean) {
        context.appSettingsDataStore.edit {
            it[DEFAULT_MET_NORWAY_KEY] = on
        }
    }

    override suspend fun saveKmaTopPriority(on: Boolean) {
        context.appSettingsDataStore.edit {
            it[KMA_TOP_PRIORITY_KEY] = on
        }
    }

    override suspend fun saveEnableCurrentLocation(on: Boolean) {
        context.appSettingsDataStore.edit {
            it[ENABLED_CURRENT_LOCATION_KEY] = on
        }
    }

    override suspend fun saveNeverAskAgainAccessLocationPermission(on: Boolean) {
        context.appSettingsDataStore.edit {
            it[NEVER_ASK_AGAIN_ACCESS_LOCATION_PERMISSION_KEY] = on
        }
    }

    override suspend fun saveEnabledBackgroundAnimation(on: Boolean) {
        context.appSettingsDataStore.edit {
            it[ENABLED_BACKGROUND_ANIMATION_KEY] = on
        }
    }

    override suspend fun saveAppIntro(on: Boolean) {
        context.appSettingsDataStore.edit {
            it[APP_INTRO_KEY] = on
        }
    }

    override suspend fun saveWidgetRefreshInterval(interval: Long) {
        context.appSettingsDataStore.edit {
            it[WIDGET_REFRESH_INTERVAL_KEY] = interval
        }
    }

    override suspend fun saveLastCoordinates(latitude: String, longitude: String) {
        context.appSettingsDataStore.edit {
            it[LAST_LATITUDE_KEY] = latitude
            it[LAST_LONGITUDE_KEY] = longitude
        }
    }
}