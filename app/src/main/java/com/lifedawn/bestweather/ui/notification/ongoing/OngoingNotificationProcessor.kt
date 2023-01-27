package com.lifedawn.bestweather.ui.notification.ongoing

import android.content.Context
import android.graphics.*
import android.location.Location
import android.os.Build
import android.text.TextPaint
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationResult
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.Geocoding
import com.lifedawn.bestweather.commons.classes.Geocoding.nominatimReverseGeocoding
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil.onBeginProcess
import com.lifedawn.bestweather.commons.classes.location.FusedLocation
import com.lifedawn.bestweather.commons.constants.WeatherDataType
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.commons.constants.WidgetNotiConstants.DataTypeOfIcon
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback
import com.lifedawn.bestweather.commons.utils.DeviceUtils.Companion.isScreenOn
import com.lifedawn.bestweather.commons.views.ProgressResultView.onSuccessful
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto.countryCode
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto.latitude
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto.longitude
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.countryCode
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.displayName
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.latitude
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.longitude
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.util.WeatherRequestUtil.loadWeatherData
import com.lifedawn.bestweather.ui.notification.NotificationHelper
import com.lifedawn.bestweather.ui.notification.NotificationHelper.NotificationObj
import com.lifedawn.bestweather.ui.notification.NotificationType
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotiViewCreator.OnRemoteViewsCallback
import java.time.ZoneId

class OngoingNotificationProcessor private constructor() {
    fun loadCurrentLocation(
        context: Context, ongoingNotificationDto: OngoingNotificationDto, collapsedRemoteViews: RemoteViews,
        expandedRemoteViews: RemoteViews, backgroundWorkCallback: BackgroundWorkCallback?
    ) {
        val fusedLocation: FusedLocation = FusedLocation(context)
        val locationCallback: FusedLocation.MyLocationCallback = object : MyLocationCallback() {
            fun onSuccessful(locationResult: LocationResult?) {
                val zoneId: ZoneId = ZoneId.of(PreferenceManager.getDefaultSharedPreferences(context).getString("zoneId", ""))
                val location: Location = getBestLocation(locationResult)
                nominatimReverseGeocoding(context, location.getLatitude(), location.getLongitude(), object : ReverseGeocodingCallback() {
                    fun onReverseGeocodingResult(address: Geocoding.AddressDto) {
                        ongoingNotificationDto.setDisplayName(address.displayName)
                        ongoingNotificationDto.setCountryCode(address.countryCode)
                        ongoingNotificationDto.setLatitude(address.latitude)
                        ongoingNotificationDto.setLongitude(address.longitude)
                        ongoingNotificationDto.setZoneId(zoneId.getId())
                        loadWeatherData(context, ongoingNotificationDto, collapsedRemoteViews, expandedRemoteViews, backgroundWorkCallback)
                    }
                })
            }

            fun onFailed(fail: Fail) {
                var errorType: RemoteViewsUtil.ErrorType? = null
                if (fail === Fail.DENIED_LOCATION_PERMISSIONS) {
                    errorType = RemoteViewsUtil.ErrorType.DENIED_GPS_PERMISSIONS
                } else if (fail === Fail.DISABLED_GPS) {
                    errorType = RemoteViewsUtil.ErrorType.GPS_OFF
                } else if (fail === Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION) {
                    errorType = RemoteViewsUtil.ErrorType.DENIED_BACKGROUND_LOCATION_PERMISSION
                } else {
                    errorType = RemoteViewsUtil.ErrorType.FAILED_LOAD_WEATHER_DATA
                }
                val ongoingNotiViewCreator: OngoingNotiViewCreator = OngoingNotiViewCreator(context, null)
                val remoteViews: Array<RemoteViews?>? = ongoingNotiViewCreator.createFailedNotification(errorType)
                makeNotification(
                    context, ongoingNotificationDto, remoteViews!!.get(0), remoteViews.get(1), R.mipmap.ic_launcher_round, null, true,
                    backgroundWorkCallback
                )
            }
        }
        if (isScreenOn(context)) {
            fusedLocation.findCurrentLocation(locationCallback, false)
        } else {
            val lastLocation: LocationResult = fusedLocation.lastCurrentLocation
            if (lastLocation.getLocations().get(0).getLatitude() == 0.0 ||
                lastLocation.getLocations().get(0).getLongitude() == 0.0
            ) {
                fusedLocation.findCurrentLocation(locationCallback, false)
            } else {
                locationCallback.onSuccessful(lastLocation)
            }
        }
    }

