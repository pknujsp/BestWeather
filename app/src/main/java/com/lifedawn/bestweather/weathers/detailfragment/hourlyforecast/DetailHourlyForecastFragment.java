package com.lifedawn.bestweather.weathers.detailfragment.hourlyforecast;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DetailHourlyForecastFragment extends BaseDetailHourlyForecastFragment {


	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hourlyForecastDtoList = (ArrayList<HourlyForecastDto>) bundle.getSerializable(WeatherDataType.hourlyForecast.name());
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.detail_hourly_forecast);
		binding.listview.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		binding.listview.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
	}

	@Override
	protected void setDataViewsByList() {
		HourlyForecastListAdapter adapter = new HourlyForecastListAdapter(getContext(),
				this, tempUnit);
		adapter.setHourlyForecastDtoList(hourlyForecastDtoList);
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