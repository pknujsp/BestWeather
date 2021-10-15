package com.lifedawn.bestweather.weathers.detailfragment.accuweather.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailCurrentConditionsAdapter;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
		List<GridItemDto> gridItemDtoList = new ArrayList<>();
		CurrentConditionsResponse.Item item = currentConditionsResponse.getItems().get(0);
		
		gridItemDtoList.add(makeGridItem(R.string.temperature, item.getTemperature().getValue(), 0));
		gridItemDtoList.add(makeGridItem(R.string.real_feel_temperature, item.getRealFeelTemperature().getValue(), 0));
		gridItemDtoList.add(makeGridItem(R.string.humidity, item.getRelativeHumidity(), 0));
		gridItemDtoList.add(makeGridItem(R.string.dew_point, item.getDewPoint().getValue(), 0));
		gridItemDtoList.add(makeGridItem(R.string.wind_direction, item.getWind().getDirection().getDegrees(), 0));
		gridItemDtoList.add(makeGridItem(R.string.wind_speed, item.getWind().getSpeed().getMetric().getValue(), 0));
		gridItemDtoList.add(makeGridItem(R.string.wind_gust, item.getWindGust().getSpeed().getMetric().getValue(), 0));
		gridItemDtoList.add(makeGridItem(R.string.wind_strength,
				WeatherResponseProcessor.getSimpleWindSpeedDescription(item.getWind().getSpeed().getMetric().getValue()), 0));
		gridItemDtoList.add(makeGridItem(R.string.pressure, item.getPressure().getMetric().getValue(), 0));
		gridItemDtoList.add(makeGridItem(R.string.uv_index, item.getuVIndexText(), 0));
		gridItemDtoList.add(makeGridItem(R.string.visibility, item.getVisibility().getMetric().getValue(), 0));
		gridItemDtoList.add(makeGridItem(R.string.cloud_cover, item.getCloudCover(), 0));
		gridItemDtoList.add(makeGridItem(R.string.precipitation_volume, item.getPrecip1hr().getMetric().getValue(), 0));
		gridItemDtoList.add(makeGridItem(R.string.precipitation_type, item.getPrecipitationType(), 0));
		
		DetailCurrentConditionsAdapter arrayAdapter = new DetailCurrentConditionsAdapter(getContext());
		arrayAdapter.setGridItemDtoList(gridItemDtoList);
		binding.conditionsGrid.setAdapter(arrayAdapter);
	}
}
