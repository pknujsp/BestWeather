package com.lifedawn.bestweather.weathers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.databinding.FragmentWeatherMainBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.FavoriteAddressRepository;
import com.lifedawn.bestweather.weathers.viewpager.WeatherViewPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WeatherMainFragment extends Fragment implements IGps {
	private FragmentWeatherMainBinding binding;
	private View.OnClickListener menuOnClickListener;
	private FavoriteAddressRepository favoriteAddressRepository;
	private WeatherViewPagerAdapter weatherViewPagerAdapter;
	private Gps gps;

	public WeatherMainFragment(View.OnClickListener menuOnClickListener) {
		this.menuOnClickListener = menuOnClickListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		favoriteAddressRepository = new FavoriteAddressRepository(getContext());
		gps = new Gps();
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
		binding.customProgressView.setContentView(binding.viewPager);
		binding.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener);
		binding.mainToolbar.gps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!gps.isProcessing()) {
					binding.customProgressView.onStartedProcessingData(getString(R.string.msg_finding_current_location));
					gps.runGps(requireActivity(), locationCallback, requestOnGpsLauncher, requestLocationPermissionLauncher);
				}
			}
		});
		binding.mainToolbar.find.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FindAddressFragment findAddressFragment = new FindAddressFragment();
				getParentFragmentManager().beginTransaction().hide(WeatherMainFragment.this)
						.add(R.id.fragment_container, findAddressFragment, getString(R.string.tag_find_address_fragment))
						.addToBackStack(getString(R.string.tag_find_address_fragment)).commit();
			}
		});

		binding.viewPager.registerOnPageChangeCallback(onPageChangeCallback);
		weatherViewPagerAdapter = new WeatherViewPagerAdapter(this);
		binding.viewPager.setAdapter(weatherViewPagerAdapter);

		favoriteAddressRepository.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
							final boolean usingCurrentLocation =
									sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), true);

							weatherViewPagerAdapter.setWeatherFragments(getContext(), usingCurrentLocation, result);
							binding.viewpagerIndicator.createDot(0, usingCurrentLocation ? result.size() + 1 : result.size());
						}
					});
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	private ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
		public int lastPosition = 0;

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			super.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}

		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			if (lastPosition != position) {
				lastPosition = position;
				binding.viewpagerIndicator.selectDot(position);
			}

			if (weatherViewPagerAdapter.isFragmentUsingCurrentLocation(position)) {
				binding.mainToolbar.gps.setVisibility(View.VISIBLE);
				binding.mainToolbar.find.setVisibility(View.GONE);
			} else {
				binding.mainToolbar.gps.setVisibility(View.GONE);
				binding.mainToolbar.find.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			super.onPageScrollStateChanged(state);
		}
	};
	private final ActivityResultCallback<ActivityResult> requestOnGpsResultCallback = new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(ActivityResult result) {
			binding.mainToolbar.gps.callOnClick();
		}
	};

	private final ActivityResultLauncher<Intent> requestOnGpsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
			requestOnGpsResultCallback);

	private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
			new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean isGranted) {
					onResultLocationPermission(isGranted);
				}
			});

	protected void onResultLocationPermission(boolean isGranted) {
		if (isGranted) {
			gps.clear();
			binding.mainToolbar.gps.callOnClick();
		} else {
			Toast.makeText(getActivity(), R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();
			locationCallback.onFailed();
		}
	}

	private final Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
		@Override
		public void onSuccessful(Location location) {
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
			editor.putString(getString(R.string.pref_key_last_current_location_latitude), String.valueOf(location.getLatitude()))
					.putString(getString(R.string.pref_key_last_current_location_longitude), String.valueOf(location.getLongitude())).apply();

			weatherViewPagerAdapter.getFragment(0).refreshForCurrentLocation(location);
			binding.customProgressView.onSuccessfulProcessingData();
		}

		@Override
		public void onFailed() {
			binding.customProgressView.onFailedProcessingData(getString(R.string.update_failed));
		}
	};

	@Override
	public void setUseGps() {
		binding.mainToolbar.gps.callOnClick();
	}
}