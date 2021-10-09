package com.lifedawn.bestweather.weathers.simplefragment.kma.currentconditions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.FragmentKmaCurrentConditionsBinding;

public class KmaCurrentConditionsFragment extends Fragment {
	private FragmentKmaCurrentConditionsBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentKmaCurrentConditionsBinding.inflate(inflater);
		return binding.getRoot();
	}
}