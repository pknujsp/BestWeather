package com.lifedawn.bestweather.retrofit.util;

import retrofit2.Response;

public abstract class JsonDownloader<T> {
	public abstract void onResponseResult(Response<? extends T> response);

	public abstract void onResponseResult(Throwable t);

}