package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.currentweather.CurrentWeatherResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.dailyforecast.DailyForecast;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.hourlyforecast.HourlyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Response;

public class OpenWeatherMapResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_ICON_ID_MAP = new HashMap<>();
	private static final Map<String, String> FLICKR_MAP = new HashMap<>();

	private OpenWeatherMapResponseProcessor() {
	}

	public static void init(Context context) {
		String[] codes = context.getResources().getStringArray(R.array.OpenWeatherMapWeatherIconCodes);
		String[] descriptions = context.getResources().getStringArray(R.array.OpenWeatherMapWeatherIconDescriptionsForCode);
		TypedArray iconIds = context.getResources().obtainTypedArray(R.array.OpenWeatherMapWeatherIconForCode);

		WEATHER_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < codes.length; i++) {
			WEATHER_ICON_DESCRIPTION_MAP.put(codes[i], descriptions[i]);
			WEATHER_ICON_ID_MAP.put(codes[i], iconIds.getResourceId(i, R.drawable.temp_icon));
		}

		String[] flickrGalleryNames = context.getResources().getStringArray(R.array.OpenWeatherMapFlickrGalleryNames);

		FLICKR_MAP.clear();
		for (int i = 0; i < codes.length; i++) {
			FLICKR_MAP.put(codes[i], flickrGalleryNames[i]);
		}
	}

	public static int getWeatherIconImg(String code, boolean night) {
		if (night) {
			switch (code) {
				case "800":
					return R.drawable.night_clear;
				case "801":
				case "802":
					return R.drawable.night_partly_cloudy;
				case "803":
					return R.drawable.night_mostly_cloudy;
				case "804":
					return R.drawable.overcast;
				default:
					return WEATHER_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_ICON_ID_MAP.get(code);
	}

	public static String getWeatherIconDescription(String code) {
		return WEATHER_ICON_DESCRIPTION_MAP.get(code);
	}

	public static CurrentWeatherResponse getCurrentWeatherObjFromJson(String response) {
		return new Gson().fromJson(response, CurrentWeatherResponse.class);
	}

	public static HourlyForecastResponse getHourlyForecastObjFromJson(String response) {
		return new Gson().fromJson(response, HourlyForecastResponse.class);
	}

	public static DailyForecast getDailyForecastObjFromJson(String response) {
		return new Gson().fromJson(response, DailyForecast.class);
	}

	public static OneCallResponse getOneCallObjFromJson(String response) {
		return new Gson().fromJson(response, OneCallResponse.class);
	}

	public static String getFlickrGalleryName(String code) {
		return FLICKR_MAP.get(code);
	}

	public static TimeZone getTimeZone(OneCallResponse oneCallResponse) {
		return TimeZone.getTimeZone(oneCallResponse.getTimezone());
	}
}