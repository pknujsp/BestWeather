package com.lifedawn.bestweather.notification.always;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentAlwaysNotificationSettingsBinding;

import org.jetbrains.annotations.NotNull;


public class AlwaysNotificationSettingsFragment extends Fragment {
	private FragmentAlwaysNotificationSettingsBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAlwaysNotificationSettingsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.always_notification);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		MobileAds.initialize(getContext());
		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adViewBottom.loadAd(adRequest);
	}
}