package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.dailyforecast;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentOwmDailyForecastBinding;

public class OwmDailyForecastFragment extends Fragment {
	private FragmentOwmDailyForecastBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentOwmDailyForecastBinding.inflate(inflater);
		return binding.getRoot();
	}
}