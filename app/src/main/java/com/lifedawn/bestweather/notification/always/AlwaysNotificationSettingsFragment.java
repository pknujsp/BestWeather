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
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


public class AlwaysNotificationSettingsFragment extends BaseNotificationSettingsFragment {
	private AlwaysNotiViewCreator alwaysNotiViewCreator;
	private boolean initializing = true;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationType = NotificationType.Always;
		originalEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(notificationType.getPreferenceName(),
				false);

		onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (initializing) {
					return false;
				}

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

				editor.commit();
				alwaysNotiViewCreator.onSharedPreferenceChanged(getContext().getSharedPreferences(notificationType.getPreferenceName(),
						Context.MODE_PRIVATE), key);
				return true;
			}
		};

		alwaysNotiViewCreator = new AlwaysNotiViewCreator(getActivity().getApplicationContext(), this);
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
				binding.settingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);

				if (initializing) {
					return;
				}
				PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
						.putBoolean(notificationType.getPreferenceName(), isChecked).commit();

				if (!isChecked) {
					alwaysNotiViewCreator = null;
					notificationHelper.cancelNotification(notificationType.getNotificationId());
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						getContext().deleteSharedPreferences(notificationType.getPreferenceName());
					} else {
						getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE).edit().clear().commit();
					}
				} else {

				}

				onSwitchEnableNotification(isChecked);
			}
		});
		binding.notificationSwitch.setChecked(originalEnabled);
		SharedPreferences notiPreferences = getActivity().getApplicationContext().getSharedPreferences(notificationType.getPreferenceName(),
				Context.MODE_PRIVATE);

		if (originalEnabled) {
			alwaysNotiViewCreator.loadPreferences();

			if (notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), LocationType.SelectedAddress.name()).equals(LocationType.SelectedAddress.name())) {
				selectedFavoriteLocation = true;
				binding.commons.selectedLocationRadio.setChecked(true);
				String text = getString(R.string.location) + ", " + notiPreferences.getString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name()
						, "");
				binding.commons.selectedLocationRadio.setText(text);
				binding.commons.changeAddressBtn.setVisibility(View.VISIBLE);
			} else {
				binding.commons.currentLocationRadio.setChecked(true);
			}

			WeatherSourceType defaultWeatherSourceType = WeatherSourceType.valueOf(notiPreferences.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
					WeatherSourceType.OPEN_WEATHER_MAP.name()));
			if (defaultWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
				binding.commons.owmRadio.setChecked(true);
			} else if (defaultWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
				binding.commons.accuWeatherRadio.setChecked(true);
			} else {
				binding.commons.kmaTopPrioritySwitch.setChecked(true);
			}

			if (notiPreferences.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), true)) {
				binding.commons.kmaTopPrioritySwitch.setChecked(true);
			}

			long autoRefreshInterval = notiPreferences.getLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L);
			final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);

			for (int i = 0; i < intervalsStr.length; i++) {
				if (Long.parseLong(intervalsStr[i]) == autoRefreshInterval) {
					binding.commons.autoRefreshIntervalSpinner.setSelection(i);
					break;
				}
			}
		}


		RemoteViews remoteViews = alwaysNotiViewCreator.createRemoteViews(true);
		View previewWidgetView = remoteViews.apply(getActivity().getApplicationContext(), binding.previewLayout);
		binding.previewLayout.addView(previewWidgetView);

		initializing = false;
	}

	@Override
	public void onDestroy() {
		if (alwaysNotiViewCreator != null && alwaysNotiViewCreator.getLocationType() == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				getContext().deleteSharedPreferences(notificationType.getPreferenceName());
			} else {
				getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE).edit().clear().commit();
			}
		}
		super.onDestroy();
	}

	@Override
	public void onSelectedFavoriteLocation(FavoriteAddressDto favoriteAddressDto) {
		if (!initializing) {
			alwaysNotiViewCreator.initNotification();
		}
	}

	@Override
	public void onSelectedCurrentLocation() {
		if (!initializing) {
			alwaysNotiViewCreator.initNotification();
		}
	}

	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}


	@Override
	public void onCheckedKmaPriority(boolean checked) {
		if (!initializing) {
			alwaysNotiViewCreator.initNotification();
		}
	}

	@Override
	public void onCheckedWeatherDataSource(WeatherSourceType weatherSourceType) {
		if (!initializing) {
			alwaysNotiViewCreator.initNotification();
		}
	}

	@Override
	public void onSelectedAutoRefreshInterval(long val) {
		if (!initializing) {

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
			alwaysNotiViewCreator = new AlwaysNotiViewCreator(getActivity().getApplicationContext(), this);
			initializing = true;
			selectedFavoriteLocation = false;
			initPreferences();
			alwaysNotiViewCreator.loadPreferences();

			binding.commons.locationRadioGroup.clearCheck();
			if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_key_accu_weather), true)) {
				binding.commons.accuWeatherRadio.setChecked(true);
			} else {
				binding.commons.owmRadio.setChecked(true);
			}
			binding.commons.selectedLocationRadio.setText(R.string.selected_location);
			binding.commons.changeAddressBtn.setVisibility(View.GONE);
			binding.commons.kmaTopPrioritySwitch.setChecked(false);
			binding.commons.autoRefreshIntervalSpinner.setSelection(0);

			alwaysNotiViewCreator.makeNotification(alwaysNotiViewCreator.createRemoteViews(true), R.drawable.temp_icon);
			initializing = false;
		} else {
			WorkManager workManager = WorkManager.getInstance(getActivity().getApplicationContext());
			workManager.cancelAllWorkByTag(notificationType.getPreferenceName());
		}
	}

}