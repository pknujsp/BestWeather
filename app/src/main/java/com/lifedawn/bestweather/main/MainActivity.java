package com.lifedawn.bestweather.main;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
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
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.room.repository.WidgetRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

	@Override
	protected void onStart() {
		super.onStart();
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
			initNotifications();
			initWidgets();

			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainer.getId(), mainTransactionFragment, MainTransactionFragment.class.getName()).commit();
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
		WidgetRepository widgetRepository = new WidgetRepository(getApplicationContext());
		widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
			@Override
			public void onResultSuccessful(List<WidgetDto> result) {
				if (result.size() > 0) {
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
					ArrayMap<Class<?>, List<Integer>> widgetArrMap = new ArrayMap<>();

					for (WidgetDto widgetDto : result) {
						final AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(widgetDto.getAppWidgetId());
						ComponentName componentName = appWidgetProviderInfo.provider;
						final String providerClassName = componentName.getClassName();
						try {
							Class<?> cls = Class.forName(providerClassName);

							if (!widgetArrMap.containsKey(cls)) {
								widgetArrMap.put(cls, new ArrayList<>());
							}
							widgetArrMap.get(cls).add(widgetDto.getAppWidgetId());
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							return;
						}
					}

					int requestCode = 100;
					for (Class<?> cls : widgetArrMap.keySet()) {
						Intent refreshIntent = null;
						try {
							refreshIntent = new Intent(getApplicationContext(), cls);
							refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

							Bundle bundle = new Bundle();
							List<Integer> idList = widgetArrMap.valueAt(widgetArrMap.indexOfKey(cls));
							int[] ids = new int[idList.size()];
							int index = 0;

							for (Integer id : idList) {
								ids[index++] = id;
							}
							bundle.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
							refreshIntent.putExtras(bundle);

							PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode++, refreshIntent,
									PendingIntent.FLAG_UPDATE_CURRENT);
							pendingIntent.send();
						} catch (PendingIntent.CanceledException e) {
							e.printStackTrace();
							return;
						}
					}


				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

}