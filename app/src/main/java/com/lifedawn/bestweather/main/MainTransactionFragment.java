package com.lifedawn.bestweather.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.CloseWindow;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.databinding.FragmentMainBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.settings.fragments.SettingsMainFragment;
import com.lifedawn.bestweather.weathers.WeatherMainFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainTransactionFragment extends Fragment {
	private FragmentMainBinding binding;
	private WeatherViewModel weatherViewModel;
	private boolean initializing = true;

	private WeatherMainFragment weatherMainFragment;
	private SharedPreferences sharedPreferences;

	private static final int favTypeTagInFavLocItemView = R.id.locationTypeTagInFavLocItemViewInSideNav;
	private static final int favDtoTagInFavLocItemView = R.id.favoriteLocationDtoTagInFavLocItemViewInSideNav;

	private List<FavoriteAddressDto> favoriteAddressDtoList = new ArrayList<>();

	private final CloseWindow closeWindow = new CloseWindow(new CloseWindow.OnBackKeyDoubleClickedListener() {
		@Override
		public void onDoubleClicked() {
			requireActivity().finish();
		}
	});

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (getChildFragmentManager().getBackStackEntryCount() > 0) {
				getChildFragmentManager().popBackStackImmediate();
			} else {
				closeWindow.clicked(getActivity());
			}
		}
	};

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		private boolean originalUsingCurrentLocation = false;

		@Override
		public void onFragmentAttached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @NonNull @NotNull Context context) {
			super.onFragmentAttached(fm, f, context);
			if (f instanceof SettingsMainFragment) {
				originalUsingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), false);
			}
		}

		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
		                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof SettingsMainFragment) {

				final boolean newUsingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location),
						true);
				final LocationType lastSelectedLocationType =
						LocationType.enumOf(sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type),
								LocationType.CurrentLocation.name()));

				if (originalUsingCurrentLocation != newUsingCurrentLocation) {
					setCurrentLocationState(newUsingCurrentLocation);
					if (newUsingCurrentLocation) {
						//날씨 프래그먼트 다시 그림
						weatherMainFragment.reDraw();

					} else {
						if (lastSelectedLocationType == LocationType.CurrentLocation) {
							if (favoriteAddressDtoList.isEmpty()) {
								binding.sideNavMenu.favorites.callOnClick();
							} else {
								binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick();
							}
						} else {
							//날씨 프래그먼트 다시 그림
							weatherMainFragment.reDraw();


						}
					}
				} else {
					//날씨 프래그먼트 다시 그림
					weatherMainFragment.reDraw();
				}
			}
		}
	};

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
		FragmentManager fragmentManager = getChildFragmentManager();
		fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true);

		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		weatherViewModel.getCurrentLocationLiveData().observe(this, new Observer<String>() {
			@Override
			public void onChanged(String addressName) {
				if (!initializing) {
					binding.sideNavMenu.addressName.setText(addressName);
				}

			}
		});

		fragmentManager.setFragmentResultListener(getString(R.string.key_back_from_favorite_to_main), this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						//변경된 위치가 있는지 확인
						List<FavoriteAddressDto> newFavoriteAddressDtoList =
								(List<FavoriteAddressDto>) result.getSerializable(getString(R.string.bundle_key_new_favorite_address_list));

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
							refreshFavoriteLocationsList(requestKey, result);
						} else {
							//변동 없음
							processIfPreviousFragmentIsFavorite(result);
						}
					}

				});
		fragmentManager.setFragmentResultListener(getString(R.string.key_back_from_find_address_to_main), this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						final boolean isSelectedNewAddress = result.getBoolean(getString(R.string.bundle_key_selected_address_dto));

						if (isSelectedNewAddress) {
							final int newFavoriteAddressDtoId = result.getInt(getString(R.string.bundle_key_new_favorite_address_dto_id));
							sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id), newFavoriteAddressDtoId).apply();
							refreshFavoriteLocationsList(requestKey, result);
						}
					}
				});

		weatherViewModel.setLocationCallback(new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {

			}

			@Override
			public void onFailed(Fail fail) {
				//기존의 현재 위치 값이 없으면 즐겨찾기로 이동
				Double latitude = Double.parseDouble(
						sharedPreferences.getString(getString(R.string.pref_key_last_current_location_latitude), "0.0"));
				Double longitude = Double.parseDouble(
						sharedPreferences.getString(getString(R.string.pref_key_last_current_location_longitude), "0.0"));

				if (latitude == 0.0 && longitude == 0.0) {
					binding.sideNavMenu.favorites.callOnClick();
				}
			}
		});
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
							callback.processResult(result);
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
	private void refreshFavoriteLocationsList(String requestKey, Bundle bundle) {
		createFavoriteLocationsList(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				if (requestKey.equals(getString(R.string.key_back_from_find_address_to_main))) {
					final int lastSelectedFavoriteAddressId =
							sharedPreferences.getInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1);
					clickLocationViewWithId(lastSelectedFavoriteAddressId);
				} else if (requestKey.equals(getString(R.string.key_back_from_favorite_to_main))) {
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
				Integer.parseInt(sharedPreferences.getString(getString(R.string.bundle_key_new_favorite_address_dto_id), "-1"));
		final LocationType lastSelectedLocationType =
				LocationType.enumOf(sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type),
						LocationType.CurrentLocation.name()));

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
			boolean enabledUseCurrentLocation = bundle.getBoolean(getString(R.string.bundle_key_changed_use_current_location));
			if (enabledUseCurrentLocation) {
				setCurrentLocationState(true);
				binding.sideNavMenu.currentLocationLayout.callOnClick();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentMainBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.sideNavMenu.favorites.setOnClickListener(sideNavOnClickListener);
		binding.sideNavMenu.settings.setOnClickListener(sideNavOnClickListener);

		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
		binding.sideNavMenu.currentLocationLayout.setPadding(padding, MainActivity.getHeightOfStatusBar(getContext()) + padding, padding, padding);

		weatherMainFragment = new WeatherMainFragment(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				binding.drawerLayout.openDrawer(binding.sideNavigation);
			}
		});

		getChildFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), weatherMainFragment,
				getString(R.string.tag_weather_main_fragment)).commit();

		binding.sideNavMenu.currentLocationLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				weatherMainFragment.setWeatherFragment(LocationType.CurrentLocation, null);
				binding.drawerLayout.closeDrawer(binding.sideNavigation);
			}
		});

		createFavoriteLocationsList(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				final boolean usingCurrentLocation = sharedPreferences.getBoolean(
						getString(R.string.pref_key_use_current_location), true);
				final LocationType lastSelectedLocationType =
						LocationType.enumOf(sharedPreferences.getString(getString(R.string.pref_key_last_selected_location_type),
								LocationType.CurrentLocation.name()));
				setCurrentLocationState(usingCurrentLocation);

				if (lastSelectedLocationType == LocationType.CurrentLocation) {
					if (usingCurrentLocation) {
						binding.sideNavMenu.currentLocationLayout.callOnClick();
					} else {
						binding.sideNavMenu.favorites.callOnClick();
					}
				} else {
					final int lastSelectedFavoriteId =
							sharedPreferences.getInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1);
					if (!clickLocationViewWithId(lastSelectedFavoriteId)) {
						binding.sideNavMenu.favorites.callOnClick();
					}
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
		initializing = false;
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
				weatherMainFragment.setWeatherFragment(locationType, favoriteAddressDto);
				binding.drawerLayout.closeDrawer(binding.sideNavigation);
			}
		});

		locationItemView.setText(favoriteAddressDto.getAddress());
		locationItemView.setTag(favDtoTagInFavLocItemView, favoriteAddressDto);
		locationItemView.setTag(favTypeTagInFavLocItemView, locationType);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());

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
					getChildFragmentManager().setFragmentResult(getString(R.string.key_from_main_to_favorite), new Bundle());
					getChildFragmentManager().beginTransaction().hide(
							getChildFragmentManager().findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(
							binding.fragmentContainer.getId(), favoritesFragment,
							getString(R.string.tag_favorites_fragment)).addToBackStack(getString(R.string.tag_favorites_fragment)).commit();
					break;
				case R.id.settings:
					SettingsMainFragment settingsMainFragment = new SettingsMainFragment();
					getChildFragmentManager().beginTransaction().hide(
							getChildFragmentManager().findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(
							binding.fragmentContainer.getId(), settingsMainFragment,
							getString(R.string.tag_settings_main_fragment)).addToBackStack(
							getString(R.string.tag_settings_main_fragment)).commit();
					break;
			}
			binding.drawerLayout.closeDrawer(binding.sideNavigation);
		}
	};
}