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
		
		addGridItem(R.string.temperature, ValueUnits.convertTemperature(item.getTemperature().getValue(), tempUnit).toString(),
				R.drawable.temp_icon, null);
		addGridItem(R.string.real_feel_temperature,
				ValueUnits.convertTemperature(item.getRealFeelTemperature().getValue(), tempUnit).toString(), R.drawable.temp_icon, null);
		addGridItem(R.string.humidity, item.getRelativeHumidity(), R.drawable.temp_icon, null);
		addGridItem(R.string.dew_point, item.getDewPoint().getValue(), R.drawable.temp_icon, null);
		addGridItem(R.string.wind_direction, item.getWind().getDirection().getDegrees(), R.drawable.temp_icon, null);
		addGridItem(R.string.wind_speed, ValueUnits.convertWindSpeed(item.getWind().getSpeed().getMetric().getValue(), windUnit).toString(),
				R.drawable.temp_icon, null);
		addGridItem(R.string.wind_gust, item.getWindGust().getSpeed().getMetric().getValue(), R.drawable.temp_icon, null);
		addGridItem(R.string.wind_strength,
				WeatherResponseProcessor.getSimpleWindSpeedDescription(item.getWind().getSpeed().getMetric().getValue()),
				R.drawable.temp_icon, null);
		addGridItem(R.string.pressure, item.getPressure().getMetric().getValue(), R.drawable.temp_icon, null);
		addGridItem(R.string.uv_index, item.getuVIndexText(), R.drawable.temp_icon, null);
		addGridItem(R.string.visibility,
				ValueUnits.convertVisibility(item.getVisibility().getMetric().getValue(), visibilityUnit).toString(), R.drawable.temp_icon,
				null);
		addGridItem(R.string.cloud_cover, item.getCloudCover(), R.drawable.temp_icon, null);
		addGridItem(R.string.precipitation_volume, item.getPrecip1hr().getMetric().getValue(), R.drawable.temp_icon, null);
		addGridItem(R.string.precipitation_type, item.getPrecipitationType(), R.drawable.temp_icon, null);
	}
}
