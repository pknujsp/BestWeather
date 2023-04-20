package com.lifedawn.bestweather.ui.settings.valueunits.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.ui.settings.valueunits.preference.UnitPreference
import com.lifedawn.bestweather.ui.settings.valueunits.viewmodel.ValueUnitsSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UnitsFragment : PreferenceFragmentCompat() {
    private lateinit var tempPreference: UnitPreference
    private lateinit var windPreference: UnitPreference
    private lateinit var visibilityPreference: UnitPreference
    private lateinit var clockPreference: UnitPreference

    private val valueUnitsSettingsViewModel by viewModels<ValueUnitsSettingsViewModel>()

    internal enum class ValueType {
        Temp, Wind, Visibility, Clock
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.unit_preference, rootKey)

        tempPreference = findPreference(getString(R.string.pref_key_unit_temp))!!
        windPreference = findPreference(getString(R.string.pref_key_unit_wind))!!
        visibilityPreference = findPreference(getString(R.string.pref_key_unit_visibility))!!
        clockPreference = findPreference(getString(R.string.pref_key_unit_clock))!!
    }

    private fun initPreferences() {
        tempPreference.unit = valueUnitsSettingsViewModel.tempFlow.value as ValueUnits
        tempPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.pref_title_unit_temp))
                .setSingleChoiceItems(
                    getList(tempPreference), getCheckedItem(ValueType.Temp, tempPreference.key)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            tempPreference.unit = ValueUnits.celsius
                        }
                        1 -> {
                            tempPreference.unit = ValueUnits.fahrenheit
                        }
                    }

                    lifecycleScope.launch {
                        valueUnitsSettingsViewModel.saveTempUnit(tempPreference.unit)
                    }

                    dialog.dismiss()
                }.create().show()
            true
        }

        //바람
        windPreference.unit = valueUnitsSettingsViewModel.windSpeedFlow.value as ValueUnits
        windPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.pref_title_unit_wind))
                .setSingleChoiceItems(
                    getList(windPreference), getCheckedItem(ValueType.Wind, windPreference.key)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            windPreference.unit = ValueUnits.mPerSec
                        }
                        1 -> {
                            windPreference.unit = ValueUnits.kmPerHour
                        }
                    }

                    lifecycleScope.launch {
                        valueUnitsSettingsViewModel.saveWindUnit(windPreference.unit)
                    }

                    // 값 업데이트
                    dialog.dismiss()
                }.create().show()
            true
        }
        preferenceScreen.addPreference(windPreference!!)

        //시정거리
        visibilityPreference.unit = valueUnitsSettingsViewModel.visibilityFlow.value as ValueUnits
        visibilityPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.pref_title_unit_visibility))
                .setSingleChoiceItems(
                    getList(visibilityPreference), getCheckedItem(ValueType.Visibility, visibilityPreference.key)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            visibilityPreference.unit = ValueUnits.km
                        }
                        1 -> {
                            visibilityPreference.unit = ValueUnits.mile
                        }
                    }

                    lifecycleScope.launch {
                        valueUnitsSettingsViewModel.saveVisibilityUnit(visibilityPreference.unit)
                    }
                    // 값 업데이트
                    dialog.dismiss()
                }.create().show()
            true
        }

        //시간제
        clockPreference.unit = valueUnitsSettingsViewModel.clockFlow.value as ValueUnits
        clockPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(clockPreference.title)
                .setSingleChoiceItems(
                    getList(clockPreference), getCheckedItem(ValueType.Clock, clockPreference.key)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            clockPreference.unit = ValueUnits.clock12
                        }
                        1 -> {
                            clockPreference.unit = ValueUnits.clock24
                        }
                    }

                    lifecycleScope.launch {
                        valueUnitsSettingsViewModel.saveClockUnit(clockPreference.unit)
                    }
                    dialog.dismiss()
                }.create().show()
            true
        }
    }

    private fun getCheckedItem(valueType: ValueType, key: String): Int {
        val valueUnit = ValueUnits.valueOf(key)

        if (valueType == ValueType.Temp) {
            return when (valueUnit) {
                ValueUnits.celsius -> 0
                ValueUnits.fahrenheit -> 1
                else -> -1
            }
        } else if (valueType == ValueType.Wind) {
            return when (valueUnit) {
                ValueUnits.mPerSec -> 0
                ValueUnits.kmPerHour -> 1
                else -> -1
            }
        } else if (valueType == ValueType.Visibility) {
            return when (valueUnit) {
                ValueUnits.km -> 0
                ValueUnits.mile -> 1
                else -> -1
            }
        } else {
            return when (valueUnit) {
                ValueUnits.clock12 -> 0
                ValueUnits.clock24 -> 1
                else -> -1
            }
        }
    }

    private fun getList(preference: Preference): Array<CharSequence> {
        return if (preference == tempPreference) {
            arrayOf(
                getString(R.string.celsius),
                getString(R.string.fahrenheit)
            )
        } else if (preference == windPreference) {
            arrayOf(
                getString(R.string.mPerSec),
                getString(R.string.kmPerHour)
            )
        } else if (preference == visibilityPreference) {
            arrayOf(getString(R.string.km), getString(R.string.mile))
        } else {
            arrayOf(
                getString(R.string.clock12),
                getString(R.string.clock24)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreferences()
    }
}