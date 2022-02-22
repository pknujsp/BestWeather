package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.util.Log;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.Queries;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.aqicn.AqicnParameter;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AqicnProcessing {
	public static Call<JsonElement> getLocalizedFeed(AqicnParameter aqicnParameter, JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

		Call<JsonElement> call = queries.getAqiCnGeolocalizedFeed(aqicnParameter.getLatitude(), aqicnParameter.getLongitude(),
				aqicnParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				AqiCnGeolocalizedFeedResponse airQualityResponse =
						AqicnResponseProcessor.getAirQualityObjFromJson(response.body().toString());
				callback.onResponseResult(response, airQualityResponse, response.body().toString());
				Log.e(RetrofitClient.LOG_TAG, "aqicn geolocalizedfeed 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "aqicn geolocalizedfeed 실패");
			}
		});
		return call;
	}

	public static void getAirQuality(Double latitude, Double longitude, MultipleRestApiDownloader multipleRestApiDownloader) {
		AqicnParameter aqicnParameter = new AqicnParameter();
		aqicnParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString());

		Call<JsonElement> localizedFeedCall = getLocalizedFeed(aqicnParameter, new JsonDownloader() {
			@Override
			public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
				multipleRestApiDownloader.processResult(WeatherProviderType.AQICN, aqicnParameter,
						RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED, response, responseObj, responseText);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleRestApiDownloader.processResult(WeatherProviderType.AQICN, aqicnParameter,
						RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED, t);
			}

		});
		multipleRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED, localizedFeedCall);

	}
}
