package com.lifedawn.bestweather.notification.daily;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.RemoteViews;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.JsonDataSaver;
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

		initPreferences();

		dailyNotiViewCreator = new DailyNotiViewCreator(getActivity().getApplicationContext());
		dailyNotiViewCreator.loadPreferences();
		alarmManager =
				(AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

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

				if (!initializing) {
					PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
							.putBoolean(notificationType.getPreferenceName(), isChecked).commit();
					onSwitchEnableNotification(isChecked);
				}

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
						setAlarm(newLocalTime.toString());
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

		if (dailyNotiViewCreator.getLocationType() == LocationType.SelectedAddress) {
			selectedFavoriteLocation = true;
			binding.commons.selectedLocationRadio.setChecked(true);
			binding.commons.selectedAddressName.setText(dailyNotiViewCreator.getAddressName());
		} else {
			binding.commons.currentLocationRadio.setChecked(true);
		}

		WeatherSourceType defaultWeatherSourceType = dailyNotiViewCreator.getWeatherSourceType();
		if (defaultWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			binding.commons.owmRadio.setChecked(true);
		} else if (defaultWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			binding.commons.accuWeatherRadio.setChecked(true);
		} else {
			binding.commons.kmaTopPrioritySwitch.setChecked(true);
		}

		if (dailyNotiViewCreator.isKmaTopPriority()) {
			binding.commons.kmaTopPrioritySwitch.setChecked(true);
		}
		LocalTime localTime = LocalTime.parse(dailyNotiViewCreator.getAlarmClock());
		binding.commons.alarmClock.setText(localTime.format(dateTimeFormatter));

		JsonDataSaver jsonDataSaver = new JsonDataSaver();
		RemoteViews remoteViews = dailyNotiViewCreator.createRemoteViews();
		dailyNotiViewCreator.setHourlyForecastViews(remoteViews, jsonDataSaver.getTempHourlyForecastObjs(16));

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
		SharedPreferences sharedPreferences = getContext().getSharedPreferences(notificationType.getPreferenceName(),
				Context.MODE_PRIVATE);

		if (sharedPreferences.getAll().isEmpty()) {
			SharedPreferences.Editor editor = getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE).edit();
			editor.putString(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name(), LocationType.CurrentLocation.name());
			editor.putString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(), WeatherSourceType.OPEN_WEATHER_MAP.name());
			editor.putBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), false);
			editor.putLong(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name(), 0L);
			editor.putInt(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name(), 0);

			LocalTime localTime = LocalTime.of(8, 0);
			editor.putString(WidgetNotiConstants.DailyNotiAttributes.ALARM_CLOCK.name(), localTime.toString());

			editor.commit();
		}
	}

	@Override
	public void onSelectedAutoRefreshInterval(long val) {

	}

	public void setAlarm(String alarmClock) {
		LocalTime localTime = LocalTime.parse(alarmClock);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
		calendar.set(Calendar.MINUTE, localTime.getMinute());
		calendar.set(Calendar.SECOND, 0);

		Intent refreshIntent = new Intent(getContext(), NotificationReceiver.class);
		refreshIntent.setAction(getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putString(NotificationType.class.getName(), notificationType.name());

		refreshIntent.putExtras(bundle);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 20, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, pendingIntent);
	}

	private void cancelAlarm() {
		Intent refreshIntent = new Intent(getContext(), NotificationReceiver.class);
		refreshIntent.setAction(getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 20, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.cancel(pendingIntent);
	}

	@Override
	public void onSwitchEnableNotification(boolean isChecked) {
		if (isChecked) {
			setAlarm(dailyNotiViewCreator.getAlarmClock());
		} else {
			cancelAlarm();
		}
	}
}