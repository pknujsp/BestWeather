package com.lifedawn.bestweather.notification.daily;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.notification.BaseNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;


public class DailyNotificationSettingsFragment extends BaseNotificationSettingsFragment {

	@Override
	public void onSwitchEnableNotification(boolean isChecked) {

	}

	@Override
	public void initPreferences() {

	}

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
	public void onSelectedFavoriteLocation(FavoriteAddressDto favoriteAddressDto) {

	}

	@Override
	public void onSelectedCurrentLocation() {

	}


	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}

	@Override
	public void onSelectedAutoRefreshInterval(long val) {

	}

	@Override
	public void onCheckedKmaPriority(boolean checked) {

	}

	@Override
	public void onCheckedWeatherDataSource(WeatherSourceType weatherSourceType) {

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
}