package com.lifedawn.bestweather.data.local.timezone

import com.lifedawn.bestweather.data.local.room.AppDb
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDao
import com.lifedawn.bestweather.data.local.timezone.model.TimeZoneIdDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocalTimeZoneRepositoryImpl @Inject constructor(private val room: AppDb) : LocalTimeZoneRepository {
    private val dao: TimeZoneIdDao = room.timeZoneIdDao()

    override fun get(lat: Double, lon: Double) = flow {
        emit(dao.get(lat, lon))
    }

    override suspend fun insert(timeZoneDto: TimeZoneIdDto) {
        if (dao.count() > 100)
            dao.reset()
        dao.insert(timeZoneDto)
    }

    override suspend fun delete(timeZoneDto: TimeZoneIdDto) {
        dao.delete(timeZoneDto)

    }

    override fun reset() = flow {
        dao.reset()
        emit(true)
    }
}