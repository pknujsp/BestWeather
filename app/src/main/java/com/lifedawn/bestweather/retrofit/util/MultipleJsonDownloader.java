package com.lifedawn.bestweather.retrofit.util;

import android.util.ArrayMap;

import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.weathers.dataprocessing.MainProcessing;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public abstract class MultipleJsonDownloader<T> {
	private int requestCount;
	private int responseCount;

	protected Map<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, ResponseResult<T>>> responseMap
			= new ArrayMap<>();

	public MultipleJsonDownloader() {
	}

	public MultipleJsonDownloader(int requestCount) {
		this.requestCount = requestCount;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}

	public abstract void onResult();

	public void processResult(MainProcessing.WeatherSourceType weatherSourceType,
	                          RetrofitClient.ServiceType serviceType, Response<? extends T> response) {
		if (!responseMap.containsKey(weatherSourceType)) {
			responseMap.put(weatherSourceType, new ArrayMap<>());
		}
		responseMap.get(weatherSourceType).put(serviceType, new ResponseResult<T>(response));

		if (requestCount == ++responseCount) {
			onResult();
		}
	}

	public void processResult(MainProcessing.WeatherSourceType weatherSourceType,
	                          RetrofitClient.ServiceType serviceType, Throwable t) {
		if (!responseMap.containsKey(weatherSourceType)) {
			responseMap.put(weatherSourceType, new ArrayMap<>());
		}
		responseMap.get(weatherSourceType).put(serviceType, new ResponseResult<T>(t));

		if (requestCount == ++responseCount) {
			onResult();
		}
	}

	public static class ResponseResult<T> {
		private Response<? extends T> response;
		private Throwable t;

		public ResponseResult() {
		}

		public ResponseResult(Throwable t) {
			this.t = t;
		}

		public ResponseResult(Response<? extends T> response) {
			this.response = response;
		}

		public Response<? extends T> getResponse() {
			return response;
		}

		public void setResponse(Response<? extends T> response) {
			this.response = response;
		}

		public Throwable getT() {
			return t;
		}

		public void setT(Throwable t) {
			this.t = t;
		}
	}
}
