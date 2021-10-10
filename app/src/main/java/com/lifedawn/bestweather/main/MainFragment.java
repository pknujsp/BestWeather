package com.lifedawn.bestweather.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.CloseWindow;
import com.lifedawn.bestweather.databinding.FragmentMainBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.settings.fragments.SettingsMainFragment;
import com.lifedawn.bestweather.weathers.WeatherMainFragment;

import org.jetbrains.annotations.NotNull;

public class MainFragment extends Fragment {
	private FragmentMainBinding binding;

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentMainBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.sideNavMenu.favorites.setOnClickListener(sideNavOnClickListener);
		binding.sideNavMenu.settings.setOnClickListener(sideNavOnClickListener);

		WeatherMainFragment weatherMainFragment = new WeatherMainFragment(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				binding.drawerLayout.openDrawer(binding.sideNavigation);
			}
		});

		getChildFragmentManager().beginTransaction()
				.add(binding.fragmentContainer.getId(), weatherMainFragment,
						getString(R.string.tag_weather_main_fragment)).commitNow();
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
					getChildFragmentManager().beginTransaction().hide(getChildFragmentManager().findFragmentByTag(getString(R.string.tag_weather_main_fragment)))
							.add(binding.fragmentContainer.getId(), favoritesFragment,
									getString(R.string.tag_favorites_fragment)).addToBackStack(getString(R.string.tag_favorites_fragment))
							.commit();
					break;
				case R.id.settings:
					SettingsMainFragment settingsMainFragment = new SettingsMainFragment();
					getChildFragmentManager().beginTransaction().hide(getChildFragmentManager().findFragmentByTag(getString(R.string.tag_weather_main_fragment)))
							.add(binding.fragmentContainer.getId(), settingsMainFragment,
									getString(R.string.tag_settings_main_fragment)).addToBackStack(getString(R.string.tag_settings_main_fragment))
							.commit();
					break;
			}
			binding.drawerLayout.closeDrawer(binding.sideNavigation);
		}
	};
}