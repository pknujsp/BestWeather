package com.lifedawn.bestweather.alarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentAlarmSettingsBinding;
import com.lifedawn.bestweather.databinding.ViewRepeatAlarmSettingsBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.AlarmRepository;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Set;


public class AlarmSettingsFragment extends Fragment {
	private FragmentAlarmSettingsBinding binding;
	private boolean initializing = true;
	private AlarmDto savedAlarmDto;
	private AlarmDto newAlarmDto;
	private AlarmRepository alarmRepository;

	private boolean newAlarmSession;
	private boolean selectedFavoriteLocation = false;
	private Bundle bundle;
	private Ringtone ringtone;
	private final DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a hh:mm");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		alarmRepository = new AlarmRepository(getContext());
		bundle = getArguments() != null ? getArguments() : savedInstanceState;
		newAlarmSession = bundle.getBoolean(BundleKey.addAlarmSession.name());

		if (newAlarmSession) {
			newAlarmDto = new AlarmDto();
			newAlarmDto.setEnabled(1);

			Uri defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			ringtone = RingtoneManager.getRingtone(getContext(), defaultAlarmUri);

			newAlarmDto.setEnableSound(1);
			newAlarmDto.setAlarmSoundVolume(100);
			newAlarmDto.setAlarmSoundUri(defaultAlarmUri.toString());

			newAlarmDto.setRepeat(0);
			newAlarmDto.setAlarmVibration(1);
			newAlarmDto.setAlarmTime(LocalTime.of(7, 0).toString());
			newAlarmDto.setAlarmDays("");
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
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

						binding.hours.setText(newLocalTime.format(hoursFormatter));
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

		initDaysCheckBoxes();

		binding.location.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FavoritesFragment favoritesFragment = new FavoritesFragment();
				favoritesFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
					@Override
					public void onResultFragment(Bundle result) {
						if (result.getSerializable(BundleKey.SelectedAddressDto.name()) == null) {
							if (!selectedFavoriteLocation) {
								Toast.makeText(getContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();

								if (newAlarmSession) {
									newAlarmDto.setAddedLocation(0);
								} else {
									savedAlarmDto.setAddedLocation(0);
								}
							}
						} else {
							selectedFavoriteLocation = true;

							FavoriteAddressDto newSelectedAddressDto =
									(FavoriteAddressDto) result.getSerializable(BundleKey.SelectedAddressDto.name());
							binding.location.setText(newSelectedAddressDto.getAddress());

							if (newAlarmSession) {
								newAlarmDto.setAddedLocation(1);
								newAlarmDto.setLocationAddressName(newSelectedAddressDto.getAddress());
								newAlarmDto.setLocationLatitude(newSelectedAddressDto.getLatitude());
								newAlarmDto.setLocationLongitude(newSelectedAddressDto.getLongitude());
								newAlarmDto.setLocationCountryCode(newSelectedAddressDto.getCountryCode());
								newAlarmDto.setLocationCountryName(newSelectedAddressDto.getCountryName());
							} else {
								savedAlarmDto.setAddedLocation(1);
								savedAlarmDto.setLocationAddressName(newSelectedAddressDto.getAddress());
								savedAlarmDto.setLocationLatitude(newSelectedAddressDto.getLatitude());
								savedAlarmDto.setLocationLongitude(newSelectedAddressDto.getLongitude());
								savedAlarmDto.setLocationCountryCode(newSelectedAddressDto.getCountryCode());
								savedAlarmDto.setLocationCountryName(newSelectedAddressDto.getCountryName());
							}
						}
					}
				});
				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.RequestFragment.name(), AlarmSettingsFragment.class.getName());
				favoritesFragment.setArguments(bundle);

				String tag = AlarmSettingsFragment.class.getName();

				getParentFragmentManager().beginTransaction().hide(AlarmSettingsFragment.this).add(R.id.fragment_container,
								favoritesFragment, tag)
						.addToBackStack(tag).commit();
			}
		});

		binding.enableAlarmSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.alarmSoundLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);

				if (!initializing) {
					if (newAlarmSession) {
						newAlarmDto.setEnableSound(isChecked ? 1 : 0);
					} else {
						savedAlarmDto.setEnableSound(isChecked ? 1 : 0);
					}
				}
			}
		});

		binding.alarmSound.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

				ringtoneLauncher.launch(intent);
			}
		});

		binding.alarmSoundVolume.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
			@Override
			public void onStartTrackingTouch(@NonNull @NotNull Slider slider) {

			}

			@Override
			public void onStopTrackingTouch(@NonNull @NotNull Slider slider) {
				if (!initializing) {
					if (newAlarmSession) {
						newAlarmDto.setAlarmSoundVolume((int) slider.getValue());
					} else {
						savedAlarmDto.setAlarmSoundVolume((int) slider.getValue());
					}
				}
			}
		});

		binding.alarmVibartor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!initializing) {
					if (newAlarmSession) {
						newAlarmDto.setAlarmVibration(isChecked ? 1 : 0);
					} else {
						savedAlarmDto.setAlarmVibration(isChecked ? 1 : 0);
					}
				}
			}
		});

		binding.repeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean repeat;
				int repeatInterval;
				int repeatCount;

				if (newAlarmSession) {
					repeat = newAlarmDto.getRepeat() == 1;
					repeatInterval = newAlarmDto.getRepeatInterval();
					repeatCount = newAlarmDto.getRepeatCount();
				} else {
					repeat = savedAlarmDto.getRepeat() == 1;
					repeatInterval = savedAlarmDto.getRepeatInterval();
					repeatCount = savedAlarmDto.getRepeatCount();
				}

				ViewRepeatAlarmSettingsBinding againSettingsViewBinding = ViewRepeatAlarmSettingsBinding.inflate(getLayoutInflater());

				AlertDialog dialog = new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.repeatAlarm)
						.setView(againSettingsViewBinding.getRoot())
						.setPositiveButton(R.string.check, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								final boolean newRepeat = againSettingsViewBinding.enableAgainAlarmSwitch.isChecked();
								final int newRepeatInterval =
										Integer.parseInt((String) againSettingsViewBinding.intervalGroup.findViewById(
												againSettingsViewBinding.intervalGroup.getCheckedRadioButtonId()).getTag());
								final int newRepeatCount = againSettingsViewBinding.endlessRepeatSwitch.isChecked() ?
										Integer.MAX_VALUE : Integer.parseInt((String) againSettingsViewBinding.repeatCountGroup.findViewById(
										againSettingsViewBinding.repeatCountGroup.getCheckedRadioButtonId()).getTag());

								if (newAlarmSession) {
									newAlarmDto.setRepeat(newRepeat ? 1 : 0);
									newAlarmDto.setRepeatInterval(newRepeatInterval);
									newAlarmDto.setRepeatCount(newRepeatCount);
								} else {
									savedAlarmDto.setRepeat(newRepeat ? 1 : 0);
									savedAlarmDto.setRepeatInterval(newRepeatInterval);
									savedAlarmDto.setRepeatCount(newRepeatCount);
								}

								if (newRepeat) {
									binding.repeat.setText(AlarmUtil.parseRepeat(getContext(), newRepeatInterval, newRepeatCount));
								} else {
									binding.repeat.setText(R.string.disabledRepeatAlarm);
								}
								dialog.dismiss();
							}
						}).create();

				againSettingsViewBinding.enableAgainAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						againSettingsViewBinding.settingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
					}
				});

				againSettingsViewBinding.endlessRepeatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						againSettingsViewBinding.repeatCountGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE);
					}
				});


				againSettingsViewBinding.enableAgainAlarmSwitch.setChecked(repeat);
				againSettingsViewBinding.settingsLayout.setVisibility(repeat ? View.VISIBLE : View.GONE);

				againSettingsViewBinding.endlessRepeatSwitch.setChecked(repeatCount == Integer.MAX_VALUE);
				againSettingsViewBinding.repeatCountGroup.setVisibility(repeatCount == Integer.MAX_VALUE ? View.VISIBLE : View.GONE);


				switch (repeatInterval) {
					case 10:
						againSettingsViewBinding.tenMin.setChecked(true);
						break;
					case 15:
						againSettingsViewBinding.fifteenMin.setChecked(true);
						break;
					case 30:
						againSettingsViewBinding.thirtyMin.setChecked(true);
						break;
					default:
						againSettingsViewBinding.fiveMin.setChecked(true);
				}

				switch (repeatCount) {
					case 1:
						againSettingsViewBinding.one.setChecked(true);
						break;
					case 3:
						againSettingsViewBinding.three.setChecked(true);
						break;
					case 5:
						againSettingsViewBinding.five.setChecked(true);
						break;
					default:
						againSettingsViewBinding.two.setChecked(true);
						break;
				}

				dialog.show();
			}
		});

		binding.check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!binding.sunday.isChecked() && !binding.monday.isChecked() &&
						!binding.tuesday.isChecked() && !binding.wednesday.isChecked()
						&& !binding.thursday.isChecked() && !binding.friday.isChecked() && !binding.saturday.isChecked()) {
					Toast.makeText(getContext(), R.string.pleaseSelectDay, Toast.LENGTH_SHORT).show();
					return;
				}

				if (newAlarmSession) {
					alarmRepository.add(newAlarmDto, new DbQueryCallback<AlarmDto>() {
						@Override
						public void onResultSuccessful(AlarmDto result) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									AlarmUtil.registerAlarm(getContext(), result);
									getParentFragmentManager().popBackStackImmediate();
								}
							});

						}

						@Override
						public void onResultNoData() {

						}
					});

				} else {
					alarmRepository.update(savedAlarmDto, new DbQueryCallback<AlarmDto>() {
						@Override
						public void onResultSuccessful(AlarmDto result) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									AlarmUtil.modifyAlarm(getContext(), savedAlarmDto);
									getParentFragmentManager().popBackStackImmediate();
								}
							});
						}

						@Override
						public void onResultNoData() {

						}
					});
				}
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		if (!newAlarmSession) {
			int savedDtoId = getArguments().getInt(BundleKey.dtoId.name());
			alarmRepository.get(savedDtoId, new DbQueryCallback<AlarmDto>() {
				@Override
				public void onResultSuccessful(AlarmDto result) {

					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								savedAlarmDto = result;

								binding.hours.setText(LocalTime.parse(savedAlarmDto.getAlarmTime()).format(hoursFormatter));
								Set<Integer> daySet = AlarmUtil.parseDays(savedAlarmDto.getAlarmDays());

								if (daySet.contains(Calendar.SUNDAY))
									binding.sunday.setChecked(true);
								if (daySet.contains(Calendar.MONDAY))
									binding.monday.setChecked(true);
								if (daySet.contains(Calendar.TUESDAY))
									binding.tuesday.setChecked(true);
								if (daySet.contains(Calendar.WEDNESDAY))
									binding.wednesday.setChecked(true);
								if (daySet.contains(Calendar.THURSDAY))
									binding.thursday.setChecked(true);
								if (daySet.contains(Calendar.FRIDAY))
									binding.friday.setChecked(true);
								if (daySet.contains(Calendar.SATURDAY))
									binding.saturday.setChecked(true);

								binding.enableAlarmSoundSwitch.setChecked(savedAlarmDto.getEnableSound() == 1);

								if (savedAlarmDto.getEnableSound() == 1) {
									ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(savedAlarmDto.getAlarmSoundUri()));
								} else {
									Uri defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
									ringtone = RingtoneManager.getRingtone(getContext(), defaultAlarmUri);
								}
								binding.alarmSound.setText(ringtone.getTitle(getContext()));

								binding.alarmSoundVolume.setValue(savedAlarmDto.getAlarmSoundVolume());
								binding.alarmVibartor.setChecked(savedAlarmDto.getAlarmVibration() == 1);

								if (savedAlarmDto.getRepeat() == 1) {
									binding.repeat.setText(AlarmUtil.parseRepeat(getContext(), savedAlarmDto.getRepeatInterval(),
											savedAlarmDto.getRepeatCount()));
								}

								if (savedAlarmDto.getAddedLocation() == 1) {
									binding.location.setText(savedAlarmDto.getLocationAddressName());
								}

								initializing = false;
							}
						});
					}
				}

				@Override
				public void onResultNoData() {

				}
			});
		} else {
			binding.enableAlarmSoundSwitch.setChecked(newAlarmDto.getEnableSound() == 1);
			binding.alarmSound.setText(ringtone.getTitle(getContext()));
			binding.alarmVibartor.setChecked(true);
			binding.repeat.setText(R.string.disabledRepeatAlarm);
			binding.hours.setText(LocalTime.parse(newAlarmDto.getAlarmTime()).format(hoursFormatter));

			initializing = false;
		}
	}

	private void initDaysCheckBoxes() {
		CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!initializing) {
					Set<Integer> daySet = AlarmUtil.parseDays(newAlarmSession ? newAlarmDto.getAlarmDays() :
							savedAlarmDto.getAlarmDays());
					final int changedDay = Integer.parseInt((String) buttonView.getTag());

					if (isChecked) {
						daySet.add(changedDay);
					} else {
						daySet.remove(changedDay);
					}

					if (newAlarmSession) {
						newAlarmDto.setAlarmDays(daySet);
					} else {
						savedAlarmDto.setAlarmDays(daySet);
					}
				}

			}
		};

		binding.sunday.setOnCheckedChangeListener(onCheckedChangeListener);
		binding.monday.setOnCheckedChangeListener(onCheckedChangeListener);
		binding.tuesday.setOnCheckedChangeListener(onCheckedChangeListener);
		binding.wednesday.setOnCheckedChangeListener(onCheckedChangeListener);
		binding.thursday.setOnCheckedChangeListener(onCheckedChangeListener);
		binding.friday.setOnCheckedChangeListener(onCheckedChangeListener);
		binding.saturday.setOnCheckedChangeListener(onCheckedChangeListener);
	}

	private final ActivityResultLauncher<Intent> ringtoneLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if (result.getData() == null) {
						return;
					}
					Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					if (uri != null) {
						binding.alarmSoundLayout.setVisibility(View.VISIBLE);
						Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
						binding.alarmSound.setText(ringtone.getTitle(getContext()));

						if (newAlarmSession) {
							newAlarmDto.setAlarmSoundUri(uri.toString());
							newAlarmDto.setAlarmSoundVolume((int) binding.alarmSoundVolume.getValue());
						} else {
							savedAlarmDto.setAlarmSoundUri(uri.toString());
							savedAlarmDto.setAlarmSoundVolume((int) binding.alarmSoundVolume.getValue());
						}

					}
				}
			});

}