package com.lifedawn.bestweather.weathers.simplefragment.accuweather.currentconditions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.AccuCurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;


public class AccuSimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	protected AccuCurrentConditionsResponse accuCurrentConditionsResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValuesToViews();
	}

	public AccuSimpleCurrentConditionsFragment setCurrentConditionsResponse(AccuCurrentConditionsResponse accuCurrentConditionsResponse) {
		this.accuCurrentConditionsResponse = accuCurrentConditionsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();

		CurrentConditionsDto currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(getContext(), accuCurrentConditionsResponse.getItems().get(0), windUnit,
				tempUnit, visibilityUnit);

		String precipitation = null;
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation = currentConditionsDto.getPrecipitationType() + ": " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = currentConditionsDto.getPrecipitationType();
		}
		binding.precipitation.setText(precipitation);

		binding.weatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
		binding.wind.setText(currentConditionsDto.getWindStrength());
		binding.sky.setText(currentConditionsDto.getWeatherDescription());

		final String tempUnitStr = ValueUnits.convertToStr(getContext(), tempUnit);
		final String temp = currentConditionsDto.getTemp().replace(tempUnitStr, "");
		binding.temperature.setText(temp);
		binding.wind.setText(currentConditionsDto.getWindStrength());

		binding.feelsLikeTemp.setText(currentConditionsDto.getFeelsLikeTemp().replace(tempUnitStr, ""));
		binding.tempUnit.setText(tempUnitStr);
		binding.feelsLikeTempUnit.setText(tempUnitStr);

		binding.tempDescription.setVisibility(View.GONE);
	}
}