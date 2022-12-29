package com.lifedawn.bestweather.findaddress;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.data.remote.retrofit.client.RestfulApiQuery;
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.google.GoogleFindPlaceParameter;
import com.lifedawn.bestweather.data.remote.retrofit.responses.google.placesearch.GooglePlaceSearchResponse;
import com.lifedawn.bestweather.data.remote.retrofit.callback.JsonDownloader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleFindPlaceProcessing {
	public static GooglePlaceSearchResponse getGooglePlaceSearchResponse(String response) {
		return new Gson().fromJson(response, GooglePlaceSearchResponse.class);
	}

	public static void findPlace(String input, JsonDownloader callback) {
		RestfulApiQuery restfulApiQuery = RetrofitClient.getApiService(RetrofitClient.ServiceType.GOOGLE_PLACE_SEARCH);
		GoogleFindPlaceParameter parameter = new GoogleFindPlaceParameter(input);
		Call<JsonElement> call = restfulApiQuery.getFindPlaceSearch(parameter.getMap());

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (response.body() != null) {
					GooglePlaceSearchResponse googlePlaceSearchResponse =
							getGooglePlaceSearchResponse(response.body().toString());
					if (googlePlaceSearchResponse.getStatus().equals("OK")) {
						callback.onResponseResult(response, googlePlaceSearchResponse, response.body().toString());
					} else {
						callback.onResponseResult(new Exception());
					}
				} else {
					callback.onResponseResult(new Exception());
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
	}
}
