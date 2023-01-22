package com.lifedawn.bestweather.ui.widget.work

import android.app.Notification
import android.appwidget.AppWidgetManager
import android.content.Context
import android.location.Location
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.ui.notification.NotificationType
import java.time.ZoneId
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WidgetListenableWorker(context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {
    private val currentLocationWidgetDtoArrayMap: MutableMap<Int, WidgetDto> = ConcurrentHashMap<Int, WidgetDto>()
    private val selectedLocationWidgetDtoArrayMap: MutableMap<Int, WidgetDto> = ConcurrentHashMap<Int, WidgetDto>()
    private val allWidgetDtoArrayMap: MutableMap<Int, WidgetDto> = ConcurrentHashMap<Int, WidgetDto>()
    private val multipleRestApiDownloaderMap: MutableMap<Int, MultipleWeatherRestApiCallback?> =
        ConcurrentHashMap<Int, MultipleWeatherRestApiCallback?>()
    private val widgetCreatorMap: MutableMap<Int, AbstractWidgetCreator?> = ConcurrentHashMap<Int, AbstractWidgetCreator?>()
    private var currentLocationResponseMultipleWeatherRestApiCallback: MultipleWeatherRestApiCallback? = null
    private val selectedLocationResponseMap: MutableMap<String?, MultipleWeatherRestApiCallback> =
        ConcurrentHashMap<String?, MultipleWeatherRestApiCallback>()
    private var currentLocationRequestObj: RequestObj?
    private val selectedLocationRequestMap: MutableMap<String?, RequestObj> = ConcurrentHashMap()
    private var widgetRepository: WidgetRepository? = null
    private var appWidgetManager: AppWidgetManager? = null
    private val executorService: ExecutorService = MyApplication.getExecutorService()
    private var fusedLocation: FusedLocation? = null
    private var requestCount = 0
    private val responseCount = AtomicInteger(0)
    private val ACTION: String?
    private val APP_WIDGET_ID: Int

    init {
        processing.set(true)
        if (widgetRepository == null) {
            widgetRepository = WidgetRepository.getINSTANCE()
        }
        if (appWidgetManager == null) {
            appWidgetManager = AppWidgetManager.getInstance(context)
        }
        val parameterData = workerParams.inputData
        ACTION = parameterData.getString("action")
        APP_WIDGET_ID = parameterData.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        currentLocationRequestObj = null
        currentLocationWidgetDtoArrayMap.clear()
        selectedLocationRequestMap.clear()
        selectedLocationResponseMap.clear()
        selectedLocationWidgetDtoArrayMap.clear()
        allWidgetDtoArrayMap.clear()
        multipleRestApiDownloaderMap.clear()
        widgetCreatorMap.clear()
    }

    override fun startWork(): ListenableFuture<Result> {
        processing.set(true)
        return CallbackToFutureAdapter.getFuture<Result>(CallbackToFutureAdapter.Resolver<Result> { completer: CallbackToFutureAdapter.Completer<Result?> ->
            val backgroundWorkCallback = BackgroundWorkCallback {
                completer.set(
                    Result.success()
                )
            }
            if (ACTION == applicationContext.getString(R.string.com_lifedawn_bestweather_action_INIT)) {
                widgetRepository.get(APP_WIDGET_ID, object : DbQueryCallback<WidgetDto?>() {
                    fun onResultSuccessful(widgetDto: WidgetDto) {
                        val widgetCreator: AbstractWidgetCreator? =
                            createWidgetViewCreator(APP_WIDGET_ID, widgetDto.widgetProviderClassName)
                        val widgetHelper = WidgetHelper(applicationContext)
                        val widgetRefreshInterval: Long = widgetHelper.getRefreshInterval()
                        if (widgetRefreshInterval > 0 && !widgetHelper.isRepeating()) {
                            widgetHelper.onSelectedAutoRefreshInterval(widgetRefreshInterval)
                        }
                        requestCount = 1
                        val remoteViewsCallback: RemoteViewsCallback = object : RemoteViewsCallback(requestCount) {
                            protected override fun onFinished(remoteViewsMap: Map<Int, RemoteViews?>) {
                                processing.set(false)
                                for (id in remoteViewsMap.keys) {
                                    appWidgetManager!!.updateAppWidget(id, remoteViewsMap[id])
                                }
                                backgroundWorkCallback.onFinished()
                            }
                        }
                        widgetCreator.setRemoteViewsCallback(remoteViewsCallback)
                        if (widgetDto.locationType === LocationType.CurrentLocation) {
                            currentLocationWidgetDtoArrayMap[widgetDto.appWidgetId] = widgetDto
                            allWidgetDtoArrayMap.putAll(currentLocationWidgetDtoArrayMap)
                            showProgressBar()
                            loadCurrentLocation(backgroundWorkCallback)
                        } else {
                            selectedLocationWidgetDtoArrayMap[widgetDto.appWidgetId] = widgetDto
                            val address: Geocoding.AddressDto = AddressDto(
                                widgetDto.latitude, widgetDto.longitude,
                                widgetDto.addressName, null, widgetDto.countryCode
                            )
                            val requestObj = RequestObj(address, ZoneId.of(widgetDto.timeZoneId))
                            requestObj.weatherDataTypeSet.addAll(widgetCreator.getRequestWeatherDataTypeSet())
                            requestObj.weatherProviderTypeSet.addAll(widgetDto.getWeatherProviderTypeSet())
                            requestObj.appWidgetSet.add(widgetDto.appWidgetId)
                            selectedLocationRequestMap[widgetDto.addressName] = requestObj
                            val addressList: MutableList<String?> = ArrayList()
                            addressList.add(widgetDto.addressName)
                            allWidgetDtoArrayMap.putAll(selectedLocationWidgetDtoArrayMap)
                            showProgressBar()
                            loadWeatherData(LocationType.SelectedAddress, addressList, backgroundWorkCallback)
                        }
                    }

                    fun onResultNoData() {
                        backgroundWorkCallback.onFinished()
                    }
                })
            } else if (ACTION == applicationContext.getString(R.string.com_lifedawn_bestweather_action_REFRESH)) {
                widgetRepository.getAll(object : DbQueryCallback<List<WidgetDto?>?>() {
                    fun onResultSuccessful(list: List<WidgetDto?>) {
                        val addressList: MutableList<String?> = ArrayList()
                        val remoteViewsCallback: RemoteViewsCallback = object : RemoteViewsCallback(list.size) {
                            protected override fun onFinished(remoteViewsMap: Map<Int, RemoteViews?>) {
                                processing.set(false)
                                for (id in remoteViewsMap.keys) {
                                    appWidgetManager!!.updateAppWidget(id, remoteViewsMap[id])
                                }
                                backgroundWorkCallback.onFinished()
                            }
                        }
                        for (widgetDto in list) {
                            val widgetCreator: AbstractWidgetCreator? = createWidgetViewCreator(
                                widgetDto.appWidgetId,
                                widgetDto.widgetProviderClassName
                            )
                            widgetCreator.setRemoteViewsCallback(remoteViewsCallback)
                            if (widgetDto.locationType === LocationType.CurrentLocation) {
                                currentLocationWidgetDtoArrayMap[widgetDto.appWidgetId] = widgetDto
                            } else {
                                selectedLocationWidgetDtoArrayMap[widgetDto.appWidgetId] = widgetDto
                                var requestObj = selectedLocationRequestMap[widgetDto.addressName]
                                if (requestObj == null) {
                                    val address: Geocoding.AddressDto = AddressDto(
                                        widgetDto.latitude, widgetDto.longitude,
                                        widgetDto.addressName, null, widgetDto.countryCode
                                    )
                                    requestObj = RequestObj(address, ZoneId.of(widgetDto.timeZoneId))
                                    selectedLocationRequestMap[widgetDto.addressName] = requestObj
                                    addressList.add(widgetDto.addressName)
                                }
                                requestObj.weatherDataTypeSet.addAll(widgetCreator.getRequestWeatherDataTypeSet())
                                requestObj.weatherProviderTypeSet.addAll(widgetDto.getWeatherProviderTypeSet())
                                requestObj.appWidgetSet.add(widgetDto.appWidgetId)
                            }
                        }
                        allWidgetDtoArrayMap.putAll(currentLocationWidgetDtoArrayMap)
                        allWidgetDtoArrayMap.putAll(selectedLocationWidgetDtoArrayMap)
                        showProgressBar()
                        requestCount = 0
                        if (!currentLocationWidgetDtoArrayMap.isEmpty()) {
                            requestCount++
                            loadCurrentLocation(backgroundWorkCallback)
                        }
                        if (!selectedLocationWidgetDtoArrayMap.isEmpty()) {
                            requestCount = addressList.size
                            loadWeatherData(LocationType.SelectedAddress, addressList, backgroundWorkCallback)
                        }
                    }

                    fun onResultNoData() {
                        backgroundWorkCallback.onFinished()
                    }
                })
            }
            backgroundWorkCallback
        })
    }

    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        val notificationHelper = NotificationHelper(applicationContext)
        val notificationObj: NotificationObj = notificationHelper.createNotification(NotificationType.ForegroundService)
        val builder: NotificationCompat.Builder = notificationObj.getNotificationBuilder()
        builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(applicationContext.getString(R.string.updatingWidgets))
            .setContentTitle(
                applicationContext.getString(R.string.updatingWidgets)
            )
            .setOnlyAlertOnce(true).setWhen(0).setOngoing(true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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
        processing.set(false)

        // WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
        // widgetHelper.reDrawWidgets(null);
    }

    private fun showProgressBar() {
        val tempWidgetCreator: AbstractWidgetCreator = EighthWidgetCreator(applicationContext, null, 0)
        val tempRemoteViews: RemoteViews = tempWidgetCreator.createRemoteViews()
        for (appWidgetId in allWidgetDtoArrayMap.keys) appWidgetManager!!.updateAppWidget(appWidgetId, tempRemoteViews)
    }

    private fun createWidgetViewCreator(appWidgetId: Int, widgetProviderClassName: String): AbstractWidgetCreator? {
        var widgetCreator: AbstractWidgetCreator? = null
        if (widgetProviderClassName == FirstWidgetProvider::class.java.getName()) {
            widgetCreator = FirstWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == SecondWidgetProvider::class.java.getName()) {
            widgetCreator = SecondWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == ThirdWidgetProvider::class.java.getName()) {
            widgetCreator = ThirdWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == FourthWidgetProvider::class.java.getName()) {
            widgetCreator = FourthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == FifthWidgetProvider::class.java.getName()) {
            widgetCreator = FifthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == SixthWidgetProvider::class.java.getName()) {
            widgetCreator = SixthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == SeventhWidgetProvider::class.java.getName()) {
            widgetCreator = SeventhWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == EighthWidgetProvider::class.java.getName()) {
            widgetCreator = EighthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == NinthWidgetProvider::class.java.getName()) {
            widgetCreator = NinthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == TenthWidgetProvider::class.java.getName()) {
            widgetCreator = TenthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (widgetProviderClassName == EleventhWidgetProvider::class.java.getName()) {
            widgetCreator = EleventhWidgetCreator(applicationContext, null, appWidgetId)
        }
        widgetCreatorMap[appWidgetId] = widgetCreator
        return widgetCreator
    }

    fun loadCurrentLocation(backgroundWorkCallback: BackgroundWorkCallback) {
        val appWidgetIdSet: Set<Int> = currentLocationWidgetDtoArrayMap.keys
        currentLocationRequestObj = RequestObj(null, null)
        for (appWidgetId in appWidgetIdSet) {
            currentLocationRequestObj!!.weatherDataTypeSet.addAll(widgetCreatorMap[appWidgetId].getRequestWeatherDataTypeSet())
            currentLocationRequestObj!!.weatherProviderTypeSet.addAll(currentLocationWidgetDtoArrayMap[appWidgetId].getWeatherProviderTypeSet())
            currentLocationRequestObj!!.appWidgetSet.add(appWidgetId)
        }
        val notificationHelper = NotificationHelper(applicationContext)
        val locationCallback: FusedLocation.MyLocationCallback = object : MyLocationCallback() {
            fun onSuccessful(locationResult: LocationResult?) {
                notificationHelper.cancelNotification(NotificationType.Location.notificationId)
                val location: Location = getBestLocation(locationResult)
                Geocoding.nominatimReverseGeocoding(
                    applicationContext, location.latitude, location.longitude,
                    object : ReverseGeocodingCallback() {
                        fun onReverseGeocodingResult(address: Geocoding.AddressDto?) {
                            if (address == null) {
                                onLocationResponse(Fail.FAILED_FIND_LOCATION, null, backgroundWorkCallback)
                            } else {
                                val addressName: String = address.displayName
                                currentLocationRequestObj!!.address = address
                                val zoneIdText = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                                    .getString("zoneId", "")
                                currentLocationRequestObj!!.zoneId = ZoneId.of(zoneIdText)
                                onResultCurrentLocation(addressName, address, backgroundWorkCallback)
                            }
                        }
                    })
            }

            fun onFailed(fail: Fail?) {
                notificationHelper.cancelNotification(NotificationType.Location.notificationId)
                onLocationResponse(fail, null, backgroundWorkCallback)
            }
        }
        fusedLocation = FusedLocation(applicationContext)
        fusedLocation.findCurrentLocation(locationCallback, true)
    }

    private fun onResultCurrentLocation(
        addressName: String,
        newAddress: Geocoding.AddressDto?,
        backgroundWorkCallback: BackgroundWorkCallback
    ) {
        for (appWidgetId in currentLocationWidgetDtoArrayMap.keys) {
            val widgetDto: WidgetDto? = currentLocationWidgetDtoArrayMap[appWidgetId]
            widgetDto.addressName = addressName
            widgetDto.countryCode = newAddress.countryCode
            widgetDto.latitude = newAddress.latitude
            widgetDto.longitude = newAddress.longitude
            widgetDto.timeZoneId = currentLocationRequestObj!!.zoneId!!.id
            widgetRepository.update(widgetDto, null)
        }
        onLocationResponse(null, addressName, backgroundWorkCallback)
    }

    private fun onLocationResponse(
        fail: FusedLocation.MyLocationCallback.Fail?, addressName: String?,
        backgroundWorkCallback: BackgroundWorkCallback
    ) {
        if (fail == null) {
            val addressesList: MutableList<String?> = ArrayList()
            addressesList.add(addressName)
            loadWeatherData(LocationType.CurrentLocation, addressesList, backgroundWorkCallback)
        } else {
            var errorType: RemoteViewsUtil.ErrorType? = null
            errorType = if (fail === FusedLocation.MyLocationCallback.Fail.DENIED_LOCATION_PERMISSIONS) {
                RemoteViewsUtil.ErrorType.DENIED_GPS_PERMISSIONS
            } else if (fail === FusedLocation.MyLocationCallback.Fail.DISABLED_GPS) {
                RemoteViewsUtil.ErrorType.GPS_OFF
            } else if (fail === FusedLocation.MyLocationCallback.Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
                RemoteViewsUtil.ErrorType.DENIED_BACKGROUND_LOCATION_PERMISSION
            } else {
                RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA
            }
            for (appWidgetId in currentLocationWidgetDtoArrayMap.keys) {
                allWidgetDtoArrayMap[appWidgetId].lastErrorType = errorType
                allWidgetDtoArrayMap[appWidgetId].isLoadSuccessful = false
            }
            onResponseResult(LocationType.CurrentLocation, null, backgroundWorkCallback)
        }
    }

    private fun loadWeatherData(locationType: LocationType, addressList: List<String?>, backgroundWorkCallback: BackgroundWorkCallback) {
        for (addressName in addressList) {
            val multipleWeatherRestApiCallback: MultipleWeatherRestApiCallback = object : MultipleWeatherRestApiCallback() {
                override fun onResult() {
                    onResponseResult(locationType, addressName, backgroundWorkCallback)
                }

                override fun onCanceled() {
                    onResponseResult(locationType, addressName, backgroundWorkCallback)
                }
            }
            var requestObj: RequestObj? = null
            if (locationType === LocationType.SelectedAddress) {
                requestObj = selectedLocationRequestMap[addressName]
                selectedLocationResponseMap[addressName] = multipleWeatherRestApiCallback
            } else {
                requestObj = currentLocationRequestObj
                currentLocationResponseMultipleWeatherRestApiCallback = multipleWeatherRestApiCallback
                var onlyKma = true
                for (appWidgetId in currentLocationWidgetDtoArrayMap.keys) {
                    if (currentLocationWidgetDtoArrayMap[appWidgetId].isTopPriorityKma && currentLocationWidgetDtoArrayMap[appWidgetId].countryCode == "KR") {
                        requestObj!!.weatherProviderTypeSet.add(WeatherProviderType.KMA_WEB)
                    } else if (!currentLocationWidgetDtoArrayMap[appWidgetId].isTopPriorityKma) {
                        onlyKma = false
                    }
                }
                if (onlyKma) {
                    requestObj!!.weatherProviderTypeSet.remove(WeatherProviderType.ACCU_WEATHER)
                    requestObj.weatherProviderTypeSet.remove(WeatherProviderType.MET_NORWAY)
                    requestObj.weatherProviderTypeSet.remove(WeatherProviderType.OWM_ONECALL)
                }
            }
            multipleWeatherRestApiCallback.setZoneId(requestObj!!.zoneId)
            WeatherRequestUtil.loadWeatherData(
                applicationContext, executorService, requestObj.address.latitude,
                requestObj.address.longitude, requestObj.weatherDataTypeSet, multipleWeatherRestApiCallback,
                requestObj.weatherProviderTypeSet, multipleWeatherRestApiCallback.zoneId
            )
        }
    }

    private fun onResponseResult(locationType: LocationType, addressName: String?, backgroundWorkCallback: BackgroundWorkCallback) {
        var restApiDownloader: MultipleWeatherRestApiCallback? = null
        var appWidgetIdSet: Set<Int>? = null
        if (locationType === LocationType.SelectedAddress) {
            restApiDownloader = selectedLocationResponseMap[addressName]
            appWidgetIdSet = selectedLocationRequestMap[addressName]!!.appWidgetSet
        } else {
            restApiDownloader = currentLocationResponseMultipleWeatherRestApiCallback
            appWidgetIdSet = currentLocationRequestObj!!.appWidgetSet
        }
        for (appWidgetId in appWidgetIdSet) {
            multipleRestApiDownloaderMap[appWidgetId] = restApiDownloader
            val widgetCreator: AbstractWidgetCreator? = widgetCreatorMap[appWidgetId]
            widgetCreator.setWidgetDto(allWidgetDtoArrayMap[appWidgetId])
            widgetCreator.setResultViews(
                appWidgetId,
                multipleRestApiDownloaderMap[appWidgetId], restApiDownloader.zoneId
            )
        }

        //응답 처리가 끝난 요청객체를 제거
        if (addressName != null && locationType === LocationType.SelectedAddress) {
            selectedLocationRequestMap.remove(addressName)
        } else if (locationType === LocationType.CurrentLocation) {
            currentLocationRequestObj = null
        }

        /*
		if (responseCount.incrementAndGet() == requestCount) {
			backgroundWorkCallback.onFinished();
		}

		 */
    }

    private class RequestObj(address: Geocoding.AddressDto?, zoneId: ZoneId?) {
        var address: Geocoding.AddressDto?
        var zoneId: ZoneId?
        var appWidgetSet: MutableSet<Int> = HashSet()
        var weatherDataTypeSet: MutableSet<WeatherDataType> = HashSet<WeatherDataType>()
        var weatherProviderTypeSet: MutableSet<WeatherProviderType> = HashSet<WeatherProviderType>()

        init {
            this.address = address
            this.zoneId = zoneId
        }
    }

    companion object {
        var processing = AtomicBoolean(false)
    }
}