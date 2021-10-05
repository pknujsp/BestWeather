package com.lifedawn.bestweather.weathers.dataprocessing;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.lifedawn.bestweather.retrofit.util.RetrofitCallListManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccuWeatherProcessing {
	/**
	 * Current Conditions
	 */
	public static Call<JsonElement> getCurrentConditions(CurrentConditionsParameter currentConditionsParameter,
	                                                     JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

		Call<JsonElement> call = querys.getCurrentConditions(currentConditionsParameter.getLocationKey(),
				currentConditionsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.processResult(response);
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}

	/**
	 * 5 Days Of Daily Forecast
	 */
	public static Call<JsonElement> get5DaysOfDailyForecasts(FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter,
	                                                         JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

		Call<JsonElement> call = querys.get5Days(fiveDaysOfDailyForecastsParameter.getLocationKey(),
				fiveDaysOfDailyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.processResult(response);
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.processResult(t);
			}
		});

		return call;
	}

	/**
	 * 12 Hours of Hourly Forecasts
	 */
	public static Call<JsonElement> get12HoursOfHourlyForecasts(TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter,
	                                                            JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_12_HOURLY);

		Call<JsonElement> call = querys.get12Hourly(twelveHoursOfHourlyForecastsParameter.getLocationKey(),
				twelveHoursOfHourlyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.processResult(response);
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}


	/**
	 * GeoPosition Search
	 */
	public static Call<JsonObject> getGeoPositionSearch(GeoPositionSearchParameter geoPositionSearchParameter,
	                                                    JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);

		Call<JsonObject> call = querys.geoPositionSearch(geoPositionSearchParameter.getMap());
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


	public static void getAccuWeatherForecasts(String latitude, String longitude, @Nullable String locationKey,
	                                           MultipleJsonDownloader multipleJsonDownloader) {
		GeoPositionSearchParameter geoPositionSearchParameter = new GeoPositionSearchParameter();
		CurrentConditionsParameter currentConditionsParameter = new CurrentConditionsParameter();
		FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter = new FiveDaysOfDailyForecastsParameter();
		TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter = new TwelveHoursOfHourlyForecastsParameter();

		RetrofitCallListManager.CallObj newCallObj = RetrofitCallListManager.newCalls();

		if (locationKey == null) {
			geoPositionSearchParameter.setLatitude(latitude).setLongitude(longitude);
			Call<JsonObject> geoPositionSearchCall = getGeoPositionSearch(geoPositionSearchParameter, new JsonDownloader<JsonObject>() {


				@Override
				public void onResponseSuccessful(Response<? extends JsonObject> response) {
					Gson gson = new Gson();
					GeoPositionResponse geoPositionResponse = gson.fromJson(response.body().toString(), GeoPositionResponse.class);

					final String locationKey = geoPositionResponse.getKey();
					currentConditionsParameter.setLocationKey(locationKey);
					fiveDaysOfDailyForecastsParameter.setLocationKey(locationKey);
					twelveHoursOfHourlyForecastsParameter.setLocationKey(locationKey);

					Call<JsonElement> currentConditionsCall = getCurrentConditions(currentConditionsParameter, new JsonDownloader<JsonElement>() {
						@Override
						public void onResponseSuccessful(Response<? extends JsonElement> response) {

						}

						@Override
						public void onResponseFailed(Exception e) {

						}

						@Override
						public void processResult(Response<? extends JsonElement> response) {
							if (response.body() != null) {
								onResponseSuccessful(response);
							} else {
								onResponseFailed(new Exception(response.toString()));
							}
						}
					});

					Call<JsonElement> fiveDaysOfDailyForecastsCall = get5DaysOfDailyForecasts(fiveDaysOfDailyForecastsParameter,
							new JsonDownloader<JsonElement>() {
								@Override
								public void onResponseSuccessful(Response<? extends JsonElement> response) {

								}

								@Override
								public void onResponseFailed(Exception e) {

								}

								@Override
								public void processResult(Response<? extends JsonElement> response) {
									if (response.body() != null) {
										onResponseSuccessful(response);
									} else {
										onResponseFailed(new Exception(response.toString()));
									}
								}
							});

					Call<JsonElement> twelveHoursOfHourlyForecastsCall = get12HoursOfHourlyForecasts(twelveHoursOfHourlyForecastsParameter,
							new JsonDownloader<JsonElement>() {
								@Override
								public void onResponseSuccessful(Response<? extends JsonElement> response) {

								}

								@Override
								public void onResponseFailed(Exception e) {

								}

								@Override
								public void processResult(Response<? extends JsonElement> response) {
									if (response.body() != null) {
										onResponseSuccessful(response);
									} else {
										onResponseFailed(new Exception(response.toString()));
									}
								}
							});

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

			newCallObj.add(geoPositionSearchCall);
		}
	}

}
