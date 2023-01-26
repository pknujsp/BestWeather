package com.lifedawn.bestweather.ui.settings.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.ui.settings.custompreferences.UnitPreference

class UnitsFragment : PreferenceFragmentCompat() {
    private var tempPreference: UnitPreference? = null
    private var windPreference: UnitPreference? = null
    private var visibilityPreference: UnitPreference? = null
    private var clockPreference: UnitPreference? = null
    private var sharedPreferences: SharedPreferences? = null

    internal enum class ValueType {
        temp, wind, visibility, clock
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.unit_preference, rootKey)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        initPreferences()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initPreferences() {
        //좌측 여백 없애기 :  app:iconSpaceReserved="false"
        val preferenceScreen = preferenceManager.preferenceScreen
        //기온
        tempPreference = UnitPreference(requireContext().applicationContext)
        tempPreference!!.key = getString(R.string.pref_key_unit_temp)
        tempPreference!!.setTitle(R.string.pref_title_unit_temp)
        tempPreference!!.unit = ValueUnits.valueOf(sharedPreferences!!.getString(getString(R.string.pref_key_unit_temp), "")!!)
        tempPreference!!.widgetLayoutResource = R.layout.custom_preference_layout
        tempPreference!!.isIconSpaceReserved = false
        tempPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(activity!!).setTitle(getString(R.string.pref_title_unit_temp))
                .setSingleChoiceItems(
                    getList(tempPreference!!), getCheckedItem(ValueType.temp, tempPreference!!.key)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            sharedPreferences!!.edit()
                                .putString(tempPreference!!.key, ValueUnits.celsius.name).commit()
                            tempPreference!!.unit = ValueUnits.celsius
                        }
                        1 -> {
                            sharedPreferences!!.edit()
                                .putString(tempPreference!!.key, ValueUnits.fahrenheit.name).commit()
                            tempPreference!!.unit = ValueUnits.fahrenheit
                        }
                    }
                    MyApplication.loadValueUnits(requireContext().applicationContext, true)
                    dialog.dismiss()
                }.create().show()
            true
        }
        preferenceScreen.addPreference(tempPreference!!)

        //바람
        windPreference = UnitPreference(requireContext().applicationContext)
        windPreference!!.key = getString(R.string.pref_key_unit_wind)
        windPreference!!.setTitle(R.string.pref_title_unit_wind)
        windPreference!!.widgetLayoutResource = R.layout.custom_preference_layout
        windPreference!!.unit = ValueUnits.valueOf(sharedPreferences!!.getString(getString(R.string.pref_key_unit_wind), "")!!)
        windPreference!!.isIconSpaceReserved = false
        windPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireActivity()).setTitle(getString(R.string.pref_title_unit_wind))
                .setSingleChoiceItems(
                    getList(windPreference!!), getCheckedItem(ValueType.wind, windPreference!!.key)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            sharedPreferences!!.edit()
                                .putString(windPreference!!.key, ValueUnits.mPerSec.name).commit()
                            windPreference!!.unit = ValueUnits.mPerSec
                        }
                        1 -> {
                            sharedPreferences!!.edit()
                                .putString(windPreference!!.key, ValueUnits.kmPerHour.name).commit()
                            windPreference!!.unit = ValueUnits.kmPerHour
                        }
                    }
                    MyApplication.loadValueUnits(requireContext().applicationContext, true)
                    dialog.dismiss()
                }.create().show()
            true
        }
        preferenceScreen.addPreference(windPreference!!)

        //시정거리
        visibilityPreference = UnitPreference(requireContext().applicationContext)
        visibilityPreference!!.key = getString(R.string.pref_key_unit_visibility)
        visibilityPreference!!.setTitle(R.string.pref_title_unit_visibility)
        visibilityPreference!!.widgetLayoutResource = R.layout.custom_preference_layout
        visibilityPreference!!.unit = ValueUnits.valueOf(sharedPreferences!!.getString(getString(R.string.pref_key_unit_visibility), "")!!)
        visibilityPreference!!.isIconSpaceReserved = false
        visibilityPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(activity!!).setTitle(getString(R.string.pref_title_unit_visibility))
                .setSingleChoiceItems(
                    getList(visibilityPreference!!), getCheckedItem(ValueType.visibility, visibilityPreference!!.key)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            sharedPreferences!!.edit()
                                .putString(visibilityPreference!!.key, ValueUnits.km.name).commit()
                            visibilityPreference!!.unit = ValueUnits.km
                        }
                        1 -> {
                            sharedPreferences!!.edit()
                                .putString(visibilityPreference!!.key, ValueUnits.mile.name).commit()
                            visibilityPreference!!.unit = ValueUnits.mile
                        }
                    }
                    MyApplication.loadValueUnits(requireContext().applicationContext, true)
                    dialog.dismiss()
                }.create().show()
            true
        }
        preferenceScreen.addPreference(visibilityPreference!!)

        //시간제
        clockPreference = UnitPreference(requireContext().applicationContext)
        clockPreference!!.key = getString(R.string.pref_key_unit_clock)
        clockPreference!!.setTitle(R.string.pref_title_unit_clock)
        clockPreference!!.widgetLayoutResource = R.layout.custom_preference_layout
        clockPreference!!.unit = ValueUnits.valueOf(sharedPreferences!!.getString(getString(R.string.pref_key_unit_clock), "")!!)
        clockPreference!!.isIconSpaceReserved = false
        clockPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialAlertDialogBuilder(activity!!).setTitle(clockPreference!!.title)
                .setSingleChoiceItems(
                    getList(clockPreference!!), getCheckedItem(ValueType.clock, clockPreference!!.key)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            sharedPreferences!!.edit()
                                .putString(clockPreference!!.key, ValueUnits.clock12.name).commit()
                            clockPreference!!.unit = ValueUnits.clock12
                        }
                        1 -> {
                            sharedPreferences!!.edit()
                                .putString(clockPreference!!.key, ValueUnits.clock24.name).commit()
                            clockPreference!!.unit = ValueUnits.clock24
                        }
                    }
                    MyApplication.loadValueUnits(requireContext().applicationContext, true)
                    dialog.dismiss()
                }.create().show()
            true
        }
        preferenceScreen.addPreference(clockPreference!!)
    }

    private fun getCheckedItem(valueType: ValueType, key: String): Int {
        val valueUnit = ValueUnits.valueOf(sharedPreferences!!.getString(key, "")!!)
        when (valueType) {
            ValueType.temp -> {
                when (valueUnit) {
                    ValueUnits.celsius -> return 0
                    ValueUnits.fahrenheit -> return 1
                }
                when (valueUnit) {
                    ValueUnits.mPerSec -> return 0
                    ValueUnits.kmPerHour -> return 1
                }
                when (valueUnit) {
                    ValueUnits.km -> return 0
                    ValueUnits.mile -> return 1
                }
                when (valueUnit) {
                    ValueUnits.clock12 -> return 0
                    ValueUnits.clock24 -> return 1
                }
            }
            ValueType.wind -> {
                when (valueUnit) {
                    ValueUnits.mPerSec -> return 0
                    ValueUnits.kmPerHour -> return 1
                }
                when (valueUnit) {
                    ValueUnits.km -> return 0
                    ValueUnits.mile -> return 1
                }
                when (valueUnit) {
                    ValueUnits.clock12 -> return 0
                    ValueUnits.clock24 -> return 1
                }
            }
            ValueType.visibility -> {
                when (valueUnit) {
                    ValueUnits.km -> return 0
                    ValueUnits.mile -> return 1
                }
                when (valueUnit) {
                    ValueUnits.clock12 -> return 0
                    ValueUnits.clock24 -> return 1
                }
            }
            ValueType.clock -> when (valueUnit) {
                ValueUnits.clock12 -> return 0
                ValueUnits.clock24 -> return 1
            }
        }
        return 0
    }

    private fun getList(preference: Preference): Array<CharSequence> {
        return if (preference === tempPreference) {
            arrayOf(
                getString(R.string.celsius),
                getString(R.string.fahrenheit)
            )
        } else if (preference === windPreference) {
            arrayOf(
                getString(R.string.mPerSec),
                getString(R.string.kmPerHour)
            )
        } else if (preference === visibilityPreference) {
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
    }
}