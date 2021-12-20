package com.lifedawn.bestweather.weathers.dataprocessing.util;

import android.content.Context;

import com.lifedawn.bestweather.R;

import java.util.HashMap;
import java.util.Map;

public class WindUtil {
	private static Map<String, String> windStrengthDescriptionMap = new HashMap<>();
	private static Map<String, String> windStrengthDescriptionSimpleMap = new HashMap<>();

	public static void init(Context context) {
		windStrengthDescriptionMap.clear();
		windStrengthDescriptionSimpleMap.clear();

		windStrengthDescriptionMap.put("1", context.getString(R.string.wind_strength_1));
		windStrengthDescriptionMap.put("2", context.getString(R.string.wind_strength_2));
		windStrengthDescriptionMap.put("3", context.getString(R.string.wind_strength_3));
		windStrengthDescriptionMap.put("4", context.getString(R.string.wind_strength_4));

		windStrengthDescriptionSimpleMap.put("1", context.getString(R.string.wind_strength_1_simple));
		windStrengthDescriptionSimpleMap.put("2", context.getString(R.string.wind_strength_2_simple));
		windStrengthDescriptionSimpleMap.put("3", context.getString(R.string.wind_strength_3_simple));
		windStrengthDescriptionSimpleMap.put("4", context.getString(R.string.wind_strength_4_simple));
	}

	public static String getWindSpeedDescription(String windSpeed) {
		double speed = Double.parseDouble(windSpeed);

		if (speed >= 14) {
			return windStrengthDescriptionMap.get("4");
		} else if (speed >= 9) {
			return windStrengthDescriptionMap.get("3");
		} else if (speed >= 4) {
			return windStrengthDescriptionMap.get("2");
		} else {
			return windStrengthDescriptionMap.get("1");
		}
	}

	public static String getSimpleWindSpeedDescription(String windSpeed) {
		double speed = Double.parseDouble(windSpeed);

		if (speed >= 14) {
			return windStrengthDescriptionSimpleMap.get("4");
		} else if (speed >= 9) {
			return windStrengthDescriptionSimpleMap.get("3");
		} else if (speed >= 4) {
			return windStrengthDescriptionSimpleMap.get("2");
		} else {
			return windStrengthDescriptionSimpleMap.get("1");
		}
	}

	public static String windDirection(Context context, String degree) {
		final int convertedToSixteen = (int) ((Integer.parseInt(degree) + 22.5 * 0.5) / 22.5);
		switch (convertedToSixteen) {
			case 1:
				return context.getString(R.string.wind_direction_NNE);

			case 2:
				return context.getString(R.string.wind_direction_NE);

			case 3:
				return context.getString(R.string.wind_direction_ENE);

			case 4:
				return context.getString(R.string.wind_direction_E);

			case 5:
				return context.getString(R.string.wind_direction_ESE);

			case 6:
				return context.getString(R.string.wind_direction_SE);

			case 7:
				return context.getString(R.string.wind_direction_SSE);

			case 8:
				return context.getString(R.string.wind_direction_S);

			case 9:
				return context.getString(R.string.wind_direction_SSW);

			case 10:
				return context.getString(R.string.wind_direction_SW);

			case 11:
				return context.getString(R.string.wind_direction_WSW);

			case 12:
				return context.getString(R.string.wind_direction_W);

			case 13:
				return context.getString(R.string.wind_direction_WNW);

			case 14:
				return context.getString(R.string.wind_direction_NW);

			case 15:
				return context.getString(R.string.wind_direction_NNW);

			default:
				return context.getString(R.string.wind_direction_N);
		}
	}
}
