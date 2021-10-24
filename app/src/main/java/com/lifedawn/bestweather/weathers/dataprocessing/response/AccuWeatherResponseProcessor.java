package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.geopositionsearch.GeoPositionResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Response;

public class AccuWeatherResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> PTY_MAP = new HashMap<>();
	private static final Map<String, String> FLICKR_MAP = new HashMap<>();
	private static final Map<String, Drawable> WEATHER_ICON_IMG_MAP = new HashMap<>();

	private AccuWeatherResponseProcessor() {
	}

	public static void init(Context context) {
		String[] codes = context.getResources().getStringArray(R.array.AccuWeatherWeatherIconCodes);
		String[] descriptions = context.getResources().getStringArray(R.array.AccuWeatherWeatherIconDescriptionsForCode);

		WEATHER_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < codes.length; i++) {
			WEATHER_ICON_DESCRIPTION_MAP.put(codes[i], descriptions[i]);
		}

		String[] flickrGalleryNames = context.getResources().getStringArray(R.array.AccuWeatherFlickrGalleryNames);

		FLICKR_MAP.clear();
		for (int i = 0; i < codes.length; i++) {
			FLICKR_MAP.put(codes[i], flickrGalleryNames[i]);
		}

		//     <!-- precipitation type 값 종류 : Rain, Snow, Ice, Null(Not), or Mixed -->
		PTY_MAP.clear();
		PTY_MAP.put("Rain", context.getString(R.string.accu_weather_pty_rain));
		PTY_MAP.put("Snow", context.getString(R.string.accu_weather_pty_snow));
		PTY_MAP.put("Ice", context.getString(R.string.accu_weather_pty_ice));
		PTY_MAP.put("Null", context.getString(R.string.accu_weather_pty_not));
		PTY_MAP.put("Mixed", context.getString(R.string.accu_weather_pty_mixed));
	}

	public static Drawable getWeatherIconImg(String code) {
		return null;
	}

	public static String getWeatherIconDescription(String code) {
		return WEATHER_ICON_DESCRIPTION_MAP.get(code);
	}

	public static String getPty(String pty) {
		return pty == null ? PTY_MAP.get("Null") : PTY_MAP.get(pty);
	}

	public static GeoPositionResponse getGeoPositionObjFromJson(String response) {
		return new Gson().fromJson(response, GeoPositionResponse.class);
	}

	public static CurrentConditionsResponse getCurrentConditionsObjFromJson(JsonElement jsonElement) {
		CurrentConditionsResponse response = new CurrentConditionsResponse();
		response.setItems(jsonElement);
		return response;
	}

	public static TwelveHoursOfHourlyForecastsResponse getHourlyForecastObjFromJson(JsonElement jsonElement) {
		TwelveHoursOfHourlyForecastsResponse response = new TwelveHoursOfHourlyForecastsResponse();
		response.setItems(jsonElement);
		return response;
	}

	public static FiveDaysOfDailyForecastsResponse getDailyForecastObjFromJson(String response) {
		return new Gson().fromJson(response, FiveDaysOfDailyForecastsResponse.class);
	}

	public static String getFlickrGalleryName(String code) {
		return FLICKR_MAP.get(code);
	}

	public static TimeZone getTimeZone(String dateTime) throws ParseException {
		return ClockUtil.convertISO8061Format(dateTime).getTimeZone();
	}
}
