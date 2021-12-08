package com.lifedawn.bestweather.alarm.alarmnotifications;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.AlarmReceiver;
import com.lifedawn.bestweather.alarm.AlarmUtil;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.databinding.FragmentAlarmingBinding;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.repository.AlarmRepository;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class AlarmOnFragment extends Fragment implements OnEndListener {
	private FragmentAlarmingBinding binding;
	private AlarmDto alarmDto;
	private AlarmRepository alarmRepository;
	private Bundle bundle;


	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm");
	private final DateTimeFormatter amPmFormatter = DateTimeFormatter.ofPattern("a");

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
		binding.ampm.setText(now.format(amPmFormatter));
		binding.currentTime.setText(now.format(timeFormatter));

		getContext().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LocalDateTime now = LocalDateTime.now();
				binding.ampm.setText(now.format(amPmFormatter));
				binding.currentTime.setText(now.format(timeFormatter));
			}
		}, new IntentFilter(Intent.ACTION_TIME_TICK));

		alarmRepository.get(bundle.getInt(BundleKey.dtoId.name()), new DbQueryCallback<AlarmDto>() {
			@Override
			public void onResultSuccessful(AlarmDto result) {
				alarmDto = result;

				MainThreadWorker.runOnUiThread(new Runnable() {
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

						getContext().getSharedPreferences(RepeatAlarmConstants.repeatAlarmLog.name(), Context.MODE_PRIVATE);
						if (repeat) {
							if (AlarmUtil.getRepeatCount(getContext(), alarmDto.getId()) >= alarmDto.getRepeatCount()) {
								binding.ringAgainBtn.setVisibility(View.INVISIBLE);
							} else {
								String text = alarmDto.getRepeatInterval() + getString(R.string.againAlarmAfterNMinutes);
								binding.ringAgainBtn.setText(text);
								binding.ringAgainBtn.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										PendingIntent pendingIntent = AlarmReceiver.getRingAgainPendingIntent(getContext(), alarmDto.getId());
										try {
											pendingIntent.send();
										} catch (PendingIntent.CanceledException e) {
											e.printStackTrace();
										}

										end();
									}
								});

								binding.ringAgainBtn.setVisibility(View.VISIBLE);
							}
						} else {
							binding.ringAgainBtn.setVisibility(View.INVISIBLE);
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
				end();
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	public void end() {
		PendingIntent pendingIntent = AlarmReceiver.getEndAlarmPendingIntent(getContext(), alarmDto.getId());
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}


}