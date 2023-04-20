package com.lifedawn.bestweather.data.local.datastore.interfaces

import androidx.datastore.preferences.core.edit
import com.lifedawn.bestweather.commons.constants.ValueUnits

interface IValueUnit {
    suspend fun saveTempUnit(temp: ValueUnits)

    suspend fun saveWindUnit(wind: ValueUnits)

    suspend fun saveVisibilityUnit(visibility: ValueUnits)

    suspend fun saveClockUnit(clock: ValueUnits)
}