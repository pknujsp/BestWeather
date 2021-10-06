package com.lifedawn.bestweather.weathers.dataprocessing;

import android.content.Context;

import com.google.gson.JsonObject;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidLandParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.MidTaParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtFcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;
import com.lifedawn.bestweather.room.repository.KmaAreaCodesRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.util.LocationDistance;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class MainProcessing {
	public enum WeatherSourceType {
		ACCU_WEATHER, KMA, MET_NORWAY, OPEN_WEATHER_MAP
	}

	public static void downloadWeatherData(Context context, final String latitude, final String longitude,
	                                       final Set<WeatherSourceType> weatherSourceTypeSet) {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));

		if (weatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			AccuWeatherProcessing.getAccuWeatherForecasts(latitude, longitude, null, new MultipleJsonDownloader<JsonObject>(4) {
				@Override
				public void onResult() {

				}
			});
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

							KmaProcessing.getKmaForecasts(nearbyKmaAreaCodeDto, (Calendar) calendar.clone(),
									new MultipleJsonDownloader<JsonObject>(5) {
										@Override
										public void onResult() {

										}
									});
						}

						@Override
						public void onResultNoData() {

						}
					});


		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.MET_NORWAY)) {
			MetNorwayProcessing.getMetNorwayForecasts(latitude, longitude, new MultipleJsonDownloader<JsonObject>(1) {
				@Override
				public void onResult() {

				}
			});
		}
		if (weatherSourceTypeSet.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
			OpenWeatherMapProcessing.getOwmForecasts(latitude, longitude, true, new MultipleJsonDownloader<JsonObject>(1) {
				@Override
				public void onResult() {

				}
			});
		}


	}
}
