package com.lifedawn.bestweather.weathers.dataprocessing.callback;

import com.lifedawn.bestweather.retrofit.interfaces.JsonDownloader;
import com.lifedawn.bestweather.retrofit.responses.kma.kmacommons.KmaRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;

import retrofit2.Response;

public class KmaVilageFcstCallback extends JsonDownloader {
	@Override
	public void onResponseSuccessful(Response response) {
	
	}
	
	@Override
	public void onResponseFailed(Exception e) {
	
	}
	
	@Override
	public void processResult(Response response) {
		VilageFcstRoot vilageFcstRoot = null;
		if (response.body() != null) {
			vilageFcstRoot = (VilageFcstRoot) response.body();
		} else {
			onResponseFailed(new Exception(response.message()));
			return;
		}
		
		if (vilageFcstRoot != null) {
			if (vilageFcstRoot.getResponse().getHeader().getResultCode().equals("00")) {
				onResponseSuccessful(response);
			} else {
				onResponseFailed(new Exception(vilageFcstRoot.getResponse().getHeader().getResultMsg()));
			}
		}
	}
}
