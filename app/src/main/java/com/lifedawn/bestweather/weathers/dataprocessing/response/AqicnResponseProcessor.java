package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.AirQualityForecastObj;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

	/**
	 * grade가 -1이면 정보없음을 뜻함
	 *
	 * @param grade
	 * @return
	 */
	public static String getGradeDescription(int grade) {
		/*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
		if (grade == -1) {
			return "?";
		}

		for (int i = 0; i < AQI_GRADES.length; i++) {
			if (grade <= AQI_GRADES[i]) {
				return AQI_GRADE_DESCRIPTIONS[i];
			}
		}
		//if hazardous
		return AQI_GRADE_DESCRIPTIONS[5];
	}

	public static GeolocalizedFeedResponse getAirQualityObjFromJson(String response) {
		return new Gson().fromJson(response, GeolocalizedFeedResponse.class);
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

	public static String getAirQuality(Context context, GeolocalizedFeedResponse airQualityResponse) {
		if (airQualityResponse != null) {
			if (airQualityResponse.getStatus().equals("ok")) {
				GeolocalizedFeedResponse.Data.IAqi iAqi = airQualityResponse.getData().getIaqi();
				int val = Integer.MIN_VALUE;

				if (iAqi.getO3() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getO3().getValue()));
				}
				if (iAqi.getPm25() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getPm25().getValue()));
				}
				if (iAqi.getPm10() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getPm10().getValue()));
				}
				if (iAqi.getNo2() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getNo2().getValue()));
				}
				if (iAqi.getSo2() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getSo2().getValue()));
				}
				if (iAqi.getCo() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getCo().getValue()));
				}
				if (iAqi.getDew() != null) {
					val = Math.max(val, (int) Double.parseDouble(iAqi.getDew().getValue()));
				}

				if (val == Integer.MIN_VALUE) {
					return context.getString(R.string.noData);
				} else {
					return AqicnResponseProcessor.getGradeDescription(val);
				}
			} else {
				return context.getString(R.string.noData);

			}
		} else {
			return context.getString(R.string.noData);

		}
	}

	private static LocalDate getDate(String day) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return LocalDate.parse(day, dateTimeFormatter);
	}

	public static boolean successfulResponse(MultipleJsonDownloader.ResponseResult result) {
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

	public static AirQualityDto makeAirQualityDto(Context context, GeolocalizedFeedResponse geolocalizedFeedResponse, ZoneOffset zoneOffset) {
		GeolocalizedFeedResponse.Data data = geolocalizedFeedResponse.getData();
		AirQualityDto airQualityDto = new AirQualityDto();
		airQualityDto.setAqi((int) Double.parseDouble(data.getAqi()));
		airQualityDto.setIdx(Integer.parseInt(data.getIdx()));

		airQualityDto.setCityName(data.getCity().getName());
		airQualityDto.setAqiCnUrl(data.getCity().getUrl());
		airQualityDto.setTime(ZonedDateTime.parse(data.getTime().getIso()));

		//------------------Current------------------------------------------------------------------------------
		AirQualityDto.Current current = new AirQualityDto.Current();
		airQualityDto.setCurrent(current);
		GeolocalizedFeedResponse.Data.IAqi iAqi = data.getIaqi();

		if (iAqi.getPm10() != null) {
			current.setPm10((int) Double.parseDouble(iAqi.getPm10().getValue()));
		}
		if (iAqi.getPm25() != null) {
			current.setPm25((int) Double.parseDouble(iAqi.getPm25().getValue()));
		}
		if (iAqi.getDew() != null) {
			current.setDew((int) Double.parseDouble(iAqi.getDew().getValue()));
		}
		if (iAqi.getCo() != null) {
			current.setCo((int) Double.parseDouble(iAqi.getCo().getValue()));
		}
		if (iAqi.getSo2() != null) {
			current.setSo2((int) Double.parseDouble(iAqi.getSo2().getValue()));
		}
		if (iAqi.getNo2() != null) {
			current.setNo2((int) Double.parseDouble(iAqi.getNo2().getValue()));
		}
		if (iAqi.getO3() != null) {
			current.setO3((int) Double.parseDouble(iAqi.getO3().getValue()));
		}

		//---------- dailyforecast-----------------------------------------------------------------------
		ArrayMap<String, AirQualityDto.DailyForecast> forecastArrMap = new ArrayMap<>();
		final ZonedDateTime todayDate = ZonedDateTime.now(zoneOffset);
		ZonedDateTime date = ZonedDateTime.of(todayDate.toLocalDateTime(), zoneOffset);
		LocalDate localDate = null;

		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm10Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getPm10();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm10Forecast) {
			localDate = getDate(valueMap.getDay());
			date = date.withYear(localDate.getYear()).withMonth(localDate.getMonthValue()).withDayOfMonth(localDate.getDayOfMonth());

			if (date.isBefore(todayDate)) {
				continue;
			}
			if (!forecastArrMap.containsKey(valueMap.getDay())) {
				AirQualityDto.DailyForecast dailyForecast = new AirQualityDto.DailyForecast();
				dailyForecast.setDate(date);
				forecastArrMap.put(valueMap.getDay(), dailyForecast);
			}
			AirQualityDto.DailyForecast.Val pm10 = new AirQualityDto.DailyForecast.Val();
			pm10.setAvg((int) Double.parseDouble(valueMap.getAvg()));
			pm10.setMax((int) Double.parseDouble(valueMap.getMax()));
			pm10.setMin((int) Double.parseDouble(valueMap.getMin()));

			forecastArrMap.get(valueMap.getDay()).setPm10(pm10);
		}

		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm25Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getPm25();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm25Forecast) {
			localDate = getDate(valueMap.getDay());
			date = date.withYear(localDate.getYear()).withMonth(localDate.getMonthValue()).withDayOfMonth(localDate.getDayOfMonth());

			if (date.isBefore(todayDate)) {
				continue;
			}
			if (!forecastArrMap.containsKey(valueMap.getDay())) {
				AirQualityDto.DailyForecast dailyForecast = new AirQualityDto.DailyForecast();
				dailyForecast.setDate(date);
				forecastArrMap.put(valueMap.getDay(), dailyForecast);
			}
			AirQualityDto.DailyForecast.Val pm25 = new AirQualityDto.DailyForecast.Val();
			pm25.setAvg((int) Double.parseDouble(valueMap.getAvg()));
			pm25.setMax((int) Double.parseDouble(valueMap.getMax()));
			pm25.setMin((int) Double.parseDouble(valueMap.getMin()));

			forecastArrMap.get(valueMap.getDay()).setPm25(pm25);
		}

		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> o3Forecast = geolocalizedFeedResponse.getData().getForecast().getDaily().getO3();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : o3Forecast) {
			localDate = getDate(valueMap.getDay());
			date = date.withYear(localDate.getYear()).withMonth(localDate.getMonthValue()).withDayOfMonth(localDate.getDayOfMonth());

			if (date.isBefore(todayDate)) {
				continue;
			}
			if (!forecastArrMap.containsKey(valueMap.getDay())) {
				AirQualityDto.DailyForecast dailyForecast = new AirQualityDto.DailyForecast();
				dailyForecast.setDate(date);
				forecastArrMap.put(valueMap.getDay(), dailyForecast);
			}
			AirQualityDto.DailyForecast.Val o3 = new AirQualityDto.DailyForecast.Val();
			o3.setAvg((int) Double.parseDouble(valueMap.getAvg()));
			o3.setMax((int) Double.parseDouble(valueMap.getMax()));
			o3.setMin((int) Double.parseDouble(valueMap.getMin()));

			forecastArrMap.get(valueMap.getDay()).setO3(o3);
		}

		List<GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> uviForecast =
				geolocalizedFeedResponse.getData().getForecast().getDaily().getUvi();
		for (GeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : uviForecast) {
			localDate = getDate(valueMap.getDay());
			date = date.withYear(localDate.getYear()).withMonth(localDate.getMonthValue()).withDayOfMonth(localDate.getDayOfMonth());

			if (date.isBefore(todayDate)) {
				continue;
			}
			if (!forecastArrMap.containsKey(valueMap.getDay())) {
				AirQualityDto.DailyForecast dailyForecast = new AirQualityDto.DailyForecast();
				dailyForecast.setDate(date);
				forecastArrMap.put(valueMap.getDay(), dailyForecast);
			}
			AirQualityDto.DailyForecast.Val uvi = new AirQualityDto.DailyForecast.Val();
			uvi.setAvg((int) Double.parseDouble(valueMap.getAvg()));
			uvi.setMax((int) Double.parseDouble(valueMap.getMax()));
			uvi.setMin((int) Double.parseDouble(valueMap.getMin()));

			forecastArrMap.get(valueMap.getDay()).setUvi(uvi);
		}

		AirQualityDto.DailyForecast[] forecastObjArr = new AirQualityDto.DailyForecast[1];
		List<AirQualityDto.DailyForecast> dailyForecastList = Arrays.asList(forecastArrMap.values().toArray(forecastObjArr));
		Collections.sort(dailyForecastList, new Comparator<AirQualityDto.DailyForecast>() {
			@Override
			public int compare(AirQualityDto.DailyForecast forecastObj, AirQualityDto.DailyForecast t1) {
				return forecastObj.getDate().compareTo(t1.getDate());
			}
		});

		airQualityDto.setDailyForecastList(dailyForecastList);
		return airQualityDto;
	}

	public static AirQualityDto parseTextToAirQualityDto(Context context, JsonObject jsonObject) {
		if (jsonObject.get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name()) != null) {
			GeolocalizedFeedResponse geolocalizedFeedResponse =
					getAirQualityObjFromJson(jsonObject.get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name()).getAsString());
			AirQualityDto airQualityDto = makeAirQualityDto(context, geolocalizedFeedResponse,
					ZoneOffset.of(jsonObject.get("zoneOffset").getAsString()));
			return airQualityDto;
		}

		return null;
	}
}
