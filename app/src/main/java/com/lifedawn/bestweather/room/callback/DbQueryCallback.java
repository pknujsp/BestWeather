package com.lifedawn.bestweather.room.callback;

public interface DbQueryCallback<T> {
	void onResultSuccessful(T result);

	void onResultNoData();

	default void processResult(T result) {
		if (result == null) {
			onResultNoData();
		} else {
			onResultSuccessful(result);
		}
	}
}