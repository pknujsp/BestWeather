package com.lifedawn.bestweather.weathers.detailfragment.base;

import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailDailyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailHourlyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;

import java.util.List;

public class BaseDetailDailyForecastFragment extends BaseDetailForecastFragment {
	protected List<DailyForecastDto> dailyForecastDtoList;

	@Override
	public void onClickedItem(Integer position) {
		DetailDailyForecastDialogFragment detailHourlyForecastDialogFragment =
				new DetailDailyForecastDialogFragment();
		detailHourlyForecastDialogFragment.setFirstSelectedPosition(position);
		detailHourlyForecastDialogFragment.setDailyForecastDtoList(dailyForecastDtoList);

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
