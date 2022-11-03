package com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.databinding.TabForecastItemBinding;
import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailDailyForecastViewPagerAdapter;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DetailDailyForecastDialogFragment extends BaseDetailDialogFragment {
	private static List<DailyForecastDto> dailyForecastDtoList;

	public static void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		DetailDailyForecastDialogFragment.dailyForecastDtoList = dailyForecastDtoList;
	}

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
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E");
		final String divider = " / ";

		int index = 0;
		TabForecastItemBinding tabItemBinding = null;

		for (DailyForecastDto item : dailyForecastDtoList) {
			tabItemBinding = TabForecastItemBinding.inflate(layoutInflater, binding.tabLayout, false);

			tabItemBinding.dateTime.setText(item.getDate().format(dateTimeFormatter));
			tabItemBinding.temp.setText(new String(item.getMinTemp() + divider + item.getMaxTemp()));

			if (item.getValuesList().size() == 1) {
				Glide.with(tabItemBinding.leftWeatherIcon).load(item.getValuesList().get(0).getWeatherIcon()).into(tabItemBinding.leftWeatherIcon);
				tabItemBinding.rightWeatherIcon.setVisibility(View.GONE);
			} else if (item.getValuesList().size() == 2) {
				Glide.with(tabItemBinding.leftWeatherIcon).load(item.getValuesList().get(0).getWeatherIcon()).into(tabItemBinding.leftWeatherIcon);
				Glide.with(tabItemBinding.rightWeatherIcon).load(item.getValuesList().get(1).getWeatherIcon()).into(tabItemBinding.rightWeatherIcon);
			} else if (item.getValuesList().size() == 4) {
				Glide.with(tabItemBinding.leftWeatherIcon).load(item.getValuesList().get(1).getWeatherIcon()).into(tabItemBinding.leftWeatherIcon);
				Glide.with(tabItemBinding.rightWeatherIcon).load(item.getValuesList().get(2).getWeatherIcon()).into(tabItemBinding.rightWeatherIcon);
			}

			binding.tabLayout.getTabAt(index++).setCustomView(tabItemBinding.getRoot());
		}
		binding.tabLayout.selectTab(binding.tabLayout.getTabAt(firstSelectedPosition));


	}
}
