package com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AccuSimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
	private TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.hourly_forecast);
		setValuesToViews();
		
	}
	
	public AccuSimpleHourlyForecastFragment setTwelveHoursOfHourlyForecastsResponse(
			TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse) {
		this.twelveHoursOfHourlyForecastsResponse = twelveHoursOfHourlyForecastsResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		//accu hourly forecast simple : 날짜, 시각, 날씨, 기온, 강수확률, 강수량
		Context context = getContext();
		
		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int CLOCK_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.clockValueRowHeightInCOMMON);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);
		
		List<TwelveHoursOfHourlyForecastsResponse.Item> items = twelveHoursOfHourlyForecastsResponse.getItems();
		
		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;
		
		//label column 설정
		final int LABEL_VIEW_WIDTH = (int) context.getResources().getDimension(R.dimen.labelIconColumnWidthInCOMMON);
		
		addLabelView(R.drawable.temp_icon, getString(R.string.date), DATE_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.clock), CLOCK_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), WEATHER_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_volume), DEFAULT_TEXT_ROW_HEIGHT);
		
		dateRow = new DateView(context, VIEW_WIDTH, DATE_ROW_HEIGHT, COLUMN_WIDTH);
		ClockView clockRow = new ClockView(context, VIEW_WIDTH, CLOCK_ROW_HEIGHT, COLUMN_WIDTH);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, VIEW_WIDTH, WEATHER_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView tempRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView precipitationVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		
		//시각 --------------------------------------------------------------------------
		List<Date> dateTimeList = new ArrayList<>();
		for (int col = 0; col < COLUMN_COUNT; col++) {
			dateTimeList.add(new Date(Long.parseLong(items.get(col).getEpochDateTime())));
		}
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		
		//날씨 아이콘
		
		//기온, 강수확률, 강수량
		List<String> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> precipitationVolumeList = new ArrayList<>();
		
		for (TwelveHoursOfHourlyForecastsResponse.Item item : items) {
			tempList.add(item.getTemperature().getValue());
			probabilityOfPrecipitationList.add(item.getPrecipitationProbability() == null ? "-" : item.getPrecipitationProbability());
			precipitationVolumeList.add(item.getTotalLiquid().getValue());
		}
		
		tempRow.setValueList(tempList);
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		precipitationVolumeRow.setValueList(precipitationVolumeList);
		
		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		
		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(precipitationVolumeRow, rowLayoutParams);
	}
	
}