package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.kma.KmaCurrentConditionsParameters;
import com.lifedawn.bestweather.retrofit.parameters.kma.KmaForecastsParameters;
import com.lifedawn.bestweather.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.AccuCurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.dailyforecasts.AccuDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.hourlyforecasts.AccuHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaCurrentConditions;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaDailyForecast;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaHourlyForecast;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.parser.KmaWebParser;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindUtil;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.tickaroo.tikxml.TikXml;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
		MultipleRestApiDownloader.ResponseResult aqiCnResponseResult = multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		AirQualityDto airQualityDto = null;

		if (aqiCnResponseResult.isSuccessful()) {
			airQualityDto = AqicnResponseProcessor.makeAirQualityDto(context, (AqiCnGeolocalizedFeedResponse) aqiCnResponseResult.getResponseObj(), zoneOffset);
		}
		return airQualityDto;
	}

	public static CurrentConditionsDto parseTextToCurrentConditionsDto(Context context, JsonObject jsonObject,
	                                                                   WeatherDataSourceType weatherDataSourceType, Double latitude,
	                                                                   Double longitude) {
		CurrentConditionsDto currentConditionsDto = null;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherDataSourceType.name());

		if (weatherDataSourceType == WeatherDataSourceType.KMA_API) {

			if (weatherSourceElement.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name()) != null) {
				try {
					TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
					VilageFcstResponse ultraSrtNcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name()).getAsString()),
									VilageFcstResponse.class);

					FinalCurrentConditions finalCurrentConditions =
							KmaResponseProcessor.getFinalCurrentConditionsByXML(ultraSrtNcstResponse);
					List<FinalHourlyForecast> finalHourlyForecastList =
							KmaResponseProcessor.getFinalHourlyForecastListByXML(ultraSrtFcstResponse, null);

					currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfXML(context, finalCurrentConditions,
							finalHourlyForecastList.get(0), windUnit,
							tempUnit, latitude, longitude);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} else if (weatherDataSourceType == WeatherDataSourceType.KMA_WEB) {

			if (weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS.name()) != null) {
				final String lastUpdatedDateTime = jsonObject.get("lastUpdatedDateTime").getAsString();
				Document currentConditionsDocument =
						Jsoup.parse(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS.name()).getAsString());
				Document hourlyForecastDocument =
						Jsoup.parse(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name()).getAsString());
				KmaCurrentConditions kmaCurrentConditions = KmaWebParser.parseCurrentConditions(currentConditionsDocument, lastUpdatedDateTime);
				List<KmaHourlyForecast> kmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(hourlyForecastDocument);

				currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(context, kmaCurrentConditions,
						kmaHourlyForecasts.get(0), windUnit, tempUnit, latitude, longitude);
			}

		} else if (weatherDataSourceType == WeatherDataSourceType.ACCU_WEATHER) {

			JsonElement jsonElement = weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS.name());

			AccuCurrentConditionsResponse accuCurrentConditionsResponse =
					AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(jsonElement);

			currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
					accuCurrentConditionsResponse.getItems().get(0), windUnit, tempUnit, visibilityUnit);
		} else if (weatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			OwmOneCallResponse owmOneCallResponse =
					OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDto(context, owmOneCallResponse,
					windUnit, tempUnit, visibilityUnit);
		}
		return currentConditionsDto;
	}

	public static CurrentConditionsDto getCurrentConditionsDto(Context context, MultipleRestApiDownloader multipleRestApiDownloader,
	                                                           WeatherDataSourceType weatherDataSourceType) {
		CurrentConditionsDto currentConditionsDto = null;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		if (weatherDataSourceType == WeatherDataSourceType.KMA_API) {
			MultipleRestApiDownloader.ResponseResult ultraSrtNcstResponseResult =
					multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.KMA_API).get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST);
			MultipleRestApiDownloader.ResponseResult ultraSrtFcstResponseResult =
					multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.KMA_API).get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);

			if (ultraSrtNcstResponseResult.isSuccessful()) {
				FinalCurrentConditions finalCurrentConditions =
						KmaResponseProcessor.getFinalCurrentConditionsByXML((VilageFcstResponse) ultraSrtNcstResponseResult.getResponseObj());
				List<FinalHourlyForecast> finalHourlyForecastList =
						KmaResponseProcessor.getFinalHourlyForecastListByXML((VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj(), null);
				UltraSrtNcstParameter ultraSrtNcstParameter = (UltraSrtNcstParameter) ultraSrtNcstResponseResult.getRequestParameter();

				currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfXML(context, finalCurrentConditions,
						finalHourlyForecastList.get(0), windUnit,
						tempUnit, ultraSrtNcstParameter.getLatitude(), ultraSrtNcstParameter.getLongitude());
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.KMA_WEB) {
			MultipleRestApiDownloader.ResponseResult currentConditionsResponseResult =
					multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.KMA_WEB).get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS);
			MultipleRestApiDownloader.ResponseResult hourlyForecastsResponseResult =
					multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.KMA_WEB).get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);

			if (currentConditionsResponseResult.isSuccessful() && hourlyForecastsResponseResult.isSuccessful()) {
				KmaCurrentConditions kmaCurrentConditions = (KmaCurrentConditions) currentConditionsResponseResult.getResponseObj();
				Object[] forecasts = (Object[]) hourlyForecastsResponseResult.getResponseObj();

				ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
				KmaCurrentConditionsParameters parameters = (KmaCurrentConditionsParameters) currentConditionsResponseResult.getRequestParameter();

				currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(context, kmaCurrentConditions,
						kmaHourlyForecasts.get(0), windUnit,
						tempUnit, parameters.getLatitude(), parameters.getLongitude());
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.ACCU_WEATHER) {
			MultipleRestApiDownloader.ResponseResult currentConditionsResponseResult = multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.ACCU_WEATHER)
					.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

			if (currentConditionsResponseResult.isSuccessful()) {
				AccuCurrentConditionsResponse accuCurrentConditionsResponse =
						(AccuCurrentConditionsResponse) currentConditionsResponseResult.getResponseObj();

				currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
						accuCurrentConditionsResponse.getItems().get(0), windUnit, tempUnit, visibilityUnit);
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			MultipleRestApiDownloader.ResponseResult owmResponseResult = multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.OWM_ONECALL)
					.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (owmResponseResult.isSuccessful()) {
				OwmOneCallResponse owmOneCallResponse =
						(OwmOneCallResponse) owmResponseResult.getResponseObj();

				currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDto(context, owmOneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}
		return currentConditionsDto;
	}

	public static List<HourlyForecastDto> parseTextToHourlyForecastDtoList(Context context, JsonObject jsonObject,
	                                                                       WeatherDataSourceType weatherDataSourceType, Double latitude,
	                                                                       Double longitude) {
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherDataSourceType.name());

		if (weatherDataSourceType == WeatherDataSourceType.KMA_API) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name()) != null) {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				try {
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse vilageFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastListByXML(ultraSrtFcstResponse,
							vilageFcstResponse);
					hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfXML(context, finalHourlyForecastList,
							latitude, longitude, windUnit, tempUnit);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.KMA_WEB) {
			Document hourlyForecastDocument =
					Jsoup.parse(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name()).getAsString());
			List<KmaHourlyForecast> kmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(hourlyForecastDocument);

			hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(context, kmaHourlyForecasts, latitude, longitude,
					windUnit, tempUnit);
		} else if (weatherDataSourceType == WeatherDataSourceType.ACCU_WEATHER) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST.name()) != null) {
				final String jsonText = weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST.name()).getAsString();
				JsonArray accuJsonArr = (JsonArray) JsonParser.parseString(jsonText);

				AccuHourlyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
						accuJsonArr);

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems()
						, windUnit, tempUnit, visibilityUnit);
			}

		} else if (weatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()) != null) {
				OwmOneCallResponse owmOneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoList(context, owmOneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}
		return hourlyForecastDtoList;
	}

	public static List<HourlyForecastDto> getHourlyForecastDtoList(Context context, MultipleRestApiDownloader multipleRestApiDownloader,
	                                                               WeatherDataSourceType weatherDataSourceType) {
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		Map<WeatherDataSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap = multipleRestApiDownloader.getResponseMap();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		ValueUnits visibilityUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_visibility),
				ValueUnits.km.name()));

		if (weatherDataSourceType == WeatherDataSourceType.KMA_API) {
			MultipleRestApiDownloader.ResponseResult ultraSrtFcstResponseResult = responseMap.get(WeatherDataSourceType.KMA_API).get(
					RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);
			MultipleRestApiDownloader.ResponseResult vilageFcstResponseResult = responseMap.get(WeatherDataSourceType.KMA_API).get(
					RetrofitClient.ServiceType.KMA_VILAGE_FCST);

			if (ultraSrtFcstResponseResult.isSuccessful() && vilageFcstResponseResult.isSuccessful()) {
				VilageFcstResponse ultraSrtFcstRoot =
						(VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj();
				VilageFcstResponse vilageFcstRoot =
						(VilageFcstResponse) vilageFcstResponseResult.getResponseObj();

				VilageFcstParameter vilageFcstParameter = (VilageFcstParameter) vilageFcstResponseResult.getRequestParameter();

				List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastListByXML(ultraSrtFcstRoot,
						vilageFcstRoot);
				hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfXML(context, finalHourlyForecastList,
						vilageFcstParameter.getLatitude(), vilageFcstParameter.getLongitude(), windUnit, tempUnit);
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.KMA_WEB) {
			MultipleRestApiDownloader.ResponseResult hourlyForecastsResponseResult =
					multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.KMA_WEB).get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);

			if (hourlyForecastsResponseResult.isSuccessful()) {
				Object[] forecasts = (Object[]) hourlyForecastsResponseResult.getResponseObj();

				ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
				KmaForecastsParameters parameters = (KmaForecastsParameters) hourlyForecastsResponseResult.getRequestParameter();

				hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(context, kmaHourlyForecasts,
						parameters.getLatitude(), parameters.getLongitude(), windUnit, tempUnit);
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.ACCU_WEATHER) {
			MultipleRestApiDownloader.ResponseResult hourlyForecastResponseResult = responseMap.get(WeatherDataSourceType.ACCU_WEATHER).get(
					RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST);

			if (hourlyForecastResponseResult.isSuccessful()) {
				AccuHourlyForecastsResponse hourlyForecastsResponse =
						(AccuHourlyForecastsResponse) hourlyForecastResponseResult.getResponseObj();

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems()
						, windUnit, tempUnit, visibilityUnit);
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			MultipleRestApiDownloader.ResponseResult responseResult = responseMap.get(WeatherDataSourceType.OWM_ONECALL).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				OwmOneCallResponse owmOneCallResponse =
						(OwmOneCallResponse) responseResult.getResponseObj();

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoList(context, owmOneCallResponse,
						windUnit, tempUnit, visibilityUnit);
			}
		}

		return hourlyForecastDtoList;
	}


	public static List<DailyForecastDto> parseTextToDailyForecastDtoList(Context context, JsonObject jsonObject,
	                                                                     WeatherDataSourceType weatherDataSourceType) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));
		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherDataSourceType.name());


		if (weatherDataSourceType == WeatherDataSourceType.KMA_API) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.KMA_MID_TA_FCST.name()) != null &&
					weatherSourceElement.get(RetrofitClient.ServiceType.KMA_MID_LAND_FCST.name()) != null &&
					weatherSourceElement.get("tmFc") != null) {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				try {
					VilageFcstResponse ultraSrtFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					VilageFcstResponse vilageFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST.name()).getAsString()),
									VilageFcstResponse.class);
					MidLandFcstResponse midLandFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_MID_LAND_FCST.name()).getAsString()),
									MidLandFcstResponse.class);
					MidTaResponse midTaFcstResponse =
							tikXml.read(new Buffer().writeUtf8(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_MID_TA_FCST.name()).getAsString()),
									MidTaResponse.class);

					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastListByXML(ultraSrtFcstResponse,
							vilageFcstResponse);
					List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastListByXML(midLandFcstResponse, midTaFcstResponse,
							weatherSourceElement.get("tmFc").getAsLong());

					dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfXML(
							KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecastList, finalHourlyForecastList), tempUnit);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.KMA_WEB) {
			Document forecastDocument =
					Jsoup.parse(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name()).getAsString());
			List<KmaHourlyForecast> kmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(forecastDocument);
			List<KmaDailyForecast> kmaDailyForecasts = KmaWebParser.parseDailyForecasts(forecastDocument);
			KmaWebParser.makeExtendedDailyForecasts(kmaHourlyForecasts, kmaDailyForecasts);

			dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts, tempUnit);
		} else if (weatherDataSourceType == WeatherDataSourceType.ACCU_WEATHER) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST.name()) != null) {
				AccuDailyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
						weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST.name()).getAsString());

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, hourlyForecastsResponse.getDailyForecasts()
						, windUnit, tempUnit);
			}

		} else if (weatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()) != null) {
				OwmOneCallResponse owmOneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(context, owmOneCallResponse,
						windUnit, tempUnit);
			}
		}
		return dailyForecastDtoList;
	}


	public static List<DailyForecastDto> getDailyForecastDtoList(Context context, MultipleRestApiDownloader multipleRestApiDownloader,
	                                                             WeatherDataSourceType weatherDataSourceType) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		Map<WeatherDataSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap = multipleRestApiDownloader.getResponseMap();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		ValueUnits windUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_wind),
				ValueUnits.mPerSec.name()));
		ValueUnits tempUnit = ValueUnits.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_unit_temp),
				ValueUnits.celsius.name()));

		if (weatherDataSourceType == WeatherDataSourceType.KMA_API) {
			MultipleRestApiDownloader.ResponseResult midLandFcstResponseResult =
					responseMap.get(WeatherDataSourceType.KMA_API).get(RetrofitClient.ServiceType.KMA_MID_LAND_FCST);
			MultipleRestApiDownloader.ResponseResult midTaFcstResponseResult = responseMap.get(WeatherDataSourceType.KMA_API).get(RetrofitClient.ServiceType.KMA_MID_TA_FCST);
			MultipleRestApiDownloader.ResponseResult vilageFcstResponseResult = responseMap.get(WeatherDataSourceType.KMA_API).get(RetrofitClient.ServiceType.KMA_VILAGE_FCST);
			MultipleRestApiDownloader.ResponseResult ultraSrtFcstResponseResult = responseMap.get(WeatherDataSourceType.KMA_API).get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);

			if (midLandFcstResponseResult.isSuccessful() && midTaFcstResponseResult.isSuccessful() &&
					vilageFcstResponseResult.isSuccessful() && ultraSrtFcstResponseResult.isSuccessful()) {
				MidLandFcstResponse midLandFcstRoot =
						(MidLandFcstResponse) midLandFcstResponseResult.getResponseObj();
				MidTaResponse midTaRoot = (MidTaResponse) midTaFcstResponseResult.getResponseObj();
				VilageFcstResponse vilageFcstRoot = (VilageFcstResponse) vilageFcstResponseResult.getResponseObj();
				VilageFcstResponse ultraSrtFcstRoot = (VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj();

				List<FinalHourlyForecast> finalHourlyForecasts = KmaResponseProcessor.getFinalHourlyForecastListByXML(ultraSrtFcstRoot,
						vilageFcstRoot);
				List<FinalDailyForecast> finalDailyForecasts = KmaResponseProcessor.getFinalDailyForecastListByXML(midLandFcstRoot, midTaRoot,
						Long.parseLong(multipleRestApiDownloader.get("tmFc")));

				dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfXML(
						KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecasts, finalHourlyForecasts), tempUnit);
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.KMA_WEB) {
			MultipleRestApiDownloader.ResponseResult dailyForecastsResponseResult =
					multipleRestApiDownloader.getResponseMap().get(WeatherDataSourceType.KMA_WEB).get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);

			if (dailyForecastsResponseResult.isSuccessful()) {
				Object[] forecasts = (Object[]) dailyForecastsResponseResult.getResponseObj();
				ArrayList<KmaDailyForecast> kmaDailyForecasts = (ArrayList<KmaDailyForecast>) forecasts[1];

				dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts, tempUnit);
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.ACCU_WEATHER) {
			MultipleRestApiDownloader.ResponseResult dailyForecastResponseResult = responseMap.get(WeatherDataSourceType.ACCU_WEATHER).get(
					RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);

			if (dailyForecastResponseResult.isSuccessful()) {
				AccuDailyForecastsResponse dailyForecastResponse =
						(AccuDailyForecastsResponse) dailyForecastResponseResult.getResponseObj();

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, dailyForecastResponse.getDailyForecasts()
						, windUnit, tempUnit);
			}
		} else if (weatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			MultipleRestApiDownloader.ResponseResult responseResult = responseMap.get(WeatherDataSourceType.OWM_ONECALL).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				OwmOneCallResponse owmOneCallResponse =
						(OwmOneCallResponse) responseResult.getResponseObj();

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(context, owmOneCallResponse,
						windUnit, tempUnit);
			}
		}

		return dailyForecastDtoList;
	}

	public static CurrentConditionsDto getTempCurrentConditionsDto(Context context) {
		CurrentConditionsDto tempCurrentConditions = new CurrentConditionsDto();
		tempCurrentConditions.setTemp("-100°").setWeatherIcon(R.drawable.day_clear).setWindDirectionDegree(120)
				.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, String.valueOf(tempCurrentConditions.getWindDirectionDegree())))
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

	public static WeatherDataSourceType getMainWeatherSourceType(Set<WeatherDataSourceType> requestWeatherDataSourceTypeSet) {
		if (requestWeatherDataSourceTypeSet.contains(WeatherDataSourceType.KMA_WEB)) {
			return WeatherDataSourceType.KMA_WEB;
		} else if (requestWeatherDataSourceTypeSet.contains(WeatherDataSourceType.ACCU_WEATHER)) {
			return WeatherDataSourceType.ACCU_WEATHER;
		} else {
			return WeatherDataSourceType.OWM_ONECALL;
		}
	}
}
