package com.lifedawn.bestweather.weathers.simplefragment.kma.dailyforecast;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentKmaDailyForecastBinding;


public class KmaDailyForecastFragment extends Fragment {
	private FragmentKmaDailyForecastBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentKmaDailyForecastBinding.inflate(inflater);
		return binding.getRoot();
	}
}