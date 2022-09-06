package com.lifedawn.bestweather.main;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindUtil;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
	public final static ValueUnitObj VALUE_UNIT_OBJ = new ValueUnitObj();
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(6);
	private static int statusBarHeight;
	private static String localeCountryCode;

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		initPreferences();
		WindUtil.init(getApplicationContext());

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
		//UvIndexProcessor.init(context);
		//AccuWeatherResponseProcessor.init(context);
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
				editor.putLong(getString(R.string.pref_key_widget_refresh_interval), 0);
				editor.putBoolean(getString(R.string.pref_key_use_current_location), true);
				editor.putBoolean(getString(R.string.pref_key_never_ask_again_permission_for_access_location), false);
				editor.putBoolean(getString(R.string.pref_key_sun_rise_notification), false);
				editor.putBoolean(getString(R.string.pref_key_sun_set_notification), false);
				editor.putBoolean(getString(R.string.pref_key_show_background_animation), true);
				editor.putBoolean(getString(R.string.pref_key_show_intro), true);
				editor.putBoolean(getString(R.string.pref_key_kma_top_priority), true).putBoolean(
								getString(R.string.pref_key_accu_weather), false).putBoolean(getString(R.string.pref_key_open_weather_map),
								false)
						.putBoolean(getString(R.string.pref_key_met),
								true).commit();
			}

			loadValueUnits(getApplicationContext(), false);
		} catch (NullPointerException e) {

		}
	}

	public static ExecutorService getExecutorService() {
		return EXECUTOR_SERVICE;
	}

	public static int getStatusBarHeight() {
		return statusBarHeight;
	}

	public static String getLocaleCountryCode() {
		return localeCountryCode;
	}

	public static void loadValueUnits(Context context, boolean force) {
		if (VALUE_UNIT_OBJ.getTempUnit() == null || force) {
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
