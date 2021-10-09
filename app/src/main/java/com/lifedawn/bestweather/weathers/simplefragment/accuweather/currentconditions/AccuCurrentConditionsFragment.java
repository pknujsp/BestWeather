package com.lifedawn.bestweather.weathers.simplefragment.accuweather.currentconditions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentAccuCurrentConditionsBinding;


public class AccuCurrentConditionsFragment extends Fragment {
	private FragmentAccuCurrentConditionsBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAccuCurrentConditionsBinding.inflate(inflater);
		return binding.getRoot();
	}
}