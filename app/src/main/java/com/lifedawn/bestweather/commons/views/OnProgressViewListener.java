package com.lifedawn.bestweather.commons.views;

public interface OnProgressViewListener {
	void onSuccessful();

	void onFailed(String text);

	void onStarted();
}