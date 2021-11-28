package com.lifedawn.bestweather.notification;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentBaseNotificationSettingsBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.notification.always.AlwaysNotificationSettingsFragment;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.widget.AbstractAppWidgetProvider;


import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class BaseNotificationSettingsFragment extends Fragment implements NotiViewCreator.NotificationUpdateCallback {
	protected FragmentBaseNotificationSettingsBinding binding;
	protected boolean isKr;
	protected FavoriteAddressDto newSelectedAddressDto;
	protected Gps gps;
	protected NotiViewCreator notiViewCreator;
	protected NotificationType notificationType;
	private long[] intervalsLong;
	private boolean initializing = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		gps = new Gps(requestOnGpsLauncher, requestLocationPermissionLauncher, moveToAppDetailSettingsLauncher);
		initLocation();
		initWeatherDataSource();
		initAutoRefreshInterval();

		SharedPreferences notiPreferences = getContext().getSharedPreferences(notificationType.getPreferenceName(),
				Context.MODE_PRIVATE);

		if (notiPreferences.getAll().isEmpty()) {
			binding.notificationSwitch.setChecked(false);
			binding.settingsLayout.setVisibility(View.GONE);
		} else {
			binding.notificationSwitch.setChecked(true);
			binding.settingsLayout.setVisibility(View.VISIBLE);

			if (notiPreferences.getString(NotificationKey.NotiAttributes.LOCATION_TYPE.name(), LocationType.SelectedAddress.name()).equals(LocationType.SelectedAddress.name())) {
				binding.selectedLocationRadio.setChecked(true);
			} else {
				binding.currentLocationRadio.setChecked(true);
			}

			if (notiPreferences.getString(NotificationKey.NotiAttributes.WEATHER_SOURCE_TYPE.name(),
					WeatherSourceType.OPEN_WEATHER_MAP.name()).equals(WeatherSourceType.OPEN_WEATHER_MAP.name())) {
				binding.owmRadio.setChecked(true);
			} else {
				binding.accuWeatherRadio.setChecked(true);
			}

			if (notiPreferences.getBoolean(NotificationKey.NotiAttributes.TOP_PRIORITY_KMA.name(), true)) {
				binding.kmaTopPrioritySwitch.setChecked(true);
			}

			long autoRefreshInterval = notiPreferences.getLong(NotificationKey.NotiAttributes.UPDATE_INTERVAL.name(), 0L);
			final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
			int i = 0;
			for (; i < intervalsStr.length; i++) {
				if (Long.parseLong(intervalsStr[i]) == autoRefreshInterval) {
					break;
				}
			}
			refreshIntervalOnItemSelectedListener.onItemSelected(null, null, i, 0);
		}

		notiViewCreator = new NotiViewCreator(getContext(), notificationType, this);
		notiPreferences.registerOnSharedPreferenceChangeListener(notiViewCreator);
		notiViewCreator.onSharedPreferenceChanged(notiPreferences, null);

		binding.notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.settingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);

				if (!isChecked) {
					getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE).edit().clear().apply();
				} else {
					notiViewCreator.initValues(getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE));
					initLocation();
					initWeatherDataSource();
					initAutoRefreshInterval();
				}

			}
		});


		updateNotification();
		initializing = false;
	}

	@Override
	public void onDestroy() {
		getContext().getSharedPreferences(notificationType.getPreferenceName(), Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(notiViewCreator);
		super.onDestroy();
	}

	Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			SharedPreferences.Editor editor = getContext().getSharedPreferences(notificationType.getPreferenceName(),
					Context.MODE_PRIVATE).edit();
			String key = preference.getKey();

			if (key.equals(NotificationKey.NotiAttributes.UPDATE_INTERVAL.name()))
				editor.putLong(key, (long) newValue);
			else if (key.equals(NotificationKey.NotiAttributes.LOCATION_TYPE.name()))
				editor.putString(key, (String) newValue);
			else if (key.equals(NotificationKey.NotiAttributes.TOP_PRIORITY_KMA.name()))
				editor.putBoolean(key, (boolean) newValue);
			else if (key.equals(NotificationKey.NotiAttributes.SELECTED_ADDRESS_DTO_ID.name()))
				editor.putInt(key, (int) newValue);
			else if (key.equals(NotificationKey.NotiAttributes.WEATHER_SOURCE_TYPE.name()))
				editor.putString(key, (String) newValue);

			editor.apply();
			return true;
		}
	};

	private void initAutoRefreshInterval() {
		final String[] intervalsDescription = getResources().getStringArray(R.array.AutoRefreshIntervals);
		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		intervalsLong = new long[intervalsStr.length];

		for (int i = 0; i < intervalsStr.length; i++) {
			intervalsLong[i] = Long.parseLong(intervalsStr[i]);
		}

		SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, intervalsDescription);
		binding.autoRefreshIntervalSpinner.setAdapter(spinnerAdapter);
		binding.autoRefreshIntervalSpinner.setOnItemSelectedListener(refreshIntervalOnItemSelectedListener);
	}

	private AdapterView.OnItemSelectedListener refreshIntervalOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (!initializing) {
				long autoRefreshInterval = intervalsLong[position];
				Preference preference = new Preference(getContext());
				preference.setKey(NotificationKey.NotiAttributes.UPDATE_INTERVAL.name());
				onPreferenceChangeListener.onPreferenceChange(preference, autoRefreshInterval);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	};

	private void initWeatherDataSource() {
		Locale locale = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = getResources().getConfiguration().getLocales().get(0);
		} else {
			locale = getResources().getConfiguration().locale;
		}
		String country = locale.getCountry();
		isKr = country.equals("KR");

		if (isKr) {
			binding.kmaTopPrioritySwitch.setVisibility(View.VISIBLE);
		} else {
			binding.kmaTopPrioritySwitch.setVisibility(View.GONE);
		}

		//기본 날씨 제공사 확인

		if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.pref_key_accu_weather), true)) {
			binding.accuWeatherRadio.setChecked(true);
		} else {
			binding.owmRadio.setChecked(true);
		}

		binding.weatherDataSourceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (!initializing) {
					Preference preference = new Preference(getContext());
					preference.setKey(NotificationKey.NotiAttributes.WEATHER_SOURCE_TYPE.name());
					onPreferenceChangeListener.onPreferenceChange(preference, checkedId == R.id.accu_weather_radio ? WeatherSourceType.ACCU_WEATHER.name()
							: WeatherSourceType.OPEN_WEATHER_MAP.name());
				}
			}
		});
		binding.kmaTopPrioritySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!initializing) {
					Preference preference = new Preference(getContext());
					preference.setKey(NotificationKey.NotiAttributes.TOP_PRIORITY_KMA.name());
					onPreferenceChangeListener.onPreferenceChange(preference, isChecked);
				}
			}
		});

	}

	private void initLocation() {
		binding.currentLocationRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!initializing) {
					if (isChecked) {
						//현재 위치
						//위치 권한, gps on 확인
						if (gps.checkPermissionAndGpsEnabled(getActivity(), locationCallback)) {
							Preference preference = new Preference(getContext());
							preference.setKey(NotificationKey.NotiAttributes.LOCATION_TYPE.name());
							onPreferenceChangeListener.onPreferenceChange(preference, LocationType.CurrentLocation.name());
						}
					}
				}
			}
		});

		binding.selectedLocationRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!initializing) {

					if (isChecked) {
						if (newSelectedAddressDto == null) {
							openFavoritesFragment();
						}
					}
				}
			}
		});

		binding.changeAddressBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openFavoritesFragment();
			}
		});
	}

	private void openFavoritesFragment() {
		FavoritesFragment favoritesFragment = new FavoritesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.RequestFragment.name(), BaseNotificationSettingsFragment.class.getName());
		favoritesFragment.setArguments(bundle);

		favoritesFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
			@Override
			public void onResultFragment(Bundle result) {
				if (result.getSerializable(BundleKey.SelectedAddressDto.name()) == null) {
					Toast.makeText(getContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();
					binding.selectedLocationRadio.setText(R.string.click_again_to_select_address);
					binding.selectedLocationRadio.setChecked(false);
					binding.currentLocationRadio.setChecked(false);
				} else {
					newSelectedAddressDto = (FavoriteAddressDto) result.getSerializable(BundleKey.SelectedAddressDto.name());
					String text = getString(R.string.location) + ", " + newSelectedAddressDto.getAddress();
					binding.selectedLocationRadio.setText(text);
					binding.changeAddressBtn.setVisibility(View.VISIBLE);

					Preference preference = new Preference(getContext());
					preference.setKey(NotificationKey.NotiAttributes.LOCATION_TYPE.name());
					onPreferenceChangeListener.onPreferenceChange(preference, LocationType.SelectedAddress.name());

					SharedPreferences.Editor editor = getContext().getSharedPreferences(notificationType.getPreferenceName(),
							Context.MODE_PRIVATE).edit();

					editor.putString(AbstractAppWidgetProvider.WidgetDataKeys.ADDRESS_NAME.name(), newSelectedAddressDto.getAddress())
							.putString(AbstractAppWidgetProvider.WidgetDataKeys.LATITUDE.name(), newSelectedAddressDto.getLatitude())
							.putString(AbstractAppWidgetProvider.WidgetDataKeys.LONGITUDE.name(), newSelectedAddressDto.getLongitude())
							.putString(AbstractAppWidgetProvider.WidgetDataKeys.COUNTRY_CODE.name(), newSelectedAddressDto.getCountryCode()).apply();
				}
			}
		});
		getParentFragmentManager().beginTransaction().hide(BaseNotificationSettingsFragment.this).add(R.id.fragment_container, favoritesFragment,
				getString(R.string.tag_favorites_fragment))
				.addToBackStack(getString(R.string.tag_favorites_fragment)).commit();
	}

	private final ActivityResultLauncher<Intent> requestOnGpsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					//gps 사용확인 화면에서 나온뒤 현재 위치 다시 파악
					LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
					boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

					if (isGpsEnabled) {
					} else {
						locationCallback.onFailed(Gps.LocationCallback.Fail.DISABLED_GPS);
					}
				}
			});

	private final ActivityResultLauncher<Intent> moveToAppDetailSettingsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if (ContextCompat.checkSelfPermission(getContext(),
							Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
						PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(
								getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false).apply();

					} else {
						locationCallback.onFailed(Gps.LocationCallback.Fail.REJECT_PERMISSION);
					}

				}
			});

	private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
			new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean isGranted) {
					//gps사용 권한
					//허가남 : 현재 위치 다시 파악
					//거부됨 : 작업 취소
					//계속 거부 체크됨 : 작업 취소
					if (isGranted) {
						PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(
								getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false).apply();
					} else {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
								Manifest.permission.ACCESS_FINE_LOCATION)) {
							PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(
									getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), true).apply();
						}
						locationCallback.onFailed(Gps.LocationCallback.Fail.REJECT_PERMISSION);
					}
				}
			});

	private final Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
		@Override
		public void onSuccessful(Location location) {
		}

		@Override
		public void onFailed(Fail fail) {
			if (fail == Fail.DISABLED_GPS) {
				Toast.makeText(getContext(), R.string.request_to_make_gps_on, Toast.LENGTH_SHORT).show();
			} else if (fail == Fail.REJECT_PERMISSION) {
				Toast.makeText(getContext(), R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();
			}

			binding.currentLocationRadio.setChecked(false);
			binding.selectedLocationRadio.setChecked(false);
		}
	};

	@Override
	public void updateNotification() {
		binding.previewLayout.removeAllViews();

		RemoteViews removeViews = notiViewCreator.createRemoteViews(true);
		View previewWidgetView = removeViews.apply(getContext(), binding.previewLayout);
		binding.previewLayout.addView(previewWidgetView);
	}
}