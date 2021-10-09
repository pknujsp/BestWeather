package com.lifedawn.bestweather.commons.enums;

import android.content.Context;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.ValueUnit;

public enum ValueUnits {
	celsius, fahrenheit, mmPerSec, kmPerHour, km, mile, clock12, clock24;

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
			case mmPerSec:
				return context.getString(R.string.mmPerSec);
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
}
