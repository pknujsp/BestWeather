package com.lifedawn.bestweather.weathers.dataprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.CurrentConditionsParameter;
import com.lifedawn.bestweather.retrofit.parameters.metnorway.LocationForecastParameter;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.LocationForecastResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MetNorwayProcessing {
	/**
	 * Location Forecast
	 */
	public static Call<JsonObject> getLocationForecast(LocationForecastParameter locationForecastParameter,
	                                                   JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

		Call<JsonObject> call = querys.getLocationForecast(locationForecastParameter.getMap());
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

	public static void getMetNorwayForecasts(String latitude, String longitude, MultipleJsonDownloader<JsonObject> multipleJsonDownloader) {
		LocationForecastParameter locationForecastParameter = new LocationForecastParameter();
		locationForecastParameter.setLatitude(latitude).setLongitude(longitude);
		Call<JsonObject> locationForecastCall = getLocationForecast(locationForecastParameter, new JsonDownloader<JsonObject>() {
			@Override
			public void onResponseSuccessful(Response<? extends JsonObject> response) {
				Gson gson = new Gson();
				LocationForecastResponse locationForecastResponse = gson.fromJson(response.body().toString(), LocationForecastResponse.class);
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
