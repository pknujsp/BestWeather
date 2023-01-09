package com.lifedawn.bestweather.data.local.room.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_area_code_table")
class KmaAreaCodeDto {
    @ColumnInfo(name = "administrative_area_code") @PrimaryKey var administrativeAreaCode: String = null
    @ColumnInfo(name = "phase1") var phase1: String? = null
    @ColumnInfo(name = "phase2") var phase2: String? = null
    @ColumnInfo(name = "phase3") var phase3: String? = null
    @ColumnInfo(name = "x") var x: String? = null
        private set
    @ColumnInfo(name = "y") var y: String? = null
        private set
    @ColumnInfo(name = "longitude_hours") var longitudeHours: String? = null
    @ColumnInfo(name = "longitude_minutes") var longitudeMinutes: String? = null
    @ColumnInfo(name = "longitude_seconds") var longitudeSeconds: String? = null
    @ColumnInfo(name = "latitude_hours") var latitudeHours: String? = null
    @ColumnInfo(name = "latitude_minutes") var latitudeMinutes: String? = null
    @ColumnInfo(name = "latitude_seconds") var latitudeSeconds: String? = null
    @ColumnInfo(name = "longitude_seconds_divide_100") var longitudeSecondsDivide100: String? = null
    @ColumnInfo(name = "latitude_seconds_divide_100") var latitudeSecondsDivide100: String? = null
    @ColumnInfo(name = "mid_land_fcst_code") var midLandFcstCode: String? = null
    @ColumnInfo(name = "mid_ta_code") var midTaCode: String? = null
    fun setX(x: String) {
        this.x = if (x.contains(".0")) x.replace(".0", "") else x
    }

    fun setY(y: String) {
        this.y = if (y.contains(".0")) y.replace(".0", "") else y
    }
}