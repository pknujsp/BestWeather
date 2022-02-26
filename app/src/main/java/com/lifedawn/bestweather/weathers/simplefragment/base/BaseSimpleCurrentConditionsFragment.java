package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class BaseSimpleCurrentConditionsFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleCurrentConditionsBinding binding;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected Double latitude;
	protected Double longitude;
	protected String addressName;
	protected String countryCode;
	protected WeatherProviderType mainWeatherProviderType;
	protected ZoneId zoneId;
	protected ZoneOffset zoneOffset;
	protected Bundle bundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();

		bundle = savedInstanceState != null ? savedInstanceState : getArguments();

		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		mainWeatherProviderType = (WeatherProviderType) bundle.getSerializable(
				BundleKey.WeatherProvider.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());

		zoneOffset = ZonedDateTime.now(zoneId).getOffset();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutSimpleCurrentConditionsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}


	@Override
	public void setValuesToViews() {
	}


}