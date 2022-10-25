package com.lifedawn.bestweather.main;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.views.HeaderbarStyle;
import com.lifedawn.bestweather.databinding.ActivityMainBinding;
import com.lifedawn.bestweather.intro.IntroTransactionFragment;
import com.lifedawn.bestweather.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationHelper;
import com.lifedawn.bestweather.notification.daily.DailyNotificationHelper;
import com.lifedawn.bestweather.notification.ongoing.OngoingNotificationViewModel;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.widget.WidgetHelper;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private NetworkStatus networkStatus;

	private OngoingNotificationViewModel ongoingNotificationViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(R.style.AppTheme_Black);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		Window window = getWindow();

		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(Color.TRANSPARENT);

		HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, this);

		networkStatus = NetworkStatus.getInstance(getApplicationContext());
		ongoingNotificationViewModel = new ViewModelProvider(this).get(OngoingNotificationViewModel.class);
	}

	@Override
	protected void onStart() {
		super.onStart();

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
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		if (sharedPreferences.getBoolean(getString(R.string.pref_key_show_intro), true)) {
			IntroTransactionFragment introTransactionFragment = new IntroTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainer.getId(), introTransactionFragment,
					IntroTransactionFragment.class.getName()).commitNow();
		} else {
			initOngoingNotifications();
			initDailyNotifications();
			initWidgets();

			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainer.getId(), mainTransactionFragment,
					MainTransactionFragment.class.getName()).commitNowAllowingStateLoss();
		}
	}

	private void initOngoingNotifications() {
		ongoingNotificationViewModel.getOngoingNotificationDto(new DbQueryCallback<OngoingNotificationDto>() {
			@Override
			public void onResultSuccessful(OngoingNotificationDto result) {
				if (result.isOn()) {
					//ongoing notification
					OngoingNotificationHelper helper = new OngoingNotificationHelper(getApplicationContext());
					PendingIntent pendingIntent = helper.createManualPendingIntent(getString(R.string.com_lifedawn_bestweather_action_RESTART),
							PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

					try {
						pendingIntent.send();
					} catch (PendingIntent.CanceledException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onResultNoData() {

			}
		});
	}

	public void initDailyNotifications() {
		DailyNotificationHelper notiHelper = new DailyNotificationHelper(getApplicationContext());
		notiHelper.reStartNotifications(null);
	}

	private void initWidgets() {
		WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
		widgetHelper.reDrawWidgets(null);
	}

}