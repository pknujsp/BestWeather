package com.lifedawn.bestweather.weathers.simplefragment.aqicn;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentAirQualitySimpleBinding;


public class AirQualitySimpleFragment extends Fragment {
	private FragmentAirQualitySimpleBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAirQualitySimpleBinding.inflate(inflater);
		return binding.getRoot();
	}
}