package com.lifedawn.bestweather.weathers.simplefragment.accuweather.currentconditions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.common.BaseSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class AccuCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		
		List<CurrentConditionsResponse.Item> items = currentConditionsResponse.getItems();
		CurrentConditionsResponse.Item item = items.get(0);
		
		if (item.getHasPrecipitation() != null) {
			// precipitation type 값 종류 : Rain, Snow, Ice, Null(Not), or Mixed
			String precipitation = AccuWeatherResponseProcessor.getPty(
					item.getPrecipitationType()) + ", " + item.getPrecip1hr().getMetric().getValue();
			
			binding.precipitation.setText(precipitation);
			binding.precipitation.setVisibility(View.VISIBLE);
		} else {
			binding.precipitation.setVisibility(View.GONE);
		}
		
		binding.sky.setText(AccuWeatherResponseProcessor.getWeatherIconDescription(item.getWeatherIcon()));
		binding.temperature.setText(item.getTemperature().getValue());
	}
}