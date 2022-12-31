package com.lifedawn.bestweather.data.local.timezone

import android.content.Context
import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDao
import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDto
import com.lifedawn.bestweather.data.local.room.AppDb
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalTimeZoneRepositoryImpl(context: Context) : LocalTimeZoneRepository {
    private val dao: TimeZoneIdDao

    init {
        dao = AppDb.getInstance(context).timeZoneIdDao()
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