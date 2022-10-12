package com.lifedawn.bestweather.model.timezone

import android.content.Context
import com.lifedawn.bestweather.room.AppDb
import com.lifedawn.bestweather.room.callback.DbQueryCallback
import com.lifedawn.bestweather.room.repository.FavoriteAddressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeZoneIdRepository(context: Context) {
    private val dao: TimeZoneIdDao

    init {
        dao = AppDb.getInstance(context).timeZoneIdDao()
    }

    companion object {
        var INSTANCE: TimeZoneIdRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = TimeZoneIdRepository(context)
            }
        }

    }


    fun get(lat: Double, lon: Double, callback: DbQueryCallback<TimeZoneIdDto?>) {
        CoroutineScope(Dispatchers.IO).launch {
            callback.processResult(dao.get(lat, lon))
        }
    }

    fun insert(timeZoneDto: TimeZoneIdDto) {
        CoroutineScope(Dispatchers.IO).launch {
            if (dao.count() > 100)
                dao.reset()
            dao.insert(timeZoneDto)
        }
    }

    fun delete(timeZoneDto: TimeZoneIdDto) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(timeZoneDto)
        }
    }

    fun reset(callback: DbQueryCallback<Boolean>) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.reset()
            callback.onResultSuccessful(true)
        }
    }
}