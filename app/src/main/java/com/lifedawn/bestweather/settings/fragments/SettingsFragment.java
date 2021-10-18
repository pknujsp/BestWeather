package com.lifedawn.bestweather.settings.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import android.preference.PreferenceFragment;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.interfaces.IAppbarTitle;

public class SettingsFragment extends PreferenceFragmentCompat {
	private IAppbarTitle iAppbarTitle;

	private SharedPreferences sharedPreferences;
	private Preference unitsPreference;
	private Preference appThemePreference;
	private Preference weatherDataSourcesPreference;
	private SwitchPreference useCurrentLocationPreference;

	public SettingsFragment(IAppbarTitle iAppbarTitle) {
		this.iAppbarTitle = iAppbarTitle;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.app_settings_main_preference, rootKey);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

			}
		});

		unitsPreference = findPreference(getString(R.string.pref_key_value_units));
		appThemePreference = findPreference(getString(R.string.pref_key_app_theme));
		weatherDataSourcesPreference = findPreference(getString(R.string.pref_key_weather_data_sources));
		useCurrentLocationPreference = findPreference(getString(R.string.pref_key_use_current_location));

		useCurrentLocationPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

		initPreferences();
	}

	private void initPreferences() {
		//값 단위
		unitsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				iAppbarTitle.setAppbarTitle(getString(R.string.pref_title_value_units));

				UnitsFragment unitsFragment = new UnitsFragment();
				getParentFragmentManager().beginTransaction().hide(SettingsFragment.this)
						.add(R.id.fragment_container, unitsFragment, getString(R.string.tag_units_fragment))
						.addToBackStack(getString(R.string.tag_units_fragment)).commit();
				return true;
			}
		});

		//앱 테마
		appThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AppThemes currentAppTheme = AppThemes.enumOf(sharedPreferences.getString(appThemePreference.getKey(), ""));
				int checkedItem = 0;
				if (currentAppTheme == AppThemes.WHITE) {
					checkedItem = 1;
				}
				CharSequence[] appThemes = new CharSequence[]{getString(R.string.black), getString(R.string.white)};

				new MaterialAlertDialogBuilder(getActivity()).setTitle(getString(R.string.pref_title_app_theme))
						.setSingleChoiceItems(appThemes, checkedItem, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case 0:
										sharedPreferences.edit()
												.putString(appThemePreference.getKey(), AppThemes.BLACK.name()).apply();
										break;
									//검정
									case 1:
										sharedPreferences.edit()
												.putString(appThemePreference.getKey(), AppThemes.WHITE.name()).apply();
										break;
									//하양
								}
								dialog.dismiss();
							}
						}).create().show();
				return true;
			}
		});

		//날씨 제공사
		weatherDataSourcesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				iAppbarTitle.setAppbarTitle(getString(R.string.pref_title_weather_data_sources));

				WeatherSourcesFragment weatherSourcesFragment = new WeatherSourcesFragment();
				getParentFragmentManager().beginTransaction().hide(SettingsFragment.this)
						.add(R.id.fragment_container, weatherSourcesFragment, getString(R.string.tag_weather_data_sources_fragment))
						.addToBackStack(getString(R.string.tag_weather_data_sources_fragment)).commit();
				return true;
			}
		});

		//현재 위치 사용
		useCurrentLocationPreference.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), true));
	}

	private Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference.getKey().equals(useCurrentLocationPreference.getKey())) {
				boolean enabled = (Boolean) newValue;

				sharedPreferences.edit().remove(getString(R.string.pref_key_last_current_location_latitude))
						.remove(getString(R.string.pref_key_last_current_location_longitude)).apply();

				if (enabled != useCurrentLocationPreference.isChecked()) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		}
	};

	@Override
	public void onViewCreated(View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			iAppbarTitle.setAppbarTitle(getString(R.string.settings));
		}
	}

}