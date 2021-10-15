package com.lifedawn.bestweather.weathers.dataprocessing.request;

import android.content.Context;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;
import com.lifedawn.bestweather.room.repository.KmaAreaCodesRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class MainProcessing {
	public enum WeatherSourceType {
		ACCU_WEATHER, KMA, MET_NORWAY, OPEN_WEATHER_MAP, AQICN
	}
	
	public static void downloadWeatherData(Context context, final String latitude, final String longitude,
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
}
