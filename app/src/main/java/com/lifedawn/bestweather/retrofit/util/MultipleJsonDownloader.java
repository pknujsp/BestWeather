package com.lifedawn.bestweather.retrofit.util;

import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.RequestParameter;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public abstract class MultipleJsonDownloader {
	private static final String tag = "MultipleJsonDownloader";
	private volatile int requestCount;
	private volatile int responseCount;
	private AlertDialog loadingDialog;
	private Map<String, String> valueMap = new HashMap<>();
	private ZonedDateTime localDateTime = ZonedDateTime.now();
	private Map<RetrofitClient.ServiceType, Call<?>> callMap = new HashMap<>();

	protected Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, ResponseResult>> responseMap = new ArrayMap<>();

	public MultipleJsonDownloader() {
	}

	public Map<RetrofitClient.ServiceType, Call<?>> getCallMap() {
		return callMap;
	}

	public Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, ResponseResult>> getResponseMap() {
		return responseMap;
	}

	public MultipleJsonDownloader(int requestCount) {
		this.requestCount = requestCount;
	}

	public ZonedDateTime getLocalDateTime() {
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


	public Map<String, String> getValueMap() {
		return valueMap;
	}

	public abstract void onResult();

	public abstract void onCanceled();

	public void cancel() {
		responseCount = requestCount + 1000;

		if (!callMap.isEmpty()) {
			for (Call<?> call : callMap.values()) {
				call.cancel();
			}
		}

	}

	public void processResult(WeatherSourceType weatherSourceType, RequestParameter requestParameter, RetrofitClient.ServiceType serviceType,
	                          Response<?> response) {
		if (!responseMap.containsKey(weatherSourceType)) {
			responseMap.put(weatherSourceType, new ArrayMap<>());
		}
		responseMap.get(weatherSourceType).put(serviceType, new ResponseResult(requestParameter, response));
		Log.e(tag, "requestCount : " + requestCount + ",  responseCount : " + responseCount);

		if (requestCount == ++responseCount) {
			Log.e(tag, "requestCount : " + requestCount + ",  responseCount : " + responseCount);
			onResult();
		}
	}

	public void processResult(WeatherSourceType weatherSourceType, RequestParameter requestParameter, RetrofitClient.ServiceType serviceType, Throwable t) {
		if (!responseMap.containsKey(weatherSourceType)) {
			responseMap.put(weatherSourceType, new ArrayMap<>());
		}

		responseMap.get(weatherSourceType).put(serviceType, new ResponseResult(requestParameter, t));
		Log.e(tag, "requestCount : " + requestCount + ",  responseCount : " + responseCount);

		if (requestCount == ++responseCount) {
			Log.e(tag, "requestCount : " + requestCount + ",  responseCount : " + responseCount);
			onResult();
		}
	}

	public RequestParameter getRequestParameter(WeatherSourceType weatherSourceType, RetrofitClient.ServiceType serviceType) {
		return responseMap.get(weatherSourceType).get(serviceType).getRequestParameter();
	}

	public static class ResponseResult {
		private final RequestParameter requestParameter;
		private final boolean successful;

		private Response<?> response;
		private Throwable t;
		private String responseStr;

		public ResponseResult(RequestParameter requestParameter, Throwable t) {
			this.t = t;
			successful = false;
			this.requestParameter = requestParameter;
		}

		public ResponseResult(RequestParameter requestParameter, Response<?> response) {
			this.response = response;
			successful = true;
			this.requestParameter = requestParameter;
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

		public String getResponseStr() {
			return responseStr;
		}

		public ResponseResult setResponseStr(String responseStr) {
			this.responseStr = responseStr;
			return this;
		}
	}
}
