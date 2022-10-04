package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.Queries;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.individual.OwmCurrentWeatherParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.individual.OwmDailyForecastParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.individual.OwmHourlyForecastParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.onecall.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.currentweather.OwmCurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.dailyforecast.OwmDailyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.hourlyforecast.OwmHourlyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenWeatherMapProcessing {

	/**
	 * one call
	 */
	public static Call<JsonElement> getOneCall(OneCallParameter oneCallParameter,
	                                           JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_ONE_CALL);

		Call<JsonElement> call = queries.getOneCall(oneCallParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				OwmOneCallResponse owmOneCallResponse = OpenWeatherMapResponseProcessor.getOneCallObjFromJson(
						response.body().toString());
				callback.onResponseResult(response, owmOneCallResponse, response.body().toString());
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

	/**
	 * current conditions
	 */
	public static Call<JsonElement> getCurrentConditions(OwmCurrentWeatherParameter owmCurrentWeatherParameter,
	                                                     JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS);

		Call<JsonElement> call = queries.getOwmCurrentConditions(owmCurrentWeatherParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				OwmCurrentConditionsResponse owmCurrentConditionsResponse =
						OpenWeatherMapResponseProcessor.getOwmCurrentConditionsResponseFromJson(response.body().toString());
				callback.onResponseResult(response, owmCurrentConditionsResponse, response.body().toString());
				Log.e(RetrofitClient.LOG_TAG, "own current conditions 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "own current conditions 실패");
			}
		});

		return call;
	}

	/**
	 * hourly forecast
	 */
	public static Call<JsonElement> getHourlyForecast(OwmHourlyForecastParameter owmHourlyForecastParameter,
	                                                  JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST);

		Call<JsonElement> call = queries.getOwmHourlyForecast(owmHourlyForecastParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				OwmHourlyForecastResponse owmHourlyForecastResponse =
						OpenWeatherMapResponseProcessor.getOwmHourlyForecastResponseFromJson(response.body().toString());
				callback.onResponseResult(response, owmHourlyForecastResponse, response.body().toString());
				Log.e(RetrofitClient.LOG_TAG, "own hourly forecast 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "own hourly forecast 실패");
			}
		});

		return call;
	}

	/**
	 * daily forecast
	 */
	public static Call<JsonElement> getDailyForecast(OwmDailyForecastParameter owmDailyForecastParameter,
	                                                 JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

		Call<JsonElement> call = queries.getOwmDailyForecast(owmDailyForecastParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				OwmDailyForecastResponse owmDailyForecastResponse =
						OpenWeatherMapResponseProcessor.getOwmDailyForecastResponseFromJson(response.body().toString());
				callback.onResponseResult(response, owmDailyForecastResponse, response.body().toString());
				Log.e(RetrofitClient.LOG_TAG, "own daily forecast 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "own daily forecast 실패");
			}
		});

		return call;
	}

	public static void requestWeatherDataOneCall(Context context, Double latitude, Double longitude,
	                                             RequestOwmOneCall requestOwmOneCall, WeatherRestApiDownloader weatherRestApiDownloader) {
		OpenWeatherMapResponseProcessor.init(context);

		OneCallParameter oneCallParameter = new OneCallParameter();
		Set<OneCallParameter.OneCallApis> excludeOneCallApis = requestOwmOneCall.getExcludeApis();
		oneCallParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString()).setOneCallApis(excludeOneCallApis);

		Call<JsonElement> oneCallCall = getOneCall(oneCallParameter, new JsonDownloader() {
			@Override
			public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
				weatherRestApiDownloader.processResult(WeatherProviderType.OWM_ONECALL, oneCallParameter,
						RetrofitClient.ServiceType.OWM_ONE_CALL, response, responseObj, responseText);
			}

			@Override
			public void onResponseResult(Throwable t) {
				weatherRestApiDownloader.processResult(WeatherProviderType.OWM_ONECALL, oneCallParameter,
						RetrofitClient.ServiceType.OWM_ONE_CALL, t);
			}

		});
		weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.OWM_ONE_CALL, oneCallCall);

	}

	public static void requestWeatherDataIndividual(Context context, Double latitude, Double longitude,
	                                                RequestOwmIndividual requestOwmIndividual, WeatherRestApiDownloader weatherRestApiDownloader) {
		Set<RetrofitClient.ServiceType> serviceTypeSet = requestOwmIndividual.getRequestServiceTypes();

		if (serviceTypeSet.contains(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS)) {
			OwmCurrentWeatherParameter owmCurrentWeatherParameter = new OwmCurrentWeatherParameter();
			owmCurrentWeatherParameter.setLatitude(latitude.toString());
			owmCurrentWeatherParameter.setLongitude(longitude.toString());

			Call<JsonElement> currentConditionsCall = getCurrentConditions(owmCurrentWeatherParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					weatherRestApiDownloader.processResult(WeatherProviderType.OWM_INDIVIDUAL, owmCurrentWeatherParameter,
							RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS, response, responseObj, responseText);
				}

				@Override
				public void onResponseResult(Throwable t) {
					weatherRestApiDownloader.processResult(WeatherProviderType.OWM_INDIVIDUAL, owmCurrentWeatherParameter,
							RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS, t);
				}

			});
			weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS, currentConditionsCall);
		}
		if (serviceTypeSet.contains(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST)) {
			OwmHourlyForecastParameter owmHourlyForecastParameter = new OwmHourlyForecastParameter();
			owmHourlyForecastParameter.setLatitude(latitude.toString());
			owmHourlyForecastParameter.setLongitude(longitude.toString());

			Call<JsonElement> hourlyForecastCall = getHourlyForecast(owmHourlyForecastParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					weatherRestApiDownloader.processResult(WeatherProviderType.OWM_INDIVIDUAL, owmHourlyForecastParameter,
							RetrofitClient.ServiceType.OWM_HOURLY_FORECAST, response, responseObj, responseText);
				}

				@Override
				public void onResponseResult(Throwable t) {
					weatherRestApiDownloader.processResult(WeatherProviderType.OWM_INDIVIDUAL, owmHourlyForecastParameter,
							RetrofitClient.ServiceType.OWM_HOURLY_FORECAST, t);
				}

			});
			weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST, hourlyForecastCall);
		}
		if (serviceTypeSet.contains(RetrofitClient.ServiceType.OWM_DAILY_FORECAST)) {
			OwmDailyForecastParameter owmDailyForecastParameter = new OwmDailyForecastParameter();
			owmDailyForecastParameter.setLatitude(latitude.toString());
			owmDailyForecastParameter.setLongitude(longitude.toString());

			Call<JsonElement> dailyForecastCall = getDailyForecast(owmDailyForecastParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					weatherRestApiDownloader.processResult(WeatherProviderType.OWM_INDIVIDUAL, owmDailyForecastParameter,
							RetrofitClient.ServiceType.OWM_DAILY_FORECAST, response, responseObj, responseText);
				}

				@Override
				public void onResponseResult(Throwable t) {
					weatherRestApiDownloader.processResult(WeatherProviderType.OWM_INDIVIDUAL, owmDailyForecastParameter,
							RetrofitClient.ServiceType.OWM_DAILY_FORECAST, t);
				}

			});
			weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.OWM_DAILY_FORECAST, dailyForecastCall);
		}
	}
}
