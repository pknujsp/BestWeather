package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;
import android.util.ArraySet;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.CurrentConditionsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.FiveDaysOfDailyForecastsParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.GeoPositionSearchParameter;
import com.lifedawn.bestweather.retrofit.parameters.accuweather.TwelveHoursOfHourlyForecastsParameter;
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
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

public class MainProcessing {
	public enum WeatherSourceType implements Serializable {
		ACCU_WEATHER, KMA, MET_NORWAY, OPEN_WEATHER_MAP, AQICN
	}
	
	public static void downloadAllWeatherData(Context context, final String latitude, final String longitude,
			final Set<WeatherSourceType> weatherSourceTypeSet, MultipleJsonDownloader<JsonElement> multipleJsonDownloader) {
		int totalRequestCount = 0;
		
		if (weatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			totalRequestCount += 4;
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
		
		multipleJsonDownloader.setRequestCount(totalRequestCount);
		
		if (weatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.getAccuWeatherForecasts(latitude, longitude, null, multipleJsonDownloader);
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
							final Calendar calendar = Calendar.getInstance();
							multipleJsonDownloader.put("calendar", String.valueOf(calendar.getTimeInMillis()));
							KmaProcessing.getKmaForecasts(nearbyKmaAreaCodeDto, calendar, multipleJsonDownloader);
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
			totalRequestCount += 2;
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			totalRequestCount += 1;
		}
		
		multipleJsonDownloader.setRequestCount(totalRequestCount);
		
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
					Calendar calendar = Calendar.getInstance();
					multipleJsonDownloader.put("calendar", String.valueOf(calendar.getTimeInMillis()));
					
					UltraSrtFcstParameter ultraSrtFcstParameter = new UltraSrtFcstParameter();
					VilageFcstParameter vilageFcstParameter = new VilageFcstParameter();
					ultraSrtFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());
					vilageFcstParameter.setNx(nearbyKmaAreaCodeDto.getX()).setNy(nearbyKmaAreaCodeDto.getY());
					
					Call<JsonElement> ultraSrtFcstCall = KmaProcessing.getUltraSrtFcstData(ultraSrtFcstParameter,
							(Calendar) calendar.clone(), new JsonDownloader<JsonElement>() {
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
					
					Call<JsonElement> vilageFcstCall = KmaProcessing.getVilageFcstData(vilageFcstParameter, (Calendar) calendar.clone(),
							new JsonDownloader<JsonElement>() {
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
			Call<JsonElement> geoPositionSearchCall = AccuWeatherProcessing.getGeoPositionSearch(geoPositionSearchParameter,
					new JsonDownloader<JsonElement>() {
						
						@Override
						public void onResponseResult(Response<? extends JsonElement> response) {
							Log.e(RetrofitClient.LOG_TAG, "accu weather geoposition search 성공");
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, response);
							
							Gson gson = new Gson();
							GeoPositionResponse geoPositionResponse = gson.fromJson(response.body().toString(), GeoPositionResponse.class);
							
							final String locationKey = geoPositionResponse.getKey();
							twelveHoursOfHourlyForecastsParameter.setLocationKey(locationKey);
							
							Call<JsonElement> twelveHoursOfHourlyForecastsCall = AccuWeatherProcessing.get12HoursOfHourlyForecasts(
									twelveHoursOfHourlyForecastsParameter, new JsonDownloader<JsonElement>() {
										@Override
										public void onResponseResult(Response<? extends JsonElement> response) {
											Log.e(RetrofitClient.LOG_TAG, "accu weather hourly forecast 성공");
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_12_HOURLY, response);
											
										}
										
										@Override
										public void onResponseResult(Throwable t) {
											Log.e(RetrofitClient.LOG_TAG, "accu weather hourly forecast 실패");
											multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
													RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
										}
										
										
									});
							
						}
						
						@Override
						public void onResponseResult(Throwable t) {
							Log.e(RetrofitClient.LOG_TAG, "accu weather geocoding search 실패");
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH, t);
							multipleJsonDownloader.processResult(MainProcessing.WeatherSourceType.ACCU_WEATHER,
									RetrofitClient.ServiceType.ACCU_12_HOURLY, t);
						}
						
					});
		}
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OneCallParameter oneCallParameter = new OneCallParameter();
			Set<OneCallParameter.OneCallApis> excludeOneCallApis = new ArraySet<>();
			excludeOneCallApis.add(OneCallParameter.OneCallApis.alerts);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.minutely);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.current);
			excludeOneCallApis.add(OneCallParameter.OneCallApis.daily);
			oneCallParameter.setLatitude(latitude.toString()).setLongitude(longitude.toString()).setOneCallApis(excludeOneCallApis);
			
			Call<JsonElement> oneCallCall = OpenWeatherMapProcessing.getOneCall(oneCallParameter, new JsonDownloader<JsonElement>() {
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
}