    fun loadWeatherData(
        context: Context, ongoingNotificationDto: OngoingNotificationDto, collapsedRemoteViews: RemoteViews,
        expandedRemoteViews: RemoteViews, backgroundWorkCallback: BackgroundWorkCallback?
    ) {
        onBeginProcess(expandedRemoteViews)
        onBeginProcess(collapsedRemoteViews)
        makeNotification(
            context,
            ongoingNotificationDto,
            collapsedRemoteViews,
            expandedRemoteViews,
            R.mipmap.ic_launcher_round,
            null,
            false,
            null
        )
        val weatherDataTypeSet: Set<WeatherDataType?> = requestWeatherDataTypeSet
        var weatherProviderType: WeatherProviderType? = ongoingNotificationDto.getWeatherSourceType()
        if (ongoingNotificationDto.isTopPriorityKma() && (ongoingNotificationDto.getCountryCode() == "KR")) {
            weatherProviderType = WeatherProviderType.KMA_WEB
        }
        val weatherProviderTypeSet: MutableSet<WeatherProviderType?> = HashSet()
        weatherProviderTypeSet.add(weatherProviderType)
        if (weatherDataTypeSet.contains(WeatherDataType.airQuality)) {
            weatherProviderTypeSet.add(WeatherProviderType.AQICN)
        }
        val zoneId: ZoneId = ZoneId.of(ongoingNotificationDto.getZoneId())
        val ongoingNotiViewCreator: OngoingNotiViewCreator = OngoingNotiViewCreator(context, ongoingNotificationDto)
        val finalWeatherProviderType: WeatherProviderType? = weatherProviderType
        loadWeatherData(context, MyApplication.getExecutorService(),
            ongoingNotificationDto.getLatitude(), ongoingNotificationDto.getLongitude(), weatherDataTypeSet,
            object : MultipleWeatherRestApiCallback() {
                public override fun onResult() {
                    ongoingNotiViewCreator.setResultViews(collapsedRemoteViews, expandedRemoteViews, finalWeatherProviderType, this,
                        object : OnRemoteViewsCallback {
                            public override fun onCreateFinished(
                                collapsedRemoteViews: RemoteViews?,
                                expandedRemoteViews: RemoteViews?,
                                icon: Int,
                                temperature: String?,
                                isFinished: Boolean
                            ) {
                                makeNotification(
                                    context,
                                    ongoingNotificationDto,
                                    collapsedRemoteViews,
                                    expandedRemoteViews,
                                    icon,
                                    temperature,
                                    isFinished,
                                    backgroundWorkCallback
                                )
                            }
                        })
                }

                public override fun onCanceled() {
                    ongoingNotiViewCreator.setResultViews(collapsedRemoteViews, expandedRemoteViews, finalWeatherProviderType, this,
                        object : OnRemoteViewsCallback {
                            public override fun onCreateFinished(
                                collapsedRemoteViews: RemoteViews?,
                                expandedRemoteViews: RemoteViews?,
                                icon: Int,
                                temperature: String?,
                                isFinished: Boolean
                            ) {
                                makeNotification(
                                    context, ongoingNotificationDto, collapsedRemoteViews, expandedRemoteViews, icon,
                                    temperature, isFinished, backgroundWorkCallback
                                )
                            }
                        })
                }
            }, weatherProviderTypeSet, zoneId
        )
    }

    fun makeNotification(
        context: Context, ongoingNotificationDto: OngoingNotificationDto, collapsedRemoteViews: RemoteViews?,
        expandedRemoteViews: RemoteViews?, icon: Int,
        temperature: String?, isFinished: Boolean, backgroundWorkCallback: BackgroundWorkCallback?
    ) {
        val notificationHelper: NotificationHelper = NotificationHelper(context)
        val notificationObj: NotificationObj? = notificationHelper.createNotification(NotificationType.Ongoing)
        val builder: NotificationCompat.Builder? = notificationObj.getNotificationBuilder()
        if (isFinished) {
            if (temperature != null) {
                if (ongoingNotificationDto.getDataTypeOfIcon() === DataTypeOfIcon.TEMPERATURE) {
                    val textSize: Int = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 18f,
                        context.getResources().getDisplayMetrics()
                    ).toInt()
                    val textPaint: TextPaint = TextPaint()
                    textPaint.setColor(Color.WHITE)
                    textPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD))
                    textPaint.setTextAlign(Paint.Align.CENTER)
                    textPaint.setTextScaleX(0.9f)
                    textPaint.setAntiAlias(true)
                    textPaint.setTextSize(textSize.toFloat())
                    val textRect: Rect = Rect()
                    textPaint.getTextBounds(temperature, 0, temperature.length, textRect)
                    val iconSize: Int = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 24f,
                        context.getResources().getDisplayMetrics()
                    ).toInt()
                    val iconBitmap: Bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
                    val canvas: Canvas = Canvas(iconBitmap)
                    canvas.drawText(temperature, canvas.getWidth() / 2f, canvas.getHeight() / 2f + textRect.height() / 2f, textPaint)
                    builder!!.setSmallIcon(IconCompat.createWithBitmap(iconBitmap))
                } else {
                    builder!!.setSmallIcon(icon)
                }
            } else {
                builder!!.setSmallIcon(icon)
            }
        } else {
            builder!!.setSmallIcon(icon)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setDefaults(0).setVibrate(null).setSound(null).setLights(0, 0, 0)
                .setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoCancel(false).setOngoing(true).setOnlyAlertOnce(true)
                .setCustomContentView(collapsedRemoteViews).setCustomBigContentView(expandedRemoteViews).setSilent(true)
        } else {
            builder.setAutoCancel(false).setOngoing(true).setOnlyAlertOnce(true)
                .setCustomContentView(expandedRemoteViews).setCustomBigContentView(expandedRemoteViews).setSilent(true)
        }
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManager.notify(NotificationType.Ongoing.getNotificationId(), builder.build())
        if (isFinished && backgroundWorkCallback != null) backgroundWorkCallback.onFinished()
    }

    protected val requestWeatherDataTypeSet: Set<WeatherDataType?>
        protected get() {
            val set: MutableSet<WeatherDataType?> = HashSet()
            set.add(WeatherDataType.currentConditions)
            set.add(WeatherDataType.airQuality)
            set.add(WeatherDataType.hourlyForecast)
            return set
        }

    companion object {
        var iNSTANCE: OngoingNotificationProcessor? = null
            get() {
                if (field == null) field = OngoingNotificationProcessor()
                return field
            }
            private set
    }
}