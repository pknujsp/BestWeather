package com.lifedawn.bestweather.weathers.viewpager;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.preference.PreferenceManager;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.FavoriteAddressType;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;


public class WeatherFragment extends Fragment {
	private FragmentWeatherBinding binding;
	private FavoriteAddressDto selectedAddress;
	private FavoriteAddressType favoriteAddressType;
	private IGps iGps;
	private WeatherViewModel.ILoadImgOfCurrentConditions iLoadImgOfCurrentConditions;
	private WeatherViewModel weatherViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		favoriteAddressType = (FavoriteAddressType) bundle.getSerializable(getString(R.string.bundle_key_favorite_address_type));

		if (favoriteAddressType == FavoriteAddressType.SelectedAddress) {
			selectedAddress = (FavoriteAddressDto) bundle.getSerializable(getString(R.string.bundle_key_selected_address));
		}

		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		iLoadImgOfCurrentConditions = weatherViewModel.getiLoadImgOfCurrentConditions();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentWeatherBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.customProgressView.setContentView(binding.scrollView);

		if (favoriteAddressType == FavoriteAddressType.SelectedAddress) {
			binding.addressName.setText(selectedAddress.getAddress());
			refreshForSelectedLocation();
		} else if (favoriteAddressType == FavoriteAddressType.CurrentLocation) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
			final Double lastLatitude =
					Double.parseDouble(sharedPreferences.getString(getString(R.string.pref_key_last_current_location_latitude), "0"));
			final Double lastLongitude = Double.parseDouble(sharedPreferences.getString(getString(R.string.pref_key_last_current_location_longitude),
					"0"));

			if (lastLatitude == 0 && lastLongitude == 0) {
				iGps.setUseGps();
			} else {
				setCurrentLocationAddressName(lastLatitude, lastLongitude, true);
			}
		}
	}

	public void setiGps(IGps iGps) {
		this.iGps = iGps;
	}

	private void setCurrentLocationAddressName(Double latitude, Double longitude, boolean refresh) {
		Geocoding.geocoding(getContext(), latitude, longitude, new Geocoding.GeocodingCallback() {
			@Override
			public void onGeocodingResult(List<Address> addressList) {
				if (addressList.isEmpty()) {

				} else {
					Address address = addressList.get(0);
					String addressName = getString(R.string.current_location) + ", " + address.getAddressLine(0);
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								binding.addressName.setText(addressName);
								if (refresh) {
									refresh(latitude, longitude, address.getCountryCode());
								}
							}
						});
					}
				}
			}
		});
	}

	public void refreshForCurrentLocation(Location currentLocation) {
		setCurrentLocationAddressName(currentLocation.getLatitude(), currentLocation.getLongitude(), true);
	}

	public void refreshForSelectedLocation() {
		refresh(Double.parseDouble(selectedAddress.getLatitude()), Double.parseDouble(selectedAddress.getLongitude()), selectedAddress.getCountryCode());
	}

	private void refresh(Double latitude, Double longitude, String countryCode) {
		binding.customProgressView.onStartedProcessingData(getString(R.string.msg_refreshing_weather_data));

		Set<MainProcessing.WeatherSourceType> weatherSourceTypeSet = new ArraySet<>();
		weatherSourceTypeSet.add(MainProcessing.WeatherSourceType.AQICN);

		if (countryCode.equals("KR")) {
			weatherSourceTypeSet.add(MainProcessing.WeatherSourceType.KMA);
		}

		SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
		if (sharedPreferences.getBoolean(getString(R.string.pref_key_accu_weather), true)) {
			weatherSourceTypeSet.add(MainProcessing.WeatherSourceType.ACCU_WEATHER);
		}
		if (sharedPreferences.getBoolean(getString(R.string.pref_key_open_weather_map), true)) {
			weatherSourceTypeSet.add(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP);
		}

		Log.e(RetrofitClient.LOG_TAG, "날씨 정보 요청, " + weatherSourceTypeSet.toString());
		iLoadImgOfCurrentConditions.loadImgOfCurrentConditions(null);
		/*
		MainProcessing.downloadWeatherData(getContext(), latitude.toString(), longitude.toString(), weatherSourceTypeSet
				, new MultipleJsonDownloader<JsonElement>() {
					@Override
					public void onResult() {
						Set<Map.Entry<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType,
								ResponseResult<JsonElement>>>> entrySet = responseMap.entrySet();

						for (Map.Entry<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType,
								ResponseResult<JsonElement>>> entry : entrySet) {
							MainProcessing.WeatherSourceType weatherSourceType = entry.getKey();
						}
					}
				});

		 */
		binding.customProgressView.onSuccessfulProcessingData();
	}

	public FavoriteAddressType getFavoriteAddressType() {
		return favoriteAddressType;
	}

	public boolean isFragmentUsingCurrentLocation() {
		return favoriteAddressType == FavoriteAddressType.CurrentLocation;
	}
}