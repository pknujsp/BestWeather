package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.util.ArrayMap;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaCurrentConditionsParameters;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.KmaForecastsParameters;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.UltraSrtNcstParameter;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.kma.VilageFcstParameter;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.currentconditions.AccuCurrentConditionsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.dailyforecasts.AccuDailyForecastsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.hourlyforecasts.AccuHourlyForecastsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.html.KmaCurrentConditions;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.html.KmaDailyForecast;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.html.KmaHourlyForecast;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.LocationForecastResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.currentweather.OwmCurrentConditionsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.dailyforecast.OwmDailyForecastResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.hourlyforecast.OwmHourlyForecastResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.data.remote.retrofit.callback.WeatherRestApiDownloader;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okio.Buffer;
import us.dustinj.timezonemap.TimeZoneMap;

public class WeatherResponseProcessor {
	public static final Map<String, ZoneId> TIMEZONE_MAP = new HashMap<>();

	public static boolean containsTimeZone(Double latitude, Double longitude) {
		return TIMEZONE_MAP.containsKey(latitude.toString() + longitude.toString());
	}

	@Nullable
	public static ZoneId getTimeZone(Double latitude, Double longitude) {
		return TIMEZONE_MAP.get(latitude.toString() + longitude.toString());
	}

	public static ZoneId putTimeZone(Double latitude, Double longitude, ZoneId zoneId) {
		return TIMEZONE_MAP.put(latitude.toString() + longitude.toString(), zoneId);
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

	public static ZoneId getZoneId(Double latitude, Double longitude) {
		final String key = latitude.toString() + longitude.toString();

		if (TIMEZONE_MAP.containsKey(key)) {
			return TIMEZONE_MAP.get(key);
		}

		TimeZoneMap map = TimeZoneMap.forRegion(latitude - 4.0,
				longitude - 4.0, latitude + 4.0
				, longitude + 4.0);

		ZoneId zoneId = ZoneId.of(map.getOverlappingTimeZone(latitude, longitude).getZoneId());
		TIMEZONE_MAP.put(key, zoneId);

		return zoneId;
	}

	public static AirQualityDto getAirQualityDto(WeatherRestApiDownloader weatherRestApiDownloader,
	                                             ZoneOffset zoneOffset) {
		if (weatherRestApiDownloader == null) {
			return null;
		}
		WeatherRestApiDownloader.ResponseResult aqiCnResponseResult = weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

		return AqicnResponseProcessor.makeAirQualityDto((AqiCnGeolocalizedFeedResponse) aqiCnResponseResult.getResponseObj(),
				zoneOffset);
	}

	public static CurrentConditionsDto parseTextToCurrentConditionsDto(Context context, JsonObject jsonObject,
	                                                                   WeatherProviderType weatherProviderType, Double latitude,
	                                                                   Double longitude, ZoneId zoneId) {
		CurrentConditionsDto currentConditionsDto = null;
		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherProviderType.name());

		if (weatherProviderType == WeatherProviderType.KMA_API) {

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
							finalHourlyForecastList.get(0),
							latitude, longitude);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} else if (weatherProviderType == WeatherProviderType.KMA_WEB) {

			if (weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS.name()) != null) {
				final String lastUpdatedDateTime = jsonObject.get("lastUpdatedDateTime").getAsString();
				Document currentConditionsDocument =
						Jsoup.parse(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS.name()).getAsString());
				Document hourlyForecastDocument =
						Jsoup.parse(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name()).getAsString());
				KmaCurrentConditions kmaCurrentConditions = KmaWebParser.parseCurrentConditions(currentConditionsDocument, lastUpdatedDateTime);
				List<KmaHourlyForecast> kmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(hourlyForecastDocument);

				currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(context, kmaCurrentConditions,
						kmaHourlyForecasts.get(0), latitude, longitude);
			}

		} else if (weatherProviderType == WeatherProviderType.ACCU_WEATHER) {

			JsonElement jsonElement = weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS.name());

			AccuCurrentConditionsResponse accuCurrentConditionsResponse =
					AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(jsonElement);

			currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
					accuCurrentConditionsResponse.getItems().get(0));
		} else if (weatherProviderType == WeatherProviderType.OWM_ONECALL) {
			OwmOneCallResponse owmOneCallResponse =
					OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoOneCall(context, owmOneCallResponse, zoneId
			);
		} else if (weatherProviderType == WeatherProviderType.MET_NORWAY) {
			LocationForecastResponse metNorwayResponse =
					MetNorwayResponseProcessor.getLocationForecastResponseObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST.name()).getAsString());

			currentConditionsDto = MetNorwayResponseProcessor.makeCurrentConditionsDto(context, metNorwayResponse,
					zoneId);
		} else if (weatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
			OwmCurrentConditionsResponse owmCurrentConditionsResponse =
					OpenWeatherMapResponseProcessor.getOwmCurrentConditionsResponseFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS.name()).getAsString());

			currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoIndividual(context,
					owmCurrentConditionsResponse, zoneId
			);
		}
		return currentConditionsDto;
	}

	public static CurrentConditionsDto getCurrentConditionsDto(Context context, WeatherRestApiDownloader weatherRestApiDownloader,
	                                                           WeatherProviderType weatherProviderType, ZoneId zoneId) {
		if (weatherRestApiDownloader == null) {
			return null;
		}
		CurrentConditionsDto currentConditionsDto = null;

		if (weatherProviderType == WeatherProviderType.KMA_API) {
			WeatherRestApiDownloader.ResponseResult ultraSrtNcstResponseResult =
					weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.KMA_API).get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST);
			WeatherRestApiDownloader.ResponseResult ultraSrtFcstResponseResult =
					weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.KMA_API).get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);

			if (ultraSrtNcstResponseResult != null && ultraSrtFcstResponseResult != null && ultraSrtNcstResponseResult.isSuccessful()) {
				FinalCurrentConditions finalCurrentConditions =
						KmaResponseProcessor.getFinalCurrentConditionsByXML((VilageFcstResponse) ultraSrtNcstResponseResult.getResponseObj());
				List<FinalHourlyForecast> finalHourlyForecastList =
						KmaResponseProcessor.getFinalHourlyForecastListByXML((VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj(), null);
				UltraSrtNcstParameter ultraSrtNcstParameter = (UltraSrtNcstParameter) ultraSrtNcstResponseResult.getRequestParameter();

				currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfXML(context, finalCurrentConditions,
						finalHourlyForecastList.get(0),
						ultraSrtNcstParameter.getLatitude(), ultraSrtNcstParameter.getLongitude());
			}
		} else if (weatherProviderType == WeatherProviderType.KMA_WEB) {
			WeatherRestApiDownloader.ResponseResult currentConditionsResponseResult =
					weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.KMA_WEB).get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS);
			WeatherRestApiDownloader.ResponseResult hourlyForecastsResponseResult =
					weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.KMA_WEB).get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);

			if (currentConditionsResponseResult != null && hourlyForecastsResponseResult != null &&
					currentConditionsResponseResult.isSuccessful() && hourlyForecastsResponseResult.isSuccessful()) {
				KmaCurrentConditions kmaCurrentConditions = (KmaCurrentConditions) currentConditionsResponseResult.getResponseObj();
				Object[] forecasts = (Object[]) hourlyForecastsResponseResult.getResponseObj();

				ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
				KmaCurrentConditionsParameters parameters = (KmaCurrentConditionsParameters) currentConditionsResponseResult.getRequestParameter();

				currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(context, kmaCurrentConditions,
						kmaHourlyForecasts.get(0),
						parameters.getLatitude(), parameters.getLongitude());
			}
		} else if (weatherProviderType == WeatherProviderType.ACCU_WEATHER) {
			WeatherRestApiDownloader.ResponseResult currentConditionsResponseResult = weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.ACCU_WEATHER)
					.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

			if (currentConditionsResponseResult != null && currentConditionsResponseResult.isSuccessful()) {
				AccuCurrentConditionsResponse accuCurrentConditionsResponse =
						(AccuCurrentConditionsResponse) currentConditionsResponseResult.getResponseObj();

				currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context,
						accuCurrentConditionsResponse.getItems().get(0));
			}
		} else if (weatherProviderType == WeatherProviderType.OWM_ONECALL) {
			WeatherRestApiDownloader.ResponseResult owmResponseResult = weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.OWM_ONECALL)
					.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (owmResponseResult != null && owmResponseResult.isSuccessful()) {
				OwmOneCallResponse owmOneCallResponse =
						(OwmOneCallResponse) owmResponseResult.getResponseObj();

				currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoOneCall(context, owmOneCallResponse, zoneId
				);
			}
		} else if (weatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
			WeatherRestApiDownloader.ResponseResult owmCurrentConditionsResponseResult =
					weatherRestApiDownloader.getResponseMap().get(weatherProviderType)
							.get(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS);

			if (owmCurrentConditionsResponseResult != null && owmCurrentConditionsResponseResult.isSuccessful()) {
				OwmCurrentConditionsResponse owmCurrentConditionsResponse =
						(OwmCurrentConditionsResponse) owmCurrentConditionsResponseResult.getResponseObj();

				currentConditionsDto = OpenWeatherMapResponseProcessor.makeCurrentConditionsDtoIndividual(context,
						owmCurrentConditionsResponse, zoneId
				);
			}
		} else if (weatherProviderType == WeatherProviderType.MET_NORWAY) {
			WeatherRestApiDownloader.ResponseResult metResponseResult =
					weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.MET_NORWAY)
							.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

			if (metResponseResult != null && metResponseResult.isSuccessful()) {
				LocationForecastResponse metResponse =
						(LocationForecastResponse) metResponseResult.getResponseObj();

				currentConditionsDto = MetNorwayResponseProcessor.makeCurrentConditionsDto(context, metResponse, zoneId);
			}
		}

		return currentConditionsDto;
	}

	public static List<HourlyForecastDto> parseTextToHourlyForecastDtoList(Context context, JsonObject jsonObject,
	                                                                       WeatherProviderType weatherProviderType, Double latitude,
	                                                                       Double longitude, ZoneId zoneId) {
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherProviderType.name());

		if (weatherProviderType == WeatherProviderType.KMA_API) {
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
							latitude, longitude);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (weatherProviderType == WeatherProviderType.KMA_WEB) {
			Document hourlyForecastDocument =
					Jsoup.parse(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name()).getAsString());
			List<KmaHourlyForecast> kmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(hourlyForecastDocument);

			hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(context, kmaHourlyForecasts, latitude, longitude
			);
		} else if (weatherProviderType == WeatherProviderType.ACCU_WEATHER) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST.name()) != null) {
				final String jsonText = weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST.name()).getAsString();
				JsonArray accuJsonArr = (JsonArray) JsonParser.parseString(jsonText);

				AccuHourlyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
						accuJsonArr);

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems());
			}

		} else if (weatherProviderType == WeatherProviderType.OWM_ONECALL) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()) != null) {
				OwmOneCallResponse owmOneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListOneCall(context, owmOneCallResponse, zoneId
				);
			}
		} else if (weatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
			OwmHourlyForecastResponse owmHourlyForecastResponse =
					OpenWeatherMapResponseProcessor.getOwmHourlyForecastResponseFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST.name()).getAsString());

			hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListIndividual(context, owmHourlyForecastResponse, zoneId
			);
		} else if (weatherProviderType == WeatherProviderType.MET_NORWAY) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST.name()) != null) {
				LocationForecastResponse metNorwayResponse =
						MetNorwayResponseProcessor.getLocationForecastResponseObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST.name()).getAsString());

				hourlyForecastDtoList = MetNorwayResponseProcessor.makeHourlyForecastDtoList(context, metNorwayResponse,
						zoneId);
			}
		}

		return hourlyForecastDtoList;
	}

	public static List<HourlyForecastDto> getHourlyForecastDtoList(Context context, WeatherRestApiDownloader
			weatherRestApiDownloader, WeatherProviderType weatherProviderType, ZoneId zoneId) {
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		if (weatherRestApiDownloader == null) {
			return hourlyForecastDtoList;
		}

		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult>> responseMap = weatherRestApiDownloader.getResponseMap();

		if (weatherProviderType == WeatherProviderType.KMA_API) {
			WeatherRestApiDownloader.ResponseResult ultraSrtFcstResponseResult = responseMap.get(WeatherProviderType.KMA_API).get(
					RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);
			WeatherRestApiDownloader.ResponseResult vilageFcstResponseResult = responseMap.get(WeatherProviderType.KMA_API).get(
					RetrofitClient.ServiceType.KMA_VILAGE_FCST);

			if (ultraSrtFcstResponseResult != null && vilageFcstResponseResult != null
					&& ultraSrtFcstResponseResult.isSuccessful() && vilageFcstResponseResult.isSuccessful()) {
				VilageFcstResponse ultraSrtFcstRoot =
						(VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj();
				VilageFcstResponse vilageFcstRoot =
						(VilageFcstResponse) vilageFcstResponseResult.getResponseObj();

				VilageFcstParameter vilageFcstParameter = (VilageFcstParameter) vilageFcstResponseResult.getRequestParameter();

				List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastListByXML(ultraSrtFcstRoot,
						vilageFcstRoot);
				hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfXML(context, finalHourlyForecastList,
						vilageFcstParameter.getLatitude(), vilageFcstParameter.getLongitude());
			}
		} else if (weatherProviderType == WeatherProviderType.KMA_WEB) {
			WeatherRestApiDownloader.ResponseResult hourlyForecastsResponseResult =
					weatherRestApiDownloader.getResponseMap().get(weatherProviderType).get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);

			if (hourlyForecastsResponseResult != null && hourlyForecastsResponseResult.isSuccessful()) {
				Object[] forecasts = (Object[]) hourlyForecastsResponseResult.getResponseObj();

				ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
				KmaForecastsParameters parameters = (KmaForecastsParameters) hourlyForecastsResponseResult.getRequestParameter();

				hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(context, kmaHourlyForecasts,
						parameters.getLatitude(), parameters.getLongitude());
			}
		} else if (weatherProviderType == WeatherProviderType.ACCU_WEATHER) {
			WeatherRestApiDownloader.ResponseResult hourlyForecastResponseResult = responseMap.get(weatherProviderType).get(
					RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST);

			if (hourlyForecastResponseResult != null && hourlyForecastResponseResult.isSuccessful()) {
				AccuHourlyForecastsResponse hourlyForecastsResponse =
						(AccuHourlyForecastsResponse) hourlyForecastResponseResult.getResponseObj();

				hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, hourlyForecastsResponse.getItems());
			}
		} else if (weatherProviderType == WeatherProviderType.OWM_ONECALL) {
			WeatherRestApiDownloader.ResponseResult responseResult = responseMap.get(weatherProviderType).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult != null && responseResult.isSuccessful()) {
				OwmOneCallResponse owmOneCallResponse =
						(OwmOneCallResponse) responseResult.getResponseObj();

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListOneCall(context, owmOneCallResponse,
						zoneId);
			}
		} else if (weatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
			WeatherRestApiDownloader.ResponseResult owmHourlyForecastResponseResult =
					weatherRestApiDownloader.getResponseMap().get(weatherProviderType)
							.get(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST);

			if (owmHourlyForecastResponseResult != null && owmHourlyForecastResponseResult.isSuccessful()) {
				OwmHourlyForecastResponse owmHourlyForecastResponse =
						(OwmHourlyForecastResponse) owmHourlyForecastResponseResult.getResponseObj();

				hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListIndividual(context,
						owmHourlyForecastResponse, zoneId
				);
			}
		} else if (weatherProviderType == WeatherProviderType.MET_NORWAY) {
			WeatherRestApiDownloader.ResponseResult responseResult =
					responseMap.get(weatherProviderType).get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

			if (responseResult != null && responseResult.isSuccessful()) {
				LocationForecastResponse metNorwayResponse =
						(LocationForecastResponse) responseResult.getResponseObj();

				hourlyForecastDtoList = MetNorwayResponseProcessor.makeHourlyForecastDtoList(context, metNorwayResponse, zoneId);
			}
		}

		return hourlyForecastDtoList;
	}


	public static List<DailyForecastDto> parseTextToDailyForecastDtoList(Context context, JsonObject jsonObject,
	                                                                     WeatherProviderType weatherProviderType, ZoneId zoneId) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		JsonObject weatherSourceElement = jsonObject.getAsJsonObject(weatherProviderType.name());

		if (weatherProviderType == WeatherProviderType.KMA_API) {
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
							KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecastList, finalHourlyForecastList));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (weatherProviderType == WeatherProviderType.KMA_WEB) {
			Document forecastDocument =
					Jsoup.parse(weatherSourceElement.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS.name()).getAsString());
			List<KmaHourlyForecast> kmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(forecastDocument);
			List<KmaDailyForecast> kmaDailyForecasts = KmaWebParser.parseDailyForecasts(forecastDocument);
			KmaWebParser.makeExtendedDailyForecasts(kmaHourlyForecasts, kmaDailyForecasts);

			dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts);
		} else if (weatherProviderType == WeatherProviderType.ACCU_WEATHER) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST.name()) != null) {
				AccuDailyForecastsResponse hourlyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
						weatherSourceElement.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST.name()).getAsString());

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, hourlyForecastsResponse.getDailyForecasts()
				);
			}

		} else if (weatherProviderType == WeatherProviderType.OWM_ONECALL) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()) != null) {
				OwmOneCallResponse owmOneCallResponse =
						OpenWeatherMapResponseProcessor.getOneCallObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_ONE_CALL.name()).getAsString());

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListOneCall(context, owmOneCallResponse,
						zoneId);
			}
		} else if (weatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.OWM_DAILY_FORECAST.name()) != null) {
				OwmDailyForecastResponse owmDailyForecastResponse =
						OpenWeatherMapResponseProcessor.getOwmDailyForecastResponseFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.OWM_DAILY_FORECAST.name()).getAsString());

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListIndividual(context, owmDailyForecastResponse,
						zoneId);
			}
		} else if (weatherProviderType == WeatherProviderType.MET_NORWAY) {
			if (weatherSourceElement.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST.name()) != null) {
				LocationForecastResponse metNorwayResponse =
						MetNorwayResponseProcessor.getLocationForecastResponseObjFromJson(weatherSourceElement.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST.name()).getAsString());

				dailyForecastDtoList = MetNorwayResponseProcessor.makeDailyForecastDtoList(context, metNorwayResponse, zoneId);
			}
		}

		return dailyForecastDtoList;
	}


	public static List<DailyForecastDto> getDailyForecastDtoList(Context context, WeatherRestApiDownloader weatherRestApiDownloader,
	                                                             WeatherProviderType weatherProviderType, ZoneId zoneId) {
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		if (weatherRestApiDownloader == null) {
			return dailyForecastDtoList;
		}
		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult>> responseMap = weatherRestApiDownloader.getResponseMap();

		if (weatherProviderType == WeatherProviderType.KMA_API) {
			WeatherRestApiDownloader.ResponseResult midLandFcstResponseResult =
					responseMap.get(WeatherProviderType.KMA_API).get(RetrofitClient.ServiceType.KMA_MID_LAND_FCST);
			WeatherRestApiDownloader.ResponseResult midTaFcstResponseResult = responseMap.get(WeatherProviderType.KMA_API).get(RetrofitClient.ServiceType.KMA_MID_TA_FCST);
			WeatherRestApiDownloader.ResponseResult vilageFcstResponseResult = responseMap.get(WeatherProviderType.KMA_API).get(RetrofitClient.ServiceType.KMA_VILAGE_FCST);
			WeatherRestApiDownloader.ResponseResult ultraSrtFcstResponseResult = responseMap.get(WeatherProviderType.KMA_API).get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);

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
						Long.parseLong(weatherRestApiDownloader.get("tmFc")));

				dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfXML(
						KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecasts, finalHourlyForecasts));
			}
		} else if (weatherProviderType == WeatherProviderType.KMA_WEB) {
			WeatherRestApiDownloader.ResponseResult dailyForecastsResponseResult =
					weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.KMA_WEB).get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);

			if (dailyForecastsResponseResult.isSuccessful()) {
				Object[] forecasts = (Object[]) dailyForecastsResponseResult.getResponseObj();
				ArrayList<KmaDailyForecast> kmaDailyForecasts = (ArrayList<KmaDailyForecast>) forecasts[1];

				dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts);
			}
		} else if (weatherProviderType == WeatherProviderType.ACCU_WEATHER) {
			WeatherRestApiDownloader.ResponseResult dailyForecastResponseResult = responseMap.get(WeatherProviderType.ACCU_WEATHER).get(
					RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);

			if (dailyForecastResponseResult.isSuccessful()) {
				AccuDailyForecastsResponse dailyForecastResponse =
						(AccuDailyForecastsResponse) dailyForecastResponseResult.getResponseObj();

				dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context, dailyForecastResponse.getDailyForecasts()
				);
			}
		} else if (weatherProviderType == WeatherProviderType.OWM_ONECALL) {
			WeatherRestApiDownloader.ResponseResult responseResult = responseMap.get(WeatherProviderType.OWM_ONECALL).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				OwmOneCallResponse owmOneCallResponse =
						(OwmOneCallResponse) responseResult.getResponseObj();

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListOneCall(context, owmOneCallResponse, zoneId
				);
			}
		} else if (weatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
			WeatherRestApiDownloader.ResponseResult responseResult =
					responseMap.get(weatherProviderType).get(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

			if (responseResult.isSuccessful()) {
				OwmDailyForecastResponse owmDailyForecastResponse =
						(OwmDailyForecastResponse) responseResult.getResponseObj();

				dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoListIndividual(context,
						owmDailyForecastResponse, zoneId);
			}
		} else if (weatherProviderType == WeatherProviderType.MET_NORWAY) {
			WeatherRestApiDownloader.ResponseResult responseResult =
					responseMap.get(WeatherProviderType.MET_NORWAY).get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

			if (responseResult.isSuccessful()) {
				LocationForecastResponse metResponse =
						(LocationForecastResponse) responseResult.getResponseObj();

				dailyForecastDtoList = MetNorwayResponseProcessor.makeDailyForecastDtoList(context, metResponse,
						zoneId);
			}
		}

		return dailyForecastDtoList;
	}

	public static CurrentConditionsDto getTempCurrentConditionsDto(Context context) {
		CurrentConditionsDto tempCurrentConditions = new CurrentConditionsDto();
		tempCurrentConditions.setTemp(context.getString(R.string.temp_temperature)).setFeelsLikeTemp(context.getString(R.string.temp_temperature)).setWeatherIcon(R.drawable.day_clear).setWindDirectionDegree(Integer.parseInt(context.getString(R.string.temp_tempWindDirectionDegree)))
				.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, String.valueOf(tempCurrentConditions.getWindDirectionDegree())))
				.setWindSpeed(context.getString(R.string.temp_windSpeed))
				.setHumidity(context.getString(R.string.temp_humidity))
				.setWindStrength(context.getString(R.string.temp_simpleWindStrength));

		return tempCurrentConditions;
	}

	public static List<HourlyForecastDto> getTempHourlyForecastDtoList(Context context, int count) {
		final String tempDegree = context.getString(R.string.temp_temperature);
		final String zeroSnowVolume = context.getString(R.string.temp_snowVolume);
		final String zeroRainVolume = context.getString(R.string.temp_rainVolume);

		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		ZonedDateTime zonedDateTime = ZonedDateTime.now();

		for (int i = 0; i < count; i++) {
			HourlyForecastDto hourlyForecastDto = new HourlyForecastDto();

			hourlyForecastDto.setHours(zonedDateTime)
					.setWeatherIcon(R.drawable.day_clear)
					.setTemp(tempDegree)
					.setPop(context.getString(R.string.temp_pop))
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
		final String minTemp = context.getString(R.string.temp_minTemperature);
		final String maxTemp = context.getString(R.string.temp_maxTemperature);

		final String zeroSnowVolume = context.getString(R.string.temp_snowVolume);
		final String zeroRainVolume = context.getString(R.string.temp_rainVolume);
		final String pop = context.getString(R.string.temp_pop);

		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();

		ZonedDateTime zonedDateTime = ZonedDateTime.now();

		for (int i = 0; i < count; i++) {
			DailyForecastDto dailyForecastDto = new DailyForecastDto();
			dailyForecastDto.getValuesList().add(new DailyForecastDto.Values());
			dailyForecastDto.getValuesList().add(new DailyForecastDto.Values());

			dailyForecastDto.setMinTemp(minTemp).setMaxTemp(maxTemp).setDate(zonedDateTime).getValuesList().get(0).setWeatherIcon(R.drawable.day_clear).setPop(pop)
					.setRainVolume(zeroRainVolume).setSnowVolume(zeroSnowVolume);
			dailyForecastDto.getValuesList().get(1).setWeatherIcon(R.drawable.day_clear).setPop(pop)
					.setRainVolume(zeroRainVolume).setSnowVolume(zeroSnowVolume);

			dailyForecastDtoList.add(dailyForecastDto);
			zonedDateTime = zonedDateTime.plusDays(1);
		}
		return dailyForecastDtoList;
	}

	public static AirQualityDto getTempAirQualityDto() {
		AirQualityDto airQualityDto = new AirQualityDto();
		airQualityDto.setSuccessful(true);
		airQualityDto.setAqi(160).setCityName("CityName");

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

		for (int i = 0; i < 7; i++) {
			AirQualityDto.DailyForecast dailyForecast = new AirQualityDto.DailyForecast();
			dailyForecast.setDate(zonedDateTime).setUvi(val).setO3(val).setPm25(val).setPm10(val);

			dailyForecastList.add(dailyForecast);
			zonedDateTime = zonedDateTime.plusDays(1);
		}

		return airQualityDto;
	}

	public static WeatherProviderType getMainWeatherSourceType(Set<WeatherProviderType> requestWeatherProviderTypeSet) {
		if (requestWeatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			return WeatherProviderType.KMA_WEB;
		} else if (requestWeatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
			return WeatherProviderType.ACCU_WEATHER;
		} else if (requestWeatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
			return WeatherProviderType.OWM_ONECALL;
		} else if (requestWeatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
			return WeatherProviderType.MET_NORWAY;
		} else {
			return WeatherProviderType.OWM_INDIVIDUAL;
		}
	}
}
