package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.TextUtil;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.flickr.FlickrImgObj;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class BaseSimpleCurrentConditionsFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleCurrentConditionsBinding binding;
	protected AqiCnGeolocalizedFeedResponse airQualityResponse;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected Double latitude;
	protected Double longitude;
	protected String addressName;
	protected String countryCode;
	protected WeatherDataSourceType mainWeatherDataSourceType;
	protected ZoneId zoneId;
	protected ZoneOffset zoneOffset;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock24.name()));

		Bundle bundle = getArguments();
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		mainWeatherDataSourceType = (WeatherDataSourceType) bundle.getSerializable(
				BundleKey.WeatherDataSource.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());

		zoneOffset = ZonedDateTime.now(zoneId).getOffset();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutSimpleCurrentConditionsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		/*
		MobileAds.initialize(getContext());
		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adView.loadAd(adRequest);

		 */

	}

	public BaseSimpleCurrentConditionsFragment setAirQualityResponse(AqiCnGeolocalizedFeedResponse airQualityResponse) {
		this.airQualityResponse = airQualityResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		setAqiValuesToViews();
	}

	public void setAqiValuesToViews() {
		AirQualityDto airQualityDto = AqicnResponseProcessor.makeAirQualityDto(getContext(), airQualityResponse, zoneOffset);
		String airQuality = null;

		if (airQualityDto.isSuccessful()) {
			Double distance = LocationDistance.distance(latitude, longitude, airQualityDto.getLatitude(), airQualityDto.getLongitude(),
					LocationDistance.Unit.KM);

			if (distance > 100.0) {
				airQuality = getString(R.string.noData);
			} else {
				airQuality = AqicnResponseProcessor.getGradeDescription((int) Double.parseDouble(airQualityResponse.getData().getAqi()));
			}
		} else {
			airQuality = getString(R.string.noData);
		}
		binding.airQuality.setText(airQuality);
	}


}