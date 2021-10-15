package com.lifedawn.bestweather.weathers.simplefragment.aqicn;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentAirQualitySimpleBinding;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class SimpleAirQualityFragment extends Fragment implements IWeatherValues {
	private FragmentAirQualitySimpleBinding binding;
	private GeolocalizedFeedResponse geolocalizedFeedResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentAirQualitySimpleBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.weatherCardViewHeader.forecastName.setText(R.string.air_quality);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// GeolocalizedFeedResponse 전달
			}
		});
		
		setValuesToViews();
	}
	
	public SimpleAirQualityFragment setGeolocalizedFeedResponse(GeolocalizedFeedResponse geolocalizedFeedResponse) {
		this.geolocalizedFeedResponse = geolocalizedFeedResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		final Double pm10Val = Double.valueOf(geolocalizedFeedResponse.getData().getIaqi().getPm10().getValue());
		final Double pm25Val = Double.valueOf(geolocalizedFeedResponse.getData().getIaqi().getPm25().getValue());
		final Double coVal = Double.valueOf(geolocalizedFeedResponse.getData().getIaqi().getCo().getValue());
		final Double no2Val = Double.valueOf(geolocalizedFeedResponse.getData().getIaqi().getNo2().getValue());
		final Double o3Val = Double.valueOf(geolocalizedFeedResponse.getData().getIaqi().getO3().getValue());
		final Double so2Val = Double.valueOf(geolocalizedFeedResponse.getData().getIaqi().getSo2().getValue());
		
		binding.pm10.setTextColor(AqicnResponseProcessor.getGradeColorId(pm10Val.intValue()));
		binding.pm25.setTextColor(AqicnResponseProcessor.getGradeColorId(pm25Val.intValue()));
		binding.co.setTextColor(AqicnResponseProcessor.getGradeColorId(coVal.intValue()));
		binding.no2.setTextColor(AqicnResponseProcessor.getGradeColorId(no2Val.intValue()));
		binding.o3.setTextColor(AqicnResponseProcessor.getGradeColorId(o3Val.intValue()));
		binding.so2.setTextColor(AqicnResponseProcessor.getGradeColorId(so2Val.intValue()));
		
		binding.pm10.setText(AqicnResponseProcessor.getGradeDescription(pm10Val.intValue()));
		binding.pm25.setText(AqicnResponseProcessor.getGradeDescription(pm25Val.intValue()));
		binding.co.setText(AqicnResponseProcessor.getGradeDescription(coVal.intValue()));
		binding.no2.setText(AqicnResponseProcessor.getGradeDescription(no2Val.intValue()));
		binding.o3.setText(AqicnResponseProcessor.getGradeDescription(o3Val.intValue()));
		binding.so2.setText(AqicnResponseProcessor.getGradeDescription(so2Val.intValue()));
		
		ArrayMap<String, ForecastObj> forecastObjMap = new ArrayMap<>();
		
		// 2021-10-13
		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm10Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getPm10();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm10Forecast) {
			if (!forecastObjMap.containsKey(valueMap.getDay())) {
				forecastObjMap.put(valueMap.getDay(), new ForecastObj(valueMap.getDay()));
			}
			forecastObjMap.get(valueMap.getDay()).pm10 = valueMap.getAvg();
		}
		
		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm25Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getPm25();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm25Forecast) {
			if (!forecastObjMap.containsKey(valueMap.getDay())) {
				forecastObjMap.put(valueMap.getDay(), new ForecastObj(valueMap.getDay()));
			}
			forecastObjMap.get(valueMap.getDay()).pm25 = valueMap.getAvg();
		}
		
		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> o3Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getO3();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : o3Forecast) {
			if (!forecastObjMap.containsKey(valueMap.getDay())) {
				forecastObjMap.put(valueMap.getDay(), new ForecastObj(valueMap.getDay()));
			}
			forecastObjMap.get(valueMap.getDay()).o3 = valueMap.getAvg();
		}
		
		ForecastObj[] forecastObjArr = new ForecastObj[1];
		List<ForecastObj> forecastObjList = Arrays.asList(forecastObjMap.values().toArray(forecastObjArr));
		Collections.sort(forecastObjList, new Comparator<ForecastObj>() {
			@Override
			public int compare(ForecastObj forecastObj, ForecastObj t1) {
				return forecastObj.date.compareTo(t1.date);
			}
		});
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd E", Locale.getDefault());
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		
		for (ForecastObj forecastObj : forecastObjList) {
			View forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
			((TextView) forecastItemView.findViewById(R.id.date)).setText(dateFormat.format(forecastObj.date));
			((TextView) forecastItemView.findViewById(R.id.pm10)).setText(forecastObj.pm10);
			((TextView) forecastItemView.findViewById(R.id.pm25)).setText(forecastObj.pm25);
			((TextView) forecastItemView.findViewById(R.id.o3)).setText(forecastObj.o3);
			
			binding.forecast.addView(forecastItemView);
		}
	}
	
	static class ForecastObj {
		Date date;
		String pm10;
		String pm25;
		String o3;
		
		public ForecastObj(String day) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
				this.date = dateFormat.parse(day);
			} catch (Exception e) {
			
			}
		}
	}
	
}