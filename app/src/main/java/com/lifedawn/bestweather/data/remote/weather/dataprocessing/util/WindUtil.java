package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util;

import android.content.Context;

import com.lifedawn.bestweather.R;

import java.util.HashMap;
import java.util.Map;

public class WindUtil {
	private static Map<String, String> windStrengthDescriptionMap = new HashMap<>();
	private static Map<String, String> windStrengthDescriptionSimpleMap = new HashMap<>();

	public static void init(Context context) {
		if (windStrengthDescriptionMap.isEmpty() || windStrengthDescriptionSimpleMap.isEmpty()) {
			windStrengthDescriptionMap.put("1", context.getString(R.string.wind_strength_1));
			windStrengthDescriptionMap.put("2", context.getString(R.string.wind_strength_2));
			windStrengthDescriptionMap.put("3", context.getString(R.string.wind_strength_3));
			windStrengthDescriptionMap.put("4", context.getString(R.string.wind_strength_4));

			windStrengthDescriptionSimpleMap.put("1", context.getString(R.string.wind_strength_1_simple));
			windStrengthDescriptionSimpleMap.put("2", context.getString(R.string.wind_strength_2_simple));
			windStrengthDescriptionSimpleMap.put("3", context.getString(R.string.wind_strength_3_simple));
			windStrengthDescriptionSimpleMap.put("4", context.getString(R.string.wind_strength_4_simple));
		}
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

	public static String parseWindDirectionDegreeAsStr(Context context, String degree) {
		final int convertedToSixteen = (int) ((Double.parseDouble(degree) + 22.5 * 0.5) / 22.5);
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

	public static String parseWindDirectionStrAsStr(Context context, String degree) {
		switch (degree) {
			case "북북동":
				return context.getString(R.string.wind_direction_NNE);

			case "북동":
				return context.getString(R.string.wind_direction_NE);

			case "동북동":
				return context.getString(R.string.wind_direction_ENE);

			case "동":
				return context.getString(R.string.wind_direction_E);

			case "동남동":
				return context.getString(R.string.wind_direction_ESE);

			case "남동":
				return context.getString(R.string.wind_direction_SE);

			case "남남동":
				return context.getString(R.string.wind_direction_SSE);

			case "남":
				return context.getString(R.string.wind_direction_S);

			case "남남서":
				return context.getString(R.string.wind_direction_SSW);

			case "남서":
				return context.getString(R.string.wind_direction_SW);

			case "서남서":
				return context.getString(R.string.wind_direction_WSW);

			case "서":
				return context.getString(R.string.wind_direction_W);

			case "서북서":
				return context.getString(R.string.wind_direction_WNW);

			case "북서":
				return context.getString(R.string.wind_direction_NW);

			case "북북서":
				return context.getString(R.string.wind_direction_NNW);

			default:
				return context.getString(R.string.wind_direction_N);
		}
	}

	public static int parseWindDirectionStrAsInt(String degree) {
		switch (degree) {
			case "북북동":
				return 25;

			case "북동":
				return 45;

			case "동북동":
				return 67;

			case "동":
				return 90;

			case "동남동":
				return 112;

			case "남동":
				return 135;

			case "남남동":
				return 157;

			case "남":
				return 180;

			case "남남서":
				return 202;

			case "남서":
				return 225;

			case "서남서":
				return 247;

			case "서":
				return 270;

			case "서북서":
				return 292;

			case "북서":
				return 315;

			case "북북서":
				return 337;

			default:
				return 0;
		}
	}
}
