package com.lifedawn.bestweather.weathers.dataprocessing;

import android.util.ArraySet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.metnorway.LocationForecastParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.CurrentWeatherParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.DailyForecastParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;

import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenWeatherMapProcessing {
	/**
	 * current weather
	 */
	public static Call<JsonObject> getCurrentWeather(CurrentWeatherParameter currentWeatherParameter,
	                                                 JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_CURRENT_WEATHER);

		Call<JsonObject> call = querys.getCurrentWeather(currentWeatherParameter.getMap());
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
			}

			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}

	/**
	 * daily forecast
	 */
	public static Call<JsonObject> getDailyForecast(DailyForecastParameter dailyForecastParameter,
	                                                JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

		Call<JsonObject> call = querys.getDailyForecast(dailyForecastParameter.getMap());
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
			}

			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}

	/**
	 * one call
	 */
	public static Call<JsonObject> getOneCall(OneCallParameter oneCallParameter,
	                                          JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_ONE_CALL);

		Call<JsonObject> call = querys.getOneCall(oneCallParameter.getMap());
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
			}

			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}

	public static void getOwmForecasts(String latitude, String longitude, boolean useOneCall,
	                                   MultipleJsonDownloader<JsonObject> multipleJsonDownloader) {
		if (useOneCall) {
			OneCallParameter oneCallParameter = new OneCallParameter();
			Set<OneCallParameter.OneCallApis> excludeOneCallApis = new ArraySet<>();
			excludeOneCallApis.add(OneCallParameter.OneCallApis.alerts);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.minutely);
			oneCallParameter.setLatitude(latitude).setLongitude(longitude).setOneCallApis(excludeOneCallApis);

			Call<JsonObject> oneCallCall = getOneCall(oneCallParameter, new JsonDownloader<JsonObject>() {
				@Override
				public void onResponseSuccessful(Response<? extends JsonObject> response) {
					Gson gson = new Gson();
					OneCallResponse oneCallResponse = gson.fromJson(response.body().toString(), OneCallResponse.class);
				}

				@Override
				public void onResponseFailed(Exception e) {

				}

				@Override
				public void processResult(Response<? extends JsonObject> response) {
					if (response.body() != null) {
						onResponseSuccessful(response);
					} else {
						onResponseFailed(new Exception(response.toString()));
					}
				}
			});
		}
	}
}
