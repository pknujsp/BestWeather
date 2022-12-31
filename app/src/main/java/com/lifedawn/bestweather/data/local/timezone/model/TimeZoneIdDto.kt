package com.lifedawn.bestweather.data.local.timezone.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimeZoneIdDto(
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "timeZoneId") val timeZoneId: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}