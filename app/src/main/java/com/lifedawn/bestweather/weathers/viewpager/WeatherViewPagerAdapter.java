package com.lifedawn.bestweather.weathers.viewpager;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.FavoriteAddressType;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WeatherViewPagerAdapter extends FragmentStateAdapter {
	private List<WeatherFragment> weatherFragments = new ArrayList<>();
	private IGps iGps;

	public WeatherViewPagerAdapter(@NonNull @NotNull Fragment fragment) {
		super(fragment);
		this.iGps = (IGps) fragment;
	}

	@NonNull
	@NotNull
	@Override
	public Fragment createFragment(int position) {
		return weatherFragments.get(position);
	}

	@Override
	public int getItemCount() {
		return weatherFragments.size();
	}

	public void setWeatherFragments(Context context, boolean usingCurrentLocation, List<FavoriteAddressDto> addressList) {
		if (usingCurrentLocation) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(context.getString(R.string.bundle_key_favorite_address_type), FavoriteAddressType.CurrentLocation);
			bundle.putSerializable(context.getString(R.string.bundle_key_favorite_address_type), FavoriteAddressType.CurrentLocation);

			WeatherFragment weatherFragment = new WeatherFragment();
			weatherFragment.setArguments(bundle);
			weatherFragment.setiGps(iGps);

			weatherFragments.add(weatherFragment);
		}

		for (FavoriteAddressDto address : addressList) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(context.getString(R.string.bundle_key_favorite_address_type), FavoriteAddressType.SelectedAddress);
			bundle.putSerializable(context.getString(R.string.bundle_key_selected_address), address);

			WeatherFragment weatherFragment = new WeatherFragment();
			weatherFragment.setArguments(bundle);

			weatherFragments.add(weatherFragment);
		}
	}

	public WeatherFragment getFragment(int position) {
		return weatherFragments.get(position);
	}

	public boolean isFragmentUsingCurrentLocation(int position) {
		return weatherFragments.get(position).isFragmentUsingCurrentLocation();
	}
}
