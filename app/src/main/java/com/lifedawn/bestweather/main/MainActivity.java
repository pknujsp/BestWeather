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
					introTransactionFragment.getTag()).commit();
		} else {
			initOngoingNotifications();
			initDailyNotifications();
			initWidgets();

			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.replace(binding.fragmentContainer.getId(), mainTransactionFragment, MainTransactionFragment.class.getName()).commit();
		}
	}

	private void initOngoingNotifications() {
		//ongoing notification
		OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());
		ongoingNotificationHelper.reStartNotification();
	}

	public void initDailyNotifications() {
		DailyNotificationHelper notiHelper = new DailyNotificationHelper(getApplicationContext());
		notiHelper.reStartNotifications();
	}

	private void initWidgets() {
		WidgetRepository widgetRepository = new WidgetRepository(getApplicationContext());
		widgetRepository.getAll(new DbQueryCallback<List<WidgetDto>>() {
			@Override
			public void onResultSuccessful(List<WidgetDto> result) {
				if (result.size() > 0) {
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
					ArrayMap<Class<?>, List<Integer>> widgetArrMap = new ArrayMap<>();
					Map<Integer, WidgetDto> widgetDtoMap = new HashMap<>();
					WidgetHelper widgetHelper = null;

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
							widgetDtoMap.put(widgetDto.getAppWidgetId(), widgetDto);
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

								if (widgetDtoMap.get(id).getUpdateIntervalMillis() > 0) {
									if (widgetHelper == null) {
										widgetHelper = new WidgetHelper(getApplicationContext(), cls);
									}
									if (!widgetHelper.isRepeating(id)) {
										widgetHelper.onSelectedAutoRefreshInterval(widgetDtoMap.get(id).getUpdateIntervalMillis(), id);
									}
								}
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