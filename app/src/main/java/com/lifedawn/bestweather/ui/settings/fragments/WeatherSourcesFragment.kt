package com.lifedawn.bestweather.ui.settings.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.views.BaseFragment
import com.lifedawn.bestweather.databinding.FragmentWeatherSourcesBinding

class WeatherSourcesFragment : BaseFragment<FragmentWeatherSourcesBinding>(R.layout.fragment_weather_sources) {
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.accuWeather.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_accu_weather), true));
        binding.openWeatherMap.setChecked(sharedPreferences!!.getBoolean(getString(R.string.pref_key_open_weather_map), false))
        binding.yr.setChecked(sharedPreferences!!.getBoolean(getString(R.string.pref_key_met), false))
        binding.kmaTopPriority.setChecked(sharedPreferences!!.getBoolean(getString(R.string.pref_key_kma_top_priority), false))

        /*
		Locale locale;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = getResources().getConfiguration().getLocales().get(0);
		} else {
			locale = getResources().getConfiguration().locale;
		}
		String country = locale.getCountry();

		if (country.equals("KR")) {
			binding.kmaPriorityLayout.setVisibility(View.VISIBLE);
		} else {
			binding.kmaPriorityLayout.setVisibility(View.GONE);
		}
		 */binding.accuWeather.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> })
        binding.openWeatherMap.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                binding.yr.setChecked(true)
            }
            if (isChecked && binding.yr.isChecked()) {
                binding.yr.setChecked(false)
            }
            sharedPreferences!!.edit().putBoolean(getString(R.string.pref_key_open_weather_map), isChecked).commit()
        })
        binding.yr.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                binding.openWeatherMap.setChecked(true)
            }
            if (isChecked && binding.openWeatherMap.isChecked()) {
                binding.openWeatherMap.setChecked(false)
            }
            sharedPreferences!!.edit().putBoolean(getString(R.string.pref_key_met), isChecked).commit()
        })
        binding.kmaTopPriority.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences!!.edit().putBoolean(
                getString(
                    R.string.pref_key_kma_top_priority
                ), isChecked
            ).commit()
        })
        binding.accuWeatherLayout.setOnClickListener(View.OnClickListener { binding.accuWeather.setChecked(!binding.accuWeather.isChecked()) })
        binding.owmLayout.setOnClickListener(View.OnClickListener { binding.openWeatherMap.setChecked(!binding.openWeatherMap.isChecked()) })
        binding.yrLayout.setOnClickListener(View.OnClickListener { binding.yr.setChecked(!binding.yr.isChecked()) })
        binding.kmaPriorityLayout.setOnClickListener(View.OnClickListener { binding.kmaTopPriority.setChecked(!binding.kmaTopPriority.isChecked()) })
    }

}