package com.lifedawn.bestweather.weathers.detailfragment.dailyforecast;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DetailDailyForecastFragment extends BaseDetailDailyForecastFragment {


	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.detail_daily_forecast);
		binding.listview.addItemDecoration(new DividerItemDecoration(requireContext().getApplicationContext(), DividerItemDecoration.VERTICAL));
	}


	@Override
	protected void setDataViewsByList() {
		boolean hasPrecipitationVolume = false;
		for (DailyForecastDto item : dailyForecastDtoList) {
			for (DailyForecastDto.Values values : item.getValuesList()) {
				if (values.isHasPrecipitationVolume() || values.isHasRainVolume() || values.isHasSnowVolume()) {
					hasPrecipitationVolume = true;
					break;
				}
			}

			if (hasPrecipitationVolume) {
				break;
			}
		}

		DailyForecastListAdapter adapter = new DailyForecastListAdapter(this);
		adapter.setDailyForecastDtoList(dailyForecastDtoList, hasPrecipitationVolume);
		binding.listview.setAdapter(adapter);
	}

	@Override
	protected void setDataViewsByTable() {

	}

	@Override
	public void onClickedItem(Integer position) {
		super.onClickedItem(position);
	}
}