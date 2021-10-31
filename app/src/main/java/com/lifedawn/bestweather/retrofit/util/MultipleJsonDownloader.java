package com.lifedawn.bestweather.retrofit.util;

import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public abstract class MultipleJsonDownloader<T> {
	private volatile int requestCount;
	private volatile int responseCount;
	private AlertDialog loadingDialog;
	private Map<String, String> valueMap = new HashMap<>();
	private LocalDateTime localDateTime = LocalDateTime.now();

	protected Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, ResponseResult<T>>> responseMap = new ArrayMap<>();

	public MultipleJsonDownloader() {
	}

	public Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, ResponseResult<T>>> getResponseMap() {
		return responseMap;
	}

	public MultipleJsonDownloader(int requestCount) {
		this.requestCount = requestCount;
	}

	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}

	public int getRequestCount() {
		return requestCount;
	}

	public void setResponseCount(int responseCount) {
		this.responseCount = responseCount;
	}

	public int getResponseCount() {
		return responseCount;
	}

	public String get(@NonNull @NotNull String key) {
		return valueMap.get(key);
	}


	public void setLoadingDialog(AlertDialog loadingDialog) {
		this.loadingDialog = loadingDialog;
	}

	public AlertDialog getLoadingDialog() {
		return loadingDialog;
	}

	public void put(@NonNull @NotNull String key, @NonNull @NotNull String value) {
		valueMap.put(key, value);
	}

	public void clear() {
		valueMap.clear();
	}

	public Map<String, String> getValueMap() {
		return valueMap;
	}

	public abstract void onResult();

	public void processResult(WeatherSourceType weatherSourceType, RequestParameter requestParameter, RetrofitClient.ServiceType serviceType,
	                          Response<? extends T> response) {
		if (!responseMap.containsKey(weatherSourceType)) {
			responseMap.put(weatherSourceType, new ArrayMap<>());
		}
		responseMap.get(weatherSourceType).put(serviceType, new ResponseResult<T>(requestParameter, response));

		if (requestCount == ++responseCount) {
			onResult();
		}
	}

	public void processResult(WeatherSourceType weatherSourceType, RequestParameter requestParameter, RetrofitClient.ServiceType serviceType, Throwable t) {
		if (!responseMap.containsKey(weatherSourceType)) {
			responseMap.put(weatherSourceType, new ArrayMap<>());
		}

		responseMap.get(weatherSourceType).put(serviceType, new ResponseResult<T>(requestParameter, t));

		if (requestCount == ++responseCount) {
			onResult();
		}
	}

	public RequestParameter getRequestParameter(WeatherSourceType weatherSourceType, RetrofitClient.ServiceType serviceType) {
		return responseMap.get(weatherSourceType).get(serviceType).getRequestParameter();
	}

	public static class ResponseResult<T> {
		private RequestParameter requestParameter;
		private Response<? extends T> response;
		private Throwable t;

		public ResponseResult() {
		}

		public ResponseResult(RequestParameter requestParameter, Throwable t) {
			this.t = t;
		}

		public ResponseResult(RequestParameter requestParameter, Response<? extends T> response) {
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

		public RequestParameter getRequestParameter() {
			return requestParameter;
		}
	}
}
