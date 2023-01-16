package com.lifedawn.bestweather.commons.classes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.lifedawn.bestweather.ui.main.MainActivity

object IntentUtil {
    @JvmStatic
    fun getNotificationSettingsIntent(context: Context): Intent {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
        }
        return intent
    }

    @JvmStatic
    fun getAppSettingsIntent(context: Context): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.packageName, null)
        return intent
    }

    @JvmStatic val locationSettingsIntent: Intent
        get() = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

    @JvmStatic
    fun getAppIntent(context: Context?): Intent {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }
}