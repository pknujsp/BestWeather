package com.lifedawn.bestweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.ActivityMainBinding;
import com.lifedawn.bestweather.main.MainFragment;
import com.lifedawn.bestweather.test.TestFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		
		initPreferences();
		AccuWeatherResponseProcessor.init(getApplicationContext());
		AqicnResponseProcessor.init(getApplicationContext());
		KmaResponseProcessor.init(getApplicationContext());
		OpenWeatherMapResponseProcessor.init(getApplicationContext());
		
		MainFragment mainFragment = new MainFragment();
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(binding.fragmentContainer.getId(), mainFragment, mainFragment.getTag()).commit();
	}
	
	private void initPreferences() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		try {
			if (sharedPreferences.getAll().isEmpty()) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(getString(R.string.pref_key_app_theme), AppThemes.BLACK.name());
				editor.putBoolean(getString(R.string.pref_key_accu_weather), true);
				editor.putBoolean(getString(R.string.pref_key_open_weather_map), true);
				editor.putString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name());
				editor.putString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name());
				editor.putString(getString(R.string.pref_key_unit_wind), ValueUnits.mmPerSec.name());
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