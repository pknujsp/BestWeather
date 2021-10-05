package com.lifedawn.bestweather.retrofit.util;

import com.google.gson.JsonObject;

import retrofit2.Response;

public abstract class JsonDownloader<T> {
	public abstract void onResponseSuccessful(Response<? extends T> response);

	public abstract void onResponseFailed(Exception e);

	public abstract void processResult(Response<? extends T> response);

	public void processResult(Throwable t) {
		onResponseFailed(new Exception(t));
	}
}