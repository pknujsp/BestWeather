package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.Log;

import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.Queries;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.kma.KmaCurrentConditionsParameters;
import com.lifedawn.bestweather.retrofit.parameters.kma.KmaForecastsParameters;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaCurrentConditions;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaDailyForecast;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaHourlyForecast;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;
import com.lifedawn.bestweather.room.repository.KmaAreaCodesRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.parser.KmaWebParser;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class KmaProcessing {
	private KmaProcessing() {

	}

	/**
	 * 현재 날씨 web
	 */
	public static Call<String> getCurrentConditionsData(KmaCurrentConditionsParameters parameter, JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS);
		Call<String> call = queries.getKmaCurrentConditions(parameter.getParametersMap());

		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				if (response.body() != null) {
					final Document currentConditionsDocument = Jsoup.parse(response.body());
					KmaCurrentConditions kmaCurrentConditions = KmaWebParser.parseCurrentConditions(currentConditionsDocument,
							ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString());

					if (kmaCurrentConditions.getTemp().equals("자료없음") || kmaCurrentConditions.getTemp().contains("999")) {
						callback.onResponseResult(new Exception());
						Log.e(RetrofitClient.LOG_TAG, "kma current conditions 실패");
					} else {
						callback.onResponseResult(response, kmaCurrentConditions, response.body());
						Log.e(RetrofitClient.LOG_TAG, "kma current conditions 성공");
					}
				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "kma current conditions 실패");
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma current conditions 실패");
			}
		});

		return call;
	}

	/**
	 * 시간별, 일별 web
	 */
	public static Call<String> getForecastsData(KmaForecastsParameters parameter, JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
		Call<String> call = queries.getKmaHourlyAndDailyForecast(parameter.getParametersMap());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				if (response.body() != null) {
					final Document forecastsDocument = Jsoup.parse(response.body());
					List<KmaHourlyForecast> kmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(forecastsDocument);
					List<KmaDailyForecast> kmaDailyForecasts = KmaWebParser.parseDailyForecasts(forecastsDocument);
					KmaWebParser.makeExtendedDailyForecasts(kmaHourlyForecasts, kmaDailyForecasts);
					Object[] lists = new Object[]{kmaHourlyForecasts, kmaDailyForecasts};

					callback.onResponseResult(response, lists, response.body());
					Log.e(RetrofitClient.LOG_TAG, "kma forecasts 성공");
				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "kma forecasts 실패");
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma forecasts 실패");
			}
		});

		return call;
	}

	/**
	 * 초단기 실황
	 */
	public static Call<String> getUltraSrtNcstData(UltraSrtNcstParameter parameter, ZonedDateTime dateTime,
	                                               JsonDownloader callback) {
		//basetime설정
		if (dateTime.getMinute() < 40) {
			dateTime = dateTime.minusHours(1);
		}

		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter HH = DateTimeFormatter.ofPattern("HH");

		parameter.setBaseDate(dateTime.toLocalDate().format(yyyyMMdd));
		parameter.setBaseTime(dateTime.toLocalTime().format(HH) + "00");

		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST);
		Call<String> call = queries.getUltraSrtNcstByXml(parameter.getMap());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				VilageFcstResponse vilageFcstResponse = KmaResponseProcessor.successfulVilageResponse(response);
				if (vilageFcstResponse != null) {
					callback.onResponseResult(response, vilageFcstResponse, response.body());
					Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 성공");
				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 실패");
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma ultra srt ncst 실패");
			}
		});

		return call;
	}

	/**
	 * 1일전(정확히는 23시간 58분전) 초단기 실황
	 */
	public static Call<String> getYesterdayUltraSrtNcstData(UltraSrtNcstParameter parameter, ZonedDateTime dateTime,
	                                                        JsonDownloader callback) {
		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter HH = DateTimeFormatter.ofPattern("HHmm");

		dateTime = dateTime.minusHours(23).minusMinutes(58);

		parameter.setBaseDate(dateTime.format(yyyyMMdd));
		parameter.setBaseTime(dateTime.format(HH));

		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST);
		Call<String> call = queries.getUltraSrtNcstByXml(parameter.getMap());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				VilageFcstResponse vilageFcstResponse = KmaResponseProcessor.successfulVilageResponse(response);
				if (vilageFcstResponse != null) {
					callback.onResponseResult(response, vilageFcstResponse, response.body());
					Log.e(RetrofitClient.LOG_TAG, "yesterday kma ultra srt ncst 성공");
				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "yesterday kma ultra srt ncst 실패");
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "yesterday kma ultra srt ncst 실패");
			}
		});

		return call;
	}


	/**
	 * 초단기예보
	 */
	public static Call<String> getUltraSrtFcstData(UltraSrtFcstParameter parameter, ZonedDateTime dateTime,
	                                               JsonDownloader callback) {
		//basetime설정
		if (dateTime.getMinute() < 45) {
			dateTime = dateTime.minusHours(1);
		}
		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter HH = DateTimeFormatter.ofPattern("HH");

		parameter.setBaseDate(dateTime.toLocalDate().format(yyyyMMdd));
		parameter.setBaseTime(dateTime.toLocalTime().format(HH) + "30");

		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);
		Call<String> xmlCall = queries.getUltraSrtFcstByXml(parameter.getMap());
		xmlCall.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				VilageFcstResponse vilageFcstResponse = KmaResponseProcessor.successfulVilageResponse(response);
				if (vilageFcstResponse != null) {
					callback.onResponseResult(response, vilageFcstResponse, response.body());
					Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 성공");

				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 실패");

				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma ultra srt fcst 실패");
			}
		});

		return xmlCall;
	}

	/**
	 * 동네예보
	 * <p>
	 * - Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
	 * - API 제공 시간(~이후) : 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
	 */
	public static Call<String> getVilageFcstData(VilageFcstParameter parameter, ZonedDateTime dateTime,
	                                             JsonDownloader callback) {
		//basetime설정
		final int currentHour = dateTime.getHour();
		final int currentMinute = dateTime.getMinute();
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
			dateTime = dateTime.minusDays(1).withHour(23);
		} else {
			dateTime = dateTime.withHour(baseHour);
		}
		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter HH = DateTimeFormatter.ofPattern("HH");

		parameter.setBaseDate(dateTime.toLocalDate().format(yyyyMMdd));
		parameter.setBaseTime(dateTime.toLocalTime().format(HH) + "00");

		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_VILAGE_FCST);
		Call<String> call = queries.getVilageFcstByXml(parameter.getMap());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				VilageFcstResponse vilageFcstResponse = KmaResponseProcessor.successfulVilageResponse(response);
				if (vilageFcstResponse != null) {
					callback.onResponseResult(response, vilageFcstResponse, response.body());
					Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 성공");
				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 실패");
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
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
	public static Call<String> getMidLandFcstData(MidLandParameter parameter, JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_MID_LAND_FCST);
		Call<String> call = queries.getMidLandFcstByXml(parameter.getMap());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				MidLandFcstResponse midLandFcstResponse = KmaResponseProcessor.successfulMidLandFcstResponse(response);
				if (midLandFcstResponse != null) {
					callback.onResponseResult(response, midLandFcstResponse, response.body());
					Log.e(RetrofitClient.LOG_TAG, "kma mid land 성공");

				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "kma mid land 실패");

				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
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
	public static Call<String> getMidTaData(MidTaParameter parameter, JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_MID_TA_FCST);
		Call<String> call = queries.getMidTaByXml(parameter.getMap());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				MidTaResponse midTaResponse = KmaResponseProcessor.successfulMidTaFcstResponse(response);
				if (midTaResponse != null) {
					callback.onResponseResult(response, midTaResponse, response.body());
					Log.e(RetrofitClient.LOG_TAG, "kma mid ta 성공");
				} else {
					callback.onResponseResult(new Exception());
					Log.e(RetrofitClient.LOG_TAG, "kma mid ta 실패");
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e(RetrofitClient.LOG_TAG, "kma mid ta 실패");
			}
		});
		return call;
	}

	public static String getTmFc(ZonedDateTime dateTime) {
		final int hour = dateTime.getHour();
		final int minute = dateTime.getMinute();
		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

		if (hour >= 18 && minute >= 1) {
			dateTime = dateTime.withHour(18);
			return dateTime.format(yyyyMMdd) + "1800";
		} else if (hour >= 6 && minute >= 1) {
			dateTime = dateTime.withHour(6);
			return dateTime.format(yyyyMMdd) + "0600";
		} else {
			dateTime = dateTime.minusDays(1).withHour(18);
			return dateTime.format(yyyyMMdd) + "1800";
		}
	}

	public static void requestWeatherDataAsXML(Context context, Double latitude, Double longitude,
	                                           RequestKma requestKma,
	                                           WeatherRestApiDownloader weatherRestApiDownloader) {
		KmaAreaCodesRepository kmaAreaCodesRepository = KmaAreaCodesRepository.getINSTANCE();
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
						ZonedDateTime koreaLocalDateTime = ZonedDateTime.now(KmaResponseProcessor.getZoneId());
						weatherRestApiDownloader.put("koreaLocalDateTime", koreaLocalDateTime.toString());

						final String tmFc = getTmFc(koreaLocalDateTime);
						weatherRestApiDownloader.put("tmFc", tmFc);
						Set<RetrofitClient.ServiceType> requestTypeSet = requestKma.getRequestServiceTypes();

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST)) {
							final UltraSrtNcstParameter ultraSrtNcstParameter = new UltraSrtNcstParameter();
							ultraSrtNcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY())
									.setLatitude(latitude).setLongitude(longitude);

							Call<String> ultraSrtNcstCall = getUltraSrtNcstData(ultraSrtNcstParameter,
									ZonedDateTime.of(koreaLocalDateTime.toLocalDateTime(), koreaLocalDateTime.getZone()),
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, ultraSrtNcstParameter,
													RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST, response, responseObj, responseText);
										}

										@Override
										public void onResponseResult(Throwable t) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, ultraSrtNcstParameter,
													RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST, t);
										}
									});
							weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST, ultraSrtNcstCall);

						}

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST)) {
							final UltraSrtNcstParameter ultraSrtNcstParameter = new UltraSrtNcstParameter();
							ultraSrtNcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY())
									.setLatitude(latitude).setLongitude(longitude);

							Call<String> yesterdayUltraSrtNcstCall = getYesterdayUltraSrtNcstData(ultraSrtNcstParameter,
									ZonedDateTime.of(koreaLocalDateTime.toLocalDateTime(), koreaLocalDateTime.getZone()),
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, ultraSrtNcstParameter,
													RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST, response, responseObj, responseText);
										}

										@Override
										public void onResponseResult(Throwable t) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, ultraSrtNcstParameter,
													RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST, t);
										}
									});
							weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST, yesterdayUltraSrtNcstCall);

						}

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST)) {
							UltraSrtFcstParameter ultraSrtFcstParameter = new UltraSrtFcstParameter();
							ultraSrtFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY()).setLatitude(latitude).setLongitude(longitude);


							Call<String> ultraSrtFcstCall = getUltraSrtFcstData(ultraSrtFcstParameter,
									ZonedDateTime.of(koreaLocalDateTime.toLocalDateTime(), koreaLocalDateTime.getZone()),
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, ultraSrtFcstParameter,
													RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST, response, responseObj, responseText);
										}

										@Override
										public void onResponseResult(Throwable t) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, ultraSrtFcstParameter,
													RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST, t);
										}
									});
							weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST, ultraSrtFcstCall);

						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_VILAGE_FCST)) {
							VilageFcstParameter vilageFcstParameter = new VilageFcstParameter();
							vilageFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY())
									.setLatitude(latitude).setLongitude(longitude);

							Call<String> vilageFcstCall = getVilageFcstData(vilageFcstParameter,
									ZonedDateTime.of(koreaLocalDateTime.toLocalDateTime(), koreaLocalDateTime.getZone()),
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, vilageFcstParameter,
													RetrofitClient.ServiceType.KMA_VILAGE_FCST, response, responseObj, responseText);
										}

										@Override
										public void onResponseResult(Throwable t) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, vilageFcstParameter,
													RetrofitClient.ServiceType.KMA_VILAGE_FCST, t);
										}
									});
							weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_VILAGE_FCST, vilageFcstCall);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_MID_LAND_FCST)) {
							MidLandParameter midLandParameter = new MidLandParameter();
							midLandParameter.setRegId(nearbyKmaAreaCodeDto.getMidLandFcstCode()).setTmFc(tmFc)
									.setLatitude(latitude).setLongitude(longitude);

							Call<String> midLandFcstCall = getMidLandFcstData(midLandParameter, new JsonDownloader() {
								@Override
								public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
									weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, midLandParameter,
											RetrofitClient.ServiceType.KMA_MID_LAND_FCST, response, responseObj, responseText);
								}

								@Override
								public void onResponseResult(Throwable t) {
									weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, midLandParameter,
											RetrofitClient.ServiceType.KMA_MID_LAND_FCST, t);
								}

							});
							weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_MID_LAND_FCST, midLandFcstCall);

						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_MID_TA_FCST)) {
							MidTaParameter midTaParameter = new MidTaParameter();
							midTaParameter.setRegId(nearbyKmaAreaCodeDto.getMidTaCode()).setTmFc(tmFc)
									.setLatitude(latitude).setLongitude(longitude);

							Call<String> midTaFcstCall = getMidTaData(midTaParameter, new JsonDownloader() {
								@Override
								public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
									weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, midTaParameter,
											RetrofitClient.ServiceType.KMA_MID_TA_FCST, response, responseObj, responseText);
								}

								@Override
								public void onResponseResult(Throwable t) {
									weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, midTaParameter,
											RetrofitClient.ServiceType.KMA_MID_TA_FCST, t);
								}

							});
							weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_MID_TA_FCST, midTaFcstCall);

						}
					}

					@Override
					public void onResultNoData() {
						Exception exception = new Exception("not found lat,lon");
						Set<RetrofitClient.ServiceType> requestTypeSet = requestKma.getRequestServiceTypes();

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST)) {
							weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, null,
									RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST)) {
							weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, null,
									RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_VILAGE_FCST)) {
							weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, null,
									RetrofitClient.ServiceType.KMA_VILAGE_FCST, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_MID_LAND_FCST)) {
							weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, null,
									RetrofitClient.ServiceType.KMA_MID_LAND_FCST, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_MID_TA_FCST)) {
							weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, null,
									RetrofitClient.ServiceType.KMA_MID_TA_FCST, exception);

						}
					}


				});
	}


	public static void requestWeatherDataAsWEB(Context context, Double latitude, Double longitude,
	                                           RequestKma requestKma,
	                                           WeatherRestApiDownloader weatherRestApiDownloader) {
		KmaResponseProcessor.init(context);

		KmaAreaCodesRepository kmaAreaCodesRepository = KmaAreaCodesRepository.getINSTANCE();
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
						ZonedDateTime koreaLocalDateTime = ZonedDateTime.now(KmaResponseProcessor.getZoneId());
						weatherRestApiDownloader.put("koreaLocalDateTime", koreaLocalDateTime.toString());

						final String tmFc = getTmFc(koreaLocalDateTime);
						weatherRestApiDownloader.put("tmFc", tmFc);
						Set<RetrofitClient.ServiceType> requestTypeSet = requestKma.getRequestServiceTypes();

						final String code = nearbyKmaAreaCodeDto.getAdministrativeAreaCode();

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS)) {
							final KmaCurrentConditionsParameters parameters = new KmaCurrentConditionsParameters(code);
							parameters.setLatitude(latitude).setLongitude(longitude);

							Call<String> currentConditionsCall = getCurrentConditionsData(parameters,
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, parameters,
													RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS, response, responseObj, responseText);
										}

										@Override
										public void onResponseResult(Throwable t) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, parameters,
													RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS, t);
										}
									});
							weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS, currentConditionsCall);

						}

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)) {
							final KmaForecastsParameters parameters = new KmaForecastsParameters(code);
							parameters.setLatitude(latitude).setLongitude(longitude);

							Call<String> forecastsCall = getForecastsData(parameters,
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, parameters,
													RetrofitClient.ServiceType.KMA_WEB_FORECASTS, response, responseObj, responseText);
										}

										@Override
										public void onResponseResult(Throwable t) {
											weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, parameters,
													RetrofitClient.ServiceType.KMA_WEB_FORECASTS, t);
										}
									});
							weatherRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_WEB_FORECASTS, forecastsCall);

						}

					}

					@Override
					public void onResultNoData() {
						Exception exception = new Exception("not found lat,lon");
						Set<RetrofitClient.ServiceType> requestTypeSet = requestKma.getRequestServiceTypes();

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS)) {
							weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, null,
									RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS, exception);
						}
						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)) {
							weatherRestApiDownloader.processResult(WeatherProviderType.KMA_WEB, null,
									RetrofitClient.ServiceType.KMA_WEB_FORECASTS, exception);
						}

					}


				});
	}

}
