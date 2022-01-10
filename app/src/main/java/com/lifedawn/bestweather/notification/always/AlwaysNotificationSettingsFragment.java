package com.lifedawn.bestweather.notification.always;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.Message;
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
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentBaseNotificationSettingsBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.notification.NotificationUpdateCallback;
import com.lifedawn.bestweather.notification.model.AlwaysNotiDataObj;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;


public class AlwaysNotificationSettingsFragment extends Fragment implements NotificationUpdateCallback {
	private final NotificationType notificationType = NotificationType.Always;
	private FragmentBaseNotificationSettingsBinding binding;

	private AlwaysNotiViewCreator alwaysNotiViewCreator;
	private boolean initializing = true;
	private AlwaysNotiHelper alwaysNotiHelper;
	private AlwaysNotiDataObj alwaysNotiDataObj;
	private FavoriteAddressDto newSelectedAddressDto;

	private long[] intervalsLong;
	private NotificationHelper notificationHelper;
	private boolean originalEnabled;
	private boolean selectedFavoriteLocation;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationHelper = new NotificationHelper(getActivity().getApplicationContext());

		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		intervalsLong = new long[intervalsStr.length];

		for (int i = 0; i < intervalsStr.length; i++) {
			intervalsLong[i] = Long.parseLong(intervalsStr[i]);
		}

		alwaysNotiViewCreator = new AlwaysNotiViewCreator(getActivity().getApplicationContext(), this);
		alwaysNotiHelper = new AlwaysNotiHelper(getActivity().getApplicationContext());

		initPreferences();
		alwaysNotiDataObj = alwaysNotiViewCreator.getNotificationDataObj();
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

		initLocation();
		initWeatherDataSource();
		initAutoRefreshInterval();

		binding.notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.settingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);

				if (!initializing) {
					PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
							.putBoolean(notificationType.getPreferenceName(), isChecked).commit();

					if (isChecked) {
						alwaysNotiViewCreator.savePreferences();
						alwaysNotiViewCreator.initNotification(new Handler(new Handler.Callback() {
							@Override
							public boolean handleMessage(@NonNull Message msg) {
								return false;
							}
						}));
						onSelectedAutoRefreshInterval(alwaysNotiDataObj.getUpdateIntervalMillis());
					} else {
						notificationHelper.cancelNotification(notificationType.getNotificationId());
						alwaysNotiHelper.cancelAutoRefresh();
					}
				}

			}
		});


		RemoteViews remoteViews = alwaysNotiViewCreator.createRemoteViews(true);
		View previewWidgetView = remoteViews.apply(getActivity().getApplicationContext(), binding.previewLayout);
		binding.previewLayout.addView(previewWidgetView);
	}

	@Override
	public void onResume() {
		super.onResume();
		binding.notificationSwitch.setChecked(originalEnabled);
		if (alwaysNotiDataObj.getLocationType() == LocationType.SelectedAddress) {
			selectedFavoriteLocation = true;
			binding.commons.selectedLocationRadio.setChecked(true);
			binding.commons.selectedAddressName.setText(alwaysNotiDataObj.getAddressName());
		} else {
			binding.commons.currentLocationRadio.setChecked(true);
		}

		binding.commons.kmaTopPrioritySwitch.setChecked(alwaysNotiDataObj.isTopPriorityKma());

		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		final long autoRefreshInterval = alwaysNotiDataObj.getUpdateIntervalMillis();

		for (int i = 0; i < intervalsStr.length; i++) {
			if (Long.parseLong(intervalsStr[i]) == autoRefreshInterval) {
				binding.commons.autoRefreshIntervalSpinner.setSelection(i);
				break;
			}
		}
		initializing = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	protected void initAutoRefreshInterval() {
		binding.commons.autoRefreshIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				long autoRefreshInterval = intervalsLong[position];
				onSelectedAutoRefreshInterval(autoRefreshInterval);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}


	protected void initWeatherDataSource() {
		binding.commons.weatherDataSourceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				WeatherDataSourceType checked = WeatherDataSourceType.OWM_ONECALL;
				onCheckedWeatherDataSource(checked);
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
		bundle.putString(BundleKey.RequestFragment.name(), AlwaysNotificationSettingsFragment.class.getName());
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

					newSelectedAddressDto = (FavoriteAddressDto) result.getSerializable(BundleKey.SelectedAddressDto.name());
					binding.commons.selectedAddressName.setText(newSelectedAddressDto.getAddress());

					SharedPreferences.Editor editor = getContext().getSharedPreferences(notificationType.getPreferenceName(),
							Context.MODE_PRIVATE).edit();

					editor.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), newSelectedAddressDto.getAddress())
							.putFloat(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(),
									Float.parseFloat(newSelectedAddressDto.getLatitude()))
							.putFloat(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(),
									Float.parseFloat(newSelectedAddressDto.getLongitude()))
							.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), newSelectedAddressDto.getCountryCode()).commit();

					onSelectedFavoriteLocation(newSelectedAddressDto);
				}
			}
		});

		String tag = FavoritesFragment.class.getName();

		getParentFragmentManager().beginTransaction().hide(AlwaysNotificationSettingsFragment.this).add(R.id.fragment_container,
				favoritesFragment,
				tag).addToBackStack(tag).commit();
	}


	public void onSelectedFavoriteLocation(FavoriteAddressDto favoriteAddressDto) {
		if (!initializing) {

			alwaysNotiDataObj.setAddressName(favoriteAddressDto.getAddress())
					.setCountryCode(favoriteAddressDto.getCountryCode())
					.setLatitude(Float.parseFloat(favoriteAddressDto.getLatitude())).setLongitude(Float.parseFloat(favoriteAddressDto.getLongitude()));
			alwaysNotiDataObj.setLocationType(LocationType.SelectedAddress);
			alwaysNotiViewCreator.savePreferences();
			alwaysNotiViewCreator.initNotification(new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(@NonNull Message msg) {
					return false;
				}
			}));
		}
	}

	public void onSelectedCurrentLocation() {
		if (!initializing) {
			alwaysNotiDataObj.setLocationType(LocationType.CurrentLocation);
			alwaysNotiViewCreator.savePreferences();

			alwaysNotiViewCreator.initNotification(new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(@NonNull Message msg) {
					return false;
				}
			}));
		}
	}

	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}


	public void onCheckedKmaPriority(boolean checked) {
		if (!initializing) {
			alwaysNotiDataObj.setTopPriorityKma(checked);
			alwaysNotiViewCreator.savePreferences();

			alwaysNotiViewCreator.initNotification(new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(@NonNull Message msg) {
					return false;
				}
			}));
		}
	}

	public void onCheckedWeatherDataSource(WeatherDataSourceType weatherDataSourceType) {
		if (!initializing) {
			if (alwaysNotiDataObj.getWeatherSourceType() != weatherDataSourceType) {

				alwaysNotiDataObj.setWeatherSourceType(weatherDataSourceType);
				alwaysNotiViewCreator.savePreferences();

				alwaysNotiViewCreator.initNotification(new Handler(new Handler.Callback() {
					@Override
					public boolean handleMessage(@NonNull Message msg) {
						return false;
					}
				}));
			}
		}
	}

	public void onSelectedAutoRefreshInterval(long val) {
		if (!initializing) {
			alwaysNotiDataObj.setUpdateIntervalMillis(val);
			alwaysNotiViewCreator.savePreferences();

			alwaysNotiHelper.onSelectedAutoRefreshInterval(alwaysNotiDataObj.getUpdateIntervalMillis());
		}
	}

	public void initPreferences() {
		alwaysNotiViewCreator.loadPreferences();
	}


}