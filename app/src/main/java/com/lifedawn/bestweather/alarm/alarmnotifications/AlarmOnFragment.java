package com.lifedawn.bestweather.alarm.alarmnotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.databinding.FragmentAlarmingBinding;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.repository.AlarmRepository;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class AlarmOnFragment extends Fragment {
	private FragmentAlarmingBinding binding;
	private AlarmDto alarmDto;
	private AlarmRepository alarmRepository;
	private Bundle bundle;

	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		alarmRepository = new AlarmRepository(getContext());
		bundle = getArguments();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAlarmingBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LocalDateTime now = LocalDateTime.now();
		binding.currentTime.setText(now.format(timeFormatter));

		getContext().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LocalDateTime now = LocalDateTime.now();
				binding.currentTime.setText(now.format(timeFormatter));
			}
		}, new IntentFilter(Intent.ACTION_TIME_TICK));

		alarmRepository.get(bundle.getInt(BundleKey.dtoId.name()), new DbQueryCallback<AlarmDto>() {
			@Override
			public void onResultSuccessful(AlarmDto result) {
				alarmDto = result;

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (alarmDto.getAddedLocation() == 1) {
							WeatherForAlarmFragment weatherForAlarmFragment = new WeatherForAlarmFragment();
							Bundle bundle = new Bundle();
							bundle.putSerializable(AlarmDto.class.getName(), alarmDto);
							weatherForAlarmFragment.setArguments(bundle);

							getChildFragmentManager().beginTransaction().
									add(binding.contentFragmentContainer.getId(), weatherForAlarmFragment,
											WeatherForAlarmFragment.class.getName()).commit();
						}

						final boolean repeat = alarmDto.getRepeat() == 1;
						binding.ringAgainBtn.setVisibility(repeat ? View.VISIBLE : View.INVISIBLE);

						if (repeat) {
							binding.ringAgainBtn.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {

								}
							});
						}


					}
				});
			}

			@Override
			public void onResultNoData() {

			}
		});

		binding.alarmEndsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});


	}
}