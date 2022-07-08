package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestMet;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.Queries;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.metnorway.LocationForecastParameter;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.LocationForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.MetNorwayResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;

import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MetNorwayProcessing {
	/**
	 * Location Forecast
	 */
	public static Call<JsonElement> getLocationForecast(LocationForecastParameter locationForecastParameter,
	                                                    JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

		Call<JsonElement> call = queries.getMetNorwayLocationForecast(locationForecastParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				LocationForecastResponse locationForecastResponse = MetNorwayResponseProcessor.getLocationForecastResponseObjFromJson(
						response.body().toString());
				callback.onResponseResult(response, locationForecastResponse, response.body().toString());
				Log.e(RetrofitClient.LOG_TAG, "norway met 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "norway met 실패");
			}
		});
		return call;
	}

	public static void getMetNorwayForecasts(String latitude, String longitude, RequestMet requestMet,
	                                         MultipleRestApiDownloader multipleRestApiDownloader) {
		LocationForecastParameter locationForecastParameter = new LocationForecastParameter();
		locationForecastParameter.setLatitude(latitude).setLongitude(longitude);
		Call<JsonElement> locationForecastCall = getLocationForecast(locationForecastParameter, new JsonDownloader() {
			@Override
			public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
				multipleRestApiDownloader.processResult(WeatherProviderType.MET_NORWAY, locationForecastParameter,
						RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, response, responseObj, responseText);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleRestApiDownloader.processResult(WeatherProviderType.MET_NORWAY, locationForecastParameter,
						RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, t);
			}

		});

		multipleRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, locationForecastCall);
	}


}
