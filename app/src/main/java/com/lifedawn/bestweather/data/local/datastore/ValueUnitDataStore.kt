package com.lifedawn.bestweather.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lifedawn.bestweather.commons.constants.ValueUnits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ValueUnitDataStore @Inject constructor(
    private val context: Context
) {
    private val Context.valueUnitDataStore by preferencesDataStore(name = "value_unit")

    private val TEMP_UNIT_KEY = stringPreferencesKey("temp")
    private val WIND_UNIT_KEY = stringPreferencesKey("wind")
    private val VISIBILITY_UNIT_KEY = stringPreferencesKey("visibility")
    private val CLOCK_UNIT_KEY = stringPreferencesKey("clock")

    val tempUnit = context.valueUnitDataStore.data
        .map {
            ValueUnits.valueOf(it[TEMP_UNIT_KEY] ?: ValueUnits.celsius.name)
        }

    val windUnit = context.valueUnitDataStore.data
        .map {
            ValueUnits.valueOf(it[WIND_UNIT_KEY] ?: ValueUnits.mPerSec.name)
        }

    val visibilityUnit = context.valueUnitDataStore.data
        .map {
            ValueUnits.valueOf(it[VISIBILITY_UNIT_KEY] ?: ValueUnits.km.name)
        }

    val clockUnit = context.valueUnitDataStore.data
        .map {
            ValueUnits.valueOf(it[CLOCK_UNIT_KEY] ?: ValueUnits.clock12.name)
        }

    suspend fun saveTempUnit(temp: ValueUnits) {
        context.valueUnitDataStore.edit {
            it[TEMP_UNIT_KEY] = temp.name
        }
    }

    suspend fun saveWindUnit(wind: ValueUnits) {
        context.valueUnitDataStore.edit {
            it[WIND_UNIT_KEY] = wind.name
        }
    }

    suspend fun saveVisibilityUnit(visibility: ValueUnits) {
        context.valueUnitDataStore.edit {
            it[VISIBILITY_UNIT_KEY] = visibility.name
        }
    }


    suspend fun saveClockUnit(clock: ValueUnits) {
        context.valueUnitDataStore.edit {
            it[CLOCK_UNIT_KEY] = clock.name
        }
    }

}