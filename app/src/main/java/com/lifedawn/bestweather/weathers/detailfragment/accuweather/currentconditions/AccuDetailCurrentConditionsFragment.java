package com.lifedawn.bestweather.weathers.detailfragment.accuweather.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class AccuDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private CurrentConditionsResponse currentConditionsResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setValuesToViews();
	}
	
	public AccuDetailCurrentConditionsFragment setCurrentConditionsResponse(CurrentConditionsResponse currentConditionsResponse) {
		this.currentConditionsResponse = currentConditionsResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		//날씨 아이콘, 기온, 체감기온, 습도, 이슬점, 풍향, 풍속, 돌풍, 바람세기, 기압, 자외선, 시정거리,
		//운량, 강수량, 강수형태
		CurrentConditionsResponse.Item item = currentConditionsResponse.getItems().get(0);
		
		addGridItem(R.string.temperature, ValueUnits.convertTemperature(item.getTemperature().getMetric().getValue(), tempUnit).toString(),
				R.drawable.temperature, null);
		addGridItem(R.string.real_feel_temperature,
				ValueUnits.convertTemperature(item.getRealFeelTemperature().getMetric().getValue(), tempUnit).toString(),
				R.drawable.realfeeltemperature, null);
		addGridItem(R.string.humidity, item.getRelativeHumidity(), R.drawable.humidity, null);
		addGridItem(R.string.dew_point, item.getDewPoint().getMetric().getValue(), R.drawable.dewpoint, null);
		addGridItem(R.string.wind_direction, item.getWind().getDirection().getDegrees(), R.drawable.winddirection, null);
		addGridItem(R.string.wind_speed,
				ValueUnits.convertWindSpeedForAccu(item.getWind().getSpeed().getMetric().getValue(), windUnit).toString(),
				R.drawable.windspeed, null);
		addGridItem(R.string.wind_gust,
				ValueUnits.convertWindSpeedForAccu(item.getWindGust().getSpeed().getMetric().getValue(), windUnit).toString(),
				R.drawable.windgust, null);
		addGridItem(R.string.wind_strength,
				WeatherResponseProcessor.getSimpleWindSpeedDescription(item.getWind().getSpeed().getMetric().getValue()),
				R.drawable.windstrength, null);
		addGridItem(R.string.pressure, item.getPressure().getMetric().getValue(), R.drawable.pressure, null);
		addGridItem(R.string.uv_index, item.getuVIndexText(), R.drawable.uv, null);
		addGridItem(R.string.visibility, ValueUnits.convertVisibility(item.getVisibility().getMetric().getValue(), visibilityUnit),
				R.drawable.visibility, null);
		addGridItem(R.string.cloud_cover, item.getCloudCover(), R.drawable.cloudiness, null);
		addGridItem(R.string.precipitation_volume, item.getPrecip1hr().getMetric().getValue(), R.drawable.pop, null);
		addGridItem(R.string.precipitation_type, item.getPrecipitationType(), R.drawable.temp_icon, null);
	}
}
