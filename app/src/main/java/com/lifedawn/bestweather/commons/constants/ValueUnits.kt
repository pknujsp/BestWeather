package com.lifedawn.bestweather.commons.constants;

import java.util.Locale;

public enum ValueUnits {
	celsius, fahrenheit, mPerSec, kmPerHour, km, mile, clock12, clock24;

	/*
	accu weather 기본 단위
	바람 : km/h, 비 : mm, 눈 : cm, 기온 : C, 기압 : mb

	owm
	바람 : m/s, 비 : mm, 눈 : mm, 기온 : C, 기압 : mb

	기상청
	바람 : m/s, 비 : mm, 눈 : mm, 기온 : C
	 */

	public static String toString(ValueUnits valueUnit) {
		switch (valueUnit) {
			case celsius:
				return "℃";
			case fahrenheit:
				return "℉";
			case mPerSec:
				return "m/s";
			case kmPerHour:
				return "km/h";
			case km:
				return "km";
			case mile:
				return "mile";
			case clock12:
				return "3:00 PM";
			case clock24:
				return "15:00";
			default:
				return null;
		}
	}

	public static Integer convertTemperature(String val, ValueUnits unit) {
		float floatTemp = 0f;

		try {
			floatTemp = Float.parseFloat(val);
		} catch (Exception e) {
			return 999;
		}

		int convertedVal = (int) Math.round(floatTemp);
		if (unit == fahrenheit) {
			//화씨 (1℃ × 9/5) + 32℉
			convertedVal = (int) Math.round((convertedVal * (9.0 / 5.0) + 32));
		}
		return convertedVal;
	}

	public static Double convertWindSpeed(String value, ValueUnits unit) {
		float convertedVal = 0f;

		try {
			convertedVal = Float.parseFloat(value);
		} catch (Exception e) {
		}

		if (unit == kmPerHour) {
			//m/s -> km/h n x 3.6 = c
			convertedVal = convertedVal * 3.6f;
		}
		return Math.round(convertedVal * 10) / 10.0;
	}

	public static Double convertWindSpeedForAccu(String value, ValueUnits unit) {
		float convertedVal = 0f;

		try {
			convertedVal = Float.parseFloat(value);
		} catch (Exception e) {
		}

		if (unit == mPerSec) {
			//m/s -> km/h n x 3.6 = c
			convertedVal = convertedVal / 3.6f;
		}
		return Math.round(convertedVal * 10) / 10.0;
	}

	public static String convertVisibility(String value, ValueUnits unit) {
		float convertedVal = 0f;

		try {
			convertedVal = Float.parseFloat(value) / 1000f;
		} catch (Exception e) {
		}

		if (unit == mile) {
			//km -> mile  n / 1.609 = c
			convertedVal = convertedVal / 1.609f;
		}
		return String.format(Locale.getDefault(), "%.1f", convertedVal);
	}


	public static Double CMToMM(String val) {
		return (Double.parseDouble(val) * 100) / 10.0;
	}

	public static Double MMToCM(String val) {
		return Double.parseDouble(val) / 10.0;
	}
}
