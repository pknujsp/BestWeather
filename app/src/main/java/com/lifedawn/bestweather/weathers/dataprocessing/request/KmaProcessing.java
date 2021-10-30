package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.Querys;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;
import com.lifedawn.bestweather.room.repository.KmaAreaCodesRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class KmaProcessing {
	private KmaProcessing() {
	}

	/**
	 * 초단기 실황
	 */
	public static Call<JsonElement> getUltraSrtNcstData(UltraSrtNcstParameter parameter, LocalDateTime localDateTime,
	                                                    JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
		//basetime설정
		if (localDateTime.getMinute() < 40) {
			localDateTime = localDateTime.minusHours(1);
		}

		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter HH = DateTimeFormatter.ofPattern("HH");

		parameter.setBaseDate(localDateTime.toLocalDate().format(yyyyMMdd));
		parameter.setBaseTime(localDateTime.toLocalTime().format(HH) + "00");

		Call<JsonElement> call = querys.getUltraSrtNcst(parameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
				Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 성공");

			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 실패");
			}
		});

		return call;
	}

	/**
	 * 초단기예보
	 */
	public static Call<JsonElement> getUltraSrtFcstData(UltraSrtFcstParameter parameter, LocalDateTime localDateTime,
	                                                    JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
		//basetime설정
		if (localDateTime.getMinute() < 45) {
			localDateTime = localDateTime.minusHours(1);
		}
		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter HH = DateTimeFormatter.ofPattern("HH");

		parameter.setBaseDate(localDateTime.toLocalDate().format(yyyyMMdd));
		parameter.setBaseTime(localDateTime.toLocalTime().format(HH) + "30");

		Call<JsonElement> call = Objects.requireNonNull(querys).getUltraSrtFcst(parameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
				Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 실패");
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
	public static Call<JsonElement> getVilageFcstData(VilageFcstParameter parameter, LocalDateTime localDateTime,
	                                                  JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.VILAGE_FCST);
		//basetime설정
		final int currentHour = localDateTime.getHour();
		final int currentMinute = localDateTime.getMinute();
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
			localDateTime = localDateTime.minusDays(1).withHour(23);
		} else {
			localDateTime = localDateTime.withHour(baseHour);
		}
		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter HH = DateTimeFormatter.ofPattern("HH");

		parameter.setBaseDate(localDateTime.toLocalDate().format(yyyyMMdd));
		parameter.setBaseTime(localDateTime.toLocalTime().format(HH) + "00");

		Call<JsonElement> call = Objects.requireNonNull(querys).getVilageFcst(parameter.getMap());

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
				Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 실패");
			}
		});
		return call;
	}


	/**
	 * 중기육상예보
	 *
	 * @param parameter
	 */
	public static Call<JsonElement> getMidLandFcstData(MidLandParameter parameter, JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_LAND_FCST);

		Call<JsonElement> call = Objects.requireNonNull(querys).getMidLandFcst(parameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
				Log.e(RetrofitClient.LOG_TAG, "kma mid land 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma mid land 실패");
			}
		});
		return call;
	}

	/**
	 * 중기기온조회
	 *
	 * @param parameter
	 */
	public static Call<JsonElement> getMidTaData(MidTaParameter parameter, JsonDownloader callback) {
		Querys querys = RetrofitClient.getApiService(RetrofitClient.ServiceType.MID_TA_FCST);

		Call<JsonElement> call = Objects.requireNonNull(querys).getMidTa(parameter.getMap());
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				callback.onResponseResult(response);
				Log.e(RetrofitClient.LOG_TAG, "kma mid ta 성공");
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma mid ta 실패");
			}
		});

		return call;
	}

	public static String getTmFc(LocalDateTime localDateTime) {
		final int hour = localDateTime.getHour();
		final int minute = localDateTime.getMinute();
		String tmFc = null;
		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

		if (hour >= 18 && minute >= 1) {
			localDateTime = localDateTime.withHour(18);
			tmFc = localDateTime.format(yyyyMMdd) + "1800";
		} else if (hour >= 6 && minute >= 1) {
			localDateTime = localDateTime.withHour(6);
			tmFc = localDateTime.format(yyyyMMdd) + "0600";
		} else {
			localDateTime = localDateTime.minusDays(1).withHour(18);
			tmFc = localDateTime.format(yyyyMMdd) + "1800";
		}
		return tmFc;
	}

	public static void requestWeatherData(Context context, Double latitude, Double longitude,
	                                      RequestKma requestKma,
	                                      MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		KmaAreaCodesRepository kmaAreaCodesRepository = new KmaAreaCodesRepository(context);
		kmaAreaCodesRepository.getAreaCodes(latitude, longitude,
				new DbQueryCallback<List<KmaAreaCodeDto>>() {
					@Override
					public void onResultSuccessful(List<KmaAreaCodeDto> result) {
						final double[] criteriaLatLng = {latitude, longitude};
						double minDistance = Double.MAX_VALUE;
						double distance = 0;
						double[] compLatLng = new double[2];
						KmaAreaCodeDto nearbyKmaAreaCodeDto = null;

						for (KmaAreaCodeDto weatherAreaCodeDTO : result) {
							compLatLng[0] = Double.parseDouble(weatherAreaCodeDTO.getLatitudeSecondsDivide100());
							compLatLng[1] = Double.parseDouble(weatherAreaCodeDTO.getLongitudeSecondsDivide100());

							distance = LocationDistance.distance(criteriaLatLng[0], criteriaLatLng[1], compLatLng[0], compLatLng[1],
									LocationDistance.Unit.METER);
							if (distance < minDistance) {
								minDistance = distance;
								nearbyKmaAreaCodeDto = weatherAreaCodeDTO;
							}
						}
						LocalDateTime koreaLocalDateTime = LocalDateTime.now(ZoneId.of(TimeZone.getTimeZone("Asia/Seoul").getID()));
						multipleJsonDownloader.put("koreaLocalDateTime", koreaLocalDateTime.toString());

						final String tmFc = getTmFc(LocalDateTime.of(koreaLocalDateTime.toLocalDate(),
								koreaLocalDateTime.toLocalTime()));
						multipleJsonDownloader.put("tmFc", tmFc);
						Set<RetrofitClient.ServiceType> requestTypeSet = requestKma.getRequestServiceTypes();

						if (requestTypeSet.contains(RetrofitClient.ServiceType.ULTRA_SRT_NCST)) {
							UltraSrtNcstParameter ultraSrtNcstParameter = new UltraSrtNcstParameter();
							ultraSrtNcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());

							getUltraSrtNcstData(ultraSrtNcstParameter, LocalDateTime.of(koreaLocalDateTime.toLocalDate(),
									koreaLocalDateTime.toLocalTime()),
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<JsonElement> response) {
											multipleJsonDownloader.processResult(WeatherSourceType.KMA, ultraSrtNcstParameter,
													RetrofitClient.ServiceType.ULTRA_SRT_NCST, response);
										}

										@Override
										public void onResponseResult(Throwable t) {
											multipleJsonDownloader.processResult(WeatherSourceType.KMA, ultraSrtNcstParameter,
													RetrofitClient.ServiceType.ULTRA_SRT_NCST, t);
										}
									});
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.ULTRA_SRT_FCST)) {
							UltraSrtFcstParameter ultraSrtFcstParameter = new UltraSrtFcstParameter();
							ultraSrtFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());

							getUltraSrtFcstData(ultraSrtFcstParameter, LocalDateTime.of(koreaLocalDateTime.toLocalDate(),
									koreaLocalDateTime.toLocalTime()),
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<JsonElement> response) {
											multipleJsonDownloader.processResult(WeatherSourceType.KMA, ultraSrtFcstParameter,
													RetrofitClient.ServiceType.ULTRA_SRT_FCST, response);
										}

										@Override
										public void onResponseResult(Throwable t) {
											multipleJsonDownloader.processResult(WeatherSourceType.KMA, ultraSrtFcstParameter,
													RetrofitClient.ServiceType.ULTRA_SRT_FCST, t);
										}
									});
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.VILAGE_FCST)) {
							VilageFcstParameter vilageFcstParameter = new VilageFcstParameter();
							vilageFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());

							getVilageFcstData(vilageFcstParameter, LocalDateTime.of(koreaLocalDateTime.toLocalDate(),
									koreaLocalDateTime.toLocalTime()),
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<JsonElement> response) {
											multipleJsonDownloader.processResult(WeatherSourceType.KMA, vilageFcstParameter,
													RetrofitClient.ServiceType.VILAGE_FCST, response);
										}

										@Override
										public void onResponseResult(Throwable t) {
											multipleJsonDownloader.processResult(WeatherSourceType.KMA, vilageFcstParameter,
													RetrofitClient.ServiceType.VILAGE_FCST, t);
										}
									});

						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.MID_LAND_FCST)) {
							MidLandParameter midLandParameter = new MidLandParameter();
							midLandParameter.setRegId(nearbyKmaAreaCodeDto.getMidLandFcstCode()).setTmFc(tmFc);

							getMidLandFcstData(midLandParameter, new JsonDownloader() {
								@Override
								public void onResponseResult(Response<JsonElement> response) {
									multipleJsonDownloader.processResult(WeatherSourceType.KMA, midLandParameter,
											RetrofitClient.ServiceType.MID_LAND_FCST, response);
								}

								@Override
								public void onResponseResult(Throwable t) {
									multipleJsonDownloader.processResult(WeatherSourceType.KMA, midLandParameter, RetrofitClient.ServiceType.MID_LAND_FCST, t);
								}

							});
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.MID_TA_FCST)) {
							MidTaParameter midTaParameter = new MidTaParameter();
							midTaParameter.setRegId(nearbyKmaAreaCodeDto.getMidTaCode()).setTmFc(tmFc);

							getMidTaData(midTaParameter, new JsonDownloader() {
								@Override
								public void onResponseResult(Response<JsonElement> response) {
									multipleJsonDownloader.processResult(WeatherSourceType.KMA, midTaParameter,
											RetrofitClient.ServiceType.MID_TA_FCST, response);
								}

								@Override
								public void onResponseResult(Throwable t) {
									multipleJsonDownloader.processResult(WeatherSourceType.KMA, midTaParameter, RetrofitClient.ServiceType.MID_TA_FCST, t);
								}

							});
						}
					}

					@Override
					public void onResultNoData() {
						Exception exception = new Exception("not found lat,lon");
						Set<RetrofitClient.ServiceType> requestTypeSet = requestKma.getRequestServiceTypes();

						if (requestTypeSet.contains(RetrofitClient.ServiceType.ULTRA_SRT_NCST)) {
							multipleJsonDownloader.processResult(WeatherSourceType.KMA, null,
									RetrofitClient.ServiceType.ULTRA_SRT_NCST, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.ULTRA_SRT_FCST)) {
							multipleJsonDownloader.processResult(WeatherSourceType.KMA, null,
									RetrofitClient.ServiceType.ULTRA_SRT_FCST, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.VILAGE_FCST)) {
							multipleJsonDownloader.processResult(WeatherSourceType.KMA, null,
									RetrofitClient.ServiceType.VILAGE_FCST, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.MID_LAND_FCST)) {
							multipleJsonDownloader.processResult(WeatherSourceType.KMA, null,
									RetrofitClient.ServiceType.MID_LAND_FCST, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.MID_TA_FCST)) {
							multipleJsonDownloader.processResult(WeatherSourceType.KMA, null,
									RetrofitClient.ServiceType.MID_TA_FCST, exception);

						}
					}


				});
	}


}
