package com.lifedawn.bestweather.settings.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.interfaces.IAppbarTitle;
import com.lifedawn.bestweather.settings.custompreferences.UnitPreference;

import org.jetbrains.annotations.NotNull;

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
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		initPreferences();
	}

	private void initPreferences() {
		PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
		//기온
		tempPreference = new UnitPreference(getContext());
		tempPreference.setKey(getString(R.string.pref_key_unit_temp));
		tempPreference.setTitle(R.string.pref_title_unit_temp);
		tempPreference.setUnit(ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), "")));
		tempPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);

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
														.putString(tempPreference.getKey(), ValueUnits.celsius.name()).apply();
												tempPreference.setUnit(ValueUnits.celsius);
												break;
											//celsius
											case 1:
												sharedPreferences.edit()
														.putString(tempPreference.getKey(), ValueUnits.fahrenheit.name()).apply();
												tempPreference.setUnit(ValueUnits.fahrenheit);
												break;
											//fahrenheit
										}
										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		preferenceScreen.addPreference(tempPreference);

		//바람
		windPreference = new UnitPreference(getContext());
		windPreference.setKey(getString(R.string.pref_key_unit_wind));
		windPreference.setTitle(R.string.pref_title_unit_wind);
		windPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);
		windPreference.setUnit(ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), "")));

		windPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new MaterialAlertDialogBuilder(getActivity()).setTitle(getString(R.string.pref_title_unit_wind))
						.setSingleChoiceItems(getList(windPreference), getCheckedItem(ValueType.wind, windPreference.getKey()),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
											case 0:
												sharedPreferences.edit()
														.putString(windPreference.getKey(), ValueUnits.mmPerSec.name()).apply();
												windPreference.setUnit(ValueUnits.mmPerSec);
												break;
											//mmPerSec
											case 1:
												sharedPreferences.edit()
														.putString(windPreference.getKey(), ValueUnits.kmPerHour.name()).apply();
												windPreference.setUnit(ValueUnits.kmPerHour);
												break;
											//kmPerHour
										}
										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		preferenceScreen.addPreference(windPreference);

		//시정거리
		visibilityPreference = new UnitPreference(getContext());
		visibilityPreference.setKey(getString(R.string.pref_key_unit_visibility));
		visibilityPreference.setTitle(R.string.pref_title_unit_visibility);
		visibilityPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);
		visibilityPreference.setUnit(ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), "")));

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
														.putString(visibilityPreference.getKey(), ValueUnits.km.name()).apply();
												visibilityPreference.setUnit(ValueUnits.km);
												break;
											//km
											case 1:
												sharedPreferences.edit()
														.putString(visibilityPreference.getKey(), ValueUnits.mile.name()).apply();
												visibilityPreference.setUnit(ValueUnits.mile);
												break;
											//mile
										}
										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		preferenceScreen.addPreference(visibilityPreference);

		//시간제
		clockPreference = new UnitPreference(getContext());
		clockPreference.setKey(getString(R.string.pref_key_unit_clock));
		clockPreference.setTitle(R.string.pref_title_unit_clock);
		clockPreference.setWidgetLayoutResource(R.layout.custom_preference_layout);
		clockPreference.setUnit(ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), "")));

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
														.putString(clockPreference.getKey(), ValueUnits.clock12.name()).apply();
												clockPreference.setUnit(ValueUnits.clock12);
												break;
											//12
											case 1:
												sharedPreferences.edit()
														.putString(clockPreference.getKey(), ValueUnits.clock24.name()).apply();
												clockPreference.setUnit(ValueUnits.clock24);
												break;
											//24
										}
										dialog.dismiss();
									}
								}).create().show();
				return true;
			}
		});

		preferenceScreen.addPreference(clockPreference);
	}

	private int getCheckedItem(ValueType valueType, String key) {
		ValueUnits valueUnit = ValueUnits.enumOf(sharedPreferences.getString(key, ""));

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
					case mmPerSec:
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
			return new CharSequence[]{getString(R.string.mmPerSec), getString(R.string.kmPerHour)};
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