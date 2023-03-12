package com.lifedawn.bestweather.ui.settings.weatherprovider.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.settings.main.viewmodel.MainAppSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeatherProvidersFragment : PreferenceFragmentCompat() {
    private val viewModel by activityViewModels<MainAppSettingsViewModel>()

    private lateinit var kmaTopPriorityReference: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.weather_provider_settings, rootKey)
        kmaTopPriorityReference = findPreference(getString(R.string.pref_key_kma_top_priority))!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kmaTopPriorityReference.isChecked = viewModel.kmaTopPriorityFlow.value
        kmaTopPriorityReference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            lifecycleScope.launch {
                viewModel.saveKmaTopPriority(newValue as Boolean)
            }
            true
        }
    }
}