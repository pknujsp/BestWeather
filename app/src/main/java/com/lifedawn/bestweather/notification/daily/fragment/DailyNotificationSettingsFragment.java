package com.lifedawn.bestweather.notification.daily.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.util.TypedValue;
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
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.databinding.FragmentDailyPushNotificationSettingsBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
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
import com.lifedawn.bestweather.notification.daily.viewmodel.DailyNotificationViewModel;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;


public class DailyNotificationSettingsFragment extends Fragment {
	private final DateTimeFormatter hoursFormatter = DateTimeFormatter.ofPattern("a h:mm");
	private FragmentDailyPushNotificationSettingsBinding binding;
	private DailyNotificationViewModel viewModel;

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof MapFragment) {
				if (!((MapFragment) f).isClickedItem() && !viewModel.isSelectedFavoriteLocation()) {
					binding.commons.currentLocationRadio.setChecked(true);
				}
			}
		}
	};

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);

		viewModel = new ViewModelProvider(this).get(DailyNotificationViewModel.class);
		viewModel.setMainWeatherProviderType(WeatherRequestUtil.getMainWeatherSourceType(getContext(), null));

		if (getArguments() != null)
			viewModel.setBundle(getArguments());

		viewModel.setNewNotificationSession(viewModel.getBundle().getBoolean(BundleKey.NewSession.name()));
	}


	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentDailyPushNotificationSettingsBinding.inflate(inflater);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.toolbar.fragmentTitle.setText(R.string.daily_notification);

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.commons.autoRefreshIntervalSpinner.setVisibility(View.GONE);
		binding.commons.autoRefreshIntervalLabel.setVisibility(View.GONE);
		binding.commons.singleWeatherDataSourceLayout.setVisibility(View.VISIBLE);

		binding.toolbar.backBtn.setOnClickListener(v -> getParentFragmentManager().popBackStackImmediate());

		binding.hours.setOnClickListener(v -> {
			final String time = viewModel.getEditingNotificationDto().getAlarmClock();
			LocalTime localTime = LocalTime.parse(time);

			MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
			MaterialTimePicker timePicker =
					builder.setTitleText(R.string.clock)
							.setTimeFormat(TimeFormat.CLOCK_12H)
							.setHour(localTime.getHour())
							.setMinute(localTime.getMinute())
							.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
							.build();

			timePicker.addOnPositiveButtonClickListener(v1 -> {
				final int newHour = timePicker.getHour();
				final int newMinute = timePicker.getMinute();

				LocalTime newLocalTime = LocalTime.of(newHour, newMinute, 0);
				viewModel.getEditingNotificationDto().setAlarmClock(newLocalTime.toString());

				binding.hours.setText(newLocalTime.format(hoursFormatter));
			});
			timePicker.addOnNegativeButtonClickListener(v12 -> timePicker.dismiss());
			timePicker.show(getChildFragmentManager(), MaterialTimePicker.class.getName());
		});

		binding.save.setOnClickListener(v -> {
			if (viewModel.isNewNotificationSession()) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					NotificationHelper notificationHelper = new NotificationHelper(requireContext().getApplicationContext());
					NotificationHelper.NotificationObj notificationObj = notificationHelper.getNotificationObj(NotificationType.Daily);
					notificationHelper.createNotificationChannel(notificationObj);
				}
				viewModel.add(viewModel.getEditingNotificationDto(), new DbQueryCallback<DailyPushNotificationDto>() {
					@Override
					public void onResultSuccessful(DailyPushNotificationDto result) {
						MainThreadWorker.runOnUiThread(() -> {
							String text =
									LocalTime.parse(result.getAlarmClock()).format(hoursFormatter) + ", " + getString(R.string.registeredDailyNotification);
							Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
							viewModel.getDailyNotificationHelper().enablePushNotification(result);
							getParentFragmentManager().popBackStack();
						});

					}

					@Override
					public void onResultNoData() {

					}
				});

			} else {
				viewModel.update(viewModel.getEditingNotificationDto(), new DbQueryCallback<DailyPushNotificationDto>() {
					@Override
					public void onResultSuccessful(DailyPushNotificationDto result) {
						MainThreadWorker.runOnUiThread(() -> {
							viewModel.getDailyNotificationHelper().modifyPushNotification(result);
							getParentFragmentManager().popBackStack();
						});
					}

					@Override
					public void onResultNoData() {

					}
				});
			}
		});

		initLocation();
		initWeatherDataSource();
		initNotificationTypeSpinner();
	}

	@Override
	public void onStart() {
		super.onStart();

		if (viewModel.isNewNotificationSession()) {
			binding.commons.currentLocationRadio.setChecked(true);
		} else {
			if (viewModel.getSavedNotificationDto().getLocationType() == LocationType.SelectedAddress) {
				binding.commons.selectedLocationRadio.setChecked(true);
				binding.commons.selectedAddressName.setText(viewModel.getSelectedFavoriteAddressDto().getDisplayName());
			} else {
				binding.commons.currentLocationRadio.setChecked(true);
			}

		}
		binding.notificationTypesSpinner.setSelection(viewModel.getEditingNotificationDto().getNotificationType().getIndex());

		LocalTime localTime = LocalTime.parse(viewModel.getEditingNotificationDto().getAlarmClock());
		binding.hours.setText(localTime.format(hoursFormatter));

		Set<WeatherProviderType> weatherProviderTypeSet = viewModel.getEditingNotificationDto().getWeatherProviderTypeSet();
		if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
			binding.commons.owmRadio.setChecked(true);
		} else {
			binding.commons.metNorwayRadio.setChecked(true);
		}

		binding.commons.kmaTopPrioritySwitch.setChecked(viewModel.getEditingNotificationDto().isTopPriorityKma());
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
		final DailyPushNotificationType dailyPushNotificationType = DailyPushNotificationType.valueOf(position);
		viewModel.getEditingNotificationDto().setNotificationType(dailyPushNotificationType);

		Context context = requireContext().getApplicationContext();
		AbstractDailyNotiViewCreator viewCreator = null;

		switch (dailyPushNotificationType) {
			case First:
				//시간별 예보
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.VISIBLE);
				viewCreator = new FirstDailyNotificationViewCreator(context);
				viewModel.getEditingNotificationDto().removeWeatherSourceType(WeatherProviderType.AQICN);
				viewModel.getEditingNotificationDto().addWeatherSourceType(binding.commons.metNorwayRadio.isChecked() ? WeatherProviderType.MET_NORWAY :
						WeatherProviderType.OWM_ONECALL);
				break;
			case Second:
				//현재날씨
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.VISIBLE);
				viewCreator = new SecondDailyNotificationViewCreator(context);
				viewModel.getEditingNotificationDto().addWeatherSourceType(WeatherProviderType.AQICN);
				viewModel.getEditingNotificationDto().addWeatherSourceType(binding.commons.metNorwayRadio.isChecked() ? WeatherProviderType.MET_NORWAY :
						WeatherProviderType.OWM_ONECALL);
				break;
			case Third:
				//일별 예보
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.VISIBLE);
				viewCreator = new ThirdDailyNotificationViewCreator(context);
				viewModel.getEditingNotificationDto().removeWeatherSourceType(WeatherProviderType.AQICN);
				viewModel.getEditingNotificationDto().addWeatherSourceType(binding.commons.metNorwayRadio.isChecked() ? WeatherProviderType.MET_NORWAY :
						WeatherProviderType.OWM_ONECALL);
				break;
			case Fourth:
				//현재 대기질
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.GONE);
				viewCreator = new FourthDailyNotificationViewCreator(context);
				viewModel.getEditingNotificationDto().getWeatherProviderTypeSet().clear();
				viewModel.getEditingNotificationDto().addWeatherSourceType(WeatherProviderType.AQICN);
				break;
			default:
				//대기질 예보
				binding.commons.singleWeatherDataSourceLayout.setVisibility(View.GONE);
				viewCreator = new FifthDailyNotificationViewCreator(context);
				viewModel.getEditingNotificationDto().getWeatherProviderTypeSet().clear();
				viewModel.getEditingNotificationDto().addWeatherSourceType(WeatherProviderType.AQICN);
				break;
		}

		RemoteViews remoteViews = viewCreator.createRemoteViews(true);
		final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		remoteViews.setViewPadding(R.id.root_layout, padding, padding, padding, padding);

		View previewWidgetView = remoteViews.apply(context, binding.previewLayout);

		binding.previewLayout.removeAllViews();
		binding.previewLayout.addView(previewWidgetView);
	}


	private void initWeatherDataSource() {
		binding.commons.weatherDataSourceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
			WeatherProviderType checked = checkedId == R.id.met_norway_radio ? WeatherProviderType.MET_NORWAY : WeatherProviderType.OWM_ONECALL;
			viewModel.getEditingNotificationDto().addWeatherSourceType(checked);
		});
		binding.commons.kmaTopPrioritySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.getEditingNotificationDto().setTopPriorityKma(isChecked));

	}

	private void initLocation() {
		binding.commons.changeAddressBtn.setVisibility(View.GONE);
		binding.commons.selectedAddressName.setVisibility(View.GONE);

		binding.commons.locationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
			if (checkedId == binding.commons.currentLocationRadio.getId() && binding.commons.currentLocationRadio.isChecked()) {
				binding.commons.changeAddressBtn.setVisibility(View.GONE);
				binding.commons.selectedAddressName.setVisibility(View.GONE);
				viewModel.getEditingNotificationDto().setLocationType(LocationType.CurrentLocation);

			} else if (checkedId == binding.commons.selectedLocationRadio.getId() && binding.commons.selectedLocationRadio.isChecked()) {
				binding.commons.changeAddressBtn.setVisibility(View.VISIBLE);
				binding.commons.selectedAddressName.setVisibility(View.VISIBLE);

				if (viewModel.isSelectedFavoriteLocation()) {
					viewModel.getEditingNotificationDto().setAddressName(viewModel.getSelectedFavoriteAddressDto().getDisplayName());
					viewModel.getEditingNotificationDto().setLatitude(Double.parseDouble(viewModel.getSelectedFavoriteAddressDto().getLatitude()));
					viewModel.getEditingNotificationDto().setLongitude(Double.parseDouble(viewModel.getSelectedFavoriteAddressDto().getLongitude()));
					viewModel.getEditingNotificationDto().setZoneId(viewModel.getSelectedFavoriteAddressDto().getZoneId());
					viewModel.getEditingNotificationDto().setCountryCode(viewModel.getSelectedFavoriteAddressDto().getCountryCode());
					viewModel.getEditingNotificationDto().setLocationType(LocationType.SelectedAddress);
				} else {
					openFavoritesFragment();
				}
			}
		});

		binding.commons.changeAddressBtn.setOnClickListener(v -> openFavoritesFragment());
	}

	private void openFavoritesFragment() {
		MapFragment mapFragment = new MapFragment();
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.RequestFragment.name(), DailyNotificationSettingsFragment.class.getName());
		mapFragment.setArguments(bundle);

		mapFragment.setOnResultFavoriteListener(new MapFragment.OnResultFavoriteListener() {
			@Override
			public void onAddedNewAddress(FavoriteAddressDto newFavoriteAddressDto, List<FavoriteAddressDto> favoriteAddressDtoList, boolean removed) {
				onClickedAddress(newFavoriteAddressDto);
			}

			@Override
			public void onResult(List<FavoriteAddressDto> favoriteAddressDtoList) {

			}

			@Override
			public void onClickedAddress(@Nullable FavoriteAddressDto favoriteAddressDto) {
				if (favoriteAddressDto == null) {
					if (!viewModel.isSelectedFavoriteLocation()) {
						Toast.makeText(getContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();
						binding.commons.currentLocationRadio.setChecked(true);
					}
				} else {
					viewModel.setSelectedFavoriteLocation(true);
					viewModel.setSelectedFavoriteAddressDto(favoriteAddressDto);
					binding.commons.selectedAddressName.setText(favoriteAddressDto.getDisplayName());

					//address,latitude,longitude,countryCode
					viewModel.getEditingNotificationDto().setAddressName(favoriteAddressDto.getDisplayName());
					viewModel.getEditingNotificationDto().setLatitude(Double.parseDouble(favoriteAddressDto.getLatitude()));
					viewModel.getEditingNotificationDto().setLongitude(Double.parseDouble(favoriteAddressDto.getLongitude()));
					viewModel.getEditingNotificationDto().setZoneId(favoriteAddressDto.getZoneId());
					viewModel.getEditingNotificationDto().setCountryCode(favoriteAddressDto.getCountryCode());
					viewModel.getEditingNotificationDto().setLocationType(LocationType.SelectedAddress);

					getParentFragmentManager().popBackStack();
				}
			}
		});

		final String tag = MapFragment.class.getName();
		getParentFragmentManager().beginTransaction().hide(DailyNotificationSettingsFragment.this).add(R.id.fragment_container,
				mapFragment, tag).addToBackStack(tag).commit();
	}
}