package com.lifedawn.bestweather.weathers.dataprocessing.util;

import android.content.Context;

import com.lifedawn.bestweather.R;

public class WindDirectionConverter {
	private WindDirectionConverter() {
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
