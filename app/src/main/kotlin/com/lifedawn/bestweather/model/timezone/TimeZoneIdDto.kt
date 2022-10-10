package com.lifedawn.bestweather.model.timezone

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimeZoneIdDto(
        @ColumnInfo(name = "addressName") val latitude: String,
        @ColumnInfo(name = "timeZoneId") val timeZoneId: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}