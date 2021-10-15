package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.util.Log;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class KmaProcessing {
	private KmaProcessing() {
	}
	
	/**
	 * 초단기 실황
	 */
	public static Call<JsonElement> getUltraSrtNcstData(UltraSrtNcstParameter parameter, Calendar calendar,
			JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
		//basetime설정
		if (calendar.get(Calendar.MINUTE) < 40) {
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}
		
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "00");
		
		Call<JsonElement> call = querys.getUltraSrtNcst(parameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		
		return call;
	}
	
	/**
	 * 초단기예보
	 */
	public static Call<JsonElement> getUltraSrtFcstData(UltraSrtFcstParameter parameter, Calendar calendar,
			JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
		//basetime설정
		if (calendar.get(Calendar.MINUTE) < 45) {
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);
		
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "30");
		
		Call<JsonElement> call = Objects.requireNonNull(querys).getUltraSrtFcst(parameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		return call;
	}
	
	/**
	 * 동네예보
	 * <p>
	 * - Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
	 * - API 제공 시간(~이후) : 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
	 */
	public static Call<JsonElement> getVilageFcstData(VilageFcstParameter parameter, Calendar calendar,
			JsonDownloader<JsonElement> callback) {
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
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);
		
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "00");
		
		Call<JsonElement> call = Objects.requireNonNull(querys).getVilageFcst(parameter.getMap());
		
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		return call;
	}
	
	
	/**
	 * 중기육상예보
	 *
	 * @param parameter
	 */
	public static Call<JsonElement> getMidLandFcstData(MidLandParameter parameter, JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_LAND_FCST);
		
		Call<JsonElement> call = Objects.requireNonNull(querys).getMidLandFcst(parameter.getMap());
		
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		return call;
	}
	
	/**
	 * 중기기온조회
	 *
	 * @param parameter
	 */
	public static Call<JsonElement> getMidTaData(MidTaParameter parameter, JsonDownloader<JsonElement> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_TA_FCST);
		
		Call<JsonElement> call = Objects.requireNonNull(querys).getMidTa(parameter.getMap());
		
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
			}
			
			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});
		
		return call;
	}
	
	public static String getTmFc(Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		String tmFc = null;
		
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		
		if (hour >= 18 && minute >= 1) {
			calendar.set(Calendar.HOUR_OF_DAY, 18);
			tmFc = yyyyMMdd.format(calendar.getTime()) + "1800";
		} else if (hour >= 6 && minute >= 1) {
			calendar.set(Calendar.HOUR_OF_DAY, 6);
			tmFc = yyyyMMdd.format(calendar.getTime()) + "0600";
		} else {
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			calendar.set(Calendar.HOUR_OF_DAY, 18);
			tmFc = yyyyMMdd.format(calendar.getTime()) + "1800";
		}
		return tmFc;
	}
	
	public static void getKmaForecasts(KmaAreaCodeDto nearbyKmaAreaCodeDto, Calendar calendar,
			MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		UltraSrtNcstParameter ultraSrtNcstParameter = new UltraSrtNcstParameter();
		UltraSrtFcstParameter ultraSrtFcstParameter = new UltraSrtFcstParameter();
		VilageFcstParameter vilageFcstParameter = new VilageFcstParameter();
		MidLandParameter midLandParameter = new MidLandParameter();
		MidTaParameter midTaParameter = new MidTaParameter();
		
		ultraSrtNcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());
		ultraSrtFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());
		vilageFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());
		midLandParameter.setRegId(nearbyKmaAreaCodeDto.getMidLandFcstCode());
		midTaParameter.setRegId(nearbyKmaAreaCodeDto.getMidTaCode());
		
		String tmFc = getTmFc(calendar);
		multipleJsonDownloader.put("tmFc", tmFc);
		
		midLandParameter.setTmFc(tmFc);
		midTaParameter.setTmFc(tmFc);
		
		Call<JsonElement> ultraSrtNcstCall = getUltraSrtNcstData(ultraSrtNcstParameter, (Calendar) calendar.clone(),
				new JsonDownloader<JsonElement>() {
					@Override
					public void onResponseResult(Response<? extends JsonElement> response) {
						Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 성공");
						multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
								RetrofitClient.ServiceType.ULTRA_SRT_NCST, response);
					}
					
					@Override
					public void onResponseResult(Throwable t) {
						Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 실패");
						multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
								RetrofitClient.ServiceType.ULTRA_SRT_NCST, t);
					}
				});
		
		Call<JsonElement> ultraSrtFcstCall = getUltraSrtFcstData(ultraSrtFcstParameter, (Calendar) calendar.clone(),
				new JsonDownloader<JsonElement>() {
					@Override
					public void onResponseResult(Response<? extends JsonElement> response) {
						Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 성공");
						multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
								RetrofitClient.ServiceType.ULTRA_SRT_FCST, response);
					}
					
					@Override
					public void onResponseResult(Throwable t) {
						Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 실패");
						multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
								RetrofitClient.ServiceType.ULTRA_SRT_FCST, t);
					}
				});
		
		Call<JsonElement> vilageFcstCall = getVilageFcstData(vilageFcstParameter, (Calendar) calendar.clone(),
				new JsonDownloader<JsonElement>() {
					@Override
					public void onResponseResult(Response<? extends JsonElement> response) {
						Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 성공");
						multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA, RetrofitClient.ServiceType.VILAGE_FCST,
								response);
					}
					
					@Override
					public void onResponseResult(Throwable t) {
						Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 실패");
						multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA, RetrofitClient.ServiceType.VILAGE_FCST,
								t);
					}
				});
		
		Call<JsonElement> midTaFcstCall = getMidTaData(midTaParameter, new JsonDownloader<JsonElement>() {
			@Override
			public void onResponseResult(Response<? extends JsonElement> response) {
				Log.e(RetrofitClient.LOG_TAG, "kma mid ta 성공");
				multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA, RetrofitClient.ServiceType.MID_TA_FCST,
						response);
			}
			
			@Override
			public void onResponseResult(Throwable t) {
				Log.e(RetrofitClient.LOG_TAG, "kma mid ta 실패");
				multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA, RetrofitClient.ServiceType.MID_TA_FCST, t);
			}
			
		});
		
		Call<JsonElement> midLandFcstCall = getMidLandFcstData(midLandParameter, new JsonDownloader<JsonElement>() {
			@Override
			public void onResponseResult(Response<? extends JsonElement> response) {
				Log.e(RetrofitClient.LOG_TAG, "kma mid land 성공");
				multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA, RetrofitClient.ServiceType.MID_LAND_FCST,
						response);
			}
			
			@Override
			public void onResponseResult(Throwable t) {
				Log.e(RetrofitClient.LOG_TAG, "kma mid land 실패");
				multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA, RetrofitClient.ServiceType.MID_LAND_FCST, t);
			}
			
		});
		
	}
	
	
}
