package com.lifedawn.bestweather.commons.classes

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R

class LocationLifeCycleObserver(private val mRegistry: ActivityResultRegistry, activity: Activity) : DefaultLifecycleObserver {
    private val context: Context
    private var activity: Activity?
    private var requestOnGpsLauncher: ActivityResultLauncher<Intent>? = null
    private var moveToAppDetailSettingsLauncher: ActivityResultLauncher<Intent>? = null
    private var backgroundLocationPermissionLauncher: ActivityResultLauncher<Intent>? = null
    private var requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var gpsResultCallback: ActivityResultCallback<ActivityResult>? = null
    private var appSettingsResultCallback: ActivityResultCallback<ActivityResult>? = null
    private var backgroundLocationPermissionResultCallback: ActivityResultCallback<ActivityResult>? = null
    private var permissionResultCallback: ActivityResultCallback<Map<String, Boolean>>? = null

    init {
        this.activity = activity
        context = activity.applicationContext
    }

    override fun onCreate(owner: LifecycleOwner) {
        requestOnGpsLauncher = mRegistry.register("gps", owner, StartActivityForResult(), ActivityResultCallback<ActivityResult> { result ->
            if (activity == null || activity!!.isFinishing) return@ActivityResultCallback

            //gps 사용확인 화면에서 나온뒤 현재 위치 다시 파악
            if (gpsResultCallback != null) {
                gpsResultCallback!!.onActivityResult(result)
            }
        })
        moveToAppDetailSettingsLauncher =
            mRegistry.register("appSettings", StartActivityForResult(), ActivityResultCallback<ActivityResult> { result ->
                if (activity == null || activity!!.isFinishing) return@ActivityResultCallback
                if (appSettingsResultCallback != null) {
                    appSettingsResultCallback!!.onActivityResult(result)
                }
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                        activity!!.getString(R.string.pref_key_never_ask_again_permission_for_access_location), false
                    ).apply()
                }
            })
        requestLocationPermissionLauncher =
            mRegistry.register("locationPermissions", RequestMultiplePermissions(), ActivityResultCallback<Map<String, Boolean>> { result ->
                if (activity == null || activity!!.isFinishing) return@ActivityResultCallback
                if (permissionResultCallback != null) {
                    permissionResultCallback!!.onActivityResult(result)
                }
                //gps사용 권한
                //허가남 : 현재 위치 다시 파악
                //거부됨 : 작업 취소
                //계속 거부 체크됨 : 작업 취소
                if (!result.containsValue(false)) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                        activity!!.getString(R.string.pref_key_never_ask_again_permission_for_access_location), false
                    ).apply()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
                        || !ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION)
                    ) {
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                            activity!!.getString(R.string.pref_key_never_ask_again_permission_for_access_location), true
                        ).apply()
                    }
                }
            })
        backgroundLocationPermissionLauncher = mRegistry.register("backgroundLocationPermission", StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (activity == null || activity!!.isFinishing) return@ActivityResultCallback
                if (backgroundLocationPermissionResultCallback != null) {
                    backgroundLocationPermissionResultCallback!!.onActivityResult(result)
                }
            })
    }

    override fun onStart(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onStart(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super@DefaultLifecycleObserver.onDestroy(owner)
        activity = null
    }

    fun launchGpsLauncher(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        gpsResultCallback = callback
        requestOnGpsLauncher!!.launch(intent)
    }

    fun launchAppSettingsLauncher(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        appSettingsResultCallback = callback
        moveToAppDetailSettingsLauncher!!.launch(intent)
    }

    fun launchPermissionsLauncher(input: Array<String>, callback: ActivityResultCallback<Map<String, Boolean>>) {
        permissionResultCallback = callback
        requestLocationPermissionLauncher!!.launch(input)
    }

    fun launchBackgroundLocationPermissionLauncher(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        backgroundLocationPermissionResultCallback = callback
        backgroundLocationPermissionLauncher!!.launch(intent)
    }
}