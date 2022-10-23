package com.lifedawn.bestweather.settings.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.interfaces.IAppbarTitle;
import com.lifedawn.bestweather.main.MainActivity;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.settings.custompreferences.WidgetRefreshIntervalPreference;
import com.lifedawn.bestweather.widget.WidgetHelper;

import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat {
	private IAppbarTitle iAppbarTitle;

	private SharedPreferences sharedPreferences;
	private Preference unitsPreference;
	private Preference appThemePreference;
	private Preference weatherDataSourcesPreference;
	private WidgetRefreshIntervalPreference widgetRefreshIntervalPreference;
	private SwitchPreference useCurrentLocationPreference;
	private SwitchPreference animationPreference;
	private Preference redrawWidgetsPreference;

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
		animationPreference = findPreference(getString(R.string.pref_key_show_background_animation));
		redrawWidgetsPreference = findPreference(getString(R.string.pref_key_redraw_widgets));

		useCurrentLocationPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
		animationPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

		initPreferences();
	}

	private void initPreferences() {
		//위젯 업데이트
		widgetRefreshIntervalPreference = new WidgetRefreshIntervalPreference(getContext());
		widgetRefreshIntervalPreference.setKey(getString(R.string.pref_key_widget_refresh_interval));
		widgetRefreshIntervalPreference.setTitle(R.string.pref_title_widget_refresh_interval);
		widgetRefreshIntervalPreference.setValue(sharedPreferences.getLong(getString(R.string.pref_key_widget_refresh_interval), 0L));
		widgetRefreshIntervalPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);
		widgetRefreshIntervalPreference.setIconSpaceReserved(false);

		widgetRefreshIntervalPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new MaterialAlertDialogBuilder(getActivity()).setTitle(getString(R.string.pref_title_widget_refresh_interval))
						.setSingleChoiceItems(widgetRefreshIntervalPreference.getWidgetRefreshIntervalTexts(),
								widgetRefreshIntervalPreference.getCurrentValueIndex(),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										long newValue = widgetRefreshIntervalPreference
												.getWidgetRefreshIntervalLongValues()[which];
										sharedPreferences.edit()
												.putLong(widgetRefreshIntervalPreference.getKey(), newValue).commit();
										widgetRefreshIntervalPreference.setValue(newValue);

										WidgetRepository widgetRepository = WidgetRepository.getINSTANCE();
										widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
											@Override
											public void onResultSuccessful(List<WidgetDto> result) {
												WidgetHelper widgetHelper = new WidgetHelper(getContext());

												if (result.isEmpty()) {
													widgetHelper.cancelAutoRefresh();
												} else {
													widgetHelper.onSelectedAutoRefreshInterval(newValue);
												}
											}

											@Override
											public void onResultNoData() {

											}
										});


										MyApplication.loadValueUnits(getContext(), true);
										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
		preferenceScreen.addPreference(widgetRefreshIntervalPreference);


		//값 단위
		unitsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				iAppbarTitle.setAppbarTitle(getString(R.string.pref_title_value_units));

				UnitsFragment unitsFragment = new UnitsFragment();
				getParentFragmentManager().beginTransaction().hide(SettingsFragment.this).add(R.id.fragment_container, unitsFragment,
						getString(R.string.tag_units_fragment)).addToBackStack(getString(R.string.tag_units_fragment)).commit();
				return true;
			}
		});

		//앱 테마
		appThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AppThemes currentAppTheme = AppThemes.valueOf(sharedPreferences.getString(appThemePreference.getKey(), ""));
				int checkedItem = 0;

				CharSequence[] appThemes = new CharSequence[]{getString(R.string.black), getString(R.string.white)};
				final int finalCheckedItem = checkedItem;

				new MaterialAlertDialogBuilder(getActivity(), R.attr.alertDialogStyle).setTitle(
						getString(R.string.pref_title_app_theme)).setSingleChoiceItems(appThemes, checkedItem,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (finalCheckedItem != which) {
									switch (which) {
										case 0:
											sharedPreferences.edit().putString(appThemePreference.getKey(), AppThemes.BLACK.name()).apply();
											break;
										//검정
										default:
									}
									dialog.dismiss();
									getActivity().finish();
									startActivity(new Intent(getActivity(), MainActivity.class));
								}


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
				getParentFragmentManager().beginTransaction().hide(SettingsFragment.this).add(R.id.fragment_container,
						weatherSourcesFragment, getString(R.string.tag_weather_data_sources_fragment)).addToBackStack(
						getString(R.string.tag_weather_data_sources_fragment)).commit();
				return true;
			}
		});

		redrawWidgetsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				WidgetHelper widgetHelper = new WidgetHelper(getActivity());
				widgetHelper.reDrawWidgets(null);
				Toast.makeText(getContext(), R.string.pref_title_redraw_widgets, Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		//현재 위치 사용
		useCurrentLocationPreference.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_use_current_location), true));
		animationPreference.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_key_show_background_animation), true));
	}

	private Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference.getKey().equals(useCurrentLocationPreference.getKey())) {
				boolean enabled = (Boolean) newValue;

				sharedPreferences.edit().putString(getString(R.string.pref_key_last_current_location_latitude), "0.0").putString(
						getString(R.string.pref_key_last_current_location_longitude), "0.0").commit();

				if (enabled != useCurrentLocationPreference.isChecked()) {
					return true;
				} else {
					return false;
				}
			} else if (preference.getKey().equals(animationPreference.getKey())) {
				boolean enabled = (Boolean) newValue;

				sharedPreferences.edit().putBoolean(getString(R.string.pref_key_show_background_animation), enabled).commit();

				if (enabled != animationPreference.isChecked()) {
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