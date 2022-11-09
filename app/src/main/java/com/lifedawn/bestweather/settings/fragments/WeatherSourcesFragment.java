package com.lifedawn.bestweather.settings.fragments;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentWeatherSourcesBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class WeatherSourcesFragment extends Fragment {
	private FragmentWeatherSourcesBinding binding;
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentWeatherSourcesBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		//binding.accuWeather.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_accu_weather), true));
		binding.openWeatherMap.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_open_weather_map), false));
		binding.yr.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_met), false));
		binding.kmaTopPriority.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_kma_top_priority), false));

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
		 */

		binding.accuWeather.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

			}
		});

		binding.openWeatherMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					binding.yr.setChecked(true);
				}
				if (isChecked && binding.yr.isChecked()) {
					binding.yr.setChecked(false);
				}
				sharedPreferences.edit().putBoolean(getString(R.string.pref_key_open_weather_map), isChecked).commit();
			}
		});

		binding.yr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					binding.openWeatherMap.setChecked(true);
				}
				if (isChecked && binding.openWeatherMap.isChecked()) {
					binding.openWeatherMap.setChecked(false);
				}
				sharedPreferences.edit().putBoolean(getString(R.string.pref_key_met), isChecked).commit();
			}
		});

		binding.kmaTopPriority.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sharedPreferences.edit().putBoolean(getString(R.string.pref_key_kma_top_priority), isChecked).commit();
			}
		});


		binding.accuWeatherLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				binding.accuWeather.setChecked(!binding.accuWeather.isChecked());
			}
		});

		binding.owmLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				binding.openWeatherMap.setChecked(!binding.openWeatherMap.isChecked());
			}
		});

		binding.yrLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				binding.yr.setChecked(!binding.yr.isChecked());
			}
		});

		binding.kmaPriorityLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				binding.kmaTopPriority.setChecked(!binding.kmaTopPriority.isChecked());
			}
		});

	}

}