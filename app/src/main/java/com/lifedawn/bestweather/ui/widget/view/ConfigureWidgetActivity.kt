package com.lifedawn.bestweather.ui.widget.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import com.google.android.material.slider.Slider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.databinding.ActivityConfigureWidgetBinding
import com.lifedawn.bestweather.ui.findaddress.map.MapFragment
import com.lifedawn.bestweather.ui.widget.WidgetHelper

class ConfigureWidgetActivity : AppCompatActivity(), WidgetUpdateCallback {
    private var binding: ActivityConfigureWidgetBinding? = null
    private var appWidgetId: Int? = null
    private var layoutId: Int? = null
    private var widgetDto: WidgetDto? = null
    private var widgetRefreshIntervalValues: Array<Long?>
    private var originalWidgetRefreshIntervalValueIndex = 0
    private var widgetRefreshIntervalValueIndex = 0
    private var widgetHeight = 0
    private var widgetWidth = 0
    private var newSelectedAddressDto: FavoriteAddressDto? = null
    private var widgetCreator: AbstractWidgetCreator? = null
    private var appWidgetManager: AppWidgetManager? = null
    private var selectedFavoriteLocation = false
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!supportFragmentManager.popBackStackImmediate()) {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }
    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                super.onFragmentAttached(fm, f, context)
                if (f is MapFragment) {
                    binding!!.widgetSettingsContainer.visibility = View.GONE
                    binding!!.fragmentContainer.visibility = View.VISIBLE
                }
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentDestroyed(fm, f)
                if (f is MapFragment) {
                    binding!!.widgetSettingsContainer.visibility = View.VISIBLE
                    binding!!.fragmentContainer.visibility = View.GONE
                }
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var denied = false
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                denied = true
                break
            }
        }
        if (denied) {
            binding!!.loadingAnimation.visibility = View.GONE
        } else {
            setBackgroundImg()
        }
    }

    private fun setBackgroundImg() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            MyApplication.getExecutorService().submit(Runnable {
                val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                val wallpaperDrawable = wallpaperManager.drawable
                if (!isFinishing) {
                    MainThreadWorker.runOnUiThread(Runnable {
                        Glide.with(this@ConfigureWidgetActivity).load(wallpaperDrawable).into(binding!!.wallpaper)
                        binding!!.loadingAnimation.visibility = View.GONE
                    })
                }
            })
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 3)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigureWidgetBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.displayDatetimeSwitch.visibility = View.GONE
        binding!!.displayLocalDatetimeSwitch.visibility = View.GONE
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        val bundle = intent.extras
        if (bundle != null) {
            appWidgetId = bundle.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId!!)
        layoutId = appWidgetProviderInfo.initialLayout
        val componentName = appWidgetProviderInfo.provider
        val widgetProviderClassName = componentName.className
        if (widgetProviderClassName == FirstWidgetProvider::class.java.getName()) {
            widgetCreator = FirstWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == SecondWidgetProvider::class.java.getName()) {
            widgetCreator = SecondWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == ThirdWidgetProvider::class.java.getName()) {
            widgetCreator = ThirdWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == FourthWidgetProvider::class.java.getName()) {
            widgetCreator = FourthWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == FifthWidgetProvider::class.java.getName()) {
            widgetCreator = FifthWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == SixthWidgetProvider::class.java.getName()) {
            widgetCreator = SixthWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == SeventhWidgetProvider::class.java.getName()) {
            widgetCreator = SeventhWidgetCreator(applicationContext, this, appWidgetId)
            binding!!.weatherDataSourceLayout.visibility = View.GONE
        } else if (widgetProviderClassName == EighthWidgetProvider::class.java.getName()) {
            widgetCreator = EighthWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == NinthWidgetProvider::class.java.getName()) {
            widgetCreator = NinthWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == TenthWidgetProvider::class.java.getName()) {
            widgetCreator = TenthWidgetCreator(applicationContext, this, appWidgetId)
        } else if (widgetProviderClassName == EleventhWidgetProvider::class.java.getName()) {
            widgetCreator = EleventhWidgetCreator(applicationContext, this, appWidgetId)
            binding!!.kmaTopPrioritySwitch.setText(R.string.containsKma)
            binding!!.weatherDataSourceLayout.visibility = View.GONE
        }
        widgetDto = widgetCreator.loadDefaultSettings()
        widgetHeight = (appWidgetProviderInfo.minHeight * 1.9).toInt()
        val layoutParams = binding!!.previewLayout.layoutParams as FrameLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight() + TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f,
            resources.displayMetrics
        ).toInt()
        layoutParams.height = widgetHeight
        binding!!.previewLayout.layoutParams = layoutParams

        //위치, 날씨제공사, 대한민국 최우선, 자동 업데이트 간격, 날짜와 시각표시,
        //현지 시각으로 표시, 글자크기, 배경 투명도
        initLocation()
        initWeatherProvider()
        initAutoRefreshInterval()
        initDisplayDateTime()
        initTextSize()
        initBackground()
        setBackgroundImg()
        binding!!.currentLocationRadio.isChecked = true
        binding!!.save.setOnClickListener { v: View? ->
            widgetCreator.saveSettings(widgetDto, object : DbQueryCallback<WidgetDto?>() {
                fun onResultSuccessful(result: WidgetDto?) {
                    MainThreadWorker.runOnUiThread(Runnable {
                        if (originalWidgetRefreshIntervalValueIndex != widgetRefreshIntervalValueIndex) {
                            PreferenceManager.getDefaultSharedPreferences(applicationContext)
                                .edit().putLong(
                                    getString(R.string.pref_key_widget_refresh_interval),
                                    widgetRefreshIntervalValues[widgetRefreshIntervalValueIndex]!!
                                ).commit()
                            val widgetHelper = WidgetHelper(applicationContext)
                            widgetHelper.onSelectedAutoRefreshInterval(widgetRefreshIntervalValues[widgetRefreshIntervalValueIndex]!!)
                        }
                        val resultValue = Intent()
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setResult(Activity.RESULT_OK, resultValue)
                        val initBundle = Bundle()
                        initBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId!!)
                        val intent = Intent(applicationContext, widgetCreator.widgetProviderClass())
                        intent.action = getString(R.string.com_lifedawn_bestweather_action_INIT)
                        intent.putExtras(initBundle)
                        val pendingIntent = PendingIntent.getBroadcast(
                            applicationContext, IntentRequestCodes.WIDGET_MANUALLY_REFRESH.requestCode,
                            intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        try {
                            pendingIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }
                        finishAndRemoveTask()
                    })
                }

                fun onResultNoData() {}
            })
        }
        binding!!.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding!!.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                widgetWidth = binding!!.previewLayout.width
                updatePreview()
            }
        })
    }

    private fun initBackground() {
        binding!!.backgroundTransparencySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setBackgroundAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                setBackgroundAlpha(seekBar.progress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                setBackgroundAlpha(seekBar.progress)
            }
        })
    }

    private fun initTextSize() {
        binding!!.textSizeSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                setTextSizeInWidget(slider.value.toInt())
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                setTextSizeInWidget(slider.value.toInt())
            }
        })
    }

    private fun initDisplayDateTime() {
        binding!!.displayDatetimeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            binding!!.displayLocalDatetimeSwitch.visibility = if (isChecked) View.VISIBLE else View.GONE
            widgetDto.isDisplayClock = isChecked
            updatePreview()
        }
        binding!!.displayLocalDatetimeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            widgetDto.isDisplayLocalClock = isChecked
        }
    }

    private fun initAutoRefreshInterval() {
        val intervalsDescription = resources.getStringArray(R.array.AutoRefreshIntervals)
        val intervalsStr = resources.getStringArray(R.array.AutoRefreshIntervalsLong)
        widgetRefreshIntervalValues = arrayOfNulls(intervalsStr.size)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val currentValue = sharedPreferences.getLong(getString(R.string.pref_key_widget_refresh_interval), 0L)
        for (i in intervalsStr.indices) {
            widgetRefreshIntervalValues[i] = intervalsStr[i].toLong()
        }
        val spinnerAdapter: SpinnerAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, intervalsDescription)
        binding!!.autoRefreshIntervalSpinner.adapter = spinnerAdapter
        binding!!.autoRefreshIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                widgetRefreshIntervalValueIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        widgetRefreshIntervalValueIndex = 0
        for (v in intervalsStr) {
            if (currentValue.toString() == v) {
                break
            }
            widgetRefreshIntervalValueIndex++
        }
        originalWidgetRefreshIntervalValueIndex = widgetRefreshIntervalValueIndex
        binding!!.autoRefreshIntervalSpinner.setSelection(widgetRefreshIntervalValueIndex)
    }

    private fun initWeatherProvider() {
        binding!!.weatherDataSourceRadioGroup.setOnCheckedChangeListener { group, checkedId -> widgetDto.addWeatherProviderType(if (checkedId == R.id.owm_radio) WeatherProviderType.OWM_ONECALL else WeatherProviderType.MET_NORWAY) }
        binding!!.kmaTopPrioritySwitch.setOnCheckedChangeListener { buttonView, isChecked -> widgetDto.isTopPriorityKma = isChecked }
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean(getString(R.string.pref_key_open_weather_map), false)
        ) {
            binding!!.owmRadio.isChecked = true
        } else {
            binding!!.metNorwayRadio.isChecked = true
        }
    }

    private fun initLocation() {
        binding!!.changeAddressBtn.visibility = View.GONE
        binding!!.selectedAddressName.visibility = View.GONE
        binding!!.locationRadioGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            if (checkedId == binding!!.currentLocationRadio.id && binding!!.currentLocationRadio.isChecked) {
                binding!!.changeAddressBtn.visibility = View.GONE
                binding!!.selectedAddressName.visibility = View.GONE
                widgetDto.locationType = LocationType.CurrentLocation
            } else if (checkedId == binding!!.selectedLocationRadio.id && binding!!.selectedLocationRadio.isChecked) {
                binding!!.changeAddressBtn.visibility = View.VISIBLE
                binding!!.selectedAddressName.visibility = View.VISIBLE
                widgetDto.locationType = LocationType.SelectedAddress
                if (!selectedFavoriteLocation) {
                    openFavoritesFragment()
                }
            }
        }
        binding!!.changeAddressBtn.setOnClickListener { v: View? -> openFavoritesFragment() }
    }

    private fun openFavoritesFragment() {
        val mapFragment = MapFragment()
        val bundle = Bundle()
        bundle.putString(BundleKey.RequestFragment.name, ConfigureWidgetActivity::class.java.name)
        mapFragment.arguments = bundle
        mapFragment.setOnResultFavoriteListener(object : OnResultFavoriteListener {
            override fun onAddedNewAddress(
                newFavoriteAddressDto: FavoriteAddressDto,
                favoriteAddressDtoList: List<FavoriteAddressDto>,
                removed: Boolean
            ) {
                onClickedAddress(newFavoriteAddressDto)
                supportFragmentManager.popBackStack()
            }

            override fun onResult(favoriteAddressDtoList: List<FavoriteAddressDto>) {}
            override fun onClickedAddress(favoriteSelectedAddressDto: FavoriteAddressDto?) {
                if (favoriteSelectedAddressDto == null) {
                    if (!selectedFavoriteLocation) {
                        Toast.makeText(applicationContext, R.string.not_selected_address, Toast.LENGTH_SHORT).show()
                        binding!!.currentLocationRadio.isChecked = true
                    }
                } else {
                    selectedFavoriteLocation = true
                    newSelectedAddressDto = favoriteSelectedAddressDto
                    val widgetDto: WidgetDto = widgetCreator.getWidgetDto()
                    widgetDto.addressName = newSelectedAddressDto.displayName
                    widgetDto.countryCode = newSelectedAddressDto.countryCode
                    widgetDto.latitude = newSelectedAddressDto.latitude.toDouble()
                    widgetDto.longitude = newSelectedAddressDto.longitude.toDouble()
                    widgetDto.timeZoneId = newSelectedAddressDto.zoneId
                    binding!!.selectedAddressName.setText(newSelectedAddressDto.displayName)
                    supportFragmentManager.popBackStack()
                }
            }
        })
        val tag = MapFragment::class.java.name
        supportFragmentManager.beginTransaction().add(binding!!.fragmentContainer.id, mapFragment, tag)
            .addToBackStack(tag).commitAllowingStateLoss()
    }

    private fun setTextSizeInWidget(value: Int) {
        //widgetCreator.setTextSize(value);
        //updatePreview();
    }

    private fun setBackgroundAlpha(alpha: Int) {
        widgetDto.backgroundAlpha = 100 - alpha
        updatePreview()
    }

    override fun updatePreview() {
        binding!!.previewLayout.removeAllViews()
        val removeViews: RemoteViews = widgetCreator.createTempViews(widgetWidth, widgetHeight)
        val view = removeViews.apply(applicationContext, binding!!.previewLayout)
        binding!!.previewLayout.addView(view)
    }
}