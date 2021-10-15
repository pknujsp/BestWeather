package com.lifedawn.bestweather.weathers.simplefragment.kma.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.WeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class KmaSimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
	private List<FinalHourlyForecast> finalHourlyForecastList;
	
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
	
	public KmaSimpleHourlyForecastFragment setFinalHourlyForecastList(List<FinalHourlyForecast> finalHourlyForecastList) {
		this.finalHourlyForecastList = finalHourlyForecastList;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		//kma hourly forecast simple : 날짜, 시각, 날씨, 기온, 강수확률, 강수량
		Context context = getContext();
		
		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.date_row_height_in_simple_forecast_view);
		final int CLOCK_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.clock_row_height_in_simple_forecast_view);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.weather_icon_row_height_in_simple_forecast_view);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.default_row_height_in_simple_forecast_view);
		final int MARGIN = (int) context.getResources().getDimension(R.dimen.row_top_bottom_margin_in_simple_forecast_view);
		
		final int COLUMN_COUNT = finalHourlyForecastList.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.column_width_in_simple_hourly_forecast_view);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;
		
		//label column 설정
		final int LABEL_VIEW_WIDTH = (int) context.getResources().getDimension(R.dimen.label_view_width_in_simple_forecast_view);
		
		addLabelView(R.drawable.temp_icon, getString(R.string.date), LABEL_VIEW_WIDTH, DATE_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.clock), LABEL_VIEW_WIDTH, CLOCK_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), LABEL_VIEW_WIDTH, WEATHER_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT,
				MARGIN);
		addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_volume), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, MARGIN);
		
		dateRow = new DateView(context, VIEW_WIDTH, DATE_ROW_HEIGHT, COLUMN_WIDTH);
		ClockView clockRow = new ClockView(context, VIEW_WIDTH, CLOCK_ROW_HEIGHT, COLUMN_WIDTH);
		WeatherIconView weatherIconRow = new WeatherIconView(context, VIEW_WIDTH, WEATHER_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView tempRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView precipitationVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		
		//시각 --------------------------------------------------------------------------
		List<Date> dateTimeList = new ArrayList<>();
		for (FinalHourlyForecast finalHourlyForecast : finalHourlyForecastList) {
			dateTimeList.add(new Date(finalHourlyForecast.getFcstDateTime()));
		}
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		//날씨 아이콘
		
		//기온, 강수확률, 강수량
		List<String> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> precipitationVolumeList = new ArrayList<>();
		
		for (FinalHourlyForecast finalHourlyForecast : finalHourlyForecastList) {
			tempList.add(finalHourlyForecast.getTemp1Hour());
			probabilityOfPrecipitationList.add(finalHourlyForecast.getProbabilityOfPrecipitation());
			precipitationVolumeList.add(finalHourlyForecast.getRainPrecipitation1Hour());
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