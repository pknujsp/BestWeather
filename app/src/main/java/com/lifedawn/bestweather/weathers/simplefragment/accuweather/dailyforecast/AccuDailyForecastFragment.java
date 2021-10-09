package com.lifedawn.bestweather.weathers.simplefragment.accuweather.dailyforecast;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentAccuDailyForecastBinding;

public class AccuDailyForecastFragment extends Fragment {
	private FragmentAccuDailyForecastBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAccuDailyForecastBinding.inflate(inflater);
		return binding.getRoot();
	}
}