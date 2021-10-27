package com.lifedawn.bestweather.main;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.ActivityMainBinding;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.FlickrUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.UvIndexProcessor;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		initPreferences();
		WeatherResponseProcessor.init(getApplicationContext());
		AccuWeatherResponseProcessor.init(getApplicationContext());
		AqicnResponseProcessor.init(getApplicationContext());
		KmaResponseProcessor.init(getApplicationContext());
		OpenWeatherMapResponseProcessor.init(getApplicationContext());
		FlickrUtil.init(getApplicationContext());
		UvIndexProcessor.init(getApplicationContext());

		AppThemes appTheme = AppThemes.enumOf(sharedPreferences.getString(getString(R.string.pref_key_app_theme), AppThemes.BLACK.name()));
		if (appTheme == AppThemes.BLACK) {
			setTheme(R.style.AppTheme_White);
		} else {
			//	setTheme(R.style.AppTheme_White);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

		MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(binding.fragmentContainer.getId(), mainTransactionFragment, mainTransactionFragment.getTag()).commit();
	}

	private void initPreferences() {
		try {
			if (sharedPreferences.getAll().isEmpty()) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(getString(R.string.pref_key_app_theme), AppThemes.BLACK.name());
				editor.putBoolean(getString(R.string.pref_key_accu_weather), true);
				editor.putBoolean(getString(R.string.pref_key_open_weather_map), true);
				editor.putString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name());
				editor.putString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name());
				editor.putString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name());
				editor.putString(getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name());
				editor.putBoolean(getString(R.string.pref_key_use_current_location), true);

				Locale locale;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					locale = getResources().getConfiguration().getLocales().get(0);
				} else {
					locale = getResources().getConfiguration().locale;
				}
				String country = locale.getCountry();

				if (country.equals("KR")) {
					editor.putBoolean(getString(R.string.pref_key_kma_top_priority), true);
				} else {
					editor.putBoolean(getString(R.string.pref_key_kma_top_priority), false);
				}
				editor.putBoolean(getString(R.string.pref_key_accu_weather), true);
				editor.putBoolean(getString(R.string.pref_key_open_weather_map), false).apply();
			}
		} catch (NullPointerException e) {

		}
	}
}