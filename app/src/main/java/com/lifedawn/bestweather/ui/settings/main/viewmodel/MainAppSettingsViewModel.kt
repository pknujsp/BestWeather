package com.lifedawn.bestweather.ui.settings.main.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifedawn.bestweather.data.local.datastore.AppSettingsDataStore
import com.lifedawn.bestweather.data.local.datastore.interfaces.IAppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainAppSettingsViewModel @Inject constructor(
    private val appSettingsDataStore: AppSettingsDataStore, application: Application
) : AndroidViewModel(application), IAppSettings {

    private val _enabledBackgroundAnimationFlow = appSettingsDataStore.enabledBackgroundAnimation
        .map {
            it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _widgetRefreshIntervalFlow = appSettingsDataStore.widgetRefreshInterval
        .map {
            it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _enabledCurrentLocationFlow = appSettingsDataStore.enableCurrentLocation
        .map {
            it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _kmaTopPriorityFlow = appSettingsDataStore.kmaTopPriority
        .map {
            it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val enabledBackgroundAnimationFlow: StateFlow<Boolean>
        get() = _enabledBackgroundAnimationFlow

    val widgetRefreshIntervalFlow: StateFlow<Any>
        get() = _widgetRefreshIntervalFlow

    val enabledCurrentLocationFlow: StateFlow<Boolean>
        get() = _enabledCurrentLocationFlow

    val kmaTopPriorityFlow: StateFlow<Boolean>
        get() = _kmaTopPriorityFlow

    override suspend fun saveDefaultProviderAsOwm(on: Boolean) {
        appSettingsDataStore.saveDefaultProviderAsOwm(on)
    }

    override suspend fun saveDefaultProviderAsMetNorway(on: Boolean) {
        appSettingsDataStore.saveDefaultProviderAsMetNorway(on)
    }

    override suspend fun saveKmaTopPriority(on: Boolean) {
        appSettingsDataStore.saveKmaTopPriority(on)
    }

    override suspend fun saveEnableCurrentLocation(on: Boolean) {
        appSettingsDataStore.saveEnableCurrentLocation(on)
    }

    override suspend fun saveNeverAskAgainAccessLocationPermission(on: Boolean) {
        appSettingsDataStore.saveNeverAskAgainAccessLocationPermission(on)
    }

    override suspend fun saveEnabledBackgroundAnimation(on: Boolean) {
        appSettingsDataStore.saveEnabledBackgroundAnimation(on)
    }

    override suspend fun saveAppIntro(on: Boolean) {
        appSettingsDataStore.saveAppIntro(on)
    }

    override suspend fun saveWidgetRefreshInterval(interval: Long) {
        appSettingsDataStore.saveWidgetRefreshInterval(interval)
    }

    override suspend fun saveLastCoordinates(latitude: String, longitude: String) {
        appSettingsDataStore.saveLastCoordinates(latitude, longitude)
    }

}