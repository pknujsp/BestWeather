package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.AirQualityForecastObj;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Response;

public class AqicnResponseProcessor {
	private static final int[] AQI_GRADES = new int[5];
	private static final int[] AQI_GRADE_COLORS = new int[6];
	private static final String[] AQI_GRADE_DESCRIPTIONS = new String[6];

	private AqicnResponseProcessor() {
	}

	public static void init(Context context) {
		int[] aqiGrades = context.getResources().getIntArray(R.array.AqiGrades);
		int[] aqiGradeColors = context.getResources().getIntArray(R.array.AqiGradeColors);
		String[] aqiGradeDescriptions = context.getResources().getStringArray(R.array.AqiGradeState);

		for (int i = 0; i < aqiGrades.length; i++) {
			AQI_GRADES[i] = aqiGrades[i];
		}
		for (int i = 0; i < aqiGradeColors.length; i++) {
			AQI_GRADE_COLORS[i] = aqiGradeColors[i];
		}
		for (int i = 0; i < aqiGradeDescriptions.length; i++) {
			AQI_GRADE_DESCRIPTIONS[i] = aqiGradeDescriptions[i];
		}

	}

	public static int getGradeColorId(int grade) {
		/*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
		for (int i = 0; i < AQI_GRADES.length; i++) {
			if (grade <= AQI_GRADES[i]) {
				return AQI_GRADE_COLORS[i];
			}
		}
		//if hazardous
		return AQI_GRADE_COLORS[5];
	}

	public static String getGradeDescription(int grade) {
		/*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
		for (int i = 0; i < AQI_GRADES.length; i++) {
			if (grade <= AQI_GRADES[i]) {
				return AQI_GRADE_DESCRIPTIONS[i];
			}
		}
		//if hazardous
		return AQI_GRADE_DESCRIPTIONS[5];
	}

	public static GeolocalizedFeedResponse getAirQualityObjFromJson(Response<JsonElement> response) {
		return new Gson().fromJson(response.body().toString(), GeolocalizedFeedResponse.class);
	}

	public static List<AirQualityForecastObj> getAirQualityForecastObjList(GeolocalizedFeedResponse geolocalizedFeedResponse, ZoneId timeZone) {
		ArrayMap<String, AirQualityForecastObj> forecastObjMap = new ArrayMap<>();
		final LocalDate todayDate = LocalDate.now(timeZone);
		LocalDate date = null;

		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm10Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getPm10();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm10Forecast) {
			date = getDate(valueMap.getDay());
			if (date.isBefore(todayDate)) {
				continue;
			}

			if (!forecastObjMap.containsKey(valueMap.getDay())) {
				forecastObjMap.put(valueMap.getDay(), new AirQualityForecastObj(date));
			}
			forecastObjMap.get(valueMap.getDay()).pm10 = Integer.parseInt(valueMap.getAvg());
		}

		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm25Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getPm25();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm25Forecast) {
			date = getDate(valueMap.getDay());
			if (date.isBefore(todayDate)) {
				continue;
			}

			if (!forecastObjMap.containsKey(valueMap.getDay())) {
				forecastObjMap.put(valueMap.getDay(), new AirQualityForecastObj(date));
			}
			forecastObjMap.get(valueMap.getDay()).pm25 = Integer.parseInt(valueMap.getAvg());
		}

		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> o3Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getO3();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : o3Forecast) {
			date = getDate(valueMap.getDay());
			if (date.isBefore(todayDate)) {
				continue;
			}

			if (!forecastObjMap.containsKey(valueMap.getDay())) {
				forecastObjMap.put(valueMap.getDay(), new AirQualityForecastObj(date));
			}
			forecastObjMap.get(valueMap.getDay()).o3 = Integer.parseInt(valueMap.getAvg());
		}

		AirQualityForecastObj[] forecastObjArr = new AirQualityForecastObj[1];
		List<AirQualityForecastObj> forecastObjList = Arrays.asList(forecastObjMap.values().toArray(forecastObjArr));
		Collections.sort(forecastObjList, new Comparator<AirQualityForecastObj>() {
			@Override
			public int compare(AirQualityForecastObj forecastObj, AirQualityForecastObj t1) {
				return forecastObj.date.compareTo(t1.date);
			}
		});

		return forecastObjList;
	}

	private static LocalDate getDate(String day) {
		LocalDate date = null;
		try {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			date = LocalDate.parse(day, dateTimeFormatter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

	public static boolean successfulResponse(MultipleJsonDownloader.ResponseResult<JsonElement> result) {
		if (result.getResponse() != null) {
			Response<JsonElement> response = (Response<JsonElement>) result.getResponse();

			if (response.isSuccessful()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
