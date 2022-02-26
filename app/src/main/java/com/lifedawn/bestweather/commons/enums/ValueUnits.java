package com.lifedawn.bestweather.commons.enums;

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

	public static ValueUnits enumOf(String value) throws IllegalArgumentException {
		for (ValueUnits valueUnit : values()) {
			if (value.equals(valueUnit.name())) {
				return valueUnit;
			}
		}
		throw new IllegalArgumentException();
	}

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
				return "PM 3:00";
			case clock24:
				return "15:00";
			default:
				return null;
		}
	}

	public static Integer convertTemperature(String val, ValueUnits unit) {
		Integer convertedVal = (int) Math.round(Double.parseDouble(val));
		if (unit == fahrenheit) {
			//화씨 (1℃ × 9/5) + 32℉
			convertedVal = (int) Math.round((convertedVal * (9.0 / 5.0) + 32));
		}
		return convertedVal;
	}

	public static Double convertWindSpeed(String val, ValueUnits unit) {
		Double convertedVal = Double.parseDouble(val);
		if (unit == kmPerHour) {
			//m/s -> km/h n x 3.6 = c
			convertedVal = convertedVal * 3.6;
		}
		return Math.round(convertedVal * 10) / 10.0;
	}

	public static Double convertWindSpeedForAccu(String val, ValueUnits unit) {
		Double convertedVal = Double.parseDouble(val);
		if (unit == mPerSec) {
			//m/s -> km/h n x 3.6 = c
			convertedVal = convertedVal / 3.6;
		}
		return Math.round(convertedVal * 10) / 10.0;
	}

	public static String convertVisibility(String val, ValueUnits unit) {
		Double convertedVal = Double.parseDouble(val) / 1000.0;
		if (unit == mile) {
			//km -> mile  n / 1.609 = c
			convertedVal = convertedVal / 1.609;
		}
		return String.format("%.1f", convertedVal);
	}

	public static String convertVisibilityForAccu(String val, ValueUnits unit) {
		Double convertedVal = Double.parseDouble(val);
		if (unit == mile) {
			//km -> mile  n / 1.609 = c
			convertedVal = convertedVal / 1.609;
		}
		return String.format("%.1f", convertedVal);
	}

	public static Double convertCMToMM(String val) {
		return (Double.parseDouble(val) * 100) / 10.0;
	}

	public static Double convertMMToCM(String val) {
		return Double.parseDouble(val) / 10.0;
	}
}
