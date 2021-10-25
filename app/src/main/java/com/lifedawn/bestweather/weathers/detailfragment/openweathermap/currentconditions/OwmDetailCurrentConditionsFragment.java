package com.lifedawn.bestweather.weathers.detailfragment.openweathermap.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.accuweather.ValuesUnit;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class OwmDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private OneCallResponse oneCallResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValuesToViews();
	}

	public OwmDetailCurrentConditionsFragment setOneCallResponse(OneCallResponse oneCallResponse) {
		this.oneCallResponse = oneCallResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		// 현재기온,체감기온,기압,습도,이슬점,운량,자외선지수,시정,풍속,돌풍,풍향,강우량,강설량,날씨상태(흐림 등)
		OneCallResponse.Current current = oneCallResponse.getCurrent();
		String notData = getString(R.string.not_data);

		addGridItem(R.string.weather, OpenWeatherMapResponseProcessor.getWeatherIconDescription(current.getWeather().get(0).getId()),
				R.drawable.temp_icon, null);
		addGridItem(R.string.temperature, ValueUnits.convertTemperature(current.getTemp(), tempUnit).toString(), R.drawable.temp_icon,
				null);
		addGridItem(R.string.real_feel_temperature, ValueUnits.convertTemperature(current.getFeelsLike(), tempUnit).toString(),
				R.drawable.temp_icon, null);
		addGridItem(R.string.humidity, current.getHumidity(), R.drawable.temp_icon, null);
		addGridItem(R.string.dew_point, ValueUnits.convertTemperature(current.getDewPoint(), tempUnit).toString(), R.drawable.temp_icon,
				null);
		addGridItem(R.string.wind_direction, current.getWind_deg(), R.drawable.temp_icon, null);
		addGridItem(R.string.wind_speed, ValueUnits.convertWindSpeed(current.getWind_speed(), windUnit).toString(), R.drawable.temp_icon,
				null);
		addGridItem(R.string.wind_gust, current.getWindGust() == null ? notData : ValueUnits.convertWindSpeed(current.getWindGust(),
				windUnit).toString(), R.drawable.temp_icon,
				null);
		addGridItem(R.string.wind_strength, WeatherResponseProcessor.getSimpleWindSpeedDescription(current.getWind_speed()),
				R.drawable.temp_icon, null);
		addGridItem(R.string.pressure, current.getPressure(), R.drawable.temp_icon, null);
		addGridItem(R.string.uv_index, current.getUvi(), R.drawable.temp_icon, null);
		addGridItem(R.string.visibility, ValueUnits.convertVisibility(current.getVisibility(), visibilityUnit),
				R.drawable.temp_icon, null);
		addGridItem(R.string.cloud_cover, current.getClouds(), R.drawable.temp_icon, null);
		addGridItem(R.string.rain_volume, current.getRain() == null ? "0" : current.getRain().getPrecipitation1Hour(), R.drawable.temp_icon,
				null);
		addGridItem(R.string.snow_volume, current.getSnow() == null ? "0" : current.getSnow().getPrecipitation1Hour(), R.drawable.temp_icon,
				null);
	}
}
