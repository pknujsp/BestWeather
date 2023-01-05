package com.lifedawn.bestweather.ui.main;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.splashscreen.SplashScreen;
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
import com.lifedawn.bestweather.data.remote.flickr.repository.FlickrRepository;
import com.lifedawn.bestweather.ui.intro.IntroTransactionFragment;
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationHelper;
import com.lifedawn.bestweather.ui.notification.daily.DailyNotificationHelper;
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationViewModel;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.ui.weathers.viewmodels.WeatherFragmentViewModel;
import com.lifedawn.bestweather.ui.widget.WidgetHelper;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private NetworkStatus networkStatus;
	private InitViewModel initViewModel;
	private OngoingNotificationViewModel ongoingNotificationViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		initViewModel = new ViewModelProvider(this).get(InitViewModel.class);
		final View content = findViewById(android.R.id.content);

		content.getViewTreeObserver().addOnPreDrawListener(
				new ViewTreeObserver.OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						// Check if the initial data is ready.
						if (initViewModel.ready) {
							// The content is ready; start drawing.
							content.getViewTreeObserver().removeOnPreDrawListener(this);
							return true;
						} else {
							// The content is not ready; suspend.
							return false;
						}
					}
				});

		Window window = getWindow();

		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(Color.TRANSPARENT);

		HeaderbarStyle.setStyle(HeaderbarStyle.Style.Black, this);

		networkStatus = NetworkStatus.getInstance(getApplicationContext());
		ongoingNotificationViewModel = new ViewModelProvider(this).get(OngoingNotificationViewModel.class);

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

	@Override
	protected void onDestroy() {
		FlickrRepository.clear();
		WeatherFragmentViewModel.clear();
		super.onDestroy();
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
			//initWidgets();


			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainer.getId(), mainTransactionFragment,
					MainTransactionFragment.class.getName()).commitNow();

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