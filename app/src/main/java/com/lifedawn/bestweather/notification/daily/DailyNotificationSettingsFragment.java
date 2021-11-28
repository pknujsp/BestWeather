package com.lifedawn.bestweather.notification.daily;

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
import com.lifedawn.bestweather.notification.BaseNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.NotiViewCreator;
import com.lifedawn.bestweather.notification.NotificationType;

import org.jetbrains.annotations.NotNull;


public class DailyNotificationSettingsFragment extends BaseNotificationSettingsFragment {

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationType = NotificationType.Daily;
	}


	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}


	@Override
	public void updateNotification() {

	}
}