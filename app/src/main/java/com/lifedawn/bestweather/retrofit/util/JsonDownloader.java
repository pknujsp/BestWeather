package com.lifedawn.bestweather.retrofit.util;

import com.google.gson.JsonObject;

import retrofit2.Response;

public abstract class JsonDownloader {
	public abstract void onResponseSuccessful(Response<JsonObject> response);
	
	public abstract void onResponseFailed(Exception e);
	
	public abstract void processResult(Response<JsonObject> response);
	
	public void processResult(Throwable t) {
		onResponseFailed(new Exception(t));
	}
}