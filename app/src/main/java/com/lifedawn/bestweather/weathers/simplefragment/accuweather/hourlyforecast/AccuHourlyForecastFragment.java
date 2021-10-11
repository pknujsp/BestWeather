package com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentAccuHourlyForecastBinding;

import org.jetbrains.annotations.NotNull;


public class AccuHourlyForecastFragment extends Fragment {
	private FragmentAccuHourlyForecastBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAccuHourlyForecastBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.baseWeatherCardView.forecastName.setText(R.string.hourly_forecast);
	}
}