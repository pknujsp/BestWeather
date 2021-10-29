package com.lifedawn.bestweather.retrofit.util;

import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public abstract class MultipleJsonDownloader<T> {
	private int requestCount;
	private int responseCount;
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


	public String get(@NonNull @NotNull String key) {
		return valueMap.get(key);
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

	public void processResult(WeatherSourceType weatherSourceType, RetrofitClient.ServiceType serviceType,
	                          Response<? extends T> response) {
		if (!responseMap.containsKey(weatherSourceType)) {
			responseMap.put(weatherSourceType, new ArrayMap<>());
		}
		responseMap.get(weatherSourceType).put(serviceType, new ResponseResult<T>(response));

		if (requestCount == ++responseCount) {
			onResult();
		}
	}

	public void processResult(WeatherSourceType weatherSourceType, RetrofitClient.ServiceType serviceType, Throwable t) {
		if (!responseMap.containsKey(weatherSourceType)) {
			responseMap.put(weatherSourceType, new ArrayMap<>());
		}

		if (!responseMap.get(weatherSourceType).containsKey(serviceType)) {
			responseMap.get(weatherSourceType).put(serviceType, new ResponseResult<T>(t));

			if (requestCount == ++responseCount) {
				onResult();
			}
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
