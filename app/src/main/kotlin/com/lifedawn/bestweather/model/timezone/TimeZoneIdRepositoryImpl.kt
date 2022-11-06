package com.lifedawn.bestweather.model.timezone

import android.app.Application
import android.content.Context
import com.lifedawn.bestweather.room.AppDb
import com.lifedawn.bestweather.room.callback.DbQueryCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeZoneIdRepositoryImpl private constructor(context: Context) : TimeZoneIdRepository {
    private val dao: TimeZoneIdDao

    init {
        dao = AppDb.getInstance(context).timeZoneIdDao()
    }

    companion object {
        var INSTANCE: TimeZoneIdRepositoryImpl? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = TimeZoneIdRepositoryImpl(context)
            }
        }

    }


    override fun get(lat: Double, lon: Double, callback: DbQueryCallback<TimeZoneIdDto?>) {
        CoroutineScope(Dispatchers.Default).launch {
            callback.processResult(dao.get(lat, lon))
        }
    }

    override fun insert(timeZoneDto: TimeZoneIdDto) {
        CoroutineScope(Dispatchers.Default).launch {
            if (dao.count() > 100)
                dao.reset()
            dao.insert(timeZoneDto)
        }
    }

    override fun delete(timeZoneDto: TimeZoneIdDto) {
        CoroutineScope(Dispatchers.Default).launch {
            dao.delete(timeZoneDto)
        }
    }

    override fun reset(callback: DbQueryCallback<Boolean>) {
        CoroutineScope(Dispatchers.Default).launch {
            dao.reset()
            callback.onResultSuccessful(true)
        }
    }
}