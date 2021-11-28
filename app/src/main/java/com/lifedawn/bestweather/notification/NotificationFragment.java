package com.lifedawn.bestweather.notification;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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


public class NotificationFragment extends Fragment {
	private FragmentNotificationBinding binding;

	private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentAttached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @NonNull @NotNull Context context) {
			super.onFragmentAttached(fm, f, context);
			binding.rootScrollView.setVisibility(View.GONE);
			binding.fragmentContainer.setVisibility(View.VISIBLE);
		}

		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
			if (f instanceof AlwaysNotificationSettingsFragment) {
				binding.toolbar.fragmentTitle.setText(R.string.always_notification);
			} else if (f instanceof AlwaysNotificationSettingsFragment) {
				binding.toolbar.fragmentTitle.setText(R.string.daily_notification);
			}
		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);
			binding.toolbar.fragmentTitle.setText(R.string.notification);
			binding.rootScrollView.setVisibility(View.VISIBLE);
			binding.fragmentContainer.setVisibility(View.GONE);
		}
	};

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
		binding.rootScrollView.setVisibility(View.VISIBLE);
		binding.fragmentContainer.setVisibility(View.GONE);
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
						.hide(NotificationFragment.this).add(R.id.fragment_container, alwaysNotificationSettingsFragment, tag)
						.addToBackStack(tag).commit();
			}
		});

		binding.daily.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DailyNotificationSettingsFragment dailyNotificationSettingsFragment = new DailyNotificationSettingsFragment();
				String tag = DailyNotificationSettingsFragment.class.getName();

				getParentFragmentManager().beginTransaction()
						.hide(NotificationFragment.this).add(R.id.fragment_container, dailyNotificationSettingsFragment, tag)
						.addToBackStack(tag).commit();
			}
		});
	}
}