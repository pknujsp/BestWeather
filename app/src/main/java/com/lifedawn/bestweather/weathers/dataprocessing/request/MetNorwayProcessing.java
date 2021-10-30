package com.lifedawn.bestweather.weathers.dataprocessing.request;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.metnorway.LocationForecastParameter;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MetNorwayProcessing {
	/**
	 * Location Forecast
	 */
	public static Call<JsonElement> getLocationForecast(LocationForecastParameter locationForecastParameter,
	                                                    JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

		Call<JsonElement> call = querys.getLocationForecast(locationForecastParameter.getMap());
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

	public static void getMetNorwayForecasts(String latitude, String longitude, MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		LocationForecastParameter locationForecastParameter = new LocationForecastParameter();
		locationForecastParameter.setLatitude(latitude).setLongitude(longitude);
		Call<JsonElement> locationForecastCall = getLocationForecast(locationForecastParameter, new JsonDownloader() {
			@Override
			public void onResponseResult(Response<JsonElement> response) {
				multipleJsonDownloader.processResult(WeatherSourceType.MET_NORWAY, locationForecastParameter,
						RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, response);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleJsonDownloader.processResult(WeatherSourceType.MET_NORWAY, locationForecastParameter,
						RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, t);
			}


		});
	}


}
