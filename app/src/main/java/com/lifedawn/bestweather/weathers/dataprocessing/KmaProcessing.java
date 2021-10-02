package com.lifedawn.bestweather.weathers.dataprocessing;

import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.interfaces.JsonDownloader;
import com.lifedawn.bestweather.retrofit.interfaces.RetrofitCallListManager;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KmaProcessing implements RetrofitCallListManager.CallManagerListener {
	private static RetrofitCallListManager retrofitCallListManager = new RetrofitCallListManager();
	private static KmaProcessing instance = new KmaProcessing();
	
	public static KmaProcessing getInstance() {
		return instance;
	}
	
	public static void close() {
		retrofitCallListManager.clear();
	}
	
	public KmaProcessing() {
	}
	
	/**
	 * 초단기 실황
	 */
	public void getUltraSrtNcstData(UltraSrtNcstParameter parameter, Calendar calendar, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
		//basetime설정
		
		if (calendar.get(Calendar.MINUTE) < 40) {
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}
		
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat HH = new SimpleDateFormat("HH");
		
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "00");
		
		Call<JsonObject> call = querys.getUltraSrtNcst(parameter.getMap());
		retrofitCallListManager.add(call);
		
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
				retrofitCallListManager.remove(call);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
				retrofitCallListManager.remove(call);
			}
		});
		
	}
	
	/**
	 * 초단기예보
	 *
	 * @param parameter
	 */
	public void getUltraSrtFcstData(UltraSrtFcstParameter parameter, Calendar calendar, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
		//basetime설정
		if (calendar.get(Calendar.MINUTE) < 45) {
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat HH = new SimpleDateFormat("HH");
		
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "30");
		
		Call<JsonObject> call = querys.getUltraSrtFcst(parameter.getMap());
		retrofitCallListManager.add(call);
		
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
				retrofitCallListManager.remove(call);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
				retrofitCallListManager.remove(call);
			}
		});
	}
	
	/**
	 * 동네예보
	 * <p>
	 * - Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
	 * - API 제공 시간(~이후) : 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
	 */
	public void getVilageFcstData(VilageFcstParameter parameter, Calendar calendar, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.VILAGE_FCST);
		//basetime설정
		final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		final int currentMinute = calendar.get(Calendar.MINUTE);
		int i = currentHour >= 0 && currentHour <= 2 ? 7 : currentHour / 3 - 1;
		int baseHour = 0;
		
		if (currentMinute > 10 && (currentHour - 2) % 3 == 0) {
			// ex)1411인 경우
			baseHour = 3 * ((currentHour - 2) / 3) + 2;
			i = 0;
		} else {
			baseHour = 3 * i + 2;
		}
		
		if (i == 7) {
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, baseHour);
		}
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat HH = new SimpleDateFormat("HH");
		
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "00");
		
		Call<JsonObject> call = querys.getVilageFcst(parameter.getMap());
		retrofitCallListManager.add(call);
		
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
				retrofitCallListManager.remove(call);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
				retrofitCallListManager.remove(call);
			}
		});
		
	}
	
	public void getHourlyFcstData(VilageFcstParameter vilageFcstParameter, UltraSrtFcstParameter ultraSrtFcstParameter, Calendar calendar,
			JsonDownloader<VilageFcstRoot> callback) {
		VilageFcstRoot hourlyFcstRoot = new VilageFcstRoot();
		
		getVilageFcstData(vilageFcstParameter, (Calendar) calendar.clone(), new JsonDownloader<JsonObject>() {
			@Override
			public void onResponseSuccessful(JsonObject result) {
				hourlyFcstRoot.setVilageFcst(result);
				checkHourlyFcstDataDownload(hourlyFcstRoot, callback);
			}
			
			@Override
			public void onResponseFailed(Exception e) {
				hourlyFcstRoot.setException(e);
				checkHourlyFcstDataDownload(hourlyFcstRoot, callback);
			}
		});
		
		getUltraSrtFcstData(ultraSrtFcstParameter, (Calendar) calendar.clone(), new JsonDownloader<JsonObject>() {
			@Override
			public void onResponseSuccessful(JsonObject result) {
				hourlyFcstRoot.setUltraSrtFcst(result);
				checkHourlyFcstDataDownload(hourlyFcstRoot, callback);
			}
			
			@Override
			public void onResponseFailed(Exception e) {
				hourlyFcstRoot.setException(e);
				checkHourlyFcstDataDownload(hourlyFcstRoot, callback);
			}
		});
	}
	
	/**
	 * 중기육상예보
	 *
	 * @param parameter
	 */
	public void getMidLandFcstData(MidLandParameter parameter, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_LAND_FCST);
		
		Call<JsonObject> call = querys.getMidLandFcst(parameter.getMap());
		retrofitCallListManager.add(call);
		
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
				retrofitCallListManager.remove(call);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
				retrofitCallListManager.remove(call);
			}
		});
		
	}
	
	/**
	 * 중기기온조회
	 *
	 * @param parameter
	 */
	public void getMidTaData(MidTaParameter parameter, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_TA_FCST);
		
		Call<JsonObject> call = querys.getMidTa(parameter.getMap());
		retrofitCallListManager.add(call);
		
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				callback.processResult(response);
				retrofitCallListManager.remove(call);
			}
			
			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				callback.processResult(t);
				retrofitCallListManager.remove(call);
			}
		});
	}
	
	public void getMidFcstData(MidLandParameter midLandFcstParameter, MidTaParameter midTaParameter,
			JsonDownloader<MidFcstParentRoot> callback) {
		MidFcstParentRoot midFcstRoot = new MidFcstParentRoot();
		
		getMidLandFcstData(midLandFcstParameter, new JsonDownloader<JsonObject>() {
			@Override
			public void onResponseSuccessful(JsonObject result) {
			}
			
			@Override
			public void onResponseFailed(Exception e) {
			}
		});
		
		getMidTaData(midTaParameter, new JsonDownloader<JsonObject>() {
			
			@Override
			public void onResponseSuccessful(JsonObject result) {
			
			}
			
			@Override
			public void onResponseFailed(Exception e) {
			
			}
			
			@Override
			public void processResult(Response<? extends JsonObject> response) {
			
			}
		});
	}
	
	
	@Override
	public void clear() {
		retrofitCallListManager.clear();
	}
}
