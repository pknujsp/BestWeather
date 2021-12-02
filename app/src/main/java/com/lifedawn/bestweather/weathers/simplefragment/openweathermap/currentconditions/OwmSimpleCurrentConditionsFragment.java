package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.currentconditions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class OwmSimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	private OneCallResponse oneCallResponse;

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

	public OwmSimpleCurrentConditionsFragment setOneCallResponse(OneCallResponse oneCallResponse) {
		this.oneCallResponse = oneCallResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		OneCallResponse.Current current = oneCallResponse.getCurrent();
		if (current.getRain() != null || current.getSnow() != null) {
			String precipitation = null;
			String precipitationUnit = "mm";

			if (current.getRain() != null && current.getSnow() != null) {
				precipitation = getString(
						R.string.owm_icon_616_rain_and_snow) + ", " + current.getRain().getPrecipitation1Hour() + precipitationUnit + ", " + current.getSnow().getPrecipitation1Hour() + precipitationUnit;
			} else if (current.getRain() != null) {
				precipitation = getString(R.string.rain) + ", " + current.getRain().getPrecipitation1Hour() + precipitationUnit;
			} else {
				precipitation = getString(R.string.snow) + ", " + current.getSnow().getPrecipitation1Hour() + precipitationUnit;
			}
			binding.precipitation.setText(precipitation);
		} else {
			binding.precipitation.setText(R.string.not_precipitation);
		}

		binding.wind.setText(WeatherResponseProcessor.getWindSpeedDescription(current.getWind_speed()));

		binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),
				OpenWeatherMapResponseProcessor.getWeatherIconImg(current.getWeather().get(0).getId(), false)));
		binding.sky.setText(OpenWeatherMapResponseProcessor.getWeatherIconDescription(current.getWeather().get(0).getId()));
		String temp = ValueUnits.convertTemperature(current.getTemp(), tempUnit).toString() + ValueUnits.convertToStr(getContext(),
				tempUnit);
		binding.temperature.setText(temp);
	}
}