package com.lifedawn.bestweather.alarm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.databinding.FragmentAlarmSettingsBinding;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;


public class AlarmSettingsFragment extends Fragment {
	private FragmentAlarmSettingsBinding binding;
	private AlarmDto savedAlarmDto;
	private AlarmDto newAlarmDto;

	private boolean newAlarmSession;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		newAlarmSession = getArguments().getBoolean(BundleKey.addAlarmSession.name());

		if (newAlarmSession) {
			newAlarmDto = new AlarmDto();
		} else {
			
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAlarmSettingsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.alarm);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});
		binding.hours.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String time = newAlarmSession ? newAlarmDto.getAlarmTime() : savedAlarmDto.getAlarmTime();
				LocalTime localTime = LocalTime.parse(time);

				MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
				MaterialTimePicker timePicker =
						builder.setTitleText(R.string.clock)
								.setTimeFormat(TimeFormat.CLOCK_12H)
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
						if (newAlarmSession) {
							newAlarmDto.setAlarmTime(newLocalTime.toString());
						} else {
							savedAlarmDto.setAlarmTime(newLocalTime.toString());
						}
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
	}
}