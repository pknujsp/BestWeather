package com.lifedawn.bestweather.notification.daily.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentDailyPushNotificationSettingsBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.daily.DailyNotificationHelper;
import com.lifedawn.bestweather.notification.daily.DailyPushNotificationType;
import com.lifedawn.bestweather.notification.daily.viewcreator.AbstractDailyNotiViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FifthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FirstDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.FourthDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.SecondDailyNotificationViewCreator;
import com.lifedawn.bestweather.notification.daily.viewcreator.ThirdDailyNotificationViewCreator;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;


public class DailyNotificationSettingsFragment extends Fragment {
	private final DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a h:mm");

	private FragmentDailyPushNotificationSettingsBinding binding;
	private DailyNotificationHelper dailyNotificationHelper;
	private DailyPushNotificationRepository repository;
	private DailyPushNotificationDto savedNotificationDto;
	private DailyPushNotificationDto newNotificationDto;
	private DailyPushNotificationDto editingNotificationDto;

	private boolean newNotificationSession;
	private boolean selectedFavoriteLocation = false;
	private boolean initializing = true;

	private Bundle bundle;

	private WeatherProviderType mainWeatherProviderType;
	private FavoriteAddressDto selectedFavoriteAddressDto;

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof FavoritesFragment) {
				if (!((FavoritesFragment) f).isClickedItem() && !selectedFavoriteLocation) {
					binding.commons.currentLocationRadio.setChecked(true);
				}
			}
		}
	};

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);

		dailyNotificationHelper = new DailyNotificationHelper(getActivity().getApplicationContext());
		repository = new DailyPushNotificationRepository(getContext());
		mainWeatherProviderType = WeatherRequestUtil.getMainWeatherSourceType(getContext(), null);

		bundle = getArguments() != null ? getArguments() : savedInstanceState;
		newNotificationSession = bundle.getBoolean(BundleKey.NewSession.name());

		if (newNotificationSession) {
			newNotificationDto = new DailyPushNotificationDto();
			newNotificationDto.setEnabled(true);
			newNotificationDto.setAlarmClock(LocalTime.of(8, 0).toString());
			newNotificationDto.addWeatherSourceType(mainWeatherProviderType);
			newNotificationDto.setNotificationType(DailyPushNotificationType.First);

			editingNotificationDto = newNotificationDto;
		} else {
			savedNotificationDto = (DailyPushNotificationDto) bundle.getSerializable("dto");
			editingNotificationDto = savedNotificationDto;
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
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
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.toolbar.fragmentTitle.setText(R.string.daily_notification);

		binding.commons.autoRefreshIntervalSpinner.setVisibility(View.GONE);
		binding.commons.autoRefreshIntervalLabel.setVisibility(View.GONE);
		binding.commons.singleWeatherDataSourceLayout.setVisibility(View.VISIBLE);

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
				if (newNotificationSession) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						NotificationHelper notificationHelper = new NotificationHelper(getContext());
						NotificationHelper.NotificationObj notificationObj = notificationHelper.getNotificationObj(NotificationType.Daily);
						notificationHelper.createNotificationChannel(notificationObj);
					}
					repository.add(newNotificationDto, new DbQueryCallback<DailyPushNotificationDto>() {
						@Override
						public void onResultSuccessful(DailyPushNotificationDto result) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									String text =
											LocalTime.parse(result.getAlarmClock()).format(hoursFormatter) + ", " + getString(R.string.registeredDailyNotification);
									Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
									dailyNotificationHelper.enablePushNotification(result);
									getParentFragmentManager().popBackStack();
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
									dailyNotificationHelper.modifyPushNotification(result);
									getParentFragmentManager().popBackStack();
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
		initNotificationTypeSpinner();

		if (newNotificationSession) {
			binding.commons.currentLocationRadio.setChecked(true);
		} else {
			if (savedNotificationDto.getLocationType() == LocationType.SelectedAddress) {
				selectedFavoriteAddressDto = new FavoriteAddressDto();
				selectedFavoriteAddressDto.setAddress(savedNotificationDto.getAddressName());
				selectedFavoriteAddressDto.setCountryCode(savedNotificationDto.getCountryCode());
				selectedFavoriteAddressDto.setLatitude(String.valueOf(savedNotificationDto.getLatitude()));
				selectedFavoriteAddressDto.setLongitude(String.valueOf(savedNotificationDto.getLongitude()));

				selectedFavoriteLocation = true;
				binding.commons.selectedLocationRadio.setChecked(true);
				binding.commons.selectedAddressName.setText(savedNotificationDto.getAddressName());
			} else {
				binding.commons.currentLocationRadio.setChecked(true);
			}

		}
		binding.notificationTypesSpinner.setSelection(editingNotificationDto.getNotificationType().getIndex());

		LocalTime localTime = LocalTime.parse(editingNotificationDto.getAlarmClock());
		binding.hours.setText(localTime.format(hoursFormatter));

		Set<WeatherProviderType> weatherProviderTypeSet = editingNotificationDto.getWeatherProviderTypeSet();
		if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
			binding.commons.owmRadio.setChecked(true);
		}

		binding.commons.kmaTopPrioritySwitch.setChecked(editingNotificationDto.isTopPriorityKma());

		initializing = false;
	}

	@Override
	public void onDestroy() {
		getParentFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}

	private void initNotificationTypeSpinner() {
		SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.DailyPushNotificationType));
		binding.notificationTypesSpinner.setAdapter(spinnerAdapter);

		binding.notificationTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				onSelectedNotificationType(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void onSelectedNotificationType(int position) {
		DailyPushNotificationType dailyPushNotificationType = DailyPushNotificationType.valueOf(position);
		editingNotificationDto.setNotificationType(dailyPushNotificationType);

		Context context = getActivity().getApplicationContext();
		AbstractDailyNotiViewCreator viewCreator = null;

		switch (dailyPushNotificationType) {
			case First:
				//시간별 예보
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.VISIBLE);
				viewCreator = new FirstDailyNotificationViewCreator(context);
				editingNotificationDto.removeWeatherSourceType(WeatherProviderType.AQICN);
				editingNotificationDto.addWeatherSourceType(WeatherProviderType.OWM_ONECALL);
				break;
			case Second:
				//현재날씨
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.VISIBLE);
				viewCreator = new SecondDailyNotificationViewCreator(context);
				editingNotificationDto.addWeatherSourceType(WeatherProviderType.AQICN);
				editingNotificationDto.addWeatherSourceType(WeatherProviderType.OWM_ONECALL);
				break;
			case Third:
				//일별 예보
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.VISIBLE);
				viewCreator = new ThirdDailyNotificationViewCreator(context);
				editingNotificationDto.removeWeatherSourceType(WeatherProviderType.AQICN);
				editingNotificationDto.addWeatherSourceType(WeatherProviderType.OWM_ONECALL);
				break;
			case Fourth:
				//현재 대기질
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.GONE);
				viewCreator = new FourthDailyNotificationViewCreator(context);
				editingNotificationDto.getWeatherProviderTypeSet().clear();
				editingNotificationDto.addWeatherSourceType(WeatherProviderType.AQICN);
				break;
			default:
				//대기질 예보
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.GONE);
				viewCreator = new FifthDailyNotificationViewCreator(context);
				editingNotificationDto.getWeatherProviderTypeSet().clear();
				editingNotificationDto.addWeatherSourceType(WeatherProviderType.AQICN);
				break;
		}

		RemoteViews remoteViews = viewCreator.createRemoteViews(true);
		View previewWidgetView = remoteViews.apply(context, binding.previewLayout);
		binding.previewLayout.removeAllViews();
		binding.previewLayout.addView(previewWidgetView);
	}


	private void initWeatherDataSource() {
		binding.commons.weatherDataSourceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				WeatherProviderType checked = WeatherProviderType.OWM_ONECALL;
				editingNotificationDto.addWeatherSourceType(checked);
			}
		});
		binding.commons.kmaTopPrioritySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				editingNotificationDto.setTopPriorityKma(isChecked);
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
					editingNotificationDto.setLocationType(LocationType.CurrentLocation);

				} else if (checkedId == binding.commons.selectedLocationRadio.getId() && binding.commons.selectedLocationRadio.isChecked()) {
					binding.commons.changeAddressBtn.setVisibility(View.VISIBLE);
					binding.commons.selectedAddressName.setVisibility(View.VISIBLE);

					if (selectedFavoriteLocation) {
						editingNotificationDto.setAddressName(selectedFavoriteAddressDto.getAddress());
						editingNotificationDto.setLatitude(Double.parseDouble(selectedFavoriteAddressDto.getLatitude()));
						editingNotificationDto.setLongitude(Double.parseDouble(selectedFavoriteAddressDto.getLongitude()));
						editingNotificationDto.setCountryCode(selectedFavoriteAddressDto.getCountryCode());
						editingNotificationDto.setLocationType(LocationType.SelectedAddress);
					} else {
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
					selectedFavoriteAddressDto = addressDto;
					binding.commons.selectedAddressName.setText(addressDto.getAddress());

					//address,latitude,longitude,countryCode
					editingNotificationDto.setAddressName(selectedFavoriteAddressDto.getAddress());
					editingNotificationDto.setLatitude(Double.parseDouble(selectedFavoriteAddressDto.getLatitude()));
					editingNotificationDto.setLongitude(Double.parseDouble(selectedFavoriteAddressDto.getLongitude()));
					editingNotificationDto.setCountryCode(selectedFavoriteAddressDto.getCountryCode());
					editingNotificationDto.setLocationType(LocationType.SelectedAddress);
				}
			}
		});

		String tag = FavoritesFragment.class.getName();

		getParentFragmentManager().beginTransaction().hide(DailyNotificationSettingsFragment.this).add(R.id.fragment_container,
				favoritesFragment, tag).addToBackStack(tag).commit();
	}
}