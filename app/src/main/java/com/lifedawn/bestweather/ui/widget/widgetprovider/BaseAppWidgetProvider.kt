package com.lifedawn.bestweather.ui.widget.widgetprovider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.work.*
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.local.room.dto.WidgetDto
import com.lifedawn.bestweather.data.local.room.repository.WidgetRepository
import com.lifedawn.bestweather.ui.widget.WidgetHelper
import com.lifedawn.bestweather.ui.widget.creator.AbstractWidgetCreator
import com.lifedawn.bestweather.ui.widget.work.WidgetListenableWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@AndroidEntryPoint
abstract class BaseAppWidgetProvider : AppWidgetProvider() {
    protected fun drawWidgets(context: Context?, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray?) {
        Executors.newSingleThreadExecutor().submit {
            val className = javaClass.name
            val backgroundWorkCallback: BackgroundWorkCallback = object : BackgroundWorkCallback {
                val requestCount = appWidgetIds!!.size
                val responseCount = AtomicInteger(0)
                override fun onFinished() {
                    val count = responseCount.incrementAndGet()
                    Log.e(className, "위젯 그리기 : " + Arrays.toString(appWidgetIds) + ", 요청 : " + requestCount + ", 응답 : " + count)
                    if (count == requestCount) {
                        Log.e(className, "위젯 그리기 완료 : " + Arrays.toString(appWidgetIds))
                    }
                }
            }
            for (appWidgetId in appWidgetIds!!) {
                if (appWidgetManager.getAppWidgetInfo(appWidgetId) == null) {
                    backgroundWorkCallback.onFinished()
                    continue
                }
                val widgetCreator: AbstractWidgetCreator =
                    AbstractWidgetCreator.Companion.getInstance(appWidgetManager, context, appWidgetId)
                widgetCreator.loadSavedSettings(object : DbQueryCallback<WidgetDto?>() {
                    fun onResultSuccessful(widgetDto: WidgetDto?) {
                        if (widgetDto != null) {
                            if (widgetDto.isInitialized) {
                                if (widgetDto.isLoadSuccessful) widgetCreator.setDataViewsOfSavedData() else {
                                    val remoteViews: RemoteViews = widgetCreator.createRemoteViews()
                                    widgetCreator.setRefreshPendingIntent(remoteViews)
                                    RemoteViewsUtil.onErrorProcess(remoteViews, context, widgetDto.lastErrorType)
                                    appWidgetManager.updateAppWidget(widgetCreator.getAppWidgetId(), remoteViews)
                                }
                            }
                        }
                        backgroundWorkCallback.onFinished()
                    }

                    fun onResultNoData() {
                        backgroundWorkCallback.onFinished()
                    }
                })
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.e(javaClass.name, Arrays.toString(appWidgetIds))
        if (!WidgetListenableWorker.Companion.processing.get()) drawWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val backgroundWorkCallback: BackgroundWorkCallback = object : BackgroundWorkCallback {
            val allDeletedCount = appWidgetIds.size
            val deletedCount = AtomicInteger(0)
            override fun onFinished() {
                if (deletedCount.incrementAndGet() == allDeletedCount) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    if (appWidgetManager.installedProviders.isEmpty()) {
                        val widgetHelper = WidgetHelper(context)
                        widgetHelper.cancelAutoRefresh()
                    }
                }
            }
        }
        val widgetRepository: WidgetRepository = WidgetRepository.getINSTANCE()
        for (appWidgetId in appWidgetIds) widgetRepository.delete(appWidgetId, object : DbQueryCallback<Boolean?>() {
            fun onResultSuccessful(result: Boolean?) {
                backgroundWorkCallback.onFinished()
            }

            fun onResultNoData() {}
        })
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        Log.e(javaClass.name, action!!)
        if (action != null) {
            val bundle = intent.extras
            if (action == context.getString(R.string.com_lifedawn_bestweather_action_INIT)) {
                val data = Data.Builder().putInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    bundle!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
                ).build()
                startWork(context, action, data)
            } else if (action == context.getString(R.string.com_lifedawn_bestweather_action_REFRESH)) {
                startWork(context, action, null)
            } else if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                val widgetHelper = WidgetHelper(context)
                widgetHelper.reDrawWidgets(null)
            } else if (action == context.getString(R.string.com_lifedawn_bestweather_action_REDRAW)) {
                val arr = bundle!!.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                drawWidgets(context, AppWidgetManager.getInstance(context), arr)
            }
        }
    }

    protected fun startWork(context: Context?, action: String, data: Data?) {
        val dataBuilder = Data.Builder()
            .putString("action", action)
        if (data != null) dataBuilder.putAll(data)
        val tag = "widget_$action"
        val request = OneTimeWorkRequest.Builder(WidgetListenableWorker::class.java)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(dataBuilder.build())
            .addTag(tag)
            .build()
        val workManager = WorkManager.getInstance(context!!)
        workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
    }
}