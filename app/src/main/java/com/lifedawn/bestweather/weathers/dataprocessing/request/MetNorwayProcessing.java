package com.lifedawn.bestweather.weathers.dataprocessing.request;

import com.google.gson.JsonElement;
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
	                                                   JsonDownloader<JsonElement> callback) {
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
		Call<JsonElement> locationForecastCall = getLocationForecast(locationForecastParameter, new JsonDownloader<JsonElement>() {
			@Override
			public void onResponseResult(Response<? extends JsonElement> response) {
				multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.MET_NORWAY,
						RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, response);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.MET_NORWAY,
						RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, t);
			}


		});
	}
}
