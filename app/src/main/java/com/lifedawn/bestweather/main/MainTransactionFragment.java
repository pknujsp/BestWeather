package com.lifedawn.bestweather.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.lifedawn.bestweather.commons.enums.FavoriteAddressType;
import com.lifedawn.bestweather.databinding.FragmentMainBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.settings.fragments.SettingsMainFragment;
import com.lifedawn.bestweather.weathers.WeatherMainFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainTransactionFragment extends Fragment {
	private FragmentMainBinding binding;
	private WeatherViewModel weatherViewModel;
	private boolean initializing = true;
	
	private WeatherMainFragment weatherMainFragment;
	private SharedPreferences sharedPreferences;
	
	private static final int favTypeTagInFavLocItemView = R.id.locationTypeTagInFavLocItemViewInSideNav;
	private static final int favDtoTagInFavLocItemView = R.id.favoriteLocationDtoTagInFavLocItemViewInSideNav;
	
	private CloseWindow closeWindow = new CloseWindow(new CloseWindow.OnBackKeyDoubleClickedListener() {
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
				originalUsingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), true);
			}
		}
		
		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);
			if (f instanceof FavoritesFragment) {
				//즐겨찾기 없고 현재 위치 미 사용인 경우 처리할 내용
				if (((FavoritesFragment) f).isEnableCurrentLocation()) {
					init();
				}
			} else if (f instanceof SettingsMainFragment) {
				boolean newUsingCurrentLocation = sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), true);
				
				if (originalUsingCurrentLocation != newUsingCurrentLocation) {
					if (newUsingCurrentLocation) {
						addFavoriteLocationItemView(FavoriteAddressType.CurrentLocation, null);
					} else {
						binding.sideNavMenu.favoriteAddressLayout.removeViewAt(0);
						init();
					}
				}
			} else if (f instanceof FindAddressFragment) {
				if (((FindAddressFragment) f).isSelectedAddress()) {
				
				}
			}
		}
	};
	
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
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		weatherViewModel.getAddAddressesLiveData().observe(this, new Observer<FavoriteAddressDto>() {
			@Override
			public void onChanged(FavoriteAddressDto favoriteAddressDto) {
				if (!initializing) {
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								addFavoriteLocationItemView(FavoriteAddressType.SelectedAddress, favoriteAddressDto);
								
								View itemView = null;
								FavoriteAddressType favoriteAddressTypeInView = null;
								FavoriteAddressDto favoriteAddressDtoInView = null;
								
								int childCount = binding.sideNavMenu.favoriteAddressLayout.getChildCount();
								for (int child = 0; child < childCount; child++) {
									itemView = binding.sideNavMenu.favoriteAddressLayout.getChildAt(child);
									favoriteAddressTypeInView = (FavoriteAddressType) itemView.getTag(favTypeTagInFavLocItemView);
									
									if (favoriteAddressTypeInView == FavoriteAddressType.SelectedAddress) {
										favoriteAddressDtoInView = (FavoriteAddressDto) itemView.getTag(favDtoTagInFavLocItemView);
										if (favoriteAddressDtoInView.getId().equals(favoriteAddressDto.getId())) {
											itemView.callOnClick();
											break;
										}
									}
								}
							}
						});
						
					}
				}
			}
		});
		
		weatherViewModel.getDeleteAddressesLiveData().observe(this, new Observer<FavoriteAddressDto>() {
			@Override
			public void onChanged(FavoriteAddressDto favoriteAddressDto) {
				if (!initializing) {
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								int deletedId = favoriteAddressDto.getId();
								View itemView = null;
								FavoriteAddressType favoriteAddressTypeInView = null;
								FavoriteAddressDto favoriteAddressDtoInView = null;
								
								int childCount = binding.sideNavMenu.favoriteAddressLayout.getChildCount();
								for (int child = 0; child < childCount; child++) {
									itemView = binding.sideNavMenu.favoriteAddressLayout.getChildAt(child);
									favoriteAddressTypeInView = (FavoriteAddressType) itemView.getTag(favTypeTagInFavLocItemView);
									
									if (favoriteAddressTypeInView == FavoriteAddressType.SelectedAddress) {
										favoriteAddressDtoInView = (FavoriteAddressDto) itemView.getTag(favDtoTagInFavLocItemView);
										if (favoriteAddressDtoInView.getId() == deletedId) {
											binding.sideNavMenu.favoriteAddressLayout.removeViewAt(child);
											break;
										}
									}
								}
								
								//현재 표시된 날씨의 위치정보가 삭제한 것과 일치하면 현재 날씨 프래그먼트 제거
								if (weatherMainFragment.getFavoriteAddressTypeOfWeatherFragment() == FavoriteAddressType.SelectedAddress) {
									if (favoriteAddressDto.getId().equals(
											weatherMainFragment.getFavoriteAddressDtoOfWeatherFragment().getId())) {
										sharedPreferences.edit().putString(getString(R.string.pref_key_last_selected_favorite_address_id),
												"").apply();
										init();
									}
								}
							}
						});
						
					}
				}
			}
		});
		
		weatherViewModel.getCurrentLocationLiveData().observe(this, new Observer<String>() {
			@Override
			public void onChanged(String addressName) {
				if (!initializing) {
					String text = getString(R.string.current_location) + "\n" + addressName;
					TextView currentLocationItemView = (TextView) binding.sideNavMenu.favoriteAddressLayout.getChildAt(0);
					currentLocationItemView.setText(text);
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
		
		init();
		initializing = false;
	}
	
	private void init() {
		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							final boolean usingCurrentLocation = sharedPreferences.getBoolean(
									getString(R.string.pref_key_use_current_location), false);
							
							if (binding.sideNavMenu.favoriteAddressLayout.getChildCount() == 0) {
								if (usingCurrentLocation) {
									addFavoriteLocationItemView(FavoriteAddressType.CurrentLocation, null);
								}
								
								for (FavoriteAddressDto favoriteAddressDto : result) {
									addFavoriteLocationItemView(FavoriteAddressType.SelectedAddress, favoriteAddressDto);
								}
							}
							String lastSelectedFavoriteAddressId = sharedPreferences.getString(
									getString(R.string.pref_key_last_selected_favorite_address_id), "");

							/*
							마지막으로 선택된 위치의 id값의 경우의 수 : 현재위치, 즐겨찾기에 추가한 주소
							1. 현재 위치인 경우 : 현재 위치 on이면 현재위치사용, 아니면 즐겨찾기에서 첫번째 주소를 선택
							즐겨찾기가 빈 경우 :  주소 추가 화면 표시

							2. 즐겨찾기에 추가한 주소 : 즐겨찾기에 존재하는 id인 경우 해당 주소를 선택,
							즐겨찾기에 없는 경우 : 현재 위치가 on인 경우에 현재위치사용, off이면 즐겨찾기에서 첫번째 주소선택
							즐겨찾기가 빈 경우 :  주소 추가 화면 표시
							 */
							
							if (lastSelectedFavoriteAddressId.equals(
									FavoriteAddressType.CurrentLocation.name()) || lastSelectedFavoriteAddressId.isEmpty()) {
								//현재 위치
								if (usingCurrentLocation) {
									weatherMainFragment.setWeatherFragment(FavoriteAddressType.CurrentLocation, null);
								} else {
									if (result.isEmpty()) {
										//주소 추가 화면 표시
										binding.sideNavMenu.favorites.callOnClick();
									} else {
										//즐겨찾기에서 첫번째 주소 선택
										weatherMainFragment.setWeatherFragment(FavoriteAddressType.SelectedAddress, result.get(0));
									}
								}
							} else {
								//즐겨찾기
								int selectedId = Integer.parseInt(lastSelectedFavoriteAddressId);
								FavoriteAddressDto selectedFavoriteAddressDto = null;
								
								for (FavoriteAddressDto favoriteAddressDto : result) {
									if (favoriteAddressDto.getId() == selectedId) {
										selectedFavoriteAddressDto = favoriteAddressDto;
										break;
									}
								}
								
								if (selectedFavoriteAddressDto != null) {
									//즐겨찾기에 있으므로 해당 주소선택
									weatherMainFragment.setWeatherFragment(FavoriteAddressType.SelectedAddress, selectedFavoriteAddressDto);
								} else {
									//즐겨찾기에 없는 경우
									if (usingCurrentLocation) {
										//현재위치사용
										weatherMainFragment.setWeatherFragment(FavoriteAddressType.CurrentLocation, null);
									} else {
										if (result.isEmpty()) {
											//주소 추가 화면 표시
											binding.sideNavMenu.favorites.callOnClick();
										} else {
											//즐겨찾기에서 첫번째 주소 선택
											weatherMainFragment.setWeatherFragment(FavoriteAddressType.SelectedAddress, result.get(0));
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
	
	private void addFavoriteLocationItemView(FavoriteAddressType favoriteAddressType, @Nullable FavoriteAddressDto favoriteAddressDto) {
		TextView locationItemView = (TextView) getLayoutInflater().inflate(R.layout.favorite_address_item_in_side_nav, null);
		locationItemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				weatherMainFragment.setWeatherFragment(favoriteAddressType, favoriteAddressDto);
				binding.drawerLayout.closeDrawer(binding.sideNavigation);
			}
		});
		
		if (favoriteAddressType == FavoriteAddressType.CurrentLocation) {
			locationItemView.setText(getString(R.string.current_location));
		} else {
			locationItemView.setText(favoriteAddressDto.getAddress());
			locationItemView.setTag(favDtoTagInFavLocItemView, favoriteAddressDto);
		}
		locationItemView.setTag(favTypeTagInFavLocItemView, favoriteAddressType);
		
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
		
		if (favoriteAddressType == FavoriteAddressType.CurrentLocation) {
			binding.sideNavMenu.favoriteAddressLayout.addView(locationItemView, 0, layoutParams);
		} else {
			binding.sideNavMenu.favoriteAddressLayout.addView(locationItemView, layoutParams);
		}
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