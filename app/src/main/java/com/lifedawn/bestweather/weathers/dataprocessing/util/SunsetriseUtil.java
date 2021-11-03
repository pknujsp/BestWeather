package com.lifedawn.bestweather.weathers.dataprocessing.util;

import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class SunsetriseUtil {
	private SunsetriseUtil() {
	}
	
	public static Map<String, SunSetRiseData> getSunSetRiseMap(ZonedDateTime begin, ZonedDateTime end, double latitude, double longitude) {
		TimeZone timeZone = TimeZone.getTimeZone(begin.getZone().toString());
		List<LocalDateTime> dateTimeList = new ArrayList<>();
		
		ZonedDateTime tempDateTime = ZonedDateTime.of(begin.toLocalDate(), begin.toLocalTime(), ZoneId.of(timeZone.getID()));
		
		while (true) {
			dateTimeList.add(tempDateTime.toLocalDateTime());
			
			if (end.getDayOfYear() == tempDateTime.getDayOfYear()) {
				break;
			}
			tempDateTime = tempDateTime.plusDays(1);
		}
		
		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(new Location(latitude, longitude), timeZone);
		Calendar sunRiseCalender = null;
		Calendar sunSetCalendar = null;
		Calendar tempCalendar = Calendar.getInstance(timeZone);
		
		Map<String, SunSetRiseData> resultMap = new HashMap<>();
		for (LocalDateTime date : dateTimeList) {
			tempCalendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth(), date.getHour(), 0, 0);
			
			sunRiseCalender = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(tempCalendar);
			sunSetCalendar = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(tempCalendar);
			resultMap.put(date.toString(), new SunSetRiseData(date, sunRiseCalender.getTime(), sunSetCalendar.getTime()));
		}
		return resultMap;
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
	
	public static class SunSetRiseData {
		final LocalDateTime date;
		final Date sunrise;
		final Date sunset;
		
		public SunSetRiseData(LocalDateTime date, Date sunrise, Date sunset) {
			this.date = date;
			this.sunrise = sunrise;
			this.sunset = sunset;
		}
		
		public LocalDateTime getDate() {
			return date;
		}
		
		public Date getSunrise() {
			return sunrise;
		}
		
		public Date getSunset() {
			return sunset;
		}
	}
}
