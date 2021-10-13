package com.lifedawn.bestweather.weathers.simplefragment.kma.currentconditions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KmaCurrentConditionsFragment extends BaseSimpleCurrentConditionsFragment {
	private FinalCurrentConditions finalCurrentConditions;
	private FinalHourlyForecast finalHourlyForecast;
	
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
	
	public KmaCurrentConditionsFragment setFinalCurrentConditions(FinalCurrentConditions finalCurrentConditions) {
		this.finalCurrentConditions = finalCurrentConditions;
		return this;
	}
	
	public KmaCurrentConditionsFragment setFinalHourlyForecast(FinalHourlyForecast finalHourlyForecast) {
		this.finalHourlyForecast = finalHourlyForecast;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		
		if (finalCurrentConditions.getPrecipitationType() == null) {
			binding.precipitation.setVisibility(View.GONE);
		} else {
			// precipitation type 값 종류 : Rain, Snow, Ice, Null(Not), or Mixed
			String precipitation = KmaResponseProcessor.getWeatherPtyIconDescription(
					finalCurrentConditions.getPrecipitationType()) + ", " + finalCurrentConditions.getPrecipitation1Hour();
			
			binding.precipitation.setText(precipitation);
			binding.precipitation.setVisibility(View.VISIBLE);
		}
		binding.sky.setText(KmaResponseProcessor.getWeatherSkyIconDescription(finalHourlyForecast.getSky()));
		binding.temperature.setText(finalCurrentConditions.getTemperature());
	}
}