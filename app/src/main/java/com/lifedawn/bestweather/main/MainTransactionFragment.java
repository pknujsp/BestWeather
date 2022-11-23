package com.lifedawn.bestweather.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.CloseWindow;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentMainBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.notification.NotificationFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.settings.fragments.SettingsMainFragment;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.interfaces.ILoadWeatherData;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainTransactionFragment extends Fragment implements IRefreshFavoriteLocationListOnSideNav, WeatherFragment.IWeatherFragment {
	private final int favTypeTagInFavLocItemView = R.id.locationTypeTagInFavLocItemViewInSideNav;
	private final int favDtoTagInFavLocItemView = R.id.favoriteLocationDtoTagInFavLocItemViewInSideNav;

	private FragmentMainBinding binding;
	private WeatherViewModel weatherViewModel;
	private SharedPreferences sharedPreferences;
	private List<FavoriteAddressDto> favoriteAddressDtoList = new ArrayList<>();
	private String currentAddressName;
	private InitViewModel initViewModel;
	private boolean init = true;

	private final CloseWindow closeWindow = new CloseWindow(new CloseWindow.OnBackKeyDoubleClickedListener() {
		@Override
		public void onDoubleClicked() {

		}
	});

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (getChildFragmentManager().getBackStackEntryCount() > 0) {
				getChildFragmentManager().popBackStackImmediate();
			} else {
				if (binding.drawerLayout.isDrawerOpen(binding.sideNavigation))
					binding.drawerLayout.closeDrawer(binding.sideNavigation);
				else
					onBeforeCloseApp();
			}
		}

	};

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		private boolean originalUsingCurrentLocation = false;

		@Override
		public void onFragmentAttached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
		                               @NonNull @NotNull Context context) {
			super.onFragmentAttached(fm, f, context);

			if (f instanceof SettingsMainFragment) {
				originalUsingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), false);
			}
		}

		@Override
		public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentStarted(fm, f);
			if (!initViewModel.ready)
				initViewModel.ready = true;
		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof SettingsMainFragment) {
				final boolean newUsingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location),
						false);
				final LocationType lastSelectedLocationType = LocationType.valueOf(
						sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type),
								LocationType.CurrentLocation.name()));

				if (originalUsingCurrentLocation != newUsingCurrentLocation) {
					setCurrentLocationState(newUsingCurrentLocation);

					if (newUsingCurrentLocation) {
						//날씨 프래그먼트 다시 그림
						WeatherFragment weatherFragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName());
						if (weatherFragment != null)
							weatherFragment.reDraw();
					} else {
						//현재 위치 사용을 끈 경우
						if (lastSelectedLocationType == LocationType.CurrentLocation) {
							if (favoriteAddressDtoList.isEmpty()) {
								binding.sideNavMenu.favorites.callOnClick();
							} else {
								binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick();
							}
						} else {
							//날씨 프래그먼트 다시 그림
							WeatherFragment weatherFragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName());
							if (weatherFragment != null)
								weatherFragment.reDraw();
						}
					}
				} else {
					//날씨 프래그먼트 다시 그림
					WeatherFragment weatherFragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName());
					if (weatherFragment != null)
						weatherFragment.reDraw();
				}

				originalUsingCurrentLocation = newUsingCurrentLocation;
			}
		}

	};

	@SuppressLint("MissingPermission")
	protected void onBeforeCloseApp() {
		View view = getLayoutInflater().inflate(R.layout.close_app_dialog, null);
		AlertDialog dialog = new MaterialAlertDialogBuilder(requireActivity())
				.setView(view).create();
		dialog.show();

		Window window = dialog.getWindow();
		if (window != null) {
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
			layoutParams.copyFrom(dialog.getWindow().getAttributes());
			layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		}

		AdLoader adLoader = new AdLoader.Builder(requireContext().getApplicationContext(), getString(R.string.NATIVE_ADVANCE_unitId))
				.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
					@Override
					public void onNativeAdLoaded(NativeAd nativeAd) {
						NativeTemplateStyle styles = new
								NativeTemplateStyle.Builder().withMainBackgroundColor(new ColorDrawable(Color.WHITE)).build();
						TemplateView template = (TemplateView) view.findViewById(R.id.adView);
						template.setStyles(styles);
						template.setNativeAd(nativeAd);
					}
				}).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true).build())
				.build();
		adLoader.loadAd(new AdRequest.Builder().build());

		view.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		view.findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				requireActivity().finish();
			}
		});

	}

	private void setCurrentLocationState(boolean newState) {
		binding.sideNavMenu.currentLocationLayout.setClickable(newState);
		binding.sideNavMenu.addressName.setText(newState ? R.string.enabled_use_current_location : R.string.disabled_use_current_location);
	}

	@Override
	public void onAttach(@NonNull @NotNull Context context) {
		super.onAttach(context);
		requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
		initViewModel = new ViewModelProvider(requireActivity()).get(InitViewModel.class);
		weatherViewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
		weatherViewModel.setLocationCallback(new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {

			}

			@Override
			public void onFailed(Fail fail) {
				if (fail == Fail.FAILED_FIND_LOCATION) {
					//기존의 현재 위치 값이 없으면 즐겨찾기로 이동
					Toast.makeText(requireContext().getApplicationContext(), R.string.failedFindingLocation, Toast.LENGTH_SHORT).show();
					LocationResult locationResult = new FusedLocation(requireContext().getApplicationContext()).getLastCurrentLocation();

					if (locationResult.getLocations().get(0).getLatitude() == 0.0 ||
							locationResult.getLocations().get(0).getLongitude() == 0.0) {
						binding.sideNavMenu.favorites.callOnClick();
					}
				}
			}
		});

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentMainBinding.inflate(inflater, container, false);

		binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		binding.sideNavMenu.favorites.setOnClickListener(sideNavOnClickListener);
		binding.sideNavMenu.settings.setOnClickListener(sideNavOnClickListener);
		binding.sideNavMenu.notificationAlarmSettings.setOnClickListener(sideNavOnClickListener);

		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
		binding.sideNavMenu.currentLocationLayout.setPadding(padding, MyApplication.getStatusBarHeight() + padding, padding,
				padding);
		return binding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("currentAddressName", currentAddressName);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null) {
			currentAddressName = savedInstanceState.getString("currentAddressName");
		}


		binding.sideNavMenu.currentLocationLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addWeatherFragment(LocationType.CurrentLocation, null, null);
				binding.drawerLayout.closeDrawer(binding.sideNavigation);
			}
		});

		weatherViewModel.getCurrentLocationLiveData().observe(getViewLifecycleOwner(), addressName -> {
			currentAddressName = addressName;

			if (currentAddressName != null) {
				binding.sideNavMenu.addressName.setText(currentAddressName);
			}
		});


		weatherViewModel.favoriteAddressListLiveData.observe(requireActivity(), result -> {
			createLocationsList(result);

			if (init) {
				init = false;

				final boolean usingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), false);
				final LocationType lastSelectedLocationType = LocationType.valueOf(
						sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type),
								LocationType.CurrentLocation.name()));
				setCurrentLocationState(usingCurrentLocation);

				if (currentAddressName != null)
					binding.sideNavMenu.addressName.setText(currentAddressName);

				if (lastSelectedLocationType == LocationType.CurrentLocation) {
					if (usingCurrentLocation) {
						addWeatherFragment(lastSelectedLocationType, null, null);
					} else {
						if (favoriteAddressDtoList.size() > 0) {
							binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick();
						} else {
							binding.sideNavMenu.favorites.callOnClick();
						}
					}

				} else {
					final int lastSelectedFavoriteId = sharedPreferences.getInt(
							getString(R.string.pref_key_last_selected_favorite_address_id), -1);
					if (!clickLocationItemById(lastSelectedFavoriteId)) {
						if (favoriteAddressDtoList.size() > 0) {
							binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick();
						} else {
							binding.sideNavMenu.favorites.callOnClick();
						}
					}
				}

			}
		});


	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	public void createLocationsList(List<FavoriteAddressDto> result) {
		favoriteAddressDtoList = result;
		binding.sideNavMenu.favoriteAddressLayout.setVisibility(favoriteAddressDtoList.size() > 0 ? View.VISIBLE :
				View.GONE);

		binding.sideNavMenu.favoriteAddressLayout.removeAllViews();

		LayoutInflater layoutInflater = getLayoutInflater();
		for (FavoriteAddressDto favoriteAddressDto : favoriteAddressDtoList) {
			addFavoriteLocationItemView(layoutInflater, LocationType.SelectedAddress, favoriteAddressDto);
		}
	}


	@Override
	public void onRefreshedFavoriteLocationsList(String requestKey, Bundle bundle) {
		final boolean isSelectedNewAddress = bundle.getBoolean("added");

		if (isSelectedNewAddress) {
			final int lastSelectedFavoriteAddressId = sharedPreferences.getInt(
					getString(R.string.pref_key_last_selected_favorite_address_id), -1);
			clickLocationItemById(lastSelectedFavoriteAddressId);
		} else {
			processIfPreviousFragmentIsFavorite();
		}
	}

	private void processIfPreviousFragmentIsFavorite() {
		final int lastSelectedFavoriteAddressId =
				sharedPreferences.getInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1);
		final LocationType lastSelectedLocationType = LocationType.valueOf(
				sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type), LocationType.CurrentLocation.name()));

		if (lastSelectedLocationType == LocationType.SelectedAddress) {
			if (favoriteAddressDtoList.isEmpty()) {
				setCurrentLocationState(true);
				binding.sideNavMenu.currentLocationLayout.callOnClick();
			} else {
				boolean removed = true;
				for (FavoriteAddressDto favoriteAddressDto : favoriteAddressDtoList) {
					if (favoriteAddressDto.getId() == lastSelectedFavoriteAddressId) {
						removed = false;
						break;
					}
				}

				if (removed) {
					binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick();
				}
			}

		} else {
			//현재 위치
			binding.sideNavMenu.currentLocationLayout.callOnClick();
		}

	}

	private boolean clickLocationItemById(int id) {
		FavoriteAddressDto favoriteAddressDto = null;

		for (int childIdx = 0; childIdx < binding.sideNavMenu.favoriteAddressLayout.getChildCount(); childIdx++) {
			favoriteAddressDto = (FavoriteAddressDto) binding.sideNavMenu.favoriteAddressLayout
					.getChildAt(childIdx).getTag(favDtoTagInFavLocItemView);

			if (favoriteAddressDto.getId() == id) {
				binding.sideNavMenu.favoriteAddressLayout.getChildAt(childIdx).callOnClick();
				return true;
			}
		}
		return false;
	}


	private void addFavoriteLocationItemView(LayoutInflater layoutInflater, LocationType locationType,
	                                         @Nullable FavoriteAddressDto favoriteAddressDto) {
		TextView locationItemView = (TextView) layoutInflater.inflate(R.layout.favorite_address_item_in_side_nav, null);
		locationItemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				binding.drawerLayout.closeDrawer(binding.sideNavigation);
				addWeatherFragment(locationType, favoriteAddressDto, null);
			}
		});
		locationItemView.setText(favoriteAddressDto.getDisplayName());
		locationItemView.setTag(favDtoTagInFavLocItemView, favoriteAddressDto);
		locationItemView.setTag(favTypeTagInFavLocItemView, locationType);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		int dp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		int dp16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());
		layoutParams.setMargins(dp16, dp8, dp16, dp8);

		binding.sideNavMenu.favoriteAddressLayout.addView(locationItemView, layoutParams);
	}

	@Override
	public void onDestroy() {
		onBackPressedCallback.remove();
		getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}

	private final View.OnClickListener sideNavOnClickListener = new View.OnClickListener() {
		@SuppressLint("NonConstantResourceId")
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.favorites:
					final MapFragment mapFragment = new MapFragment();
					Bundle bundle = new Bundle();
					bundle.putString(BundleKey.RequestFragment.name(), MainTransactionFragment.class.getName());

					mapFragment.setArguments(bundle);
					mapFragment.setOnResultFavoriteListener(new MapFragment.OnResultFavoriteListener() {
						@Override
						public void onAddedNewAddress(FavoriteAddressDto newFavoriteAddressDto, List<FavoriteAddressDto> favoriteAddressDtoList, boolean removed) {
							getChildFragmentManager().popBackStackImmediate();
							onResultMapFragment(newFavoriteAddressDto);
						}

						@Override
						public void onResult(List<FavoriteAddressDto> favoriteAddressDtoList) {
							onResultMapFragment(null);
						}

						@Override
						public void onClickedAddress(@Nullable FavoriteAddressDto favoriteAddressDto) {

						}
					});

					final String tag = MapFragment.class.getName();

					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.hide(getChildFragmentManager().getPrimaryNavigationFragment()).add(
							binding.fragmentContainer.getId(), mapFragment,
							tag).addToBackStack(tag).setPrimaryNavigationFragment(mapFragment).commitAllowingStateLoss();
					break;
				case R.id.settings:
					SettingsMainFragment settingsMainFragment = new SettingsMainFragment();
					getChildFragmentManager().beginTransaction().hide(getChildFragmentManager().getPrimaryNavigationFragment()).add(
							binding.fragmentContainer.getId(), settingsMainFragment,
							getString(R.string.tag_settings_main_fragment)).addToBackStack(
							getString(R.string.tag_settings_main_fragment)).setPrimaryNavigationFragment(settingsMainFragment).commitAllowingStateLoss();
					break;
				case R.id.notificationAlarmSettings:
					NotificationFragment notificationFragment = new NotificationFragment();
					String notiTag = NotificationFragment.class.getName();

					getChildFragmentManager().beginTransaction().hide(getChildFragmentManager().getPrimaryNavigationFragment()).add(
							binding.fragmentContainer.getId(), notificationFragment,
							notiTag).addToBackStack(notiTag).setPrimaryNavigationFragment(notificationFragment).commitAllowingStateLoss();
					break;
			}
			binding.drawerLayout.closeDrawer(binding.sideNavigation, false);
		}
	};

	public void onResultMapFragment(@Nullable FavoriteAddressDto newFavoriteAddressDto) {
		//변경된 위치가 있는지 확인
		setCurrentLocationState(sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), false));
		final boolean added = newFavoriteAddressDto != null;

		if (added) {
			//즐겨찾기 변동 발생
			Bundle result = new Bundle();
			result.putBoolean("added", true);

			onRefreshedFavoriteLocationsList(null, result);
		} else {
			//변동 없음
			processIfPreviousFragmentIsFavorite();
		}
	}

	@Override
	public void addWeatherFragment(LocationType locationType, @Nullable FavoriteAddressDto favoriteAddressDto, @Nullable Bundle arguments) {
		final Bundle bundle = new Bundle();

		if (arguments != null)
			bundle.putAll(arguments);

		bundle.putSerializable("LocationType", locationType);
		bundle.putSerializable("FavoriteAddressDto", favoriteAddressDto);

		final WeatherFragment newWeatherFragment = new WeatherFragment(this);

		newWeatherFragment.setOnAsyncLoadCallback(new WeatherFragment.OnAsyncLoadCallback() {
			@Override
			public void onFinished(Fragment fragment) {
				if (fragment.isAdded())
					((ILoadWeatherData) fragment).load();
			}
		});

		newWeatherFragment.setArguments(bundle);
		newWeatherFragment.setMenuOnClickListener(v -> binding.drawerLayout.openDrawer(binding.sideNavigation));

		newWeatherFragment.setiRefreshFavoriteLocationListOnSideNav((IRefreshFavoriteLocationListOnSideNav) this);

		final FragmentManager fragmentManager = getChildFragmentManager();
		fragmentManager.beginTransaction().replace(binding.fragmentContainer.getId(), newWeatherFragment,
				WeatherFragment.class.getName()).setPrimaryNavigationFragment(newWeatherFragment).commit();
	}

	@Override
	public void refreshFavorites(DbQueryCallback<List<FavoriteAddressDto>> callback) {
		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				MainThreadWorker.runOnUiThread(() -> callback.onResultSuccessful(result));
			}

			@Override
			public void onResultNoData() {

			}
		});
	}


}