package com.lifedawn.bestweather.ui.settings.valueunits.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.data.local.datastore.ValueUnitDataStore
import com.lifedawn.bestweather.data.local.datastore.interfaces.IValueUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ValueUnitsSettingsViewModel @Inject constructor(
    private val valueUnitDataStore: ValueUnitDataStore, application: Application
) : AndroidViewModel(application), IValueUnit {

    private val _tempFlow = valueUnitDataStore.tempUnit
        .map {
            it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _windSpeedFlow = valueUnitDataStore.windUnit
        .map {
            it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _visibilityFlow = valueUnitDataStore.visibilityUnit
        .map {
            it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _clockFlow = valueUnitDataStore.clockUnit
        .map {
            it
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val tempFlow: StateFlow<Any>
        get() = _tempFlow

    val windSpeedFlow: StateFlow<Any>
        get() = _windSpeedFlow

    val visibilityFlow: StateFlow<Any>
        get() = _visibilityFlow

    val clockFlow: StateFlow<Any>
        get() = _clockFlow

    override suspend fun saveTempUnit(temp: ValueUnits) {
        valueUnitDataStore.saveTempUnit(temp)
    }

    override suspend fun saveWindUnit(wind: ValueUnits) {
        valueUnitDataStore.saveWindUnit(wind)
    }

    override suspend fun saveVisibilityUnit(visibility: ValueUnits) {
        valueUnitDataStore.saveVisibilityUnit(visibility)
    }

    override suspend fun saveClockUnit(clock: ValueUnits) {
        valueUnitDataStore.saveClockUnit(clock)
    }

}