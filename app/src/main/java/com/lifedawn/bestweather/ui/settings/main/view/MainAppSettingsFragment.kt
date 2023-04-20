package com.lifedawn.bestweather.ui.settings.main.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.settings.refreshwidgets.WidgetRefreshIntervalPreference
import com.lifedawn.bestweather.ui.settings.main.viewmodel.MainAppSettingsViewModel
import kotlinx.coroutines.launch

class MainAppSettingsFragment() : PreferenceFragmentCompat() {
    private lateinit var unitsPreference: Preference
    private lateinit var appThemePreference: Preference
    private lateinit var weatherDataSourcesPreference: Preference
    private lateinit var widgetRefreshIntervalPreference: WidgetRefreshIntervalPreference
    private lateinit var useCurrentLocationPreference: SwitchPreference
    private lateinit var animationPreference: SwitchPreference
    private lateinit var redrawWidgetsPreference: Preference

    private val appSettingsViewModel by activityViewModels<MainAppSettingsViewModel>()

    private val onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        if (preference.key == useCurrentLocationPreference.key) {
            val enabled = newValue as Boolean
            lifecycleScope.launch {
                appSettingsViewModel.saveLastCoordinates("", "")
            }
        } else if (preference.key == animationPreference.key) {
            val enabled = newValue as Boolean
            lifecycleScope.launch {
                appSettingsViewModel.saveEnabledBackgroundAnimation(enabled)
            }
        }
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_app_settings, rootKey)

        unitsPreference = findPreference(getString(R.string.pref_key_value_units))!!
        appThemePreference = findPreference(getString(R.string.pref_key_app_theme))!!
        weatherDataSourcesPreference = findPreference(getString(R.string.pref_key_weather_data_sources))!!
        useCurrentLocationPreference = findPreference(getString(R.string.pref_key_use_current_location))!!
        animationPreference = findPreference(getString(R.string.pref_key_show_background_animation))!!
        redrawWidgetsPreference = findPreference(getString(R.string.pref_key_redraw_widgets))!!
        widgetRefreshIntervalPreference = findPreference(getString(R.string.pref_key_widget_refresh_interval))!!

        useCurrentLocationPreference.onPreferenceChangeListener = onPreferenceChangeListener
        animationPreference.onPreferenceChangeListener = onPreferenceChangeListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreferences()
    }

    private fun initPreferences() {
        //위젯 업데이트
        widgetRefreshIntervalPreference.setValue(appSettingsViewModel.widgetRefreshIntervalFlow.value as Long)

        lifecycleScope.launch {
            appSettingsViewModel.widgetRefreshIntervalFlow.collect {
                widgetRefreshIntervalPreference.setValue(it as Long)
            }
        }

        //현재 위치 사용
        useCurrentLocationPreference.isChecked = appSettingsViewModel.enabledCurrentLocationFlow.value
        animationPreference.isChecked = appSettingsViewModel.enabledBackgroundAnimationFlow.value

        widgetRefreshIntervalPreference.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.pref_title_widget_refresh_interval))
                .setSingleChoiceItems(widgetRefreshIntervalPreference.widgetRefreshIntervalTexts,
                    widgetRefreshIntervalPreference.currentValueIndex,
                    DialogInterface.OnClickListener { dialog, which ->
                        val newValue = widgetRefreshIntervalPreference
                            .widgetRefreshIntervalLongValues[which]

                        lifecycleScope.launch {
                            appSettingsViewModel.saveWidgetRefreshInterval(newValue)
                        }

                        // 값 바꾸면서 위젯에 적용

                        dialog.dismiss()
                    }).create().show()
            true
        }


        //값 단위
        unitsPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            // 클릭시 설정 화면으로 이동
            /*
            val unitsFragment = UnitsFragment()
            getParentFragmentManager().beginTransaction().hide(this@MainAppSettingsFragment).add(
                R.id.fragment_container, unitsFragment,
                getString(R.string.tag_units_fragment)
            ).addToBackStack(getString(R.string.tag_units_fragment)).commit()

             */
            true
        }

        //날씨 제공사
        weatherDataSourcesPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            // 클릭 시 설정 화면으로 이동
            /*
            val weatherProvidersFragment = WeatherProvidersFragment()
            getParentFragmentManager().beginTransaction().hide(this@MainAppSettingsFragment).add(
                R.id.fragment_container,
                weatherProvidersFragment, getString(R.string.tag_weather_data_sources_fragment)
            ).addToBackStack(
                getString(R.string.tag_weather_data_sources_fragment)
            ).commit()

             */
            true
        }

        redrawWidgetsPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            // 클릭 시 위젯 업데이트
            /*
            val widgetHelper = WidgetHelper(requireContext().getApplicationContext())
            widgetHelper.reDrawWidgets(null)
            Toast.makeText(requireContext().getApplicationContext(), R.string.pref_title_redraw_widgets, Toast.LENGTH_SHORT).show()

             */
            true
        }


    }


}