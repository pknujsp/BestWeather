package com.lifedawn.bestweather.weathers.detailfragment.currentconditions;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;

import org.jetbrains.annotations.NotNull;

public class DetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private CurrentConditionsDto currentConditionsDto;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValuesToViews();
	}

	@Override
	public void setValuesToViews() {
		binding.conditionsGrid.removeAllViews();

		if (currentConditionsDto.getWeatherDescription() != null) {
			addGridItem(R.string.weather, currentConditionsDto.getWeatherDescription(),
					currentConditionsDto.getWeatherIcon());
		}

		if (currentConditionsDto.getPrecipitationType() != null) {
			addGridItem(R.string.precipitation_type, currentConditionsDto.getPrecipitationType(), null);
		}
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			addGridItem(R.string.precipitation_volume, currentConditionsDto.getPrecipitationVolume(), null);
		}
		if (currentConditionsDto.isHasRainVolume()) {
			addGridItem(R.string.rain_volume, currentConditionsDto.getRainVolume(), null);
		}
		if (currentConditionsDto.isHasSnowVolume()) {
			addGridItem(R.string.snow_volume, currentConditionsDto.getSnowVolume(), null);
		}

		if (currentConditionsDto.getTemp() != null) {
			addGridItem(R.string.temperature, currentConditionsDto.getTemp(),
					null);
		}
		if (currentConditionsDto.getFeelsLikeTemp() != null) {
			addGridItem(R.string.real_feel_temperature, currentConditionsDto.getFeelsLikeTemp(),
					null);
		}
		if (currentConditionsDto.getHumidity() != null) {
			addGridItem(R.string.humidity, currentConditionsDto.getHumidity(), null);
		}
		if (currentConditionsDto.getWindDirection() != null) {
			View windDirectionView = addGridItem(R.string.wind_direction, currentConditionsDto.getWindDirection(),
					R.drawable.arrow);
			((ImageView) windDirectionView.findViewById(R.id.label_icon)).setRotation(currentConditionsDto.getWindDirectionDegree() + 180);
		}
		if (currentConditionsDto.getWindSpeed() != null) {
			addGridItem(R.string.wind_speed,
					currentConditionsDto.getWindSpeed(), null);
		}
		if (currentConditionsDto.getWindGust() != null) {
			addGridItem(R.string.wind_gust,
					currentConditionsDto.getWindGust() == null ? getString(R.string.not_available) :
							currentConditionsDto.getWindGust(), null);
		}
		if (currentConditionsDto.getWindStrength() != null) {
			addGridItem(R.string.wind_strength, currentConditionsDto.getSimpleWindStrength(), null);
		}
		if (currentConditionsDto.getPressure() != null) {
			addGridItem(R.string.pressure, currentConditionsDto.getPressure(), null);
		}
		if (currentConditionsDto.getVisibility() != null) {
			addGridItem(R.string.visibility, currentConditionsDto.getVisibility(), null);
		}
		if (currentConditionsDto.getCloudiness() != null) {
			addGridItem(R.string.cloud_cover, currentConditionsDto.getCloudiness(), null);
		}
		if (currentConditionsDto.getDewPoint() != null) {
			addGridItem(R.string.dew_point, currentConditionsDto.getDewPoint(), null);
		}
		if (currentConditionsDto.getUvIndex() != null) {
			addGridItem(R.string.uv_index, currentConditionsDto.getUvIndex(), null);
		}
	}

	public void setCurrentConditionsDto(CurrentConditionsDto currentConditionsDto) {
		this.currentConditionsDto = currentConditionsDto;
	}
}