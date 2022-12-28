package com.lifedawn.bestweather.data.valueunit.dto

import com.lifedawn.bestweather.data.valueunit.enums.ValueUnitType

class ValueUnitDto {
    companion object {
        lateinit var TEMP_UNIT: ValueUnitType
        lateinit var WIND_UNIT: ValueUnitType
        lateinit var VISIBILITY_UNIT: ValueUnitType
    }
}