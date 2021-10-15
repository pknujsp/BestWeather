package com.lifedawn.bestweather.weathers.simplefragment.kma.dailyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.WeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class KmaSimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private List<FinalDailyForecast> finalDailyForecastList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast);
		setValuesToViews();
		
	}
	
	public KmaSimpleDailyForecastFragment setFinalDailyForecastList(List<FinalDailyForecast> finalDailyForecastList) {
		this.finalDailyForecastList = finalDailyForecastList;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜, 최저/최고 기온 ,낮과 밤의 날씨상태, 강수확률
		Context context = getContext();
		
		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.date_row_height_in_simple_forecast_view);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.weather_icon_row_height_in_simple_forecast_view);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.default_row_height_in_simple_forecast_view);
		final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(
				R.dimen.detail_temperature_row_height_in_simple_forecast_view);
		final int MARGIN = (int) context.getResources().getDimension(R.dimen.row_top_bottom_margin_in_simple_forecast_view);
		
		final int COLUMN_COUNT = finalDailyForecastList.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.column_width_in_simple_daily_forecast_view);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;
		
		//label column 설정
		final int LABEL_VIEW_WIDTH = (int) context.getResources().getDimension(R.dimen.label_view_width_in_simple_forecast_view);
		
		addLabelView(R.drawable.temp_icon, getString(R.string.date), LABEL_VIEW_WIDTH, DATE_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), LABEL_VIEW_WIDTH, WEATHER_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT,
				MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), LABEL_VIEW_WIDTH, TEMP_ROW_HEIGHT, MARGIN);
		
		TextValueView dateRow = new TextValueView(context, VIEW_WIDTH, DATE_ROW_HEIGHT, COLUMN_WIDTH);
		WeatherIconView weatherIconRow = new WeatherIconView(context, VIEW_WIDTH, WEATHER_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		
		//시각 --------------------------------------------------------------------------
		List<String> dateList = new ArrayList<>();
		SimpleDateFormat MdE = new SimpleDateFormat("M/d E", Locale.getDefault());
		
		for (FinalDailyForecast forecast : finalDailyForecastList) {
			dateList.add(MdE.format(forecast.getDate()));
		}
		dateRow.setValueList(dateList);
		
		//날씨 아이콘
		
		//기온, 강수확률, 강수량
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		
		int index = 0;
		for (; index < 5; index++) {
			minTempList.add(Integer.parseInt(finalDailyForecastList.get(index).getMinTemp()));
			maxTempList.add(Integer.parseInt(finalDailyForecastList.get(index).getMaxTemp()));
			
			probabilityOfPrecipitationList.add(
					finalDailyForecastList.get(index).getAmProbabilityOfPrecipitation() + " / " + finalDailyForecastList.get(
							index).getPmProbabilityOfPrecipitation());
		}
		for (; index < finalDailyForecastList.size(); index++) {
			minTempList.add(Integer.parseInt(finalDailyForecastList.get(index).getMinTemp()));
			maxTempList.add(Integer.parseInt(finalDailyForecastList.get(index).getMaxTemp()));
			
			probabilityOfPrecipitationList.add(finalDailyForecastList.get(index).getProbabilityOfPrecipitation());
		}
		
		
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), VIEW_WIDTH, TEMP_ROW_HEIGHT, COLUMN_WIDTH,
				minTempList, maxTempList);
		
		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.topMargin = MARGIN;
		rowLayoutParams.bottomMargin = MARGIN;
		
		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		
	}
}