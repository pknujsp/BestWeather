package com.lifedawn.bestweather.weathers;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import android.widget.RelativeLayout;
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
import com.google.android.gms.ads.nativead.NativeAd;
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
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestMet;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.Flickr;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.commons.views.HeaderbarStyle;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.flickr.FlickrImgObj;
import com.lifedawn.bestweather.flickr.FlickrLoader;
import com.lifedawn.bestweather.main.IRefreshFavoriteLocationListOnSideNav;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.model.timezone.TimeZoneIdRepository;
import com.lifedawn.bestweather.rainviewer.view.RainViewerFragment;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.onecall.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.AccuCurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.dailyforecasts.AccuDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.hourlyforecasts.AccuHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaCurrentConditions;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaDailyForecast;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaHourlyForecast;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.LocationForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.currentweather.OwmCurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.dailyforecast.OwmDailyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.hourlyforecast.OwmHourlyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.timezone.TimeZoneUtils;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.MetNorwayResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.detailfragment.currentconditions.DetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.SimpleAirQualityFragment;
import com.lifedawn.bestweather.weathers.simplefragment.currentconditions.SimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.dailyforecast.SimpleDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.hourlyforecast.SimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.sunsetrise.SunsetriseFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class WeatherFragment extends Fragment implements WeatherViewModel.ILoadImgOfCurrentConditions, IGps {
	private ExecutorService executorService = MyApplication.getExecutorService();
	private DateTimeFormatter dateTimeFormatter;

	private FragmentWeatherBinding binding;
	private FavoriteAddressDto selectedFavoriteAddressDto;
	private LocationType locationType;
	private WeatherViewModel weatherViewModel;
	private OnResultFragmentListener onResultFragmentListener;
	private View.OnClickListener menuOnClickListener;
	private FusedLocation fusedLocation;
	private NetworkStatus networkStatus;
	private FusedLocation.MyLocationCallback locationCallbackInMainFragment;
	private WeatherViewController weatherViewController;

	private WeatherProviderType mainWeatherProviderType;
	private Double latitude;
	private Double longitude;
	private String countryCode;
	private String addressName;
	private SharedPreferences sharedPreferences;
	private ZoneId zoneId;
	private boolean load;

	private WeatherRestApiDownloader weatherRestApiDownloader;
	private IRefreshFavoriteLocationListOnSideNav iRefreshFavoriteLocationListOnSideNav;
	private LocationLifeCycleObserver locationLifeCycleObserver;
	private Bundle arguments;
	private final IWeatherFragment iWeatherFragment;

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

	public WeatherFragment setOnResultFragmentListener(OnResultFragmentListener onResultFragmentListener) {
		this.onResultFragmentListener = onResultFragmentListener;
		return this;
	}

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
		                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);

			if (f instanceof AlertFragment) {
				onHiddenChanged(true);
				binding.scrollView.setVisibility(View.GONE);
			}
		}

		@Override
		public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
			super.onFragmentViewCreated(fm, f, v, savedInstanceState);

		}

		@Override
		public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentStarted(fm, f);
		}

		@Override
		public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentResumed(fm, f);

		}

		@Override
		public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentStopped(fm, f);
		}

		@Override
		public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof AlertFragment) {
				onHiddenChanged(false);
				binding.scrollView.setVisibility(View.VISIBLE);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);

		HeaderbarStyle.setStyle(HeaderbarStyle.Style.White, getActivity());

		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);

		networkStatus = NetworkStatus.getInstance(getContext());
		fusedLocation = FusedLocation.getInstance(getContext());

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		weatherViewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
		weatherViewModel.setiLoadImgOfCurrentConditions(this);
		locationCallbackInMainFragment = weatherViewModel.getLocationCallback();

		arguments = savedInstanceState != null ? savedInstanceState : getArguments();
		load = arguments.getBoolean("load", false);
	}

	/**
	 * hidden is true이면 black, else white
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

		if (hidden) {
			// 상단바 블랙으로
			HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, getActivity());
		} else {
			// 상단바 하양으로
			HeaderbarStyle.setStyle(HeaderbarStyle.Style.White, getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentWeatherBinding.inflate(inflater);
		return binding.getRoot();
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.currentConditionsImg.setColorFilter(ContextCompat.getColor(getContext(), R.color.black_alpha_30), PorterDuff.Mode.DARKEN);

		binding.scrollView.setVisibility(View.GONE);
		binding.flickrImageUrl.setVisibility(View.GONE);
		binding.loadingAnimation.setVisibility(View.GONE);

		final int statusBarHeight = MyApplication.getStatusBarHeight();

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.mainToolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = statusBarHeight;
		binding.mainToolbar.getRoot().setLayoutParams(layoutParams);

		int topMargin =
				(int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics())
						+ getResources().getDimension(R.dimen.toolbarHeight) + statusBarHeight);
		RelativeLayout.LayoutParams headerLayoutParams = (RelativeLayout.LayoutParams) binding.headerLayout.getLayoutParams();
		headerLayoutParams.topMargin = topMargin;
		binding.headerLayout.setLayoutParams(headerLayoutParams);

		weatherViewController = new WeatherViewController(binding.rootLayout);
		weatherViewController.setWeatherView(PrecipType.CLEAR, null);

		binding.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener);
		binding.mainToolbar.gps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (networkStatus.networkAvailable()) {
					ProgressDialog.show(getActivity(), getString(R.string.msg_finding_current_location), null);
					fusedLocation.findCurrentLocation(MY_LOCATION_CALLBACK, false);
				} else {
					Toast.makeText(getContext(), R.string.disconnected_network, Toast.LENGTH_SHORT).show();
				}
			}
		});

		binding.mainToolbar.find.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (networkStatus.networkAvailable()) {
					final MapFragment mapFragment = new MapFragment();

					final Bundle arguments = new Bundle();
					arguments.putString(BundleKey.RequestFragment.name(), WeatherFragment.class.getName());
					mapFragment.setArguments(arguments);

					mapFragment.setOnResultFavoriteListener(new MapFragment.OnResultFavoriteListener() {
						@Override
						public void onAddedNewAddress(FavoriteAddressDto newFavoriteAddressDto, List<FavoriteAddressDto> favoriteAddressDtoList, boolean removed) {
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
							MapFragment.class.getName()).commitAllowingStateLoss();
				}
			}
		});

		binding.mainToolbar.refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (networkStatus.networkAvailable()) {
					requestNewData();
				} else {
					Toast.makeText(getContext(), R.string.disconnected_network, Toast.LENGTH_SHORT).show();
				}
			}
		});

		binding.flickrImageUrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (binding.flickrImageUrl.getTag() != null) {
					String url = (String) binding.flickrImageUrl.getTag();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					startActivity(intent);
				}

			}
		});

		AdRequest adRequest = new AdRequest.Builder().build();
		AdLoader adLoader = new AdLoader.Builder(requireActivity(), getString(R.string.NATIVE_ADVANCE_unitId))
				.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
					@Override
					public void onNativeAdLoaded(NativeAd nativeAd) {
						NativeTemplateStyle styles = new
								NativeTemplateStyle.Builder().withMainBackgroundColor(new ColorDrawable(Color.WHITE)).build();
						TemplateView template = binding.adViewBottom;
						template.setStyles(styles);
						template.setNativeAd(nativeAd);

						if (nativeAd.isCustomMuteThisAdEnabled()) {

						}
					}
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

		//LocationType locationType, @Nullable FavoriteAddressDto favoriteAddressDto
		final LocationType locationType = (LocationType) arguments.getSerializable("LocationType");
		final FavoriteAddressDto favoriteAddressDto = arguments.containsKey("FavoriteAddressDto") ?
				(FavoriteAddressDto) arguments.getSerializable("FavoriteAddressDto") : null;

		if (load) {
			load = false;
			arguments.remove("load");
			setArguments(arguments);
			load(locationType, favoriteAddressDto);
		}
	}


	private void load(LocationType locationType, @Nullable FavoriteAddressDto favoriteAddressDto) {
		binding.mainToolbar.gps.setVisibility(locationType == LocationType.CurrentLocation ? View.VISIBLE : View.GONE);
		binding.mainToolbar.find.setVisibility(locationType == LocationType.CurrentLocation ? View.GONE : View.VISIBLE);

		final Bundle bundle = new Bundle();
		bundle.putSerializable(BundleKey.SelectedAddressDto.name(), favoriteAddressDto);
		bundle.putSerializable(BundleKey.IGps.name(), (IGps) this);
		bundle.putString(BundleKey.LocationType.name(), locationType.name());
		bundle.putString(BundleKey.RequestFragment.name(), WeatherFragment.class.getName());

		locationType = LocationType.valueOf(bundle.getString(BundleKey.LocationType.name()));
		this.locationType = locationType;

		final boolean clickGps = arguments.getBoolean("clickGps", false);


		if (locationType == LocationType.CurrentLocation) {
			sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1).putString(
					getString(R.string.pref_key_last_selected_location_type), locationType.name()).commit();

			LocationResult locationResult = fusedLocation.getLastCurrentLocation();
			latitude = locationResult.getLocations().get(0).getLatitude();
			longitude = locationResult.getLocations().get(0).getLongitude();

			if (latitude == 0.0 && longitude == 0.0) {
				//최근에 현재위치로 잡힌 위치가 없으므로 현재 위치 요청
				requestCurrentLocation();
			} else if (clickGps) {
				binding.mainToolbar.gps.callOnClick();
			} else {
				zoneId = ZoneId.of(PreferenceManager.getDefaultSharedPreferences(getContext())
						.getString("zoneId", ""));
				//위/경도에 해당하는 지역명을 불러오고, 날씨 데이터 다운로드
				//이미 존재하는 날씨 데이터면 다운로드X
				boolean refresh = !containWeatherData(latitude, longitude);

				if (isOldDownloadedData(latitude, longitude)) {
					removeOldDownloadedData(latitude, longitude);
					refresh = true;
				}

				requestAddressOfLocation(latitude, longitude, refresh);
			}
		} else {
			selectedFavoriteAddressDto = (FavoriteAddressDto) bundle.getSerializable(
					BundleKey.SelectedAddressDto.name());

			sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
					selectedFavoriteAddressDto.getId()).putString(getString(R.string.pref_key_last_selected_location_type),
					locationType.name()).commit();

			mainWeatherProviderType = getMainWeatherSourceType(selectedFavoriteAddressDto.getCountryCode());
			countryCode = selectedFavoriteAddressDto.getCountryCode();
			addressName = selectedFavoriteAddressDto.getDisplayName();
			latitude = Double.parseDouble(selectedFavoriteAddressDto.getLatitude());
			longitude = Double.parseDouble(selectedFavoriteAddressDto.getLongitude());
			zoneId = ZoneId.of(selectedFavoriteAddressDto.getZoneId());

			binding.addressName.setText(addressName);

			if (containWeatherData(latitude, longitude)) {
				if (isOldDownloadedData(latitude, longitude)) {
					removeOldDownloadedData(latitude, longitude);
					requestNewData();
				} else {
					//기존 데이터 표시
					mainWeatherProviderType =
							weatherViewModel.FINAL_RESPONSE_MAP.get(latitude.toString() + longitude.toString()).requestMainWeatherProviderType;
					reDraw();
				}
			} else {
				requestNewData();
			}
		}

	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(arguments);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (weatherRestApiDownloader != null) {
			weatherRestApiDownloader.cancel();
		}

		getLifecycle().removeObserver(locationLifeCycleObserver);
		getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}


	private void setFlickrImgInfo(FlickrImgObj flickrImgInfo) {
		final String text = flickrImgInfo.getPhoto().getOwner() + "-" + flickrImgInfo.getPhoto().getTitle();
		binding.flickrImageUrl.setText(TextUtil.getUnderLineColorText(text, text,
				ContextCompat.getColor(getContext(), R.color.white)));
		binding.flickrImageUrl.setTag(flickrImgInfo.getRealFlickrUrl());
		binding.flickrImageUrl.setVisibility(View.VISIBLE);
		binding.loadingAnimation.setVisibility(View.GONE);

		setBackgroundWeatherView(flickrImgInfo.getWeather(), flickrImgInfo.getVolume());
	}

	private void setBackgroundWeatherView(String weather, String volume) {
		if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_key_show_background_animation), false)) {
			weatherViewController.setWeatherView(PrecipType.CLEAR, null);
			return;
		}

		if (weather.equals(Flickr.Weather.rain.getText())) {
			weatherViewController.setWeatherView(PrecipType.RAIN, volume);
		} else if (weather.equals(Flickr.Weather.snow.getText())) {
			weatherViewController.setWeatherView(PrecipType.SNOW, volume);
		} else {
			weatherViewController.setWeatherView(PrecipType.CLEAR, volume);
		}
	}

	@Override
	public void loadImgOfCurrentConditions(WeatherProviderType weatherProviderType, String val, Double latitude, Double longitude,
	                                       ZoneId zoneId, String volume) {
		MainThreadWorker.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				binding.loadingAnimation.setVisibility(View.VISIBLE);
				binding.flickrImageUrl.setVisibility(View.GONE);
			}
		});

		FlickrLoader.loadImg(getContext(), weatherProviderType, val, latitude, longitude, zoneId, volume, new FlickrLoader.GlideImgCallback() {
					@Override
					public void onLoadedImg(FlickrImgObj flickrImgObj, boolean successful) {
						if (getActivity() != null && isAdded()) {
							MainThreadWorker.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (successful) {
										Glide.with(getContext()).load(flickrImgObj.getImg()).diskCacheStrategy(DiskCacheStrategy.ALL).transition(
												DrawableTransitionOptions.withCrossFade(300)).into(binding.currentConditionsImg);
										setFlickrImgInfo(flickrImgObj);
									} else {
										Glide.with(getContext()).clear(binding.currentConditionsImg);
										binding.loadingAnimation.setVisibility(View.GONE);
										binding.flickrImageUrl.setVisibility(View.VISIBLE);
										final String text = getString(R.string.error);
										binding.flickrImageUrl.setText(TextUtil.getUnderLineColorText(text, text,
												ContextCompat.getColor(getContext(), R.color.white)));

										if (flickrImgObj != null) {
											setBackgroundWeatherView(flickrImgObj.getWeather(), flickrImgObj.getVolume());
										}
									}

								}
							});
						}
					}
				},
				ZonedDateTime.parse(weatherViewModel.FINAL_RESPONSE_MAP.get(latitude.toString() + longitude.toString()).weatherRestApiDownloader.getRequestDateTime().toString()));
	}

	private final FusedLocation.MyLocationCallback MY_LOCATION_CALLBACK = new FusedLocation.MyLocationCallback() {
		@Override
		public void onSuccessful(LocationResult locationResult) {
			//현재 위치 파악 성공
			//현재 위/경도 좌표를 최근 현재위치의 위/경도로 등록
			//날씨 데이터 요청
			final Location location = getBestLocation(locationResult);
			zoneId = ZoneId.of(PreferenceManager.getDefaultSharedPreferences(getContext())
					.getString("zoneId", ""));
			onChangedCurrentLocation(location);
			locationCallbackInMainFragment.onSuccessful(locationResult);
		}

		@Override
		public void onFailed(Fail fail) {
			locationCallbackInMainFragment.onFailed(fail);
			ProgressDialog.clearDialogs();

			if (fail == Fail.DISABLED_GPS) {
				fusedLocation.onDisabledGps(requireActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (fusedLocation.isOnGps()) {
							binding.mainToolbar.gps.callOnClick();
						}
					}
				});
			} else if (fail == Fail.DENIED_LOCATION_PERMISSIONS) {
				fusedLocation.onRejectPermissions(requireActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (fusedLocation.checkDefaultPermissions()) {
							binding.mainToolbar.gps.callOnClick();
						}
					}
				}, new ActivityResultCallback<Map<String, Boolean>>() {
					@Override
					public void onActivityResult(Map<String, Boolean> result) {
						//gps사용 권한
						//허가남 : 현재 위치 다시 파악
						//거부됨 : 작업 취소
						//계속 거부 체크됨 : 작업 취소
						if (!result.containsValue(false)) {
							binding.mainToolbar.gps.callOnClick();
						} else {

						}
					}
				});

			} else if (fail == Fail.FAILED_FIND_LOCATION) {
				List<AlertFragment.BtnObj> btnObjList = new ArrayList<>();
				btnObjList.add(new AlertFragment.BtnObj(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Bundle argument = new Bundle();
						argument.putBoolean("clickGps", true);

						iWeatherFragment.addWeatherFragment(locationType, selectedFavoriteAddressDto, argument);
					}
				}, getString(R.string.again)));
				setFailFragment(btnObjList);
			}

		}
	};

	@Override
	public void requestCurrentLocation() {
		binding.mainToolbar.gps.callOnClick();
	}

	private boolean containWeatherData(Double latitude, Double longitude) {
		return weatherViewModel.FINAL_RESPONSE_MAP.containsKey(latitude.toString() + longitude.toString());
	}

	private void removeOldDownloadedData(Double latitude, Double longitude) {
		weatherViewModel.FINAL_RESPONSE_MAP.remove(latitude.toString() + longitude.toString());
	}

	private boolean isOldDownloadedData(Double latitude, Double longitude) {
		if (!weatherViewModel.FINAL_RESPONSE_MAP.containsKey(latitude.toString() + longitude.toString())) {
			return false;
		}

		long dataDownloadedMinutes = TimeUnit.SECONDS.toMinutes(
				weatherViewModel.FINAL_RESPONSE_MAP.get(latitude.toString() + longitude).dataDownloadedDateTime.getSecond());
		long now = TimeUnit.SECONDS.toMinutes(LocalDateTime.now().getSecond());

		if (now - dataDownloadedMinutes > 120) {
			return true;
		} else {
			return false;
		}
	}


	private void requestAddressOfLocation(Double latitude, Double longitude, boolean refresh) {
		binding.scrollView.setVisibility(View.GONE);

		Geocoding.nominatimGeocoding(getContext(), latitude, longitude, new Geocoding.GeocodingCallback() {
			@Override
			public void onGeocodingResult(Geocoding.AddressDto address) {
				if (getActivity() != null) {
					if (address == null) {
						//검색 결과가 없으면 주소 정보 미 표시하고 데이터 로드
						mainWeatherProviderType = getMainWeatherSourceType("");
						countryCode = "";
						addressName = getString(R.string.unknown_address);

						final String addressStr = getString(R.string.current_location) + " : " + addressName;
						onResultCurrentLocation(addressStr, addressName, refresh);
					} else {
						addressName = address.toName();
						mainWeatherProviderType = getMainWeatherSourceType(address.countryCode);
						countryCode = address.countryCode;

						final String addressStr = getString(R.string.current_location) + " : " + addressName;

						SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();

						TimeZoneUtils.Companion.getTimeZone(latitude, longitude, new TimeZoneUtils.TimeZoneCallback() {
							@Override
							public void onResult(@NonNull ZoneId zoneId) {
								editor.putString("zoneId", zoneId.getId()).commit();
								onResultCurrentLocation(addressStr, addressName, refresh);
							}
						});

					}


				}
			}
		});
	}

	private void onResultCurrentLocation(String addressStr, String addressName, boolean refresh) {
		requireActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				binding.addressName.setText(addressStr);
				weatherViewModel.setCurrentLocationAddressName(addressName);

				if (refresh) {
					requestNewData();
				} else {
					//이미 데이터가 있으면 다시 그림
					mainWeatherProviderType = weatherViewModel.FINAL_RESPONSE_MAP.get(
							latitude.toString() + longitude.toString()).requestMainWeatherProviderType;
					reDraw();
				}

			}
		});
	}

	private WeatherProviderType getMainWeatherSourceType(@NonNull String countryCode) {
		if (arguments.containsKey("anotherProvider")) {
			WeatherProviderType weatherProviderType = (WeatherProviderType) arguments.getSerializable("anotherProvider");
			arguments.remove("anotherProvider");
			return weatherProviderType;
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		WeatherProviderType mainWeatherProviderType = sharedPreferences.getBoolean(getString(R.string.pref_key_met), true) ?
				WeatherProviderType.MET_NORWAY : WeatherProviderType.OWM_ONECALL;

		if (countryCode != null && countryCode.equals("KR")) {
			boolean kmaIsTopPriority = sharedPreferences.getBoolean(getString(R.string.pref_key_kma_top_priority), true);
			if (kmaIsTopPriority) {
				mainWeatherProviderType = WeatherProviderType.KMA_WEB;
			}
		}

		return mainWeatherProviderType;
	}


	public void reDraw() {
		//날씨 프래그먼트 다시 그림
		if (weatherViewModel.FINAL_RESPONSE_MAP.containsKey(latitude.toString() + longitude.toString())) {
			ProgressDialog.show(requireActivity(), getString(R.string.refreshing_view), null);

			executorService.execute(new Runnable() {
				@Override
				public void run() {
					Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();
					weatherProviderTypeSet.add(mainWeatherProviderType);
					weatherProviderTypeSet.add(WeatherProviderType.AQICN);
					setWeatherFragments(weatherProviderTypeSet, weatherViewModel.FINAL_RESPONSE_MAP.get(latitude.toString() + longitude.toString()).weatherRestApiDownloader,
							latitude, longitude);
				}
			});

		}
	}

	public void onChangedCurrentLocation(Location currentLocation) {
		this.latitude = currentLocation.getLatitude();
		this.longitude = currentLocation.getLongitude();
		weatherViewModel.FINAL_RESPONSE_MAP.remove(latitude.toString() + longitude.toString());
		requestAddressOfLocation(latitude, longitude, true);
	}


	public void requestNewData() {
		binding.scrollView.setVisibility(View.GONE);
		ProgressDialog.show(requireActivity(), getString(R.string.msg_refreshing_weather_data), null);

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				//메인 날씨 제공사만 요청
				final Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();
				weatherProviderTypeSet.add(mainWeatherProviderType);
				weatherProviderTypeSet.add(WeatherProviderType.AQICN);

				ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
				setRequestWeatherSourceWithSourceTypes(weatherProviderTypeSet, requestWeatherSources);

				final ResponseResultObj responseResultObj = new ResponseResultObj(weatherProviderTypeSet, requestWeatherSources, mainWeatherProviderType);

				weatherRestApiDownloader = new WeatherRestApiDownloader() {
					@Override
					public void onResult() {
						responseResultObj.weatherRestApiDownloader = this;
						processOnResult(responseResultObj);
					}

					@Override
					public void onCanceled() {

					}
				};

				weatherRestApiDownloader.setZoneId(zoneId);
				MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, requestWeatherSources, weatherRestApiDownloader);
			}
		});
	}

	private void requestNewDataWithAnotherWeatherSource(WeatherProviderType newWeatherProviderType, WeatherProviderType lastWeatherProviderType) {
		binding.scrollView.setVisibility(View.GONE);
		ProgressDialog.show(requireActivity(), getString(R.string.msg_refreshing_weather_data), null);

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
				//메인 날씨 제공사만 요청
				Set<WeatherProviderType> newWeatherProviderTypeSet = new HashSet<>();
				newWeatherProviderTypeSet.add(newWeatherProviderType);
				newWeatherProviderTypeSet.add(WeatherProviderType.AQICN);

				setRequestWeatherSourceWithSourceTypes(newWeatherProviderTypeSet, requestWeatherSources);

				final ResponseResultObj responseResultObj = new ResponseResultObj(newWeatherProviderTypeSet, requestWeatherSources, newWeatherProviderType);
				weatherRestApiDownloader = new WeatherRestApiDownloader() {
					@Override
					public void onResult() {
						responseResultObj.weatherRestApiDownloader = this;
						processOnResult(responseResultObj);
					}

					@Override
					public void onCanceled() {

					}
				};
				weatherRestApiDownloader.setZoneId(zoneId);
				MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, requestWeatherSources, weatherRestApiDownloader);
			}
		});
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
					if (containWeatherData(latitude, longitude)) {
						MainThreadWorker.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								binding.scrollView.setVisibility(View.VISIBLE);
								Toast.makeText(getContext(), R.string.update_failed, Toast.LENGTH_SHORT).show();
							}
						});

					} else {
						//다시시도, 취소 중 택1
						List<AlertFragment.BtnObj> btnObjList = new ArrayList<>();

						Set<WeatherProviderType> otherTypes = getOtherWeatherSourceTypes(weatherProviderType,
								mainWeatherProviderType);

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
							failedDialogItems[arrIndex++] = getString(R.string.met) + ", " + getString(
									R.string.rerequest_another_weather_datasource);
						}

						btnObjList.add(new AlertFragment.BtnObj(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								iWeatherFragment.addWeatherFragment(locationType, selectedFavoriteAddressDto, null);
							}
						}, getString(R.string.again)));

						int index = 0;
						for (WeatherProviderType anotherProvider : weatherProviderTypeArr) {
							btnObjList.add(new AlertFragment.BtnObj(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Bundle argument = new Bundle();
									argument.putSerializable("anotherProvider", anotherProvider);

									iWeatherFragment.addWeatherFragment(locationType, selectedFavoriteAddressDto, argument);
								}
							}, failedDialogItems[index]));
							index++;
						}
						setFailFragment(btnObjList);
					}

					ProgressDialog.clearDialogs();
					return;
				}
			}
		}

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				//응답 성공 하면
				final WeatherResponseObj weatherResponseObj = new WeatherResponseObj(responseResultObj.weatherRestApiDownloader,
						responseResultObj.weatherProviderTypeSet, responseResultObj.mainWeatherProviderType);
				weatherViewModel.FINAL_RESPONSE_MAP.put(latitude.toString() + longitude.toString(), weatherResponseObj);
				setWeatherFragments(responseResultObj.weatherProviderTypeSet, responseResultObj.weatherRestApiDownloader, latitude, longitude);
			}
		});

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
				AlertFragment.class.getName()).commitAllowingStateLoss();
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
	private Set<WeatherProviderType> getOtherWeatherSourceTypes(WeatherProviderType requestWeatherProviderType,
	                                                            WeatherProviderType lastWeatherProviderType) {
		Set<WeatherProviderType> others = new HashSet<>();

		if (requestWeatherProviderType == WeatherProviderType.KMA_WEB) {

			if (lastWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
				others.add(WeatherProviderType.MET_NORWAY);
			} else if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				others.add(WeatherProviderType.OWM_ONECALL);
			} else {
				others.add(WeatherProviderType.OWM_ONECALL);
				others.add(WeatherProviderType.MET_NORWAY);
			}
		} else if (requestWeatherProviderType == WeatherProviderType.KMA_API) {

			if (lastWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
				others.add(WeatherProviderType.MET_NORWAY);
			} else if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				others.add(WeatherProviderType.OWM_ONECALL);
			} else {
				others.add(WeatherProviderType.OWM_ONECALL);
				others.add(WeatherProviderType.MET_NORWAY);
			}
		} else if (requestWeatherProviderType == WeatherProviderType.MET_NORWAY) {

			if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				if (countryCode != null && countryCode.equals("KR")) {
					others.add(WeatherProviderType.OWM_ONECALL);
					others.add(WeatherProviderType.KMA_WEB);
				} else {
					others.add(WeatherProviderType.OWM_ONECALL);
				}
			} else if (lastWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.KMA_WEB);
				}
			} else {
				others.add(WeatherProviderType.OWM_ONECALL);
			}
		} else if (requestWeatherProviderType == WeatherProviderType.OWM_ONECALL) {

			if (lastWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.MET_NORWAY);
					others.add(WeatherProviderType.KMA_WEB);
				} else {
					others.add(WeatherProviderType.MET_NORWAY);
				}
			} else if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.KMA_WEB);
				}
			} else {
				others.add(WeatherProviderType.MET_NORWAY);
			}
		} else if (requestWeatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {

			if (lastWeatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.MET_NORWAY);
					others.add(WeatherProviderType.KMA_WEB);
				} else {
					others.add(WeatherProviderType.MET_NORWAY);
				}
			} else if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.KMA_WEB);
				}
			} else {
				others.add(WeatherProviderType.MET_NORWAY);
			}
		}

		return others;
	}

	private void reRefreshBySameWeatherSource(ResponseResultObj responseResultObj) {
		ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);
		binding.scrollView.setVisibility(View.GONE);

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.reRequestWeatherDataBySameWeatherSourceIfFailed(getContext(), latitude, longitude, responseResultObj.requestWeatherSources,
						responseResultObj.weatherRestApiDownloader);
			}
		});

	}

	private void reRefreshByAnotherWeatherSource(ResponseResultObj responseResultObj) {
		binding.scrollView.setVisibility(View.GONE);
		ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				ArrayMap<WeatherProviderType, RequestWeatherSource> newRequestWeatherSources = new ArrayMap<>();
				setRequestWeatherSourceWithSourceTypes(responseResultObj.weatherProviderTypeSet, newRequestWeatherSources);
				responseResultObj.requestWeatherSources = newRequestWeatherSources;

				MainProcessing.reRequestWeatherDataByAnotherWeatherSourceIfFailed(getContext(), latitude, longitude,
						responseResultObj.requestWeatherSources, responseResultObj.weatherRestApiDownloader);
			}
		});

	}

	private void setRequestWeatherSourceWithSourceTypes(Set<WeatherProviderType> weatherProviderTypeSet,
	                                                    ArrayMap<WeatherProviderType, RequestWeatherSource> newRequestWeatherSources) {
		if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST).addRequestServiceType(
							RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).addRequestServiceType(
							RetrofitClient.ServiceType.KMA_VILAGE_FCST).addRequestServiceType(
							RetrofitClient.ServiceType.KMA_MID_LAND_FCST).addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_TA_FCST)
					.addRequestServiceType(RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST);
			newRequestWeatherSources.put(WeatherProviderType.KMA_WEB, requestKma);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).addRequestServiceType(
					RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
			newRequestWeatherSources.put(WeatherProviderType.KMA_WEB, requestKma);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
			RequestOwmOneCall requestOwmOneCall = new RequestOwmOneCall();
			requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
			Set<OneCallParameter.OneCallApis> excludes = new HashSet<>();
			excludes.add(OneCallParameter.OneCallApis.minutely);
			excludes.add(OneCallParameter.OneCallApis.alerts);
			requestOwmOneCall.setExcludeApis(excludes);

			newRequestWeatherSources.put(WeatherProviderType.OWM_ONECALL, requestOwmOneCall);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
			RequestMet requestMet = new RequestMet();
			requestMet.addRequestServiceType(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

			newRequestWeatherSources.put(WeatherProviderType.MET_NORWAY, requestMet);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
			RequestAccu requestAccu = new RequestAccu();
			requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).addRequestServiceType(
					RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST).addRequestServiceType(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);

			newRequestWeatherSources.put(WeatherProviderType.ACCU_WEATHER, requestAccu);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_INDIVIDUAL)) {
			RequestOwmIndividual requestOwmIndividual = new RequestOwmIndividual();
			requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS).addRequestServiceType(
					RetrofitClient.ServiceType.OWM_HOURLY_FORECAST).addRequestServiceType(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

			newRequestWeatherSources.put(WeatherProviderType.OWM_INDIVIDUAL, requestOwmIndividual);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.AQICN)) {
			RequestAqicn requestAqicn = new RequestAqicn();
			requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

			newRequestWeatherSources.put(WeatherProviderType.AQICN, requestAqicn);
		}

	}


	private void setWeatherFragments(Set<WeatherProviderType> weatherProviderTypeSet, WeatherRestApiDownloader weatherRestApiDownloader,
	                                 Double latitude, Double longitude) {
		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult>> responseMap = weatherRestApiDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult> arrayMap = null;

		CurrentConditionsDto currentConditionsDto = null;
		List<HourlyForecastDto> hourlyForecastDtoList = null;
		List<DailyForecastDto> dailyForecastDtoList = null;

		final SimpleCurrentConditionsFragment simpleCurrentConditionsFragment = new SimpleCurrentConditionsFragment();
		final SimpleHourlyForecastFragment simpleHourlyForecastFragment = new SimpleHourlyForecastFragment();
		final SimpleDailyForecastFragment simpleDailyForecastFragment = new SimpleDailyForecastFragment();
		final DetailCurrentConditionsFragment detailCurrentConditionsFragment = new DetailCurrentConditionsFragment();
		final SimpleAirQualityFragment simpleAirQualityFragment = new SimpleAirQualityFragment();
		final SunsetriseFragment sunSetRiseFragment = new SunsetriseFragment();

		String currentConditionsWeatherVal = null;

		if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_API)) {
			arrayMap = responseMap.get(WeatherProviderType.KMA_API);

			FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditionsByXML(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST).getResponseObj());
			FinalCurrentConditions yesterDayFinalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditionsByXML(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST).getResponseObj());

			List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastListByXML(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).getResponseObj(),
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST).getResponseObj());

			List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastListByXML(
					(MidLandFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_MID_LAND_FCST).getResponseObj(),
					(MidTaResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_MID_TA_FCST).getResponseObj(),
					Long.parseLong(weatherRestApiDownloader.get("tmFc")));

			finalDailyForecastList = KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecastList, finalHourlyForecastList);

			currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfXML(getContext(), finalCurrentConditions,
					finalHourlyForecastList.get(0), latitude, longitude);
			currentConditionsDto.setYesterdayTemp(ValueUnits.convertTemperature(yesterDayFinalCurrentConditions.getTemperature(),
					MyApplication.VALUE_UNIT_OBJ.getTempUnit()) + MyApplication.VALUE_UNIT_OBJ.getTempUnitText());

			hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfXML(getContext(),
					finalHourlyForecastList, latitude, longitude);

			dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfXML(finalDailyForecastList);

			String sky = finalHourlyForecastList.get(0).getSky();
			String pty = finalCurrentConditions.getPrecipitationType();

			currentConditionsWeatherVal = pty.equals("0") ? sky + "_sky" : pty + "_pty";
			mainWeatherProviderType = WeatherProviderType.KMA_API;

		} else if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			arrayMap = responseMap.get(WeatherProviderType.KMA_WEB);

			KmaCurrentConditions kmaCurrentConditions = (KmaCurrentConditions) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).getResponseObj();
			Object[] forecasts = (Object[]) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseObj();

			ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
			ArrayList<KmaDailyForecast> kmaDailyForecasts = (ArrayList<KmaDailyForecast>) forecasts[1];

			currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(getContext(),
					kmaCurrentConditions, kmaHourlyForecasts.get(0), latitude, longitude);

			hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(getContext(),
					kmaHourlyForecasts, latitude, longitude);

			dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts);

			String pty = kmaCurrentConditions.getPty();

			currentConditionsWeatherVal = pty.isEmpty() ? kmaHourlyForecasts.get(0).getWeatherDescription() : pty;
			mainWeatherProviderType = WeatherProviderType.KMA_WEB;

		} else if (weatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(WeatherProviderType.ACCU_WEATHER);

			AccuCurrentConditionsResponse accuCurrentConditionsResponse =
					(AccuCurrentConditionsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponseObj();

			AccuHourlyForecastsResponse accuHourlyForecastsResponse =
					(AccuHourlyForecastsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST).getResponseObj();

			AccuDailyForecastsResponse accuDailyForecastsResponse =
					(AccuDailyForecastsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST).getResponseObj();

			currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(getContext(), accuCurrentConditionsResponse.getItems().get(0)
			);

			hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(getContext(), accuHourlyForecastsResponse.getItems()
			);

			dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(getContext(),
					accuDailyForecastsResponse.getDailyForecasts());

			currentConditionsWeatherVal = accuCurrentConditionsResponse.getItems().get(0).getWeatherIcon();
			mainWeatherProviderType = WeatherProviderType.ACCU_WEATHER;

		} else if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
			arrayMap = responseMap.get(WeatherProviderType.OWM_ONECALL);

			OwmOneCallResponse owmOneCallResponse =
					(OwmOneCallResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponseObj();

			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoOneCall(getContext(), owmOneCallResponse, zoneId
			);

			hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListOneCall(getContext(), owmOneCallResponse, zoneId
			);

			dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListOneCall(getContext(), owmOneCallResponse, zoneId
			);

			currentConditionsWeatherVal = owmOneCallResponse.getCurrent().getWeather().get(0).getId();

			mainWeatherProviderType = WeatherProviderType.OWM_ONECALL;

		} else if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_INDIVIDUAL)) {
			arrayMap = responseMap.get(WeatherProviderType.OWM_INDIVIDUAL);

			OwmCurrentConditionsResponse owmCurrentConditionsResponse =
					(OwmCurrentConditionsResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS).getResponseObj();
			OwmHourlyForecastResponse owmHourlyForecastResponse =
					(OwmHourlyForecastResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST).getResponseObj();
			OwmDailyForecastResponse owmDailyForecastResponse =
					(OwmDailyForecastResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_DAILY_FORECAST).getResponseObj();

			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoIndividual(getContext(), owmCurrentConditionsResponse, zoneId
			);

			hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListIndividual(getContext(),
					owmHourlyForecastResponse, zoneId);

			dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListIndividual(getContext(),
					owmDailyForecastResponse, zoneId);

			currentConditionsWeatherVal = owmCurrentConditionsResponse.getWeather().get(0).getId();

			mainWeatherProviderType = WeatherProviderType.OWM_INDIVIDUAL;
		} else if (weatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
			arrayMap = responseMap.get(WeatherProviderType.MET_NORWAY);

			LocationForecastResponse locationForecastResponse =
					(LocationForecastResponse) arrayMap.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST).getResponseObj();

			currentConditionsDto = MetNorwayResponseProcessor.makeCurrentConditionsDto(getContext(), locationForecastResponse
					, zoneId);

			hourlyForecastDtoList = MetNorwayResponseProcessor.makeHourlyForecastDtoList(getContext(), locationForecastResponse, zoneId);

			dailyForecastDtoList = MetNorwayResponseProcessor.makeDailyForecastDtoList(getContext(), locationForecastResponse, zoneId);

			currentConditionsWeatherVal = locationForecastResponse.getProperties().getTimeSeries().get(0)
					.getData().getNext_1_hours().getSummary().getSymbolCode().replace("day", "").replace("night", "")
					.replace("_", "");

			mainWeatherProviderType = WeatherProviderType.MET_NORWAY;
		}

		WeatherRestApiDownloader.ResponseResult aqicnResponse = responseMap.get(WeatherProviderType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		AqiCnGeolocalizedFeedResponse airQualityResponse = null;

		if (aqicnResponse.isSuccessful()) {
			airQualityResponse = (AqiCnGeolocalizedFeedResponse) aqicnResponse.getResponseObj();
		}

		final AirQualityDto airQualityDto = AqicnResponseProcessor.makeAirQualityDto(airQualityResponse,
				ZonedDateTime.now(zoneId).getOffset());

		final Bundle defaultBundle = new Bundle();
		defaultBundle.putDouble(BundleKey.Latitude.name(), this.latitude);
		defaultBundle.putDouble(BundleKey.Longitude.name(), this.longitude);
		defaultBundle.putString(BundleKey.AddressName.name(), addressName);
		defaultBundle.putString(BundleKey.CountryCode.name(), countryCode);
		defaultBundle.putSerializable(BundleKey.WeatherProvider.name(), mainWeatherProviderType);
		defaultBundle.putSerializable(BundleKey.TimeZone.name(), zoneId);

		// simple current conditions ------------------------------------------------------------------------------------------------------
		final Bundle simpleCurrentConditionsBundle = new Bundle();
		simpleCurrentConditionsBundle.putAll(defaultBundle);

		simpleCurrentConditionsFragment.setCurrentConditionsDto(currentConditionsDto)
				.setAirQualityDto(airQualityDto);

		// hourly forecasts ----------------------------------------------------------------------------------------------------------------
		final Bundle hourlyForecastBundle = new Bundle();
		hourlyForecastBundle.putAll(defaultBundle);

		simpleHourlyForecastFragment.setHourlyForecastDtoList(hourlyForecastDtoList);

		// daily forecasts ----------------------------------------------------------------------------------------------------------------
		final Bundle dailyForecastBundle = new Bundle();
		dailyForecastBundle.putAll(defaultBundle);

		simpleDailyForecastFragment.setDailyForecastDtoList(dailyForecastDtoList);

		// detail current conditions ----------------------------------------------
		final Bundle detailCurrentConditionsBundle = new Bundle();
		detailCurrentConditionsBundle.putAll(defaultBundle);
		detailCurrentConditionsBundle.putSerializable(WeatherDataType.currentConditions.name(), currentConditionsDto);

		detailCurrentConditionsFragment.setCurrentConditionsDto(currentConditionsDto);

		// air quality  ----------------------------------------------
		final Bundle airQualityBundle = new Bundle();
		airQualityBundle.putAll(defaultBundle);

		simpleAirQualityFragment.setAirQualityDto(airQualityDto)
				.setAqiCnGeolocalizedFeedResponse(airQualityResponse);

		final RainViewerFragment rainViewerFragment = new RainViewerFragment();
		rainViewerFragment.setArguments(defaultBundle);

		simpleAirQualityFragment.setArguments(airQualityBundle);
		sunSetRiseFragment.setArguments(defaultBundle);
		simpleHourlyForecastFragment.setArguments(hourlyForecastBundle);
		simpleDailyForecastFragment.setArguments(dailyForecastBundle);
		simpleCurrentConditionsFragment.setArguments(simpleCurrentConditionsBundle);
		detailCurrentConditionsFragment.setArguments(detailCurrentConditionsBundle);

		final String finalCurrentConditionsWeatherVal = currentConditionsWeatherVal;
		final ZoneId finalZoneId = zoneId;

		String precipitationVolume = null;
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitationVolume = currentConditionsDto.getPrecipitationVolume();
		} else if (currentConditionsDto.isHasRainVolume()) {
			precipitationVolume = currentConditionsDto.getRainVolume();
		} else if (currentConditionsDto.isHasSnowVolume()) {
			precipitationVolume = currentConditionsDto.getSnowVolume();
		}
		final String finalPrecipitationVolume = precipitationVolume;

		if (getActivity() != null) {
			loadImgOfCurrentConditions(mainWeatherProviderType, finalCurrentConditionsWeatherVal, latitude, longitude,
					finalZoneId, finalPrecipitationVolume);

			MainThreadWorker.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					changeWeatherDataSourcePicker(countryCode);
					ZonedDateTime dateTime = weatherRestApiDownloader.getRequestDateTime();
					dateTimeFormatter = DateTimeFormatter.ofPattern(
							MyApplication.VALUE_UNIT_OBJ.getClockUnit() == ValueUnits.clock12 ? getString(R.string.datetime_pattern_clock12) :
									getString(R.string.datetime_pattern_clock24), Locale.getDefault());
					binding.updatedDatetime.setText(dateTime.format(dateTimeFormatter));
					binding.scrollView.setVisibility(View.VISIBLE);

					FragmentManager fragmentManager = getChildFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

					if (fragmentManager.findFragmentByTag(AlertFragment.class.getName()) != null) {
						fragmentTransaction.remove(fragmentManager.findFragmentByTag(AlertFragment.class.getName()));
					}

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
									RainViewerFragment.class.getName())
							.commitAllowingStateLoss();

					ProgressDialog.clearDialogs();
				}
			});

		}
	}

	private void changeWeatherDataSourcePicker(String countryCode) {
		String provide = getString(R.string.provide) + " : ";

		switch (mainWeatherProviderType) {
			case KMA_WEB:
			case KMA_API:
				binding.weatherDataSourceName.setText(new String(provide + getString(R.string.kma)));
				binding.weatherDataSourceIcon.setImageResource(R.drawable.kmaicon);
				break;
			case ACCU_WEATHER:
				binding.weatherDataSourceName.setText(new String(provide + getString(R.string.accu_weather)));
				binding.weatherDataSourceIcon.setImageResource(R.drawable.accuicon);
				break;
			case OWM_ONECALL:
			case OWM_INDIVIDUAL:
				binding.weatherDataSourceName.setText(new String(provide + getString(R.string.owm)));
				binding.weatherDataSourceIcon.setImageResource(R.drawable.owmicon);
				break;
			case MET_NORWAY:
				binding.weatherDataSourceName.setText(new String(provide + getString(R.string.met)));
				binding.weatherDataSourceIcon.setImageResource(R.drawable.metlogo);
				break;
		}

		binding.weatherDataSourceLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				CharSequence[] items = new CharSequence[countryCode != null && countryCode.equals("KR") ? 3 : 2];
				int checkedItemIdx = 0;

				if (countryCode != null && countryCode.equals("KR")) {
					items[0] = getString(R.string.kma);
					items[1] = getString(R.string.owm);
					items[2] = getString(R.string.met);

					if (mainWeatherProviderType == WeatherProviderType.KMA_WEB) {
						checkedItemIdx = 0;
					} else if (mainWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
						checkedItemIdx = 1;
					} else {
						checkedItemIdx = 2;
					}
				} else {
					items[0] = getString(R.string.owm);
					items[1] = getString(R.string.met);

					if (mainWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
						checkedItemIdx = 0;
					} else {
						checkedItemIdx = 1;
					}
				}
				final int finalCheckedItemIdx = checkedItemIdx;

				new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.title_pick_weather_data_source).setSingleChoiceItems(items,
						checkedItemIdx, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int index) {
								WeatherProviderType lastWeatherProviderType = mainWeatherProviderType;
								WeatherProviderType newWeatherProviderType;

								if (finalCheckedItemIdx != index) {
									if (items[index].equals(getString(R.string.kma))) {
										newWeatherProviderType = WeatherProviderType.KMA_WEB;
									} else if (items[index].equals(getString(R.string.met))) {
										newWeatherProviderType = WeatherProviderType.MET_NORWAY;
									} else {
										newWeatherProviderType = WeatherProviderType.OWM_ONECALL;
									}
									requestNewDataWithAnotherWeatherSource(newWeatherProviderType, lastWeatherProviderType);
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


	private final static class ResponseResultObj implements Serializable {
		WeatherRestApiDownloader weatherRestApiDownloader;
		Set<WeatherProviderType> weatherProviderTypeSet;
		WeatherProviderType mainWeatherProviderType;
		ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources;

		public ResponseResultObj(Set<WeatherProviderType> weatherProviderTypeSet,
		                         ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources, WeatherProviderType mainWeatherProviderType) {
			this.weatherProviderTypeSet = weatherProviderTypeSet;
			this.requestWeatherSources = requestWeatherSources;
			this.mainWeatherProviderType = mainWeatherProviderType;
		}
	}

	public static class WeatherResponseObj implements Serializable {
		final WeatherRestApiDownloader weatherRestApiDownloader;
		final Set<WeatherProviderType> requestWeatherProviderTypeSet;
		final WeatherProviderType requestMainWeatherProviderType;
		LocalDateTime dataDownloadedDateTime;

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
}