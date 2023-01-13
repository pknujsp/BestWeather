package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SunRiseSetUtil {
	private SunRiseSetUtil() {
	}

	public static Map<Integer, SunRiseSetObj> getDailySunRiseSetMap(ZonedDateTime begin, ZonedDateTime end, double latitude,
	                                                                double longitude) {
		final ZoneId zoneId = begin.getZone();
		final ZoneId utc0TimeZone = ZoneId.of(TimeZone.getTimeZone("UTC").getID());
		final TimeZone realTimeZone = TimeZone.getTimeZone(zoneId.getId());

		ZonedDateTime beginUtc0ZonedDateTime = ZonedDateTime.of(begin.toLocalDateTime(), zoneId);
		ZonedDateTime beginRealZonedDateTime = ZonedDateTime.of(begin.toLocalDateTime(), zoneId);
		ZonedDateTime endUtc0ZonedDateTime = ZonedDateTime.of(end.toLocalDateTime(), zoneId);

		beginUtc0ZonedDateTime = beginUtc0ZonedDateTime.withZoneSameLocal(utc0TimeZone);
		endUtc0ZonedDateTime = endUtc0ZonedDateTime.withZoneSameLocal(utc0TimeZone);

		long beginDay;
		final long endDay = TimeUnit.MILLISECONDS.toDays(endUtc0ZonedDateTime.toInstant().toEpochMilli());

		Map<Integer, SunRiseSetObj> map = new HashMap<>();
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location(latitude, longitude), realTimeZone);
		Calendar calendar = Calendar.getInstance(realTimeZone);

		do {
			calendar.setTimeInMillis(beginRealZonedDateTime.toInstant().toEpochMilli());
			map.put(beginRealZonedDateTime.getDayOfYear(), new SunRiseSetObj(calculator.getOfficialSunriseCalendarForDate(calendar),
					calculator.getOfficialSunsetCalendarForDate(calendar)));

			beginUtc0ZonedDateTime = beginUtc0ZonedDateTime.plusDays(1);
			beginRealZonedDateTime = beginRealZonedDateTime.plusDays(1);
			beginDay = TimeUnit.MILLISECONDS.toDays(beginUtc0ZonedDateTime.toInstant().toEpochMilli());
		} while (beginDay <= endDay);

		return map;
	}

	public static boolean isNight(Calendar compDate, Calendar sunRiseDate, Calendar sunSetDate) {
		long compH = TimeUnit.MILLISECONDS.toHours(compDate.getTimeInMillis());
		long sunRiseH = TimeUnit.MILLISECONDS.toHours(sunRiseDate.getTimeInMillis());
		long sunSetH = TimeUnit.MILLISECONDS.toHours(sunSetDate.getTimeInMillis());
		if (compH > sunSetH || compH < sunRiseH) {
			return true;
		} else
			return false;
	}

	public static class SunRiseSetObj {
		ZonedDateTime zonedDateTime;
		final Calendar sunrise;
		final Calendar sunset;

		public SunRiseSetObj(Calendar sunrise, Calendar sunset) {
			this.sunrise = sunrise;
			this.sunset = sunset;
		}

		public SunRiseSetObj(ZonedDateTime zonedDateTime, Calendar sunrise, Calendar sunset) {
			this.zonedDateTime = zonedDateTime;
			this.sunrise = sunrise;
			this.sunset = sunset;
		}

		public Calendar getSunrise() {
			return sunrise;
		}

		public Calendar getSunset() {
			return sunset;
		}

		public ZonedDateTime getZonedDateTime() {
			return zonedDateTime;
		}
	}
}
