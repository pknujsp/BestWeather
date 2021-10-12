package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.lifedawn.bestweather.R;

import java.util.HashMap;
import java.util.Map;

public class KmaResponseProcessor {
	private static final Map<String, String> WEATHER_SKY_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> WEATHER_PTY_ICON_DESCRIPTION_MAP = new HashMap<>();
	
	private static final Map<String, Drawable> WEATHER_SKY_ICON_IMG_MAP = new HashMap<>();
	private static final Map<String, Drawable> WEATHER_PTY_ICON_IMG_MAP = new HashMap<>();
	
	private KmaResponseProcessor() {
	}
	
	public static void init(Context context) {
		String[] skyCodes = context.getResources().getStringArray(R.array.KmaSkyIconCodes);
		String[] ptyCodes = context.getResources().getStringArray(R.array.KmaPtyIconCodes);
		String[] skyDescriptions = context.getResources().getStringArray(R.array.KmaSkyIconDescriptionsForCode);
		String[] ptyDescriptions = context.getResources().getStringArray(R.array.KmaPtyIconDescriptionsForCode);
		
		WEATHER_SKY_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < skyCodes.length; i++) {
			WEATHER_SKY_ICON_DESCRIPTION_MAP.put(skyCodes[i], skyDescriptions[i]);
		}
		
		WEATHER_PTY_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < ptyCodes.length; i++) {
			WEATHER_PTY_ICON_DESCRIPTION_MAP.put(ptyCodes[i], ptyDescriptions[i]);
		}
		
	}
	
	public static Drawable getWeatherSkyIconImg(String code) {
		return null;
	}
	
	public static String getWeatherSkyIconDescription(String code) {
		return WEATHER_SKY_ICON_DESCRIPTION_MAP.get(code);
	}
	
	public static Drawable getWeatherPtyIconImg(String code) {
		return null;
	}
	
	public static String getWeatherPtyIconDescription(String code) {
		return WEATHER_SKY_ICON_DESCRIPTION_MAP.get(code);
	}
}
