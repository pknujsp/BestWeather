package com.lifedawn.bestweather.ui.main

import android.R
import android.app.PendingIntent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.MobileAds
import com.lifedawn.bestweather.commons.classes.NetworkStatus
import com.lifedawn.bestweather.commons.views.HeaderbarStyle
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.remote.flickr.repository.FlickrRepository
import com.lifedawn.bestweather.databinding.ActivityMainBinding
import com.lifedawn.bestweather.ui.intro.IntroTransactionFragment
import com.lifedawn.bestweather.ui.notification.daily.DailyNotificationHelper
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationHelper
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationViewModel
import com.lifedawn.bestweather.ui.weathers.viewmodels.WeatherFragmentViewModel
import com.lifedawn.bestweather.ui.widget.WidgetHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var networkStatus: NetworkStatus? = null
    private var initViewModel: InitViewModel? = null
    private var ongoingNotificationViewModel: OngoingNotificationViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen.installSplashScreen(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        initViewModel = ViewModelProvider(this).get(InitViewModel::class.java)
        val content = findViewById<View>(R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check if the initial data is ready.
                    return if (initViewModel!!.ready) {
                        // The content is ready; start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready; suspend.
                        false
                    }
                }
            })
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, this)
        networkStatus = NetworkStatus.getInstance(applicationContext)
        ongoingNotificationViewModel = ViewModelProvider(this).get<OngoingNotificationViewModel>(
            OngoingNotificationViewModel::class.java
        )
        if (networkStatus.networkAvailable()) {
            processNextStep()
        } else {
            AlertDialog.Builder(this).setTitle(com.lifedawn.bestweather.R.string.networkProblem)
                .setMessage(com.lifedawn.bestweather.R.string.need_to_connect_network)
                .setPositiveButton(com.lifedawn.bestweather.R.string.check) { dialog, which ->
                    dialog.dismiss()
                    finish()
                }.setCancelable(false).create().show()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onDestroy() {
        FlickrRepository.clear()
        WeatherFragmentViewModel.clear()
        super.onDestroy()
    }

    private fun processNextStep() {
        // 초기화
        MobileAds.initialize(this) { }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (sharedPreferences.getBoolean(getString(com.lifedawn.bestweather.R.string.pref_key_show_intro), true)) {
            val introTransactionFragment = IntroTransactionFragment()
            fragmentTransaction.add(
                binding!!.fragmentContainer.id, introTransactionFragment,
                IntroTransactionFragment::class.java.getName()
            ).commitNow()
        } else {
            initOngoingNotifications()
            initDailyNotifications()
            //initWidgets();
            val mainTransactionFragment = MainTransactionFragment()
            fragmentTransaction.add(
                binding!!.fragmentContainer.id, mainTransactionFragment,
                MainTransactionFragment::class.java.name
            ).commitNow()
        }
    }

    private fun initOngoingNotifications() {
        ongoingNotificationViewModel.getOngoingNotificationDto(object : DbQueryCallback<OngoingNotificationDto?>() {
            fun onResultSuccessful(result: OngoingNotificationDto) {
                if (result.isOn()) {
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