package com.lifedawn.bestweather.weathers.simplefragment.kma.currentconditions;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class KmaSimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	private FinalCurrentConditions finalCurrentConditions;
	private FinalHourlyForecast finalHourlyForecast;


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

	public KmaSimpleCurrentConditionsFragment setFinalCurrentConditions(FinalCurrentConditions finalCurrentConditions) {
		this.finalCurrentConditions = finalCurrentConditions;
		return this;
	}

	public KmaSimpleCurrentConditionsFragment setFinalHourlyForecast(FinalHourlyForecast finalHourlyForecast) {
		this.finalHourlyForecast = finalHourlyForecast;
		return this;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();

		// precipitation type 값 종류 : Rain, Snow, Ice, Null(Not), or Mixed
		String precipitation = KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType());

		if (Double.parseDouble(finalCurrentConditions.getPrecipitation1Hour()) > 0.0) {
			String precipitationUnit = "mm";

			precipitation += ", " + finalCurrentConditions.getPrecipitation1Hour() + precipitationUnit;
		}

		binding.precipitation.setText(precipitation);
		binding.weatherIcon.setImageDrawable(
				ContextCompat.getDrawable(getContext(), KmaResponseProcessor.getWeatherSkyIconImg(finalHourlyForecast.getSky(), false)));
		binding.sky.setText(KmaResponseProcessor.getWeatherSkyIconDescription(finalHourlyForecast.getSky()));
		String temp = ValueUnits.convertTemperature(finalCurrentConditions.getTemperature(),
				tempUnit).toString() + (tempUnit == ValueUnits.celsius ? getString(R.string.celsius) : getString(R.string.fahrenheit));
		binding.temperature.setText(temp);
	}
}