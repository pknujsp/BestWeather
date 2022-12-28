package com.lifedawn.bestweather.settings.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.settings.custompreferences.UnitPreference;

public class UnitsFragment extends PreferenceFragmentCompat {
	private UnitPreference tempPreference;
	private UnitPreference windPreference;
	private UnitPreference visibilityPreference;
	private UnitPreference clockPreference;
	private SharedPreferences sharedPreferences;

	enum ValueType {
		temp, wind, visibility, clock
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.unit_preference, rootKey);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
		initPreferences();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initPreferences() {
		//좌측 여백 없애기 :  app:iconSpaceReserved="false"
		PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
		//기온
		tempPreference = new UnitPreference(requireContext().getApplicationContext());
		tempPreference.setKey(getString(R.string.pref_key_unit_temp));
		tempPreference.setTitle(R.string.pref_title_unit_temp);
		tempPreference.setUnit(ValueUnits.valueOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), "")));
		tempPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);
		tempPreference.setIconSpaceReserved(false);

		tempPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new MaterialAlertDialogBuilder(getActivity()).setTitle(getString(R.string.pref_title_unit_temp))
						.setSingleChoiceItems(getList(tempPreference), getCheckedItem(ValueType.temp, tempPreference.getKey()),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
											case 0:
												sharedPreferences.edit()
														.putString(tempPreference.getKey(), ValueUnits.celsius.name()).commit();
												tempPreference.setUnit(ValueUnits.celsius);
												break;
											//celsius
											case 1:
												sharedPreferences.edit()
														.putString(tempPreference.getKey(), ValueUnits.fahrenheit.name()).commit();
												tempPreference.setUnit(ValueUnits.fahrenheit);
												break;
											//fahrenheit
										}
										MyApplication.loadValueUnits(requireContext().getApplicationContext(), true);

										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		preferenceScreen.addPreference(tempPreference);

		//바람
		windPreference = new UnitPreference(requireContext().getApplicationContext());
		windPreference.setKey(getString(R.string.pref_key_unit_wind));
		windPreference.setTitle(R.string.pref_title_unit_wind);
		windPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);
		windPreference.setUnit(ValueUnits.valueOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), "")));
		windPreference.setIconSpaceReserved(false);

		windPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new MaterialAlertDialogBuilder(requireActivity()).setTitle(getString(R.string.pref_title_unit_wind))
						.setSingleChoiceItems(getList(windPreference), getCheckedItem(ValueType.wind, windPreference.getKey()),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
											case 0:
												sharedPreferences.edit()
														.putString(windPreference.getKey(), ValueUnits.mPerSec.name()).commit();
												windPreference.setUnit(ValueUnits.mPerSec);
												break;
											//mmPerSec
											case 1:
												sharedPreferences.edit()
														.putString(windPreference.getKey(), ValueUnits.kmPerHour.name()).commit();
												windPreference.setUnit(ValueUnits.kmPerHour);
												break;
											//kmPerHour
										}
										MyApplication.loadValueUnits(requireContext().getApplicationContext(), true);

										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		preferenceScreen.addPreference(windPreference);

		//시정거리
		visibilityPreference = new UnitPreference(requireContext().getApplicationContext());
		visibilityPreference.setKey(getString(R.string.pref_key_unit_visibility));
		visibilityPreference.setTitle(R.string.pref_title_unit_visibility);
		visibilityPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);
		visibilityPreference.setUnit(ValueUnits.valueOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), "")));
		visibilityPreference.setIconSpaceReserved(false);

		visibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new MaterialAlertDialogBuilder(getActivity()).setTitle(getString(R.string.pref_title_unit_visibility))
						.setSingleChoiceItems(getList(visibilityPreference), getCheckedItem(ValueType.visibility, visibilityPreference.getKey()),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
											case 0:
												sharedPreferences.edit()
														.putString(visibilityPreference.getKey(), ValueUnits.km.name()).commit();
												visibilityPreference.setUnit(ValueUnits.km);
												break;
											//km
											case 1:
												sharedPreferences.edit()
														.putString(visibilityPreference.getKey(), ValueUnits.mile.name()).commit();
												visibilityPreference.setUnit(ValueUnits.mile);
												break;
											//mile
										}
										MyApplication.loadValueUnits(requireContext().getApplicationContext(), true);

										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		preferenceScreen.addPreference(visibilityPreference);

		//시간제
		clockPreference = new UnitPreference(requireContext().getApplicationContext());
		clockPreference.setKey(getString(R.string.pref_key_unit_clock));
		clockPreference.setTitle(R.string.pref_title_unit_clock);
		clockPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);
		clockPreference.setUnit(ValueUnits.valueOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), "")));
		clockPreference.setIconSpaceReserved(false);

		clockPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new MaterialAlertDialogBuilder(getActivity()).setTitle(clockPreference.getTitle())
						.setSingleChoiceItems(getList(clockPreference), getCheckedItem(ValueType.clock, clockPreference.getKey()),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
											case 0:
												sharedPreferences.edit()
														.putString(clockPreference.getKey(), ValueUnits.clock12.name()).commit();
												clockPreference.setUnit(ValueUnits.clock12);
												break;
											//12
											case 1:
												sharedPreferences.edit()
														.putString(clockPreference.getKey(), ValueUnits.clock24.name()).commit();
												clockPreference.setUnit(ValueUnits.clock24);
												break;
											//24
										}
										MyApplication.loadValueUnits(requireContext().getApplicationContext(), true);

										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		preferenceScreen.addPreference(clockPreference);
	}

	private int getCheckedItem(ValueType valueType, String key) {
		ValueUnits valueUnit = ValueUnits.valueOf(sharedPreferences.getString(key, ""));

		switch (valueType) {
			case temp:
				switch (valueUnit) {
					case celsius:
						return 0;
					case fahrenheit:
						return 1;
				}

			case wind:
				switch (valueUnit) {
					case mPerSec:
						return 0;
					case kmPerHour:
						return 1;
				}

			case visibility:
				switch (valueUnit) {
					case km:
						return 0;
					case mile:
						return 1;
				}

			case clock:
				switch (valueUnit) {
					case clock12:
						return 0;
					case clock24:
						return 1;
				}
		}
		return 0;
	}

	private CharSequence[] getList(Preference preference) {
		if (preference == tempPreference) {
			return new CharSequence[]{getString(R.string.celsius), getString(R.string.fahrenheit)};
		} else if (preference == windPreference) {
			return new CharSequence[]{getString(R.string.mPerSec), getString(R.string.kmPerHour)};
		} else if (preference == visibilityPreference) {
			return new CharSequence[]{getString(R.string.km), getString(R.string.mile)};
		} else {
			return new CharSequence[]{getString(R.string.clock12), getString(R.string.clock24)};
		}
	}

	@Override
	public void onViewCreated(View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
}