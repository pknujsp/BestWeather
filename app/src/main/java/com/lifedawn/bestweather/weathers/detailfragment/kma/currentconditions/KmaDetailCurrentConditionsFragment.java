package com.lifedawn.bestweather.weathers.detailfragment.kma.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class KmaDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private FinalCurrentConditions finalCurrentConditions;
	
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
	
	@Override
	public void setValuesToViews() {
		// 기온,1시간강수량,습도,강수형태,풍향,풍속
		addGridItem(R.string.temperature, ValueUnits.convertTemperature(finalCurrentConditions.getTemperature(), tempUnit).toString(),
				R.drawable.temp_icon, null);
		addGridItem(R.string.humidity, finalCurrentConditions.getHumidity(), R.drawable.temp_icon, null);
		addGridItem(R.string.wind_direction, finalCurrentConditions.getWindDirection(), R.drawable.temp_icon, null);
		addGridItem(R.string.wind_speed, ValueUnits.convertWindSpeed(finalCurrentConditions.getWindSpeed(), windUnit).toString(),
				R.drawable.temp_icon, null);
		addGridItem(R.string.wind_strength, WeatherResponseProcessor.getSimpleWindSpeedDescription(finalCurrentConditions.getWindSpeed()),
				R.drawable.temp_icon, null);
		addGridItem(R.string.precipitation_volume, finalCurrentConditions.getPrecipitation1Hour(), R.drawable.temp_icon, null);
		addGridItem(R.string.precipitation_type,
				KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType()), R.drawable.temp_icon,
				null);
		
	}
}
