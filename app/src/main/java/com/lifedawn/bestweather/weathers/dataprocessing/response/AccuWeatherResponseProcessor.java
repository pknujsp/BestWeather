package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.res.TypedArray;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.geopositionsearch.GeoPositionResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public class AccuWeatherResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> PTY_MAP = new HashMap<>();
	private static final Map<String, Integer> PTY_ICON_MAP = new HashMap<>();
	private static final Map<String, String> FLICKR_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_ICON_ID_MAP = new HashMap<>();

	private AccuWeatherResponseProcessor() {
	}

	public static void init(Context context) {
		if (WEATHER_ICON_DESCRIPTION_MAP.isEmpty() || PTY_MAP.isEmpty() || FLICKR_MAP.isEmpty() || WEATHER_ICON_ID_MAP.isEmpty()) {
			String[] codes = context.getResources().getStringArray(R.array.AccuWeatherWeatherIconCodes);
			String[] descriptions = context.getResources().getStringArray(R.array.AccuWeatherWeatherIconDescriptionsForCode);
			TypedArray iconIds = context.getResources().obtainTypedArray(R.array.AccuWeatherWeatherIconForCode);

			WEATHER_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < codes.length; i++) {
				WEATHER_ICON_DESCRIPTION_MAP.put(codes[i], descriptions[i]);
				WEATHER_ICON_ID_MAP.put(codes[i], iconIds.getResourceId(i, 0));
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

			PTY_ICON_MAP.clear();
			PTY_ICON_MAP.put("Rain", R.drawable.raindrop);
			PTY_ICON_MAP.put("Snow", R.drawable.snowparticle);
			PTY_ICON_MAP.put("Ice", R.drawable.snowparticle);
			PTY_ICON_MAP.put("Null", R.drawable.pty_null);
			PTY_ICON_MAP.put("Mixed", R.drawable.sleet);
		}
	}

	public static int getWeatherIconImg(String code) {
		return WEATHER_ICON_ID_MAP.get(code);
	}

	public static String getWeatherIconDescription(String code) {
		return WEATHER_ICON_DESCRIPTION_MAP.get(code);
	}

	public static String getPty(String pty) {
		return pty == null ? PTY_MAP.get("Null") : PTY_MAP.get(pty);
	}

	public static int getPtyIcon(String pty) {
		return pty == null ? PTY_ICON_MAP.get("Null") : PTY_ICON_MAP.get(pty);
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


	public static boolean successfulResponse(MultipleJsonDownloader.ResponseResult result) {
		if (result.getResponse() != null) {
			Response<JsonElement> response = (Response<JsonElement>) result.getResponse();

			if (response.isSuccessful()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
