package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.util.Log;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.aqicn.AqicnParameter;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AqicnProcessing {
	public static Call<JsonElement> getLocalizedFeed(AqicnParameter aqicnParameter, JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

		Call<JsonElement> call = querys.getGeolocalizedFeed(aqicnParameter.getLatitude(), aqicnParameter.getLongitude(),
				aqicnParameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
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

	public static void getAirQuality(Double latitude, Double longitude, MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		AqicnParameter aqicnParameter = new AqicnParameter();
		aqicnParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString());

		getLocalizedFeed(aqicnParameter, new JsonDownloader() {
			@Override
			public void onResponseResult(Response<JsonElement> response) {
				multipleJsonDownloader.processResult(WeatherSourceType.AQICN,
						RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED, response);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleJsonDownloader.processResult(WeatherSourceType.AQICN,
						RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED, t);
			}

		});

	}
}
