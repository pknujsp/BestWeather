package com.lifedawn.bestweather.ui.notification.ongoing.work

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.ui.notification.NotificationType
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class OngoingNotificationListenableWorker constructor(context: Context, workerParams: WorkerParameters) :
    ListenableWorker(context, workerParams) {
    private var ongoingNotiViewCreator: OngoingNotiViewCreator? = null
    private val ACTION: String?

    init {
        ACTION = workerParams.getInputData().getString("action")
    }

    public override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture<Result>(CallbackToFutureAdapter.Resolver<Result>({ completer: CallbackToFutureAdapter.Completer<Result?> ->
            val backgroundWorkCallback: BackgroundWorkCallback = BackgroundWorkCallback({
                if (!isStopped()) {
                    completer.set(Result.success())
                }
            })
            val repository: OngoingNotificationRepository = OngoingNotificationRepository.Companion.getINSTANCE()
            repository.getOngoingNotificationDto(object : DbQueryCallback<OngoingNotificationDto?>() {
                fun onResultSuccessful(ongoingNotificationDto: OngoingNotificationDto) {
                    if (((ACTION == Intent.ACTION_BOOT_COMPLETED) || (ACTION == Intent.ACTION_MY_PACKAGE_REPLACED) || (ACTION == getApplicationContext().getString(
                            R.string.com_lifedawn_bestweather_action_RESTART
                        )))
                    ) {
                        ongoingNotiViewCreator = OngoingNotiViewCreator(getApplicationContext(), ongoingNotificationDto)
                        reStartNotification(ongoingNotificationDto, backgroundWorkCallback)
                    } else if ((ACTION == getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
                        if (ongoingNotificationDto.getUpdateIntervalMillis() > 0) {
                            val ongoingNotificationHelper: OngoingNotificationHelper = OngoingNotificationHelper(getApplicationContext())
                            if (!ongoingNotificationHelper.isRepeating()) ongoingNotificationHelper.onSelectedAutoRefreshInterval(
                                ongoingNotificationDto.getUpdateIntervalMillis()
                            )
                        }
                        if (DeviceUtils.Companion.isScreenOn(getApplicationContext())) {
                            ongoingNotiViewCreator = OngoingNotiViewCreator(getApplicationContext(), ongoingNotificationDto)
                            createNotification(ongoingNotificationDto, backgroundWorkCallback)
                        } else {
                            backgroundWorkCallback.onFinished()
                        }
                    }
                }

                fun onResultNoData() {
                    val notificationHelper: NotificationHelper = NotificationHelper(getApplicationContext())
                    notificationHelper.cancelNotification(NotificationType.Ongoing.getNotificationId())
                    backgroundWorkCallback.onFinished()
                }
            })
            backgroundWorkCallback
        }))
    }

    public override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        val notificationId: Int = System.currentTimeMillis().toInt()
        val notificationHelper: NotificationHelper = NotificationHelper(getApplicationContext())
        val notificationObj: NotificationObj = notificationHelper.createNotification(NotificationType.ForegroundService)
        val builder: NotificationCompat.Builder = notificationObj.getNotificationBuilder()
        builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getApplicationContext().getString(R.string.updatingNotification))
            .setContentTitle(
                getApplicationContext().getString(R.string.updatingNotification)
            )
            .setOnlyAlertOnce(true).setWhen(0).setOngoing(true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        val notification: Notification = notificationObj.getNotificationBuilder().build()
        val foregroundInfo: ForegroundInfo = ForegroundInfo(notificationId, notification)
        return object : ListenableFuture<ForegroundInfo?> {
            public override fun addListener(listener: Runnable, executor: Executor) {}
            public override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                return false
            }

            public override fun isCancelled(): Boolean {
                return false
            }

            public override fun isDone(): Boolean {
                return true
            }

            @Throws(ExecutionException::class, InterruptedException::class)
            public override fun get(): ForegroundInfo {
                return foregroundInfo
            }

            @Throws(ExecutionException::class, InterruptedException::class, TimeoutException::class)
            public override fun get(timeout: Long, unit: TimeUnit): ForegroundInfo {
                return foregroundInfo
            }
        }
    }

    public override fun onStopped() {
        val notificationHelper: NotificationHelper = NotificationHelper(getApplicationContext())
        notificationHelper.cancelNotification(NotificationType.Ongoing.getNotificationId())
    }

    fun reStartNotification(ongoingNotificationDto: OngoingNotificationDto, callback: BackgroundWorkCallback) {
        val notificationHelper: NotificationHelper = NotificationHelper(getApplicationContext())
        val ongoingNotificationHelper: OngoingNotificationHelper = OngoingNotificationHelper(getApplicationContext())
        if (ongoingNotificationDto.getUpdateIntervalMillis() > 0 && !ongoingNotificationHelper.isRepeating()) {
            ongoingNotificationHelper.onSelectedAutoRefreshInterval(ongoingNotificationDto.getUpdateIntervalMillis())
        }
        val active: Boolean = notificationHelper.activeNotification(NotificationType.Ongoing.getNotificationId())
        if (active) {
            callback.onFinished()
        } else {
            createNotification(ongoingNotificationDto, callback)
        }
    }

    private fun createNotification(ongoingNotificationDto: OngoingNotificationDto, callback: BackgroundWorkCallback) {
        val remoteViews: Array<RemoteViews> = ongoingNotiViewCreator.createRemoteViews(false)
        val collapsedView: RemoteViews = remoteViews.get(0)
        val expandedView: RemoteViews = remoteViews.get(1)
        RemoteViewsUtil.onBeginProcess(expandedView)
        RemoteViewsUtil.onBeginProcess(collapsedView)
        val processor: OngoingNotificationProcessor = OngoingNotificationProcessor.Companion.getINSTANCE()
        processor.makeNotification(
            getApplicationContext(), ongoingNotificationDto, collapsedView, expandedView, R.drawable.refresh, null,
            false, null
        )
        if (ongoingNotificationDto.getLocationType() === LocationType.CurrentLocation) {
            processor.loadCurrentLocation(getApplicationContext(), ongoingNotificationDto, collapsedView, expandedView, callback)
        } else {
            processor.loadWeatherData(getApplicationContext(), ongoingNotificationDto, collapsedView, expandedView, callback)
        }
    }
}