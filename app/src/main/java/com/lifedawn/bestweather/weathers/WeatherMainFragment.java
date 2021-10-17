package com.lifedawn.bestweather.weathers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.databinding.FragmentWeatherMainBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.flickr.FlickrGetPhotosFromGalleryParameter;
import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.FavoriteAddressRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;
import com.lifedawn.bestweather.weathers.viewpager.WeatherViewPagerAdapter;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherMainFragment extends Fragment implements IGps, WeatherViewModel.ILoadImgOfCurrentConditions {
	private FragmentWeatherMainBinding binding;
	private View.OnClickListener menuOnClickListener;
	private FavoriteAddressRepository favoriteAddressRepository;
	private WeatherViewPagerAdapter weatherViewPagerAdapter;
	private Gps gps;
	private WeatherViewModel weatherViewModel;

	public WeatherMainFragment(View.OnClickListener menuOnClickListener) {
		this.menuOnClickListener = menuOnClickListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		favoriteAddressRepository = new FavoriteAddressRepository(getContext());
		gps = new Gps();

		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		weatherViewModel.setiLoadImgOfCurrentConditions(this);
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

		binding.mainToolbar.refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

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

	@Override
	public void loadImgOfCurrentConditions(MainProcessing.WeatherSourceType weatherSourceType, String val, Double latitude, Double longitude) {
		FlickrGetPhotosFromGalleryParameter photosFromGalleryParameter = new FlickrGetPhotosFromGalleryParameter();
		photosFromGalleryParameter.setGalleryId("72157719980390655");

		Calendar calendar = Calendar.getInstance();
		SunriseSunsetCalculator sunriseSunsetCalculator =
				new SunriseSunsetCalculator(new com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude), calendar.getTimeZone());
		Calendar sunRiseCalendar = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar);
		Calendar sunSetCalendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar);

		final long currentTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(calendar.getTimeInMillis());
		final long sunRiseTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(sunRiseCalendar.getTimeInMillis());
		final long sunSetTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(sunSetCalendar.getTimeInMillis());

		String time = null;

		//현재 시각 파악 : 낮, 밤, 일출, 일몰(+-20분)
		if (currentTimeMinutes < sunRiseTimeMinutes - 2) {
			//새벽
			time = "night";
		} else if (currentTimeMinutes <= sunRiseTimeMinutes + 20) {
			//일출
			time = "sunrise";
		} else if (currentTimeMinutes > sunRiseTimeMinutes + 20 && currentTimeMinutes <= sunSetTimeMinutes - 20) {
			//낮
			time = "day";
		} else if (currentTimeMinutes < sunSetTimeMinutes + 2) {
			//일몰
			time = "sunset";
		} else {
			//밤
			time = "night";
		}

		String weather = null;


		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR);
		Call<JsonElement> call = querys.getPhotosFromGallery(photosFromGalleryParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				Gson gson = new Gson();
				PhotosFromGalleryResponse photosFromGalleryResponse = gson.fromJson(response.body().toString(),
						PhotosFromGalleryResponse.class);

				if (!photosFromGalleryResponse.getPhotos().getTotal().equals("0")) {
					// https://live.staticflickr.com/65535/50081787401_355bcec912_b.jpg
					// https://live.staticflickr.com/server/id_secret_size.jpg
					final String url = "https://live.staticflickr.com/" + photosFromGalleryResponse.getPhotos().getPhoto().get(0).getServer()
							+ "/" + photosFromGalleryResponse.getPhotos().getPhoto().get(0).getId() + "_" +
							photosFromGalleryResponse.getPhotos().getPhoto().get(0).getSecret() + "_b.jpg";
					Glide.with(WeatherMainFragment.this).load(url).into(binding.currentConditionsImg);
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {

			}
		});
	}
}