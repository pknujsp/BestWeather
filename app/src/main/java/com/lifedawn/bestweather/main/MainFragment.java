package com.lifedawn.bestweather.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.CloseWindow;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.FavoriteAddressType;
import com.lifedawn.bestweather.databinding.FragmentMainBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.settings.fragments.SettingsMainFragment;
import com.lifedawn.bestweather.weathers.WeatherMainFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainFragment extends Fragment {
	private FragmentMainBinding binding;
	private WeatherViewModel weatherViewModel;
	private boolean initializing = true;
	
	private TextView currentLocationView;
	private WeatherMainFragment weatherMainFragment;
	private SharedPreferences sharedPreferences;
	
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
	
	@Override
	public void onAttach(@NonNull @NotNull Context context) {
		super.onAttach(context);
		requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		weatherViewModel.getAddAddressesLiveData().observe(this, new Observer<FavoriteAddressDto>() {
			@Override
			public void onChanged(FavoriteAddressDto favoriteAddressDto) {
				if (!initializing) {
				
				}
			}
		});
		
		weatherViewModel.getDeleteAddressesLiveData().observe(this, new Observer<FavoriteAddressDto>() {
			@Override
			public void onChanged(FavoriteAddressDto favoriteAddressDto) {
				if (!initializing) {
				
				}
			}
		});
		
		weatherViewModel.getCurrentLocationLiveData().observe(this, new Observer<String>() {
			@Override
			public void onChanged(String addressName) {
				if (!initializing) {
					String text = getString(R.string.current_location) + "\n" + addressName;
					currentLocationView.setText(text);
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
		
		weatherViewModel.getAll(new DbQueryCallback<List<FavoriteAddressDto>>() {
			@Override
			public void onResultSuccessful(List<FavoriteAddressDto> result) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							LayoutInflater layoutInflater = getLayoutInflater();
							final boolean usingCurrentLocation = sharedPreferences.getBoolean(
									getString(R.string.pref_key_use_current_location), false);
							
							binding.sideNavMenu.favoriteAddressLayout.removeAllViews();
							
							if (usingCurrentLocation) {
								currentLocationView = (TextView) layoutInflater.inflate(R.layout.favorite_address_item_in_side_nav, null);
								currentLocationView.setText(getString(R.string.current_location));
								currentLocationView.setTag(FavoriteAddressType.CurrentLocation);
								currentLocationView.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										weatherMainFragment.setWeatherFragment(FavoriteAddressType.CurrentLocation, null);
										binding.drawerLayout.closeDrawer(binding.sideNavigation);
									}
								});
								binding.sideNavMenu.favoriteAddressLayout.addView(currentLocationView);
							}
							
							for (FavoriteAddressDto favoriteAddressDto : result) {
								TextView favoriteAddressView = (TextView) layoutInflater.inflate(R.layout.favorite_address_item_in_side_nav,
										null);
								favoriteAddressView.setText(favoriteAddressDto.getAddress());
								favoriteAddressView.setTag(favoriteAddressDto);
								favoriteAddressView.setTag(FavoriteAddressType.SelectedAddress);
								favoriteAddressView.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										weatherMainFragment.setWeatherFragment(FavoriteAddressType.SelectedAddress, favoriteAddressDto);
										binding.drawerLayout.closeDrawer(binding.sideNavigation);
									}
								});
								binding.sideNavMenu.favoriteAddressLayout.addView(favoriteAddressView);
							}
							
							String lastSelectedFavoriteAddressId = sharedPreferences.getString(
									getString(R.string.pref_key_last_selected_favorite_address_id), "");
							
							if (lastSelectedFavoriteAddressId.equals("currentLocation")) {
								//현재 위치 사용
								weatherMainFragment.setWeatherFragment(FavoriteAddressType.CurrentLocation, null);
							} else if (lastSelectedFavoriteAddressId.isEmpty()) {
								//없음
								if (usingCurrentLocation) {
									currentLocationView.callOnClick();
								} else {
									// 즐겨찾기 추가 화면 표시
								}
							} else {
								//favorite address
								int id = Integer.parseInt(lastSelectedFavoriteAddressId);
								FavoriteAddressDto selectedFavoriteAddressDto = null;
								for (FavoriteAddressDto favoriteAddressDto : result) {
									if (id == favoriteAddressDto.getId()) {
										selectedFavoriteAddressDto = favoriteAddressDto;
										break;
									}
								}
								
								weatherMainFragment.setWeatherFragment(FavoriteAddressType.SelectedAddress, selectedFavoriteAddressDto);
							}
						}
					});
				}
			}
			
			@Override
			public void onResultNoData() {
			
			}
		});
		
		getChildFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), weatherMainFragment,
				getString(R.string.tag_weather_main_fragment)).commitNow();
		
		initializing = false;
	}
	
	@Override
	public void onDestroy() {
		onBackPressedCallback.remove();
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