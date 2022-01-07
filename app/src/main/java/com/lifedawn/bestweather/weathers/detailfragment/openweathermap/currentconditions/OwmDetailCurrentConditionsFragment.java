package com.lifedawn.bestweather.weathers.detailfragment.openweathermap.currentconditions;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.currentweather.OwmCurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindUtil;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;

import org.jetbrains.annotations.NotNull;

public class OwmDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private OwmOneCallResponse owmOneCallResponse;
	private OwmCurrentConditionsResponse owmCurrentConditionsResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValuesToViews();
	}

	public OwmDetailCurrentConditionsFragment setOneCallResponse(OwmOneCallResponse owmOneCallResponse) {
		this.owmOneCallResponse = owmOneCallResponse;
		return this;
	}

	public OwmDetailCurrentConditionsFragment setOwmCurrentConditionsResponse(OwmCurrentConditionsResponse owmCurrentConditionsResponse) {
		this.owmCurrentConditionsResponse = owmCurrentConditionsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		// 현재기온,체감기온,기압,습도,이슬점,운량,자외선지수,시정,풍속,돌풍,풍향,강우량,강설량,날씨상태(흐림 등)
		CurrentConditionsDto currentConditionsDto = null;
		if (mainWeatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoOneCall(getContext(), owmOneCallResponse
					, windUnit, tempUnit, visibilityUnit);
		} else if (mainWeatherDataSourceType == WeatherDataSourceType.OWM_INDIVIDUAL) {
			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoIndividual(getContext(), owmCurrentConditionsResponse
					, windUnit, tempUnit, visibilityUnit);
		}

		binding.conditionsGrid.removeAllViews();

		addGridItem(R.string.weather, currentConditionsDto.getWeatherDescription(),
				currentConditionsDto.getWeatherIcon());
		addGridItem(R.string.temperature, currentConditionsDto.getTemp(),
				null);
		addGridItem(R.string.real_feel_temperature, currentConditionsDto.getFeelsLikeTemp(),
				null);
		addGridItem(R.string.humidity, currentConditionsDto.getHumidity(), null);
		View windDirectionView = addGridItem(R.string.wind_direction, currentConditionsDto.getWindDirection(),
				R.drawable.arrow);
		((ImageView) windDirectionView.findViewById(R.id.label_icon)).setRotation(currentConditionsDto.getWindDirectionDegree() + 180);
		addGridItem(R.string.wind_speed,
				currentConditionsDto.getWindSpeed(), null);
		addGridItem(R.string.wind_gust,
				currentConditionsDto.getWindGust() == null ? getString(R.string.not_available) :
						currentConditionsDto.getWindGust(), null);
		addGridItem(R.string.wind_strength, currentConditionsDto.getSimpleWindStrength(), null);
		addGridItem(R.string.pressure, currentConditionsDto.getPressure(), null);
		addGridItem(R.string.visibility,
				currentConditionsDto.getVisibility(),
				null);
		addGridItem(R.string.cloud_cover, currentConditionsDto.getCloudiness(), null);
		if (currentConditionsDto.isHasRainVolume()) {
			addGridItem(R.string.rain_volume, currentConditionsDto.getRainVolume(), null);
		}
		if (currentConditionsDto.isHasSnowVolume()) {
			addGridItem(R.string.snow_volume, currentConditionsDto.getSnowVolume(), null);
		}
	}
}
