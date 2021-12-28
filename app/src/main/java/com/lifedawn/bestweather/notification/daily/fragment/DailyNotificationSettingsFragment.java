package com.lifedawn.bestweather.notification.daily.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentDailyPushNotificationSettingsBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.notification.daily.DailyNotiHelper;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;


public class DailyNotificationSettingsFragment extends Fragment {
	private final DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a hh:mm");

	private FragmentDailyPushNotificationSettingsBinding binding;
	private DailyNotiHelper dailyNotiHelper;
	private DailyPushNotificationRepository repository;
	private DailyPushNotificationDto savedNotificationDto;
	private DailyPushNotificationDto newNotificationDto;
	private DailyPushNotificationDto editingNotificationDto;

	private boolean newNotificationSession;
	private boolean selectedFavoriteLocation = false;
	private boolean initializing = true;
	private boolean isKr;
	private boolean useMultipleWeatherDataSource = false;

	private WeatherSourceType mainWeatherSourceType;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dailyNotiHelper = new DailyNotiHelper(getActivity().getApplicationContext());
		repository = new DailyPushNotificationRepository(getContext());
		mainWeatherSourceType = WeatherRequestUtil.getMainWeatherSourceType(getContext(), null);

		Locale locale = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = getResources().getConfiguration().getLocales().get(0);
		} else {
			locale = getResources().getConfiguration().locale;
		}
		isKr = locale.getCountry().equals("KR");

		Bundle bundle = getArguments();
		newNotificationSession = bundle.getBoolean(BundleKey.NewSession.name());

		if (newNotificationSession) {
			newNotificationDto = new DailyPushNotificationDto();
			newNotificationDto.setEnabled(true);
			newNotificationDto.setAlarmClock(LocalTime.of(8, 0).toString());
			newNotificationDto.addWeatherSourceType(mainWeatherSourceType);

			editingNotificationDto = newNotificationDto;
		} else {
			savedNotificationDto = (DailyPushNotificationDto) bundle.getSerializable("dto");
			editingNotificationDto = savedNotificationDto;
		}
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentDailyPushNotificationSettingsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.daily_notification);

		binding.commons.multipleWeatherDataSourceLayout.setVisibility(View.VISIBLE);
		binding.commons.autoRefreshIntervalSpinner.setVisibility(View.GONE);
		binding.commons.autoRefreshIntervalLabel.setVisibility(View.GONE);
		binding.commons.singleWeatherDataSourceLayout.setVisibility(View.GONE);

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.hours.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String time = editingNotificationDto.getAlarmClock();
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
						editingNotificationDto.setAlarmClock(newLocalTime.toString());

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

		binding.check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!binding.commons.accuWeatherChk.isChecked() && !binding.commons.owmChk.isChecked()
						&& !binding.commons.kmaChk.isChecked()) {
					Toast.makeText(getContext(), R.string.notSelectedDefaultWeatherSource, Toast.LENGTH_SHORT).show();
					return;
				}

				if (newNotificationSession) {
					repository.add(newNotificationDto, new DbQueryCallback<DailyPushNotificationDto>() {
						@Override
						public void onResultSuccessful(DailyPushNotificationDto result) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									dailyNotiHelper.enablePushNotification(result);
									getParentFragmentManager().popBackStackImmediate();
								}
							});

						}

						@Override
						public void onResultNoData() {

						}
					});

				} else {
					repository.update(savedNotificationDto, new DbQueryCallback<DailyPushNotificationDto>() {
						@Override
						public void onResultSuccessful(DailyPushNotificationDto result) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									dailyNotiHelper.modifyPushNotification(result);
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

		initLocation();
		initWeatherDataSource();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (newNotificationSession) {
			binding.commons.currentLocationRadio.setChecked(true);
		} else {
			if (savedNotificationDto.getLocationType() == LocationType.SelectedAddress) {
				selectedFavoriteLocation = true;
				binding.commons.selectedLocationRadio.setChecked(true);
				binding.commons.selectedAddressName.setText(savedNotificationDto.getAddressName());
			} else {
				binding.commons.currentLocationRadio.setChecked(true);
			}

		}
		LocalTime localTime = LocalTime.parse(editingNotificationDto.getAlarmClock());
		binding.hours.setText(localTime.format(hoursFormatter));

		Set<WeatherSourceType> weatherSourceTypeSet = editingNotificationDto.getWeatherSourceTypeSet();
		if (weatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			binding.commons.owmChk.setChecked(true);
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			binding.commons.accuWeatherChk.setChecked(true);
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.KMA_WEB)) {
			binding.commons.kmaChk.setChecked(true);
		}

		initializing = false;
	}

	protected void initWeatherDataSource() {
		binding.commons.accuWeatherChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					editingNotificationDto.addWeatherSourceType(WeatherSourceType.ACCU_WEATHER);
				} else {
					editingNotificationDto.removeWeatherSourceType(WeatherSourceType.ACCU_WEATHER);
				}
			}
		});
		binding.commons.owmChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					editingNotificationDto.addWeatherSourceType(WeatherSourceType.OPEN_WEATHER_MAP);
				} else {
					editingNotificationDto.removeWeatherSourceType(WeatherSourceType.OPEN_WEATHER_MAP);
				}
			}
		});
		binding.commons.kmaChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					editingNotificationDto.addWeatherSourceType(WeatherSourceType.KMA_WEB);
				} else {
					editingNotificationDto.removeWeatherSourceType(WeatherSourceType.KMA_WEB);
				}
			}
		});

	}

	protected void initLocation() {
		binding.commons.locationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == binding.commons.currentLocationRadio.getId() && binding.commons.currentLocationRadio.isChecked()) {
					binding.commons.changeAddressBtn.setVisibility(View.GONE);
					binding.commons.selectedAddressName.setVisibility(View.GONE);

				} else if (checkedId == binding.commons.selectedLocationRadio.getId() && binding.commons.selectedLocationRadio.isChecked()) {
					binding.commons.changeAddressBtn.setVisibility(View.VISIBLE);
					binding.commons.selectedAddressName.setVisibility(View.VISIBLE);

					if (!selectedFavoriteLocation) {
						openFavoritesFragment();
					}
				}
			}
		});

		binding.commons.changeAddressBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openFavoritesFragment();
			}
		});
	}

	protected void openFavoritesFragment() {
		FavoritesFragment favoritesFragment = new FavoritesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.RequestFragment.name(), DailyNotificationSettingsFragment.class.getName());
		favoritesFragment.setArguments(bundle);

		favoritesFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
			@Override
			public void onResultFragment(Bundle result) {
				if (result.getSerializable(BundleKey.SelectedAddressDto.name()) == null) {
					if (!selectedFavoriteLocation) {
						Toast.makeText(getContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();
						binding.commons.currentLocationRadio.setChecked(true);
					}
				} else {
					selectedFavoriteLocation = true;

					FavoriteAddressDto addressDto = (FavoriteAddressDto) result.getSerializable(BundleKey.SelectedAddressDto.name());
					binding.commons.selectedAddressName.setText(addressDto.getAddress());

					//address,latitude,longitude,countryCode
					editingNotificationDto.setAddressName(addressDto.getAddress());
					editingNotificationDto.setLatitude(Double.parseDouble(addressDto.getLatitude()));
					editingNotificationDto.setLongitude(Double.parseDouble(addressDto.getLongitude()));
					editingNotificationDto.setCountryCode(addressDto.getCountryCode());
				}
			}
		});

		String tag = FavoritesFragment.class.getName();

		getParentFragmentManager().beginTransaction().hide(DailyNotificationSettingsFragment.this).add(R.id.fragment_container,
				favoritesFragment, tag).addToBackStack(tag).commit();
	}
}