package com.lifedawn.bestweather.weathers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.matteobattilana.weather.PrecipType;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alert.AlertFragment;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.classes.TextUtil;
import com.lifedawn.bestweather.commons.classes.WeatherViewController;

import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.Flickr;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.interfaces.IGps;

import com.lifedawn.bestweather.commons.views.HeaderbarStyle;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.flickr.FlickrRepository;
import com.lifedawn.bestweather.flickr.FlickrViewModel;
import com.lifedawn.bestweather.main.IRefreshFavoriteLocationListOnSideNav;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.rainviewer.view.SimpleRainViewerFragment;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;

import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import com.lifedawn.bestweather.timezone.TimeZoneUtils;

import com.lifedawn.bestweather.weathers.detailfragment.currentconditions.DetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.interfaces.ILoadWeatherData;
import com.lifedawn.bestweather.weathers.models.WeatherDataDTO;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.SimpleAirQualityFragment;
import com.lifedawn.bestweather.weathers.simplefragment.currentconditions.SimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.dailyforecast.SimpleDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.hourlyforecast.SimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.sunsetrise.SunsetriseFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherFragmentViewModel;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;


public class WeatherFragment extends Fragment implements IGps, ILoadWeatherData {
	private final ExecutorService executorService = MyApplication.getExecutorService();
	private FragmentWeatherBinding binding;
	private LoadingViewAsyncBinding asyncBinding;
	private WeatherViewModel weatherViewModel;
	private View.OnClickListener menuOnClickListener;
	private FusedLocation fusedLocation;
	private NetworkStatus networkStatus;
	private FusedLocation.MyLocationCallback locationCallbackInMainFragment;
	private WeatherViewController weatherViewController;
	private FlickrViewModel flickrViewModel;
	private IRefreshFavoriteLocationListOnSideNav iRefreshFavoriteLocationListOnSideNav;
	private LocationLifeCycleObserver locationLifeCycleObserver;
	private final IWeatherFragment iWeatherFragment;

	private WeatherFragmentViewModel weatherFragmentViewModel;

	public WeatherFragment(IWeatherFragment iWeatherFragment) {
		this.iWeatherFragment = iWeatherFragment;
	}


	public WeatherFragment setMenuOnClickListener(View.OnClickListener menuOnClickListener) {
		this.menuOnClickListener = menuOnClickListener;
		return this;
	}

	public WeatherFragment setiRefreshFavoriteLocationListOnSideNav(IRefreshFavoriteLocationListOnSideNav iRefreshFavoriteLocationListOnSideNav) {
		this.iRefreshFavoriteLocationListOnSideNav = iRefreshFavoriteLocationListOnSideNav;
		return this;
	}


	private OnAsyncLoadCallback onAsyncLoadCallback;

