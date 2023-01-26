package com.lifedawn.bestweather.ui.settings.fragments

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.interfaces.IAppbarTitle
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.data.local.room.repository.WidgetRepository
import com.lifedawn.bestweather.ui.settings.custompreferences.WidgetRefreshIntervalPreference

class SettingsFragment(iAppbarTitle: IAppbarTitle) : PreferenceFragmentCompat() {
    private val iAppbarTitle: IAppbarTitle
    private var sharedPreferences: SharedPreferences? = null
    private var unitsPreference: Preference? = null
    private var appThemePreference: Preference? = null
    private var weatherDataSourcesPreference: Preference? = null
    private var widgetRefreshIntervalPreference: WidgetRefreshIntervalPreference? = null
    private var useCurrentLocationPreference: SwitchPreference? = null
    private var animationPreference: SwitchPreference? = null
    private var redrawWidgetsPreference: Preference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_settings_main_preference, rootKey)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key -> })
        unitsPreference = findPreference<Preference>(getString(R.string.pref_key_value_units))
        appThemePreference = findPreference<Preference>(getString(R.string.pref_key_app_theme))
        weatherDataSourcesPreference = findPreference<Preference>(getString(R.string.pref_key_weather_data_sources))
        useCurrentLocationPreference = findPreference<SwitchPreference>(getString(R.string.pref_key_use_current_location))
        animationPreference = findPreference<SwitchPreference>(getString(R.string.pref_key_show_background_animation))
        redrawWidgetsPreference = findPreference<Preference>(getString(R.string.pref_key_redraw_widgets))
        useCurrentLocationPreference!!.onPreferenceChangeListener = onPreferenceChangeListener
        animationPreference!!.onPreferenceChangeListener = onPreferenceChangeListener
        initPreferences()
    }

    private fun initPreferences() {
        //위젯 업데이트
        widgetRefreshIntervalPreference = WidgetRefreshIntervalPreference(getContext())
        widgetRefreshIntervalPreference.setKey(getString(R.string.pref_key_widget_refresh_interval))
        widgetRefreshIntervalPreference.setTitle(R.string.pref_title_widget_refresh_interval)
        widgetRefreshIntervalPreference.setValue(sharedPreferences!!.getLong(getString(R.string.pref_key_widget_refresh_interval), 0L))
        widgetRefreshIntervalPreference.setWidgetLayoutResource(R.layout.custom_preference_layout)
        widgetRefreshIntervalPreference.setIconSpaceReserved(false)
        widgetRefreshIntervalPreference.setOnPreferenceClickListener(Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(getActivity()).setTitle(getString(R.string.pref_title_widget_refresh_interval))
                .setSingleChoiceItems(widgetRefreshIntervalPreference.getWidgetRefreshIntervalTexts(),
                    widgetRefreshIntervalPreference.getCurrentValueIndex(),
                    DialogInterface.OnClickListener { dialog, which ->
                        val newValue: Long = widgetRefreshIntervalPreference
                            .getWidgetRefreshIntervalLongValues().get(which)
                        sharedPreferences!!.edit()
                            .putLong(widgetRefreshIntervalPreference.getKey(), newValue).commit()
                        widgetRefreshIntervalPreference.setValue(newValue)
                        val widgetRepository: WidgetRepository = WidgetRepository.getINSTANCE()
                        widgetRepository.getAll(object : DbQueryCallback<List<WidgetDto?>?>() {
                            fun onResultSuccessful(result: List<WidgetDto?>) {
                                val widgetHelper = WidgetHelper(requireContext().getApplicationContext())
                                if (result.isEmpty()) {
                                    widgetHelper.cancelAutoRefresh()
                                } else {
                                    widgetHelper.onSelectedAutoRefreshInterval(newValue)
                                }
                            }

                            fun onResultNoData() {}
                        })
                        MyApplication.loadValueUnits(requireContext().getApplicationContext(), true)
                        dialog.dismiss()
                    }).create().show()
            true
        })
        val preferenceScreen: PreferenceScreen = getPreferenceManager().getPreferenceScreen()
        preferenceScreen.addPreference(widgetRefreshIntervalPreference)


        //값 단위
        unitsPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            iAppbarTitle.setAppbarTitle(getString(R.string.pref_title_value_units))
            val unitsFragment = UnitsFragment()
            getParentFragmentManager().beginTransaction().hide(this@SettingsFragment).add(
                R.id.fragment_container, unitsFragment,
                getString(R.string.tag_units_fragment)
            ).addToBackStack(getString(R.string.tag_units_fragment)).commit()
            true
        }

        //앱 테마
        appThemePreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val currentAppTheme: AppThemes = AppThemes.valueOf(sharedPreferences!!.getString(appThemePreference!!.key, ""))
            val checkedItem = 0
            val appThemes = arrayOf<CharSequence>(getString(R.string.black), getString(R.string.white))
            MaterialAlertDialogBuilder(getActivity(), R.attr.alertDialogStyle).setTitle(
                getString(R.string.pref_title_app_theme)
            ).setSingleChoiceItems(appThemes, checkedItem,
                DialogInterface.OnClickListener { dialog, which ->
                    if (checkedItem != which) {
                        when (which) {
                            0 -> sharedPreferences!!.edit().putString(appThemePreference!!.key, AppThemes.BLACK.name).apply()
                            else -> {}
                        }
                        dialog.dismiss()
                        getActivity().finish()
                        startActivity(Intent(getActivity(), MainActivity::class.java))
                    }
                }).create().show()
            true
        }

        //날씨 제공사
        weatherDataSourcesPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            iAppbarTitle.setAppbarTitle(getString(R.string.pref_title_weather_data_sources))
            val weatherSourcesFragment = WeatherSourcesFragment()
            getParentFragmentManager().beginTransaction().hide(this@SettingsFragment).add(
                R.id.fragment_container,
                weatherSourcesFragment, getString(R.string.tag_weather_data_sources_fragment)
            ).addToBackStack(
                getString(R.string.tag_weather_data_sources_fragment)
            ).commit()
            true
        }
        redrawWidgetsPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val widgetHelper = WidgetHelper(requireContext().getApplicationContext())
            widgetHelper.reDrawWidgets(null)
            Toast.makeText(requireContext().getApplicationContext(), R.string.pref_title_redraw_widgets, Toast.LENGTH_SHORT).show()
            true
        }

        //현재 위치 사용
        useCurrentLocationPreference!!.isChecked = sharedPreferences!!.getBoolean(getString(R.string.pref_key_use_current_location), true)
        animationPreference!!.isChecked = sharedPreferences!!.getBoolean(getString(R.string.pref_key_show_background_animation), true)
    }

    private val onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
        if (preference.key == useCurrentLocationPreference!!.key) {
            val enabled = newValue as Boolean
            sharedPreferences!!.edit().putString(getString(R.string.pref_key_last_current_location_latitude), "0.0").putString(
                getString(R.string.pref_key_last_current_location_longitude), "0.0"
            ).commit()
            return@OnPreferenceChangeListener if (enabled != useCurrentLocationPreference!!.isChecked) {
                true
            } else {
                false
            }
        } else if (preference.key == animationPreference!!.key) {
            val enabled = newValue as Boolean
            sharedPreferences!!.edit().putBoolean(getString(R.string.pref_key_show_background_animation), enabled).commit()
            return@OnPreferenceChangeListener if (enabled != animationPreference!!.isChecked) {
                true
            } else {
                false
            }
        }
        false
    }

    init {
        this.iAppbarTitle = iAppbarTitle
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            iAppbarTitle.setAppbarTitle(getString(R.string.settings))
        }
    }
}