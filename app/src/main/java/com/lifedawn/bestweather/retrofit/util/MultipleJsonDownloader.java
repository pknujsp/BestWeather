package com.lifedawn.bestweather.retrofit.util;

import com.google.gson.JsonObject;

import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public abstract class MultipleJsonDownloader {
	final int REQUEST_COUNT;
	int responseCount;
	
	List<Response<JsonObject>> responseList = new ArrayList<>();
	List<Exception> exceptionList = new ArrayList<>();
	
	public MultipleJsonDownloader(int REQUEST_COUNT) {
		this.REQUEST_COUNT = REQUEST_COUNT;
	}
	
	public abstract void onResult();
	
	public void processResult(Response<JsonObject> response) {
		responseList.add(response);
		if (REQUEST_COUNT == ++responseCount) {
			onResult();
		}
	}
	
	public void processResult(Exception e) {
		exceptionList.add(e);
		if (REQUEST_COUNT == ++responseCount) {
			onResult();
		}
	}
}
