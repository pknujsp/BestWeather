package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.WeatherIconView;

import org.jetbrains.annotations.NotNull;

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
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.weatherIconValueRowHeightInSC);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);
		
		List<OneCallResponse.Hourly> items = oneCallResponse.getHourly();
		
		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;
		
		//label column 설정
		final int LABEL_VIEW_WIDTH = (int) context.getResources().getDimension(R.dimen.labelIconColumnWidthInCOMMON);
		
		addLabelView(R.drawable.temp_icon, getString(R.string.date), LABEL_VIEW_WIDTH, DATE_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.clock), LABEL_VIEW_WIDTH, CLOCK_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), LABEL_VIEW_WIDTH, WEATHER_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.rain_volume), LABEL_VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT);
		ImageView snowVolumeLabel = addLabelView(R.drawable.temp_icon, getString(R.string.snow_volume), LABEL_VIEW_WIDTH,
				DEFAULT_TEXT_ROW_HEIGHT);
		
		dateRow = new DateView(context, VIEW_WIDTH, DATE_ROW_HEIGHT, COLUMN_WIDTH);
		ClockView clockRow = new ClockView(context, VIEW_WIDTH, CLOCK_ROW_HEIGHT, COLUMN_WIDTH);
		WeatherIconView weatherIconRow = new WeatherIconView(context, VIEW_WIDTH, WEATHER_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView tempRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView rainVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView snowVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		
		//시각 --------------------------------------------------------------------------
		List<Date> dateTimeList = new ArrayList<>();
		for (OneCallResponse.Hourly item : items) {
			dateTimeList.add(new Date(Long.parseLong(item.getDt())));
		}
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		
		//날씨 아이콘
		
		//기온, 강수확률, 강수량
		List<String> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();
		
		boolean haveSnowVolumes = false;
		
		for (OneCallResponse.Hourly item : items) {
			tempList.add(item.getTemp());
			probabilityOfPrecipitationList.add(item.getPop());
			rainVolumeList.add(item.getRain() == null ? "-" : item.getRain().getPrecipitation1Hour());
			if (item.getSnow() != null) {
				if (!haveSnowVolumes) {
					haveSnowVolumes = true;
				}
			}
			snowVolumeList.add(item.getSnow() == null ? "-" : item.getSnow().getPrecipitation1Hour());
		}
		
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