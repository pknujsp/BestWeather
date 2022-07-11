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

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.weathers.detailfragment.adapters.DetailDailyForecastViewPagerAdapter;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DetailDailyForecastDialogFragment extends BaseDetailDialogFragment {
	private static List<DailyForecastDto> dailyForecastDtoList;
	private LayoutInflater layoutInflater;

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

		layoutInflater = getLayoutInflater();
		ImageView leftIcon = null;
		ImageView rightIcon = null;
		TextView temp = null;
		TextView dateTime = null;
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E");
		final String divider = " / ";

		int index = 0;
		for (DailyForecastDto item : dailyForecastDtoList) {
			LinearLayout itemView = (LinearLayout) layoutInflater.inflate(R.layout.tab_forecast_item, binding.tabLayout, false);

			leftIcon = (ImageView) itemView.findViewById(R.id.left_weather_icon);
			rightIcon = (ImageView) itemView.findViewById(R.id.right_weather_icon);
			temp = (TextView) itemView.findViewById(R.id.temp);
			dateTime = (TextView) itemView.findViewById(R.id.dateTime);

			dateTime.setText(item.getDate().format(dateTimeFormatter));
			temp.setText(new String(item.getMinTemp() + divider + item.getMaxTemp()));

			if (item.getValuesList().size() == 1) {
				leftIcon.setImageResource(item.getValuesList().get(0).getWeatherIcon());
				rightIcon.setVisibility(View.GONE);
			} else if (item.getValuesList().size() == 2) {
				leftIcon.setImageResource(item.getValuesList().get(0).getWeatherIcon());
				rightIcon.setImageResource(item.getValuesList().get(1).getWeatherIcon());
			} else if (item.getValuesList().size() == 4) {
				leftIcon.setImageResource(item.getValuesList().get(1).getWeatherIcon());
				rightIcon.setImageResource(item.getValuesList().get(2).getWeatherIcon());
			}

			binding.tabLayout.getTabAt(index++).setCustomView(itemView);
		}
		binding.tabLayout.selectTab(binding.tabLayout.getTabAt(firstSelectedPosition));


	}
}
