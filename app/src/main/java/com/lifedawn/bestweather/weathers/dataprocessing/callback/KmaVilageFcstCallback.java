package com.lifedawn.bestweather.weathers.dataprocessing.callback;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;

import retrofit2.Response;

public class KmaVilageFcstCallback extends JsonDownloader {
	@Override
	public void onResponseSuccessful(Response<JsonObject> response) {
	
	}
	
	@Override
	public void onResponseFailed(Exception e) {
	
	}
	
	@Override
	public void processResult(Response<JsonObject> response) {
		VilageFcstRoot vilageFcstRoot = null;
		if (response.body() != null) {
			Gson gson = new Gson();
			vilageFcstRoot = gson.fromJson(response.body().toString(), VilageFcstRoot.class);
		} else {
			onResponseFailed(new Exception(response.message()));
			return;
		}
		
		if (vilageFcstRoot != null) {
			if (vilageFcstRoot.getResponse().getHeader().getResultCode().equals("00")) {
				onResponseSuccessful(response);
				vilageFcstRoot = null;
			} else {
				onResponseFailed(new Exception(vilageFcstRoot.getResponse().getHeader().getResultMsg()));
			}
		}
		
	}
}
