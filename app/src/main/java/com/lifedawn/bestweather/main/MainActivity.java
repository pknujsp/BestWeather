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
import androidx.lifecycle.Observer;
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
	private NetworkStatus networkStatus;
	private SharedPreferences sharedPreferences;

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

		networkStatus = NetworkStatus.getInstance(getApplicationContext());
		if(!networkStatus.networkAvailable()){
			finish();
		}

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


}