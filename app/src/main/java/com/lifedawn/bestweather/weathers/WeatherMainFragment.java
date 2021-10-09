package com.lifedawn.bestweather.weathers;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.databinding.FragmentWeatherMainBinding;

import org.jetbrains.annotations.NotNull;

public class WeatherMainFragment extends Fragment {
	private FragmentWeatherMainBinding binding;
	private View.OnClickListener menuOnClickListener;

	public WeatherMainFragment(View.OnClickListener menuOnClickListener) {
		this.menuOnClickListener = menuOnClickListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentWeatherMainBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener);
	}
}