package com.lifedawn.bestweather.weathers.simplefragment.currentconditions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public class SimpleCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	private static CurrentConditionsDto currentConditionsDto;
	private static AirQualityDto airQualityDto;

	public SimpleCurrentConditionsFragment setAirQualityDto(AirQualityDto airQualityDto) {
		this.airQualityDto = airQualityDto;
		return this;
	}

	public SimpleCurrentConditionsFragment setCurrentConditionsDto(CurrentConditionsDto currentConditionsDto) {
		this.currentConditionsDto = currentConditionsDto;
		return this;
	}

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
	}


	@Override
	public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
		super.onInflateFinished(view, resid, parent);
		setValuesToViews();

	}

	@Override
	public void setValuesToViews() {
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			String precipitation = getString(R.string.precipitation_volume) + " : " + currentConditionsDto.getPrecipitationVolume();
			binding.precipitation.setText(precipitation);
		} else {
			binding.precipitation.setVisibility(View.GONE);
		}

		if (currentConditionsDto.getWindDirection() != null) {
			binding.windDirectionArrow.setRotation(currentConditionsDto.getWindDirectionDegree() + 180);
		}
		binding.windDirectionArrow.setVisibility(currentConditionsDto.getWindDirection() == null ? View.GONE : View.VISIBLE);
		binding.windDirection.setVisibility(currentConditionsDto.getWindDirection() == null ? View.GONE : View.VISIBLE);

		Glide.with(binding.weatherIcon).load(currentConditionsDto.getWeatherIcon()).into(binding.weatherIcon);
		binding.sky.setText(currentConditionsDto.getWeatherDescription());
		binding.wind.setText(currentConditionsDto.getWindStrength() != null ? currentConditionsDto.getWindStrength() :
				getString(R.string.noWindData));

		binding.windDirection.setText(currentConditionsDto.getWindDirection());

		binding.humidity.setText(String.format("%s %s", getString(R.string.humidity), currentConditionsDto.getHumidity()));

		final String tempUnitStr = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String currentTempText = currentConditionsDto.getTemp().replace(tempUnitStr, "");

		binding.temperature.setText(currentTempText);
		binding.tempUnit.setText(tempUnitStr);

		binding.feelsLikeTemp.setText(currentConditionsDto.getFeelsLikeTemp().replace(tempUnitStr, ""));
		binding.feelsLikeTempUnit.setText(tempUnitStr);

		if (currentConditionsDto.getYesterdayTemp() != null) {
			binding.tempDescription.setText(WeatherUtil.makeTempCompareToYesterdayText(currentConditionsDto.getTemp(),
					currentConditionsDto.getYesterdayTemp(), tempUnit, requireContext().getApplicationContext()));
			binding.tempDescription.setVisibility(View.VISIBLE);
		} else {
			binding.tempDescription.setVisibility(View.GONE);
		}

		String airQuality = null;

		if (airQualityDto.isSuccessful()) {
			final double distance = LocationDistance.distance(latitude, longitude, airQualityDto.getLatitude(), airQualityDto.getLongitude(),
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
