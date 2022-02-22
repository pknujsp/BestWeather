package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.os.Bundle;

import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailDailyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailHourlyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;

import java.io.Serializable;
import java.util.List;

public class BaseDetailDailyForecastFragment extends BaseDetailForecastFragment {
	protected List<DailyForecastDto> dailyForecastDtoList;

	@Override
	public void onClickedItem(Integer position) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(WeatherDataType.dailyForecast.name(), (Serializable) dailyForecastDtoList);
		bundle.putInt("FirstSelectedPosition", position);

		DetailDailyForecastDialogFragment detailHourlyForecastDialogFragment =
				new DetailDailyForecastDialogFragment();
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
