package com.lifedawn.bestweather.main;

import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.AppThemes;
import com.lifedawn.bestweather.databinding.ActivityMainBinding;
import com.lifedawn.bestweather.intro.IntroTransactionFragment;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.always.AlwaysNotiHelper;
import com.lifedawn.bestweather.notification.always.AlwaysNotiViewCreator;
import com.lifedawn.bestweather.room.repository.WidgetRepository;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private NetworkStatus networkStatus;
	private SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		AppThemes appTheme = AppThemes.valueOf(sharedPreferences.getString(getString(R.string.pref_key_app_theme), AppThemes.BLACK.name()));
		if (appTheme == AppThemes.BLACK) {
			setTheme(R.style.AppTheme_Black);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

		final Window window = getWindow();

		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(Color.TRANSPARENT);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

		final AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(R.string.networkProblem)
				.setMessage(R.string.need_to_connect_network).setPositiveButton(R.string.check, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				}).setCancelable(false).create();
		networkStatus = NetworkStatus.getInstance(getApplicationContext());

		if (networkStatus.networkAvailable()) {
			processNextStep();
		} else {
			alertDialog.show();
		}
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
			fragmentTransaction.add(binding.fragmentContainer.getId(), introTransactionFragment,
					introTransactionFragment.getTag()).commit();
		} else {
			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainer.getId(), mainTransactionFragment, MainTransactionFragment.class.getName()).commit();

			initNotifications();
			initWidgets();
		}
	}

	private void initNotifications() {
		//ongoing notification
		final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		final boolean enabledOngoingNotification = sharedPreferences.getBoolean(NotificationType.Always.getPreferenceName(), false);

		if (enabledOngoingNotification) {
			StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
			Boolean active = false;
			for (StatusBarNotification statusBarNotification : statusBarNotifications) {
				if (statusBarNotification.getId() == NotificationType.Always.getNotificationId()) {
					active = true;
					break;
				}
			}

			if (!active) {
				AlwaysNotiViewCreator alwaysNotiViewCreator = new AlwaysNotiViewCreator(getApplicationContext(), null);
				alwaysNotiViewCreator.loadPreferences();
				alwaysNotiViewCreator.initNotification(new Handler(new Handler.Callback() {
					@Override
					public boolean handleMessage(@NonNull Message msg) {
						return false;
					}
				}));
				AlwaysNotiHelper alwaysNotiHelper = new AlwaysNotiHelper(getApplicationContext());
				alwaysNotiHelper.onSelectedAutoRefreshInterval(alwaysNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis());
			}
			Log.e("Ongoing notification", active.toString());

		}

	}

	private void initWidgets() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(getComponentName());
		if (appWidgetIds.length > 0) {
			//WidgetRepository widgetRepository = new WidgetRepository(getApplicationContext());

		}
	}

}