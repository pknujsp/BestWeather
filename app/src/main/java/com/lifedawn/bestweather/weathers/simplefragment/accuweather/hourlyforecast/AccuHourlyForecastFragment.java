package com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentAccuHourlyForecastBinding;


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
}