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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentMainBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.notification.NotificationFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.settings.fragments.SettingsMainFragment;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainTransactionFragment extends Fragment implements IRefreshFavoriteLocationListOnSideNav {
	private final int favTypeTagInFavLocItemView = R.id.locationTypeTagInFavLocItemViewInSideNav;
	private final int favDtoTagInFavLocItemView = R.id.favoriteLocationDtoTagInFavLocItemViewInSideNav;

	private FragmentMainBinding binding;
	private WeatherViewModel weatherViewModel;
	private boolean initializing = true;
	private SharedPreferences sharedPreferences;
	private List<FavoriteAddressDto> favoriteAddressDtoList = new ArrayList<>();

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
				//closeWindow.clicked(getActivity());
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
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);


			if (f instanceof SettingsMainFragment) {
				final boolean newUsingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location),
						true);
				final LocationType lastSelectedLocationType = LocationType.valueOf(
						sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type),
								LocationType.CurrentLocation.name()));

				if (originalUsingCurrentLocation != newUsingCurrentLocation) {
					setCurrentLocationState(newUsingCurrentLocation);
					if (newUsingCurrentLocation) {
						//날씨 프래그먼트 다시 그림
						WeatherFragment weatherFragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName());
						weatherFragment.reDraw();

					} else {
						if (lastSelectedLocationType == LocationType.CurrentLocation) {
							if (favoriteAddressDtoList.isEmpty()) {
								binding.sideNavMenu.favorites.callOnClick();
							} else {
								binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick();
							}
						} else {
							//날씨 프래그먼트 다시 그림
							WeatherFragment weatherFragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName());
							weatherFragment.reDraw();

						}
					}
				} else {
					//날씨 프래그먼트 다시 그림
					WeatherFragment weatherFragment = (WeatherFragment) getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName());
					weatherFragment.reDraw();
				}
			}
		}

	};

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
			//layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
			//window.setAttributes(layoutParams);
		}

		AdLoader adLoader = new AdLoader.Builder(requireActivity(), getString(R.string.NATIVE_ADVANCE_testUnitId))
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

		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		weatherViewModel.getCurrentLocationLiveData().observe(this, new Observer<String>() {
			@Override
			public void onChanged(String addressName) {
				if (addressName != null) {
					binding.sideNavMenu.addressName.setText(addressName);
				}

			}
		});

		weatherViewModel.setLocationCallback(new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {

			}

			@Override
			public void onFailed(Fail fail) {
				if (fail == Fail.FAILED_FIND_LOCATION) {
					//기존의 현재 위치 값이 없으면 즐겨찾기로 이동
					double latitude = Double.parseDouble(
							sharedPreferences.getString(getString(R.string.pref_key_last_current_location_latitude), "0.0"));
					double longitude = Double.parseDouble(
							sharedPreferences.getString(getString(R.string.pref_key_last_current_location_longitude), "0.0"));

					if (latitude == 0.0 || longitude == 0.0) {
						binding.sideNavMenu.favorites.callOnClick();
					}
				}
			}
		});
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentMainBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		binding.sideNavMenu.favorites.setOnClickListener(sideNavOnClickListener);
		binding.sideNavMenu.settings.setOnClickListener(sideNavOnClickListener);
		binding.sideNavMenu.notificationAlarmSettings.setOnClickListener(sideNavOnClickListener);

		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
		binding.sideNavMenu.currentLocationLayout.setPadding(padding, MyApplication.getStatusBarHeight() + padding, padding,
				padding);
		binding.sideNavMenu.currentLocationLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addWeatherFragment(LocationType.CurrentLocation, null);
				binding.drawerLayout.closeDrawer(binding.sideNavigation, false);
			}
		});

		createFavoriteLocationsList(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				final boolean usingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), true);
				final LocationType lastSelectedLocationType = LocationType.valueOf(
						sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type),
								LocationType.CurrentLocation.name()));
				setCurrentLocationState(usingCurrentLocation);

				if (lastSelectedLocationType == LocationType.CurrentLocation) {
					if (usingCurrentLocation) {
						addWeatherFragment(lastSelectedLocationType, null);
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
					if (!clickLocationViewWithId(lastSelectedFavoriteId)) {
						if (favoriteAddressDtoList.size() > 0) {
							binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick();
						} else {
							binding.sideNavMenu.favorites.callOnClick();
						}
					}
				}
			}

			@Override
			public void onResultNoData() {

			}
		});


		initializing = false;
	}


	private void createFavoriteLocationsList(DbQueryCallback<List<FavoriteAddressDto>> callback) {
		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							favoriteAddressDtoList = result;
							binding.sideNavMenu.favoriteAddressLayout.removeAllViews();

							for (FavoriteAddressDto favoriteAddressDto : favoriteAddressDtoList) {
								addFavoriteLocationItemView(LocationType.SelectedAddress, favoriteAddressDto);
							}

							if (favoriteAddressDtoList.size() > 0) {
								binding.sideNavMenu.favoriteAddressLayout.setVisibility(View.VISIBLE);
							} else {
								binding.sideNavMenu.favoriteAddressLayout.setVisibility(View.GONE);
							}
							callback.onResultSuccessful(result);
						}
					});
				}
			}

			@Override
			public void onResultNoData() {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							callback.onResultNoData();
						}
					});
				}
			}
		});
	}

	/**
	 * Always processing on ui-thread
	 **/
	@Override
	public void refreshFavoriteLocationsList(String requestKey, Bundle bundle) {
		createFavoriteLocationsList(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				if (requestKey.equals(FindAddressFragment.class.getName())) {
					final int lastSelectedFavoriteAddressId = sharedPreferences.getInt(
							getString(R.string.pref_key_last_selected_favorite_address_id), -1);
					clickLocationViewWithId(lastSelectedFavoriteAddressId);
				} else if (requestKey.equals(FavoritesFragment.class.getName())) {
					processIfPreviousFragmentIsFavorite(bundle);
				}

			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	private void processIfPreviousFragmentIsFavorite(Bundle bundle) {
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
			boolean enabledUseCurrentLocation = bundle.getBoolean(BundleKey.ChangedUseCurrentLocation.name());
			boolean refresh = bundle.getBoolean(BundleKey.Refresh.name());

			if (enabledUseCurrentLocation || refresh) {
				setCurrentLocationState(true);
				binding.sideNavMenu.currentLocationLayout.callOnClick();
			}
		}
	}

	private boolean clickLocationViewWithId(int id) {
		View view = null;
		FavoriteAddressDto favoriteAddressDto = null;

		for (int childIdx = 0; childIdx < binding.sideNavMenu.favoriteAddressLayout.getChildCount(); childIdx++) {
			view = binding.sideNavMenu.favoriteAddressLayout.getChildAt(childIdx);
			favoriteAddressDto = (FavoriteAddressDto) view.getTag(favDtoTagInFavLocItemView);

			if (favoriteAddressDto.getId() == id) {
				binding.sideNavMenu.favoriteAddressLayout.getChildAt(childIdx).callOnClick();
				return true;
			}
		}
		return false;
	}


	private void addFavoriteLocationItemView(LocationType locationType, @Nullable FavoriteAddressDto favoriteAddressDto) {
		TextView locationItemView = (TextView) getLayoutInflater().inflate(R.layout.favorite_address_item_in_side_nav, null);
		locationItemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				binding.drawerLayout.closeDrawer(binding.sideNavigation);
				addWeatherFragment(locationType, favoriteAddressDto);
			}
		});

		locationItemView.setText(favoriteAddressDto.getAddress());
		locationItemView.setTag(favDtoTagInFavLocItemView, favoriteAddressDto);
		locationItemView.setTag(favTypeTagInFavLocItemView, locationType);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		int dp12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
		int dp16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());
		layoutParams.setMargins(dp16, 0, dp16, dp12);

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
					FavoritesFragment favoritesFragment = new FavoritesFragment();
					Bundle bundle = new Bundle();
					bundle.putString(BundleKey.RequestFragment.name(), MainTransactionFragment.class.getName());
					favoritesFragment.setArguments(bundle);

					favoritesFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
						@Override
						public void onResultFragment(Bundle result) {
							//변경된 위치가 있는지 확인
							String lastFragment = result.getString(BundleKey.LastFragment.name());
							List<FavoriteAddressDto> newFavoriteAddressDtoList = (List<FavoriteAddressDto>) result.getSerializable(
									BundleKey.newFavoriteAddressDtoList.name());

							Set<Integer> lastSet = new HashSet<>();
							Set<Integer> newSet = new HashSet<>();

							for (FavoriteAddressDto favoriteAddressDto : favoriteAddressDtoList) {
								lastSet.add(favoriteAddressDto.getId());
							}
							for (FavoriteAddressDto favoriteAddressDto : newFavoriteAddressDtoList) {
								newSet.add(favoriteAddressDto.getId());
							}

							Set<Integer> addedSet = new HashSet<>(newSet);
							addedSet.removeAll(lastSet);
							Set<Integer> removedSet = new HashSet<>(lastSet);
							removedSet.removeAll(newSet);

							if (!addedSet.isEmpty() || !removedSet.isEmpty()) {
								//즐겨찾기 변동 발생
								refreshFavoriteLocationsList(lastFragment, result);
							} else {
								//변동 없음
								processIfPreviousFragmentIsFavorite(result);
							}
						}
					});

					String tag = FavoritesFragment.class.getName();

					getChildFragmentManager().beginTransaction().hide(
							getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName())).add(
							binding.fragmentContainer.getId(), favoritesFragment,
							tag).addToBackStack(tag).commit();
					break;
				case R.id.settings:
					SettingsMainFragment settingsMainFragment = new SettingsMainFragment();
					getChildFragmentManager().beginTransaction().hide(
							getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName())).add(
							binding.fragmentContainer.getId(), settingsMainFragment,
							getString(R.string.tag_settings_main_fragment)).addToBackStack(
							getString(R.string.tag_settings_main_fragment)).commit();
					break;
				case R.id.notificationAlarmSettings:
					NotificationFragment notificationFragment = new NotificationFragment();
					String favTag = NotificationFragment.class.getName();

					getChildFragmentManager().beginTransaction().hide(
							getChildFragmentManager().findFragmentByTag(WeatherFragment.class.getName())).add(
							binding.fragmentContainer.getId(), notificationFragment,
							favTag).addToBackStack(favTag).commit();
					break;
			}
			binding.drawerLayout.closeDrawer(binding.sideNavigation, false);
		}
	};

	private void addWeatherFragment(LocationType locationType, @Nullable FavoriteAddressDto favoriteAddressDto) {
		Bundle bundle = new Bundle();

		bundle.putSerializable("LocationType", locationType);
		if (favoriteAddressDto != null) {
			bundle.putSerializable("FavoriteAddressDto", favoriteAddressDto);
		}

		WeatherFragment newWeatherFragment = new WeatherFragment();
		newWeatherFragment.setArguments(bundle);
		newWeatherFragment.setMenuOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				binding.drawerLayout.openDrawer(binding.sideNavigation);

			}
		});
		newWeatherFragment.setiRefreshFavoriteLocationListOnSideNav((IRefreshFavoriteLocationListOnSideNav) this);
		newWeatherFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
			@Override
			public void onResultFragment(Bundle result) {

			}
		});

		getChildFragmentManager().beginTransaction().replace(binding.fragmentContainer.getId(), newWeatherFragment,
				WeatherFragment.class.getName()).commitAllowingStateLoss();
	}
}