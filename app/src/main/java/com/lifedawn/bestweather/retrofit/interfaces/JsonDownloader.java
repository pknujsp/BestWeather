package com.lifedawn.bestweather.retrofit.interfaces;

import retrofit2.Response;

public abstract class JsonDownloader {
	public abstract void onResponseSuccessful(Response response);
	
	public abstract void onResponseFailed(Exception e);
	
	public abstract void processResult(Response response);
	
	public void processResult(Throwable t) {
		onResponseFailed(new Exception(t));
	}
}