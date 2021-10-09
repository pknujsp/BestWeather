package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.currentconditions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentOwmCurrentConditionsBinding;

public class OwmCurrentConditionsFragment extends Fragment {
	private FragmentOwmCurrentConditionsBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentOwmCurrentConditionsBinding.inflate(inflater);
		return binding.getRoot();
	}
}