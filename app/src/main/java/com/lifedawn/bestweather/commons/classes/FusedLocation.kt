package com.lifedawn.bestweather.commons.classes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.internal.ConnectionCallbacks
import com.google.android.gms.common.api.internal.OnConnectionFailedListener
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.utils.TimeZoneUtils
import com.lifedawn.bestweather.commons.utils.TimeZoneUtils.TimeZoneCallback
import com.lifedawn.bestweather.ui.notification.NotificationHelper
import com.lifedawn.bestweather.ui.notification.NotificationType
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FusedLocation(private val context: Context) : ConnectionCallbacks, OnConnectionFailedListener {
    private val fusedLocationClient: FusedLocationProviderClient
    private val locationManager: LocationManager
    private val networkStatus: NetworkStatus
    private val locationRequestObjMap: MutableMap<MyLocationCallback, LocationRequestObj> = ConcurrentHashMap()
    private var timerTask: TimerTask? = null
    private val timer = Timer()

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        networkStatus = NetworkStatus.getInstance(context)
    }

    override fun onConnected(bundle: Bundle?) {}
    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    val lastCurrentLocation: LocationResult
        get() {
            val locations: MutableList<Location> = ArrayList()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context
            )
            val latitude =
                sharedPreferences.getString(context.getString(R.string.pref_key_last_current_location_latitude), "0.0")!!.toDouble()
            val longitude =
                sharedPreferences.getString(context.getString(R.string.pref_key_last_current_location_longitude), "0.0")!!.toDouble()
            val location = Location("")
            location.latitude = latitude
            location.longitude = longitude
            locations.add(location)
            return LocationResult.create(locations)
        }

    fun findCurrentLocation(myLocationCallback: MyLocationCallback, isBackground: Boolean) {
        if (!isOnGps) {
            myLocationCallback.onFailed(MyLocationCallback.Fail.DISABLED_GPS)
        } else if (!isOnNetwork) {
            myLocationCallback.onFailed(MyLocationCallback.Fail.FAILED_FIND_LOCATION)
        } else {
            if (checkDefaultPermissions()) {
                if (isBackground && !checkBackgroundLocationPermission()) {
                    myLocationCallback.onFailed(MyLocationCallback.Fail.DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION)
                    return
                }
                notifyNotification()
                val cancellationTokenSource = CancellationTokenSource()
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY.toLong()).build()
                val locationCallback: LocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        if (timerTask != null) timerTask!!.cancel()
                        cancelNotification()
                        locationRequestObjMap.remove(myLocationCallback)
                        fusedLocationClient.removeLocationUpdates(this)
                        if (locationResult != null) {
                            if (locationResult.locations.size > 0) {
                                val location = myLocationCallback.getBestLocation(locationResult)
                                val latitude = location.latitude
                                val longitude = location.longitude
                                TimeZoneUtils.INSTANCE.getTimeZone(latitude, longitude) { zoneId ->
                                    onResultTimeZone(
                                        latitude, longitude,
                                        zoneId,
                                        myLocationCallback, locationResult
                                    )
                                }
                            } else {
                                myLocationCallback.onFailed(MyLocationCallback.Fail.FAILED_FIND_LOCATION)
                            }
                        } else {
                            myLocationCallback.onFailed(MyLocationCallback.Fail.FAILED_FIND_LOCATION)
                        }
                    }

                    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                        super.onLocationAvailability(locationAvailability)
                    }
                }
                timerTask = object : TimerTask() {
                    override fun run() {
                        cancelNotification()
                        locationRequestObjMap.remove(myLocationCallback)
                        cancellationTokenSource.cancel()
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        MainThreadWorker.runOnUiThread({ myLocationCallback.onFailed(MyLocationCallback.Fail.TIME_OUT) })
                    }
                }
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                val locationRequestObj = LocationRequestObj()
                locationRequestObjMap[myLocationCallback] = locationRequestObj
                timer.schedule(timerTask, 6000L)
                @SuppressLint("MissingPermission") val currentLocationTask = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )
                locationRequestObj.currentLocationTask = currentLocationTask
                locationRequestObj.cancellationTokenSource = cancellationTokenSource
                currentLocationTask.addOnSuccessListener(OnSuccessListener { location ->
                    if (!currentLocationTask.isCanceled) {
                        if (location == null) {
                            locationRequestObj.locationCallback = locationCallback
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                        } else {
                            val locations: MutableList<Location> = ArrayList()
                            locations.add(location)
                            val latitude = location.latitude
                            val longitude = location.longitude
                            TimeZoneUtils.INSTANCE.getTimeZone(latitude, longitude, object : TimeZoneCallback {
                                override fun onResult(zoneId: ZoneId) {
                                    onResultTimeZone(latitude, longitude, zoneId, object : MyLocationCallback {
                                        override fun onSuccessful(locationResult: LocationResult?) {
                                            locationCallback.onLocationResult((locationResult)!!)
                                        }

                                        override fun onFailed(fail: MyLocationCallback.Fail?) {}
                                    }, LocationResult.create(locations))
                                }
                            })
                        }
                    }
                })
            } else {
                myLocationCallback.onFailed(MyLocationCallback.Fail.DENIED_LOCATION_PERMISSIONS)
            }
        }
    }

    private fun onResultTimeZone(
        latitude: Double, longitude: Double, zoneId: ZoneId, myLocationCallback: MyLocationCallback,
        locationResult: LocationResult
    ) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(
            context.getString(R.string.pref_key_last_current_location_latitude),
            latitude.toString()
        )
            .putString(
                context.getString(R.string.pref_key_last_current_location_longitude),
                longitude.toString()
            )
            .putString("zoneId", zoneId.id).commit()
        MainThreadWorker.runOnUiThread(object : Runnable {
            override fun run() {
                myLocationCallback.onSuccessful(locationResult)
            }
        })
    }

    fun checkDefaultPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun checkBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    val isOnGps: Boolean
        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isOnNetwork: Boolean
        get() = networkStatus.networkAvailable()

    fun onDisabledGps(
        activity: Activity, locationLifeCycleObserver: LocationLifeCycleObserver,
        gpsResultCallback: ActivityResultCallback<ActivityResult?>?
    ) {
        MaterialAlertDialogBuilder(activity).setMessage(activity.getString(R.string.request_to_make_gps_on)).setPositiveButton(
            activity.getString(R.string.check), object : DialogInterface.OnClickListener {
                override fun onClick(paramDialogInterface: DialogInterface, paramInt: Int) {
                    locationLifeCycleObserver.launchGpsLauncher(IntentUtil.locationSettingsIntent, (gpsResultCallback)!!)
                }
            }).setNegativeButton(activity.getString(R.string.no), object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface, i: Int) {}
        }).setCancelable(false).create().show()
    }

    fun onRejectPermissions(
        activity: Activity?, locationLifeCycleObserver: LocationLifeCycleObserver,
        appSettingsResultCallback: ActivityResultCallback<ActivityResult?>?,
        permissionsResultCallback: ActivityResultCallback<Map<String?, Boolean?>?>?
    ) {
        // 다시 묻지 않음을 선택했는지 확인
        val neverAskAgain = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_never_ask_again_permission_for_access_location), false
        )
        if (neverAskAgain) {
            locationLifeCycleObserver.launchAppSettingsLauncher(IntentUtil.getAppSettingsIntent(activity), (appSettingsResultCallback)!!)
        } else {
            locationLifeCycleObserver.launchPermissionsLauncher(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), (permissionsResultCallback)!!
            )
        }
    }

    fun cancel(myLocationCallback: MyLocationCallback) {
        if (locationRequestObjMap.containsKey(myLocationCallback)) {
            cancelNotification()
            val locationRequestObj = locationRequestObjMap[myLocationCallback]
            if (timerTask != null) timerTask!!.cancel()
            if (locationRequestObj == null) {
                return
            }
            if (locationRequestObj.locationCallback != null) {
                fusedLocationClient.removeLocationUpdates(locationRequestObj.locationCallback!!)
            }
            if (locationRequestObj.currentLocationTask != null) {
                locationRequestObj.cancellationTokenSource!!.cancel()
            }
            locationRequestObjMap.remove(myLocationCallback)
        }
    }

    private fun notifyNotification() {
        val notificationHelper = NotificationHelper(context)
        val notificationObj = notificationHelper.createNotification(NotificationType.Location)
        val builder = notificationObj.notificationBuilder
        builder.setSmallIcon(R.drawable.location).setContentText(context.getString(R.string.msg_finding_current_location))
            .setContentTitle(context.getString(R.string.current_location))
            .setOngoing(false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        val notification = notificationObj.notificationBuilder.build()
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NotificationType.Location.notificationId, notification)
    }

    private fun cancelNotification() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NotificationType.Location.notificationId)
    }

    interface MyLocationCallback {
        enum class Fail {
            DISABLED_GPS, DENIED_LOCATION_PERMISSIONS, FAILED_FIND_LOCATION, DENIED_ACCESS_BACKGROUND_LOCATION_PERMISSION, TIME_OUT
        }

        fun onSuccessful(locationResult: LocationResult?)
        fun onFailed(fail: Fail?)
        fun getBestLocation(locationResult: LocationResult): Location {
            var bestIndex = 0
            var accuracy = Float.MIN_VALUE
            val locations = locationResult.locations
            for (i in locations.indices) {
                if (locations[i].accuracy > accuracy) {
                    accuracy = locations[i].accuracy
                    bestIndex = i
                }
            }
            return locations[bestIndex]
        }
    }

    private class LocationRequestObj() {
        var locationCallback: LocationCallback? = null
        var currentLocationTask: Task<Location>? = null
        var cancellationTokenSource: CancellationTokenSource? = null
    }
}