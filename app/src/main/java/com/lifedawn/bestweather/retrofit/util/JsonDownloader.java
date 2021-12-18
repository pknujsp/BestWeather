package com.lifedawn.bestweather.retrofit.util;

import retrofit2.Response;

public abstract class JsonDownloader {
	public abstract void onResponseResult(Response<?> response, Object responseObj, String responseText);

	public abstract void onResponseResult(Throwable t);

}