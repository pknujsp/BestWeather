package com.lifedawn.bestweather.data.remote.weather.dataprocessing.request;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestMet;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery;
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.metnorway.LocationForecastParameter;
import com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.LocationForecastResponse;
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback;
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.MetNorwayResponseProcessor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MetNorwayProcessing {
	/**
	 * Location Forecast
	 */
	public static Call<JsonElement> getLocationForecast(LocationForecastParameter locationForecastParameter,
	                                                    JsonDownloader callback) {
		RestfulApiQuery restfulApiQuery = RetrofitClient.getApiService(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

		Call<JsonElement> call = restfulApiQuery.getMetNorwayLocationForecast(locationForecastParameter.getMap());
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
	                                         MultipleWeatherRestApiCallback multipleWeatherRestApiCallback, Context context) {
		MetNorwayResponseProcessor.init(context);

		LocationForecastParameter locationForecastParameter = new LocationForecastParameter();
		locationForecastParameter.setLatitude(latitude).setLongitude(longitude);
		Call<JsonElement> locationForecastCall = getLocationForecast(locationForecastParameter, new JsonDownloader() {
			@Override
			public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
				multipleWeatherRestApiCallback.processResult(WeatherProviderType.MET_NORWAY, locationForecastParameter,
						RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, response, responseObj, responseText);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleWeatherRestApiCallback.processResult(WeatherProviderType.MET_NORWAY, locationForecastParameter,
						RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, t);
			}

		});

		multipleWeatherRestApiCallback.getCallMap().put(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST, locationForecastCall);
	}


}
