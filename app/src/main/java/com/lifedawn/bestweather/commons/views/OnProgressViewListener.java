package com.lifedawn.bestweather.commons.views;

public interface OnProgressViewListener {
	void onSuccessfulProcessingData();

	void onFailedProcessingData(String text);

	void onStartedProcessingData();
}