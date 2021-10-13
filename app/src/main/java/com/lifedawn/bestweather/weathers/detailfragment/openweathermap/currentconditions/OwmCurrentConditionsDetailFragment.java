package com.lifedawn.bestweather.weathers.detailfragment.openweathermap.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class OwmCurrentConditionsDetailFragment extends BaseDetailCurrentConditionsFragment {
	private OneCallResponse oneCallResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public OwmCurrentConditionsDetailFragment setOneCallResponse(OneCallResponse oneCallResponse) {
		this.oneCallResponse = oneCallResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		// 현재기온,체감기온,기압,습도,이슬점,운량,자외선지수,시정,풍속,돌풍,풍향,강우량,강설량,날씨상태(흐림 등)
		View weatherIconView = addGridItemView();
		View tempView = addGridItemView();
		View realFeelTempView = addGridItemView();
		View pressureView = addGridItemView();
		View humidityView = addGridItemView();
		View dewPointView = addGridItemView();
		View cloudCoverView = addGridItemView();
		View uvView = addGridItemView();
		View visibilityView = addGridItemView();
		View windDirectionView = addGridItemView();
		View windSpeedView = addGridItemView();
		View windStrengthView = addGridItemView();
		View windGustView = addGridItemView();
		View rainVolumeView = addGridItemView();
		View snowVolumeView = addGridItemView();

		setLabel(weatherIconView, R.string.weather);
		setLabel(tempView, R.string.temperature);
		setLabel(realFeelTempView, R.string.real_feel_temperature);
		setLabel(humidityView, R.string.humidity);
		setLabel(dewPointView, R.string.dew_point);
		setLabel(windDirectionView, R.string.wind_direction);
		setLabel(windSpeedView, R.string.wind_speed);
		setLabel(windGustView, R.string.wind_gust);
		setLabel(windStrengthView, R.string.wind_strength);
		setLabel(pressureView, R.string.pressure);
		setLabel(uvView, R.string.uv_index);
		setLabel(visibilityView, R.string.visibility);
		setLabel(cloudCoverView, R.string.cloud_cover);
		setLabel(rainVolumeView, R.string.rain_volume);
		setLabel(snowVolumeView, R.string.snow_volume);

		OneCallResponse.Current current = oneCallResponse.getCurrent();

		setValue(weatherIconView, OpenWeatherMapResponseProcessor.getWeatherIconDescription(current.getWeather().get(0).getIcon()));
		setValue(tempView, current.getTemp());
		setValue(realFeelTempView, current.getFeelsLike());
		setValue(humidityView, current.getHumidity());
		setValue(dewPointView, current.getDewPoint());
		setValue(windDirectionView, current.getWind_deg());
		setValue(windSpeedView, current.getWind_speed());
		setValue(windGustView, current.getWindGust());
		setValue(windStrengthView, WeatherResponseProcessor.getSimpleWindSpeedDescription(current.getWind_speed()));
		setValue(pressureView, current.getPressure());
		setValue(uvView, current.getUvi());
		setValue(visibilityView, current.getVisibility());
		setValue(cloudCoverView, current.getClouds());
		setValue(rainVolumeView, current.getRain() == null ? "-" : current.getRain().getPrecipitation1Hour());
		setValue(snowVolumeView, current.getSnow() == null ? "-" : current.getSnow().getPrecipitation1Hour());
	}
}
