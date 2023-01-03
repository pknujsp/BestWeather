package com.lifedawn.bestweather.ui.weathers.detailfragment.dailyforecast;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.ui.weathers.detailfragment.base.BaseDetailDailyForecastFragment;
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto;


import org.jetbrains.annotations.NotNull;

public class DetailDailyForecastFragment extends BaseDetailDailyForecastFragment {
	private DailyForecastListAdapter adapter;

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

		adapter = new DailyForecastListAdapter(this);
		adapter.setDailyForecastDtoList(dailyForecastDtoList, hasPrecipitationVolume);
		binding.listview.setAdapter(adapter);
	}

	@Override
	protected void setDataViewsByTable() {

	}

	@Override
	public void onDestroy() {
		adapter.dailyForecastDtoList.clear();
		adapter = null;
		super.onDestroy();
	}

	@Override
	public void onClickedItem(Integer position) {
		super.onClickedItem(position);
	}
}