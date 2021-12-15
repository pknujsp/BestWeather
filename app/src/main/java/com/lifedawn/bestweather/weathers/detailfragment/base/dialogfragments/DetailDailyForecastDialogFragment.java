package com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailDailyForecastViewPagerAdapter;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DetailDailyForecastDialogFragment extends BaseDetailDialogFragment {
	private List<DailyForecastDto> dailyForecastDtoList;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		DetailDailyForecastViewPagerAdapter adapter = new DetailDailyForecastViewPagerAdapter(getContext());
		adapter.setDailyForecastDtoList(dailyForecastDtoList);

		binding.detailForecastViewPager.setAdapter(adapter);
		binding.detailForecastViewPager.setCurrentItem(firstSelectedPosition, false);
		binding.detailForecastViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		this.dailyForecastDtoList = dailyForecastDtoList;
	}

	@Override
	public void setFirstSelectedPosition(int firstSelectedPosition) {
		this.firstSelectedPosition = firstSelectedPosition;
	}
}
