package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.AirQualityForecastObj;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

	public static AqiCnGeolocalizedFeedResponse getAirQualityObjFromJson(String response) {
		return new Gson().fromJson(response, AqiCnGeolocalizedFeedResponse.class);
	}

	public static List<AirQualityForecastObj> getAirQualityForecastObjList(AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse, ZoneId timeZone) {
		ArrayMap<String, AirQualityForecastObj> forecastObjMap = new ArrayMap<>();
		final LocalDate todayDate = LocalDate.now(timeZone);
		LocalDate date = null;

		List<AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm10Forecast = aqiCnGeolocalizedFeedResponse.getData().getForecast().getDaily().getPm10();
		for (AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm10Forecast) {
			date = getDate(valueMap.getDay());
			if (date.isBefore(todayDate)) {
				continue;
			}

			if (!forecastObjMap.containsKey(valueMap.getDay())) {
				forecastObjMap.put(valueMap.getDay(), new AirQualityForecastObj(date));
			}
			forecastObjMap.get(valueMap.getDay()).pm10 = Integer.parseInt(valueMap.getAvg());
		}

		List<AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm25Forecast = aqiCnGeolocalizedFeedResponse.getData().getForecast().getDaily().getPm25();
		for (AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm25Forecast) {
			date = getDate(valueMap.getDay());
			if (date.isBefore(todayDate)) {
				continue;
			}

			if (!forecastObjMap.containsKey(valueMap.getDay())) {
				forecastObjMap.put(valueMap.getDay(), new AirQualityForecastObj(date));
			}
			forecastObjMap.get(valueMap.getDay()).pm25 = Integer.parseInt(valueMap.getAvg());
		}

		List<AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> o3Forecast = aqiCnGeolocalizedFeedResponse.getData().getForecast().getDaily().getO3();
		for (AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : o3Forecast) {
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

	public static String getAirQuality(Context context, AqiCnGeolocalizedFeedResponse airQualityResponse) {
		if (airQualityResponse != null) {
			if (airQualityResponse.getStatus().equals("ok")) {
				AqiCnGeolocalizedFeedResponse.Data.IAqi iAqi = airQualityResponse.getData().getIaqi();
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

	public static boolean successfulResponse(MultipleRestApiDownloader.ResponseResult result) {
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

	public static AirQualityDto makeAirQualityDto(Context context, AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse, ZoneOffset zoneOffset) {
		AirQualityDto airQualityDto = new AirQualityDto();

		if (aqiCnGeolocalizedFeedResponse == null) {
			airQualityDto.setAqi(-1).setSuccessful(false);
		} else {
			if (aqiCnGeolocalizedFeedResponse.getStatus().equals("ok")) {
				airQualityDto.setSuccessful(true);
				AqiCnGeolocalizedFeedResponse.Data data = aqiCnGeolocalizedFeedResponse.getData();

				if (zoneOffset == null) {
					zoneOffset = ZoneOffset.of(data.getTime().getTz());
				}
				//-----------------time----
				AirQualityDto.Time time = new AirQualityDto.Time();
				time.setS(data.getTime().getS());
				time.setTz(data.getTime().getTz());
				time.setV(data.getTime().getV());
				time.setIso(data.getTime().getIso());

				airQualityDto.setAqi((int) Double.parseDouble(data.getAqi()));
				airQualityDto.setIdx(Integer.parseInt(data.getIdx()));
				airQualityDto.setTimeInfo(time);
				airQualityDto.setLatitude(Double.parseDouble(data.getCity().getGeo().get(0)));
				airQualityDto.setLongitude(Double.parseDouble(data.getCity().getGeo().get(1)));

				airQualityDto.setCityName(data.getCity().getName());
				airQualityDto.setAqiCnUrl(data.getCity().getUrl());
				airQualityDto.setTime(ZonedDateTime.parse(data.getTime().getIso()));

				//------------------Current------------------------------------------------------------------------------
				AirQualityDto.Current current = new AirQualityDto.Current();
				airQualityDto.setCurrent(current);
				AqiCnGeolocalizedFeedResponse.Data.IAqi iAqi = data.getIaqi();

				current.setPm10(iAqi.getPm10() != null ? (int) Double.parseDouble(iAqi.getPm10().getValue()) : -1);
				current.setPm25(iAqi.getPm25() != null ? (int) Double.parseDouble(iAqi.getPm25().getValue()) : -1);
				current.setDew(iAqi.getDew() != null ? (int) Double.parseDouble(iAqi.getDew().getValue()) : -1);
				current.setCo(iAqi.getCo() != null ? (int) Double.parseDouble(iAqi.getCo().getValue()) : -1);
				current.setSo2(iAqi.getSo2() != null ? (int) Double.parseDouble(iAqi.getSo2().getValue()) : -1);
				current.setNo2(iAqi.getNo2() != null ? (int) Double.parseDouble(iAqi.getNo2().getValue()) : -1);
				current.setO3(iAqi.getO3() != null ? (int) Double.parseDouble(iAqi.getO3().getValue()) : -1);

				//---------- dailyforecast-----------------------------------------------------------------------
				ArrayMap<String, AirQualityDto.DailyForecast> forecastArrMap = new ArrayMap<>();

				final ZonedDateTime todayDate = ZonedDateTime.now(zoneOffset);
				ZonedDateTime date = ZonedDateTime.of(todayDate.toLocalDateTime(), zoneOffset);
				LocalDate localDate = null;

				AqiCnGeolocalizedFeedResponse.Data.Forecast forecast = data.getForecast();

				List<AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm10Forecast = forecast.getDaily().getPm10();
				for (AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm10Forecast) {
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

				List<AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> pm25Forecast = forecast.getDaily().getPm25();
				for (AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : pm25Forecast) {
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

				List<AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> o3Forecast = forecast.getDaily().getO3();
				for (AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : o3Forecast) {
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

				List<AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap> uviForecast =
						aqiCnGeolocalizedFeedResponse.getData().getForecast().getDaily().getUvi();
				for (AqiCnGeolocalizedFeedResponse.Data.Forecast.Daily.ValueMap valueMap : uviForecast) {
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
				forecastObjArr = forecastArrMap.values().toArray(forecastObjArr);

				List<AirQualityDto.DailyForecast> dailyForecastList = new ArrayList<>();
				for (AirQualityDto.DailyForecast dailyForecast : forecastObjArr) {
					dailyForecastList.add(dailyForecast);
				}

				Collections.sort(dailyForecastList, new Comparator<AirQualityDto.DailyForecast>() {
					@Override
					public int compare(AirQualityDto.DailyForecast forecastObj, AirQualityDto.DailyForecast t1) {
						return forecastObj.getDate().compareTo(t1.getDate());
					}
				});

				airQualityDto.setDailyForecastList(dailyForecastList);
			} else {
				airQualityDto.setAqi(-1).setSuccessful(false);
			}
		}
		return airQualityDto;

	}

	public static AirQualityDto parseTextToAirQualityDto(Context context, JsonObject jsonObject) {
		if (jsonObject.get(WeatherProviderType.AQICN.name()) != null) {
			JsonObject aqiCnObject = jsonObject.getAsJsonObject(WeatherProviderType.AQICN.name());
			AqiCnGeolocalizedFeedResponse aqiCnGeolocalizedFeedResponse =
					getAirQualityObjFromJson(aqiCnObject.get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED.name()).getAsString());
			AirQualityDto airQualityDto = makeAirQualityDto(context, aqiCnGeolocalizedFeedResponse,
					ZoneOffset.of(jsonObject.get("zoneOffset").getAsString()));
			return airQualityDto;
		}

		return null;
	}
}
