package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailDailyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailHourlyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;

import java.io.Serializable;
import java.util.List;

public class BaseDetailDailyForecastFragment extends BaseDetailForecastFragment {
	protected static List<DailyForecastDto> dailyForecastDtoList;

	public static void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		BaseDetailDailyForecastFragment.dailyForecastDtoList = dailyForecastDtoList;
	}

	@Override
	public void onClickedItem(Integer position) {
		if (clickableItem) {
			clickableItem = false;
			Bundle bundle = new Bundle();
			bundle.putInt("FirstSelectedPosition", position);

			DetailDailyForecastDialogFragment detailHourlyForecastDialogFragment =
					new DetailDailyForecastDialogFragment();
			detailHourlyForecastDialogFragment.setArguments(bundle);
			DetailDailyForecastDialogFragment.setDailyForecastDtoList(dailyForecastDtoList);

			detailHourlyForecastDialogFragment.show(getChildFragmentManager(),
					DetailHourlyForecastDialogFragment.class.getName());
		}
	}

	@Override
	protected void onFragmentStarted(Fragment fragment) {
		if (fragment instanceof DetailDailyForecastDialogFragment) {
			super.onFragmentStarted(fragment);
		}
	}

	@Override
	protected void setDataViewsByList() {

	}

	@Override
	protected void setDataViewsByTable() {

	}
}
