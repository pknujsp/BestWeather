package com.lifedawn.bestweather.weathers.detailfragment.accuweather.currentconditions;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailCurrentConditionsFragment;

import org.jetbrains.annotations.NotNull;

public class AccuCurrentConditionsDetailFragment extends BaseDetailCurrentConditionsFragment {
	private CurrentConditionsResponse currentConditionsResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	public AccuCurrentConditionsDetailFragment setCurrentConditionsResponse(CurrentConditionsResponse currentConditionsResponse) {
		this.currentConditionsResponse = currentConditionsResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
	
	}
}
