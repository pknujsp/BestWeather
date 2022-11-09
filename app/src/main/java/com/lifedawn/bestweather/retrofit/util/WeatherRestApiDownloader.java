package com.lifedawn.bestweather.retrofit.util;

import android.util.ArrayMap;

import androidx.annotation.NonNull;

import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Response;

public abstract class WeatherRestApiDownloader {
	private final ZonedDateTime requestDateTime = ZonedDateTime.now();
	private int requestCount;
	private AtomicInteger responseCount = new AtomicInteger(0);
	private boolean responseCompleted;
	private ZoneId zoneId;

	public Map<String, String> valueMap = new ConcurrentHashMap<>();
	public Map<RetrofitClient.ServiceType, Call<?>> callMap = new ConcurrentHashMap<>();

	public Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, ResponseResult>> responseMap = new ConcurrentHashMap<>();

	public WeatherRestApiDownloader() {
	}

	public WeatherRestApiDownloader setResponseCompleted(boolean responseCompleted) {
		this.responseCompleted = responseCompleted;
		return this;
	}

	public WeatherRestApiDownloader setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
		return this;
	}

	private synchronized int getResponseCount() {
		return responseCount.get();
	}

	public ZoneId getZoneId() {
		return zoneId;
	}

	public Map<RetrofitClient.ServiceType, Call<?>> getCallMap() {
		return callMap;
	}

	public Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, ResponseResult>> getResponseMap() {
		return responseMap;
	}

	public WeatherRestApiDownloader(int requestCount) {
		this.requestCount = requestCount;
	}

	public ZonedDateTime getRequestDateTime() {
		return requestDateTime;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}

	public int getRequestCount() {
		return requestCount;
	}

	public void setResponseCount(int responseCount) {
		this.responseCount.set(responseCount);
	}


	public String get(@NonNull @NotNull String key) {
		return valueMap.get(key);
	}


	public void put(@NonNull @NotNull String key, @NonNull @NotNull String value) {
		valueMap.put(key, value);
	}

	public boolean isResponseCompleted() {
		return responseCompleted;
	}

	public Map<String, String> getValueMap() {
		return valueMap;
	}

	public abstract void onResult();

	public abstract void onCanceled();

	public void cancel() {
		responseCount.set(10000);

		if (!callMap.isEmpty()) {
			for (Call<?> call : callMap.values()) {
				call.cancel();
			}
		}

	}

	public void processResult(WeatherProviderType weatherProviderType, RequestParameter requestParameter, RetrofitClient.ServiceType serviceType,
	                          Response<?> response, Object responseObj, String responseText) {
		responseCount.incrementAndGet();

		if (!responseMap.containsKey(weatherProviderType)) {
			responseMap.put(weatherProviderType, new ArrayMap<>());
		}
		responseMap.get(weatherProviderType).put(serviceType, new ResponseResult(requestParameter, response, responseObj, responseText));

		if (requestCount == responseCount.get()) {
			responseCompleted = true;
			onResult();
		}
	}

	public void processResult(WeatherProviderType weatherProviderType, RequestParameter requestParameter, RetrofitClient.ServiceType serviceType, Throwable t) {
		responseCount.incrementAndGet();

		if (!responseMap.containsKey(weatherProviderType)) {
			responseMap.put(weatherProviderType, new ArrayMap<>());
		}

		responseMap.get(weatherProviderType).put(serviceType, new ResponseResult(requestParameter, t));

		if (requestCount == responseCount.get()) {
			responseCompleted = true;
			onResult();
		}
	}

	public RequestParameter getRequestParameter(WeatherProviderType weatherProviderType, RetrofitClient.ServiceType serviceType) {
		return responseMap.get(weatherProviderType).get(serviceType).getRequestParameter();
	}

	public static class ResponseResult {
		private final RequestParameter requestParameter;
		private boolean successful;

		private Response<?> response;
		private Throwable t;
		private String responseText;
		private Object responseObj;

		public ResponseResult(RequestParameter requestParameter, Throwable t) {
			this.t = t;
			successful = false;
			this.requestParameter = requestParameter;
		}

		public ResponseResult(RequestParameter requestParameter, Response<?> response, Object responseObj, String responseText) {
			this.response = response;
			successful = true;
			this.responseObj = responseObj;
			this.requestParameter = requestParameter;
			this.responseText = responseText;
		}

		public Response<?> getResponse() {
			return response;
		}

		public void setResponse(Response<?> response) {
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

		public boolean isSuccessful() {
			return successful;
		}

		public String getResponseText() {
			return responseText;
		}

		public Object getResponseObj() {
			return responseObj;
		}
	}
}
