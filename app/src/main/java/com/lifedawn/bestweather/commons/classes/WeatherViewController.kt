package com.lifedawn.bestweather.commons.classes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;

public class WeatherViewController implements DefaultLifecycleObserver {
	private WeatherView weatherView;

	public WeatherViewController(WeatherView weatherView) {
		this.weatherView = weatherView;
	}

	public void setWeatherView(PrecipType precipType, @Nullable String volume) {
		weatherView.setWeatherData(precipType);
		if (precipType == PrecipType.CLEAR) {
		} else {
			// mm단위
			int volumeValue = 0;

			if (volume == null) {

			} else {
				if (volume.contains("mm") || volume.contains("cm")) {
					String intStr = volume.replaceAll("[^0-9]", "");
					volumeValue = Integer.parseInt(intStr);

					if (volume.contains("cm")) {
						volumeValue = (volumeValue * 100) / 10;
					}
				}
			}


			final float originalEr = weatherView.getEmissionRate();
			float amount = 0;

			if (precipType == PrecipType.RAIN) {
				/*
			‘약한 비’는 1시간에 3㎜ 미만
			‘(보통) 비’는 1시간에 3∼15㎜
			‘강한 비’는 1시간에 15㎜ 이상
			‘매우 강한 비’는 1시간에 30㎜ 이상
				 */
				if (volumeValue >= 30) {
					amount = 1.3f;
				} else if (volumeValue >= 15) {
					amount = 1.2f;
				} else if (volumeValue >= 3) {
					amount = 0.9f;
				} else {
					amount = 0.7f;
				}

				weatherView.setEmissionRate(originalEr * amount);
			} else if (precipType == PrecipType.SNOW) {

			}
		}
	}

	@Override
	public void onCreate(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onCreate(owner);
	}

	@Override
	public void onStart(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onStart(owner);
	}

	@Override
	public void onResume(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onResume(owner);
	}

	@Override
	public void onPause(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onPause(owner);
	}

	@Override
	public void onStop(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onStop(owner);
	}

	@Override
	public void onDestroy(@NonNull LifecycleOwner owner) {
		DefaultLifecycleObserver.super.onDestroy(owner);
		weatherView = null;
	}
}
