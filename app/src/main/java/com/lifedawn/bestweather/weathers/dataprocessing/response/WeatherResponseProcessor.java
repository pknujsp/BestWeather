package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class WeatherResponseProcessor {
	private static Map<String, String> windStrengthDescriptionMap = new HashMap<>();
	private static Map<String, String> windStrengthDescriptionSimpleMap = new HashMap<>();

	public static void init(Context context) {
		windStrengthDescriptionMap.clear();
		windStrengthDescriptionSimpleMap.clear();

		windStrengthDescriptionMap.put("1", context.getString(R.string.wind_strength_1));
		windStrengthDescriptionMap.put("2", context.getString(R.string.wind_strength_2));
		windStrengthDescriptionMap.put("3", context.getString(R.string.wind_strength_3));
		windStrengthDescriptionMap.put("4", context.getString(R.string.wind_strength_4));

		windStrengthDescriptionSimpleMap.put("1", context.getString(R.string.wind_strength_1_simple));
		windStrengthDescriptionSimpleMap.put("2", context.getString(R.string.wind_strength_2_simple));
		windStrengthDescriptionSimpleMap.put("3", context.getString(R.string.wind_strength_3_simple));
		windStrengthDescriptionSimpleMap.put("4", context.getString(R.string.wind_strength_4_simple));
	}

	public static String getWindSpeedDescription(String windSpeed) {
		double speed = Double.parseDouble(windSpeed);

		if (speed >= 14) {
			return windStrengthDescriptionMap.get("4");
		} else if (speed >= 9) {
			return windStrengthDescriptionMap.get("3");
		} else if (speed >= 4) {
			return windStrengthDescriptionMap.get("2");
		} else {
			return windStrengthDescriptionMap.get("1");
		}
	}

	public static String getSimpleWindSpeedDescription(String windSpeed) {
		double speed = Double.parseDouble(windSpeed);

		if (speed >= 14) {
			return windStrengthDescriptionSimpleMap.get("4");
		} else if (speed >= 9) {
			return windStrengthDescriptionSimpleMap.get("3");
		} else if (speed >= 4) {
			return windStrengthDescriptionSimpleMap.get("2");
		} else {
			return windStrengthDescriptionSimpleMap.get("1");
		}
	}

	public static ZonedDateTime convertDateTimeOfDailyForecast(long millis, ZoneId zoneId) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId).withHour(0).withMinute(0).withSecond(0).withNano(0);
	}

	public static ZonedDateTime convertDateTimeOfCurrentConditions(long millis, ZoneId zoneId) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId);
	}


	public static ZonedDateTime convertDateTimeOfHourlyForecast(long millis, ZoneId zoneId) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId).withMinute(0).withSecond(0).withNano(0);
	}

	public static AirQualityDto getAirQualityDto(Context context, MultipleJsonDownloader multipleJsonDownloader,
	                                             WeatherSourceType weatherSourceType, ZoneOffset zoneOffset) {
		MultipleJsonDownloader.ResponseResult aqiCnResponseResult = multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		AirQualityDto airQualityDto = null;

		if (aqiCnResponseResult.getResponse() != null) {
			GeolocalizedFeedResponse airQualityResponse = AqicnResponseProcessor.getAirQualityObjFromJson((Response<JsonElement>) aqiCnResponseResult.getResponse());
			airQualityDto = AqicnResponseProcessor.makeAirQualityDto(context, airQualityResponse, zoneOffset);
		}
		return airQualityDto;
	}

	public static CurrentConditionsDto getCurrentConditionsDto(Context context, MultipleJsonDownloader multipleJsonDownloader,
	                                                           WeatherSourceType weatherSourceType) {
		CurrentConditionsDto currentConditionsDto = null;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		if (weatherSourceType == WeatherSourceType.KMA) {
			MultipleJsonDownloader.ResponseResult ultraSrtResponseResult =
					multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST);

			if (ultraSrtResponseResult.isSuccessful()) {
				FinalCurrentConditions finalCurrentConditions =
						KmaResponseProcessor.getFinalCurrentConditions((VilageFcstResponse) ultraSrtResponseResult.getResponse().body());
				UltraSrtNcstParameter ultraSrtNcstParameter = (UltraSrtNcstParameter) ultraSrtResponseResult.getRequestParameter();

				currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDto(context, finalCurrentConditions, windUnit, tempUnit,
						ultraSrtNcstParameter.getLatitude(), ultraSrtNcstParameter.getLongitude());
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			MultipleJsonDownloader.ResponseResult currentConditionsResponseResult = multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER)
					.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

			if (currentConditionsResponseResult.isSuccessful()) {
				CurrentConditionsResponse currentConditionsResponse =
						AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson((JsonElement) currentConditionsResponseResult.getResponse().body());

				currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
						currentConditionsResponse.getItems().get(0), windUnit, tempUnit, visibilityUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleJsonDownloader.ResponseResult owmResponseResult = multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP)
					.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (owmResponseResult.isSuccessful()) {
				OneCallResponse oneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(owmResponseResult.getResponse().body().toString());

				currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDto(context, oneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}
		return currentConditionsDto;
	}

	public static List<HourlyForecastDto> getHourlyForecastDtoList(Context context, MultipleJsonDownloader multipleJsonDownloader,
	                                                               WeatherSourceType weatherSourceType) {
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult>> responseMap = multipleJsonDownloader.getResponseMap();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		if (weatherSourceType == WeatherSourceType.KMA) {
			MultipleJsonDownloader.ResponseResult ultraSrtFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(
					RetrofitClient.ServiceType.ULTRA_SRT_FCST);
			MultipleJsonDownloader.ResponseResult vilageFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(
					RetrofitClient.ServiceType.VILAGE_FCST);

			if (ultraSrtFcstResponseResult.isSuccessful() && vilageFcstResponseResult.isSuccessful()) {
				VilageFcstResponse ultraSrtFcstRoot =
						(VilageFcstResponse) ultraSrtFcstResponseResult.getResponse().body();
				VilageFcstResponse vilageFcstRoot =
						(VilageFcstResponse) vilageFcstResponseResult.getResponse().body();

				VilageFcstParameter vilageFcstParameter = (VilageFcstParameter) vilageFcstResponseResult.getRequestParameter();

				List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstRoot,
						vilageFcstRoot);
				hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoList(context, finalHourlyForecastList,
						vilageFcstParameter.getLatitude(), vilageFcstParameter.getLongitude(), windUnit, tempUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			MultipleJsonDownloader.ResponseResult hourlyForecastResponseResult = responseMap.get(WeatherSourceType.ACCU_WEATHER).get(
					RetrofitClient.ServiceType.ACCU_12_HOURLY);

			if (hourlyForecastResponseResult.isSuccessful()) {
				TwelveHoursOfHourlyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
						(JsonElement) hourlyForecastResponseResult.getResponse().body());

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems()
						, windUnit, tempUnit, visibilityUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleJsonDownloader.ResponseResult responseResult = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				OneCallResponse oneCallResponse = OpenWeatherMapResponseProcessor.getOneCallObjFromJson(
						responseResult.getResponse().body().toString());

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}

		return hourlyForecastDtoList;
	}

	public static List<DailyForecastDto> getDailyForecastDtoList(Context context, MultipleJsonDownloader multipleJsonDownloader,
	                                                             WeatherSourceType weatherSourceType) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult>> responseMap = multipleJsonDownloader.getResponseMap();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));

		if (weatherSourceType == WeatherSourceType.KMA) {
			MultipleJsonDownloader.ResponseResult midLandFcstResponseResult =
					responseMap.get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_LAND_FCST);
			MultipleJsonDownloader.ResponseResult midTaFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_TA_FCST);
			MultipleJsonDownloader.ResponseResult vilageFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST);
			MultipleJsonDownloader.ResponseResult ultraSrtFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);

			if (midLandFcstResponseResult.isSuccessful() && midTaFcstResponseResult.isSuccessful() &&
					vilageFcstResponseResult.isSuccessful() && ultraSrtFcstResponseResult.isSuccessful()) {
				MidLandFcstResponse midLandFcstRoot =
						(MidLandFcstResponse) midLandFcstResponseResult.getResponse().body();
				MidTaResponse midTaRoot = (MidTaResponse) midTaFcstResponseResult.getResponse().body();
				VilageFcstResponse vilageFcstRoot = (VilageFcstResponse) vilageFcstResponseResult.getResponse().body();
				VilageFcstResponse ultraSrtFcstRoot = (VilageFcstResponse) ultraSrtFcstResponseResult.getResponse().body();

				List<FinalHourlyForecast> finalHourlyForecasts = KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstRoot,
						vilageFcstRoot);
				List<FinalDailyForecast> finalDailyForecasts = KmaResponseProcessor.getFinalDailyForecastList(midLandFcstRoot, midTaRoot,
						Long.parseLong(multipleJsonDownloader.get("tmFc")));

				dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoList(context,
						KmaResponseProcessor.getDailyForecastList(finalDailyForecasts, finalHourlyForecasts), tempUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			MultipleJsonDownloader.ResponseResult dailyForecastResponseResult = responseMap.get(WeatherSourceType.ACCU_WEATHER).get(
					RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

			if (dailyForecastResponseResult.isSuccessful()) {
				FiveDaysOfDailyForecastsResponse dailyForecastResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
						dailyForecastResponseResult.getResponse().body().toString());

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, dailyForecastResponse.getDailyForecasts()
						, windUnit, tempUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleJsonDownloader.ResponseResult responseResult = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				OneCallResponse oneCallResponse = OpenWeatherMapResponseProcessor.getOneCallObjFromJson(
						responseResult.getResponse().body().toString());

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit);
			}
		}

		return dailyForecastDtoList;
	}
}
