package com.lifedawn.bestweather.weathers;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.flickr.FlickrImgObj;
import com.lifedawn.bestweather.main.IRefreshFavoriteLocationListOnSideNav;
import com.lifedawn.bestweather.main.MainActivity;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.flickr.FlickrGetPhotosFromGalleryParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.flickr.PhotosFromGalleryResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.FlickrUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.detailfragment.accuweather.currentconditions.AccuDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.detailfragment.kma.currentconditions.KmaDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.currentconditions.OwmDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.accuweather.currentconditions.AccuSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.accuweather.dailyforecast.AccuSimpleDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast.AccuSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.SimpleAirQualityFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.kma.currentconditions.KmaSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.kma.dailyforecast.KmaSimpleDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.kma.hourlyforecast.KmaSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.openweathermap.currentconditions.OwmSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.openweathermap.dailyforecast.OwmSimpleDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast.OwmSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.sunsetrise.SunsetriseFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WeatherFragment extends Fragment implements WeatherViewModel.ILoadImgOfCurrentConditions, IGps {
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
	private static final Map<String, WeatherResponseObj> FINAL_RESPONSE_MAP = new HashMap<>();
	private static final Map<String, FlickrImgObj> BACKGROUND_IMG_MAP = new HashMap<>();

	private FragmentWeatherBinding binding;
	private FavoriteAddressDto selectedFavoriteAddressDto;
	private LocationType locationType;
	private WeatherViewModel weatherViewModel;
	private OnResultFragmentListener onResultFragmentListener;
	private View.OnClickListener menuOnClickListener;
	private Gps gps;
	private NetworkStatus networkStatus;
	private Gps.LocationCallback locationCallbackInMainFragment;

	private WeatherSourceType mainWeatherSourceType;
	private Double latitude;
	private Double longitude;
	private String countryCode;
	private String addressName;
	private SharedPreferences sharedPreferences;

	private MultipleRestApiDownloader multipleRestApiDownloader;
	private IRefreshFavoriteLocationListOnSideNav iRefreshFavoriteLocationListOnSideNav;


	public WeatherFragment setMenuOnClickListener(View.OnClickListener menuOnClickListener) {
		this.menuOnClickListener = menuOnClickListener;
		return this;
	}

	public WeatherFragment setiRefreshFavoriteLocationListOnSideNav(IRefreshFavoriteLocationListOnSideNav iRefreshFavoriteLocationListOnSideNav) {
		this.iRefreshFavoriteLocationListOnSideNav = iRefreshFavoriteLocationListOnSideNav;
		return this;
	}

	public WeatherFragment setOnResultFragmentListener(OnResultFragmentListener onResultFragmentListener) {
		this.onResultFragmentListener = onResultFragmentListener;
		return this;
	}

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
		                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		networkStatus = NetworkStatus.getInstance(getContext());
		gps = new Gps(getContext(), requestOnGpsLauncher, requestLocationPermissionLauncher, moveToAppDetailSettingsLauncher);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		weatherViewModel.setiLoadImgOfCurrentConditions(this);
		locationCallbackInMainFragment = weatherViewModel.getLocationCallback();
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentWeatherBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.mainLayout.getLayoutParams();
		layoutParams.topMargin = MainActivity.getHeightOfStatusBar(getContext());
		binding.mainLayout.setLayoutParams(layoutParams);

		MobileAds.initialize(getContext());
		AdRequest adRequest = new AdRequest.Builder().build();

		binding.adViewBelowAirQuality.loadAd(adRequest);
		binding.adViewBottom.loadAd(adRequest);

		binding.adViewBelowAirQuality.setVisibility(View.GONE);
		binding.adViewBottom.setVisibility(View.GONE);

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
					Bundle bundle = new Bundle();
					bundle.putString(BundleKey.RequestFragment.name(), WeatherFragment.class.getName());
					findAddressFragment.setArguments(bundle);

					findAddressFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
						@Override
						public void onResultFragment(Bundle result) {
							final boolean isSelectedNewAddress = result.getBoolean(BundleKey.SelectedAddressDto.name());

							if (isSelectedNewAddress) {
								final int newFavoriteAddressDtoId = result.getInt(BundleKey.newFavoriteAddressDtoId.name());
								sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
										newFavoriteAddressDtoId).apply();
								iRefreshFavoriteLocationListOnSideNav.refreshFavoriteLocationsList(result.getString(BundleKey.LastFragment.name()), result);
							}
						}
					});

					getParentFragmentManager().beginTransaction().hide(WeatherFragment.this).add(R.id.fragment_container,
							findAddressFragment, getString(R.string.tag_find_address_fragment)).addToBackStack(
							getString(R.string.tag_find_address_fragment)).commit();
				}
			}
		});

		binding.mainToolbar.refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (networkStatus.networkAvailable()) {
					requestNewData();
				}
			}
		});

	}

	public void load(LocationType locationType, @Nullable FavoriteAddressDto favoriteAddressDto) {
		Glide.with(this).clear(binding.currentConditionsImg);

		binding.mainToolbar.gps.setVisibility(locationType == LocationType.CurrentLocation ? View.VISIBLE : View.GONE);
		binding.mainToolbar.find.setVisibility(locationType == LocationType.CurrentLocation ? View.GONE : View.VISIBLE);

		Bundle bundle = new Bundle();
		bundle.putSerializable(BundleKey.SelectedAddressDto.name(), favoriteAddressDto);
		bundle.putSerializable(BundleKey.IGps.name(), (IGps) this);
		bundle.putString(BundleKey.LocationType.name(), locationType.name());
		bundle.putString(BundleKey.RequestFragment.name(), WeatherFragment.class.getName());

		locationType = LocationType.valueOf(bundle.getString(BundleKey.LocationType.name()));
		this.locationType = locationType;

		if (locationType == LocationType.CurrentLocation) {
			sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1).putString(
					getString(R.string.pref_key_last_selected_location_type), locationType.name()).apply();

			latitude = Double.parseDouble(
					sharedPreferences.getString(getString(R.string.pref_key_last_current_location_latitude), "0.0"));
			longitude = Double.parseDouble(
					sharedPreferences.getString(getString(R.string.pref_key_last_current_location_longitude), "0.0"));

			if (latitude == 0.0 && longitude == 0.0) {
				//최근에 현재위치로 잡힌 위치가 없으므로 현재 위치 요청
				requestCurrentLocation();
			} else {
				//위/경도에 해당하는 지역명을 불러오고, 날씨 데이터 다운로드
				//이미 존재하는 날씨 데이터면 다운로드X
				requestAddressOfLocation(latitude, longitude, !containWeatherData(latitude, longitude));
			}
		} else {
			selectedFavoriteAddressDto = (FavoriteAddressDto) bundle.getSerializable(
					BundleKey.SelectedAddressDto.name());

			sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
					selectedFavoriteAddressDto.getId()).putString(getString(R.string.pref_key_last_selected_location_type),
					locationType.name()).apply();

			mainWeatherSourceType = getMainWeatherSourceType(selectedFavoriteAddressDto.getCountryCode());
			countryCode = selectedFavoriteAddressDto.getCountryCode();
			addressName = selectedFavoriteAddressDto.getAddress();
			latitude = Double.parseDouble(selectedFavoriteAddressDto.getLatitude());
			longitude = Double.parseDouble(selectedFavoriteAddressDto.getLongitude());

			binding.addressName.setText(addressName);

			if (containWeatherData(latitude, longitude)) {
				//기존 데이터 표시
				mainWeatherSourceType = FINAL_RESPONSE_MAP.get(latitude.toString() + longitude.toString()).requestMainWeatherSourceType;
				reDraw();
			} else {
				requestNewData();
			}
		}
	}

	@Override
	public void onDestroy() {
		if (multipleRestApiDownloader != null) {
			multipleRestApiDownloader.cancel();
		}
		getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}


	@Override
	public void loadImgOfCurrentConditions(WeatherSourceType weatherSourceType, String val, Double latitude, Double longitude,
	                                       ZoneId zoneId) {
		EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				ZonedDateTime lastRefreshDateTime =
						ZonedDateTime.parse(FINAL_RESPONSE_MAP.get(latitude.toString() + longitude.toString()).multipleRestApiDownloader.getRequestDateTime().toString());
				lastRefreshDateTime = lastRefreshDateTime.withZoneSameInstant(zoneId);

				SimpleTimeZone timeZone = new SimpleTimeZone(lastRefreshDateTime.getOffset().getTotalSeconds() * 1000, "");
				Calendar currentCalendar = Calendar.getInstance(timeZone);
				currentCalendar.set(lastRefreshDateTime.getYear(), lastRefreshDateTime.getMonthValue() - 1,
						lastRefreshDateTime.getDayOfMonth(), lastRefreshDateTime.getHour(), lastRefreshDateTime.getMinute(),
						lastRefreshDateTime.getSecond());

				SunriseSunsetCalculator sunRiseSunsetCalculator = new SunriseSunsetCalculator(
						new com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude), currentCalendar.getTimeZone());
				Calendar sunRiseCalendar = sunRiseSunsetCalculator.getOfficialSunriseCalendarForDate(currentCalendar);
				Calendar sunSetCalendar = sunRiseSunsetCalculator.getOfficialSunsetCalendarForDate(currentCalendar);

				if (sunRiseCalendar == null || sunSetCalendar == null) {
					return;
				}

				final long currentTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(currentCalendar.getTimeInMillis());
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
				if (BACKGROUND_IMG_MAP.containsKey(galleryName)) {
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (getChildFragmentManager().findFragmentByTag(getString(R.string.tag_simple_current_conditions_fragment)) != null) {
									Glide.with(WeatherFragment.this).load(BACKGROUND_IMG_MAP.get(galleryName).getImg()).transition(
											DrawableTransitionOptions.withCrossFade(500)).into(binding.currentConditionsImg);
									BaseSimpleCurrentConditionsFragment currentConditionsFragment =
											(BaseSimpleCurrentConditionsFragment) getChildFragmentManager().findFragmentByTag(getString(R.string.tag_simple_current_conditions_fragment));
									currentConditionsFragment.setFlickrImgInfo(BACKGROUND_IMG_MAP.get(galleryName));
								}
							}
						});
					}


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
									final String backgroundImgUrl = "https://live.staticflickr.com/" + photo.getServer() + "/" + photo.getId() + "_" + photo.getSecret() + "_b.jpg";

									final FlickrImgObj flickrImgObj = new FlickrImgObj();
									flickrImgObj.setPhoto(photo);
									BACKGROUND_IMG_MAP.put(galleryName, flickrImgObj);

									//Glide.with(WeatherMainFragment.this).load(url).into(binding.currentConditionsImg);
									Target<Drawable> img = new CustomTarget<Drawable>() {
										@Override
										public void onResourceReady(@NonNull @NotNull Drawable resource,
										                            @Nullable @org.jetbrains.annotations.Nullable Transition<? super Drawable> transition) {
											if (getActivity() == null) {
												return;
											} else {
												BACKGROUND_IMG_MAP.get(galleryName).setImg(resource);
												Glide.with(WeatherFragment.this).load(resource).transition(
														DrawableTransitionOptions.withCrossFade(500)).into(binding.currentConditionsImg);

												getActivity().runOnUiThread(new Runnable() {
													@Override
													public void run() {
														if (getChildFragmentManager().findFragmentByTag(getString(R.string.tag_simple_current_conditions_fragment)) != null) {
															BaseSimpleCurrentConditionsFragment currentConditionsFragment =
																	(BaseSimpleCurrentConditionsFragment) getChildFragmentManager().findFragmentByTag(getString(R.string.tag_simple_current_conditions_fragment));
															currentConditionsFragment.setFlickrImgInfo(BACKGROUND_IMG_MAP.get(galleryName));
														}
													}
												});
											}

										}

										@Override
										public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

										}
									};
									Glide.with(WeatherFragment.this).load(backgroundImgUrl).into(img);
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
		});

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
					getString(R.string.pref_key_last_current_location_longitude), String.valueOf(location.getLongitude())).commit();

			onChangedCurrentLocation(location);
			locationCallbackInMainFragment.onSuccessful(location);
		}

		@Override
		public void onFailed(Fail fail) {
			locationCallbackInMainFragment.onFailed(fail);
		}
	};

	@Override
	public void requestCurrentLocation() {
		binding.mainToolbar.gps.callOnClick();
	}


	private boolean containWeatherData(Double latitude, Double longitude) {
		return FINAL_RESPONSE_MAP.containsKey(latitude.toString() + longitude.toString());
	}


	private void requestAddressOfLocation(Double latitude, Double longitude, boolean refresh) {
		Geocoding.geocoding(getContext(), latitude, longitude, new Geocoding.GeocodingCallback() {
			@Override
			public void onGeocodingResult(List<Address> addressList) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (addressList.isEmpty()) {
								//검색 결과가 없으면 주소 정보 미 표시하고 데이터 로드
								mainWeatherSourceType = getMainWeatherSourceType("");
								countryCode = "";
								addressName = getString(R.string.unknown_address);

								String addressStr = getString(R.string.current_location) + " : " + addressName;
								binding.addressName.setText(addressStr);
								weatherViewModel.setCurrentLocationAddressName(addressName);
							} else {
								Address address = addressList.get(0);
								addressName = address.getAddressLine(0);
								mainWeatherSourceType = getMainWeatherSourceType(address.getCountryCode());
								countryCode = address.getCountryCode();

								String addressStr = getString(R.string.current_location) + " : " + addressName;
								binding.addressName.setText(addressStr);
								weatherViewModel.setCurrentLocationAddressName(addressName);
							}


							if (refresh) {
								requestNewData();
							} else {
								//이미 데이터가 있으면 다시 그림
								mainWeatherSourceType = FINAL_RESPONSE_MAP.get(
										latitude.toString() + longitude.toString()).requestMainWeatherSourceType;
								reDraw();
							}
						}
					});

				}

			}
		});
	}

	private WeatherSourceType getMainWeatherSourceType(@NonNull String countryCode) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		WeatherSourceType mainWeatherSourceType = null;

		if (sharedPreferences.getBoolean(getString(R.string.pref_key_accu_weather), true)) {
			mainWeatherSourceType = WeatherSourceType.ACCU_WEATHER;
		} else {
			mainWeatherSourceType = WeatherSourceType.OPEN_WEATHER_MAP;
		}

		if (countryCode.equals("KR")) {
			boolean kmaIsTopPriority = sharedPreferences.getBoolean(getString(R.string.pref_key_kma_top_priority), true);
			if (kmaIsTopPriority) {
				mainWeatherSourceType = WeatherSourceType.KMA;
			}
		}

		return mainWeatherSourceType;
	}


	public void reDraw() {
		//날씨 프래그먼트 다시 그림
		Set<WeatherSourceType> weatherSourceTypeSet = new HashSet<>();
		weatherSourceTypeSet.add(mainWeatherSourceType);
		weatherSourceTypeSet.add(WeatherSourceType.AQICN);
		setWeatherFragments(weatherSourceTypeSet, FINAL_RESPONSE_MAP.get(latitude.toString() + longitude.toString()).multipleRestApiDownloader,
				latitude, longitude, null);
	}

	public void onChangedCurrentLocation(Location currentLocation) {
		this.latitude = currentLocation.getLatitude();
		this.longitude = currentLocation.getLongitude();
		FINAL_RESPONSE_MAP.remove(latitude.toString() + longitude.toString());
		requestAddressOfLocation(latitude, longitude, true);
	}


	public void requestNewData() {
		AlertDialog loadingDialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);

		//메인 날씨 제공사만 요청
		final Set<WeatherSourceType> weatherSourceTypeSet = new HashSet<>();
		weatherSourceTypeSet.add(mainWeatherSourceType);
		weatherSourceTypeSet.add(WeatherSourceType.AQICN);

		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		setRequestWeatherSourceWithSourceTypes(weatherSourceTypeSet, requestWeatherSources);
		final ResponseResultObj responseResultObj = new ResponseResultObj(weatherSourceTypeSet, requestWeatherSources, mainWeatherSourceType);

		multipleRestApiDownloader = new MultipleRestApiDownloader() {
			@Override
			public void onResult() {
				responseResultObj.multipleRestApiDownloader = this;
				processOnResult(responseResultObj);
			}

			@Override
			public void onCanceled() {

			}
		};
		multipleRestApiDownloader.setLoadingDialog(loadingDialog);

		EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, requestWeatherSources, multipleRestApiDownloader);
			}
		});
	}

	private void requestNewDataWithAnotherWeatherSource(WeatherSourceType newWeatherSourceType, WeatherSourceType lastWeatherSourceType) {
		AlertDialog loadingDialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);

		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();

		//메인 날씨 제공사만 요청
		Set<WeatherSourceType> newWeatherSourceTypeSet = new HashSet<>();
		newWeatherSourceTypeSet.add(newWeatherSourceType);
		newWeatherSourceTypeSet.add(WeatherSourceType.AQICN);

		setRequestWeatherSourceWithSourceTypes(newWeatherSourceTypeSet, requestWeatherSources);

		final ResponseResultObj responseResultObj = new ResponseResultObj(newWeatherSourceTypeSet, requestWeatherSources, newWeatherSourceType);
		multipleRestApiDownloader = new MultipleRestApiDownloader() {
			@Override
			public void onResult() {
				responseResultObj.multipleRestApiDownloader = this;
				processOnResult(responseResultObj);
			}

			@Override
			public void onCanceled() {

			}
		};
		multipleRestApiDownloader.setLoadingDialog(loadingDialog);
		EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, requestWeatherSources, multipleRestApiDownloader);
			}
		});
	}

	private void processOnResult(ResponseResultObj responseResultObj) {
		Set<Map.Entry<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>>> entrySet = responseResultObj.multipleRestApiDownloader.getResponseMap().entrySet();
		//메인 날씨 제공사의 데이터가 정상이면 메인 날씨 제공사의 프래그먼트들을 설정하고 값을 표시한다.
		//메인 날씨 제공사의 응답이 불량이면 재 시도, 취소 중 택1 다이얼로그 표시
		for (Map.Entry<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> entry : entrySet) {
			final WeatherSourceType weatherSourceType = entry.getKey();

			if (weatherSourceType == WeatherSourceType.AQICN) {
				continue;
			}

			for (MultipleRestApiDownloader.ResponseResult responseResult : entry.getValue().values()) {
				if (!responseResult.isSuccessful()) {

					if (getActivity() != null) {
						//다시시도, 취소 중 택1
						responseResultObj.multipleRestApiDownloader.getLoadingDialog().dismiss();
						Set<WeatherSourceType> otherTypes = getOtherWeatherSourceTypes(weatherSourceType,
								mainWeatherSourceType);

						final String[] failedDialogItems = new String[otherTypes.size() + 2];
						failedDialogItems[0] = getString(R.string.cancel);
						failedDialogItems[1] = getString(R.string.again);

						final WeatherSourceType[] weatherSourceTypeArr = new WeatherSourceType[otherTypes.size()];
						int arrIndex = 2;

						if (otherTypes.contains(WeatherSourceType.KMA)) {
							weatherSourceTypeArr[arrIndex - 2] = WeatherSourceType.KMA;
							failedDialogItems[arrIndex++] = getString(R.string.kma) + ", " + getString(
									R.string.rerequest_another_weather_datasource);
						}
						if (otherTypes.contains(WeatherSourceType.ACCU_WEATHER)) {
							weatherSourceTypeArr[arrIndex - 2] = WeatherSourceType.ACCU_WEATHER;
							failedDialogItems[arrIndex++] = getString(R.string.accu_weather) + ", " + getString(
									R.string.rerequest_another_weather_datasource);
						}
						if (otherTypes.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
							weatherSourceTypeArr[arrIndex - 2] = WeatherSourceType.OPEN_WEATHER_MAP;
							failedDialogItems[arrIndex] = getString(R.string.owm) + ", " + getString(
									R.string.rerequest_another_weather_datasource);
						}

						final AlertDialog failedDialog = new AlertDialog.Builder(getActivity()).setCancelable(false).setTitle(
								R.string.update_failed).setItems(failedDialogItems, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (which == 1) {
									//다시 시도
									responseResultObj.multipleRestApiDownloader.setLoadingDialog(
											reRefreshBySameWeatherSource(responseResultObj));
								} else if (which == 0) {
									//취소
									if (!containWeatherData(latitude, longitude)) {
										getActivity().finish();
									}
								} else if (which >= 2) {
									//다른 제공사로 요청
									responseResultObj.mainWeatherSourceType = weatherSourceTypeArr[which - 2];
									responseResultObj.weatherSourceTypeSet.clear();
									responseResultObj.weatherSourceTypeSet.add(responseResultObj.mainWeatherSourceType);
									responseResultObj.weatherSourceTypeSet.add(WeatherSourceType.AQICN);
									responseResultObj.multipleRestApiDownloader.setLoadingDialog(
											reRefreshByAnotherWeatherSource(responseResultObj));
								}
								dialog.dismiss();
							}
						}).create();

						MainThreadWorker.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								failedDialog.show();
							}
						});
					}

					return;
				}
			}

		}
		//응답 성공 하면
		final WeatherResponseObj weatherResponseObj = new WeatherResponseObj(responseResultObj.multipleRestApiDownloader,
				responseResultObj.weatherSourceTypeSet, responseResultObj.mainWeatherSourceType);

		FINAL_RESPONSE_MAP.put(latitude.toString() + longitude.toString(), weatherResponseObj);
		setWeatherFragments(responseResultObj.weatherSourceTypeSet, responseResultObj.multipleRestApiDownloader, latitude, longitude,
				responseResultObj.multipleRestApiDownloader.getLoadingDialog());
	}

	/**
	 * kma, accu, owm
	 * 요청 : kma, 현재 : owm ->  accu
	 * 요청 : kma, 현재 : accu ->  owm
	 * 요청 : kma, 현재 : kma ->  owm, accu
	 * <p>
	 * 요청 : accu, 현재 : accu ->  owm
	 * 요청 : accu, 현재 : accu ->  owm, kma (only kr)
	 * 요청 : accu, 현재 : owm ->  미 표시
	 * 요청 : accu, 현재 : owm ->  kma (only kr)
	 * 요청 : accu, 현재 : kma ->  owm
	 * <p>
	 * 요청 : owm, 현재 : owm ->  accu
	 * 요청 : owm, 현재 : owm ->  accu, kma (only kr)
	 * 요청 : owm, 현재 : accu ->  미 표시
	 * 요청 : owm, 현재 : accu ->  kma (only kr)
	 * 요청 : owm, 현재 : kma ->  accu
	 */
	private Set<WeatherSourceType> getOtherWeatherSourceTypes(WeatherSourceType requestWeatherSourceType,
	                                                          WeatherSourceType lastWeatherSourceType) {
		Set<WeatherSourceType> others = new HashSet<>();

		if (requestWeatherSourceType == WeatherSourceType.KMA) {

			if (lastWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
				others.add(WeatherSourceType.ACCU_WEATHER);
			} else if (lastWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
				others.add(WeatherSourceType.OPEN_WEATHER_MAP);
			} else {
				others.add(WeatherSourceType.OPEN_WEATHER_MAP);
				others.add(WeatherSourceType.ACCU_WEATHER);
			}
		} else if (requestWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {

			if (lastWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
				if (countryCode.equals("KR")) {
					others.add(WeatherSourceType.OPEN_WEATHER_MAP);
					others.add(WeatherSourceType.KMA);
				} else {
					others.add(WeatherSourceType.OPEN_WEATHER_MAP);
				}
			} else if (lastWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
				if (countryCode.equals("KR")) {
					others.add(WeatherSourceType.KMA);
				}
			} else {
				others.add(WeatherSourceType.OPEN_WEATHER_MAP);
			}
		} else if (requestWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {

			if (lastWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
				if (countryCode.equals("KR")) {
					others.add(WeatherSourceType.ACCU_WEATHER);
					others.add(WeatherSourceType.KMA);
				} else {
					others.add(WeatherSourceType.ACCU_WEATHER);
				}
			} else if (lastWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
				if (countryCode.equals("KR")) {
					others.add(WeatherSourceType.KMA);
				}
			} else {
				others.add(WeatherSourceType.ACCU_WEATHER);
			}
		}

		return others;
	}

	private AlertDialog reRefreshBySameWeatherSource(ResponseResultObj responseResultObj) {
		final AlertDialog loadingDialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);

		final WeatherSourceType requestWeatherSource = responseResultObj.mainWeatherSourceType;
		ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> result = responseResultObj.multipleRestApiDownloader.getResponseMap().get(
				requestWeatherSource);

		ArrayMap<WeatherSourceType, RequestWeatherSource> newRequestWeatherSources = new ArrayMap<>();
		//요청한 날씨 제공사만 가져옴
		RequestWeatherSource failedRequestWeatherSource = responseResultObj.requestWeatherSources.get(requestWeatherSource);
		newRequestWeatherSources.put(requestWeatherSource, failedRequestWeatherSource);
		failedRequestWeatherSource.getRequestServiceTypes().clear();

		//실패한 자료만 재 요청
		for (int i = 0; i < result.size(); i++) {
			if (result.valueAt(i).getT() != null) {
				failedRequestWeatherSource.addRequestServiceType(result.keyAt(i));
			}
		}

		EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.reRequestWeatherDataBySameWeatherSourceIfFailed(getContext(), latitude, longitude, newRequestWeatherSources,
						responseResultObj.multipleRestApiDownloader);
			}
		});

		return loadingDialog;
	}

	private AlertDialog reRefreshByAnotherWeatherSource(ResponseResultObj responseResultObj) {
		final AlertDialog loadingDialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);

		ArrayMap<WeatherSourceType, RequestWeatherSource> newRequestWeatherSources = new ArrayMap<>();
		setRequestWeatherSourceWithSourceTypes(responseResultObj.weatherSourceTypeSet, newRequestWeatherSources);
		responseResultObj.requestWeatherSources = newRequestWeatherSources;

		EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.reRequestWeatherDataByAnotherWeatherSourceIfFailed(getContext(), latitude, longitude,
						responseResultObj.requestWeatherSources, responseResultObj.multipleRestApiDownloader);
			}
		});

		return loadingDialog;
	}

	private void setRequestWeatherSourceWithSourceTypes(Set<WeatherSourceType> weatherSourceTypeSet,
	                                                    ArrayMap<WeatherSourceType, RequestWeatherSource> newRequestWeatherSources) {

		if (weatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_NCST).addRequestServiceType(
					RetrofitClient.ServiceType.ULTRA_SRT_FCST).addRequestServiceType(
					RetrofitClient.ServiceType.VILAGE_FCST).addRequestServiceType(
					RetrofitClient.ServiceType.MID_LAND_FCST).addRequestServiceType(RetrofitClient.ServiceType.MID_TA_FCST)
					.addRequestServiceType(RetrofitClient.ServiceType.YESTERDAY_ULTRA_SRT_NCST);
			newRequestWeatherSources.put(WeatherSourceType.KMA, requestKma);
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			RequestOwm requestOwm = new RequestOwm();
			requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
			Set<OneCallParameter.OneCallApis> excludes = new HashSet<>();
			excludes.add(OneCallParameter.OneCallApis.minutely);
			excludes.add(OneCallParameter.OneCallApis.alerts);
			requestOwm.setExcludeApis(excludes);

			newRequestWeatherSources.put(WeatherSourceType.OPEN_WEATHER_MAP, requestOwm);
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			RequestAccu requestAccu = new RequestAccu();
			requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).addRequestServiceType(
					RetrofitClient.ServiceType.ACCU_12_HOURLY).addRequestServiceType(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

			newRequestWeatherSources.put(WeatherSourceType.ACCU_WEATHER, requestAccu);
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.AQICN)) {
			RequestAqicn requestAqicn = new RequestAqicn();
			requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

			newRequestWeatherSources.put(WeatherSourceType.AQICN, requestAqicn);
		}

	}


	private void setWeatherFragments(Set<WeatherSourceType> weatherSourceTypeSet, MultipleRestApiDownloader multipleRestApiDownloader,
	                                 Double latitude, Double longitude, @Nullable AlertDialog loadingDialog) {
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap = multipleRestApiDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> arrayMap = null;

		Fragment simpleCurrentConditionsFragment = null;
		Fragment simpleHourlyForecastFragment = null;
		Fragment simpleDailyForecastFragment = null;
		Fragment detailCurrentConditionsFragment = null;
		GeolocalizedFeedResponse airQualityResponse = null;

		String currentConditionsWeatherVal = null;
		ZoneId zoneId = null;

		if (weatherSourceTypeSet.contains(WeatherSourceType.AQICN)) {
			MultipleRestApiDownloader.ResponseResult aqicnResponse = responseMap.get(WeatherSourceType.AQICN).get(
					RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
			if (aqicnResponse.isSuccessful()) {
				airQualityResponse = (GeolocalizedFeedResponse) aqicnResponse.getResponseObj();
			}

		}

		if (weatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			arrayMap = responseMap.get(WeatherSourceType.KMA);

			FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditions(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST).getResponseObj());
			FinalCurrentConditions yesterDayFinalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditions(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.YESTERDAY_ULTRA_SRT_NCST).getResponseObj());

			List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST).getResponseObj(),
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST).getResponseObj());

			List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(
					(MidLandFcstResponse) arrayMap.get(RetrofitClient.ServiceType.MID_LAND_FCST).getResponseObj(),
					(MidTaResponse) arrayMap.get(RetrofitClient.ServiceType.MID_TA_FCST).getResponseObj(),
					Long.parseLong(multipleRestApiDownloader.get("tmFc")));

			finalDailyForecastList = KmaResponseProcessor.getDailyForecastList(finalDailyForecastList, finalHourlyForecastList);

			KmaSimpleCurrentConditionsFragment kmaSimpleCurrentConditionsFragment = new KmaSimpleCurrentConditionsFragment();
			KmaSimpleHourlyForecastFragment kmaSimpleHourlyForecastFragment = new KmaSimpleHourlyForecastFragment();
			KmaSimpleDailyForecastFragment kmaSimpleDailyForecastFragment = new KmaSimpleDailyForecastFragment();
			KmaDetailCurrentConditionsFragment kmaDetailCurrentConditionsFragment = new KmaDetailCurrentConditionsFragment();

			kmaSimpleCurrentConditionsFragment.setTodayFinalCurrentConditions(finalCurrentConditions).setFinalHourlyForecast(
					finalHourlyForecastList.get(0)).setAirQualityResponse(airQualityResponse);
			kmaSimpleCurrentConditionsFragment.setYesterdayFinalCurrentConditions(yesterDayFinalCurrentConditions);
			kmaSimpleCurrentConditionsFragment.setFinalHourlyForecast(finalHourlyForecastList.get(0));
			kmaSimpleHourlyForecastFragment.setFinalHourlyForecastList(finalHourlyForecastList);
			kmaSimpleDailyForecastFragment.setFinalDailyForecastList(finalDailyForecastList);
			kmaDetailCurrentConditionsFragment.setFinalCurrentConditions(finalCurrentConditions);
			kmaDetailCurrentConditionsFragment.setFinalHourlyForecast(finalHourlyForecastList.get(0));

			simpleCurrentConditionsFragment = kmaSimpleCurrentConditionsFragment;
			simpleHourlyForecastFragment = kmaSimpleHourlyForecastFragment;
			simpleDailyForecastFragment = kmaSimpleDailyForecastFragment;
			detailCurrentConditionsFragment = kmaDetailCurrentConditionsFragment;

			String sky = finalHourlyForecastList.get(0).getSky();
			String pty = finalCurrentConditions.getPrecipitationType();

			currentConditionsWeatherVal = pty.equals("0") ? sky + "_sky" : pty + "_pty";
			zoneId = KmaResponseProcessor.getZoneId();
			mainWeatherSourceType = WeatherSourceType.KMA;

		} else if (weatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(WeatherSourceType.ACCU_WEATHER);

			AccuSimpleCurrentConditionsFragment accuSimpleCurrentConditionsFragment = new AccuSimpleCurrentConditionsFragment();
			AccuSimpleHourlyForecastFragment accuSimpleHourlyForecastFragment = new AccuSimpleHourlyForecastFragment();
			AccuSimpleDailyForecastFragment accuSimpleDailyForecastFragment = new AccuSimpleDailyForecastFragment();
			AccuDetailCurrentConditionsFragment accuDetailCurrentConditionsFragment = new AccuDetailCurrentConditionsFragment();

			CurrentConditionsResponse currentConditionsResponse =
					(CurrentConditionsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponseObj();

			accuSimpleCurrentConditionsFragment.setCurrentConditionsResponse(currentConditionsResponse).setAirQualityResponse(
					airQualityResponse);
			accuSimpleHourlyForecastFragment.setTwelveHoursOfHourlyForecastsResponse(
					(TwelveHoursOfHourlyForecastsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_12_HOURLY).getResponseObj());
			accuSimpleDailyForecastFragment.setFiveDaysOfDailyForecastsResponse(
					(FiveDaysOfDailyForecastsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponseObj());
			accuDetailCurrentConditionsFragment.setCurrentConditionsResponse(currentConditionsResponse);

			simpleCurrentConditionsFragment = accuSimpleCurrentConditionsFragment;
			simpleHourlyForecastFragment = accuSimpleHourlyForecastFragment;
			simpleDailyForecastFragment = accuSimpleDailyForecastFragment;
			detailCurrentConditionsFragment = accuDetailCurrentConditionsFragment;

			currentConditionsWeatherVal = currentConditionsResponse.getItems().get(0).getWeatherIcon();
			zoneId = ZonedDateTime.parse(currentConditionsResponse.getItems().get(0).getLocalObservationDateTime()).getZone();

			mainWeatherSourceType = WeatherSourceType.ACCU_WEATHER;
		} else if (weatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			arrayMap = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP);

			OwmSimpleCurrentConditionsFragment owmSimpleCurrentConditionsFragment = new OwmSimpleCurrentConditionsFragment();
			OwmSimpleHourlyForecastFragment owmSimpleHourlyForecastFragment = new OwmSimpleHourlyForecastFragment();
			OwmSimpleDailyForecastFragment owmSimpleDailyForecastFragment = new OwmSimpleDailyForecastFragment();
			OwmDetailCurrentConditionsFragment owmDetailCurrentConditionsFragment = new OwmDetailCurrentConditionsFragment();

			OneCallResponse oneCallResponse =
					(OneCallResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponseObj();

			owmSimpleCurrentConditionsFragment.setOneCallResponse(oneCallResponse).setAirQualityResponse(airQualityResponse);
			owmSimpleHourlyForecastFragment.setOneCallResponse(oneCallResponse);
			owmSimpleDailyForecastFragment.setOneCallResponse(oneCallResponse);
			owmDetailCurrentConditionsFragment.setOneCallResponse(oneCallResponse);

			simpleCurrentConditionsFragment = owmSimpleCurrentConditionsFragment;
			simpleHourlyForecastFragment = owmSimpleHourlyForecastFragment;
			simpleDailyForecastFragment = owmSimpleDailyForecastFragment;
			detailCurrentConditionsFragment = owmDetailCurrentConditionsFragment;

			currentConditionsWeatherVal = oneCallResponse.getCurrent().getWeather().get(0).getId();

			zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);
			mainWeatherSourceType = WeatherSourceType.OPEN_WEATHER_MAP;

		}

		loadImgOfCurrentConditions(mainWeatherSourceType, currentConditionsWeatherVal, latitude, longitude,
				zoneId);

		if (getActivity() != null) {
			final Bundle defaultBundle = new Bundle();
			defaultBundle.putDouble(BundleKey.Latitude.name(), this.latitude);
			defaultBundle.putDouble(BundleKey.Longitude.name(), this.longitude);
			defaultBundle.putString(BundleKey.AddressName.name(), addressName);
			defaultBundle.putString(BundleKey.CountryCode.name(), countryCode);
			defaultBundle.putSerializable(BundleKey.WeatherDataSource.name(), mainWeatherSourceType);
			defaultBundle.putSerializable(BundleKey.TimeZone.name(), zoneId);

			SimpleAirQualityFragment simpleAirQualityFragment = new SimpleAirQualityFragment();
			simpleAirQualityFragment.setGeolocalizedFeedResponse(airQualityResponse);
			simpleAirQualityFragment.setArguments(defaultBundle);

			Fragment sunSetRiseFragment = new SunsetriseFragment();
			sunSetRiseFragment.setArguments(defaultBundle);

			Fragment finalSimpleDailyForecastFragment = simpleDailyForecastFragment;
			Fragment finalSimpleHourlyForecastFragment = simpleHourlyForecastFragment;
			Fragment finalSimpleCurrentConditionsFragment = simpleCurrentConditionsFragment;
			Fragment finalDetailCurrentConditionsFragment = detailCurrentConditionsFragment;

			finalSimpleHourlyForecastFragment.setArguments(defaultBundle);
			finalSimpleDailyForecastFragment.setArguments(defaultBundle);
			finalSimpleCurrentConditionsFragment.setArguments(defaultBundle);
			finalDetailCurrentConditionsFragment.setArguments(defaultBundle);

			ValueUnits clockUnit = ValueUnits.valueOf(
					sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name()));
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					createWeatherDataSourcePicker(countryCode);
					ZonedDateTime dateTime = multipleRestApiDownloader.getRequestDateTime();
					DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
							clockUnit == ValueUnits.clock12 ? getString(R.string.datetime_pattern_clock12) :
									getString(R.string.datetime_pattern_clock24), Locale.getDefault());
					binding.updatedDatetime.setText(dateTime.format(dateTimeFormatter));

					getChildFragmentManager().beginTransaction().replace(binding.simpleCurrentConditions.getId(),
							finalSimpleCurrentConditionsFragment, getString(R.string.tag_simple_current_conditions_fragment)).replace(
							binding.simpleHourlyForecast.getId(), finalSimpleHourlyForecastFragment,
							getString(R.string.tag_simple_hourly_forecast_fragment)).replace(binding.simpleDailyForecast.getId(),
							finalSimpleDailyForecastFragment, getString(R.string.tag_simple_daily_forecast_fragment)).replace(
							binding.detailCurrentConditions.getId(), finalDetailCurrentConditionsFragment,
							getString(R.string.tag_detail_current_conditions_fragment)).replace(binding.simpleAirQuality.getId(),
							simpleAirQualityFragment, getString(R.string.tag_simple_air_quality_fragment)).replace(
							binding.sunSetRise.getId(), sunSetRiseFragment, getString(R.string.tag_sun_set_rise_fragment)).commitAllowingStateLoss();

					binding.adViewBelowAirQuality.setVisibility(View.VISIBLE);
					binding.adViewBottom.setVisibility(View.VISIBLE);

					if (loadingDialog != null) {
						loadingDialog.dismiss();
					}
				}
			});

		}
	}

	private void createWeatherDataSourcePicker(String countryCode) {
		switch (mainWeatherSourceType) {
			case KMA:
				binding.datasource.setText(R.string.kma);
				break;
			case ACCU_WEATHER:
				binding.datasource.setText(R.string.accu_weather);
				break;
			case OPEN_WEATHER_MAP:
				binding.datasource.setText(R.string.owm);
				break;
		}

		binding.datasource.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				CharSequence[] items = new CharSequence[countryCode.equals("KR") ? 3 : 2];
				int checkedItemIdx = 0;

				if (countryCode.equals("KR")) {
					items[0] = getString(R.string.kma);
					items[1] = getString(R.string.accu_weather);
					items[2] = getString(R.string.owm);

					checkedItemIdx = (mainWeatherSourceType == WeatherSourceType.KMA) ? 0 : (mainWeatherSourceType == WeatherSourceType.ACCU_WEATHER) ? 1 : 2;
				} else {
					items[0] = getString(R.string.accu_weather);
					items[1] = getString(R.string.owm);
					checkedItemIdx = mainWeatherSourceType == WeatherSourceType.ACCU_WEATHER ? 0 : 1;
				}
				final int finalCheckedItemIdx = checkedItemIdx;

				new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.title_pick_weather_data_source).setSingleChoiceItems(items,
						checkedItemIdx, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int index) {
								WeatherSourceType lastWeatherSourceType = mainWeatherSourceType;
								WeatherSourceType newWeatherSourceType;

								if (finalCheckedItemIdx != index) {
									if (!items[index].equals(getString(R.string.kma))) {
										// 선택된 제공사가 accu, owm 둘 중 하나이면 우선순위 변경
										boolean accu = items[index].equals(getString(R.string.accu_weather));

										SharedPreferences.Editor editor = sharedPreferences.edit();
										editor.putBoolean(getString(R.string.pref_key_accu_weather), accu);
										editor.putBoolean(getString(R.string.pref_key_open_weather_map), !accu);
										editor.apply();

										newWeatherSourceType = accu ? WeatherSourceType.ACCU_WEATHER : WeatherSourceType.OPEN_WEATHER_MAP;
									} else {
										newWeatherSourceType = WeatherSourceType.KMA;
									}
									requestNewDataWithAnotherWeatherSource(newWeatherSourceType, lastWeatherSourceType);
								}
								dialogInterface.dismiss();
							}
						}).create().show();
			}
		});
	}

	public LocationType getLocationType() {
		return locationType;
	}

	private static class ResponseResultObj implements Serializable {
		MultipleRestApiDownloader multipleRestApiDownloader;
		Set<WeatherSourceType> weatherSourceTypeSet;
		WeatherSourceType mainWeatherSourceType;
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources;

		public ResponseResultObj(Set<WeatherSourceType> weatherSourceTypeSet,
		                         ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources, WeatherSourceType mainWeatherSourceType) {
			this.weatherSourceTypeSet = weatherSourceTypeSet;
			this.requestWeatherSources = requestWeatherSources;
			this.mainWeatherSourceType = mainWeatherSourceType;
		}
	}

	private static class WeatherResponseObj implements Serializable {
		final MultipleRestApiDownloader multipleRestApiDownloader;
		final Set<WeatherSourceType> requestWeatherSourceTypeSet;
		final WeatherSourceType requestMainWeatherSourceType;

		public WeatherResponseObj(MultipleRestApiDownloader multipleRestApiDownloader, Set<WeatherSourceType> requestWeatherSourceTypeSet, WeatherSourceType requestMainWeatherSourceType) {
			this.multipleRestApiDownloader = multipleRestApiDownloader;
			this.requestWeatherSourceTypeSet = requestWeatherSourceTypeSet;
			this.requestMainWeatherSourceType = requestMainWeatherSourceType;
		}
	}

}