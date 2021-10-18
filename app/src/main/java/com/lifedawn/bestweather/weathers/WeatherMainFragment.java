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
import com.lifedawn.bestweather.weathers.dataprocessing.request.KmaProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.FlickrUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;
import com.lifedawn.bestweather.weathers.viewpager.WeatherViewPagerAdapter;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherMainFragment extends Fragment implements WeatherViewModel.ILoadImgOfCurrentConditions {
	private FragmentWeatherMainBinding binding;
	private View.OnClickListener menuOnClickListener;
	private FavoriteAddressRepository favoriteAddressRepository;
	private WeatherViewPagerAdapter weatherViewPagerAdapter;
	private WeatherViewModel weatherViewModel;
	private IGps iGps;
	
	public WeatherMainFragment(View.OnClickListener menuOnClickListener) {
		this.menuOnClickListener = menuOnClickListener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		favoriteAddressRepository = new FavoriteAddressRepository(getContext());
		
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		weatherViewModel.setiLoadImgOfCurrentConditions(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentWeatherMainBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener);
		binding.mainToolbar.gps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				iGps.requestCurrentLocation();
			}
		});
		
		binding.mainToolbar.find.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FindAddressFragment findAddressFragment = new FindAddressFragment();
				getParentFragmentManager().beginTransaction().hide(WeatherMainFragment.this).add(R.id.fragment_container,
						findAddressFragment, getString(R.string.tag_find_address_fragment)).addToBackStack(
						getString(R.string.tag_find_address_fragment)).commit();
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
							final boolean usingCurrentLocation = sharedPreferences.getBoolean(
									getString(R.string.pref_key_use_current_location), true);
							
							weatherViewPagerAdapter.setWeatherFragments(getContext(), usingCurrentLocation, result);
							WeatherMainFragment.this.iGps = weatherViewPagerAdapter.getiGps();
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
	
	
	@Override
	public void loadImgOfCurrentConditions(MainProcessing.WeatherSourceType weatherSourceType, String val, Double latitude,
			Double longitude) {
		
		
		Calendar calendar = Calendar.getInstance();
		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(
				new com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude), calendar.getTimeZone());
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
		switch (weatherSourceType) {
			case KMA:
				String code = val.substring(0, 1);
				weather = val.contains("_sky") ? KmaResponseProcessor.getSkyFlickrGalleryName(
						code) : KmaResponseProcessor.getPtyFlickrGalleryName(code);
				break;
			case ACCU_WEATHER:
				weather = AccuWeatherResponseProcessor.getFlickrGalleryName(val);
				break;
			case OPEN_WEATHER_MAP:
				weather = OpenWeatherMapResponseProcessor.getFlickrGalleryName(val);
				break;
		}
		
		final String galleryName = time + " " + weather;
		// time : sunrise, sunset, day, night
		// weather : clear, partly cloudy, mostly cloudy, overcast, rain, snow
		
		FlickrGetPhotosFromGalleryParameter photosFromGalleryParameter = new FlickrGetPhotosFromGalleryParameter();
		photosFromGalleryParameter.setGalleryId(FlickrUtil.getWeatherGalleryId(galleryName));
		
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR);
		Call<JsonElement> call = querys.getPhotosFromGallery(photosFromGalleryParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				Gson gson = new Gson();
				PhotosFromGalleryResponse photosFromGalleryResponse = gson.fromJson(response.body().toString(),
						PhotosFromGalleryResponse.class);
				
				if (photosFromGalleryResponse.getStat().equals("ok")) {
					if (!photosFromGalleryResponse.getPhotos().getTotal().equals("0")) {
						// https://live.staticflickr.com/65535/50081787401_355bcec912_b.jpg
						// https://live.staticflickr.com/server/id_secret_size.jpg
						int randomIdx = new Random().nextInt(Integer.parseInt(photosFromGalleryResponse.getPhotos().getTotal()));
						PhotosFromGalleryResponse.Photos.Photo photo = photosFromGalleryResponse.getPhotos().getPhoto().get(randomIdx);
						final String url = "https://live.staticflickr.com/" + photo.getServer() + "/" + photo.getId() + "_" + photo.getSecret() + "_b.jpg";
						Glide.with(WeatherMainFragment.this).load(url).into(binding.currentConditionsImg);
					} else {
					
					}
				} else {
				
				}
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
			
			}
		});
	}
}