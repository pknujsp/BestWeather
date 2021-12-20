package com.lifedawn.bestweather.weathers.detailfragment.kma.currentconditions;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindUtil;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.TimeZone;

public class KmaDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private FinalCurrentConditions finalCurrentConditions;
	private FinalHourlyForecast finalHourlyForecast;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValuesToViews();
	}

	public KmaDetailCurrentConditionsFragment setFinalCurrentConditions(FinalCurrentConditions finalCurrentConditions) {
		this.finalCurrentConditions = finalCurrentConditions;
		return this;
	}

	public void setFinalHourlyForecast(FinalHourlyForecast finalHourlyForecast) {
		this.finalHourlyForecast = finalHourlyForecast;
	}

	@Override
	public void setValuesToViews() {
		// 기온,1시간강수량,습도,강수형태,풍향,풍속

		CurrentConditionsDto currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDto(getContext(), finalCurrentConditions,
				finalHourlyForecast, windUnit, tempUnit, latitude, longitude);

		binding.conditionsGrid.requestLayout();

		addGridItem(R.string.weather, currentConditionsDto.getWeatherDescription(), currentConditionsDto.getWeatherIcon(), null);

		addGridItem(R.string.precipitation_volume, currentConditionsDto.getPrecipitationType(), R.drawable.precipitationvolume, null);

		addGridItem(R.string.temperature, currentConditionsDto.getTemp(),
				R.drawable.temperature, null);

		addGridItem(R.string.real_feel_temperature, currentConditionsDto.getFeelsLikeTemp(),
				R.drawable.realfeeltemperature, null);

		addGridItem(R.string.humidity, currentConditionsDto.getHumidity(), R.drawable.humidity,
				null);
		View windDirectionView = addGridItem(R.string.wind_direction, WindUtil.windDirection(getContext(),
				String.valueOf(currentConditionsDto.getWindDirectionDegree())), R.drawable.arrow,
				null);
		((ImageView) windDirectionView.findViewById(R.id.label_icon)).setRotation(currentConditionsDto.getWindDirectionDegree() + 180);
		addGridItem(R.string.wind_speed,
				currentConditionsDto.getWindSpeed(),
				R.drawable.windspeed, null);
		addGridItem(R.string.wind_strength, currentConditionsDto.getSimpleWindStrength(),
				R.drawable.windstrength, null);


	}
}
