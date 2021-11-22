package com.lifedawn.bestweather.weathers.dataprocessing.util;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SunRiseSetUtil {
	private SunRiseSetUtil() {
	}

	public static Map<Integer, SunRiseSetObj> getDailySunRiseSetMap(ZonedDateTime begin, ZonedDateTime end, double latitude,
	                                                                double longitude) {
		ZonedDateTime criteria = ZonedDateTime.of(begin.toLocalDateTime(), begin.getZone());
		int beginDay = criteria.getDayOfYear();
		final int endDay = end.getDayOfYear();

		Map<Integer, SunRiseSetObj> map = new HashMap<>();

		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location(latitude, longitude),
				TimeZone.getTimeZone(begin.getZone().getId()));

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(begin.getZone().getId()));

		do {
			calendar.setTimeInMillis(criteria.toInstant().toEpochMilli());

			map.put(beginDay, new SunRiseSetObj(calculator.getOfficialSunriseCalendarForDate(calendar), calculator.getOfficialSunsetCalendarForDate(calendar)));

			criteria = criteria.plusDays(1);
			beginDay = criteria.getDayOfYear();
		} while (beginDay != endDay);

		return map;
	}

	public static boolean isNight(Date compDate, Date sunRiseDate, Date sunSetDate) {
		if (compDate.before(sunRiseDate)) {
			return true;
		} else if (compDate.before(sunSetDate)) {
			return false;
		} else {
			return true;
		}
	}

	public static class SunRiseSetObj {
		final Calendar sunrise;
		final Calendar sunset;

		public SunRiseSetObj(Calendar sunrise, Calendar sunset) {
			this.sunrise = sunrise;
			this.sunset = sunset;
		}

		public Calendar getSunrise() {
			return sunrise;
		}

		public Calendar getSunset() {
			return sunset;
		}
	}
}