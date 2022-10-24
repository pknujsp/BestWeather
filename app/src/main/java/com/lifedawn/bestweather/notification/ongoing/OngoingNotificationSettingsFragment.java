package com.lifedawn.bestweather.notification.ongoing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

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

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.IntentUtil;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.databinding.FragmentBaseNotificationSettingsBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.NotificationUpdateCallback;
import com.lifedawn.bestweather.notification.model.OngoingNotiDtoOngoing;
import com.lifedawn.bestweather.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class OngoingNotificationSettingsFragment extends Fragment implements NotificationUpdateCallback {
	private final WidgetNotiConstants.DataTypeOfIcon[] dataTypeOfIcons =
			new WidgetNotiConstants.DataTypeOfIcon[]{WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE, WidgetNotiConstants.DataTypeOfIcon.WEATHER_ICON};
	private OngoingNotificationViewModel ongoingNotificationViewModel;
	private FragmentBaseNotificationSettingsBinding binding;

	private FavoriteAddressDto newSelectedAddressDto;
	private FavoriteAddressDto originalSelectedFavoriteAddressDto;

	private OngoingNotificationDto ongoingNotificationDto;

	private long[] intervalsLong;
	private boolean selectedFavoriteLocation;

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof MapFragment) {
				if (!((MapFragment) f).isClickedItem() && !selectedFavoriteLocation) {
					binding.commons.currentLocationRadio.setChecked(true);
				}
			}
		}
	};

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ongoingNotificationViewModel = new ViewModelProvider(requireActivity()).get(OngoingNotificationViewModel.class);
		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);

		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		intervalsLong = new long[intervalsStr.length];

		for (int i = 0; i < intervalsStr.length; i++) {
			intervalsLong[i] = Long.parseLong(intervalsStr[i]);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentBaseNotificationSettingsBinding.inflate(inflater);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});
		binding.toolbar.fragmentTitle.setText(R.string.always_notification);
		if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_key_met), true)) {
			binding.commons.metNorwayRadio.setChecked(true);
		} else {
			binding.commons.owmRadio.setChecked(true);
		}

		SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.AutoRefreshIntervals));
		binding.commons.autoRefreshIntervalSpinner.setAdapter(spinnerAdapter);

		binding.dataTypeOfIconSpinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
				getResources().getStringArray(R.array.DataTypeOfIcons)));

		if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_key_met), true)) {
			binding.commons.metNorwayRadio.setChecked(true);
		} else {
			binding.commons.owmRadio.setChecked(true);
		}

		initLocation();
		initWeatherProvider();
		initAutoRefreshInterval();
		initDataTypeOfIconSpinner();

		OngoingNotiViewCreator ongoingNotiViewCreator = new OngoingNotiViewCreator(requireContext().getApplicationContext(), null);
		RemoteViews[] remoteViews = ongoingNotiViewCreator.createRemoteViews(true);
		final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		remoteViews[1].setViewPadding(R.id.root_layout, padding, padding, padding, padding);

		binding.previewLayout.addView(remoteViews[1].apply(requireContext().getApplicationContext(), binding.previewLayout));

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ongoingNotificationViewModel.getOngoingNotificationDto(new DbQueryCallback<OngoingNotificationDto>() {
			@Override
			public void onResultSuccessful(OngoingNotificationDto originalDto) {
				ongoingNotificationDto = originalDto;

				if (originalDto.getLocationType() == LocationType.SelectedAddress) {
					originalSelectedFavoriteAddressDto = new FavoriteAddressDto();
					originalSelectedFavoriteAddressDto.setDisplayName(ongoingNotificationDto.getDisplayName());
					originalSelectedFavoriteAddressDto.setCountryCode(ongoingNotificationDto.getCountryCode());
					originalSelectedFavoriteAddressDto.setLatitude(String.valueOf(ongoingNotificationDto.getLatitude()));
					originalSelectedFavoriteAddressDto.setLongitude(String.valueOf(ongoingNotificationDto.getLongitude()));
					originalSelectedFavoriteAddressDto.setZoneId(ongoingNotificationDto.getZoneId());

					selectedFavoriteLocation = true;

					binding.commons.selectedAddressName.setText(ongoingNotificationDto.getDisplayName());
					binding.commons.selectedLocationRadio.setChecked(true);
				} else {
					binding.commons.currentLocationRadio.setChecked(true);
				}

				init();
			}

			@Override
			public void onResultNoData() {
				ongoingNotificationDto = createDefaultDto();
				init();
			}
		});

		binding.saveBtn.setOnClickListener(v -> {
			ongoingNotificationViewModel.save(ongoingNotificationDto, new BackgroundWorkCallback() {
				@Override
				public void onFinished() {
					MainThreadWorker.runOnUiThread(() -> {
						// 저장된 알림 데이터가 있으면 알림 표시
						Context context = requireContext().getApplicationContext();

						if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
							Intent intent = new Intent(context, OngoingNotificationReceiver.class);
							intent.setAction(getString(R.string.com_lifedawn_bestweather_action_REFRESH));

							PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
									NotificationType.Ongoing.getNotificationId(),
									intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

							try {
								pendingIntent.send();
								getParentFragmentManager().popBackStack();
							} catch (PendingIntent.CanceledException e) {
								e.printStackTrace();
							}
						} else {
							ongoingNotificationViewModel.remove();
							Toast.makeText(getContext(), R.string.disabledNotification, Toast.LENGTH_SHORT).show();
							startActivity(IntentUtil.getNotificationSettingsIntent(getActivity()));
						}

					});

				}
			});
		});
	}

	private void init() {
		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		final long autoRefreshInterval = ongoingNotificationDto.getUpdateIntervalMillis();

		for (int i = 0; i < intervalsStr.length; i++) {
			if (Long.parseLong(intervalsStr[i]) == autoRefreshInterval) {
				binding.commons.autoRefreshIntervalSpinner.setSelection(i);
				break;
			}
		}
		binding.dataTypeOfIconSpinner.setSelection(ongoingNotificationDto.getDataTypeOfIcon() == WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE
				? 0 : 1, false);
		binding.commons.kmaTopPrioritySwitch.setChecked(ongoingNotificationDto.isTopPriorityKma());
	}


	@Override
	public void onDestroy() {
		getParentFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}

	protected void initAutoRefreshInterval() {
		binding.commons.autoRefreshIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				ongoingNotificationDto.setUpdateIntervalMillis(intervalsLong[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	protected void initDataTypeOfIconSpinner() {
		binding.dataTypeOfIconSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				ongoingNotificationDto.setDataTypeOfIcon(dataTypeOfIcons[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}


	protected void initWeatherProvider() {
		binding.commons.weatherDataSourceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				WeatherProviderType checkedWeatherProviderType = checkedId == R.id.met_norway_radio ? WeatherProviderType.MET_NORWAY :
						WeatherProviderType.OWM_ONECALL;
				onCheckedWeatherProvider(checkedWeatherProviderType);
			}
		});

		binding.commons.kmaTopPrioritySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ongoingNotificationDto.setTopPriorityKma(isChecked);
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

					onSelectedCurrentLocation();
				} else if (checkedId == binding.commons.selectedLocationRadio.getId() && binding.commons.selectedLocationRadio.isChecked()) {
					binding.commons.changeAddressBtn.setVisibility(View.VISIBLE);
					binding.commons.selectedAddressName.setVisibility(View.VISIBLE);

					if (selectedFavoriteLocation) {
						onSelectedFavoriteLocation(originalSelectedFavoriteAddressDto);
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
		MapFragment mapFragment = new MapFragment();
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.RequestFragment.name(), OngoingNotificationSettingsFragment.class.getName());
		mapFragment.setArguments(bundle);

		mapFragment.setOnResultFavoriteListener(new MapFragment.OnResultFavoriteListener() {
			@Override
			public void onAddedNewAddress(FavoriteAddressDto newFavoriteAddressDto, List<FavoriteAddressDto> favoriteAddressDtoList, boolean removed) {

			}

			@Override
			public void onResult(List<FavoriteAddressDto> favoriteAddressDtoList) {

			}

			@Override
			public void onClickedAddress(@Nullable FavoriteAddressDto favoriteAddressDto) {
				if (favoriteAddressDto == null) {
					if (!selectedFavoriteLocation) {
						Toast.makeText(getContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();
						binding.commons.currentLocationRadio.setChecked(true);
					}
				} else {
					newSelectedAddressDto = favoriteAddressDto;
					binding.commons.selectedAddressName.setText(newSelectedAddressDto.getDisplayName());

					onSelectedFavoriteLocation(newSelectedAddressDto);
				}
				getParentFragmentManager().popBackStack();
			}
		});

		String tag = MapFragment.class.getName();
		getParentFragmentManager().beginTransaction().hide(OngoingNotificationSettingsFragment.this).add(R.id.fragment_container,
				mapFragment, tag).addToBackStack(tag).commitAllowingStateLoss();
	}


	public void onSelectedFavoriteLocation(FavoriteAddressDto favoriteAddressDto) {
		selectedFavoriteLocation = true;
		originalSelectedFavoriteAddressDto = favoriteAddressDto;

		ongoingNotificationDto.setDisplayName(favoriteAddressDto.getDisplayName());
		ongoingNotificationDto.setCountryCode(favoriteAddressDto.getCountryCode());
		ongoingNotificationDto.setZoneId(favoriteAddressDto.getZoneId());
		ongoingNotificationDto.setLatitude(Double.parseDouble(favoriteAddressDto.getLatitude()));
		ongoingNotificationDto.setLongitude(Double.parseDouble(favoriteAddressDto.getLongitude()));
		ongoingNotificationDto.setLocationType(LocationType.SelectedAddress);

	}

	public void onSelectedCurrentLocation() {
		ongoingNotificationDto.setLocationType(LocationType.CurrentLocation);
	}

	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}


	public void onCheckedWeatherProvider(WeatherProviderType weatherProviderType) {
		if (ongoingNotificationDto.getWeatherSourceType() != weatherProviderType) {
			ongoingNotificationDto.setWeatherSourceType(weatherProviderType);
		}
	}


	private OngoingNotificationDto createDefaultDto() {
		OngoingNotificationDto defaultDto = new OngoingNotiDtoOngoing();
		defaultDto.setLocationType(LocationType.CurrentLocation);
		defaultDto.setOn(true);
		defaultDto.setWeatherSourceType(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_key_met), true) ? WeatherProviderType.MET_NORWAY : WeatherProviderType.OWM_ONECALL);
		defaultDto.setTopPriorityKma(false);
		defaultDto.setUpdateIntervalMillis(0);
		defaultDto.setDataTypeOfIcon(WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE);

		return defaultDto;
	}

}