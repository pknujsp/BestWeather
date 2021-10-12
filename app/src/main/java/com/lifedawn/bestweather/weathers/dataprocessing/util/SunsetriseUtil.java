package com.lifedawn.bestweather.weathers.dataprocessing.util;

import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SunsetriseUtil {
	private SunsetriseUtil() {
	}

	public static List<SunSetRiseData> getSunsetRiseList(long begin, long end, double latitudeSecondsDivide100,
	                                                     double longitudeSecondsDivide100) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(begin);

		List<Calendar> calendarList = new ArrayList<>();

		while (true) {
			calendarList.add((Calendar) calendar.clone());

			if (ClockUtil.areSameDate(calendar.getTimeInMillis(), end)) {
				break;
			}
			calendar.add(Calendar.DATE, 1);
		}

		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(new Location(latitudeSecondsDivide100,
				longitudeSecondsDivide100), calendar.getTimeZone());
		Calendar sunRise = null;
		Calendar sunSet = null;

		List<SunSetRiseData> resultList = new ArrayList<>();
		for (Calendar date : calendarList) {
			sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(date);
			sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(date);
			resultList.add(new SunSetRiseData(date.getTime(), sunRise.getTime(), sunSet.getTime()));
		}
		return resultList;
	}

	public static class SunSetRiseData {
		private Date date;
		private Date sunrise;
		private Date sunset;

		public SunSetRiseData(Date date, Date sunrise, Date sunset) {
			this.date = date;
			this.sunrise = sunrise;
			this.sunset = sunset;
		}

		public Date getDate() {
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
