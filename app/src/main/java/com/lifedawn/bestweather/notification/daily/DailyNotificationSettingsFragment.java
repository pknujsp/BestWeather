package com.lifedawn.bestweather.notification.daily;

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
import androidx.work.WorkManager;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.RemoteViews;
import android.widget.TimePicker;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.JsonDataSaver;
import com.lifedawn.bestweather.forremoteviews.WeatherDataRequest;
import com.lifedawn.bestweather.notification.BaseNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.NotificationReceiver;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;


public class DailyNotificationSettingsFragment extends BaseNotificationSettingsFragment {
	private DailyNotiViewCreator dailyNotiViewCreator;
	private boolean initializing = true;
	private ValueUnits clockUnit;
	private DateTimeFormatter dateTimeFormatter;
	private AlarmManager alarmManager;


	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationType = NotificationType.Daily;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		originalEnabled = sharedPreferences.getBoolean(notificationType.getPreferenceName(),
				false);
		clockUnit = ValueUnits.valueOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name()));
		dateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? getString(R.string.clock_12_pattern) :
				getString(R.string.clock_24_pattern));

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
				else if (key.equals(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name()))
					editor.putString(key, ((LocalTime) newValue).toString());

				editor.commit();
				dailyNotiViewCreator.onSharedPreferenceChanged(getContext().getSharedPreferences(notificationType.getPreferenceName(),
						Context.MODE_PRIVATE), key);
				return true;
			}
		};

		dailyNotiViewCreator = new DailyNotiViewCreator(getActivity().getApplicationContext());

		alarmManager =
				(AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
	}


	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.daily_notification);
		binding.notificationSwitch.setText(R.string.use_daily_notification);

		binding.commons.autoRefreshIntervalSpinner.setVisibility(View.GONE);
		binding.commons.autoRefreshIntervalLabel.setVisibility(View.GONE);

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
					dailyNotiViewCreator = null;
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

		binding.commons.alarmClock.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LocalTime localTime = LocalTime.parse(dailyNotiViewCreator.getAlarmClock());

				MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
				MaterialTimePicker timePicker =
						builder.setTitleText(R.string.alarmClock)
								.setTimeFormat(clockUnit == ValueUnits.clock24 ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
								.setHour(localTime.getHour())
								.setMinute(localTime.getMinute())
								.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
								.build();

				timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final int newHour = timePicker.getHour();
						final int newMinute = timePicker.getMinute();

						LocalTime newLocalTime = LocalTime.of(newHour, newMinute, 0);

						Preference preference = new Preference(getContext());
						preference.setKey(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name());
						onPreferenceChangeListener.onPreferenceChange(preference, newLocalTime);

						binding.commons.alarmClock.setText(newLocalTime.format(dateTimeFormatter));
						cancelAlarm();
						enqueueWork(newLocalTime.toString());
					}
				});
				timePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						timePicker.dismiss();
					}
				});
				timePicker.show(getChildFragmentManager(), MaterialTimePicker.class.getName());
			}
		});

		binding.notificationSwitch.setChecked(originalEnabled);
		SharedPreferences notiPreferences = getActivity().getApplicationContext().getSharedPreferences(notificationType.getPreferenceName(),
				Context.MODE_PRIVATE);

		if (originalEnabled) {
			dailyNotiViewCreator.loadPreferences();

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

		}

		JsonDataSaver jsonDataSaver = new JsonDataSaver();
		RemoteViews remoteViews = dailyNotiViewCreator.createRemoteViews();
		dailyNotiViewCreator.setHourlyForecastViews(remoteViews, jsonDataSaver.getTempHourlyForecastObjs(16));

		View previewWidgetView = remoteViews.apply(getActivity().getApplicationContext(), binding.previewLayout);
		binding.previewLayout.addView(previewWidgetView);

		initializing = false;
	}

	@Override
	public void onDestroy() {
		if (dailyNotiViewCreator != null && dailyNotiViewCreator.getLocationType() == null) {
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
	}

	@Override
	public void onSelectedCurrentLocation() {
	}

	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}


	@Override
	public void onCheckedKmaPriority(boolean checked) {
	}

	@Override
	public void onCheckedWeatherDataSource(WeatherSourceType weatherSourceType) {
	}

	@Override
	public void initPreferences() {
		SharedPreferences.Editor editor = getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE).edit();
		editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), null);
		editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), WeatherSourceType.OPEN_WEATHER_MAP.name());
		editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false);
		editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L);
		editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0);
		editor.putString(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name(), LocalTime.now().toString());

		editor.commit();
	}

	@Override
	public void onSelectedAutoRefreshInterval(long val) {

	}

	public void enqueueWork(String alarmClock) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		LocalTime localTime = LocalTime.parse(alarmClock);
		calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
		calendar.set(Calendar.MINUTE, localTime.getMinute());
		calendar.set(Calendar.SECOND, 0);

		Intent refreshIntent = new Intent(getContext(), NotificationReceiver.class);
		refreshIntent.setAction(getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		refreshIntent.putExtra(NotificationType.class.getName(), notificationType);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 20, refreshIntent, 0);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, pendingIntent);
	}

	private void cancelAlarm() {
		Intent refreshIntent = new Intent(getContext(), NotificationReceiver.class);
		refreshIntent.setAction(getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		refreshIntent.putExtra(NotificationType.class.getName(), notificationType);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 20, refreshIntent, 0);

		alarmManager.cancel(pendingIntent);
	}

	@Override
	public void onSwitchEnableNotification(boolean isChecked) {
		if (isChecked) {
			dailyNotiViewCreator = new DailyNotiViewCreator(getActivity().getApplicationContext());
			initializing = true;
			selectedFavoriteLocation = false;
			initPreferences();
			dailyNotiViewCreator.loadPreferences();

			binding.commons.locationRadioGroup.clearCheck();
			if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_key_accu_weather), true)) {
				binding.commons.accuWeatherRadio.setChecked(true);
			} else {
				binding.commons.owmRadio.setChecked(true);
			}
			binding.commons.selectedLocationRadio.setText(R.string.selected_location);
			binding.commons.changeAddressBtn.setVisibility(View.GONE);
			binding.commons.kmaTopPrioritySwitch.setChecked(false);

			binding.commons.alarmClock.setText(LocalTime.parse(dailyNotiViewCreator.getAlarmClock()).format(dateTimeFormatter));

			initializing = false;

			enqueueWork(dailyNotiViewCreator.getAlarmClock());
		} else {
			cancelAlarm();
		}
	}
}