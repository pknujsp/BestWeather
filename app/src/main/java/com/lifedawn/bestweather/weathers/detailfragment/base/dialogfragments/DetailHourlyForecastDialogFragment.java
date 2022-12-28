package com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.lifedawn.bestweather.databinding.TabForecastItemBinding;
import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailHourlyForecastViewPagerAdapter;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DetailHourlyForecastDialogFragment extends BaseDetailDialogFragment {
	private static List<HourlyForecastDto> hourlyForecastDtoList;

	public static void setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
		DetailHourlyForecastDialogFragment.hourlyForecastDtoList = hourlyForecastDtoList;
	}

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		DetailHourlyForecastViewPagerAdapter adapter = new DetailHourlyForecastViewPagerAdapter();
		adapter.setHourlyForecastDtoList(hourlyForecastDtoList);

		binding.detailForecastViewPager.setAdapter(adapter);
		binding.detailForecastViewPager.setCurrentItem(firstSelectedPosition, false);
		binding.detailForecastViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
			}
		});

		setTabCustomView();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void setTabCustomView() {
		super.setTabCustomView();

			LayoutInflater layoutInflater = getLayoutInflater();
			DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E H");

			int index = 0;
			TabForecastItemBinding tabItemBinding = null;

			for (HourlyForecastDto item : hourlyForecastDtoList) {
				tabItemBinding = TabForecastItemBinding.inflate(layoutInflater, binding.tabLayout, false);
				tabItemBinding.rightWeatherIcon.setVisibility(View.GONE);

				tabItemBinding.dateTime.setText(item.getHours().format(hour0Formatter));
				Glide.with(tabItemBinding.leftWeatherIcon).load(item.getWeatherIcon()).into(tabItemBinding.leftWeatherIcon);
				tabItemBinding.temp.setText(item.getTemp());

				binding.tabLayout.getTabAt(index++).setCustomView(tabItemBinding.getRoot());
			}
			binding.tabLayout.selectTab(binding.tabLayout.getTabAt(firstSelectedPosition));


	}
}
