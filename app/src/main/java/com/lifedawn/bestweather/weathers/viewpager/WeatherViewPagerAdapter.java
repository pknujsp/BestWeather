package com.lifedawn.bestweather.weathers.viewpager;

import android.location.Address;
import android.location.Geocoder;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WeatherViewPagerAdapter extends FragmentStateAdapter {
	public WeatherViewPagerAdapter(@NonNull @NotNull Fragment fragment) {
		super(fragment);
	}

	@NonNull
	@NotNull
	@Override
	public Fragment createFragment(int position) {
		return null;
	}

	@Override
	public int getItemCount() {
		return 0;
	}
}
