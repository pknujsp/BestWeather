package com.lifedawn.bestweather.ui.widget.view

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.databinding.ActivityDialogBinding

class DialogActivity : Activity() {
    private var binding: ActivityDialogBinding? = null
    private var appWidgetId = 0
    private var widgetCreator: AbstractWidgetCreator? = null
    private var alertDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(0))
        binding = ActivityDialogBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        val bundle = intent.extras
        appWidgetId = bundle!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        val componentName = appWidgetProviderInfo.provider
        val providerClassName = componentName.className
        if (providerClassName == FirstWidgetProvider::class.java.getName()) {
            widgetCreator = FirstWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == SecondWidgetProvider::class.java.getName()) {
            widgetCreator = SecondWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == ThirdWidgetProvider::class.java.getName()) {
            widgetCreator = ThirdWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == FourthWidgetProvider::class.java.getName()) {
            widgetCreator = FourthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == FifthWidgetProvider::class.java.getName()) {
            widgetCreator = FifthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == SixthWidgetProvider::class.java.getName()) {
            widgetCreator = SixthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == SeventhWidgetProvider::class.java.getName()) {
            widgetCreator = SeventhWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == EighthWidgetProvider::class.java.getName()) {
            widgetCreator = EighthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == NinthWidgetProvider::class.java.getName()) {
            widgetCreator = NinthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == TenthWidgetProvider::class.java.getName()) {
            widgetCreator = TenthWidgetCreator(applicationContext, null, appWidgetId)
        } else if (providerClassName == EleventhWidgetProvider::class.java.getName()) {
            widgetCreator = EleventhWidgetCreator(applicationContext, null, appWidgetId)
        }
        val widgetProviderClass: Class<*> = widgetCreator.widgetProviderClass()
        val dialogView = layoutInflater.inflate(R.layout.view_widget_dialog, null)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val refreshInterval = sharedPreferences.getLong(getString(R.string.pref_key_widget_refresh_interval), 0L)
        var refreshIntervalText: String? = null
        if (refreshInterval > 0) {
            val autoRefreshIntervalsValue = resources.getStringArray(R.array.AutoRefreshIntervalsLong)
            val autoRefreshIntervalsText = resources.getStringArray(R.array.AutoRefreshIntervals)
            for (i in autoRefreshIntervalsValue.indices) {
                if (refreshInterval.toString() == autoRefreshIntervalsValue[i]) {
                    refreshIntervalText = autoRefreshIntervalsText[i]
                    break
                }
            }
        } else {
            refreshIntervalText = getString(R.string.disable_auto_refresh)
        }
        (dialogView.findViewById<View>(R.id.auto_refresh_interval) as TextView).text = refreshIntervalText
        (dialogView.findViewById<View>(R.id.widgetTypeName) as TextView).text = appWidgetProviderInfo.loadLabel(
            packageManager
        )
        (dialogView.findViewById<View>(R.id.openAppBtn) as TextView).setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            alertDialog!!.dismiss()
            startActivity(intent)
            finish()
        }
        (dialogView.findViewById<View>(R.id.updateBtn) as TextView).setOnClickListener {
            val pendingIntent: PendingIntent = widgetCreator.getRefreshPendingIntent()
            try {
                pendingIntent.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
            alertDialog!!.dismiss()
            finish()
        }
        (dialogView.findViewById<View>(R.id.cancelBtn) as TextView).setOnClickListener {
            alertDialog!!.dismiss()
            finish()
        }
        widgetCreator.loadSavedSettings(object : DbQueryCallback<WidgetDto?>() {
            fun onResultSuccessful(result: WidgetDto?) {
                MainThreadWorker.runOnUiThread(Runnable {
                    if (this@DialogActivity.isFinishing || this@DialogActivity.isDestroyed) {
                        return@Runnable
                    }
                    alertDialog = AlertDialog.Builder(
                        ContextThemeWrapper(
                            this@DialogActivity,
                            R.style.Theme_AppCompat_Light_Dialog
                        )
                    )
                        .setCancelable(true)
                        .setOnCancelListener { dialog ->
                            dialog.dismiss()
                            finish()
                        }
                        .setView(dialogView)
                        .create()
                    alertDialog!!.show()
                    alertDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                })
            }

            fun onResultNoData() {}
        })
    }
}