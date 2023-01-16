package com.lifedawn.bestweather.data.local.room.repository

import android.content.Context
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import java.util.concurrent.ExecutorService

class WidgetRepository private constructor(context: Context) {
    private val widgetDao: WidgetDao
    private val executorService: ExecutorService = MyApplication.getExecutorService()

    init {
        widgetDao = AppDb.getInstance(context).widgetDao()
    }

    fun add(widgetDto: WidgetDto?, callback: DbQueryCallback<WidgetDto?>) {
        executorService.execute {
            val newDtoId: Long = widgetDao.add(widgetDto)
            callback.onResultSuccessful(widgetDao.get(newDtoId))
        }
    }

    operator fun get(appWidgetId: Int, callback: DbQueryCallback<WidgetDto?>) {
        executorService.execute { callback.onResultSuccessful(widgetDao.get(appWidgetId)) }
    }

    fun getAll(callback: DbQueryCallback<List<WidgetDto?>?>) {
        executorService.execute { callback.onResultSuccessful(widgetDao.all) }
    }

    fun getAll(widgetProviderClassName: String?, callback: DbQueryCallback<List<WidgetDto?>?>) {
        executorService.execute { callback.onResultSuccessful(widgetDao.getAll(widgetProviderClassName)) }
    }

    operator fun get(widgetDtoId: Long, callback: DbQueryCallback<WidgetDto?>) {
        executorService.execute { callback.onResultSuccessful(widgetDao.get(widgetDtoId)) }
    }

    fun update(widgetDto: WidgetDto, callback: DbQueryCallback<WidgetDto?>?) {
        executorService.execute {
            widgetDao.update(widgetDto)
            if (callback != null) {
                callback.onResultSuccessful(widgetDao.get(widgetDto.id))
            }
        }
    }

    fun delete(appWidgetId: Int, callback: DbQueryCallback<Boolean?>?) {
        executorService.execute {
            widgetDao.delete(appWidgetId)
            if (callback != null) {
                callback.onResultSuccessful(true)
            }
        }
    }

    companion object {
        var iNSTANCE: WidgetRepository? = null
            private set

        @JvmStatic
        fun initialize(context: Context) {
            if (iNSTANCE == null) iNSTANCE = WidgetRepository(context)
        }
    }
}