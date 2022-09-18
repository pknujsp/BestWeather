package com.lifedawn.bestweather.main;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.commons.views.HeaderbarStyle;
import com.lifedawn.bestweather.databinding.ActivityMainBinding;
import com.lifedawn.bestweather.intro.IntroTransactionFragment;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationHelper;
import com.lifedawn.bestweather.notification.daily.DailyNotificationHelper;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.WidgetHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private NetworkStatus networkStatus;
	private SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		setTheme(R.style.AppTheme_Black);

		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		Window window = getWindow();

		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(Color.TRANSPARENT);

		HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, this);

		networkStatus = NetworkStatus.getInstance(getApplicationContext());

		if (networkStatus.networkAvailable()) {
			processNextStep();
		} else {
			new AlertDialog.Builder(this).setTitle(R.string.networkProblem)
					.setMessage(R.string.need_to_connect_network).setPositiveButton(R.string.check, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					}).setCancelable(false).create().show();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	private void processNextStep() {
		// 초기화
		MobileAds.initialize(this, new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) {
			}
		});

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

		if (sharedPreferences.getBoolean(getString(R.string.pref_key_show_intro), true)) {
			IntroTransactionFragment introTransactionFragment = new IntroTransactionFragment();
			fragmentTransaction.replace(binding.fragmentContainer.getId(), introTransactionFragment,
					IntroTransactionFragment.class.getName()).commitAllowingStateLoss();
		} else {
			initOngoingNotifications();
			initDailyNotifications();
			initWidgets();

			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.replace(binding.fragmentContainer.getId(), mainTransactionFragment,
					MainTransactionFragment.class.getName()).commitNowAllowingStateLoss();
		}
	}

	private void initOngoingNotifications() {
		//ongoing notification
		OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());
		ongoingNotificationHelper.reStartNotification(new Callback() {
			@Override
			public void onResult() {

			}
		});
	}

	public void initDailyNotifications() {
		DailyNotificationHelper notiHelper = new DailyNotificationHelper(getApplicationContext());
		notiHelper.reStartNotifications();
	}

	private void initWidgets() {
		WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
		widgetHelper.reDrawWidgets();
	}

}