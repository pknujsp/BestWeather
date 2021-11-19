package com.lifedawn.bestweather.commons.enums;

import android.content.Context;
import android.util.Log;

import com.lifedawn.bestweather.R;

public enum ValueUnits {
	celsius, fahrenheit, mPerSec, kmPerHour, km, mile, clock12, clock24, percent, mm, hpa;

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

	public static String convertToStr(Context context, ValueUnits valueUnit) {
		switch (valueUnit) {
			case celsius:
				return context.getString(R.string.celsius);
			case fahrenheit:
				return context.getString(R.string.fahrenheit);
			case mPerSec:
				return context.getString(R.string.mPerSec);
			case kmPerHour:
				return context.getString(R.string.kmPerHour);
			case km:
				return context.getString(R.string.km);
			case mile:
				return context.getString(R.string.mile);
			case clock12:
				return context.getString(R.string.clock12);
			case clock24:
				return context.getString(R.string.clock24);
			case percent:
				return context.getString(R.string.percent);
			case mm:
				return context.getString(R.string.mm);
			case hpa:
				return context.getString(R.string.hpa);
			default:
				return null;
		}
	}

	public static Integer convertTemperature(String val, ValueUnits unit) {
		Integer convertedVal = (int) Math.round(Double.parseDouble(val));
		if (unit == fahrenheit) {
			//화씨 (1°C × 9/5) + 32°F
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

	public static Integer convertCMToMM(String val) {
		return (int) (Double.parseDouble(val) * 10.0);
	}
}
