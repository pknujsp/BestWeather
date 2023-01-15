package com.lifedawn.bestweather.data.local.room.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_area_code_table")
data class KmaAreaCodeDto(
    @ColumnInfo(name = "administrative_area_code") @PrimaryKey val administrativeAreaCode: String = "",
    @ColumnInfo(name = "phase1") var phase1: String = "",
    @ColumnInfo(name = "phase2") var phase2: String = "",
    @ColumnInfo(name = "phase3") var phase3: String = "",
    @ColumnInfo(name = "x") var _x: String = "",
    @ColumnInfo(name = "y") var _y: String = "",
    @ColumnInfo(name = "longitude_hours") val longitudeHours: String = "",
    @ColumnInfo(name = "longitude_minutes") val longitudeMinutes: String = "",
    @ColumnInfo(name = "longitude_seconds") val longitudeSeconds: String = "",
    @ColumnInfo(name = "latitude_hours") val latitudeHours: String = "",
    @ColumnInfo(name = "latitude_minutes") val latitudeMinutes: String = "",
    @ColumnInfo(name = "latitude_seconds") val latitudeSeconds: String = "",
    @ColumnInfo(name = "longitude_seconds_divide_100") val longitudeSecondsDivide100: String = "",
    @ColumnInfo(name = "latitude_seconds_divide_100") val latitudeSecondsDivide100: String = "",
    @ColumnInfo(name = "mid_land_fcst_code") val midLandFcstCode: String = "",
    @ColumnInfo(name = "mid_ta_code") val midTaCode: String = ""
) {
    var x: String = _x
        set(value) {
            field = if (value.contains(".0")) value.replace(".0", "") else value
        }

    var y: String = _y
        set(value) {
            field = if (value.contains(".0")) value.replace(".0", "") else value
        }
}