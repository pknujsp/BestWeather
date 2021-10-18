package com.lifedawn.bestweather.commons.classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class ClockUtil {
	public static final TimeZone KR_TIMEZONE = TimeZone.getTimeZone("Asia/Seoul");

	private ClockUtil() {
	}

	public static boolean areSameDate(long dt1, long dt2) {
		GregorianCalendar dt1Calendar = new GregorianCalendar();
		dt1Calendar.setTimeInMillis(dt1);
		GregorianCalendar dt2Calendar = new GregorianCalendar();
		dt2Calendar.setTimeInMillis(dt2);

		if (dt1Calendar.get(Calendar.YEAR) == dt2Calendar.get(Calendar.YEAR) &&
				dt1Calendar.get(Calendar.DAY_OF_YEAR) == dt2Calendar.get(Calendar.DAY_OF_YEAR)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean areSameDate(Calendar calendar1, Calendar calendar2) {
		if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
				calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)) {
			return true;
		} else {
			return false;
		}
	}

}
