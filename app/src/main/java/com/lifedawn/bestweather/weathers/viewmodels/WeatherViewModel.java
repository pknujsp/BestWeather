package com.lifedawn.bestweather.weathers.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;

import org.jetbrains.annotations.NotNull;

public class WeatherViewModel extends AndroidViewModel {
	private ILoadImgOfCurrentConditions iLoadImgOfCurrentConditions;

	public WeatherViewModel(@NonNull @NotNull Application application) {
		super(application);
	}

	public void setiLoadImgOfCurrentConditions(ILoadImgOfCurrentConditions iLoadImgOfCurrentConditions) {
		this.iLoadImgOfCurrentConditions = iLoadImgOfCurrentConditions;
	}

	public ILoadImgOfCurrentConditions getiLoadImgOfCurrentConditions() {
		return iLoadImgOfCurrentConditions;
	}

	public interface ILoadImgOfCurrentConditions {
		void loadImgOfCurrentConditions(MainProcessing.WeatherSourceType weatherSourceType, String val, Double latitude, Double longitude);
	}
}
