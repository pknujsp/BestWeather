package com.lifedawn.bestweather.model.timezone

import android.content.Context
import androidx.room.Delete
import androidx.room.Insert
import com.lifedawn.bestweather.main.MyApplication
import com.lifedawn.bestweather.room.AppDb
import com.lifedawn.bestweather.room.callback.DbQueryCallback
import com.lifedawn.bestweather.room.dao.WidgetDao
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


    fun get(addressName: String, callback: DbQueryCallback<TimeZoneIdDto>) {
        CoroutineScope(Dispatchers.IO).launch {
            callback.processResult(dao.get(addressName))
        }
    }

    fun insert(timeZoneDto: TimeZoneIdDto) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(timeZoneDto)
        }
    }

    fun delete(timeZoneDto: TimeZoneIdDto) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(timeZoneDto)
        }
    }
}