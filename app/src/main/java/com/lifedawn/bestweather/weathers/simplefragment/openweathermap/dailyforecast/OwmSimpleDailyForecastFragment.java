package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.dailyforecast;

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
import com.lifedawn.bestweather.weathers.comparison.dailyforecast.DailyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.dailyforecast.OwmDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwmSimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private OneCallResponse oneCallResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast);
		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DailyForecastComparisonFragment comparisonFragment = new DailyForecastComparisonFragment();
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
			public void onClick(View view) {
				OwmDetailDailyForecastFragment detailDailyForecastFragment = new OwmDetailDailyForecastFragment();
				detailDailyForecastFragment.setDailyList(oneCallResponse.getDaily());
				
				Bundle bundle = new Bundle();
				bundle.putString(getString(R.string.bundle_key_address_name), addressName);
				detailDailyForecastFragment.setArguments(bundle);
				
				String tag = getString(R.string.tag_detail_daily_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						detailDailyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});
		
		setValuesToViews();
	}
	
	public OwmSimpleDailyForecastFragment setOneCallResponse(OneCallResponse oneCallResponse) {
		this.oneCallResponse = oneCallResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜 ,낮과 밤의 날씨상태, 강수확률, 강우량, 강설량, 최저/최고 기온
		Context context = getContext();
		
		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);
		final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.doubleTemperatureRowHeightInSC);
		
		List<OneCallResponse.Daily> items = oneCallResponse.getDaily();
		
		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCDaily);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;
		
		addLabelView(R.drawable.temp_icon, getString(R.string.date), DATE_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), WEATHER_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.rain_volume), DEFAULT_TEXT_ROW_HEIGHT);
		ImageView snowVolumeLabel = addLabelView(R.drawable.temp_icon, getString(R.string.snow_volume), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), TEMP_ROW_HEIGHT);
		
		TextValueView dateRow = new TextValueView(context, VIEW_WIDTH, DATE_ROW_HEIGHT, COLUMN_WIDTH);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, VIEW_WIDTH, WEATHER_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView rainVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		TextValueView snowVolumeRow = new TextValueView(context, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		
		//시각 --------------------------------------------------------------------------
		List<String> dateList = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d E", Locale.getDefault());
		
		for (OneCallResponse.Daily item : items) {
			dateList.add(dateFormat.format(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getDt()) * 1000L)));
		}
		dateRow.setValueList(dateList);
		
		//날씨 아이콘
		
		//기온, 강수확률, 강수량
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();
		
		String rainVolume = null;
		String snowVolume = null;
		
		boolean haveSnowVolumes = false;
		
		for (OneCallResponse.Daily item : items) {
			minTempList.add(ValueUnits.convertTemperature(item.getTemp().getMin(), tempUnit));
			maxTempList.add(ValueUnits.convertTemperature(item.getTemp().getMax(), tempUnit));
			
			rainVolume = item.getRain() == null ? "-" : item.getRain();
			if (item.getSnow() != null) {
				if (!haveSnowVolumes) {
					haveSnowVolumes = true;
				}
			}
			snowVolume = item.getSnow() == null ? "-" : item.getSnow();
			
			probabilityOfPrecipitationList.add(String.valueOf((int) (Double.parseDouble(item.getPop()) * 100.0)));
			rainVolumeList.add(rainVolume);
			snowVolumeList.add(snowVolume);
		}
		
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);
		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), VIEW_WIDTH, TEMP_ROW_HEIGHT, COLUMN_WIDTH,
				minTempList, maxTempList);
		
		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		
		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
		if (haveSnowVolumes) {
			binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
		} else {
			binding.labels.removeView(snowVolumeLabel);
		}
		binding.forecastView.addView(tempRow, rowLayoutParams);
	}
	
}