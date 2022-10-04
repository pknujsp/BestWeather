package com.lifedawn.bestweather.notification.ongoing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.databinding.FragmentBaseNotificationSettingsBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.NotificationUpdateCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class OngoingNotificationSettingsFragment extends Fragment implements NotificationUpdateCallback {
	private final NotificationType notificationType = NotificationType.Ongoing;
	private final WidgetNotiConstants.DataTypeOfIcon[] dataTypeOfIcons =
			new WidgetNotiConstants.DataTypeOfIcon[]{WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE, WidgetNotiConstants.DataTypeOfIcon.WEATHER_ICON};

	private FragmentBaseNotificationSettingsBinding binding;

	private OngoingNotiViewCreator ongoingNotiViewCreator;
	private boolean initializing = true;
	private OngoingNotificationHelper ongoingNotificationHelper;
	private FavoriteAddressDto newSelectedAddressDto;
	private FavoriteAddressDto originalSelectedFavoriteAddressDto;

	private long[] intervalsLong;

	private NotificationHelper notificationHelper;
	private boolean originalEnabled;
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
		notificationHelper = new NotificationHelper(getActivity().getApplicationContext());

		getParentFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);

		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		intervalsLong = new long[intervalsStr.length];

		for (int i = 0; i < intervalsStr.length; i++) {
			intervalsLong[i] = Long.parseLong(intervalsStr[i]);
		}

		ongoingNotiViewCreator = new OngoingNotiViewCreator(getActivity().getApplicationContext(), this);
		ongoingNotificationHelper = new OngoingNotificationHelper(getActivity().getApplicationContext());

		initPreferences();
		originalEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(notificationType.getPreferenceName(),
				false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentBaseNotificationSettingsBinding.inflate(inflater);
		return binding.getRoot();
	}


	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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
		binding.notificationSwitch.setText(R.string.use_always_notification);

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

		binding.notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.settingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);

				if (!initializing) {
					PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
							.putBoolean(notificationType.getPreferenceName(), isChecked).commit();

					if (isChecked) {
						if (NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
							ongoingNotiViewCreator.savePreferences();
							ongoingNotiViewCreator.loadSavedPreferences();
							ongoingNotiViewCreator.initNotification(null);

							ongoingNotificationHelper.onSelectedAutoRefreshInterval(ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis());
						} else {
							Toast.makeText(getContext(), R.string.disabledNotification, Toast.LENGTH_SHORT).show();
							binding.notificationSwitch.setChecked(false);
							binding.settingsLayout.setVisibility(View.GONE);

							startActivity(IntentUtil.getNotificationSettingsIntent(getActivity()));
						}
					} else {
						ongoingNotificationHelper.cancelAutoRefresh();
						notificationHelper.cancelNotification(notificationType.getNotificationId());
					}
				}

			}
		});

		RemoteViews[] remoteViews = ongoingNotiViewCreator.createRemoteViews(true);
		final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		remoteViews[1].setViewPadding(R.id.root_layout, padding, padding, padding, padding);

		binding.previewLayout.addView(remoteViews[1].apply(getActivity().getApplicationContext(), binding.previewLayout));

		binding.notificationSwitch.setChecked(originalEnabled);
		if (ongoingNotiViewCreator.getNotificationDataObj().getLocationType() == LocationType.SelectedAddress) {
			originalSelectedFavoriteAddressDto = new FavoriteAddressDto();
			originalSelectedFavoriteAddressDto.setAddress(ongoingNotiViewCreator.getNotificationDataObj().getAddressName());
			originalSelectedFavoriteAddressDto.setAdmin(ongoingNotiViewCreator.getNotificationDataObj().getAdmin());
			originalSelectedFavoriteAddressDto.setCountryCode(ongoingNotiViewCreator.getNotificationDataObj().getCountryCode());
			originalSelectedFavoriteAddressDto.setLatitude(String.valueOf(ongoingNotiViewCreator.getNotificationDataObj().getLatitude()));
			originalSelectedFavoriteAddressDto.setLongitude(String.valueOf(ongoingNotiViewCreator.getNotificationDataObj().getLongitude()));
			originalSelectedFavoriteAddressDto.setZoneId(ongoingNotiViewCreator.getNotificationDataObj().getZoneId());

			selectedFavoriteLocation = true;

			binding.commons.selectedLocationRadio.setChecked(true);
			binding.commons.selectedAddressName.setText(ongoingNotiViewCreator.getNotificationDataObj().getAddressName());
		} else {
			binding.commons.currentLocationRadio.setChecked(true);
		}

		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		final long autoRefreshInterval = ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis();

		for (int i = 0; i < intervalsStr.length; i++) {
			if (Long.parseLong(intervalsStr[i]) == autoRefreshInterval) {
				binding.commons.autoRefreshIntervalSpinner.setSelection(i);
				break;
			}
		}
		binding.dataTypeOfIconSpinner.setSelection(ongoingNotiViewCreator.getNotificationDataObj().getDataTypeOfIcon() == WidgetNotiConstants.DataTypeOfIcon.TEMPERATURE
				? 0 : 1, false);
		binding.commons.kmaTopPrioritySwitch.setChecked(ongoingNotiViewCreator.getNotificationDataObj().isTopPriorityKma());
	}

	@Override
	public void onResume() {
		super.onResume();
		initializing = false;
	}

	@Override
	public void onDestroy() {
		getParentFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}

	protected void initAutoRefreshInterval() {
		binding.commons.autoRefreshIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			boolean init = true;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (!init) {
					ongoingNotiViewCreator.getNotificationDataObj().setUpdateIntervalMillis(intervalsLong[position]);
					ongoingNotiViewCreator.savePreferences();
					ongoingNotificationHelper.onSelectedAutoRefreshInterval(ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis());
				} else {
					init = false;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	protected void initDataTypeOfIconSpinner() {
		binding.dataTypeOfIconSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			boolean init = true;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (!init) {
					ongoingNotiViewCreator.getNotificationDataObj().setDataTypeOfIcon(dataTypeOfIcons[position]);
					ongoingNotiViewCreator.savePreferences();
					ongoingNotiViewCreator.initNotification(null);
				} else {
					init = false;
				}
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
				WeatherProviderType checked = checkedId == R.id.met_norway_radio ? WeatherProviderType.MET_NORWAY :
						WeatherProviderType.OWM_ONECALL;
				onCheckedWeatherProvider(checked);
			}
		});
		binding.commons.kmaTopPrioritySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onCheckedKmaPriority(isChecked);
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
					selectedFavoriteLocation = true;

					newSelectedAddressDto = favoriteAddressDto;
					binding.commons.selectedAddressName.setText(newSelectedAddressDto.getAddress());

					SharedPreferences.Editor editor = getContext().getSharedPreferences(notificationType.getPreferenceName(),
							Context.MODE_PRIVATE).edit();

					editor.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), newSelectedAddressDto.getAddress())
							.putFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(),
									Float.parseFloat(newSelectedAddressDto.getLatitude()))
							.putFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(),
									Float.parseFloat(newSelectedAddressDto.getLongitude()))
							.putString(WidgetNotiConstants.Commons.DataKeys.ZONE_ID.name(), newSelectedAddressDto.getZoneId())
							.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), newSelectedAddressDto.getCountryCode()).commit();

					onSelectedFavoriteLocation(newSelectedAddressDto);
				}
			}
		});

		String tag = MapFragment.class.getName();

		getParentFragmentManager().beginTransaction().hide(OngoingNotificationSettingsFragment.this).add(R.id.fragment_container,
				mapFragment, tag).addToBackStack(tag).commit();
	}


	public void onSelectedFavoriteLocation(FavoriteAddressDto favoriteAddressDto) {
		if (!initializing) {
			originalSelectedFavoriteAddressDto = favoriteAddressDto;
			ongoingNotiViewCreator.getNotificationDataObj().setAddressName(favoriteAddressDto.getAddress())
					.setCountryCode(favoriteAddressDto.getCountryCode())
					.setAdmin(favoriteAddressDto.getAdmin())
					.setZoneId(favoriteAddressDto.getZoneId())
					.setLatitude(Float.parseFloat(favoriteAddressDto.getLatitude())).setLongitude(Float.parseFloat(favoriteAddressDto.getLongitude()));

			ongoingNotiViewCreator.getNotificationDataObj().setLocationType(LocationType.SelectedAddress);
			ongoingNotiViewCreator.savePreferences();
			ongoingNotiViewCreator.loadSavedPreferences();
			ongoingNotiViewCreator.initNotification(null);
		}
	}

	public void onSelectedCurrentLocation() {
		if (!initializing) {
			ongoingNotiViewCreator.getNotificationDataObj().setLocationType(LocationType.CurrentLocation);
			ongoingNotiViewCreator.savePreferences();
			ongoingNotiViewCreator.initNotification(null);
		}
	}

	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}


	public void onCheckedKmaPriority(boolean checked) {
		if (!initializing) {
			ongoingNotiViewCreator.getNotificationDataObj().setTopPriorityKma(checked);
			ongoingNotiViewCreator.savePreferences();

			ongoingNotiViewCreator.initNotification(null);
		}
	}

	public void onCheckedWeatherProvider(WeatherProviderType weatherProviderType) {
		if (!initializing) {
			if (ongoingNotiViewCreator.getNotificationDataObj().getWeatherSourceType() != weatherProviderType) {
				ongoingNotiViewCreator.getNotificationDataObj().setWeatherSourceType(weatherProviderType);
				ongoingNotiViewCreator.savePreferences();
				ongoingNotiViewCreator.initNotification(null);
			}
		}
	}


	public void initPreferences() {
		ongoingNotiViewCreator.loadPreferences();
	}

}