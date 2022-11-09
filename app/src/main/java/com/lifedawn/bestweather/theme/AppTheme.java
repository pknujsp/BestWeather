package com.lifedawn.bestweather.theme;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.FragmentType;

public class AppTheme {
	public static int getColor(Context context, int id) {
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(id, value, true);
		return value.data;
	}

	public static int getTextColor(FragmentType fragmentType) {

		switch (fragmentType) {
			case Detail:
			case Comparison:
				return Color.BLACK;
			default:
				return Color.WHITE;
		}

	}
}
