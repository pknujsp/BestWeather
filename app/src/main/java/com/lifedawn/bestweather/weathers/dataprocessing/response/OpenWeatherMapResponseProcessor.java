package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
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

import retrofit2.Response;

public class OpenWeatherMapResponseProcessor extends WeatherResponseProcessor {
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
}