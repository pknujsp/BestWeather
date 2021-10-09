package com.lifedawn.bestweather.weathers.dataprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.aqicn.AqicnParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.CurrentWeatherParameter;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.retrofit.util.RetrofitCallListManager;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AqicnProcessing {
	public static Call<JsonObject> getLocalizedFeed(AqicnParameter aqicnParameter, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		
		Call<JsonObject> call = querys.getGeolocalizedFeed(aqicnParameter.getLatitude(), aqicnParameter.getLongitude(),
				aqicnParameter.getMap());
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
			}
		});
		return call;
	}
	
	public static void getAirQuality(String latitude, String longitude, MultipleJsonDownloader<JsonObject> multipleJsonDownloader) {
		AqicnParameter aqicnParameter = new AqicnParameter();
		aqicnParameter.setLatitude(latitude).setLongitude(longitude);
		
		Call<JsonObject> geolocalizedFeed = getLocalizedFeed(aqicnParameter, new JsonDownloader<JsonObject>() {
			@Override
			public void onResponseSuccessful(Response<? extends JsonObject> response) {
			
			}
			
			@Override
			public void onResponseFailed(Exception e) {
			
			}
			
			@Override
			public void processResult(Response<? extends JsonObject> response) {
				if (response.isSuccessful()) {
					onResponseSuccessful(response);
				} else {
					onResponseFailed(new Exception(response.toString()));
				}
			}
		});
		
		RetrofitCallListManager.CallObj newCallObj = RetrofitCallListManager.newCalls();
		newCallObj.add(geolocalizedFeed);
	}
}
