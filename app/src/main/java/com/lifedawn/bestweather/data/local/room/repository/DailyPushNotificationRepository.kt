package com.lifedawn.bestweather.data.local.room.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import java.util.concurrent.ExecutorService

class DailyPushNotificationRepository private constructor(context: Context) {
    private val dailyPushNotificationDao: DailyPushNotificationDao
    private val executorService: ExecutorService = MyApplication.getExecutorService()

    init {
        dailyPushNotificationDao = AppDb.getInstance(context).dailyPushNotificationDao()
    }

    fun listLiveData(): LiveData<List<DailyPushNotificationDto>> {
        return dailyPushNotificationDao.list()
    }

    fun getAll(callback: DbQueryCallback<List<DailyPushNotificationDto?>?>) {
        executorService.execute { callback.processResult(dailyPushNotificationDao.all) }
    }

    fun size(callback: DbQueryCallback<Int?>) {
        executorService.execute { callback.processResult(dailyPushNotificationDao.size()) }
    }

    operator fun get(id: Int, callback: DbQueryCallback<DailyPushNotificationDto?>) {
        executorService.execute { callback.processResult(dailyPushNotificationDao.get(id)) }
    }

    fun delete(dailyPushNotificationDto: DailyPushNotificationDto?, callback: DbQueryCallback<Boolean?>?) {
        executorService.execute {
            dailyPushNotificationDao.delete(dailyPushNotificationDto)
            if (callback != null) {
                callback.processResult(true)
            }
        }
    }

    fun add(dailyPushNotificationDto: DailyPushNotificationDto?, callback: DbQueryCallback<DailyPushNotificationDto?>) {
        executorService.execute {
            val id = dailyPushNotificationDao.add(dailyPushNotificationDto) as Int
            callback.processResult(dailyPushNotificationDao.get(id))
        }
    }

    fun update(dailyPushNotificationDto: DailyPushNotificationDto, callback: DbQueryCallback<DailyPushNotificationDto?>?) {
        executorService.execute {
            dailyPushNotificationDao.update(dailyPushNotificationDto)
            if (callback != null) {
                val updated: DailyPushNotificationDto = dailyPushNotificationDao.get(dailyPushNotificationDto.id)
                callback.onResultSuccessful(updated)
            }
        }
    }

    companion object {
        var iNSTANCE: DailyPushNotificationRepository? = null
            private set

        @JvmStatic
        fun initialize(context: Context) {
            if (iNSTANCE == null) iNSTANCE = DailyPushNotificationRepository(context)
        }
    }
}