package com.lifedawn.bestweather.commons.enums;

import android.content.Context;

import com.lifedawn.bestweather.R;

public enum ValueUnits {
	celsius, fahrenheit, mPerSec, kmPerHour, km, mile, clock12, clock24;
	
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
			default:
				return null;
		}
	}
	
	public static Integer convertTemperature(String val, ValueUnits unit) {
		Integer convertedVal = (int) Double.parseDouble(val);
		if (unit == fahrenheit) {
			//화씨 (1°C × 9/5) + 32°F
			convertedVal = (int) (convertedVal * (9.0 / 5.0)) + 32;
		}
		return convertedVal;
	}
	
	public static Double convertWindSpeed(String val, ValueUnits unit) {
		Double convertedVal = Double.parseDouble(val);
		if (unit == kmPerHour) {
			//m/s -> km/h n x 3.6 = c
			convertedVal = convertedVal * 3.6;
		}
		return convertedVal;
	}
	
	public static String convertVisibility(String val, ValueUnits unit) {
		Integer convertedVal = Integer.parseInt(val);
		if (unit == mile) {
			//km -> mile  n / 1.609 = c
			convertedVal = (int) (convertedVal / 1.609);
		}
		return convertedVal.toString();
	}
}
