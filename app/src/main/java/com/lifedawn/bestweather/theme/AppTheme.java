package com.lifedawn.bestweather.theme;

import android.content.Context;
import android.util.TypedValue;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.weathers.view.FragmentType;

public class AppTheme {
	public static int getColor(Context context, int id) {
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(id, value, true);
		return value.data;
	}

	public static int getTextColor(Context context, FragmentType fragmentType) {
		TypedValue value = new TypedValue();
		switch (fragmentType) {
			case Simple:
				context.getTheme().resolveAttribute(R.attr.textColorInWeatherCard, value, true);
				break;
			case Detail:
			case Comparison:
				context.getTheme().resolveAttribute(R.attr.textColor, value, true);
				break;
		}
		return value.data;
	}
}
