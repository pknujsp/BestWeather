package com.lifedawn.bestweather.weathers.simplefragment.accuweather.dailyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.WeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccuSimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private FiveDaysOfDailyForecastsResponse fiveDaysOfDailyForecastsResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast);
	}
	
	public AccuSimpleDailyForecastFragment setFiveDaysOfDailyForecastsResponse(
			FiveDaysOfDailyForecastsResponse fiveDaysOfDailyForecastsResponse) {
		this.fiveDaysOfDailyForecastsResponse = fiveDaysOfDailyForecastsResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜, 최저/최고 기온 ,낮과 밤의 날씨상태, 강수확률, 강수량
		Context context = getContext();
		
		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.date_row_height_in_simple_forecast_view);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.weather_icon_row_height_in_simple_forecast_view);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.default_row_height_in_simple_forecast_view);
		final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(
				R.dimen.detail_temperature_row_height_in_simple_forecast_view);
		final int MARGIN = (int) context.getResources().getDimension(R.dimen.row_top_bottom_margin_in_simple_forecast_view);
		
		List<FiveDaysOfDailyForecastsResponse.DailyForecasts> items = fiveDaysOfDailyForecastsResponse.getDailyForecasts();
		
		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.column_width_in_simple_daily_forecast_view);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;
		
		//label column 설정
		final int LABEL_VIEW_WIDTH = (int) context.getResources().getDimension(R.dimen.label_view_width_in_simple_forecast_view);
		
		addLabelView(R.drawable.temp_icon, getString(R.string.date), LABEL_VIEW_WIDTH, DATE_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), LABEL_VIEW_WIDTH, WEATHER_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), LABEL_VIEW_WIDTH, TEMP_ROW_HEIGHT, MARGIN);
		
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT,
				MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_volume), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, MARGIN);
		
		TextValueView dateRow = new TextValueView(context, VIEW_WIDTH, DATE_ROW_HEIGHT, COLUMN_WIDTH);
		WeatherIconView weatherIconRow = new WeatherIconView(context, VIEW_WIDTH, WEATHER_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView precipitationVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		
		//시각 --------------------------------------------------------------------------
		List<String> dateList = new ArrayList<>();
		Date date = new Date();
		SimpleDateFormat MdE = new SimpleDateFormat("M/d E", Locale.getDefault());
		
		for (FiveDaysOfDailyForecastsResponse.DailyForecasts dailyForecasts : items) {
			date.setTime(Long.parseLong(dailyForecasts.getEpochDate()));
			dateList.add(MdE.format(date));
		}
		dateRow.setValueList(dateList);
		
		//날씨 아이콘
		
		//기온, 강수확률, 강수량
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> precipitationVolumeList = new ArrayList<>();
		
		String pop = null;
		String volume = null;
		
		for (FiveDaysOfDailyForecastsResponse.DailyForecasts dailyForecasts : items) {
			minTempList.add(Integer.parseInt(dailyForecasts.getTemperature().getMinimum().getMetric().getValue()));
			maxTempList.add(Integer.parseInt(dailyForecasts.getTemperature().getMaximum().getMetric().getValue()));
			
			if (dailyForecasts.getDay().getPrecipitationProbability() != null) {
				pop = dailyForecasts.getDay().getPrecipitationProbability();
			} else {
				pop = "-";
			}
			
			if (dailyForecasts.getNight().getPrecipitationProbability() != null) {
				pop += dailyForecasts.getNight().getPrecipitationProbability();
			} else {
				pop += "-";
			}
			
			if (dailyForecasts.getDay().getTotalLiquid().getValue() != null) {
				volume = dailyForecasts.getDay().getTotalLiquid().getValue();
			} else {
				volume = "-";
			}
			
			volume += " / ";
			
			if (dailyForecasts.getNight().getTotalLiquid().getValue() != null) {
				volume += dailyForecasts.getNight().getTotalLiquid().getValue();
			} else {
				volume += "-";
			}
			
			probabilityOfPrecipitationList.add(pop);
			precipitationVolumeList.add(volume);
		}
		
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		precipitationVolumeRow.setValueList(precipitationVolumeList);
		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), VIEW_WIDTH, TEMP_ROW_HEIGHT, COLUMN_WIDTH,
				minTempList, maxTempList);
		
		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.topMargin = MARGIN;
		rowLayoutParams.bottomMargin = MARGIN;
		
		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(precipitationVolumeRow, rowLayoutParams);
	}
}