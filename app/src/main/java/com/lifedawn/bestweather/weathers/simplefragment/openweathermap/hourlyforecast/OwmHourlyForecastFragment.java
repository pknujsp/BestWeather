package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentOwmHourlyForecastBinding;

public class OwmHourlyForecastFragment extends Fragment {
	private FragmentOwmHourlyForecastBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentOwmHourlyForecastBinding.inflate(inflater);
		return binding.getRoot();
	}
}