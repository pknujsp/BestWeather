package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.lifedawn.bestweather.R;

import java.util.HashMap;
import java.util.Map;

public class OpenWeatherMapResponseProcessor {
	private static final Map<String, String> WEATHER_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, Drawable> WEATHER_ICON_IMG_MAP = new HashMap<>();
	
	private OpenWeatherMapResponseProcessor() {
	}
	
	public static void init(Context context) {
		String[] codes = context.getResources().getStringArray(R.array.OpenWeatherMapWeatherIconCodes);
		String[] descriptions = context.getResources().getStringArray(R.array.OpenWeatherMapWeatherIconDescriptionsForCode);
		
		WEATHER_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < codes.length; i++) {
			WEATHER_ICON_DESCRIPTION_MAP.put(codes[i], descriptions[i]);
		}
	}
	
	public static Drawable getWeatherIconImg(String code) {
		return null;
	}
	
	public static String getWeatherIconDescription(String code) {
		return WEATHER_ICON_DESCRIPTION_MAP.get(code);
	}
}