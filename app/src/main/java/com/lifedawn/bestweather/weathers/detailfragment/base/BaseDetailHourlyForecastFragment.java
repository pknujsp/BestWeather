package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.os.Bundle;

import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailHourlyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.io.Serializable;
import java.util.List;

public class BaseDetailHourlyForecastFragment extends BaseDetailForecastFragment {
	protected List<HourlyForecastDto> hourlyForecastDtoList;

	@Override
	public void onClickedItem(Integer position) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(WeatherDataType.hourlyForecast.name(), (Serializable) hourlyForecastDtoList);
		bundle.putInt("FirstSelectedPosition", position);

		DetailHourlyForecastDialogFragment detailHourlyForecastDialogFragment =
				new DetailHourlyForecastDialogFragment();
		detailHourlyForecastDialogFragment.setArguments(bundle);

		detailHourlyForecastDialogFragment.show(getChildFragmentManager(),
				DetailHourlyForecastDialogFragment.class.getName());
	}

	@Override
	protected void setDataViewsByList() {

	}

	@Override
	protected void setDataViewsByTable() {

	}
}
