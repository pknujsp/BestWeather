package com.lifedawn.bestweather.weathers.simplefragment.accuweather.currentconditions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class AccuSimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	protected CurrentConditionsResponse currentConditionsResponse;

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

	public AccuSimpleCurrentConditionsFragment setCurrentConditionsResponse(CurrentConditionsResponse currentConditionsResponse) {
		this.currentConditionsResponse = currentConditionsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();

		List<CurrentConditionsResponse.Item> items = currentConditionsResponse.getItems();
		CurrentConditionsResponse.Item item = items.get(0);
		String precipitation = null;
		if (item.getHasPrecipitation().equals("true")) {
			// precipitation type 값 종류 : Rain, Snow, Ice, Null(Not), or Mixed
			String precipitationUnit = "mm";

			precipitation = AccuWeatherResponseProcessor.getPty(
					item.getPrecipitationType()) + ", " + item.getPrecip1hr().getMetric().getValue() + precipitationUnit;
		} else {
			precipitation = getString(R.string.not_precipitation);
		}
		binding.precipitation.setText(precipitation);

		binding.weatherIcon.setImageDrawable(
				ContextCompat.getDrawable(getContext(), AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon())));
		binding.sky.setText(AccuWeatherResponseProcessor.getWeatherIconDescription(item.getWeatherIcon()));
		String temp = ValueUnits.convertTemperature(item.getTemperature().getMetric().getValue(), tempUnit) + ValueUnits.convertToStr(
				getContext(), tempUnit);
		binding.temperature.setText(temp);
	}
}