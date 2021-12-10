package com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailHourlyForecastViewPagerAdapter;
import com.lifedawn.bestweather.weathers.detailfragment.dto.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DetailHourlyForecastDialogFragment extends BaseDetailDialogFragment {
	private List<HourlyForecastDto> hourlyForecastDtoList;

	public void setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
		this.hourlyForecastDtoList = hourlyForecastDtoList;
	}

	@Override
	public void setFirstSelectedPosition(int firstSelectedPosition) {
		this.firstSelectedPosition = firstSelectedPosition;
	}

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		DetailHourlyForecastViewPagerAdapter adapter = new DetailHourlyForecastViewPagerAdapter(getContext());
		adapter.setHourlyForecastDtoList(hourlyForecastDtoList);

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
}
