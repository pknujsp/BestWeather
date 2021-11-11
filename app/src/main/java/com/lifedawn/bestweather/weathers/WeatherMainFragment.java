package com.lifedawn.bestweather.weathers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.databinding.FragmentWeatherMainBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.main.MainActivity;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.flickr.FlickrGetPhotosFromGalleryParameter;
import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.FlickrUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherMainFragment extends Fragment implements WeatherViewModel.ILoadImgOfCurrentConditions, IGps {
	private static final Map<String, Drawable> backgroundImgMap = new HashMap<>();

	private FragmentWeatherMainBinding binding;
	private View.OnClickListener menuOnClickListener;
	private WeatherViewModel weatherViewModel;
	private Gps gps;
	private SharedPreferences sharedPreferences;
	private Gps.LocationCallback locationCallbackInMainFragment;
	private NetworkStatus networkStatus;

	public WeatherMainFragment(View.OnClickListener menuOnClickListener) {
		this.menuOnClickListener = menuOnClickListener;
	}

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
		                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		networkStatus = new NetworkStatus(getContext(), new ConnectivityManager.NetworkCallback() {
		});

		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		weatherViewModel.setiLoadImgOfCurrentConditions(this);
		locationCallbackInMainFragment = weatherViewModel.getLocationCallback();

		gps = new Gps(requestOnGpsLauncher, requestLocationPermissionLauncher, moveToAppDetailSettingsLauncher);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentWeatherMainBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		} else {
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
	}


	@Override
	public void onDestroy() {
		getParentFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.mainLayout.getLayoutParams();
		layoutParams.topMargin = MainActivity.getHeightOfStatusBar(getContext());
		binding.mainLayout.setLayoutParams(layoutParams);

		binding.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener);
		binding.mainToolbar.gps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (networkStatus.networkAvailable()) {
					gps.runGps(requireActivity(), locationCallback);
				}
			}
		});

		binding.mainToolbar.find.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (networkStatus.networkAvailable()) {
					FindAddressFragment findAddressFragment = new FindAddressFragment();
					getParentFragmentManager().setFragmentResult(getString(R.string.key_from_main_to_find_address), new Bundle());
					getParentFragmentManager().beginTransaction().hide(WeatherMainFragment.this).add(R.id.fragment_container,
							findAddressFragment, getString(R.string.tag_find_address_fragment)).addToBackStack(
							getString(R.string.tag_find_address_fragment)).commit();
				}
			}
		});

		binding.mainToolbar.refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (networkStatus.networkAvailable()) {
					WeatherFragment weatherFragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(
							getString(R.string.tag_weather_fragment));
					weatherFragment.requestNewData();
				}
			}
		});

	}

	public void setWeatherFragment(LocationType locationType, @Nullable FavoriteAddressDto favoriteAddressDto) {
		Glide.with(this).clear(binding.currentConditionsImg);

		binding.mainToolbar.gps.setVisibility(locationType == LocationType.CurrentLocation ? View.VISIBLE : View.GONE);
		binding.mainToolbar.find.setVisibility(locationType == LocationType.CurrentLocation ? View.GONE : View.VISIBLE);

		Bundle bundle = new Bundle();
		bundle.putSerializable(getString(R.string.bundle_key_selected_address_dto), favoriteAddressDto);
		bundle.putSerializable(getString(R.string.bundle_key_igps), (IGps) this);

		String requestKey = locationType == LocationType.CurrentLocation ? getString(R.string.key_current_location) : getString(
				R.string.key_selected_location);

		WeatherFragment weatherFragment = new WeatherFragment();
		getChildFragmentManager().clearFragmentResult(requestKey);
		getChildFragmentManager().clearFragmentResultListener(requestKey);
		getChildFragmentManager().setFragmentResult(requestKey, bundle);
		getChildFragmentManager().beginTransaction().setPrimaryNavigationFragment(weatherFragment).replace(
				binding.weatherFragmentsContainer.getId(), weatherFragment, getString(R.string.tag_weather_fragment)).commit();

	}

	@Override
	public void loadImgOfCurrentConditions(WeatherSourceType weatherSourceType, String val, Double latitude, Double longitude,
	                                       TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance(timeZone);
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

		//이미 다운로드 된 이미지가 있으면 다운로드 하지 않음
		if (backgroundImgMap.containsKey(galleryName)) {
			Glide.with(WeatherMainFragment.this).load(backgroundImgMap.get(galleryName)).transition(
					DrawableTransitionOptions.withCrossFade(500)).into(binding.currentConditionsImg);
		} else {

			FlickrGetPhotosFromGalleryParameter photosFromGalleryParameter = new FlickrGetPhotosFromGalleryParameter();
			photosFromGalleryParameter.setGalleryId(FlickrUtil.getWeatherGalleryId(galleryName));

			Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.FLICKR);
			Call<JsonElement> call = querys.getPhotosFromGallery(photosFromGalleryParameter.getMap());
			call.enqueue(new Callback<JsonElement>() {
				@Override
				public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
					if (getActivity() == null) {
						return;
					}

					Gson gson = new Gson();
					PhotosFromGalleryResponse photosFromGalleryResponse = gson.fromJson(response.body().toString(),
							PhotosFromGalleryResponse.class);

					if (photosFromGalleryResponse.getStat().equals("ok")) {
						if (!photosFromGalleryResponse.getPhotos().getTotal().equals("0")) {
							// https://live.staticflickr.com/65535/50081787401_355bcec912_b.jpg
							// https://live.staticflickr.com/server/id_secret_size.jpg
							int randomIdx = new Random().nextInt(Integer.parseInt(photosFromGalleryResponse.getPhotos().getTotal()));
							PhotosFromGalleryResponse.Photos.Photo photo = photosFromGalleryResponse.getPhotos().getPhoto().get(randomIdx);
							final String imgUrl = "https://live.staticflickr.com/" + photo.getServer() + "/" + photo.getId() + "_" + photo.getSecret() + "_b.jpg";

							//Glide.with(WeatherMainFragment.this).load(url).into(binding.currentConditionsImg);
							Target<Drawable> img = new CustomTarget<Drawable>() {
								@Override
								public void onResourceReady(@NonNull @NotNull Drawable resource,
								                            @Nullable @org.jetbrains.annotations.Nullable Transition<? super Drawable> transition) {
									if (getActivity() == null) {
										return;
									}
									backgroundImgMap.put(galleryName, resource);
									Glide.with(WeatherMainFragment.this).load(resource).transition(
											DrawableTransitionOptions.withCrossFade(500)).into(binding.currentConditionsImg);
								}

								@Override
								public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

								}
							};
							Glide.with(WeatherMainFragment.this).load(imgUrl).into(img);
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

	private final ActivityResultLauncher<Intent> requestOnGpsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					//gps 사용확인 화면에서 나온뒤 현재 위치 다시 파악
					LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
					boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
					if (isGpsEnabled) {
						binding.mainToolbar.gps.callOnClick();
					} else {
						locationCallback.onFailed(Gps.LocationCallback.Fail.DISABLED_GPS);
					}
				}
			});

	private final ActivityResultLauncher<Intent> moveToAppDetailSettingsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if (ContextCompat.checkSelfPermission(getContext(),
							Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
						sharedPreferences.edit().putBoolean(
								getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false).apply();
						binding.mainToolbar.gps.callOnClick();
					} else {
						locationCallback.onFailed(Gps.LocationCallback.Fail.REJECT_PERMISSION);
					}

				}
			});

	private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
			new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean isGranted) {
					//gps사용 권한
					//허가남 : 현재 위치 다시 파악
					//거부됨 : 작업 취소
					//계속 거부 체크됨 : 작업 취소
					if (isGranted) {
						sharedPreferences.edit().putBoolean(
								getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false).apply();
						binding.mainToolbar.gps.callOnClick();
					} else {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
							sharedPreferences.edit().putBoolean(
									getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), true).apply();
						}
						locationCallback.onFailed(Gps.LocationCallback.Fail.REJECT_PERMISSION);
					}
				}
			});

	private final Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
		@Override
		public void onSuccessful(Location location) {
			//현재 위치 파악 성공
			//현재 위/경도 좌표를 최근 현재위치의 위/경도로 등록
			//날씨 데이터 요청
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(getString(R.string.pref_key_last_current_location_latitude), String.valueOf(location.getLatitude())).putString(
					getString(R.string.pref_key_last_current_location_longitude), String.valueOf(location.getLongitude())).apply();

			WeatherFragment weatherFragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(
					getString(R.string.tag_weather_fragment));
			weatherFragment.onChangedCurrentLocation(location);
			locationCallbackInMainFragment.onSuccessful(location);
		}

		@Override
		public void onFailed(Fail fail) {
			locationCallbackInMainFragment.onFailed(fail);

			if (fail == Fail.DISABLED_GPS) {
				Toast.makeText(getContext(), R.string.request_to_make_gps_on, Toast.LENGTH_SHORT).show();
			} else if (fail == Fail.REJECT_PERMISSION) {
				Toast.makeText(getContext(), R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void requestCurrentLocation() {
		binding.mainToolbar.gps.callOnClick();
	}

	public void reDraw() {
		//날씨 프래그먼트 다시 그림
		String tag = getString(R.string.tag_weather_fragment);
		WeatherFragment fragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(tag);

		if (fragment != null) {
			fragment.reDraw();
		}
	}
}