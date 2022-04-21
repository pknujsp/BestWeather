package com.lifedawn.bestweather.weathers.simplefragment.currentconditions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public class SimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	private CurrentConditionsDto currentConditionsDto;
	private AirQualityDto airQualityDto;
	private AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse;

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
		currentConditionsDto = (CurrentConditionsDto) bundle.getSerializable(WeatherDataType.currentConditions.name());
		aqiCnGeolocalizedFeedResponse = (AqiCnGeolocalizedFeedResponse) bundle.getSerializable("AqiCnGeolocalizedFeedResponse");
		airQualityDto = AqicnResponseProcessor.makeAirQualityDto(aqiCnGeolocalizedFeedResponse, ZonedDateTime.now(zoneId).getOffset());

		setValuesToViews();
	}


	@Override
	public void setValuesToViews() {
		String precipitation = null;
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitation = getString(R.string.precipitation_volume) + " : " + currentConditionsDto.getPrecipitationVolume();
		} else {
			precipitation = getString(R.string.not_precipitation);
		}
		binding.precipitation.setText(precipitation);

		binding.weatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
		binding.sky.setText(currentConditionsDto.getWeatherDescription());
		binding.wind.setText(currentConditionsDto.getWindStrength() != null ? currentConditionsDto.getWindStrength() :
				getString(R.string.noWindData));

		final String tempUnitStr = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String currentTempText = currentConditionsDto.getTemp().replace(tempUnitStr, "");

		binding.temperature.setText(currentTempText);
		binding.tempUnit.setText(tempUnitStr);

		binding.feelsLikeTemp.setText(currentConditionsDto.getFeelsLikeTemp().replace(tempUnitStr, ""));
		binding.feelsLikeTempUnit.setText(tempUnitStr);

		if (currentConditionsDto.getYesterdayTemp() != null) {
			binding.tempDescription.setText(WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, getContext()));

			binding.tempDescription.setVisibility(View.VISIBLE);
		} else {
			binding.tempDescription.setVisibility(View.GONE);
		}

		String airQuality = null;

		if (airQualityDto.isSuccessful()) {
			Double distance = LocationDistance.distance(latitude, longitude, airQualityDto.getLatitude(), airQualityDto.getLongitude(),
					LocationDistance.Unit.KM);

			if (distance > 100.0) {
				airQuality = getString(R.string.noData);
			} else {
				airQuality = AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
			}
		} else {
			airQuality = getString(R.string.noData);
		}
		binding.airQuality.setText(airQuality);
	}

}
