package com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailHourlyForecastViewPagerAdapter;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DetailHourlyForecastDialogFragment extends BaseDetailDialogFragment {
	private List<HourlyForecastDto> hourlyForecastDtoList;
	private LayoutInflater layoutInflater;

	public void setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
		this.hourlyForecastDtoList = hourlyForecastDtoList;
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

		setTabCustomView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void setTabCustomView() {
		super.setTabCustomView();

		layoutInflater = getLayoutInflater();
		ImageView weatherIcon = null;
		TextView temp = null;
		TextView hour = null;
		DateTimeFormatter hour0Formatter = DateTimeFormatter.ofPattern("E H");

		int index = 0;
		for (HourlyForecastDto item : hourlyForecastDtoList) {
			LinearLayout itemView = (LinearLayout) layoutInflater.inflate(R.layout.tab_forecast_item, binding.tabLayout, false);
			itemView.findViewById(R.id.right_weather_icon).setVisibility(View.GONE);
			weatherIcon = (ImageView) itemView.findViewById(R.id.left_weather_icon);
			temp = (TextView) itemView.findViewById(R.id.temp);
			hour = (TextView) itemView.findViewById(R.id.dateTime);

			hour.setText(item.getHours().format(hour0Formatter));
			weatherIcon.setImageResource(item.getWeatherIcon());
			temp.setText(item.getTemp());

			binding.tabLayout.getTabAt(index++).setCustomView(itemView);
		}
		binding.tabLayout.selectTab(binding.tabLayout.getTabAt(firstSelectedPosition));

	}
}
