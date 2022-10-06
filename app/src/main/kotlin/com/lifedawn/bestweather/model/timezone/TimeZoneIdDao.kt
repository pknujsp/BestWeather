package com.lifedawn.bestweather.model.timezone

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport

@Dao
interface TimeZoneIdDao {
    @Query("SELECT * FROM TimeZoneIdDto WHERE latitude == :latitude AND " +
            "longitude == :longitude")
    fun get(latitude: Double, longitude: Double): TimeZoneIdDto

    @Insert(onConflict = ABORT)
    fun insert(timeZoneDto: TimeZoneIdDto)

    @Delete
    fun delete(timeZoneDto: TimeZoneIdDto)
}