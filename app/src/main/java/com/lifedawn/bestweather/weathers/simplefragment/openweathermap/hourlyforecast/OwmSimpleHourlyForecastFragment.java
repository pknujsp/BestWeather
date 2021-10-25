package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.hourlyforecast.OwmDetailHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OwmSimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
	private OneCallResponse oneCallResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.hourly_forecast);
		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				HourlyForecastComparisonFragment comparisonFragment = new HourlyForecastComparisonFragment();
				comparisonFragment.setArguments(getArguments());

				String tag = getString(R.string.tag_comparison_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						comparisonFragment, tag).addToBackStack(tag).commit();
			}
		});

		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OwmDetailHourlyForecastFragment detailHourlyForecastFragment = new OwmDetailHourlyForecastFragment();
				detailHourlyForecastFragment.setHourlyList(oneCallResponse.getHourly());

				Bundle bundle = new Bundle();
				bundle.putString(getString(R.string.bundle_key_address_name), addressName);
				bundle.putSerializable(getString(R.string.bundle_key_timezone), timeZone);

				detailHourlyForecastFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_hourly_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						detailHourlyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});

		setValuesToViews();
	}

	public OwmSimpleHourlyForecastFragment setOneCallResponse(OneCallResponse oneCallResponse) {
		this.oneCallResponse = oneCallResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		//owm hourly forecast simple : 날짜, 시각, 날씨, 기온, 강수확률, 강수량, 강설량
		Context context = getContext();

		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int CLOCK_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.clockValueRowHeightInCOMMON);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);

		List<OneCallResponse.Hourly> items = oneCallResponse.getHourly();

		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

		addLabelView(R.drawable.temp_icon, getString(R.string.date), DATE_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.clock), CLOCK_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), WEATHER_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.rain_volume), DEFAULT_TEXT_ROW_HEIGHT);
		ImageView snowVolumeLabel = addLabelView(R.drawable.temp_icon, getString(R.string.snow_volume), DEFAULT_TEXT_ROW_HEIGHT);

		dateRow = new DateView(context, VIEW_WIDTH, DATE_ROW_HEIGHT, COLUMN_WIDTH);
		ClockView clockRow = new ClockView(context, VIEW_WIDTH, CLOCK_ROW_HEIGHT, COLUMN_WIDTH);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, VIEW_WIDTH, WEATHER_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView tempRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView rainVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView snowVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);

		//시각, 기온, 강수확률, 강수량
		List<LocalDateTime> dateTimeList = new ArrayList<>();
		List<String> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		boolean haveSnowVolumes = false;

		for (OneCallResponse.Hourly item : items) {
			dateTimeList.add(WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(item.getDt()) * 1000L, timeZone));
			tempList.add(ValueUnits.convertTemperature(item.getTemp(), tempUnit).toString());
			probabilityOfPrecipitationList.add(String.valueOf((int) (Double.parseDouble(item.getPop()) * 100.0)));
			rainVolumeList.add(item.getRain() == null ? "-" : item.getRain().getPrecipitation1Hour());

			if (item.getSnow() != null) {
				if (!haveSnowVolumes) {
					haveSnowVolumes = true;
				}
			}
			snowVolumeList.add(item.getSnow() == null ? "-" : item.getSnow().getPrecipitation1Hour());
		}
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		tempRow.setValueList(tempList);
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
		if (haveSnowVolumes) {
			binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
		} else {
			binding.labels.removeView(snowVolumeLabel);
		}
	}

}