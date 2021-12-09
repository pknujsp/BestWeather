package com.lifedawn.bestweather.notification.always;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.SystemClock;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.notification.BaseNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.NotificationReceiver;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.model.AlwaysNotiDataObj;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class AlwaysNotificationSettingsFragment extends BaseNotificationSettingsFragment {
	private AlwaysNotiViewCreator alwaysNotiViewCreator;
	private boolean initializing = true;
	private AlwaysNotiHelper alwaysNotiHelper;
	private AlwaysNotiDataObj alwaysNotiDataObj;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationType = NotificationType.Always;
		alwaysNotiViewCreator = new AlwaysNotiViewCreator(getActivity().getApplicationContext(), this);
		alwaysNotiHelper = new AlwaysNotiHelper(getActivity().getApplicationContext());

		initPreferences();
		alwaysNotiDataObj = alwaysNotiViewCreator.getNotificationDataObj();
		originalEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(notificationType.getPreferenceName(),
				false);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.always_notification);
		binding.notificationSwitch.setText(R.string.use_always_notification);
		binding.commons.alarmClock.setVisibility(View.GONE);
		binding.commons.alarmClockLabel.setVisibility(View.GONE);

		binding.notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onSwitchEnableNotification(isChecked);

				if (!initializing) {
					PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
							.putBoolean(notificationType.getPreferenceName(), isChecked).commit();
				}

			}
		});
		binding.notificationSwitch.setChecked(originalEnabled);
		onSwitchEnableNotification(originalEnabled);

		if (alwaysNotiDataObj.getLocationType() == LocationType.SelectedAddress) {
			selectedFavoriteLocation = true;
			binding.commons.selectedLocationRadio.setChecked(true);
			binding.commons.selectedAddressName.setText(alwaysNotiDataObj.getAddressName());
		} else {
			binding.commons.currentLocationRadio.setChecked(true);
		}

		WeatherSourceType defaultWeatherSourceType = alwaysNotiDataObj.getWeatherSourceType();
		if (defaultWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			binding.commons.owmRadio.setChecked(true);
		} else if (defaultWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			binding.commons.accuWeatherRadio.setChecked(true);
		} else {
			binding.commons.kmaTopPrioritySwitch.setChecked(true);
		}

		if (alwaysNotiDataObj.isTopPriorityKma()) {
			binding.commons.kmaTopPrioritySwitch.setChecked(true);
		}

		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		final long autoRefreshInterval = alwaysNotiDataObj.getUpdateIntervalMillis();

		for (int i = 0; i < intervalsStr.length; i++) {
			if (Long.parseLong(intervalsStr[i]) == autoRefreshInterval) {
				binding.commons.autoRefreshIntervalSpinner.setSelection(i);
				break;
			}
		}

		RemoteViews remoteViews = alwaysNotiViewCreator.createRemoteViews(true);
		View previewWidgetView = remoteViews.apply(getActivity().getApplicationContext(), binding.previewLayout);
		binding.previewLayout.addView(previewWidgetView);

		initializing = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onSelectedFavoriteLocation(FavoriteAddressDto favoriteAddressDto) {
		if (!initializing) {

			alwaysNotiDataObj.setAddressName(favoriteAddressDto.getAddress())
					.setCountryCode(favoriteAddressDto.getCountryCode())
					.setLatitude(Float.parseFloat(favoriteAddressDto.getLatitude())).setLongitude(Float.parseFloat(favoriteAddressDto.getLongitude()));
			alwaysNotiDataObj.setLocationType(LocationType.SelectedAddress);
			alwaysNotiViewCreator.savePreferences();
			alwaysNotiViewCreator.initNotification();
		}
	}

	@Override
	public void onSelectedCurrentLocation() {
		if (!initializing) {
			alwaysNotiDataObj.setLocationType(LocationType.CurrentLocation);
			alwaysNotiViewCreator.savePreferences();

			alwaysNotiViewCreator.initNotification();
		}
	}

	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}


	@Override
	public void onCheckedKmaPriority(boolean checked) {
		if (!initializing) {
			alwaysNotiDataObj.setTopPriorityKma(checked);
			alwaysNotiViewCreator.savePreferences();

			alwaysNotiViewCreator.initNotification();
		}
	}

	@Override
	public void onCheckedWeatherDataSource(WeatherSourceType weatherSourceType) {
		if (!initializing) {
			alwaysNotiDataObj.setWeatherSourceType(weatherSourceType);
			alwaysNotiViewCreator.savePreferences();

			alwaysNotiViewCreator.initNotification();
		}
	}

	@Override
	public void onSelectedAutoRefreshInterval(long val) {
		if (!initializing) {
			alwaysNotiDataObj.setUpdateIntervalMillis(val);
			alwaysNotiViewCreator.savePreferences();

			alwaysNotiHelper.onSelectedAutoRefreshInterval(val);
		}
	}

	@Override
	public void initPreferences() {
		alwaysNotiViewCreator.loadPreferences();
	}

	@Override
	public void onSwitchEnableNotification(boolean isChecked) {
		binding.settingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		if (!initializing) {
			if (isChecked) {
				alwaysNotiViewCreator.savePreferences();
				alwaysNotiViewCreator.initNotification();
				onSelectedAutoRefreshInterval(alwaysNotiDataObj.getUpdateIntervalMillis());
			} else {
				notificationHelper.cancelNotification(notificationType.getNotificationId());
				alwaysNotiHelper.cancelAutoRefresh();
			}
		}
	}


}