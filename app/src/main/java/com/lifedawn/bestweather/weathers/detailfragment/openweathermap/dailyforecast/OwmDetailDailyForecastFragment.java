package com.lifedawn.bestweather.weathers.detailfragment.openweathermap.dailyforecast;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.DoubleWindDirectionView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.SingleWindDirectionView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwmDetailDailyForecastFragment extends BaseDetailForecastFragment {
	private List<OneCallResponse.Daily> dailyList;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.detail_daily_forecast);
		setValuesToViews();
	}

	public OwmDetailDailyForecastFragment setDailyList(List<OneCallResponse.Daily> dailyList) {
		this.dailyList = dailyList;
		return this;
	}

	@Override
	public void setValuesToViews() {
		Context context = getContext();

		final int dateRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int weatherRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInD);
		final int tempRowHeight = (int) getResources().getDimension(R.dimen.doubleTemperatureRowHeightInD);
		final int windDirectionRowHeight = (int) getResources().getDimension(R.dimen.singleWindDirectionIconValueRowHeightInD);
		final int defaultTextRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInD);

		final int columnsCount = dailyList.size();
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInDDaily);
		final int viewWidth = columnsCount * columnWidth;
		SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E", Locale.getDefault());

		List<String> dateList = new ArrayList<>();
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> popList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();
		List<SingleWindDirectionView.WindDirectionObj> windDirectionList = new ArrayList<>();
		List<String> windSpeedList = new ArrayList<>();
		List<String> windStrengthList = new ArrayList<>();
		List<String> windGustList = new ArrayList<>();
		List<String> pressureList = new ArrayList<>();
		List<String> humidityList = new ArrayList<>();
		List<String> dewPointList = new ArrayList<>();
		List<String> cloudinessList = new ArrayList<>();
		List<String> uvMaxIndexList = new ArrayList<>();

		for (OneCallResponse.Daily daily : dailyList) {
			dateList.add(dateFormat.format(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(daily.getDt()) * 1000L)));
			minTempList.add(ValueUnits.convertTemperature(daily.getTemp().getMin(), tempUnit));
			maxTempList.add(ValueUnits.convertTemperature(daily.getTemp().getMax(), tempUnit));
			popList.add(String.valueOf((int) (Double.parseDouble(daily.getPop()) * 100.0)));
			rainVolumeList.add(daily.getRain() == null ? "-" : daily.getRain());
			snowVolumeList.add(daily.getSnow() == null ? "-" : daily.getSnow());
			windDirectionList.add(new SingleWindDirectionView.WindDirectionObj(Integer.parseInt(daily.getWindDeg())));
			windSpeedList.add(ValueUnits.convertWindSpeed(daily.getWindSpeed(), windUnit).toString());
			windStrengthList.add(WeatherResponseProcessor.getSimpleWindSpeedDescription(daily.getWindSpeed()));
			windGustList.add(daily.getWindGust() == null ? "-" : daily.getWindGust());
			pressureList.add(daily.getPressure());
			humidityList.add(daily.getHumidity());
			dewPointList.add(daily.getDew_point());
			cloudinessList.add(daily.getClouds());
			uvMaxIndexList.add(daily.getUvi());
		}

		//순서 : 날짜, 날씨상태, 최저/최고 기온, 강수확률, 하루 강우량(nullable), 하루 강설량(nullable)
		//풍향, 풍속, 바람세기, 돌풍(nullable), 기압, 습도, 이슬점, 운량, 자외선최고치

		//아침/낮/저녁/밤 기온(체감) 제외
		addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), weatherRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), tempRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.rain_volume), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.snow_volume), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.wind_direction), windDirectionRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.wind_speed), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.wind_strength), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.wind_gust), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.pressure), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.humidity), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.dew_point), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.cloud_cover), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.uv_index), defaultTextRowHeight);

		TextValueView dateRow = new TextValueView(context, viewWidth, dateRowHeight, columnWidth);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, viewWidth, weatherRowHeight, columnWidth);
		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(context, viewWidth, tempRowHeight, columnWidth, minTempList,
				maxTempList);
		TextValueView popRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView rainVolumeRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView snowVolumeRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		SingleWindDirectionView windDirectionRow = new SingleWindDirectionView(context, viewWidth, windDirectionRowHeight, columnWidth);
		TextValueView windSpeedRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView windStrengthRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView windGustRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView pressureRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView humidityRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView dewPointRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView cloudCoverRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView uvIndexRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);

		dateRow.setValueList(dateList);
		popRow.setValueList(popList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);
		windDirectionRow.setIcons(windDirectionList);
		windSpeedRow.setValueList(windSpeedList);
		windStrengthRow.setValueList(windStrengthList);
		windGustRow.setValueList(windGustList);
		pressureRow.setValueList(pressureList);
		humidityRow.setValueList(humidityList);
		dewPointRow.setValueList(dewPointList);
		cloudCoverRow.setValueList(cloudinessList);
		uvIndexRow.setValueList(uvMaxIndexList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER;

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(popRow, rowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
		binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
		binding.forecastView.addView(windDirectionRow, rowLayoutParams);
		binding.forecastView.addView(windSpeedRow, rowLayoutParams);
		binding.forecastView.addView(windStrengthRow, rowLayoutParams);
		binding.forecastView.addView(windGustRow, rowLayoutParams);
		binding.forecastView.addView(pressureRow, rowLayoutParams);
		binding.forecastView.addView(humidityRow, rowLayoutParams);
		binding.forecastView.addView(dewPointRow, rowLayoutParams);
		binding.forecastView.addView(cloudCoverRow, rowLayoutParams);
		binding.forecastView.addView(uvIndexRow, rowLayoutParams);
	}
}
