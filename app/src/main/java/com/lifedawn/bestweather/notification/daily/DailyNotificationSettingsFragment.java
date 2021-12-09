package com.lifedawn.bestweather.notification.daily;

import android.content.Context;
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
import com.lifedawn.bestweather.notification.BaseNotificationSettingsFragment;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.model.DailyNotiDataObj;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class DailyNotificationSettingsFragment extends BaseNotificationSettingsFragment {
	private DailyNotiViewCreator dailyNotiViewCreator;
	private boolean initializing = true;
	private ValueUnits clockUnit;
	private DateTimeFormatter dateTimeFormatter;
	private DailyNotiHelper dailyNotiHelper;
	private DailyNotiDataObj dailyNotiDataObj;

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


		dailyNotiViewCreator = new DailyNotiViewCreator(getActivity().getApplicationContext(), null);
		initPreferences();
		dailyNotiDataObj = dailyNotiViewCreator.getNotificationDataObj();
		dailyNotiHelper = new DailyNotiHelper(getActivity().getApplicationContext());
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
				onSwitchEnableNotification(isChecked);

				if (!initializing) {
					PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
							.putBoolean(notificationType.getPreferenceName(), isChecked).commit();
				}

			}
		});

		binding.commons.alarmClock.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LocalTime localTime = LocalTime.parse(dailyNotiViewCreator.getNotificationDataObj().getAlarmClock());

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

						dailyNotiDataObj.setAlarmClock(newLocalTime.toString());
						dailyNotiViewCreator.savePreferences();

						binding.commons.alarmClock.setText(newLocalTime.format(dateTimeFormatter));
						dailyNotiHelper.cancelAlarm();
						dailyNotiHelper.setAlarm(newLocalTime.toString());
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
		onSwitchEnableNotification(originalEnabled);

		if (dailyNotiDataObj.getLocationType() == LocationType.SelectedAddress) {
			selectedFavoriteLocation = true;
			binding.commons.selectedLocationRadio.setChecked(true);
			binding.commons.selectedAddressName.setText(dailyNotiDataObj.getAddressName());
		} else {
			binding.commons.currentLocationRadio.setChecked(true);
		}

		WeatherSourceType defaultWeatherSourceType = dailyNotiDataObj.getWeatherSourceType();
		if (defaultWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			binding.commons.owmRadio.setChecked(true);
		} else if (defaultWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			binding.commons.accuWeatherRadio.setChecked(true);
		} else {
			binding.commons.kmaTopPrioritySwitch.setChecked(true);
		}

		if (dailyNotiDataObj.isTopPriorityKma()) {
			binding.commons.kmaTopPrioritySwitch.setChecked(true);
		}
		LocalTime localTime = LocalTime.parse(dailyNotiDataObj.getAlarmClock());
		binding.commons.alarmClock.setText(localTime.format(dateTimeFormatter));

		RemoteViews remoteViews = dailyNotiViewCreator.createRemoteViews(false);
		dailyNotiViewCreator.setTempHourlyForecastViews(remoteViews);

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

			dailyNotiDataObj.setAddressName(favoriteAddressDto.getAddress())
					.setCountryCode(favoriteAddressDto.getCountryCode())
					.setLatitude(Float.parseFloat(favoriteAddressDto.getLatitude())).setLongitude(Float.parseFloat(favoriteAddressDto.getLongitude()));
			dailyNotiDataObj.setLocationType(LocationType.SelectedAddress);
			dailyNotiViewCreator.savePreferences();
		}
	}

	@Override
	public void onSelectedCurrentLocation() {
		if (!initializing) {
			dailyNotiDataObj.setLocationType(LocationType.CurrentLocation);
			dailyNotiViewCreator.savePreferences();
		}
	}

	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}


	@Override
	public void onCheckedKmaPriority(boolean checked) {
		if (!initializing) {
			dailyNotiDataObj.setTopPriorityKma(checked);
			dailyNotiViewCreator.savePreferences();
		}
	}

	@Override
	public void onCheckedWeatherDataSource(WeatherSourceType weatherSourceType) {
		if (!initializing) {
			dailyNotiDataObj.setWeatherSourceType(weatherSourceType);
			dailyNotiViewCreator.savePreferences();
		}
	}

	@Override
	public void initPreferences() {
		dailyNotiViewCreator.loadPreferences();
	}

	@Override
	public void onSelectedAutoRefreshInterval(long val) {
		// 사용안함
	}


	@Override
	public void onSwitchEnableNotification(boolean isChecked) {
		binding.settingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);

		if (!initializing) {
			if (isChecked) {
				dailyNotiHelper.setAlarm(dailyNotiDataObj.getAlarmClock());
			} else {
				dailyNotiHelper.cancelAlarm();
			}
		}
	}
}