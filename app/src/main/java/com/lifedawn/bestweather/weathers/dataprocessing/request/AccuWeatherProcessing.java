package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.CurrentConditionsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.FiveDaysOfDailyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.GeoPositionSearchParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.TwelveHoursOfHourlyForecastsParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.geopositionsearch.GeoPositionResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccuWeatherProcessing {

	public static boolean checkResponse(Response<JsonElement> response) {
		if (response.body() == null) {
			return false;
		} else if (response.isSuccessful()) {
			return true;
		} else {
			return true;
		}
	}


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
				if (checkResponse(response)) {
					callback.onResponseResult(response);
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
	                                                         JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

		Call<JsonElement> call = querys.get5Days(fiveDaysOfDailyForecastsParameter.getLocationKey(),
				fiveDaysOfDailyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (checkResponse(response)) {
					callback.onResponseResult(response);
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
	                                                            JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_12_HOURLY);

		Call<JsonElement> call = querys.get12Hourly(twelveHoursOfHourlyForecastsParameter.getLocationKey(),
				twelveHoursOfHourlyForecastsParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (checkResponse(response)) {
					callback.onResponseResult(response);
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

	public static String getLocationKey(Context context, String latitude, String longitude) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(latitude + longitude, "");
	}


	/**
	 * GeoPosition Search
	 */
	public static Call<JsonElement> getGeoPositionSearch(Context context, GeoPositionSearchParameter geoPositionSearchParameter,
	                                                     JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);

		Call<JsonElement> call = querys.geoPositionSearch(geoPositionSearchParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (checkResponse(response)) {
					GeoPositionResponse geoPositionResponse =
							AccuWeatherResponseProcessor.getGeoPositionObjFromJson(response.body().toString());

					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
					sharedPreferences.edit().putString(geoPositionSearchParameter.getLatitude() + geoPositionSearchParameter.getLongitude(),
							geoPositionResponse.getKey()).apply();

					callback.onResponseResult(response);
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


	public static void getAccuWeatherForecasts(Context context, String latitude, String longitude,
	                                           MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		GeoPositionSearchParameter geoPositionSearchParameter = new GeoPositionSearchParameter();
		CurrentConditionsParameter currentConditionsParameter = new CurrentConditionsParameter();
		FiveDaysOfDailyForecastsParameter fiveDaysOfDailyForecastsParameter = new FiveDaysOfDailyForecastsParameter();
		TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter = new TwelveHoursOfHourlyForecastsParameter();

		geoPositionSearchParameter.setLatitude(latitude).setLongitude(longitude);
		final String locationKey = getLocationKey(context, latitude, longitude);

		if (locationKey.isEmpty()) {
			Call<JsonElement> geoPositionSearchCall = getGeoPositionSearch(context, geoPositionSearchParameter,
					new JsonDownloader<JsonElement>() {

						@Override
						public void onResponseResult(Response<? extends JsonElement> response) {
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, response);

							GeoPositionResponse geoPositionResponse =
									AccuWeatherResponseProcessor.getGeoPositionObjFromJson(response.body().toString());

							final String locationKey = geoPositionResponse.getKey();
							currentConditionsParameter.setLocationKey(locationKey);
							fiveDaysOfDailyForecastsParameter.setLocationKey(locationKey);
							twelveHoursOfHourlyForecastsParameter.setLocationKey(locationKey);

							Call<JsonElement> currentConditionsCall = getCurrentConditions(currentConditionsParameter,
									new JsonDownloader<JsonElement>() {
										@Override
										public void onResponseResult(Response<? extends JsonElement> response) {
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, response);
										}

										@Override
										public void onResponseResult(Throwable t) {
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
										}


									});

							Call<JsonElement> fiveDaysOfDailyForecastsCall = get5DaysOfDailyForecasts(fiveDaysOfDailyForecastsParameter,
									new JsonDownloader<JsonElement>() {
										@Override
										public void onResponseResult(Response<? extends JsonElement> response) {
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, response);
										}

										@Override
										public void onResponseResult(Throwable t) {
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_12_HOURLY, t);

										}

									});

							Call<JsonElement> twelveHoursOfHourlyForecastsCall = get12HoursOfHourlyForecasts(twelveHoursOfHourlyForecastsParameter,
									new JsonDownloader<JsonElement>() {
										@Override
										public void onResponseResult(Response<? extends JsonElement> response) {
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_12_HOURLY, response);

										}

										@Override
										public void onResponseResult(Throwable t) {
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
										}


									});

						}

						@Override
						public void onResponseResult(Throwable t) {
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
						}


					});

		} else {
			currentConditionsParameter.setLocationKey(locationKey);
			fiveDaysOfDailyForecastsParameter.setLocationKey(locationKey);
			twelveHoursOfHourlyForecastsParameter.setLocationKey(locationKey);

			Call<JsonElement> currentConditionsCall = getCurrentConditions(currentConditionsParameter,
					new JsonDownloader<JsonElement>() {
						@Override
						public void onResponseResult(Response<? extends JsonElement> response) {
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, response);
						}

						@Override
						public void onResponseResult(Throwable t) {
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
						}

					});

			Call<JsonElement> fiveDaysOfDailyForecastsCall = get5DaysOfDailyForecasts(fiveDaysOfDailyForecastsParameter,
					new JsonDownloader<JsonElement>() {
						@Override
						public void onResponseResult(Response<? extends JsonElement> response) {
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, response);
						}

						@Override
						public void onResponseResult(Throwable t) {
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);

						}

					});

			Call<JsonElement> twelveHoursOfHourlyForecastsCall = get12HoursOfHourlyForecasts(twelveHoursOfHourlyForecastsParameter,
					new JsonDownloader<JsonElement>() {
						@Override
						public void onResponseResult(Response<? extends JsonElement> response) {
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_12_HOURLY, response);
						}

						@Override
						public void onResponseResult(Throwable t) {
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
						}


					});
		}


	}

}
