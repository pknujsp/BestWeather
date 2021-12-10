package com.lifedawn.bestweather.weathers.detailfragment.base;

import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailHourlyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.detailfragment.dto.HourlyForecastDto;

import java.util.List;

public class BaseDetailHourlyForecastFragment extends BaseDetailForecastFragment {
	protected List<HourlyForecastDto> hourlyForecastDtoList;

	@Override
	public void onClickedItem(Integer position) {
		DetailHourlyForecastDialogFragment detailHourlyForecastDialogFragment =
				new DetailHourlyForecastDialogFragment();
		detailHourlyForecastDialogFragment.setFirstSelectedPosition(position);
		detailHourlyForecastDialogFragment.setHourlyForecastDtoList(hourlyForecastDtoList);

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
