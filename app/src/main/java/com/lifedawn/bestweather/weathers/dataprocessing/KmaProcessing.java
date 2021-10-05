package com.lifedawn.bestweather.weathers.dataprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.retrofit.util.RetrofitCallListManager;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
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
	public static Call<JsonObject> getUltraSrtNcstData(UltraSrtNcstParameter parameter, Calendar calendar, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
		//basetime설정
		if (calendar.get(Calendar.MINUTE) < 40) {
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}

		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);
		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "00");

		Call<JsonObject> call = querys.getUltraSrtNcst(parameter.getMap());
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

	/**
	 * 초단기예보
	 */
	public static Call<JsonObject> getUltraSrtFcstData(UltraSrtFcstParameter parameter, Calendar calendar, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
		//basetime설정
		if (calendar.get(Calendar.MINUTE) < 45) {
			calendar.add(Calendar.HOUR_OF_DAY, -1);
		}
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd", Locale.US);
		SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.US);

		parameter.setBaseDate(yyyyMMdd.format(calendar.getTime()));
		parameter.setBaseTime(HH.format(calendar.getTime()) + "30");

		Call<JsonObject> call = Objects.requireNonNull(querys).getUltraSrtFcst(parameter.getMap());
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

	/**
	 * 동네예보
	 * <p>
	 * - Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
	 * - API 제공 시간(~이후) : 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
	 */
	public static Call<JsonObject> getVilageFcstData(VilageFcstParameter parameter, Calendar calendar, JsonDownloader<JsonObject> callback) {
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

		Call<JsonObject> call = Objects.requireNonNull(querys).getVilageFcst(parameter.getMap());

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


	/**
	 * 중기육상예보
	 *
	 * @param parameter
	 */
	public static Call<JsonObject> getMidLandFcstData(MidLandParameter parameter, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_LAND_FCST);

		Call<JsonObject> call = Objects.requireNonNull(querys).getMidLandFcst(parameter.getMap());

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

	/**
	 * 중기기온조회
	 *
	 * @param parameter
	 */
	public static Call<JsonObject> getMidTaData(MidTaParameter parameter, JsonDownloader<JsonObject> callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_TA_FCST);

		Call<JsonObject> call = Objects.requireNonNull(querys).getMidTa(parameter.getMap());

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

	public static void getKmaForecasts(KmaAreaCodeDto nearbyKmaAreaCodeDto, Calendar calendar,
	                                   MultipleJsonDownloader<JsonObject> multipleJsonDownloader) {
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

		midLandParameter.setTmFc(tmFc);
		midTaParameter.setTmFc(tmFc);

		final JsonDownloader<JsonObject> kmaVilageFcstCallback = new JsonDownloader<JsonObject>() {
			@Override
			public void onResponseSuccessful(Response<? extends JsonObject> response) {
				multipleJsonDownloader.processResult(response);
			}

			@Override
			public void onResponseFailed(Exception e) {
				multipleJsonDownloader.processResult(e);
			}

			@Override
			public void processResult(Response<? extends JsonObject> response) {
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
		};


		Call<JsonObject> ultraSrtNcstCall = getUltraSrtNcstData(ultraSrtNcstParameter, (Calendar) calendar.clone(),
				kmaVilageFcstCallback);

		Call<JsonObject> ultraSrtFcstCall = getUltraSrtFcstData(ultraSrtFcstParameter, (Calendar) calendar.clone(),
				kmaVilageFcstCallback);

		Call<JsonObject> vilageFcstCall = getVilageFcstData(vilageFcstParameter, (Calendar) calendar.clone(), kmaVilageFcstCallback);

		Call<JsonObject> midTaFcstCall = getMidTaData(midTaParameter, new JsonDownloader<JsonObject>() {
			@Override
			public void onResponseSuccessful(Response<? extends JsonObject> response) {
				multipleJsonDownloader.processResult(response);
			}

			@Override
			public void onResponseFailed(Exception e) {
				multipleJsonDownloader.processResult(e);
			}

			@Override
			public void processResult(Response<? extends JsonObject> response) {
				MidTaRoot midTaRoot = null;
				if (response.body() != null) {
					Gson gson = new Gson();
					midTaRoot = gson.fromJson(response.body().toString(), MidTaRoot.class);
				} else {
					onResponseFailed(new Exception(response.message()));
					return;
				}

				if (midTaRoot != null) {
					if (midTaRoot.getResponse().getHeader().getResultCode().equals("00")) {
						onResponseSuccessful(response);
						midTaRoot = null;

					} else {
						onResponseFailed(new Exception(midTaRoot.getResponse().getHeader().getResultMsg()));
					}
				}
			}
		});

		Call<JsonObject> midLandFcstCall = getMidLandFcstData(midLandParameter, new JsonDownloader<JsonObject>() {
			@Override
			public void onResponseSuccessful(Response<? extends JsonObject> response) {
				multipleJsonDownloader.processResult(response);
			}

			@Override
			public void onResponseFailed(Exception e) {
				multipleJsonDownloader.processResult(e);
			}

			@Override
			public void processResult(Response<? extends JsonObject> response) {
				MidLandFcstRoot midLandFcstRoot = null;
				if (response.body() != null) {
					Gson gson = new Gson();
					midLandFcstRoot = gson.fromJson(response.body().toString(), MidLandFcstRoot.class);
				} else {
					onResponseFailed(new Exception(response.message()));
					return;
				}

				if (midLandFcstRoot != null) {
					if (midLandFcstRoot.getResponse().getHeader().getResultCode().equals("00")) {
						onResponseSuccessful(response);
						midLandFcstRoot = null;
					} else {
						onResponseFailed(new Exception(midLandFcstRoot.getResponse().getHeader().getResultMsg()));
					}
				}

			}
		});

		RetrofitCallListManager.CallObj newCallObj = RetrofitCallListManager.newCalls();
		newCallObj.add(ultraSrtNcstCall);
		newCallObj.add(ultraSrtFcstCall);
		newCallObj.add(vilageFcstCall);
		newCallObj.add(midTaFcstCall);
		newCallObj.add(midLandFcstCall);
	}


}