	public WeatherFragment setOnAsyncLoadCallback(OnAsyncLoadCallback onAsyncLoadCallback) {
		this.onAsyncLoadCallback = onAsyncLoadCallback;
		return this;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, requireActivity());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);

		networkStatus = NetworkStatus.getInstance(requireContext().getApplicationContext());
		fusedLocation = new FusedLocation(requireContext().getApplicationContext());
		weatherFragmentViewModel = new ViewModelProvider(this).get(WeatherFragmentViewModel.class);

		weatherViewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
		locationCallbackInMainFragment = weatherViewModel.getLocationCallback();

		flickrViewModel = new ViewModelProvider(this).get(FlickrViewModel.class);

		weatherFragmentViewModel.arguments = getArguments() == null ? savedInstanceState : getArguments();
		weatherFragmentViewModel.locationType = (LocationType) weatherFragmentViewModel.arguments.getSerializable("LocationType");
		weatherFragmentViewModel.favoriteAddressDto = weatherFragmentViewModel.arguments.containsKey("FavoriteAddressDto") ?
				(FavoriteAddressDto) weatherFragmentViewModel.arguments.getSerializable("FavoriteAddressDto") : null;
	}

	/**
	 * hidden is true이면 black, else white
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (hidden) {
			HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, getActivity());
		} else {
			HeaderbarStyle.setStyle(HeaderbarStyle.Style.White, getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false);
		final AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(requireContext());
		asyncLayoutInflater.inflate(R.layout.fragment_weather, container, new AsyncLayoutInflater.OnInflateFinishedListener() {
			@SuppressLint("MissingPermission")
			@Override
			public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
				binding = FragmentWeatherBinding.bind(view);
				asyncBinding.getRoot().addView(binding.getRoot());
				asyncBinding.progressCircular.pauseAnimation();
				asyncBinding.progressCircular.setVisibility(View.GONE);

				shimmer(true, false);
				final int statusBarHeight = MyApplication.getStatusBarHeight();

				FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.mainToolbar.getRoot().getLayoutParams();
				layoutParams.topMargin = statusBarHeight;
				binding.mainToolbar.getRoot().setLayoutParams(layoutParams);

				int topMargin = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics())
						+ getResources().getDimension(R.dimen.toolbarHeight) + statusBarHeight);
				ConstraintLayout.LayoutParams headerLayoutParams = (ConstraintLayout.LayoutParams) binding.weatherDataSourceLayout.getLayoutParams();
				headerLayoutParams.topMargin = topMargin;
				binding.weatherDataSourceLayout.setLayoutParams(headerLayoutParams);

				onChangedStateBackgroundImg(false);

				binding.loadingAnimation.setVisibility(View.VISIBLE);
				binding.flickrImageUrl.setVisibility(View.GONE);

				binding.currentConditionsImg.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black_alpha_30), PorterDuff.Mode.DARKEN);

				weatherViewController = new WeatherViewController(binding.rootLayout);
				getLifecycle().addObserver(weatherViewController);

				weatherViewController.setWeatherView(PrecipType.CLEAR, null);

				binding.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener);
				binding.mainToolbar.gps.setOnClickListener(v -> {
					if (networkStatus.networkAvailable()) {
						shimmer(true, false);
						fusedLocation.findCurrentLocation(MY_LOCATION_CALLBACK, false);
					} else {
						Toast.makeText(getContext(), R.string.disconnected_network, Toast.LENGTH_SHORT).show();
					}
				});

				binding.mainToolbar.find.setOnClickListener(v -> {
					if (networkStatus.networkAvailable()) {
						final MapFragment mapFragment = new MapFragment();

						final Bundle arguments = new Bundle();
						arguments.putString(BundleKey.RequestFragment.name(), WeatherFragment.class.getName());
						mapFragment.setArguments(arguments);
						mapFragment.setOnResultFavoriteListener(new MapFragment.OnResultFavoriteListener() {
							@Override
							public void onAddedNewAddress(FavoriteAddressDto newFavoriteAddressDto, List<FavoriteAddressDto> favoriteAddressDtoList, boolean removed) {
								getParentFragmentManager().popBackStack();
								iRefreshFavoriteLocationListOnSideNav.onResultMapFragment(newFavoriteAddressDto);
							}

							@Override
							public void onResult(List<FavoriteAddressDto> favoriteAddressDtoList) {
								iRefreshFavoriteLocationListOnSideNav.onResultMapFragment(null);
							}

							@Override
							public void onClickedAddress(@Nullable FavoriteAddressDto favoriteAddressDto) {

							}
						});

						getParentFragmentManager().beginTransaction().hide(WeatherFragment.this).add(R.id.fragment_container,
								mapFragment, MapFragment.class.getName()).addToBackStack(
								MapFragment.class.getName()).setPrimaryNavigationFragment(mapFragment).commitAllowingStateLoss();
					}
				});

				binding.mainToolbar.refresh.setOnClickListener(v -> {
					if (networkStatus.networkAvailable()) {
						requestNewData();
					} else {
						Toast.makeText(getContext(), R.string.disconnected_network, Toast.LENGTH_SHORT).show();
					}
				});

				binding.flickrImageUrl.setOnClickListener(v -> {
					if (binding.flickrImageUrl.getTag() != null) {
						String url = (String) binding.flickrImageUrl.getTag();

						if (url.equals("failed")) {
							loadImgOfCurrentConditions(flickrViewModel.getLastParameter());
						} else {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(url));
							startActivity(intent);
						}
					}

				});

				AdRequest adRequest = new AdRequest.Builder().build();
				AdLoader adLoader = new AdLoader.Builder(requireActivity(), getString(R.string.NATIVE_ADVANCE_unitId))
						.forNativeAd(nativeAd -> {
							NativeTemplateStyle styles = new
									NativeTemplateStyle.Builder().withMainBackgroundColor(new ColorDrawable(Color.WHITE)).build();
							TemplateView template = binding.adViewBottom;
							template.setStyles(styles);
							template.setNativeAd(nativeAd);
						}).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true).build())
						.build();

				adLoader.loadAd(adRequest);

				binding.adViewBelowAirQuality.loadAd(adRequest);
				binding.adViewBelowAirQuality.setAdListener(new AdListener() {
					@Override
					public void onAdClosed() {
						super.onAdClosed();
						binding.adViewBelowAirQuality.loadAd(new AdRequest.Builder().build());
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						super.onAdFailedToLoad(loadAdError);
					}
				});

				binding.adViewBelowDetailCurrentConditions.loadAd(adRequest);
				binding.adViewBelowDetailCurrentConditions.setAdListener(new AdListener() {
					@Override
					public void onAdClosed() {
						super.onAdClosed();
						binding.adViewBelowDetailCurrentConditions.loadAd(new AdRequest.Builder().build());
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						super.onAdFailedToLoad(loadAdError);
					}
				});


				weatherFragmentViewModel.resumedFragmentObserver.observe(getViewLifecycleOwner(), aBoolean -> {
					shimmer(false, true);
					loadImgOfCurrentConditions(flickrViewModel.getLastParameter());
				});

				flickrViewModel.img.observe(getViewLifecycleOwner(), flickrImgResponse -> {
					onChangedStateBackgroundImg(flickrImgResponse.successful);

					if (flickrImgResponse.successful) {
						Glide.with(requireContext()).load(flickrImgResponse.flickrImgData.getImg()).diskCacheStrategy(DiskCacheStrategy.ALL).transition(
								DrawableTransitionOptions.withCrossFade(300)).into(binding.currentConditionsImg);

						final String text = flickrImgResponse.flickrImgData.getPhoto().getOwner() + "-" + flickrImgResponse.flickrImgData.getPhoto().getTitle();
						binding.flickrImageUrl.setText(TextUtil.getUnderLineColorText(text, text,
								ContextCompat.getColor(requireContext(), R.color.white)));
						binding.flickrImageUrl.setTag(flickrImgResponse.flickrImgData.getRealFlickrUrl());

						setBackgroundWeatherView(flickrImgResponse.flickrImgData.getWeather(), flickrImgResponse.flickrImgData.getVolume());
					} else {
						Glide.with(requireContext()).clear(binding.currentConditionsImg);

						final String text = getString(R.string.failed_load_img);
						binding.flickrImageUrl.setText(TextUtil.getUnderLineColorText(text, text,
								ContextCompat.getColor(requireContext(), R.color.black)));
						binding.flickrImageUrl.setTag("failed");

						if (flickrImgResponse.flickrImgData != null) {
							setBackgroundWeatherView(flickrImgResponse.flickrImgData.getWeather(), flickrImgResponse.flickrImgData.getVolume());
						}

					}
					binding.loadingAnimation.setVisibility(View.GONE);
					binding.flickrImageUrl.setVisibility(View.VISIBLE);
				});


				weatherFragmentViewModel.weatherDataResponse.observe(getViewLifecycleOwner(), responseResultObj -> {
					if (responseResultObj != null) {
						processOnResult(responseResultObj);
					}
				});

				shimmer(false, false);
				if (onAsyncLoadCallback != null)
					onAsyncLoadCallback.onFinished(WeatherFragment.this);
				onAsyncLoadCallback = null;
			}
		});

		return asyncBinding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(weatherFragmentViewModel.arguments);
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}


	@Override
	public void load() {
		shimmer(true, false);

		binding.mainToolbar.gps.setVisibility(weatherFragmentViewModel.locationType == LocationType.CurrentLocation ? View.VISIBLE : View.GONE);
		binding.mainToolbar.find.setVisibility(weatherFragmentViewModel.locationType == LocationType.CurrentLocation ? View.GONE : View.VISIBLE);

		executorService.submit(() -> {
			final Bundle bundle = new Bundle();
			bundle.putSerializable(BundleKey.SelectedAddressDto.name(), weatherFragmentViewModel.favoriteAddressDto);
			bundle.putSerializable(BundleKey.IGps.name(), (IGps) this);
			bundle.putString(BundleKey.LocationType.name(), weatherFragmentViewModel.locationType.name());
			bundle.putString(BundleKey.RequestFragment.name(), WeatherFragment.class.getName());

			weatherFragmentViewModel.locationType = LocationType.valueOf(bundle.getString(BundleKey.LocationType.name()));

			final boolean clickGps = weatherFragmentViewModel.arguments.getBoolean("clickGps", false);

			if (weatherFragmentViewModel.locationType == LocationType.CurrentLocation) {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
				sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1).putString(
						getString(R.string.pref_key_last_selected_location_type), weatherFragmentViewModel.locationType.name()).commit();

				LocationResult locationResult = fusedLocation.getLastCurrentLocation();
				weatherFragmentViewModel.latitude = locationResult.getLocations().get(0).getLatitude();
				weatherFragmentViewModel.longitude = locationResult.getLocations().get(0).getLongitude();

				if (weatherFragmentViewModel.latitude == 0.0 && weatherFragmentViewModel.longitude == 0.0) {
					//최근에 현재위치로 잡힌 위치가 없으므로 현재 위치 요청
					requestCurrentLocation();
				} else if (clickGps) {
					MainThreadWorker.runOnUiThread(() -> {
						binding.mainToolbar.gps.callOnClick();
					});

				} else {
					weatherFragmentViewModel.zoneId = ZoneId.of(PreferenceManager.getDefaultSharedPreferences(requireContext())
							.getString("zoneId", ""));
					//위/경도에 해당하는 지역명을 불러오고, 날씨 데이터 다운로드
					//이미 존재하는 날씨 데이터면 다운로드X
					boolean refresh = !weatherFragmentViewModel.containWeatherData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude);

					if (weatherFragmentViewModel.isOldDownloadedData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude)) {
						refresh = true;
						weatherFragmentViewModel.removeOldDownloadedData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude);
					}

					boolean finalRefresh = refresh;
					MainThreadWorker.runOnUiThread(() -> {
						requestAddressOfLocation(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude, finalRefresh);
					});

				}
			} else {
				weatherFragmentViewModel.selectedFavoriteAddressDto = (FavoriteAddressDto) bundle.getSerializable(
						BundleKey.SelectedAddressDto.name());

				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
				sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
						weatherFragmentViewModel.selectedFavoriteAddressDto.getId()).putString(getString(R.string.pref_key_last_selected_location_type),
						weatherFragmentViewModel.locationType.name()).commit();

				weatherFragmentViewModel.mainWeatherProviderType =
						weatherFragmentViewModel.getMainWeatherSourceType(weatherFragmentViewModel.selectedFavoriteAddressDto.getCountryCode());
				weatherFragmentViewModel.countryCode = weatherFragmentViewModel.selectedFavoriteAddressDto.getCountryCode();
				weatherFragmentViewModel.addressName = weatherFragmentViewModel.selectedFavoriteAddressDto.getDisplayName();
				weatherFragmentViewModel.latitude = Double.parseDouble(weatherFragmentViewModel.selectedFavoriteAddressDto.getLatitude());
				weatherFragmentViewModel.longitude = Double.parseDouble(weatherFragmentViewModel.selectedFavoriteAddressDto.getLongitude());
				weatherFragmentViewModel.zoneId = ZoneId.of(weatherFragmentViewModel.selectedFavoriteAddressDto.getZoneId());

				if (weatherFragmentViewModel.containWeatherData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude)) {
					if (weatherFragmentViewModel.isOldDownloadedData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude)) {
						weatherFragmentViewModel.removeOldDownloadedData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude);
						requestNewData();
					} else {
						//기존 데이터 표시
						try {
							final WeatherResponseObj responseResultObj =
									WeatherFragmentViewModel.FINAL_RESPONSE_MAP.get(weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString());
							if (responseResultObj != null) {
								weatherFragmentViewModel.mainWeatherProviderType = responseResultObj.requestMainWeatherProviderType;
								MainThreadWorker.runOnUiThread(this::reDraw);
							}
						} catch (Exception e) {
							requestNewData();
						}

					}
				} else {
					MainThreadWorker.runOnUiThread(() -> {
						requestNewData();
					});

				}

				MainThreadWorker.runOnUiThread(() -> {
					binding.addressName.setText(weatherFragmentViewModel.addressName);
					binding.countryName.setText(weatherFragmentViewModel.selectedFavoriteAddressDto.getCountryName());
				});
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (weatherFragmentViewModel.weatherRestApiDownloader != null)
			weatherFragmentViewModel.weatherRestApiDownloader.cancel();
		if (weatherViewController != null)
			getLifecycle().removeObserver(weatherViewController);
		if (locationLifeCycleObserver != null)
			getLifecycle().removeObserver(locationLifeCycleObserver);
		binding = null;
		asyncBinding = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	private void setBackgroundWeatherView(String weather, String volume) {
		if (!PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext()).getBoolean(getString(R.string.pref_key_show_background_animation), false)) {
			weatherViewController.setWeatherView(PrecipType.CLEAR, null);
		} else {
			if (weather.equals(Flickr.Weather.rain.getText())) {
				weatherViewController.setWeatherView(PrecipType.RAIN, volume);
			} else if (weather.equals(Flickr.Weather.snow.getText())) {
				weatherViewController.setWeatherView(PrecipType.SNOW, volume);
			} else {
				weatherViewController.setWeatherView(PrecipType.CLEAR, volume);
			}
		}
	}

	private void loadImgOfCurrentConditions(FlickrRepository.FlickrRequestParameter flickrRequestParameter) {
		HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, requireActivity());

		binding.loadingAnimation.setVisibility(View.VISIBLE);
		binding.flickrImageUrl.setVisibility(View.GONE);

		flickrViewModel.loadImg(flickrRequestParameter);
	}

	private final FusedLocation.MyLocationCallback MY_LOCATION_CALLBACK = new FusedLocation.MyLocationCallback() {
		@Override
		public void onSuccessful(LocationResult locationResult) {
			//현재 위치 파악 성공
			//현재 위/경도 좌표를 최근 현재위치의 위/경도로 등록
			//날씨 데이터 요청
			final Location location = getBestLocation(locationResult);
			weatherFragmentViewModel.zoneId = ZoneId.of(PreferenceManager.getDefaultSharedPreferences(requireContext())
					.getString("zoneId", ""));
			onChangedCurrentLocation(location);
			locationCallbackInMainFragment.onSuccessful(locationResult);
		}

		@Override
		public void onFailed(Fail fail) {
			shimmer(false, false);
			locationCallbackInMainFragment.onFailed(fail);

			if (fail == Fail.DISABLED_GPS) {
				fusedLocation.onDisabledGps(requireActivity(), locationLifeCycleObserver, result -> {
					if (fusedLocation.isOnGps()) {
						binding.mainToolbar.gps.callOnClick();
					}
				});
			} else if (fail == Fail.DENIED_LOCATION_PERMISSIONS) {
				fusedLocation.onRejectPermissions(requireActivity(), locationLifeCycleObserver, result -> {
					if (fusedLocation.checkDefaultPermissions()) {
						binding.mainToolbar.gps.callOnClick();
					}
				}, result -> {
					//gps사용 권한
					//허가남 : 현재 위치 다시 파악
					//거부됨 : 작업 취소
					//계속 거부 체크됨 : 작업 취소
					if (!result.containsValue(false))
						binding.mainToolbar.gps.callOnClick();

				});

			} else if (fail == Fail.FAILED_FIND_LOCATION) {
				List<AlertFragment.BtnObj> btnObjList = new ArrayList<>();
				btnObjList.add(new AlertFragment.BtnObj(v -> {
					Bundle argument = new Bundle();
					argument.putBoolean("clickGps", true);

					iWeatherFragment.addWeatherFragment(weatherFragmentViewModel.locationType, weatherFragmentViewModel.selectedFavoriteAddressDto, argument);
				}, getString(R.string.again)));
				setFailFragment(btnObjList);
			}

		}
	};

	@Override
	public void requestCurrentLocation() {
		binding.mainToolbar.gps.callOnClick();
	}


	private void requestAddressOfLocation(Double latitude, Double longitude, boolean refresh) {
		Geocoding.nominatimReverseGeocoding(requireContext().getApplicationContext(), latitude, longitude, address -> {
			if (getActivity() != null) {
				weatherFragmentViewModel.addressName = address.displayName;
				weatherFragmentViewModel.mainWeatherProviderType = weatherFragmentViewModel.getMainWeatherSourceType(address.countryCode);
				weatherFragmentViewModel.countryCode = address.countryCode;

				final String addressStr = getString(R.string.current_location) + " : " + weatherFragmentViewModel.addressName;

				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();

				TimeZoneUtils.INSTANCE.getTimeZone(latitude, longitude, zoneId -> {
					editor.putString("zoneId", zoneId.getId()).apply();
					onResultCurrentLocation(addressStr, address, refresh);
				});


			}
		});
	}

	private void onResultCurrentLocation(String addressStr, Geocoding.AddressDto addressDto, boolean refresh) {
		requireActivity().runOnUiThread(() -> {
			binding.addressName.setText(addressStr);
			binding.countryName.setText(addressDto.country);
			weatherViewModel.setCurrentLocationAddressName(addressDto.displayName);

			if (refresh) {
				requestNewData();
			} else {
				//이미 데이터가 있으면 다시 그림
				final String key =
						weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString();
				try {
					weatherFragmentViewModel.mainWeatherProviderType =
							WeatherFragmentViewModel.FINAL_RESPONSE_MAP.get(key).requestMainWeatherProviderType;
					reDraw();
				} catch (Exception e) {
					requestNewData();
				}
			}

		});
	}

	public void reDraw() {
		//날씨 프래그먼트 다시 그림
		final String key = weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString();

		if (WeatherFragmentViewModel.FINAL_RESPONSE_MAP.containsKey(key)) {
			shimmer(true, false);

			executorService.submit(() -> {

				Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();
				weatherProviderTypeSet.add(weatherFragmentViewModel.mainWeatherProviderType);
				weatherProviderTypeSet.add(WeatherProviderType.AQICN);
				try {
					WeatherResponseObj responseObj = WeatherFragmentViewModel.FINAL_RESPONSE_MAP.get(key);
					if (responseObj != null) {
						setWeatherFragments(weatherProviderTypeSet, responseObj.weatherRestApiDownloader,
								weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude);
					} else {
						requestNewData();
					}
				} catch (Exception e) {
					requestNewData();
				}

			});

		}
	}

	public void onChangedCurrentLocation(Location currentLocation) {
		weatherFragmentViewModel.latitude = currentLocation.getLatitude();
		weatherFragmentViewModel.longitude = currentLocation.getLongitude();
		WeatherFragmentViewModel.FINAL_RESPONSE_MAP.remove(weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString());
		requestAddressOfLocation(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude, true);
	}


	public void requestNewData() {
		shimmer(true, false);
		weatherFragmentViewModel.requestNewData();
	}

	private void requestNewDataWithAnotherWeatherSource(WeatherProviderType newWeatherProviderType) {
		shimmer(true, false);
		weatherFragmentViewModel.requestNewDataWithAnotherWeatherSource(newWeatherProviderType);
	}

	private void processOnResult(ResponseResultObj responseResultObj) {
		Set<Map.Entry<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult>>> entrySet =
				responseResultObj.weatherRestApiDownloader.getResponseMap().entrySet();
		//메인 날씨 제공사의 데이터가 정상이면 메인 날씨 제공사의 프래그먼트들을 설정하고 값을 표시한다.
		//메인 날씨 제공사의 응답이 불량이면 재 시도, 취소 중 택1 다이얼로그 표시
		for (Map.Entry<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult>> entry : entrySet) {
			final WeatherProviderType weatherProviderType = entry.getKey();

			if (weatherProviderType == WeatherProviderType.AQICN) {
				continue;
			}

			for (WeatherRestApiDownloader.ResponseResult responseResult : entry.getValue().values()) {
				if (!responseResult.isSuccessful()) {
					if (weatherFragmentViewModel.containWeatherData(weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude)) {
						MainThreadWorker.runOnUiThread(() -> {
							shimmer(false, false);
							Toast.makeText(getContext(), R.string.update_failed, Toast.LENGTH_SHORT).show();
						});

					} else {
						//다시시도, 취소 중 택1
						List<AlertFragment.BtnObj> btnObjList = new ArrayList<>();

						Set<WeatherProviderType> otherTypes = weatherFragmentViewModel.getOtherWeatherSourceTypes(weatherProviderType,
								weatherFragmentViewModel.mainWeatherProviderType);

						final String[] failedDialogItems = new String[otherTypes.size()];
						final WeatherProviderType[] weatherProviderTypeArr = new WeatherProviderType[otherTypes.size()];
						int arrIndex = 0;

						if (otherTypes.contains(WeatherProviderType.KMA_WEB)) {
							weatherProviderTypeArr[arrIndex] = WeatherProviderType.KMA_WEB;
							failedDialogItems[arrIndex++] = getString(R.string.kma) + ", " + getString(
									R.string.rerequest_another_weather_datasource);
						}
						if (otherTypes.contains(WeatherProviderType.ACCU_WEATHER)) {
							weatherProviderTypeArr[arrIndex] = WeatherProviderType.ACCU_WEATHER;
							failedDialogItems[arrIndex++] = getString(R.string.accu_weather) + ", " + getString(
									R.string.rerequest_another_weather_datasource);
						}
						if (otherTypes.contains(WeatherProviderType.OWM_ONECALL)) {
							weatherProviderTypeArr[arrIndex] = WeatherProviderType.OWM_ONECALL;
							failedDialogItems[arrIndex++] = getString(R.string.owm) + ", " + getString(
									R.string.rerequest_another_weather_datasource);
						}
						if (otherTypes.contains(WeatherProviderType.MET_NORWAY)) {
							weatherProviderTypeArr[arrIndex] = WeatherProviderType.MET_NORWAY;
							failedDialogItems[arrIndex] = getString(R.string.met) + ", " + getString(
									R.string.rerequest_another_weather_datasource);
						}

						btnObjList.add(new AlertFragment.BtnObj(v -> iWeatherFragment.addWeatherFragment(weatherFragmentViewModel.locationType, weatherFragmentViewModel.selectedFavoriteAddressDto, null), getString(R.string.again)));

						int index = 0;
						for (WeatherProviderType anotherProvider : weatherProviderTypeArr) {
							btnObjList.add(new AlertFragment.BtnObj(v -> {
								Bundle argument = new Bundle();
								argument.putSerializable("anotherProvider", anotherProvider);

								iWeatherFragment.addWeatherFragment(weatherFragmentViewModel.locationType, weatherFragmentViewModel.selectedFavoriteAddressDto, argument);
							}, failedDialogItems[index]));
							index++;
						}

						MainThreadWorker.runOnUiThread(() -> {
							shimmer(false, false);
							setFailFragment(btnObjList);
						});
					}

					return;
				}
			}
		}

		//응답 성공 하면
		final WeatherResponseObj weatherResponseObj = new WeatherResponseObj(responseResultObj.weatherRestApiDownloader,
				responseResultObj.weatherProviderTypeSet, responseResultObj.mainWeatherProviderType);
		WeatherFragmentViewModel.FINAL_RESPONSE_MAP.put(weatherFragmentViewModel.latitude.toString() + weatherFragmentViewModel.longitude.toString(), weatherResponseObj);
		setWeatherFragments(responseResultObj.weatherProviderTypeSet, responseResultObj.weatherRestApiDownloader, weatherFragmentViewModel.latitude, weatherFragmentViewModel.longitude);
	}

	private void setFailFragment(List<AlertFragment.BtnObj> btnObjList) {
		FragmentManager fragmentManager = getParentFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		Bundle bundle = new Bundle();
		bundle.putInt(AlertFragment.Constant.DRAWABLE_ID.name(), R.drawable.error);
		bundle.putString(AlertFragment.Constant.MESSAGE.name(), getString(R.string.update_failed));

		AlertFragment alertFragment = new AlertFragment();

		alertFragment.setMenuOnClickListener(menuOnClickListener);
		alertFragment.setBtnObjList(btnObjList);
		alertFragment.setArguments(bundle);

		fragmentTransaction.replace(R.id.fragment_container, alertFragment,
				AlertFragment.class.getName()).setPrimaryNavigationFragment(alertFragment).commitNow();
	}


	private void setWeatherFragments(Set<WeatherProviderType> weatherProviderTypeSet, WeatherRestApiDownloader weatherRestApiDownloader,
	                                 Double latitude, Double longitude) {
		final WeatherDataDTO weatherDataDTO = weatherFragmentViewModel.createWeatherFragments(weatherProviderTypeSet,
				weatherRestApiDownloader, latitude,
				longitude);

		final Bundle defaultBundle = new Bundle();
		defaultBundle.putDouble(BundleKey.Latitude.name(), weatherDataDTO.getLatitude());
		defaultBundle.putDouble(BundleKey.Longitude.name(), weatherDataDTO.getLongitude());
		defaultBundle.putString(BundleKey.AddressName.name(), weatherDataDTO.getAddressName());
		defaultBundle.putString(BundleKey.CountryCode.name(), weatherDataDTO.getCountryCode());
		defaultBundle.putSerializable(BundleKey.WeatherProvider.name(), weatherDataDTO.getMainWeatherProviderType());
		defaultBundle.putSerializable(BundleKey.TimeZone.name(), weatherDataDTO.getZoneId());

		// simple current conditions ------------------------------------------------------------------------------------------------------
		final Bundle simpleCurrentConditionsBundle = new Bundle();
		simpleCurrentConditionsBundle.putAll(defaultBundle);

		final SimpleCurrentConditionsFragment simpleCurrentConditionsFragment = new SimpleCurrentConditionsFragment();
		simpleCurrentConditionsFragment.setCurrentConditionsDto(weatherDataDTO.getCurrentConditionsDto())
				.setAirQualityDto(weatherDataDTO.getAirQualityDto());

		// hourly forecasts ----------------------------------------------------------------------------------------------------------------
		final Bundle hourlyForecastBundle = new Bundle();
		hourlyForecastBundle.putAll(defaultBundle);

		final SimpleHourlyForecastFragment simpleHourlyForecastFragment = new SimpleHourlyForecastFragment();
		simpleHourlyForecastFragment.setHourlyForecastDtoList(weatherDataDTO.getHourlyForecastList());

		// daily forecasts ----------------------------------------------------------------------------------------------------------------
		final Bundle dailyForecastBundle = new Bundle();
		dailyForecastBundle.putAll(defaultBundle);

		final SimpleDailyForecastFragment simpleDailyForecastFragment = new SimpleDailyForecastFragment();
		simpleDailyForecastFragment.setDailyForecastDtoList(weatherDataDTO.getDailyForecastList());

		// detail current conditions ----------------------------------------------
		final Bundle detailCurrentConditionsBundle = new Bundle();
		detailCurrentConditionsBundle.putAll(defaultBundle);
		detailCurrentConditionsBundle.putSerializable(WeatherDataType.currentConditions.name(), weatherDataDTO.getCurrentConditionsDto());

		final DetailCurrentConditionsFragment detailCurrentConditionsFragment = new DetailCurrentConditionsFragment();
		detailCurrentConditionsFragment.setCurrentConditionsDto(weatherDataDTO.getCurrentConditionsDto());

		// air quality  ----------------------------------------------
		final Bundle airQualityBundle = new Bundle();
		airQualityBundle.putAll(defaultBundle);

		final SimpleAirQualityFragment simpleAirQualityFragment = new SimpleAirQualityFragment();
		simpleAirQualityFragment.setAirQualityDto(weatherDataDTO.getAirQualityDto())
				.setAqiCnGeolocalizedFeedResponse(weatherDataDTO.getAirQualityResponse());

		final SimpleRainViewerFragment rainViewerFragment = new SimpleRainViewerFragment();
		rainViewerFragment.setArguments(defaultBundle);

		assert rainViewerFragment.getArguments() != null;
		rainViewerFragment.getArguments().putBoolean("simpleMode", true);

		simpleAirQualityFragment.setArguments(airQualityBundle);

		final SunsetriseFragment sunSetRiseFragment = new SunsetriseFragment();
		sunSetRiseFragment.setArguments(defaultBundle);
		simpleHourlyForecastFragment.setArguments(hourlyForecastBundle);
		simpleDailyForecastFragment.setArguments(dailyForecastBundle);
		simpleCurrentConditionsFragment.setArguments(simpleCurrentConditionsBundle);
		detailCurrentConditionsFragment.setArguments(detailCurrentConditionsBundle);


		FlickrRepository.FlickrRequestParameter flickrRequestParameter = new FlickrRepository.FlickrRequestParameter(
				weatherDataDTO.getMainWeatherProviderType(), weatherDataDTO.getCurrentConditionsWeatherVal(), latitude, longitude,
				weatherDataDTO.getZoneId(), weatherDataDTO.getPrecipitationVolume(),
				ZonedDateTime.parse(weatherRestApiDownloader.getRequestDateTime().toString())
		);

		flickrViewModel.setLastParameter(flickrRequestParameter);
		weatherFragmentViewModel.iTextColor = simpleCurrentConditionsFragment;

		try {
			requireActivity().runOnUiThread(() -> {
				changeWeatherDataSourcePicker(weatherFragmentViewModel.countryCode);
				binding.updatedDatetime.setText(weatherRestApiDownloader.getRequestDateTime().format(weatherFragmentViewModel.dateTimeFormatter));

				FragmentManager fragmentManager = getChildFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

				fragmentTransaction
						.replace(binding.simpleCurrentConditions.getId(),
								simpleCurrentConditionsFragment, getString(R.string.tag_simple_current_conditions_fragment))
						.replace(binding.simpleHourlyForecast.getId(), simpleHourlyForecastFragment,
								getString(R.string.tag_simple_hourly_forecast_fragment))
						.replace(binding.simpleDailyForecast.getId(), simpleDailyForecastFragment, getString(R.string.tag_simple_daily_forecast_fragment))
						.replace(binding.detailCurrentConditions.getId(), detailCurrentConditionsFragment,
								getString(R.string.tag_detail_current_conditions_fragment))
						.replace(binding.simpleAirQuality.getId(), simpleAirQualityFragment, getString(R.string.tag_simple_air_quality_fragment))
						.replace(binding.sunSetRise.getId(), sunSetRiseFragment,
								getString(R.string.tag_sun_set_rise_fragment))
						.replace(binding.radar.getId(), rainViewerFragment,
								SimpleRainViewerFragment.class.getName())
						.commit();
			});

		} catch (Exception e) {

		}


	}

	private void changeWeatherDataSourcePicker(String countryCode) {
		String provide = getString(R.string.provide) + " : ";

		switch (weatherFragmentViewModel.mainWeatherProviderType) {
			case KMA_WEB:
			case KMA_API:
				binding.weatherDataSourceName.setText(String.format("%s%s", provide, getString(R.string.kma)));
				binding.weatherDataSourceIcon.setImageResource(R.drawable.kmaicon);
				break;
			case ACCU_WEATHER:
				binding.weatherDataSourceName.setText(String.format("%s%s", provide, getString(R.string.accu_weather)));
				binding.weatherDataSourceIcon.setImageResource(R.drawable.accuicon);
				break;
			case OWM_ONECALL:
			case OWM_INDIVIDUAL:
				binding.weatherDataSourceName.setText(String.format("%s%s", provide, getString(R.string.owm)));
				binding.weatherDataSourceIcon.setImageResource(R.drawable.owmicon);
				break;
			case MET_NORWAY:
				binding.weatherDataSourceName.setText(String.format("%s%s", provide, getString(R.string.met)));
				binding.weatherDataSourceIcon.setImageResource(R.drawable.metlogo);
				break;
		}

		binding.weatherDataSourceLayout.setOnClickListener(view -> {
			CharSequence[] items = new CharSequence[countryCode != null && countryCode.equals("KR") ? 3 : 2];
			int checkedItemIdx = 0;

			if (countryCode != null && countryCode.equals("KR")) {
				items[0] = getString(R.string.kma);
				items[1] = getString(R.string.owm);
				items[2] = getString(R.string.met);

				if (weatherFragmentViewModel.mainWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
					checkedItemIdx = 1;
				} else if (weatherFragmentViewModel.mainWeatherProviderType == WeatherProviderType.MET_NORWAY) {
					checkedItemIdx = 2;
				}
			} else {
				items[0] = getString(R.string.owm);
				items[1] = getString(R.string.met);

				if (weatherFragmentViewModel.mainWeatherProviderType != WeatherProviderType.OWM_ONECALL) {
					checkedItemIdx = 1;
				}
			}
			final int finalCheckedItemIdx = checkedItemIdx;

			new MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.title_pick_weather_data_source).setSingleChoiceItems(items,
					finalCheckedItemIdx, (dialogInterface, index) -> {
						WeatherProviderType newWeatherProviderType;

						if (finalCheckedItemIdx != index) {
							if (items[index].equals(getString(R.string.kma))) {
								newWeatherProviderType = WeatherProviderType.KMA_WEB;
							} else if (items[index].equals(getString(R.string.met))) {
								newWeatherProviderType = WeatherProviderType.MET_NORWAY;
							} else {
								newWeatherProviderType = WeatherProviderType.OWM_ONECALL;
							}
							requestNewDataWithAnotherWeatherSource(newWeatherProviderType);
						}
						dialogInterface.dismiss();
					}).create().show();
		});
	}


	private void onChangedStateBackgroundImg(boolean isShow) {
		HeaderbarStyle.setStyle(isShow ? HeaderbarStyle.Style.White : HeaderbarStyle.Style.Black, requireActivity());
		final int color = isShow ? Color.WHITE : Color.BLACK;

		binding.weatherDataSourceName.setTextColor(color);
		binding.updatedDatetimeLabel.setTextColor(color);
		binding.updatedDatetime.setTextColor(color);
		binding.countryName.setTextColor(color);
		binding.addressName.setTextColor(color);
		TextViewCompat.setCompoundDrawableTintList(binding.weatherDataSourceName, ColorStateList.valueOf(color));

		try {
			weatherFragmentViewModel.iTextColor.changeColor(color);
		} catch (Exception e) {

		}
	}

	private void shimmer(boolean showShimmer, boolean noChangeHeader) {
		if (showShimmer) {
			if (!noChangeHeader)
				HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, requireActivity());
			binding.rootSubLayout.setVisibility(View.GONE);
			binding.shimmer.startShimmer();
			binding.shimmer.setVisibility(View.VISIBLE);
		} else {
			if (!noChangeHeader)
				HeaderbarStyle.setStyle(HeaderbarStyle.Style.White, requireActivity());
			binding.shimmer.stopShimmer();
			binding.shimmer.setVisibility(View.GONE);
			binding.rootSubLayout.setVisibility(View.VISIBLE);
		}
	}


	public static final class ResponseResultObj implements Serializable {
		public WeatherRestApiDownloader weatherRestApiDownloader;
		public Set<WeatherProviderType> weatherProviderTypeSet;
		public WeatherProviderType mainWeatherProviderType;
		public ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources;

		public ResponseResultObj(Set<WeatherProviderType> weatherProviderTypeSet,
		                         ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources, WeatherProviderType mainWeatherProviderType) {
			this.weatherProviderTypeSet = weatherProviderTypeSet;
			this.requestWeatherSources = requestWeatherSources;
			this.mainWeatherProviderType = mainWeatherProviderType;
		}
	}

	public static final class WeatherResponseObj implements Serializable {
		public WeatherRestApiDownloader weatherRestApiDownloader;
		public Set<WeatherProviderType> requestWeatherProviderTypeSet;
		public WeatherProviderType requestMainWeatherProviderType;
		public LocalDateTime dataDownloadedDateTime;

		public WeatherResponseObj(WeatherRestApiDownloader weatherRestApiDownloader, Set<WeatherProviderType> requestWeatherProviderTypeSet, WeatherProviderType requestMainWeatherProviderType) {
			this.weatherRestApiDownloader = weatherRestApiDownloader;
			this.requestWeatherProviderTypeSet = requestWeatherProviderTypeSet;
			this.requestMainWeatherProviderType = requestMainWeatherProviderType;

			dataDownloadedDateTime = LocalDateTime.now();
		}
	}

	public interface IWeatherFragment {
		void addWeatherFragment(LocationType locationType, @Nullable FavoriteAddressDto favoriteAddressDto, Bundle arguments);
	}

	public interface ITextColor {
		void changeColor(int color);
	}

	public interface OnAsyncLoadCallback {
		void onFinished(Fragment fragment);
	}

	public interface OnResumeFragment {
		void onResumeWithAsync(Fragment fragment);
	}
}