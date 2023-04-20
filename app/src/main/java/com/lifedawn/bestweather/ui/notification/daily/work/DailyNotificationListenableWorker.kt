package com.lifedawn.bestweather.ui.notification.daily.work

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.ui.notification.NotificationType
import com.lifedawn.bestweather.ui.notification.daily.DailyPushNotificationType
import java.time.ZoneId
import java.util.concurrent.*

class DailyNotificationListenableWorker(context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {
    private val action: String?
    private val repository: DailyPushNotificationRepository
    private val id: Int
    private val dailyPushNotificationType: DailyPushNotificationType
    private var viewCreator: AbstractDailyNotiViewCreator? = null

    init {
        action = workerParams.inputData.getString("action")
        repository = DailyPushNotificationRepository.getINSTANCE()
        id = workerParams.inputData.getInt(BundleKey.dtoId.name, -1)
        dailyPushNotificationType = DailyPushNotificationType.valueOf(workerParams.inputData.getString("DailyPushNotificationType")!!)
    }

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture<Result>(CallbackToFutureAdapter.Resolver<Result> { completer: CallbackToFutureAdapter.Completer<Result?> ->
            val backgroundWorkCallback: BackgroundWorkCallback = object : BackgroundWorkCallback {
                override fun onFinished() {
                    completer.set(Result.success())
                }
            }
            if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                val notiHelper = DailyNotificationHelper(applicationContext)
                notiHelper.reStartNotifications(backgroundWorkCallback)
            } else {
                workNotification(
                    applicationContext,
                    Executors.newSingleThreadExecutor(),
                    id,
                    dailyPushNotificationType,
                    backgroundWorkCallback
                )
            }
            backgroundWorkCallback
        })
    }

    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        val notificationHelper = NotificationHelper(applicationContext)
        val notificationObj: NotificationObj = notificationHelper.createNotification(NotificationType.ForegroundService)
        val builder: NotificationCompat.Builder = notificationObj.getNotificationBuilder()
        builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(applicationContext.getString(R.string.msg_refreshing_weather_data))
            .setContentTitle(applicationContext.getString(R.string.msg_refreshing_weather_data))
            .setOnlyAlertOnce(true).setWhen(0).setOngoing(true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        val notification: Notification = notificationObj.getNotificationBuilder().build()
        val notificationId = System.currentTimeMillis().toInt()
        val foregroundInfo = ForegroundInfo(notificationId, notification)
        return object : ListenableFuture<ForegroundInfo?> {
            override fun addListener(listener: Runnable, executor: Executor) {}
            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                return false
            }

            override fun isCancelled(): Boolean {
                return false
            }

            override fun isDone(): Boolean {
                return true
            }

            @Throws(ExecutionException::class, InterruptedException::class)
            override fun get(): ForegroundInfo {
                return foregroundInfo
            }

            @Throws(ExecutionException::class, InterruptedException::class, TimeoutException::class)
            override fun get(timeout: Long, unit: TimeUnit): ForegroundInfo {
                return foregroundInfo
            }
        }
    }

    override fun onStopped() {
        super.onStopped()
    }

    fun workNotification(
        context: Context?, executorService: ExecutorService?, notificationDtoId: Int?,
        type: DailyPushNotificationType?, backgroundWorkCallback: BackgroundWorkCallback
    ) {
        repository.get(notificationDtoId, object : DbQueryCallback<DailyPushNotificationDto?>() {
            fun onResultSuccessful(dto: DailyPushNotificationDto) {
                when (type) {
                    DailyPushNotificationType.First -> viewCreator = FirstDailyNotificationViewCreator(context)
                    DailyPushNotificationType.Second -> viewCreator = SecondDailyNotificationViewCreator(context)
                    DailyPushNotificationType.Third -> viewCreator = ThirdDailyNotificationViewCreator(context)
                    DailyPushNotificationType.Fourth -> viewCreator = FourthDailyNotificationViewCreator(context)
                    else -> viewCreator = FifthDailyNotificationViewCreator(context)
                }
                viewCreator.setBackgroundCallback(backgroundWorkCallback)
                val remoteViews: RemoteViews = viewCreator.createRemoteViews(false)
                if (dto.locationType === LocationType.CurrentLocation) {
                    loadCurrentLocation(context, executorService, remoteViews, dto, backgroundWorkCallback)
                } else {
                    loadWeatherData(context, executorService, remoteViews, dto, backgroundWorkCallback)
                }
            }

            fun onResultNoData() {
                backgroundWorkCallback.onFinished()
            }
        })
    }

    fun loadCurrentLocation(
        context: Context?, executorService: ExecutorService?, remoteViews: RemoteViews?,
        dailyPushNotificationDto: DailyPushNotificationDto, backgroundWorkCallback: BackgroundWorkCallback
    ) {
        val notificationHelper = NotificationHelper(applicationContext)
        val fusedLocation = FusedLocation(context)
        val locationCallback: FusedLocation.MyLocationCallback = object : MyLocationCallback() {
            fun onSuccessful(locationResult: LocationResult?) {
                val zoneId = ZoneId.of(PreferenceManager.getDefaultSharedPreferences(context!!).getString("zoneId", ""))
                notificationHelper.cancelNotification(NotificationType.Location.notificationId)
                val location: Location = getBestLocation(locationResult)
                Geocoding.nominatimReverseGeocoding(context, location.latitude, location.longitude,
                    object : ReverseGeocodingCallback() {
                        fun onReverseGeocodingResult(address: Geocoding.AddressDto) {
                            dailyPushNotificationDto.addressName = address.displayName
                            dailyPushNotificationDto.countryCode = address.countryCode
                            dailyPushNotificationDto.latitude = address.latitude.doubleValue()
                            dailyPushNotificationDto.longitude = address.longitude.doubleValue()
                            dailyPushNotificationDto.zoneId = zoneId.id
                            repository.update(dailyPushNotificationDto, null)
                            loadWeatherData(context, executorService, remoteViews, dailyPushNotificationDto, backgroundWorkCallback)
                        }
                    })
            }

            fun onFailed(fail: Fail) {
                notificationHelper.cancelNotification(NotificationType.Location.notificationId)
                var failText: String? = null
                failText = if (fail === Fail.DENIED_LOCATION_PERMISSIONS) {
                    applicationContext.getString(R.string.message_needs_location_permission)
                } else if (fail === Fail.DISABLED_GPS) {
                    applicationContext.getString(R.string.request_to_make_gps_on)
                } else if (fail === Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
                    applicationContext.getString(R.string.message_needs_background_location_permission)
                } else {
                    applicationContext.getString(R.string.failedFindingLocation)
                }
                viewCreator.makeFailedNotification(dailyPushNotificationDto.id, failText)
                wakeLock()
                backgroundWorkCallback.onFinished()
            }
        }
        fusedLocation.findCurrentLocation(locationCallback, true)
    }

    fun loadWeatherData(
        context: Context?, executorService: ExecutorService?, remoteViews: RemoteViews?,
        dailyPushNotificationDto: DailyPushNotificationDto, backgroundWorkCallback: BackgroundWorkCallback
    ) {
        val weatherDataTypeSet: Set<WeatherDataType> = viewCreator.getRequestWeatherDataTypeSet()
        val weatherProviderTypeSet: MutableSet<WeatherProviderType> = HashSet<WeatherProviderType>()
        if (dailyPushNotificationDto.weatherProviderType != null) weatherProviderTypeSet.add(dailyPushNotificationDto.weatherProviderType)
        if (dailyPushNotificationDto.isShowAirQuality) weatherProviderTypeSet.add(WeatherProviderType.AQICN)
        if (dailyPushNotificationDto.isTopPriorityKma && dailyPushNotificationDto.countryCode == "KR") {
            weatherProviderTypeSet.clear()
            weatherProviderTypeSet.add(WeatherProviderType.KMA_WEB)
        }
        WeatherRequestUtil.loadWeatherData(context,
            executorService,
            dailyPushNotificationDto.latitude,
            dailyPushNotificationDto.longitude,
            weatherDataTypeSet,
            object : MultipleWeatherRestApiCallback() {
                override fun onResult() {
                    viewCreator.setResultViews(remoteViews, dailyPushNotificationDto, weatherProviderTypeSet, this, weatherDataTypeSet)
                    wakeLock()
                    backgroundWorkCallback.onFinished()
                }

                override fun onCanceled() {
                    backgroundWorkCallback.onFinished()
                }
            },
            weatherProviderTypeSet,
            ZoneId.of(dailyPushNotificationDto.zoneId)
        )
    }

    private fun wakeLock() {
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "TAG:WAKE_NOTIFICATION"
        )
        wakeLock.acquire(4000L)
        wakeLock.release()
    }
}