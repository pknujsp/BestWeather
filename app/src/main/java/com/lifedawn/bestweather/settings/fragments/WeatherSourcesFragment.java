package com.lifedawn.bestweather.settings.fragments;

import android.content.SharedPreferences;
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

public class WeatherSourcesFragment extends Fragment {
	private FragmentWeatherSourcesBinding binding;
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentWeatherSourcesBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.accuWeather.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_accu_weather), true));
		binding.openWeatherMap.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_open_weather_map), true));

		binding.accuWeather.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked && !binding.openWeatherMap.isChecked()) {
					Toast.makeText(getContext(), R.string.msg_at_least_one_must_be_selected, Toast.LENGTH_SHORT).show();
					binding.accuWeather.setChecked(true);
					return;
				}
				sharedPreferences.edit().putBoolean(getString(R.string.pref_key_accu_weather), isChecked).apply();
			}
		});

		binding.openWeatherMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked && !binding.accuWeather.isChecked()) {
					Toast.makeText(getContext(), R.string.msg_at_least_one_must_be_selected, Toast.LENGTH_SHORT).show();
					binding.openWeatherMap.setChecked(true);
					return;
				}
				sharedPreferences.edit().putBoolean(getString(R.string.pref_key_open_weather_map), isChecked).apply();
			}
		});
	}
}