package com.lifedawn.bestweather.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
		                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
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
				if (originalUsingCurrentLocation != newUsingCurrentLocation) {
					setCurrentLocationState(newUsingCurrentLocation);
					if (!newUsingCurrentLocation && weatherMainFragment.getFavoriteAddressTypeOfWeatherFragment() == LocationType.CurrentLocation) {
						if (favoriteAddressDtoList.size() > 0) {
							binding.sideNavMenu.favoriteAddressLayout.getChildAt(0).callOnClick();
						} else {
							weatherMainFragment.removeFragment();
							binding.sideNavMenu.favorites.callOnClick();
						}
					} else if (newUsingCurrentLocation) {
						if (weatherMainFragment.getWeatherFragment() == null) {
							binding.sideNavMenu.currentLocationLayout.callOnClick();
						}
					}
				} else {
					weatherMainFragment.getWeatherFragment().reDraw();
				}
			}
		}
	};

	private void setCurrentLocationState(boolean newState) {
		binding.sideNavMenu.currentLocationLayout.setClickable(newState);
		if (!newState) {
			binding.sideNavMenu.addressName.setText(getString(R.string.disabled_use_current_location));
		} else {
			binding.sideNavMenu.addressName.setText(getString(R.string.enabled_use_current_location));
		}
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
						final boolean usingCurrentLocation =
								sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), false);
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

						/*
						<경우의 수>
						1.현재 위치를 보여주는 경우 : 즐겨찾기에 변화가 생긴 경우, 그대로 인 경우
						변화 생김 : 리스트 갱신
						그대로 : 유지
						
						2.즐겨찾기의 위치를 보여주는 경우 : 기존 표시 장소가 삭제된 경우, 삭제 되지 않고 목록에 변화가 생긴 경우,
						목록이 빈 경우
						기존 표시 장소 삭제 : 리스트 갱신하고, 다른 장소 표시
						기존 표시 장소 삭제없고 목록에 변화(목록이 비지 않음) : 리스트 갱신
						목록이 빔 : 현재 위치 사용(즐겨찾기 화면에서 현재 위치 사용확인 후 나옴)

						3.아무것도 표시하지 않는 경우 :
						즐겨찾기가 빔 : 현재 위치 사용(즐겨찾기 화면에서 현재 위치 사용확인 후 나옴)
						비지 않음 : 다른 장소 표시
						 */
						if (weatherMainFragment.getWeatherFragment() == null) {
							if (newSet.isEmpty()) {
								binding.sideNavMenu.currentLocationLayout.callOnClick();
							} else {
								init(true);
							}
						} else {
							FavoriteAddressDto currentLocationFavoriteAddressDto = weatherMainFragment.getFavoriteAddressDtoOfWeatherFragment();
							LocationType currentLocationLocationType = weatherMainFragment.getFavoriteAddressTypeOfWeatherFragment();

							if (currentLocationLocationType == LocationType.CurrentLocation) {
								if (!addedSet.isEmpty() || !removedSet.isEmpty()) {
									init(false);
								}
							} else {
								if (removedSet.contains(currentLocationFavoriteAddressDto.getId())) {
									init(true);
								} else {
									if (newSet.isEmpty()) {
										binding.sideNavMenu.currentLocationLayout.callOnClick();
									} else if (!addedSet.isEmpty() || !removedSet.isEmpty()) {
										init(false);
									}
								}
							}
						}
					}

				});
		fragmentManager.setFragmentResultListener(getString(R.string.key_back_from_find_address_to_main), this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						final boolean isSelectedAddress = result.getBoolean(getString(R.string.bundle_key_selected_address_dto));

						if (isSelectedAddress) {
							final int newFavoriteAddressDtoId = result.getInt(getString(R.string.bundle_key_new_favorite_address_dto_id));
							sharedPreferences.edit().putString(getString(R.string.pref_key_last_selected_favorite_address_id),
									String.valueOf(newFavoriteAddressDtoId)).apply();
							init(true);
						}
					}
				});

		weatherViewModel.setLocationCallback(new Gps.LocationCallback() {
			@Override
			public void onSuccessful(Location location) {

			}

			@Override
			public void onFailed(Fail fail) {
				weatherMainFragment.removeFragment();

				if (fail == Fail.REJECT_PERMISSION) {
					binding.sideNavMenu.favorites.callOnClick();
				} else if (fail == Fail.DISABLED_GPS) {
					binding.sideNavMenu.favorites.callOnClick();
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
		binding.sideNavMenu.favorites.setOnClickListener(sideNavOnClickListener);
		binding.sideNavMenu.settings.setOnClickListener(sideNavOnClickListener);

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

		init(true);
		initializing = false;
	}

	private void init(boolean select) {
		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							binding.sideNavMenu.favoriteAddressLayout.removeAllViews();
							favoriteAddressDtoList = result;

							for (FavoriteAddressDto favoriteAddressDto : result) {
								addFavoriteLocationItemView(LocationType.SelectedAddress, favoriteAddressDto);
							}

							if (result.size() > 0) {
								binding.sideNavMenu.favoriteAddressLayout.setVisibility(View.VISIBLE);
							} else {
								binding.sideNavMenu.favoriteAddressLayout.setVisibility(View.GONE);
							}
							/*
							마지막으로 선택된 위치의 id값의 경우의 수 : 현재위치 또는 없음, 즐겨찾기에 추가한 주소
							1. 현재 위치인 경우 : 현재 위치 on 이면 현재위치 사용, 아니면 즐겨찾기에서 첫번째 주소를 선택
							즐겨찾기가 빈 경우 :  주소 추가 화면 표시

							2. 즐겨찾기에 추가한 주소 : 즐겨찾기에 존재하는 id인 경우 해당 주소를 선택,
							즐겨찾기에 없는 경우 : 현재 위치가 on인 경우에 현재위치사용, off이면 즐겨찾기에서 첫번째 주소선택
							즐겨찾기가 빈 경우 :  주소 추가 화면 표시
							 */

							final boolean usingCurrentLocation = sharedPreferences.getBoolean(
									getString(R.string.pref_key_use_current_location), true);
							setCurrentLocationState(usingCurrentLocation);

							if (select) {
								final String lastSelectedFavoriteAddressId = sharedPreferences.getString(
										getString(R.string.pref_key_last_selected_favorite_address_id), "");

								if (lastSelectedFavoriteAddressId.equals(
										LocationType.CurrentLocation.name()) || lastSelectedFavoriteAddressId.isEmpty()) {
									//현재 위치 또는 없음
									if (usingCurrentLocation) {
										binding.sideNavMenu.currentLocationLayout.callOnClick();
									} else {
										if (result.isEmpty()) {
											//주소 추가 화면 표시
											binding.sideNavMenu.favorites.callOnClick();
										} else {
											//즐겨찾기에서 첫번째 주소 선택
											weatherMainFragment.setWeatherFragment(LocationType.SelectedAddress, result.get(0));
										}
									}
								} else {
									//즐겨찾기
									final int selectedId = Integer.parseInt(lastSelectedFavoriteAddressId);
									FavoriteAddressDto selectedFavoriteAddressDto = null;

									for (FavoriteAddressDto favoriteAddressDto : result) {
										if (favoriteAddressDto.getId() == selectedId) {
											selectedFavoriteAddressDto = favoriteAddressDto;
											break;
										}
									}

									if (selectedFavoriteAddressDto != null) {
										//즐겨찾기에 있으므로 해당 주소선택
										weatherMainFragment.setWeatherFragment(LocationType.SelectedAddress,
												selectedFavoriteAddressDto);
									} else {
										//즐겨찾기에 없는 경우
										if (usingCurrentLocation) {
											//현재위치사용
											binding.sideNavMenu.currentLocationLayout.callOnClick();
										} else {
											if (result.isEmpty()) {
												//주소 추가 화면 표시
												binding.sideNavMenu.favorites.callOnClick();
											} else {
												//즐겨찾기에서 첫번째 주소 선택
												weatherMainFragment.setWeatherFragment(LocationType.SelectedAddress, result.get(0));
											}
										}
									}

								}
							}
						}
					});
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
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