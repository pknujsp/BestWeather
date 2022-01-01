package com.lifedawn.bestweather.notification;

import android.content.Context;
import android.content.SharedPreferences;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.FragmentBaseNotificationSettingsBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;


import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class BaseNotificationSettingsFragment extends Fragment implements NotificationUpdateCallback {
	protected FragmentBaseNotificationSettingsBinding binding;
	protected boolean isKr;
	protected FavoriteAddressDto newSelectedAddressDto;
	protected NotificationType notificationType;
	protected long[] intervalsLong;
	protected NotificationHelper notificationHelper;
	protected boolean originalEnabled;
	protected boolean selectedFavoriteLocation;


	abstract public void onSwitchEnableNotification(boolean isChecked);

	abstract public void initPreferences();

	abstract public void onSelectedAutoRefreshInterval(long val);

	abstract public void onCheckedKmaPriority(boolean checked);

	abstract public void onCheckedWeatherDataSource(WeatherSourceType weatherSourceType);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notificationHelper = new NotificationHelper(getActivity().getApplicationContext());

		Locale locale = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = getResources().getConfiguration().getLocales().get(0);
		} else {
			locale = getResources().getConfiguration().locale;
		}
		String country = locale.getCountry();
		isKr = country.equals("KR");

		String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		intervalsLong = new long[intervalsStr.length];

		for (int i = 0; i < intervalsStr.length; i++) {
			intervalsLong[i] = Long.parseLong(intervalsStr[i]);
		}
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

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		if (isKr) {
			binding.commons.kmaTopPrioritySwitch.setVisibility(View.VISIBLE);
		} else {
			binding.commons.kmaTopPrioritySwitch.setVisibility(View.GONE);
		}

		SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.AutoRefreshIntervals));
		binding.commons.autoRefreshIntervalSpinner.setAdapter(spinnerAdapter);

		initLocation();
		initWeatherDataSource();
		initAutoRefreshInterval();
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

				WeatherSourceType checked = checkedId == R.id.accu_weather_radio ? WeatherSourceType.ACCU_WEATHER
						: WeatherSourceType.OPEN_WEATHER_MAP;
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
		bundle.putString(BundleKey.RequestFragment.name(), BaseNotificationSettingsFragment.class.getName());
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

		getParentFragmentManager().beginTransaction().hide(BaseNotificationSettingsFragment.this).add(R.id.fragment_container,
				favoritesFragment,
				tag).addToBackStack(tag).commit();
	}

	abstract public void onSelectedFavoriteLocation(FavoriteAddressDto favoriteAddressDto);

	abstract public void onSelectedCurrentLocation();


	@Override
	public void updateNotification(RemoteViews remoteViews) {

	}
}