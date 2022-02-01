package com.lifedawn.bestweather.notification;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.AlarmListFragment;
import com.lifedawn.bestweather.databinding.FragmentNotificationBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.always.AlwaysNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.daily.fragment.DailyNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.daily.fragment.DailyPushNotificationListFragment;

import org.jetbrains.annotations.NotNull;


public class NotificationFragment extends Fragment {
	private FragmentNotificationBinding binding;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentNotificationBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.toolbar.fragmentTitle.setText(R.string.notification);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adViewBottom.loadAd(adRequest);

		binding.always.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlwaysNotificationSettingsFragment alwaysNotificationSettingsFragment = new AlwaysNotificationSettingsFragment();
				String tag = AlwaysNotificationSettingsFragment.class.getName();

				getParentFragmentManager().beginTransaction().hide(NotificationFragment.this).add(R.id.fragment_container,
						alwaysNotificationSettingsFragment,
						tag)
						.addToBackStack(tag).commit();
			}
		});

		binding.daily.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DailyPushNotificationListFragment listFragment = new DailyPushNotificationListFragment();
				String tag = DailyPushNotificationListFragment.class.getName();

				getParentFragmentManager().beginTransaction().hide(NotificationFragment.this).add(R.id.fragment_container,
						listFragment, tag)
						.addToBackStack(tag).commit();
			}
		});

		binding.alarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmListFragment alarmListFragment = new AlarmListFragment();
				String tag = AlarmListFragment.class.getName();

				getParentFragmentManager().beginTransaction().hide(NotificationFragment.this).add(R.id.fragment_container,
						alarmListFragment, tag)
						.addToBackStack(tag).commit();
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}