package com.lifedawn.bestweather.model.timezone

import android.content.Context
import com.lifedawn.bestweather.room.AppDb
import com.lifedawn.bestweather.room.callback.DbQueryCallback
import com.lifedawn.bestweather.room.repository.FavoriteAddressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface TimeZoneIdRepository {
    fun get(lat: Double, lon: Double, callback: DbQueryCallback<TimeZoneIdDto?>)

    fun insert(timeZoneDto: TimeZoneIdDto)

    fun delete(timeZoneDto: TimeZoneIdDto)

    fun reset(callback: DbQueryCallback<Boolean>)
}