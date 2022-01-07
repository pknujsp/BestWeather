package com.lifedawn.bestweather.main;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.FlickrUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.UvIndexProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindUtil;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
	private SharedPreferences sharedPreferences;
	private static final ExecutorService executorService = Executors.newFixedThreadPool(3);
	private static int statusBarHeight;

	@Override
	public void onCreate() {
		super.onCreate();

		Context context = getApplicationContext();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		initPreferences();
		WindUtil.init(context);
		AccuWeatherResponseProcessor.init(context);
		AqicnResponseProcessor.init(context);
		KmaResponseProcessor.init(context);
		OpenWeatherMapResponseProcessor.init(context);
		FlickrUtil.init(context);
		UvIndexProcessor.init(context);

		int height = 0;
		int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (id > 0) {
			height = context.getResources().getDimensionPixelSize(id);
		}
		statusBarHeight = height;
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
				editor.putBoolean(getString(R.string.pref_key_never_ask_again_permission_for_access_location), false);
				editor.putBoolean(getString(R.string.pref_key_show_intro), true);

				Locale locale;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					locale = getResources().getConfiguration().getLocales().get(0);
				} else {
					locale = getResources().getConfiguration().locale;
				}

				editor.putBoolean(getString(R.string.pref_key_kma_top_priority), false).putBoolean(
						getString(R.string.pref_key_accu_weather), false).putBoolean(getString(R.string.pref_key_open_weather_map),
						true).apply();
			}
		} catch (NullPointerException e) {

		}
	}

	public static ExecutorService getExecutorService() {
		return executorService;
	}

	public static int getStatusBarHeight() {
		return statusBarHeight;
	}
}
