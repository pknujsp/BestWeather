package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

public class BaseSimpleCurrentConditionsFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleCurrentConditionsBinding binding;
	protected CurrentConditionsResponse currentConditionsResponse;
	protected GeolocalizedFeedResponse airQualityResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutSimpleCurrentConditionsBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void setValuesToViews() {
		setAqiValuesToViews();
	}
	
	public void setAqiValuesToViews() {
		if (airQualityResponse.getStatus().equals("ok")) {
			binding.airQualityInCurrentConditions.setVisibility(View.VISIBLE);
			binding.pm10Grade.setTextColor(
					AqicnResponseProcessor.getGradeColorId(Integer.parseInt(airQualityResponse.getData().getIaqi().getPm10().getValue())));
			binding.pm25Grade.setTextColor(
					AqicnResponseProcessor.getGradeColorId(Integer.parseInt(airQualityResponse.getData().getIaqi().getPm25().getValue())));
			
			binding.pm10Grade.setText(AqicnResponseProcessor.getGradeDescription(
					Integer.parseInt(airQualityResponse.getData().getIaqi().getPm10().getValue())));
			binding.pm25Grade.setText(AqicnResponseProcessor.getGradeDescription(
					Integer.parseInt(airQualityResponse.getData().getIaqi().getPm25().getValue())));
		} else {
			binding.airQualityInCurrentConditions.setVisibility(View.GONE);
		}
	}
}