package com.lifedawn.bestweather.commons.interfaces;

public interface OnProgressViewListener {
	void onSuccessful();

	void onFailed(String text);

	void onStarted();
}