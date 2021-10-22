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

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

public class BaseSimpleCurrentConditionsFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleCurrentConditionsBinding binding;
	protected GeolocalizedFeedResponse airQualityResponse;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.clock24.name()));
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
	
	public BaseSimpleCurrentConditionsFragment setAirQualityResponse(GeolocalizedFeedResponse airQualityResponse) {
		this.airQualityResponse = airQualityResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		setAqiValuesToViews();
	}
	
	public void setAqiValuesToViews() {
		if (airQualityResponse.getStatus().equals("ok")) {
			if (airQualityResponse.getData().getIaqi().getPm10() == null) {
				binding.pm10Grade.setText(R.string.not_data);
			} else {
				binding.pm10Grade.setText(AqicnResponseProcessor.getGradeDescription(
						Integer.parseInt(airQualityResponse.getData().getIaqi().getPm10().getValue())));
			}
			if (airQualityResponse.getData().getIaqi().getPm25() == null) {
				binding.pm25Grade.setText(R.string.not_data);
			} else {
				binding.pm25Grade.setText(AqicnResponseProcessor.getGradeDescription(
						Integer.parseInt(airQualityResponse.getData().getIaqi().getPm25().getValue())));
			}
			
		} else {
			binding.pm10Grade.setText(R.string.not_data);
			binding.pm25Grade.setText(R.string.not_data);
		}
	}
}