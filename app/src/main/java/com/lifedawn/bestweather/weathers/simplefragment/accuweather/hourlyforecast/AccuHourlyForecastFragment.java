package com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.WeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class AccuHourlyForecastFragment extends BaseSimpleHourlyForecastFragment {
	private TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	public AccuHourlyForecastFragment setTwelveHoursOfHourlyForecastsResponse(
			TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse) {
		this.twelveHoursOfHourlyForecastsResponse = twelveHoursOfHourlyForecastsResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		//accu hourly forecast simple : 날짜, 시각, 날씨, 기온, 강수확률, 강수량
		Context context = getContext();
		
		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.date_row_height_in_simple_forecast_view);
		final int CLOCK_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.clock_row_height_in_simple_forecast_view);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.weather_icon_row_height_in_simple_forecast_view);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.default_row_height_in_simple_forecast_view);
		final int MARGIN = (int) context.getResources().getDimension(R.dimen.row_top_bottom_margin_in_simple_forecast_view);
		
		List<TwelveHoursOfHourlyForecastsResponse.Item> items = twelveHoursOfHourlyForecastsResponse.getItems();
		
		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.column_width_in_simple_forecast_view);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;
		
		//label column 설정
		final int LABEL_VIEW_WIDTH = (int) context.getResources().getDimension(R.dimen.label_view_width_in_simple_forecast_view);
		
		addLabelView(R.drawable.temp_img, getString(R.string.date), LABEL_VIEW_WIDTH, DATE_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_img, getString(R.string.clock), LABEL_VIEW_WIDTH, CLOCK_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_img, getString(R.string.weather), LABEL_VIEW_WIDTH, WEATHER_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_img, getString(R.string.temperature), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_img, getString(R.string.probability_of_precipitation), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT,
				MARGIN);
		addLabelView(R.drawable.temp_img, getString(R.string.precipitation_volume), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, MARGIN);
		
		dateRow = null;
		ClockView clockRow = new ClockView(context, VIEW_WIDTH, CLOCK_ROW_HEIGHT, COLUMN_WIDTH);
		WeatherIconView weatherIconRow = new WeatherIconView(context, VIEW_WIDTH, WEATHER_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView tempRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView precipitationVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		
		//시각 --------------------------------------------------------------------------
		Calendar date = Calendar.getInstance();
		dateXMap.clear();
		date.setTimeInMillis(Long.parseLong(items.get(0).getEpochDateTime()));
		date.add(Calendar.DATE, -15);
		long lastDate = date.getTimeInMillis();
		
		List<DateView.DateValue> dateValueList = new ArrayList<>();
		int beginX = 0;
		
		for (int col = 0; col < COLUMN_COUNT; col++) {
			date.setTimeInMillis(Long.parseLong(items.get(col).getEpochDateTime()));
			
			if (date.get(Calendar.HOUR_OF_DAY) == 0 || col == 0) {
				if (dateValueList.size() > 0) {
					dateValueList.get(dateValueList.size() - 1).endX = COLUMN_WIDTH * (col - 1) + COLUMN_WIDTH / 2;
				}
				beginX = COLUMN_WIDTH * col + COLUMN_WIDTH / 2;
				dateValueList.add(new DateView.DateValue(beginX, date.getTime()));
			}
			
			if (!ClockUtil.areSameDate(lastDate, date.getTimeInMillis())) {
				dateXMap.put(COLUMN_WIDTH * col, date.getTime());
				lastDate = date.getTimeInMillis();
			}
		}
		dateValueList.get(dateValueList.size() - 1).endX = COLUMN_WIDTH * (COLUMN_COUNT - 1) + COLUMN_WIDTH / 2;
		
		dateRow = new DateView(getContext(), VIEW_WIDTH, DATE_ROW_HEIGHT);
		dateRow.setDateValueList(dateValueList);
		
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
		rowLayoutParams.topMargin = MARGIN;
		rowLayoutParams.bottomMargin = MARGIN;
		
		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(precipitationVolumeRow, rowLayoutParams);
	}
	
}