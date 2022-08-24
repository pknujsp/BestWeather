package com.lifedawn.bestweather.weathers.dataprocessing.response;

import java.util.HashMap;
import java.util.Map;

public class FlickrUtil {
	private static final Map<String, String> WEATHER_GALLERY_ID_MAP = new HashMap<>();

	private FlickrUtil() {
	}

	public static void init() {
		if (!WEATHER_GALLERY_ID_MAP.isEmpty()) {
			return;
		}
		WEATHER_GALLERY_ID_MAP.clear();

		WEATHER_GALLERY_ID_MAP.put("sunrise clear", "72157719913955346");
		WEATHER_GALLERY_ID_MAP.put("sunrise partly cloudy", "72157719931023221");
		WEATHER_GALLERY_ID_MAP.put("sunrise mostly cloudy", "72157719938087287");
		WEATHER_GALLERY_ID_MAP.put("sunrise overcast", "72157719938089657");
		WEATHER_GALLERY_ID_MAP.put("sunrise rain", "72157719938090082");
		WEATHER_GALLERY_ID_MAP.put("sunrise snow", "72157719938090672");

		WEATHER_GALLERY_ID_MAP.put("sunset clear", "72157719931028631");
		WEATHER_GALLERY_ID_MAP.put("sunset partly cloudy", "72157719980387665");
		WEATHER_GALLERY_ID_MAP.put("sunset mostly cloudy", "72157719925493763");
		WEATHER_GALLERY_ID_MAP.put("sunset overcast", "72157719925494163");
		WEATHER_GALLERY_ID_MAP.put("sunset rain", "72157719931030491");
		WEATHER_GALLERY_ID_MAP.put("sunset snow", "72157719925495538");

		WEATHER_GALLERY_ID_MAP.put("day clear", "72157719980390655");
		WEATHER_GALLERY_ID_MAP.put("day partly cloudy", "72157719938096007");
		WEATHER_GALLERY_ID_MAP.put("day mostly cloudy", "72157719938096492");
		WEATHER_GALLERY_ID_MAP.put("day overcast", "72157719938096892");
		WEATHER_GALLERY_ID_MAP.put("day rain", "72157719931034166");
		WEATHER_GALLERY_ID_MAP.put("day snow", "72157719938097657");

		WEATHER_GALLERY_ID_MAP.put("night clear", "72157719931035301");
		WEATHER_GALLERY_ID_MAP.put("night partly cloudy", "72157719927362534");
		WEATHER_GALLERY_ID_MAP.put("night mostly cloudy", "72157719925500593");
		WEATHER_GALLERY_ID_MAP.put("night overcast", "72157719938100367");
		WEATHER_GALLERY_ID_MAP.put("night rain", "72157719980396000");
		WEATHER_GALLERY_ID_MAP.put("night snow", "72157719931038496");
	}

	public static String getWeatherGalleryId(String galleryName) {
		return WEATHER_GALLERY_ID_MAP.get(galleryName);
	}
}
