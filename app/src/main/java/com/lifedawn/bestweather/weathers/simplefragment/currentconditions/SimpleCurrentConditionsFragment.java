package com.lifedawn.bestweather.weathers.simplefragment.currentconditions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class SimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	private CurrentConditionsDto currentConditionsDto;

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

	public void setCurrentConditionsDto(CurrentConditionsDto currentConditionsDto) {
		this.currentConditionsDto = currentConditionsDto;
	}

	@Override
	public void setValuesToViews() {
		setAqiValuesToViews();

		String precipitation = null;
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation = currentConditionsDto.getPrecipitationType() + " : " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = getString(R.string.not_precipitation);
		}
		binding.precipitation.setText(precipitation);

		binding.weatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
		binding.sky.setText(currentConditionsDto.getWeatherDescription());
		binding.wind.setText(currentConditionsDto.getWindStrength() != null ? currentConditionsDto.getWindStrength() :
				getString(R.string.noWindData));

		final String tempUnitStr = ValueUnits.convertToStr(getContext(), tempUnit);
		final String currentTempText = currentConditionsDto.getTemp().replace(tempUnitStr, "");

		binding.temperature.setText(currentTempText);
		binding.tempUnit.setText(tempUnitStr);

		binding.feelsLikeTemp.setText(currentConditionsDto.getFeelsLikeTemp().replace(tempUnitStr, ""));
		binding.feelsLikeTempUnit.setText(tempUnitStr);

		if (currentConditionsDto.getYesterdayTemp() != null) {
			int yesterdayTemp = ValueUnits.convertTemperature(currentConditionsDto.getYesterdayTemp().replace(tempUnitStr, ""), tempUnit);
			int todayTemp = Integer.parseInt(currentTempText);

			if (yesterdayTemp == todayTemp) {
				binding.tempDescription.setText(R.string.TheTemperatureIsTheSameAsYesterday);
			} else {
				String text = getString(R.string.thanYesterday);

				if (todayTemp > yesterdayTemp) {
					text += " " + (todayTemp - yesterdayTemp) + "° " + getString(R.string.higherTemperature);
				} else {
					text += " " + (yesterdayTemp - todayTemp) + "° " + getString(R.string.lowerTemperature);
				}
				binding.tempDescription.setText(text);
			}
			binding.tempDescription.setVisibility(View.VISIBLE);
		} else {
			binding.tempDescription.setVisibility(View.GONE);
		}
	}

}
