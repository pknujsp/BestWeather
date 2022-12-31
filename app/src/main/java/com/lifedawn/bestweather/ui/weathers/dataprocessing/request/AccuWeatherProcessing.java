package com.lifedawn.bestweather.ui.weathers.dataprocessing.request;

import android.content.Context;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery;
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.accuweather.CurrentConditionsParameter;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.accuweather.FiveDaysOfDailyForecastsParameter;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.accuweather.GeoPositionSearchParameter;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.accuweather.TwelveHoursOfHourlyForecastsParameter;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.currentconditions.AccuCurrentConditionsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.dailyforecasts.AccuDailyForecastsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.geopositionsearch.AccuGeoPositionResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.hourlyforecasts.AccuHourlyForecastsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.callback.WeatherRestApiDownloader;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.AccuWeatherResponseProcessor;

import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccuWeatherProcessing {

	public static boolean successfulResponse(Response<JsonElement> response) {
		if (response.body() == null) {
			return false;
		} else {
			return response.errorBody() == null;
		}
	}


	/**
	 * Current Conditions
	 */
	public static Call<JsonElement> getCurrentConditions(CurrentConditionsParameter currentConditionsParameter, JsonDownloader callback) {
		RestfulApiQuery restfulApiQuery = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

		Call<JsonElement> call = restfulApiQuery.getAccuCurrentConditions(currentConditionsParameter.getLocationKey(),
				currentConditionsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (successfulResponse(response)) {
					AccuCurrentConditionsResponse accuCurrentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
							response.body());

					callback.onResponseResult(response, accuCurrentConditionsResponse, response.body().toString());
					Log.e(RetrofitClient.LOG_TAG, "accu weather current conditions 성공");

				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "accu weather current conditions 실패");
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "accu weather current conditions 실패");
			}
		});
		return call;
	}

	/**
	 * 5 Days Of Daily Forecast
	 */
	public static Call<JsonElement> get5DaysOfDailyForecasts(FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter,
	                                                         JsonDownloader callback) {
		RestfulApiQuery restfulApiQuery = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);

		Call<JsonElement> call = restfulApiQuery.getAccuDailyForecast(fiveDaysOfDailyForecastsParameter.getLocationKey(),
				fiveDaysOfDailyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (successfulResponse(response)) {
					AccuDailyForecastsResponse dailyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
							response.body().toString());
					callback.onResponseResult(response, dailyForecastsResponse, response.body().toString());
					Log.e(RetrofitClient.LOG_TAG, "accu weather daily forecast 성공");

				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "accu weather daily forecast 실패");

				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "accu weather daily forecast 실패");
			}
		});

		return call;
	}

	/**
	 * 12 Hours of Hourly Forecasts
	 */
	public static Call<JsonElement> get12HoursOfHourlyForecasts(TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter,
	                                                            JsonDownloader callback) {
		RestfulApiQuery restfulApiQuery = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST);

		Call<JsonElement> call = restfulApiQuery.getAccuHourlyForecast(twelveHoursOfHourlyForecastsParameter.getLocationKey(),
				twelveHoursOfHourlyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (successfulResponse(response)) {
					AccuHourlyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(response.body());
					callback.onResponseResult(response, hourlyForecastsResponse, response.body().toString());
					Log.e(RetrofitClient.LOG_TAG, "accu weather hourly forecast 성공");

				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "accu weather hourly forecast 실패");
				}

			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "accu weather hourly forecast 실패");
			}
		});
		return call;
	}

	public static String getLocationKey(Context context, Double latitude, Double longitude) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(latitude.toString() + longitude.toString(), "");
	}


	/**
	 * GeoPosition Search
	 */
	public static Call<JsonElement> getGeoPositionSearch(Context context, GeoPositionSearchParameter geoPositionSearchParameter,
	                                                     JsonDownloader callback) {
		RestfulApiQuery restfulApiQuery = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);

		Call<JsonElement> call = restfulApiQuery.geoAccuPositionSearch(geoPositionSearchParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (successfulResponse(response)) {
					AccuGeoPositionResponse accuGeoPositionResponse = AccuWeatherResponseProcessor.getGeoPositionObjFromJson(
							response.body().toString());

					PreferenceManager.getDefaultSharedPreferences(context).edit().putString(
							geoPositionSearchParameter.getLatitude() + geoPositionSearchParameter.getLongitude(),
							accuGeoPositionResponse.getKey()).apply();

					callback.onResponseResult(response, accuGeoPositionResponse, response.body().toString());
					Log.e(RetrofitClient.LOG_TAG, "accu weather geoposition search 성공");
				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "accu weather geoposition search 실패");
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "accu weather geoposition search 실패");
			}
		});

		return call;
	}

	public static void requestWeatherData(Context context, Double latitude, Double longitude, RequestAccu requestAccu,
	                                      WeatherRestApiDownloader weatherRestApiDownloader) {
		final String locationKey = getLocationKey(context, latitude, longitude);

		if (locationKey.isEmpty()) {
			//locationKey 요청 후 처리
			GeoPositionSearchParameter geoPositionSearchParameter = new GeoPositionSearchParameter();
			geoPositionSearchParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString());

			Call<JsonElement> geoPositionCall = getGeoPositionSearch(context, geoPositionSearchParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, geoPositionSearchParameter,
							RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, response, responseObj, responseText);
					AccuGeoPositionResponse accuGeoPositionResponse = AccuWeatherResponseProcessor.getGeoPositionObjFromJson(
							response.body().toString());
					requestWeatherDataIfHasLocationKey(requestAccu, accuGeoPositionResponse.getKey(), weatherRestApiDownloader);
				}

				@Override
				public void onResponseResult(Throwable t) {
					Set<RetrofitClient.ServiceType> requestTypeSet = requestAccu.getRequestServiceTypes();

					weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, geoPositionSearchParameter,
							RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, t);

					if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS)) {
						weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, null,
								RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
					}
					if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST)) {
						weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, null,
								RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST, t);
					}
					if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST)) {
						weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, null,
								RetrofitClient.ServiceType.ACCU_DAILY_FORECAST, t);
					}
				}
			});

			weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, geoPositionCall);
		} else {
			requestWeatherDataIfHasLocationKey(requestAccu, locationKey, weatherRestApiDownloader);
		}
	}

	private static void requestWeatherDataIfHasLocationKey(RequestAccu requestAccu, String locationKey,
	                                                       WeatherRestApiDownloader weatherRestApiDownloader) {
		Set<RetrofitClient.ServiceType> requestTypeSet = requestAccu.getRequestServiceTypes();

		if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS)) {
			CurrentConditionsParameter currentConditionsParameter = new CurrentConditionsParameter();
			currentConditionsParameter.setLocationKey(locationKey);

			Call<JsonElement> currentConditionsCall = getCurrentConditions(currentConditionsParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, currentConditionsParameter,
							RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, response, responseObj, responseText);
				}

				@Override
				public void onResponseResult(Throwable t) {
					weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, currentConditionsParameter,
							RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
				}

			});

			weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, currentConditionsCall);
		}
		if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST)) {
			TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter = new TwelveHoursOfHourlyForecastsParameter();
			twelveHoursOfHourlyForecastsParameter.setLocationKey(locationKey);

			Call<JsonElement> hourlyForecastCall = get12HoursOfHourlyForecasts(twelveHoursOfHourlyForecastsParameter,
					new JsonDownloader() {
						@Override
						public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
							weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, twelveHoursOfHourlyForecastsParameter,
									RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST, response, responseObj, responseText);

						}

						@Override
						public void onResponseResult(Throwable t) {
							weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, twelveHoursOfHourlyForecastsParameter,
									RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST, t);
						}


					});
			weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST, hourlyForecastCall);

		}
		if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST)) {
			FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter = new FiveDaysOfDailyForecastsParameter();
			fiveDaysOfDailyForecastsParameter.setLocationKey(locationKey);

			Call<JsonElement> dailyForecastCall = get5DaysOfDailyForecasts(fiveDaysOfDailyForecastsParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, fiveDaysOfDailyForecastsParameter,
							RetrofitClient.ServiceType.ACCU_DAILY_FORECAST, response, responseObj, responseText);
				}

				@Override
				public void onResponseResult(Throwable t) {
					weatherRestApiDownloader.processResult(WeatherProviderType.ACCU_WEATHER, fiveDaysOfDailyForecastsParameter,
							RetrofitClient.ServiceType.ACCU_DAILY_FORECAST, t);
				}

			});
			weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST, dailyForecastCall);

		}
	}


}