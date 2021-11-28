package com.lifedawn.bestweather.notification.always;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.BaseNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.NotificationType;

import org.jetbrains.annotations.NotNull;


public class AlwaysNotificationSettingsFragment extends BaseNotificationSettingsFragment {

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationType = NotificationType.Always;
	}


	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.notificationSwitch.setText(R.string.use_always_notification);
	}


	@Override
	public void updateNotification() {
		super.updateNotification();
	}
}