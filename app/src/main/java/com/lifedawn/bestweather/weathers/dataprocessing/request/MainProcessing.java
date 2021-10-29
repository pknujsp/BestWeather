package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.FiveDaysOfDailyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.GeoPositionSearchParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.TwelveHoursOfHourlyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.geopositionsearch.GeoPositionResponse;
import com.lifedawn.bestweather.retrofit.util.JsonDownloader;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;
import com.lifedawn.bestweather.room.repository.KmaAreaCodesRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Response;

public class MainProcessing {

	public static void requestWeatherData(Context context, Double latitude, Double longitude,
	                                      ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources,
	                                      MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		int totalRequestCount = 0;
		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			//요청 좌표의 locationKey가 저장되어 있는지 확인
			if (AccuWeatherProcessing.getLocationKey(context, latitude, longitude).isEmpty()) {
				++totalRequestCount;
				requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER).addRequestServiceType(RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);
			}
		}

		multipleJsonDownloader.setRequestCount(totalRequestCount);

		if (requestWeatherSources.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.requestWeatherData(context, latitude, longitude, (RequestAccu) requestWeatherSources.get(WeatherSourceType.ACCU_WEATHER), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.KMA)) {
			KmaProcessing.requestWeatherData(context, latitude, longitude, (RequestKma) requestWeatherSources.get(WeatherSourceType.KMA), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.requestWeatherData(context, latitude, longitude,
					(RequestOwm) requestWeatherSources.get(WeatherSourceType.OPEN_WEATHER_MAP), multipleJsonDownloader);
		}
		if (requestWeatherSources.containsKey(WeatherSourceType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, multipleJsonDownloader);
		}
	}

	public static void downloadAllWeatherData(Context context, final String latitude, final String longitude,
	                                          final Set<WeatherSourceType> weatherSourceTypeSet, MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		int totalRequestCount = 0;

		if (weatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			if (AccuWeatherProcessing.getLocationKey(context, latitude, longitude).isEmpty()) {
				totalRequestCount += 4;
			} else {
				totalRequestCount += 3;
			}
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.AQICN)) {
			totalRequestCount += 1;
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			totalRequestCount += 5;
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.MET_NORWAY)) {
			totalRequestCount += 1;
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			totalRequestCount += 1;
		}

		final LocalDateTime localDateTime = LocalDateTime.now();
		multipleJsonDownloader.put("localDateTime", localDateTime.toString());

		multipleJsonDownloader.setRequestCount(totalRequestCount);

		if (weatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.getAccuWeatherForecasts(context, latitude, longitude, multipleJsonDownloader);
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			KmaAreaCodesRepository kmaAreaCodesRepository = new KmaAreaCodesRepository(context);
			kmaAreaCodesRepository.getAreaCodes(Double.parseDouble(latitude), Double.parseDouble(longitude),
					new DbQueryCallback<List<KmaAreaCodeDto>>() {
						@Override
						public void onResultSuccessful(List<KmaAreaCodeDto> result) {
							final double[] criteriaLatLng = {Double.parseDouble(latitude), Double.parseDouble(longitude)};
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

							KmaProcessing.getKmaForecasts(nearbyKmaAreaCodeDto, multipleJsonDownloader);
						}

						@Override
						public void onResultNoData() {

						}
					});


		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.MET_NORWAY)) {
			MetNorwayProcessing.getMetNorwayForecasts(latitude, longitude, multipleJsonDownloader);
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.getOwmForecasts(latitude, longitude, true, multipleJsonDownloader);
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.AQICN)) {
			AqicnProcessing.getAirQuality(latitude, longitude, multipleJsonDownloader);
		}

	}

	public static void downloadHourlyForecasts(Context context, Double latitude, Double longitude,
	                                           Set<WeatherSourceType> requestWeatherSourceTypeSet, MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		int totalRequestCount = 0;

		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			totalRequestCount += 2;
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			if (AccuWeatherProcessing.getLocationKey(context, latitude, longitude).isEmpty()) {
				totalRequestCount += 2;
			} else {
				totalRequestCount += 1;
			}
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			totalRequestCount += 1;
		}

		multipleJsonDownloader.setRequestCount(totalRequestCount);
		final LocalDateTime localDateTime = LocalDateTime.now();
		multipleJsonDownloader.put("localDateTime", localDateTime.toString());

		//request
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			KmaAreaCodesRepository kmaAreaCodesRepository = new KmaAreaCodesRepository(context);
			kmaAreaCodesRepository.getAreaCodes(latitude, longitude, new DbQueryCallback<List<KmaAreaCodeDto>>() {
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

					UltraSrtFcstParameter ultraSrtFcstParameter = new UltraSrtFcstParameter();
					VilageFcstParameter vilageFcstParameter = new VilageFcstParameter();
					ultraSrtFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());
					vilageFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());

					Call<JsonElement> ultraSrtFcstCall = KmaProcessing.getUltraSrtFcstData(ultraSrtFcstParameter,
							LocalDateTime.of(koreaLocalDateTime.toLocalDate(),
									koreaLocalDateTime.toLocalTime()), new JsonDownloader() {
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

					Call<JsonElement> vilageFcstCall = KmaProcessing.getVilageFcstData(vilageFcstParameter,
							LocalDateTime.of(koreaLocalDateTime.toLocalDate(),
									koreaLocalDateTime.toLocalTime()),
							new JsonDownloader() {
								@Override
								public void onResponseResult(Response<? extends JsonElement> response) {
									Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 성공");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
											RetrofitClient.ServiceType.VILAGE_FCST, response);
								}

								@Override
								public void onResponseResult(Throwable t) {
									Log.e(RetrofitClient.LOG_TAG, "kma vilage fcst 실패");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
											RetrofitClient.ServiceType.VILAGE_FCST, t);
								}
							});
				}

				@Override
				public void onResultNoData() {

				}
			});
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			GeoPositionSearchParameter geoPositionSearchParameter = new GeoPositionSearchParameter();
			TwelveHoursOfHourlyForecastsParameter twelveHoursOfHourlyForecastsParameter = new TwelveHoursOfHourlyForecastsParameter();

			geoPositionSearchParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString());
			final String locationKey = AccuWeatherProcessing.getLocationKey(context, latitude.toString(), longitude.toString());

			if (locationKey.isEmpty()) {
				Call<JsonElement> geoPositionSearchCall = AccuWeatherProcessing.getGeoPositionSearch(context,
						geoPositionSearchParameter, new JsonDownloader() {

							@Override
							public void onResponseResult(Response<? extends JsonElement> response) {
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, response);

								Gson gson = new Gson();
								GeoPositionResponse geoPositionResponse = gson.fromJson(response.body().toString(), GeoPositionResponse.class);

								final String newLocationKey = geoPositionResponse.getKey();
								twelveHoursOfHourlyForecastsParameter.setLocationKey(newLocationKey);

								Call<JsonElement> twelveHoursOfHourlyForecastsCall = AccuWeatherProcessing.get12HoursOfHourlyForecasts(
										twelveHoursOfHourlyForecastsParameter, new JsonDownloader() {
											@Override
											public void onResponseResult(Response<? extends JsonElement> response) {
												multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
														RetrofitClient.ServiceType.ACCU_12_HOURLY, response);

											}

											@Override
											public void onResponseResult(Throwable t) {
												multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
														RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
											}


										});

							}

							@Override
							public void onResponseResult(Throwable t) {
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, t);
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
							}

						});
			} else {
				twelveHoursOfHourlyForecastsParameter.setLocationKey(locationKey);

				Call<JsonElement> twelveHoursOfHourlyForecastsCall = AccuWeatherProcessing.get12HoursOfHourlyForecasts(
						twelveHoursOfHourlyForecastsParameter, new JsonDownloader() {
							@Override
							public void onResponseResult(Response<? extends JsonElement> response) {
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_12_HOURLY, response);

							}

							@Override
							public void onResponseResult(Throwable t) {
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
							}


						});
			}
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OneCallParameter oneCallParameter = new OneCallParameter();
			Set<OneCallParameter.OneCallApis> excludeOneCallApis = new ArraySet<>();
			excludeOneCallApis.add(OneCallParameter.OneCallApis.alerts);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.minutely);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.current);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.daily);
			oneCallParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString()).setOneCallApis(excludeOneCallApis);

			Call<JsonElement> oneCallCall = OpenWeatherMapProcessing.getOneCall(oneCallParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<? extends JsonElement> response) {
					Log.e(RetrofitClient.LOG_TAG, "own one call 성공");
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP,
							RetrofitClient.ServiceType.OWM_ONE_CALL, response);
				}

				@Override
				public void onResponseResult(Throwable t) {
					Log.e(RetrofitClient.LOG_TAG, "own one call 실패");
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP,
							RetrofitClient.ServiceType.OWM_ONE_CALL, t);
				}


			});
		}
	}

	public static void downloadDailyForecasts(Context context, Double latitude, Double longitude,
	                                          Set<WeatherSourceType> requestWeatherSourceTypeSet, MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		int totalRequestCount = 0;

		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			totalRequestCount += 2;
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			if (AccuWeatherProcessing.getLocationKey(context, latitude.toString(), longitude.toString()).isEmpty()) {
				totalRequestCount += 2;
			} else {
				totalRequestCount += 1;
			}
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			totalRequestCount += 1;
		}

		multipleJsonDownloader.setRequestCount(totalRequestCount);
		final LocalDateTime localDateTime = LocalDateTime.now();
		multipleJsonDownloader.put("localDateTime", localDateTime.toString());

		//request
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			KmaAreaCodesRepository kmaAreaCodesRepository = new KmaAreaCodesRepository(context);
			kmaAreaCodesRepository.getAreaCodes(latitude, longitude, new DbQueryCallback<List<KmaAreaCodeDto>>() {
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

					MidTaParameter midTaParameter = new MidTaParameter();
					MidLandParameter midLandParameter = new MidLandParameter();
					midLandParameter.setRegId(nearbyKmaAreaCodeDto.getMidLandFcstCode());
					midTaParameter.setRegId(nearbyKmaAreaCodeDto.getMidTaCode());

					LocalDateTime koreaLocalDateTime = LocalDateTime.now(ZoneId.of(TimeZone.getTimeZone("Asia/Seoul").getID()));
					multipleJsonDownloader.put("koreaLocalDateTime", koreaLocalDateTime.toString());

					String tmFc = KmaProcessing.getTmFc(LocalDateTime.of(koreaLocalDateTime.toLocalDate(),
							koreaLocalDateTime.toLocalTime()));
					multipleJsonDownloader.put("tmFc", tmFc);

					midLandParameter.setTmFc(tmFc);
					midTaParameter.setTmFc(tmFc);

					Call<JsonElement> midTaFcstCall = KmaProcessing.getMidTaData(midTaParameter, new JsonDownloader() {
						@Override
						public void onResponseResult(Response<? extends JsonElement> response) {
							Log.e(RetrofitClient.LOG_TAG, "kma mid ta 성공");
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
									RetrofitClient.ServiceType.MID_TA_FCST, response);
						}

						@Override
						public void onResponseResult(Throwable t) {
							Log.e(RetrofitClient.LOG_TAG, "kma mid ta 실패");
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
									RetrofitClient.ServiceType.MID_TA_FCST, t);
						}

					});

					Call<JsonElement> midLandFcstCall = KmaProcessing.getMidLandFcstData(midLandParameter,
							new JsonDownloader() {
								@Override
								public void onResponseResult(Response<? extends JsonElement> response) {
									Log.e(RetrofitClient.LOG_TAG, "kma mid land 성공");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
											RetrofitClient.ServiceType.MID_LAND_FCST, response);
								}

								@Override
								public void onResponseResult(Throwable t) {
									Log.e(RetrofitClient.LOG_TAG, "kma mid land 실패");
									multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.KMA,
											RetrofitClient.ServiceType.MID_LAND_FCST, t);
								}

							});
				}

				@Override
				public void onResultNoData() {

				}
			});
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			GeoPositionSearchParameter geoPositionSearchParameter = new GeoPositionSearchParameter();

			geoPositionSearchParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString());
			final String locationKey = AccuWeatherProcessing.getLocationKey(context, latitude.toString(), longitude.toString());

			if (locationKey.isEmpty()) {
				Call<JsonElement> geoPositionSearchCall = AccuWeatherProcessing.getGeoPositionSearch(context,
						geoPositionSearchParameter, new JsonDownloader() {

							@Override
							public void onResponseResult(Response<? extends JsonElement> response) {
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, response);

								Gson gson = new Gson();
								GeoPositionResponse geoPositionResponse = gson.fromJson(response.body().toString(), GeoPositionResponse.class);

								final String newLocationKey = geoPositionResponse.getKey();
								FiveDaysOfDailyForecastsParameter dailyForecastsParameter = new FiveDaysOfDailyForecastsParameter();
								dailyForecastsParameter.setLocationKey(newLocationKey);

								Call<JsonElement> dailyForecastCall = AccuWeatherProcessing.get5DaysOfDailyForecasts(dailyForecastsParameter,
										new JsonDownloader() {
											@Override
											public void onResponseResult(Response<? extends JsonElement> response) {
												multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
														RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, response);
											}

											@Override
											public void onResponseResult(Throwable t) {
												multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
														RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
											}

										});
							}

							@Override
							public void onResponseResult(Throwable t) {
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, t);
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
							}

						});
			} else {
				FiveDaysOfDailyForecastsParameter dailyForecastsParameter = new FiveDaysOfDailyForecastsParameter();
				dailyForecastsParameter.setLocationKey(locationKey);

				Call<JsonElement> dailyForecastCall = AccuWeatherProcessing.get5DaysOfDailyForecasts(dailyForecastsParameter,
						new JsonDownloader() {
							@Override
							public void onResponseResult(Response<? extends JsonElement> response) {
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, response);
							}

							@Override
							public void onResponseResult(Throwable t) {
								multipleJsonDownloader.processResult(WeatherSourceType.ACCU_WEATHER,
										RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY, t);
							}

						});
			}
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OneCallParameter oneCallParameter = new OneCallParameter();
			Set<OneCallParameter.OneCallApis> excludeOneCallApis = new ArraySet<>();
			excludeOneCallApis.add(OneCallParameter.OneCallApis.alerts);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.minutely);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.current);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.hourly);
			oneCallParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString()).setOneCallApis(excludeOneCallApis);

			Call<JsonElement> oneCallCall = OpenWeatherMapProcessing.getOneCall(oneCallParameter, new JsonDownloader() {
				@Override
				public void onResponseResult(Response<? extends JsonElement> response) {
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP,
							RetrofitClient.ServiceType.OWM_ONE_CALL, response);
				}

				@Override
				public void onResponseResult(Throwable t) {
					multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP,
							RetrofitClient.ServiceType.OWM_ONE_CALL, t);
				}


			});
		}
	}
}