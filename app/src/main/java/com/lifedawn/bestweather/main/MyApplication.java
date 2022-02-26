package com.lifedawn.bestweather.main;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
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
	public final static ValueUnitObj VALUE_UNIT_OBJ = new ValueUnitObj();
	private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
	private static int statusBarHeight;
	private static String localeCountryCode;

	@Override
	public void onCreate() {
		super.onCreate();
		Context context = getApplicationContext();

		final int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (id > 0) {
			statusBarHeight = getResources().getDimensionPixelSize(id);
		}

		Locale locale = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = getResources().getConfiguration().getLocales().get(0);
		} else {
			locale = getResources().getConfiguration().locale;
		}
		localeCountryCode = locale.getCountry();

		initPreferences();
		WindUtil.init(context);
		//AccuWeatherResponseProcessor.init(context);
		AqicnResponseProcessor.init(context);
		KmaResponseProcessor.init(context);
		OpenWeatherMapResponseProcessor.init(context);
		FlickrUtil.init(context);
		UvIndexProcessor.init(context);
	}


	private void initPreferences() {
		try {
			if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getAll().isEmpty()) {
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
				editor.putString(getString(R.string.pref_key_app_theme), AppThemes.BLACK.name());
				editor.putBoolean(getString(R.string.pref_key_accu_weather), false);
				editor.putBoolean(getString(R.string.pref_key_open_weather_map), true);
				editor.putString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name());
				editor.putString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name());
				editor.putString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name());
				editor.putString(getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name());
				editor.putBoolean(getString(R.string.pref_key_use_current_location), true);
				editor.putBoolean(getString(R.string.pref_key_never_ask_again_permission_for_access_location), false);
				editor.putBoolean(getString(R.string.pref_key_show_intro), true);
				editor.putBoolean(getString(R.string.pref_key_kma_top_priority), false).putBoolean(
						getString(R.string.pref_key_accu_weather), false).putBoolean(getString(R.string.pref_key_open_weather_map),
						true).commit();
			}

			loadValueUnits(getApplicationContext());
		} catch (NullPointerException e) {

		}
	}

	public static ExecutorService getExecutorService() {
		return executorService;
	}

	public static int getStatusBarHeight() {
		return statusBarHeight;
	}

	public static String getLocaleCountryCode() {
		return localeCountryCode;
	}

	public static void loadValueUnits(Context context) {
		if (VALUE_UNIT_OBJ.getTempUnit() == null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

			VALUE_UNIT_OBJ.setTempUnit(ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
					ValueUnits.celsius.name()))).setWindUnit(ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
					ValueUnits.mPerSec.name()))).setVisibilityUnit(ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
					ValueUnits.km.name()))).setClockUnit(ValueUnits.valueOf(
					sharedPreferences.getString(context.getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name())));
		}
	}

	public static class ValueUnitObj {
		private ValueUnits tempUnit;
		private String tempUnitText;

		private ValueUnits windUnit;
		private String windUnitText;

		private ValueUnits visibilityUnit;
		private String visibilityUnitText;

		private ValueUnits clockUnit;

		public ValueUnits getTempUnit() {
			return tempUnit;
		}

		public ValueUnitObj setTempUnit(ValueUnits tempUnit) {
			this.tempUnit = tempUnit;
			tempUnitText = ValueUnits.toString(tempUnit);
			return this;
		}

		public ValueUnits getWindUnit() {
			return windUnit;
		}

		public ValueUnitObj setWindUnit(ValueUnits windUnit) {
			this.windUnit = windUnit;
			windUnitText = ValueUnits.toString(windUnit);
			return this;
		}

		public ValueUnits getVisibilityUnit() {
			return visibilityUnit;
		}

		public ValueUnitObj setVisibilityUnit(ValueUnits visibilityUnit) {
			this.visibilityUnit = visibilityUnit;
			visibilityUnitText = ValueUnits.toString(visibilityUnit);
			return this;
		}

		public ValueUnits getClockUnit() {
			return clockUnit;
		}

		public ValueUnitObj setClockUnit(ValueUnits clockUnit) {
			this.clockUnit = clockUnit;
			return this;
		}

		public String getTempUnitText() {
			return tempUnitText;
		}

		public String getWindUnitText() {
			return windUnitText;
		}

		public String getVisibilityUnitText() {
			return visibilityUnitText;
		}
	}
}
