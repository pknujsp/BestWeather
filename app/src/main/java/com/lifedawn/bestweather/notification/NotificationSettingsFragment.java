package com.lifedawn.bestweather.notification;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentNotificationBinding;
import com.lifedawn.bestweather.notification.always.AlwaysNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.daily.DailyNotificationSettingsFragment;

import org.jetbrains.annotations.NotNull;


public class NotificationSettingsFragment extends Fragment {
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
		binding.toolbar.fragmentTitle.setText(R.string.notification);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		MobileAds.initialize(getContext());
		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adViewBottom.loadAd(adRequest);

		binding.always.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlwaysNotificationSettingsFragment alwaysNotificationSettingsFragment = new AlwaysNotificationSettingsFragment();
				String tag = AlwaysNotificationSettingsFragment.class.getName();

				getParentFragmentManager().beginTransaction()
						.hide(NotificationSettingsFragment.this).add(R.id.fragment_container, alwaysNotificationSettingsFragment, tag)
						.addToBackStack(tag).commit();
			}
		});

		binding.daily.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DailyNotificationSettingsFragment dailyNotificationSettingsFragment = new DailyNotificationSettingsFragment();
				String tag = DailyNotificationSettingsFragment.class.getName();

				getParentFragmentManager().beginTransaction()
						.hide(NotificationSettingsFragment.this).add(R.id.fragment_container, dailyNotificationSettingsFragment, tag)
						.addToBackStack(tag).commit();
			}
		});
	}
}