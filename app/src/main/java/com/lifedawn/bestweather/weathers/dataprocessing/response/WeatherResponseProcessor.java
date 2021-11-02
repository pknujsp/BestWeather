package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;

import com.lifedawn.bestweather.R;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class WeatherResponseProcessor {
	private static Map<String, String> windStrengthDescriptionMap = new HashMap<>();
	private static Map<String, String> windStrengthDescriptionSimpleMap = new HashMap<>();

	public static void init(Context context) {
		windStrengthDescriptionMap.clear();
		windStrengthDescriptionSimpleMap.clear();

		windStrengthDescriptionMap.put("1", context.getString(R.string.wind_strength_1));
		windStrengthDescriptionMap.put("2", context.getString(R.string.wind_strength_2));
		windStrengthDescriptionMap.put("3", context.getString(R.string.wind_strength_3));
		windStrengthDescriptionMap.put("4", context.getString(R.string.wind_strength_4));

		windStrengthDescriptionSimpleMap.put("1", context.getString(R.string.wind_strength_1_simple));
		windStrengthDescriptionSimpleMap.put("2", context.getString(R.string.wind_strength_2_simple));
		windStrengthDescriptionSimpleMap.put("3", context.getString(R.string.wind_strength_3_simple));
		windStrengthDescriptionSimpleMap.put("4", context.getString(R.string.wind_strength_4_simple));
	}

	public static String getWindSpeedDescription(String windSpeed) {
		double speed = Double.parseDouble(windSpeed);

		if (speed >= 14) {
			return windStrengthDescriptionMap.get("4");
		} else if (speed >= 9) {
			return windStrengthDescriptionMap.get("3");
		} else if (speed >= 4) {
			return windStrengthDescriptionMap.get("2");
		} else {
			return windStrengthDescriptionMap.get("1");
		}
	}

	public static String getSimpleWindSpeedDescription(String windSpeed) {
		double speed = Double.parseDouble(windSpeed);

		if (speed >= 14) {
			return windStrengthDescriptionSimpleMap.get("4");
		} else if (speed >= 9) {
			return windStrengthDescriptionSimpleMap.get("3");
		} else if (speed >= 4) {
			return windStrengthDescriptionSimpleMap.get("2");
		} else {
			return windStrengthDescriptionSimpleMap.get("1");
		}
	}

	public static LocalDateTime convertDateTimeOfDailyForecast(long millis, TimeZone timeZone) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of(timeZone.getID())).withHour(0).withMinute(0).withSecond(0).withNano(0);
	}


	public static LocalDateTime convertDateTimeOfHourlyForecast(long millis, TimeZone timeZone) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of(timeZone.getID())).withMinute(0).withSecond(0).withNano(0);
	}
}
