package com.lifedawn.bestweather.forremoteviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.forremoteviews.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.forremoteviews.dto.DailyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.HeaderObj;
import com.lifedawn.bestweather.forremoteviews.dto.HourlyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataSaver {

	public static class JsonKey {

		public enum Root {
			successful
		}

		public enum Type {
			ForecastJson, Header, Current, Hourly, Daily, zoneId
		}

		public enum Header {
			address, refreshDateTime
		}

		public enum Current {
			weatherIcon, temp, realFeelTemp, airQuality, precipitation
		}

		public enum Hourly {
			forecasts, clock, temp, weatherIcon
		}

		public enum Daily {
			forecasts, date, minTemp, maxTemp, leftWeatherIcon, rightWeatherIcon, isSingle
		}
	}

	public CurrentConditionsObj getTempCurrentConditionsObj() {
		CurrentConditionsObj currentConditionsObj = new CurrentConditionsObj(true);
		currentConditionsObj.setWeatherIcon(R.drawable.day_clear);

		String temp = "20";
		currentConditionsObj.setTemp(temp);
		currentConditionsObj.setRealFeelTemp(temp);
		currentConditionsObj.setAirQuality("10");
		currentConditionsObj.setPrecipitation(null);
		currentConditionsObj.setZoneId(ZoneId.systemDefault().getId());
		return currentConditionsObj;
	}

	public WeatherJsonObj.HourlyForecasts getTempHourlyForecastObjs() {
		WeatherJsonObj.HourlyForecasts hourlyForecasts = new WeatherJsonObj.HourlyForecasts();
		List<HourlyForecastObj> tempHourlyForecastObjs = new ArrayList<>();
		hourlyForecasts.setHourlyForecastObjs(tempHourlyForecastObjs);
		ZonedDateTime now = ZonedDateTime.now();
		hourlyForecasts.setZoneId(now.getZone().getId());

		String temp = "20";

		for (int i = 0; i < 10; i++) {
			HourlyForecastObj hourlyForecastObj = new HourlyForecastObj(true);
			hourlyForecastObj.setWeatherIcon(R.drawable.day_clear);
			hourlyForecastObj.setClock(now.toString());
			hourlyForecastObj.setTemp(temp);
			tempHourlyForecastObjs.add(hourlyForecastObj);

			now = now.plusHours(1);
		}
		return hourlyForecasts;
	}

	public WeatherJsonObj.DailyForecasts getTempDailyForecastObjs() {
		WeatherJsonObj.DailyForecasts dailyForecasts = new WeatherJsonObj.DailyForecasts();
		List<DailyForecastObj> tempDailyForecastObjs = new ArrayList<>();
		dailyForecasts.setDailyForecastObjs(tempDailyForecastObjs);
		ZonedDateTime now = ZonedDateTime.now();
		dailyForecasts.setZoneId(now.getZone().getId());

		String temp = "20";

		for (int i = 0; i < 5; i++) {
			DailyForecastObj dailyForecastObj = new DailyForecastObj(true, false);
			dailyForecastObj.setLeftWeatherIcon(R.drawable.day_clear);
			dailyForecastObj.setRightWeatherIcon(R.drawable.night_clear);
			dailyForecastObj.setDate(now.toString());
			dailyForecastObj.setMinTemp(temp);
			dailyForecastObj.setMaxTemp(temp);
			tempDailyForecastObjs.add(dailyForecastObj);

			now = now.plusDays(1);
		}
		return dailyForecasts;
	}

	public static WeatherJsonObj getSavedWeatherData(Context context, String sharedPreferenceName) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE);
		WeatherJsonObj weatherJsonObj = new Gson().fromJson(sharedPreferences.getString(JsonKey.Type.ForecastJson.name(), ""),
				WeatherJsonObj.class);

		return weatherJsonObj;
	}


	public static void saveWeatherData(String sharedPreferenceName, Context context, @Nullable HeaderObj headerObj,
	                                   @Nullable CurrentConditionsObj currentConditionsObj,
	                                   @Nullable WeatherJsonObj.HourlyForecasts hourlyForecastObjs,
	                                   @Nullable WeatherJsonObj.DailyForecasts dailyForecastObjs) {
		JsonObject weatherDataJsonObject = new JsonObject();

		if (headerObj != null) {
			JsonObject rootObject = new JsonObject();
			rootObject.addProperty(JsonKey.Header.address.name(), headerObj.getAddress());
			rootObject.addProperty(JsonKey.Header.refreshDateTime.name(), headerObj.getRefreshDateTime());

			weatherDataJsonObject.add(JsonKey.Type.Header.name(), rootObject);
		}
		if (currentConditionsObj != null) {
			if (currentConditionsObj.isSuccessful()) {
				JsonObject rootObject = new JsonObject();
				rootObject.addProperty(JsonKey.Current.weatherIcon.name(), currentConditionsObj.getWeatherIcon());
				rootObject.addProperty(JsonKey.Current.temp.name(), currentConditionsObj.getTemp());
				rootObject.addProperty(JsonKey.Current.realFeelTemp.name(), currentConditionsObj.getRealFeelTemp());
				rootObject.addProperty(JsonKey.Current.airQuality.name(), currentConditionsObj.getAirQuality());
				rootObject.addProperty(JsonKey.Current.precipitation.name(), currentConditionsObj.getPrecipitation());
				rootObject.addProperty(JsonKey.Type.zoneId.name(), currentConditionsObj.getZoneId());

				weatherDataJsonObject.add(JsonKey.Type.Current.name(), rootObject);
			}
		}
		if (hourlyForecastObjs != null) {
			JsonArray forecasts = new JsonArray();

			for (HourlyForecastObj hourlyForecastObj : hourlyForecastObjs.getHourlyForecastObjs()) {
				JsonObject forecastObject = new JsonObject();
				forecastObject.addProperty(JsonKey.Hourly.clock.name(), hourlyForecastObj.getClock());
				forecastObject.addProperty(JsonKey.Hourly.weatherIcon.name(), hourlyForecastObj.getWeatherIcon());
				forecastObject.addProperty(JsonKey.Hourly.temp.name(), hourlyForecastObj.getTemp());

				forecasts.add(forecastObject);
			}

			if (!forecasts.isEmpty()) {
				JsonObject rootObject = new JsonObject();
				rootObject.add(JsonKey.Hourly.forecasts.name(), forecasts);
				rootObject.addProperty(JsonKey.Type.zoneId.name(), hourlyForecastObjs.getZoneId());

				weatherDataJsonObject.add(JsonKey.Type.Hourly.name(), rootObject);
			}
		}
		if (dailyForecastObjs != null) {
			JsonArray forecasts = new JsonArray();

			for (DailyForecastObj dailyForecastObj : dailyForecastObjs.getDailyForecastObjs()) {
				JsonObject forecastObject = new JsonObject();
				forecastObject.addProperty(JsonKey.Daily.date.name(), dailyForecastObj.getDate());
				forecastObject.addProperty(JsonKey.Daily.isSingle.name(), dailyForecastObj.isSingle());
				forecastObject.addProperty(JsonKey.Daily.leftWeatherIcon.name(), dailyForecastObj.getLeftWeatherIcon());
				forecastObject.addProperty(JsonKey.Daily.rightWeatherIcon.name(), dailyForecastObj.getRightWeatherIcon());
				forecastObject.addProperty(JsonKey.Daily.minTemp.name(), dailyForecastObj.getMinTemp());
				forecastObject.addProperty(JsonKey.Daily.maxTemp.name(), dailyForecastObj.getMaxTemp());

				forecasts.add(forecastObject);
			}

			if (!forecasts.isEmpty()) {
				JsonObject rootObject = new JsonObject();
				rootObject.add(JsonKey.Daily.forecasts.name(), forecasts);
				rootObject.addProperty(JsonKey.Type.zoneId.name(), dailyForecastObjs.getZoneId());

				weatherDataJsonObject.add(JsonKey.Type.Daily.name(), rootObject);
			}
		}

		if (weatherDataJsonObject.size() <= 1) {
			weatherDataJsonObject.addProperty(JsonKey.Root.successful.name(), false);
		} else {
			weatherDataJsonObject.addProperty(JsonKey.Root.successful.name(), true);
		}

		SharedPreferences.Editor editor = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE).edit();
		editor.putString(JsonKey.Type.ForecastJson.name(), weatherDataJsonObject.toString()).apply();
	}
}
