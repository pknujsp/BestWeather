package com.lifedawn.bestweather.notification.always;

import android.content.Context;
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

import android.view.View;
import android.widget.CompoundButton;
import android.widget.RemoteViews;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.notification.BaseNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.NotificationType;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


public class AlwaysNotificationSettingsFragment extends BaseNotificationSettingsFragment {

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationType = NotificationType.Always;
		originalEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(notificationType.getPreferenceName(),
				false);

		onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences.Editor editor = getContext().getSharedPreferences(notificationType.getPreferenceName(),
						Context.MODE_PRIVATE).edit();
				String key = preference.getKey();

				if (key.equals(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name()))
					editor.putLong(key, (long) newValue);
				else if (key.equals(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name()))
					editor.putString(key, (String) newValue);
				else if (key.equals(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name()))
					editor.putBoolean(key, (boolean) newValue);
				else if (key.equals(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name()))
					editor.putInt(key, (int) newValue);
				else if (key.equals(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name()))
					editor.putString(key, (String) newValue);

				editor.apply();
				return true;
			}
		};
	}


	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.notificationSwitch.setText(R.string.use_always_notification);
		SharedPreferences notiPreferences = getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE);

		if (!originalEnabled) {
			binding.notificationSwitch.setChecked(false);
			binding.settingsLayout.setVisibility(View.GONE);
		} else {
			binding.notificationSwitch.setChecked(true);
			binding.settingsLayout.setVisibility(View.VISIBLE);

			if (notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), LocationType.SelectedAddress.name()).equals(LocationType.SelectedAddress.name())) {
				binding.selectedLocationRadio.setChecked(true);
				String text = getString(R.string.location) + ", " + notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name()
						, "");
				binding.selectedLocationRadio.setText(text);
				binding.changeAddressBtn.setVisibility(View.VISIBLE);
			} else {
				binding.currentLocationRadio.setChecked(true);
			}

			if (notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
					WeatherSourceType.OPEN_WEATHER_MAP.name()).equals(WeatherSourceType.OPEN_WEATHER_MAP.name())) {
				binding.owmRadio.setChecked(true);
			} else {
				binding.accuWeatherRadio.setChecked(true);
			}

			if (notiPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), true)) {
				binding.kmaTopPrioritySwitch.setChecked(true);
			}

			long autoRefreshInterval = notiPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L);
			final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);

			for (int i = 0; i < intervalsStr.length; i++) {
				if (Long.parseLong(intervalsStr[i]) == autoRefreshInterval) {
					binding.autoRefreshIntervalSpinner.setSelection(i);
					break;
				}
			}
		}

		alwaysNotiViewCreator = new AlwaysNotiViewCreator(getActivity().getApplicationContext(), this);
		notiPreferences.registerOnSharedPreferenceChangeListener(alwaysNotiViewCreator);
		alwaysNotiViewCreator.onSharedPreferenceChanged(notiPreferences, null);

		binding.notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.settingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
						.putBoolean(notificationType.getPreferenceName(), isChecked).apply();

				if (!isChecked) {
					notificationHelper.cancelNotification(notificationType.getNotificationId());
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						getContext().deleteSharedPreferences(notificationType.getPreferenceName());
					} else {
						getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE).edit().clear().apply();
					}
				} else {

				}

				onSwitchEnableNotification(isChecked);
			}
		});

		initLocation();
		initWeatherDataSource();
		initAutoRefreshInterval();
	}

	@Override
	public void onSelectedAddress() {
		alwaysNotiViewCreator.initNotification();
	}

	@Override
	public void onSelectedCurrentLocation() {
		alwaysNotiViewCreator.initNotification();
	}

	@Override
	public void updateNotification(RemoteViews remoteViews) {
		binding.previewLayout.removeAllViews();
		View previewWidgetView = remoteViews.apply(getActivity().getApplicationContext(), binding.previewLayout);
		binding.previewLayout.addView(previewWidgetView);
	}

	@Override
	public void updateNotification() {
		alwaysNotiViewCreator.initNotification();
	}

	@Override
	public void onSelectedAutoRefreshInterval(long val) {
		WorkManager workManager = WorkManager.getInstance(getActivity().getApplicationContext());
		workManager.cancelAllWorkByTag(notificationType.getPreferenceName());

		if (val == 0) {
			return;
		}

		WorkRequest refreshWeathersWorker =
				new PeriodicWorkRequest.Builder(AlwaysNotiWorker.class,
						val, TimeUnit.MILLISECONDS,
						5, TimeUnit.MINUTES)
						.addTag(notificationType.getPreferenceName())
						.build();
		workManager.enqueue(refreshWeathersWorker);
	}

	@Override
	public void initPreferences() {
		SharedPreferences.Editor editor = getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE).edit();
		editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), null);
		editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), WeatherSourceType.OPEN_WEATHER_MAP.name());
		editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false);
		editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L);
		editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0);

		editor.commit();
	}

	@Override
	public void onSwitchEnableNotification(boolean isChecked) {
		if (isChecked) {
			initPreferences();

			binding.currentLocationRadio.setChecked(false);
			binding.selectedLocationRadio.setChecked(false);
			if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_key_accu_weather), true)) {
				binding.accuWeatherRadio.setChecked(true);
			} else {
				binding.owmRadio.setChecked(true);
			}
			binding.kmaTopPrioritySwitch.setChecked(false);
			binding.autoRefreshIntervalSpinner.setSelection(0);

			alwaysNotiViewCreator.makeNotification(alwaysNotiViewCreator.createRemoteViews(true));
		} else {
			WorkManager workManager = WorkManager.getInstance(getActivity().getApplicationContext());
			workManager.cancelAllWorkByTag(notificationType.getPreferenceName());
		}
	}

}