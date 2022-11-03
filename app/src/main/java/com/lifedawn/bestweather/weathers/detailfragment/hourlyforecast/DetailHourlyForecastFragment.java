package com.lifedawn.bestweather.weathers.detailfragment.hourlyforecast;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailHourlyForecastFragment;

import org.jetbrains.annotations.NotNull;

public class DetailHourlyForecastFragment extends BaseDetailHourlyForecastFragment {


	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
		final int selectedItem = sharedPreferences.getInt(getString(R.string.pref_key_detail_hourly_forecast_show_data), 0);

		HourlyForecastListAdapter.ShowDataType showDataType = null;
		switch (selectedItem) {
			case 0:
				showDataType = HourlyForecastListAdapter.ShowDataType.Precipitation;
				break;
			case 1:
				showDataType = HourlyForecastListAdapter.ShowDataType.Wind;
				break;
			case 2:
				showDataType = HourlyForecastListAdapter.ShowDataType.Humidity;
				break;
		}
		HourlyForecastListAdapter adapter = new HourlyForecastListAdapter(
				this, showDataType);
		adapter.setHourlyForecastDtoList(hourlyForecastDtoList);
		binding.listview.setHasFixedSize(true);
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