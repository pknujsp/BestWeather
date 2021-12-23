package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindUtil;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.tickaroo.tikxml.TikXml;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public static AirQualityDto getAirQualityDto(Context context, MultipleRestApiDownloader multipleRestApiDownloader,
	                                             ZoneOffset zoneOffset) {
		MultipleRestApiDownloader.ResponseResult aqiCnResponseResult = multipleRestApiDownloader.getResponseMap().get(WeatherSourceType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		AirQualityDto airQualityDto = null;

		if (aqiCnResponseResult.isSuccessful()) {
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

		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherSourceType.name());

		if (weatherSourceType == WeatherSourceType.KMA) {

			if (weatherSourceElement.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()) != null) {
				try {
					TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
					VilageFcstResponse ultraSrtNcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()).getAsString()),
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

			JsonElement jsonElement = weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS.name());

			CurrentConditionsResponse currentConditionsResponse =
					AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(jsonElement);

			currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
					currentConditionsResponse.getItems().get(0), windUnit, tempUnit, visibilityUnit);
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			OneCallResponse oneCallResponse =
					OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDto(context, oneCallResponse,
					windUnit, tempUnit, visibilityUnit);
		}
		return currentConditionsDto;
	}

	public static CurrentConditionsDto getCurrentConditionsDto(Context context, MultipleRestApiDownloader multipleRestApiDownloader,
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
			MultipleRestApiDownloader.ResponseResult ultraSrtNcstResponseResult =
					multipleRestApiDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
			MultipleRestApiDownloader.ResponseResult ultraSrtFcstResponseResult =
					multipleRestApiDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);

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
			MultipleRestApiDownloader.ResponseResult currentConditionsResponseResult = multipleRestApiDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER)
					.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

			if (currentConditionsResponseResult.isSuccessful()) {
				CurrentConditionsResponse currentConditionsResponse =
						(CurrentConditionsResponse) currentConditionsResponseResult.getResponseObj();

				currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
						currentConditionsResponse.getItems().get(0), windUnit, tempUnit, visibilityUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleRestApiDownloader.ResponseResult owmResponseResult = multipleRestApiDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP)
					.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (owmResponseResult.isSuccessful()) {
				OneCallResponse oneCallResponse =
						(OneCallResponse) owmResponseResult.getResponseObj();

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

		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherSourceType.name());

		if (weatherSourceType == WeatherSourceType.KMA) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.VILAGE_FCST.name()) != null) {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				try {
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse vilageFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.VILAGE_FCST.name()).getAsString()),
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
			if (weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_12_HOURLY.name()) != null) {
				final String jsonText = weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_12_HOURLY.name()).getAsString();
				JsonArray accuJsonArr = (JsonArray) JsonParser.parseString(jsonText);

				TwelveHoursOfHourlyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
						accuJsonArr);

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems()
						, windUnit, tempUnit, visibilityUnit);
			}

		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()) != null) {
				OneCallResponse oneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}
		return hourlyForecastDtoList;
	}

	public static List<HourlyForecastDto> getHourlyForecastDtoList(Context context, MultipleRestApiDownloader multipleRestApiDownloader,
	                                                               WeatherSourceType weatherSourceType) {
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap = multipleRestApiDownloader.getResponseMap();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		if (weatherSourceType == WeatherSourceType.KMA) {
			MultipleRestApiDownloader.ResponseResult ultraSrtFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(
					RetrofitClient.ServiceType.ULTRA_SRT_FCST);
			MultipleRestApiDownloader.ResponseResult vilageFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(
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
			MultipleRestApiDownloader.ResponseResult hourlyForecastResponseResult = responseMap.get(WeatherSourceType.ACCU_WEATHER).get(
					RetrofitClient.ServiceType.ACCU_12_HOURLY);

			if (hourlyForecastResponseResult.isSuccessful()) {
				TwelveHoursOfHourlyForecastsResponse hourlyForecastsResponse =
						(TwelveHoursOfHourlyForecastsResponse) hourlyForecastResponseResult.getResponseObj();

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems()
						, windUnit, tempUnit, visibilityUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleRestApiDownloader.ResponseResult responseResult = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

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
		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherSourceType.name());


		if (weatherSourceType == WeatherSourceType.KMA) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.VILAGE_FCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.MID_TA_FCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.MID_LAND_FCST.name()) != null &&
					weatherSourceElement.get("tmFc") != null) {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				try {
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse vilageFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.VILAGE_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					MidLandFcstResponse midLandFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.MID_LAND_FCST.name()).getAsString()),
									MidLandFcstResponse.class);
					MidTaResponse midTaFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.MID_TA_FCST.name()).getAsString()),
									MidTaResponse.class);

					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstResponse,
							vilageFcstResponse);
					List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(midLandFcstResponse, midTaFcstResponse,
							weatherSourceElement.get("tmFc").getAsLong());

					dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoList(context,
							KmaResponseProcessor.getDailyForecastList(finalDailyForecastList, finalHourlyForecastList), tempUnit);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY.name()) != null) {
				FiveDaysOfDailyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
						weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY.name()).getAsString());

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, hourlyForecastsResponse.getDailyForecasts()
						, windUnit, tempUnit);
			}

		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()) != null) {
				OneCallResponse oneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit);
			}
		}
		return dailyForecastDtoList;
	}


	public static List<DailyForecastDto> getDailyForecastDtoList(Context context, MultipleRestApiDownloader multipleRestApiDownloader,
	                                                             WeatherSourceType weatherSourceType) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap = multipleRestApiDownloader.getResponseMap();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));

		if (weatherSourceType == WeatherSourceType.KMA) {
			MultipleRestApiDownloader.ResponseResult midLandFcstResponseResult =
					responseMap.get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_LAND_FCST);
			MultipleRestApiDownloader.ResponseResult midTaFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_TA_FCST);
			MultipleRestApiDownloader.ResponseResult vilageFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST);
			MultipleRestApiDownloader.ResponseResult ultraSrtFcstResponseResult = responseMap.get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);

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
						Long.parseLong(multipleRestApiDownloader.get("tmFc")));

				dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoList(context,
						KmaResponseProcessor.getDailyForecastList(finalDailyForecasts, finalHourlyForecasts), tempUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			MultipleRestApiDownloader.ResponseResult dailyForecastResponseResult = responseMap.get(WeatherSourceType.ACCU_WEATHER).get(
					RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

			if (dailyForecastResponseResult.isSuccessful()) {
				FiveDaysOfDailyForecastsResponse dailyForecastResponse =
						(FiveDaysOfDailyForecastsResponse) dailyForecastResponseResult.getResponseObj();

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, dailyForecastResponse.getDailyForecasts()
						, windUnit, tempUnit);
			}
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			MultipleRestApiDownloader.ResponseResult responseResult = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				OneCallResponse oneCallResponse =
						(OneCallResponse) responseResult.getResponseObj();

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(context, oneCallResponse,
						windUnit, tempUnit);
			}
		}

		return dailyForecastDtoList;
	}

	public static CurrentConditionsDto getTempCurrentConditionsDto(Context context) {
		CurrentConditionsDto tempCurrentConditions = new CurrentConditionsDto();
		tempCurrentConditions.setTemp("-100°").setWeatherIcon(R.drawable.day_clear).setWindDirectionDegree(120)
				.setWindDirection(WindUtil.windDirection(context, String.valueOf(tempCurrentConditions.getWindDirectionDegree())))
				.setWindSpeed(ValueUnits.convertWindSpeed("2.6", ValueUnits.mPerSec) + ValueUnits.convertToStr(context, ValueUnits.mPerSec))
				.setHumidity("45%")
				.setWindStrength(WindUtil.getWindSpeedDescription("2.6"));

		return tempCurrentConditions;
	}

	public static List<HourlyForecastDto> getTempHourlyForecastDtoList(Context context, int count) {
		final String tempDegree = "10°";
		final String percent = "%";

		final String zeroSnowVolume = "0.0mm";
		final String zeroRainVolume = "0.0mm";
		final String zeroPrecipitationVolume = "0.0mm";

		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		ZonedDateTime zonedDateTime = ZonedDateTime.now();

		for (int i = 0; i < count; i++) {
			HourlyForecastDto hourlyForecastDto = new HourlyForecastDto();

			hourlyForecastDto.setHours(zonedDateTime)
					.setWeatherIcon(R.drawable.day_clear)
					.setTemp(tempDegree)
					.setPop(10 + percent)
					.setPrecipitationVolume(zeroPrecipitationVolume)
					.setHasRain(false)
					.setHasSnow(false)
					.setRainVolume(zeroRainVolume)
					.setSnowVolume(zeroSnowVolume)
					.setFeelsLikeTemp(tempDegree);

			hourlyForecastDtoList.add(hourlyForecastDto);
			zonedDateTime = zonedDateTime.plusHours(1);
		}
		return hourlyForecastDtoList;
	}

	public static List<DailyForecastDto> getTempDailyForecastDtoList(Context context, int count) {
		final String minTemp = "-10°";
		final String maxTemp = "10°";
		final String percent = "%";

		final String zeroSnowVolume = "0.0mm";
		final String zeroRainVolume = "0.0mm";
		final String zeroPrecipitationVolume = "0.0mm";
		final String pop = "10%";

		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();

		ZonedDateTime zonedDateTime = ZonedDateTime.now();

		for (int i = 0; i < count; i++) {
			DailyForecastDto dailyForecastDto = new DailyForecastDto();

			dailyForecastDto.setAmValues(new DailyForecastDto.Values()).setPmValues(new DailyForecastDto.Values())
					.setMinTemp(minTemp).setMaxTemp(maxTemp).setDate(zonedDateTime).getAmValues().setWeatherIcon(R.drawable.day_clear).setPop(pop)
					.setRainVolume(zeroRainVolume).setSnowVolume(zeroSnowVolume);
			dailyForecastDto.getPmValues().setWeatherIcon(R.drawable.day_clear).setPop(pop)
					.setRainVolume(zeroRainVolume).setSnowVolume(zeroSnowVolume);

			dailyForecastDtoList.add(dailyForecastDto);
			zonedDateTime = zonedDateTime.plusDays(1);
		}
		return dailyForecastDtoList;
	}

	public static AirQualityDto getTempAirQualityDto() {
		AirQualityDto airQualityDto = new AirQualityDto();
		airQualityDto.setAqi(160).setCityName("stationName");

		AirQualityDto.Current current = new AirQualityDto.Current();
		airQualityDto.setCurrent(current);
		current.setO3(10);
		current.setNo2(20);
		current.setCo(30);
		current.setSo2(40);
		current.setPm25(100);
		current.setPm10(200);
		current.setDew(70);

		ZonedDateTime zonedDateTime = ZonedDateTime.now();
		List<AirQualityDto.DailyForecast> dailyForecastList = new ArrayList<>();
		airQualityDto.setDailyForecastList(dailyForecastList);

		AirQualityDto.DailyForecast.Val val = new AirQualityDto.DailyForecast.Val();
		val.setMin(10).setMax(20).setAvg(15);

		for (int i = 0; i < 6; i++) {
			AirQualityDto.DailyForecast dailyForecast = new AirQualityDto.DailyForecast();
			dailyForecast.setDate(zonedDateTime).setUvi(val).setO3(val).setPm25(val).setPm10(val);

			dailyForecastList.add(dailyForecast);
			zonedDateTime = zonedDateTime.plusDays(1);
		}

		return airQualityDto;
	}

	public static WeatherSourceType getMainWeatherSourceType(Set<WeatherSourceType> requestWeatherSourceTypeSet) {
		if (requestWeatherSourceTypeSet.contains(WeatherSourceType.KMA)) {
			return WeatherSourceType.KMA;
		} else if (requestWeatherSourceTypeSet.contains(WeatherSourceType.ACCU_WEATHER)) {
			return WeatherSourceType.ACCU_WEATHER;
		} else {
			return WeatherSourceType.OPEN_WEATHER_MAP;
		}
	}
}
