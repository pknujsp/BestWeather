package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.ArraySet;
import android.util.Log;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.CurrentWeatherParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.DailyForecastParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;

import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenWeatherMapProcessing {
	/**
	 * current weather
	 */
	public static Call<JsonElement> getCurrentWeather(CurrentWeatherParameter currentWeatherParameter,
	                                                  JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_CURRENT_WEATHER);

		Call<JsonElement> call = querys.getCurrentWeather(currentWeatherParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		return call;
	}

	/**
	 * daily forecast
	 */
	public static Call<JsonElement> getDailyForecast(DailyForecastParameter dailyForecastParameter,
	                                                 JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

		Call<JsonElement> call = querys.getDailyForecast(dailyForecastParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		return call;
	}

	/**
	 * one call
	 */
	public static Call<JsonElement> getOneCall(OneCallParameter oneCallParameter,
	                                           JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_ONE_CALL);

		Call<JsonElement> call = querys.getOneCall(oneCallParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
				Log.e(RetrofitClient.LOG_TAG, "own one call 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "own one call 실패");
			}
		});

		return call;
	}

	public static void getOwmForecasts(String latitude, String longitude, boolean useOneCall,
	                                   MultipleJsonDownloader multipleJsonDownloader) {
		if (useOneCall) {
			OneCallParameter oneCallParameter = new OneCallParameter();
			Set<OneCallParameter.OneCallApis> excludeOneCallApis = new ArraySet<>();
			excludeOneCallApis.add(OneCallParameter.OneCallApis.alerts);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.minutely);
			oneCallParameter.setLatitude(latitude).setLongitude(longitude).setOneCallApis(excludeOneCallApis);

			Call<JsonElement> oneCallCall = getOneCall(oneCallParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response) {
					Log.e(RetrofitClient.LOG_TAG, "own one call 성공");
					multipleJsonDownloader.processResult(WeatherSourceType.OPEN_WEATHER_MAP, oneCallParameter,
							RetrofitClient.ServiceType.OWM_ONE_CALL, response);
				}

				@Override
				public void onResponseResult(Throwable t) {
					Log.e(RetrofitClient.LOG_TAG, "own one call 실패");
					multipleJsonDownloader.processResult(WeatherSourceType.OPEN_WEATHER_MAP, oneCallParameter,
							RetrofitClient.ServiceType.OWM_ONE_CALL, t);
				}


			});
		}
	}

	public static void requestWeatherData(Context context, Double latitude, Double longitude,
	                                      RequestOwm requestOwm,
	                                      MultipleJsonDownloader multipleJsonDownloader) {
		OneCallParameter oneCallParameter = new OneCallParameter();
		Set<OneCallParameter.OneCallApis> excludeOneCallApis = requestOwm.getExcludeApis();
		oneCallParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString()).setOneCallApis(excludeOneCallApis);

		Call<JsonElement> oneCallCall = getOneCall(oneCallParameter, new JsonDownloader() {
			@Override
			public void onResponseResult(Response<?> response) {
				multipleJsonDownloader.processResult(WeatherSourceType.OPEN_WEATHER_MAP, oneCallParameter,
						RetrofitClient.ServiceType.OWM_ONE_CALL, response);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleJsonDownloader.processResult(WeatherSourceType.OPEN_WEATHER_MAP, oneCallParameter,
						RetrofitClient.ServiceType.OWM_ONE_CALL, t);
			}

		});
		multipleJsonDownloader.getCallMap().put(RetrofitClient.ServiceType.OWM_ONE_CALL, oneCallCall);

	}
}
