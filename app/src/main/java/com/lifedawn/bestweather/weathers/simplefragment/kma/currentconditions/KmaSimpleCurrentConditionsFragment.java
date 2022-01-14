package com.lifedawn.bestweather.weathers.simplefragment.kma.currentconditions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaCurrentConditions;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class KmaSimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	private FinalCurrentConditions todayFinalCurrentConditions;
	private FinalCurrentConditions yesterdayFinalCurrentConditions;
	private FinalHourlyForecast finalHourlyForecast;
	private KmaCurrentConditions kmaCurrentConditions;
	private KmaHourlyForecast kmaHourlyForecast;

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

	public KmaSimpleCurrentConditionsFragment setKmaHourlyForecast(KmaHourlyForecast kmaHourlyForecast) {
		this.kmaHourlyForecast = kmaHourlyForecast;
		return this;
	}

	public KmaSimpleCurrentConditionsFragment setKmaCurrentConditions(KmaCurrentConditions kmaCurrentConditions) {
		this.kmaCurrentConditions = kmaCurrentConditions;
		return this;
	}

	public KmaSimpleCurrentConditionsFragment setTodayFinalCurrentConditions(FinalCurrentConditions todayFinalCurrentConditions) {
		this.todayFinalCurrentConditions = todayFinalCurrentConditions;
		return this;
	}

	public KmaSimpleCurrentConditionsFragment setFinalHourlyForecast(FinalHourlyForecast finalHourlyForecast) {
		this.finalHourlyForecast = finalHourlyForecast;
		return this;
	}

	public void setYesterdayFinalCurrentConditions(FinalCurrentConditions yesterdayFinalCurrentConditions) {
		this.yesterdayFinalCurrentConditions = yesterdayFinalCurrentConditions;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();

		CurrentConditionsDto currentConditionsDto = null;
		if (todayFinalCurrentConditions != null) {
			currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfXML(getContext(), todayFinalCurrentConditions,
					finalHourlyForecast, windUnit, tempUnit, latitude, longitude);
		} else {
			currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(getContext(), kmaCurrentConditions, kmaHourlyForecast
					, windUnit, tempUnit, latitude, longitude);
		}

		String precipitation = currentConditionsDto.getPrecipitationType();
		if (currentConditionsDto.isHasPrecipitationVolume() && !currentConditionsDto.getPrecipitationVolume().equals("0.0mm")) {
			precipitation += ": " + currentConditionsDto.getPrecipitationVolume();
		}
		binding.precipitation.setText(precipitation);

		binding.weatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
		binding.sky.setText(currentConditionsDto.getWeatherDescription());

		final String tempUnitStr = ValueUnits.convertToStr(getContext(), tempUnit);
		final String currentTemp = currentConditionsDto.getTemp().replace(tempUnitStr, "");
		binding.temperature.setText(currentTemp);

		binding.wind.setText(currentConditionsDto.getWindStrength() != null ? currentConditionsDto.getWindStrength() :
				getString(R.string.noWindData));

		binding.feelsLikeTemp.setText(currentConditionsDto.getFeelsLikeTemp().replace(tempUnitStr, ""));
		binding.tempUnit.setText(tempUnitStr);
		binding.feelsLikeTempUnit.setText(tempUnitStr);

		int yesterdayTemp = ValueUnits.convertTemperature(yesterdayFinalCurrentConditions == null ? kmaCurrentConditions.getYesterdayTemp()
				: yesterdayFinalCurrentConditions.getTemperature(), tempUnit);
		int todayTemp = Integer.parseInt(currentTemp);

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
	}
}