package com.lifedawn.bestweather.weathers.simplefragment.kma.hourlyforecast;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentKmaHourlyForecastBinding;


public class KmaHourlyForecastFragment extends Fragment {
	private FragmentKmaHourlyForecastBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentKmaHourlyForecastBinding.inflate(inflater);
		return binding.getRoot();
	}
}