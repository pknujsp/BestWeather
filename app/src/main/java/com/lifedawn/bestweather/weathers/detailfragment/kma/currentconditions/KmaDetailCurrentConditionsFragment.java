package com.lifedawn.bestweather.weathers.detailfragment.kma.currentconditions;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.MainActivity;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailCurrentConditionsAdapter;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KmaDetailCurrentConditionsFragment extends BaseDetailCurrentConditionsFragment {
	private FinalCurrentConditions finalCurrentConditions;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// setValuesToViews();
	}
	
	public KmaDetailCurrentConditionsFragment setFinalCurrentConditions(FinalCurrentConditions finalCurrentConditions) {
		this.finalCurrentConditions = finalCurrentConditions;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		// 기온,1시간강수량,습도,강수형태,풍향,풍속
		List<GridItemDto> gridItemDtoList = new ArrayList<>();
		gridItemDtoList.add(makeGridItem(R.string.temperature, finalCurrentConditions.getTemperature(), 0));
		gridItemDtoList.add(makeGridItem(R.string.humidity, finalCurrentConditions.getHumidity(), 0));
		gridItemDtoList.add(makeGridItem(R.string.wind_direction, finalCurrentConditions.getWindDirection(), 0));
		gridItemDtoList.add(makeGridItem(R.string.wind_speed, finalCurrentConditions.getWindSpeed(), 0));
		gridItemDtoList.add(makeGridItem(R.string.wind_strength,
				WeatherResponseProcessor.getSimpleWindSpeedDescription(finalCurrentConditions.getWindSpeed()), 0));
		gridItemDtoList.add(makeGridItem(R.string.precipitation_volume, finalCurrentConditions.getPrecipitation1Hour(), 0));
		gridItemDtoList.add(makeGridItem(R.string.precipitation_type,
				KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType()), 0));
		
		DetailCurrentConditionsAdapter arrayAdapter = new DetailCurrentConditionsAdapter(getContext());
		arrayAdapter.setGridItemDtoList(gridItemDtoList);
		binding.conditionsGrid.setAdapter(arrayAdapter);
	}
}
