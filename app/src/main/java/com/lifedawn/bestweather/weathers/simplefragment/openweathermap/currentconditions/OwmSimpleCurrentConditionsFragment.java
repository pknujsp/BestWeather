package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.currentconditions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.currentweather.OwmCurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class OwmSimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	private OwmOneCallResponse owmOneCallResponse;
	private OwmCurrentConditionsResponse owmCurrentConditionsResponse;

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

	public OwmSimpleCurrentConditionsFragment setOneCallResponse(OwmOneCallResponse owmOneCallResponse) {
		this.owmOneCallResponse = owmOneCallResponse;
		return this;
	}

	public void setOwmCurrentConditionsResponse(OwmCurrentConditionsResponse owmCurrentConditionsResponse) {
		this.owmCurrentConditionsResponse = owmCurrentConditionsResponse;
	}


	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		CurrentConditionsDto currentConditionsDto = null;
		if (mainWeatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoOneCall(getContext(), owmOneCallResponse
					, windUnit, tempUnit, visibilityUnit);
		} else if (mainWeatherDataSourceType == WeatherDataSourceType.OWM_INDIVIDUAL) {
			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoIndividual(getContext(), owmCurrentConditionsResponse
					, windUnit, tempUnit, visibilityUnit);
		}

		if (currentConditionsDto.isHasPrecipitationVolume()) {
			String precipitation = null;

			if (currentConditionsDto.isHasRainVolume() && currentConditionsDto.isHasSnowVolume()) {
				precipitation = getString(
						R.string.owm_icon_616_rain_and_snow) + ": " + getString(R.string.rain) + " - " + currentConditionsDto.getRainVolume() +
						", " + getString(R.string.snow) + " - " + currentConditionsDto.getSnowVolume();
			} else if (currentConditionsDto.isHasRainVolume()) {
				precipitation = getString(R.string.rain) + ": " + currentConditionsDto.getRainVolume();
			} else {
				precipitation = getString(R.string.snow) + ": " + currentConditionsDto.getSnowVolume();
			}
			binding.precipitation.setText(precipitation);
		} else {
			binding.precipitation.setText(R.string.not_precipitation);
		}

		binding.wind.setText(currentConditionsDto.getWindStrength());

		binding.weatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
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