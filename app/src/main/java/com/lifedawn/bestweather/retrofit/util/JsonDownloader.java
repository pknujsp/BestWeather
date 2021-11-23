package com.lifedawn.bestweather.retrofit.util;

import com.google.gson.JsonElement;

import retrofit2.Response;

public abstract class JsonDownloader {
	public abstract void onResponseResult(Response<?> response);

	public abstract void onResponseResult(Throwable t);

}