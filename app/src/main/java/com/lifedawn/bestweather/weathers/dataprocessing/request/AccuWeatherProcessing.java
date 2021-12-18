package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.CurrentConditionsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.FiveDaysOfDailyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.GeoPositionSearchParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.TwelveHoursOfHourlyForecastsParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.geopositionsearch.GeoPositionResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;

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
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

		Call<JsonElement> call = querys.getCurrentConditions(currentConditionsParameter.getLocationKey(),
				currentConditionsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (successfulResponse(response)) {
					CurrentConditionsResponse currentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
							response.body());

					callback.onResponseResult(response, currentConditionsResponse, response.body().toString());
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
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

		Call<JsonElement> call = querys.get5Days(fiveDaysOfDailyForecastsParameter.getLocationKey(),
				fiveDaysOfDailyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (successfulResponse(response)) {
					FiveDaysOfDailyForecastsResponse dailyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
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
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_12_HOURLY);

		Call<JsonElement> call = querys.get12Hourly(twelveHoursOfHourlyForecastsParameter.getLocationKey(),
				twelveHoursOfHourlyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (successfulResponse(response)) {
					TwelveHoursOfHourlyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(response.body());
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
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);

		Call<JsonElement> call = querys.geoPositionSearch(geoPositionSearchParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (successfulResponse(response)) {
					GeoPositionResponse geoPositionResponse = AccuWeatherResponseProcessor.getGeoPositionObjFromJson(
							response.body().toString());

					PreferenceManager.getDefaultSharedPreferences(context).edit().putString(
							geoPositionSearchParameter.getLatitude() + geoPositionSearchParameter.getLongitude(),
							geoPositionResponse.getKey()).apply();

					callback.onResponseResult(response, geoPositionResponse, response.body().toString());
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
	                                      MultipleJsonDownloader multipleJsonDownloader) {
		final String locationKey = getLocationKey(context, latitude, longitude);

		if (locationKey.isEmpty()) {
			//locationKey 요청 후 처리
			GeoPositionSearchParameter geoPositionSearchParameter = new GeoPositionSearchParameter();
			geoPositionSearchParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString());

			Call<JsonElement> geoPositionCall = getGeoPositionSearch(context, geoPositionSearchParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, geoPositionSearchParameter,
							RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, response, responseObj, responseText);
					GeoPositionResponse geoPositionResponse = AccuWeatherResponseProcessor.getGeoPositionObjFromJson(
							response.body().toString());
					requestWeatherDataIfHasLocationKey(requestAccu, geoPositionResponse.getKey(), multipleJsonDownloader);
				}

				@Override
				public void onResponseResult(Throwable t) {
					Set<RetrofitClient.ServiceType> requestTypeSet = requestAccu.getRequestServiceTypes();

					multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, geoPositionSearchParameter,
							RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, t);

					if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS)) {
						multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, null,
								RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
					}
					if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_12_HOURLY)) {
						multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, null,
								RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
					}
					if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY)) {
						multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, null,
								RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
					}
				}
			});

			multipleJsonDownloader.getCallMap().put(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, geoPositionCall);
		} else {
			requestWeatherDataIfHasLocationKey(requestAccu, locationKey, multipleJsonDownloader);
		}
	}

	private static void requestWeatherDataIfHasLocationKey(RequestAccu requestAccu, String locationKey,
	                                                       MultipleJsonDownloader multipleJsonDownloader) {
		Set<RetrofitClient.ServiceType> requestTypeSet = requestAccu.getRequestServiceTypes();

		if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS)) {
			CurrentConditionsParameter currentConditionsParameter = new CurrentConditionsParameter();
			currentConditionsParameter.setLocationKey(locationKey);

			Call<JsonElement> currentConditionsCall = getCurrentConditions(currentConditionsParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, currentConditionsParameter,
							RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, response, responseObj, responseText);
				}

				@Override
				public void onResponseResult(Throwable t) {
					multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, currentConditionsParameter,
							RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
				}

			});

			multipleJsonDownloader.getCallMap().put(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, currentConditionsCall);
		}
		if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_12_HOURLY)) {
			TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter = new TwelveHoursOfHourlyForecastsParameter();
			twelveHoursOfHourlyForecastsParameter.setLocationKey(locationKey);

			Call<JsonElement> hourlyForecastCall = get12HoursOfHourlyForecasts(twelveHoursOfHourlyForecastsParameter,
					new JsonDownloader() {
						@Override
						public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
							multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, twelveHoursOfHourlyForecastsParameter,
									RetrofitClient.ServiceType.ACCU_12_HOURLY, response, responseObj, responseText);

						}

						@Override
						public void onResponseResult(Throwable t) {
							multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, twelveHoursOfHourlyForecastsParameter,
									RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
						}


					});
			multipleJsonDownloader.getCallMap().put(RetrofitClient.ServiceType.ACCU_12_HOURLY, hourlyForecastCall);

		}
		if (requestTypeSet.contains(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY)) {
			FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter = new FiveDaysOfDailyForecastsParameter();
			fiveDaysOfDailyForecastsParameter.setLocationKey(locationKey);

			Call<JsonElement> dailyForecastCall = get5DaysOfDailyForecasts(fiveDaysOfDailyForecastsParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
					multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, fiveDaysOfDailyForecastsParameter,
							RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, response, responseObj, responseText);
				}

				@Override
				public void onResponseResult(Throwable t) {
					multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER, fiveDaysOfDailyForecastsParameter,
							RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
				}

			});
			multipleJsonDownloader.getCallMap().put(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, dailyForecastCall);

		}
	}


}
