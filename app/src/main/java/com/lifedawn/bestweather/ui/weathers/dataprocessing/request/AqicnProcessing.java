package com.lifedawn.bestweather.ui.weathers.dataprocessing.request;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery;
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.aqicn.AqicnParameter;
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.AqicnResponseProcessor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AqicnProcessing {
	public static Call<JsonElement> getLocalizedFeed(AqicnParameter aqicnParameter, JsonDownloader callback) {
		RestfulApiQuery restfulApiQuery = RetrofitClient.getApiService(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

		Call<JsonElement> call = restfulApiQuery.getAqiCnGeolocalizedFeed(aqicnParameter.getLatitude(), aqicnParameter.getLongitude(),
				aqicnParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (response.body() == null) {
					onFailure(call, new Exception("aqicn response failed"));
				} else {
					try {
						AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse =
								AqicnResponseProcessor.getAirQualityObjFromJson(response.body().toString());
						callback.onResponseResult(response, aqiCnGeolocalizedFeedResponse, response.body().toString());
						Log.e(RetrofitClient.LOG_TAG, "aqicn geolocalizedfeed 성공");
					} catch (Exception e) {
						onFailure(call, new Exception("aqicn response failed"));
					}
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "aqicn response failed");
			}
		});

		return call;
	}

	public static void getAirQuality(Double latitude, Double longitude, MultipleWeatherRestApiCallback multipleWeatherRestApiCallback, Context context) {
		AqicnResponseProcessor.init(context);

		AqicnParameter aqicnParameter = new AqicnParameter();
		aqicnParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString());

		Call<JsonElement> localizedFeedCall = getLocalizedFeed(aqicnParameter, new JsonDownloader() {
			@Override
			public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
				multipleWeatherRestApiCallback.processResult(WeatherProviderType.AQICN, aqicnParameter,
						RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED, response, responseObj, responseText);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleWeatherRestApiCallback.processResult(WeatherProviderType.AQICN, aqicnParameter,
						RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED, t);
			}
		});

		multipleWeatherRestApiCallback.getCallMap().put(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED, localizedFeedCall);
	}

}
