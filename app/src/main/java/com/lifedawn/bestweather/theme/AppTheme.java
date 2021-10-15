package com.lifedawn.bestweather.theme;

import android.content.Context;
import android.util.TypedValue;

public class AppTheme {
	public static int getColor(Context context, int id) {
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(id, value, true);
		return value.data;
	}
}
