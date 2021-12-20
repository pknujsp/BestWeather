package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.tickaroo.tikxml.TikXml;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okio.Buffer;

public class WeatherResponseProcessor {


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
			airQualityDto = AqicnResponseProcessor.makeAirQualityDto(context, (GeolocalizedFeedResponse) aqiCnResponseResult.getResponseObj(), zoneOffset);
		}
		return airQualityDto;
	}

	public static CurrentConditionsDto parseTextToCurrentConditionsDto(Context context, JsonObject jsonObject,
	                                                                   WeatherSourceType weatherSourceType, Double latitude,
	                                                                   Double longitude) {
		CurrentConditionsDto currentConditionsDto = null;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		if (weatherSourceType == WeatherSourceType.KMA) {
			if (jsonObject.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST.name()) != null &&
					jsonObject.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()) != null) {
				try {
					TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
					VilageFcstResponse ultraSrtNcstResponse =
							tikXml.read(new Buffer().writeUtf8(jsonObject.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(jsonObject.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()).getAsString()),
									VilageFcstResponse.class);

					FinalCurrentConditions finalCurrentConditions =
							KmaResponseProcessor.getFinalCurrentConditions(ultraSrtNcstResponse);
					List<FinalHourlyForecast> finalHourlyForecastList =
							KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstResponse, null);

					currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDto(context, finalCurrentConditions,
							finalHourlyForecastList.get(0), windUnit,
							tempUnit, latitude, longitude);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			JsonElement jsonElement = jsonObject.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS.name());

			CurrentConditionsResponse currentConditionsResponse =
					AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(jsonElement);

			currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
					currentConditionsResponse.getItems().get(0), windUnit, tempUnit, visibilityUnit);
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			OneCallResponse oneCallResponse =
					OpenWeatherMapResponseProcessor.getOneCallObjFromJson(jsonObject.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDto(context, oneCallResponse,
					windUnit, tempUnit, visibilityUnit);
		}
		return currentConditionsDto;
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
			MultipleJsonDownloader.ResponseResult ultraSrtNcstResponseResult =
					multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
			MultipleJsonDownloader.ResponseResult ultraSrtFcstResponseResult =
					multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);

			if (ultraSrtNcstResponseResult.isSuccessful()) {
				FinalCurrentConditions finalCurrentConditions =
						KmaResponseProcessor.getFinalCurrentConditions((VilageFcstResponse) ultraSrtNcstResponseResult.getResponseObj());
				List<FinalHourlyForecast> finalHourlyForecastList =
						KmaResponseProcessor.getFinalHourlyForecastList((VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj(), null);
				UltraSrtNcstParameter ultraSrtNcstParameter = (UltraSrtNcstParameter) ultraSrtNcstResponseResult.getRequestParameter();

				currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDto(context, finalCurrentConditions,
						finalHourlyForecastList.get(0), windUnit,
						tempUnit, ultraSrtNcstParameter.getLatitude(), ultraSrtNcstParameter.getLongitude());
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			MultipleJsonDownloader.ResponseResult currentConditionsResponseResult = multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER)
					.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

			if (currentConditionsResponseResult.isSuccessful()) {
				CurrentConditionsResponse currentConditionsResponse =
						AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson((JsonElement) currentConditionsResponseResult.getResponseObj());

				currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
						currentConditionsResponse.getItems().get(0), windUnit, tempUnit, visibilityUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleJsonDownloader.ResponseResult owmResponseResult = multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP)
					.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (owmResponseResult.isSuccessful()) {
				OneCallResponse oneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson((String) owmResponseResult.getResponseObj());

				currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDto(context, oneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}
		return currentConditionsDto;
	}

	public static List<HourlyForecastDto> parseTextToHourlyForecastDtoList(Context context, JsonObject jsonObject,
	                                                                       WeatherSourceType weatherSourceType, Double latitude,
	                                                                       Double longitude) {
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		if (weatherSourceType == WeatherSourceType.KMA) {
			if (jsonObject.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()) != null &&
					jsonObject.get(RetrofitClient.ServiceType.VILAGE_FCST.name()) != null) {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				try {
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(jsonObject.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse vilageFcstResponse =
							tikXml.read(new Buffer().writeUtf8(jsonObject.get(RetrofitClient.ServiceType.VILAGE_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstResponse,
							vilageFcstResponse);
					hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoList(context, finalHourlyForecastList,
							latitude, longitude, windUnit, tempUnit);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			if (jsonObject.get(RetrofitClient.ServiceType.ACCU_12_HOURLY.name()) != null) {
				TwelveHoursOfHourlyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
						jsonObject.get(RetrofitClient.ServiceType.ACCU_12_HOURLY.name()));

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems()
						, windUnit, tempUnit, visibilityUnit);
			}

		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			if (jsonObject.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()) != null) {
				OneCallResponse oneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(jsonObject.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}
		return hourlyForecastDtoList;
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
						(VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj();
				VilageFcstResponse vilageFcstRoot =
						(VilageFcstResponse) vilageFcstResponseResult.getResponseObj();

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
				TwelveHoursOfHourlyForecastsResponse hourlyForecastsResponse =
						(TwelveHoursOfHourlyForecastsResponse) hourlyForecastResponseResult.getResponseObj();

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems()
						, windUnit, tempUnit, visibilityUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleJsonDownloader.ResponseResult responseResult = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				OneCallResponse oneCallResponse =
						(OneCallResponse) responseResult.getResponseObj();

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}

		return hourlyForecastDtoList;
	}


	public static List<DailyForecastDto> parseTextToDailyForecastDtoList(Context context, JsonObject jsonObject,
	                                                                     WeatherSourceType weatherSourceType) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));

		if (weatherSourceType == WeatherSourceType.KMA) {
			if (jsonObject.get(RetrofitClient.ServiceType.VILAGE_FCST.name()) != null &&
					jsonObject.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()) != null &&
					jsonObject.get(RetrofitClient.ServiceType.MID_TA_FCST.name()) != null &&
					jsonObject.get(RetrofitClient.ServiceType.MID_LAND_FCST.name()) != null &&
					jsonObject.get("tmFc") != null) {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				try {
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(jsonObject.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse vilageFcstResponse =
							tikXml.read(new Buffer().writeUtf8(jsonObject.get(RetrofitClient.ServiceType.VILAGE_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					MidLandFcstResponse midLandFcstResponse =
							tikXml.read(new Buffer().writeUtf8(jsonObject.get(RetrofitClient.ServiceType.MID_LAND_FCST.name()).getAsString()),
									MidLandFcstResponse.class);
					MidTaResponse midTaFcstResponse =
							tikXml.read(new Buffer().writeUtf8(jsonObject.get(RetrofitClient.ServiceType.MID_TA_FCST.name()).getAsString()),
									MidTaResponse.class);

					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstResponse,
							vilageFcstResponse);
					List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(midLandFcstResponse, midTaFcstResponse,
							jsonObject.get("tmFc").getAsLong());

					dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoList(context,
							KmaResponseProcessor.getDailyForecastList(finalDailyForecastList, finalHourlyForecastList), tempUnit);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			if (jsonObject.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY.name()) != null) {
				FiveDaysOfDailyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
						jsonObject.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY.name()).getAsString());

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, hourlyForecastsResponse.getDailyForecasts()
						, windUnit, tempUnit);
			}

		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			if (jsonObject.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()) != null) {
				OneCallResponse oneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(jsonObject.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit);
			}
		}
		return dailyForecastDtoList;
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
						(MidLandFcstResponse) midLandFcstResponseResult.getResponseObj();
				MidTaResponse midTaRoot = (MidTaResponse) midTaFcstResponseResult.getResponseObj();
				VilageFcstResponse vilageFcstRoot = (VilageFcstResponse) vilageFcstResponseResult.getResponseObj();
				VilageFcstResponse ultraSrtFcstRoot = (VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj();

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
				FiveDaysOfDailyForecastsResponse dailyForecastResponse =
						(FiveDaysOfDailyForecastsResponse) dailyForecastResponseResult.getResponseObj();

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, dailyForecastResponse.getDailyForecasts()
						, windUnit, tempUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleJsonDownloader.ResponseResult responseResult = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				OneCallResponse oneCallResponse =
						(OneCallResponse) responseResult.getResponseObj();

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit);
			}
		}

		return dailyForecastDtoList;
	}
}
