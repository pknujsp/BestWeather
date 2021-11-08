package com.lifedawn.bestweather.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.ActivityMainBinding;
import com.lifedawn.bestweather.intro.IntroTransactionFragment;
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
	private NetworkStatus networkStatus;

	public static void setWindowFlag(Activity activity, final int bits, boolean on) {
		Window win = activity.getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	public static int getHeightOfStatusBar(Context context) {
		int height = 0;
		int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (id > 0) {
			height = context.getResources().getDimensionPixelSize(id);
		}
		return height;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		networkStatus = new NetworkStatus(getApplicationContext(), new ConnectivityManager.NetworkCallback() {
		});
		if (!networkStatus.networkAvailable()) {
			Toast.makeText(this, getString(R.string.need_to_connect_network), Toast.LENGTH_SHORT).show();
			finish();
		}

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
			setTheme(R.style.AppTheme_Black);
		} else {
			setTheme(R.style.AppTheme_White);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

		// 초기화
		MobileAds.initialize(this, new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) {
			}
		});

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

		if (sharedPreferences.getBoolean(getString(R.string.pref_key_show_intro), true)) {
			IntroTransactionFragment introTransactionFragment = new IntroTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainer.getId(), introTransactionFragment,
					introTransactionFragment.getTag()).commit();
		} else {
			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainer.getId(), mainTransactionFragment, mainTransactionFragment.getTag()).commit();
		}

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
				editor.putBoolean(getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false);
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
}