package com.lifedawn.bestweather.ui.main

import android.app.PendingIntent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.MobileAds
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.NetworkStatus
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.databinding.ActivityMainBinding
import com.lifedawn.bestweather.ui.intro.IntroTransactionFragment
import com.lifedawn.bestweather.ui.notification.daily.DailyNotificationHelper
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationHelper
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationViewModel
import com.lifedawn.bestweather.ui.widget.WidgetHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject private lateinit var networkStatus: NetworkStatus
    private val initViewModel: InitViewModel by viewModels()
    private val ongoingNotificationViewModel: OngoingNotificationViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val content = findViewById<View>(R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (initViewModel.ready) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            })

        if (networkStatus.networkAvailable()) {
            nextStep()
        } else {
            AlertDialog.Builder(this).setTitle(R.string.networkProblem)
                .setMessage(com.lifedawn.bestweather.R.string.need_to_connect_network)
                .setPositiveButton(com.lifedawn.bestweather.R.string.check) { dialog, which ->
                    dialog.dismiss()
                    finish()
                }.setCancelable(false).create().show()
        }
    }

    private fun nextStep() {
        // 초기화
        MobileAds.initialize(this) { }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (sharedPreferences.getBoolean(getString(com.lifedawn.bestweather.R.string.pref_key_show_intro), true)) {
            val introTransactionFragment = IntroTransactionFragment()
            fragmentTransaction.add(
                binding.fragmentContainer.id, introTransactionFragment,
                IntroTransactionFragment::class.java.getName()
            ).commitNow()
        } else {
            initOngoingNotifications()
            initDailyNotifications()
            //initWidgets();
            val mainFragment = MainFragment()
            fragmentTransaction.add(
                binding.fragmentContainer.id, mainFragment,
                MainFragment::class.java.name
            ).commitNow()
        }
    }

    private fun initOngoingNotifications() {
        ongoingNotificationViewModel.getOngoingNotificationDto(object : DbQueryCallback<OngoingNotificationDto?>() {
            fun onResultSuccessful(result: OngoingNotificationDto) {
                if (result.isOn) {
                    //ongoing notification
                    val helper = OngoingNotificationHelper(applicationContext)
                    val pendingIntent: PendingIntent = helper.createManualPendingIntent(
                        getString(com.lifedawn.bestweather.R.string.com_lifedawn_bestweather_action_RESTART),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    try {
                        pendingIntent.send()
                    } catch (e: PendingIntent.CanceledException) {
                        e.printStackTrace()
                    }
                }
            }

            fun onResultNoData() {}
        })
    }

    fun initDailyNotifications() {
        val notiHelper = DailyNotificationHelper(applicationContext)
        notiHelper.reStartNotifications(null)
    }

    private fun initWidgets() {
        val widgetHelper = WidgetHelper(applicationContext)
        widgetHelper.reDrawWidgets(null)
    }
}