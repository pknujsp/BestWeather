package com.lifedawn.bestweather.weathers.detailfragment.dailyforecast;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;


import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DetailDailyForecastFragment extends BaseDetailDailyForecastFragment {

	public void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		this.dailyForecastDtoList = dailyForecastDtoList;
	}

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.detail_daily_forecast);
		binding.listview.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.listview.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
	}


	@Override
	protected void setDataViewsByList() {
		DailyForecastListAdapter adapter = new DailyForecastListAdapter(getContext(), this);
		adapter.setDailyForecastDtoList(dailyForecastDtoList);
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