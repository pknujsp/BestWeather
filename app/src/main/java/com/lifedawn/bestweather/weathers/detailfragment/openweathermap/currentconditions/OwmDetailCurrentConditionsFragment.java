package com.lifedawn.bestweather.weathers.detailfragment.openweathermap.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailCurrentConditionsAdapter;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OwmDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private OneCallResponse oneCallResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public OwmDetailCurrentConditionsFragment setOneCallResponse(OneCallResponse oneCallResponse) {
		this.oneCallResponse = oneCallResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		// 현재기온,체감기온,기압,습도,이슬점,운량,자외선지수,시정,풍속,돌풍,풍향,강우량,강설량,날씨상태(흐림 등)
		OneCallResponse.Current current = oneCallResponse.getCurrent();

		addGridItem(R.string.weather, OpenWeatherMapResponseProcessor.getWeatherIconDescription(current.getWeather().get(0).getIcon()), 0);
		addGridItem(R.string.temperature, current.getTemp(), 0);
		addGridItem(R.string.real_feel_temperature, current.getFeelsLike(), 0);
		addGridItem(R.string.humidity, current.getHumidity(), 0);
		addGridItem(R.string.dew_point, current.getDewPoint(), 0);
		addGridItem(R.string.wind_direction, current.getWind_deg(), 0);
		addGridItem(R.string.wind_speed, current.getWind_speed(), 0);
		addGridItem(R.string.wind_gust, current.getWindGust(), 0);
		addGridItem(R.string.wind_strength, WeatherResponseProcessor.getSimpleWindSpeedDescription(current.getWind_speed()), 0);
		addGridItem(R.string.pressure, current.getPressure(), 0);
		addGridItem(R.string.uv_index, current.getUvi(), 0);
		addGridItem(R.string.visibility, current.getVisibility(), 0);
		addGridItem(R.string.cloud_cover, current.getClouds(), 0);
		addGridItem(R.string.rain_volume, current.getRain() == null ? "-" : current.getRain().getPrecipitation1Hour(), 0);
		addGridItem(R.string.snow_volume, current.getSnow() == null ? "-" : current.getSnow().getPrecipitation1Hour(), 0);
	}
}
