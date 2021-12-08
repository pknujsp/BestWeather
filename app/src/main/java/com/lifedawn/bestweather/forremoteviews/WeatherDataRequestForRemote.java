package com.lifedawn.bestweather.forremoteviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.forremoteviews.dto.CurrentConditionsObj;
import com.lifedawn.bestweather.forremoteviews.dto.DailyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.HeaderObj;
import com.lifedawn.bestweather.forremoteviews.dto.HourlyForecastObj;
import com.lifedawn.bestweather.forremoteviews.dto.WeatherJsonObj;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class WeatherDataRequestForRemote {
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private Context context;

	public WeatherDataRequestForRemote(Context context) {
		this.context = context;
	}


	public void loadWeatherData(Context context, String preferenceName,
	                            Set<RequestWeatherDataType> requestWeatherDataTypeSet,
	                            MultipleJsonDownloader multipleJsonDownloader) {
		SharedPreferences attributes =
				context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
		WeatherSourceType weatherSourceType =
				WeatherSourceType.valueOf(attributes.getString(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name(),
						WeatherSourceType.OPEN_WEATHER_MAP.name()));
		String countryCode = attributes.getString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), "");
		if (attributes.getBoolean(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name(), true) &&
				countryCode.equals("KR")) {
			weatherSourceType = WeatherSourceType.KMA;
		}

		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources =
				makeRequestWeatherSources(weatherSourceType);

		WeatherRequestUtil.setRequestWeatherSourceWithSourceType(weatherSourceType, requestWeatherSources, requestWeatherDataTypeSet);

		Double latitude = Double.parseDouble(attributes.getString(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), "0.0"));
		Double longitude = Double.parseDouble(attributes.getString(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), "0.0"));

		WeatherSourceType finalWeatherSourceType = weatherSourceType;
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				initWeatherSourceUniqueValues(finalWeatherSourceType);
				MainProcessing.requestNewWeatherData(context, latitude, longitude,
						requestWeatherSources, multipleJsonDownloader);
			}
		});

	}

	private ArrayMap<WeatherSourceType, RequestWeatherSource> makeRequestWeatherSources(WeatherSourceType weatherSourceType) {
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		RequestWeatherSource requestWeatherSource = null;

		if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
			requestWeatherSource = new RequestAccu();
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
			requestWeatherSource = new RequestOwm();
		} else if (weatherSourceType == WeatherSourceType.KMA) {
			requestWeatherSource = new RequestKma();
		}
		requestWeatherSources.put(weatherSourceType, requestWeatherSource);
		return requestWeatherSources;
	}

	private void initWeatherSourceUniqueValues(WeatherSourceType weatherSourceType) {
		switch (weatherSourceType) {
			case KMA:
				KmaResponseProcessor.init(context);
				break;
			case ACCU_WEATHER:
				AccuWeatherResponseProcessor.init(context);
				break;
			case OPEN_WEATHER_MAP:
				OpenWeatherMapResponseProcessor.init(context);
				break;
		}
		AqicnResponseProcessor.init(context);
	}

	public HeaderObj getHeader(Context context, String preferenceName) {
		HeaderObj headerObj = new HeaderObj();
		headerObj.setAddress(context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
				.getString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), ""));
		headerObj.setRefreshDateTime(ZonedDateTime.now().toString());

		return headerObj;
	}

	public CurrentConditionsObj getCurrentConditions(Context context, WeatherSourceType weatherSourceType,
	                                                 MultipleJsonDownloader multipleJsonDownloader, String preferenceName) {
		final ZonedDateTime updatedTime = ZonedDateTime.of(multipleJsonDownloader.getLocalDateTime().toLocalDateTime(), ZoneId.systemDefault());

		ZoneId zoneId = null;
		CurrentConditionsObj currentConditionsObj = new CurrentConditionsObj();
		boolean successfulResponse = true;

		final ValueUnits windUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_wind),
						ValueUnits.mPerSec.name()));

		switch (weatherSourceType) {
			case KMA:
				MultipleJsonDownloader.ResponseResult kmaResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST);

				if (kmaResponseResult.isSuccessful()) {
					FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditions(
							(VilageFcstResponse) kmaResponseResult.getResponse().body());
					zoneId = KmaResponseProcessor.getZoneId();

					currentConditionsObj.setTemp(finalCurrentConditions.getTemperature());
					currentConditionsObj.setPrecipitation(finalCurrentConditions.getPrecipitation1Hour().equals("0") ? null : finalCurrentConditions.getPrecipitation1Hour());
					currentConditionsObj.setRealFeelTemp(null);
					currentConditionsObj.setWindSpeed(ValueUnits.convertWindSpeed(finalCurrentConditions.getWindSpeed(), windUnit).toString());

					SharedPreferences sharedPreferences =
							context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);

					final double latitude = Double.parseDouble(sharedPreferences.getString(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), "0.0"));
					final double longitude = Double.parseDouble(sharedPreferences.getString(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), "0.0"));

					SunriseSunsetCalculator sunriseSunsetCalculator =
							new SunriseSunsetCalculator(new com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude),
									TimeZone.getTimeZone(zoneId.getId()));

					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
					calendar.setTimeInMillis(updatedTime.toInstant().toEpochMilli());

					Calendar sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar);
					Calendar sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar);

					currentConditionsObj.setWeatherIcon(KmaResponseProcessor.getWeatherPtyIconImg(finalCurrentConditions.getPrecipitationType()
							, SunRiseSetUtil.isNight(calendar, sunRise, sunSet)));
					currentConditionsObj.setPrecipitationType(KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType()));
				} else {
					successfulResponse = false;
				}
				break;
			case ACCU_WEATHER:
				MultipleJsonDownloader.ResponseResult accuResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

				if (accuResponseResult.isSuccessful()) {
					CurrentConditionsResponse currentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
							(JsonElement) accuResponseResult.getResponse().body());
					CurrentConditionsResponse.Item item = currentConditionsResponse.getItems().get(0);
					zoneId = ZonedDateTime.parse(item.getLocalObservationDateTime()).getZone();

					currentConditionsObj.setTemp(item.getTemperature().getMetric().getValue());
					currentConditionsObj.setRealFeelTemp(item.getRealFeelTemperature().getMetric().getValue());
					currentConditionsObj.setPrecipitation(item.getPrecip1hr() == null ? null : item.getPrecip1hr().getMetric().getValue());
					currentConditionsObj.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon()));
					currentConditionsObj.setPrecipitationType(AccuWeatherResponseProcessor.getPty(item.getPrecipitationType()));
					currentConditionsObj.setWindSpeed(ValueUnits.convertVisibilityForAccu(item.getWind().getSpeed().getMetric().getValue(),
							windUnit));
				} else {
					successfulResponse = false;
				}
				break;
			case OPEN_WEATHER_MAP:
				MultipleJsonDownloader.ResponseResult owmResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

				if (owmResponseResult.isSuccessful()) {
					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(owmResponseResult.getResponse().body().toString());
					OneCallResponse.Current current = oneCallResponse.getCurrent();
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

					currentConditionsObj.setTemp(current.getTemp());
					currentConditionsObj.setRealFeelTemp(current.getFeelsLike());
					currentConditionsObj.setWindSpeed(ValueUnits.convertWindSpeed(current.getWind_speed(), windUnit).toString());

					if (current.getRain() != null) {
						currentConditionsObj.setPrecipitation(current.getRain().getPrecipitation1Hour());
						currentConditionsObj.setPrecipitationType(context.getString(R.string.rain));
					} else if (current.getSnow() != null) {
						currentConditionsObj.setPrecipitation(current.getSnow().getPrecipitation1Hour());
						currentConditionsObj.setPrecipitationType(context.getString(R.string.snow));
					}
					currentConditionsObj.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(current.getWeather().get(0).getId()
							, current.getWeather().get(0).getIcon().contains("n")));
				} else {
					successfulResponse = false;
				}
				break;
		}
		currentConditionsObj.setSuccessful(successfulResponse);
		currentConditionsObj.setZoneId(successfulResponse ? zoneId.getId() : null);

		if (successfulResponse) {
			/*
			String airQuality = null;

			MultipleJsonDownloader.ResponseResult aqiCnResponseResult =
					multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN).get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

			if (aqiCnResponseResult.isSuccessful()) {
				GeolocalizedFeedResponse geolocalizedFeedResponse =
						AqicnResponseProcessor.getAirQualityObjFromJson((Response<JsonElement>) aqiCnResponseResult.getResponse());
				airQuality = geolocalizedFeedResponse.getData().getIaqi().getPm10() == null ? null :
						geolocalizedFeedResponse.getData().getIaqi().getPm10().getValue();
			} else {
				airQuality = null;
			}
			currentConditionsObj.setAirQuality(airQuality);

			 */
		}
		return currentConditionsObj;
	}

	public WeatherJsonObj.HourlyForecasts getHourlyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                         MultipleJsonDownloader multipleJsonDownloader,
	                                                         String sharedPreferenceName) {
		boolean successfulResponse = true;
		List<HourlyForecastObj> hourlyForecastObjList = new ArrayList<>();
		ZoneId zoneId = null;

		switch (weatherSourceType) {
			case KMA:
				MultipleJsonDownloader.ResponseResult ultraSrtFcstResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
				MultipleJsonDownloader.ResponseResult vilageFcstResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST);

				if (ultraSrtFcstResponseResult.isSuccessful() && vilageFcstResponseResult.isSuccessful()) {
					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
							(VilageFcstResponse) ultraSrtFcstResponseResult.getResponse().body(),
							(VilageFcstResponse) vilageFcstResponseResult.getResponse().body());
					zoneId = KmaResponseProcessor.getZoneId();

					ZonedDateTime begin = ZonedDateTime.of(finalHourlyForecastList.get(0).getFcstDateTime().toLocalDateTime(), zoneId);
					ZonedDateTime end =
							ZonedDateTime.of(finalHourlyForecastList.get(finalHourlyForecastList.size() - 1).getFcstDateTime().toLocalDateTime(),
									zoneId);
					SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferenceName,
							Context.MODE_PRIVATE);

					final double latitude = Double.parseDouble(sharedPreferences.getString(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), "0.0"));
					final double longitude = Double.parseDouble(sharedPreferences.getString(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), "0.0"));

					Map<Integer, SunRiseSetUtil.SunRiseSetObj> sunRiseSetObjMap = SunRiseSetUtil.getDailySunRiseSetMap(begin, end, latitude
							, longitude);
					ZonedDateTime fcstDateTime = null;
					Calendar itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));

					for (FinalHourlyForecast hourlyForecast : finalHourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj();

						fcstDateTime = ZonedDateTime.of(hourlyForecast.getFcstDateTime().toLocalDateTime(), zoneId);
						hourlyForecastObj.setClock(fcstDateTime.toString());

						int dayOfYear = fcstDateTime.getDayOfYear();

						itemCalendar.setTimeInMillis(fcstDateTime.toInstant().toEpochMilli());
						Calendar sunRise = sunRiseSetObjMap.get(dayOfYear).getSunrise();
						Calendar sunSet = sunRiseSetObjMap.get(dayOfYear).getSunset();

						hourlyForecastObj.setWeatherIcon(KmaResponseProcessor.getWeatherSkyAndPtyIconImg(hourlyForecast.getPrecipitationType(),
								hourlyForecast.getSky(),
								SunRiseSetUtil.isNight(itemCalendar, sunRise, sunSet)));
						hourlyForecastObj.setTemp(hourlyForecast.getTemp1Hour());

						hourlyForecastObjList.add(hourlyForecastObj);
					}
				} else {
					successfulResponse = false;
				}
				break;
			case ACCU_WEATHER:
				MultipleJsonDownloader.ResponseResult accuResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_12_HOURLY);
				if (accuResponseResult.isSuccessful()) {
					TwelveHoursOfHourlyForecastsResponse hourlyForecastResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
							(JsonElement) accuResponseResult.getResponse().body());
					List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyForecastList = hourlyForecastResponse.getItems();
					zoneId = ZonedDateTime.parse(hourlyForecastList.get(0).getDateTime()).getZone();

					for (TwelveHoursOfHourlyForecastsResponse.Item hourlyForecast : hourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj();

						hourlyForecastObj.setClock(WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourlyForecast.getEpochDateTime()) * 1000L, zoneId).toString());
						hourlyForecastObj.setTemp(hourlyForecast.getTemperature().getValue());
						hourlyForecastObj.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(hourlyForecast.getWeatherIcon()));

						hourlyForecastObjList.add(hourlyForecastObj);
					}
				} else {
					successfulResponse = false;
				}
				break;

			case OPEN_WEATHER_MAP:
				MultipleJsonDownloader.ResponseResult owmResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);
				if (owmResponseResult.isSuccessful()) {
					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(owmResponseResult.getResponse().body().toString());
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

					List<OneCallResponse.Hourly> hourly = oneCallResponse.getHourly();

					for (OneCallResponse.Hourly item : hourly) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj();

						hourlyForecastObj.setClock(WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(item.getDt()) * 1000L,
								zoneId).toString());
						hourlyForecastObj.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(),
								item.getWeather().get(0).getIcon().contains("n")));
						hourlyForecastObj.setTemp(item.getTemp());

						hourlyForecastObjList.add(hourlyForecastObj);
					}
				} else {
					successfulResponse = false;
				}
				break;

		}
		WeatherJsonObj.HourlyForecasts hourlyForecasts = new WeatherJsonObj.HourlyForecasts();
		hourlyForecasts.setSuccessful(successfulResponse);
		if (successfulResponse) {
			hourlyForecasts.setHourlyForecastObjs(hourlyForecastObjList);
			hourlyForecasts.setZoneId(successfulResponse ? zoneId.getId() : null);
		}
		return hourlyForecasts;
	}

	public WeatherJsonObj.DailyForecasts getDailyForecasts(WeatherSourceType weatherSourceType,
	                                                       MultipleJsonDownloader multipleJsonDownloader) {
		boolean successfulResponse = true;
		List<DailyForecastObj> dailyForecastObjList = new ArrayList<>();
		ZoneId zoneId = null;

		switch (weatherSourceType) {
			case KMA:

				MultipleJsonDownloader.ResponseResult midLandFcstResponse =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_LAND_FCST);
				MultipleJsonDownloader.ResponseResult midTaFcstResponse =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.MID_TA_FCST);
				MultipleJsonDownloader.ResponseResult vilageFcstResponse =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST);
				MultipleJsonDownloader.ResponseResult ultraSrtFcstResponse =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);

				if (midLandFcstResponse.isSuccessful() && midTaFcstResponse.isSuccessful() &&
						vilageFcstResponse.isSuccessful() && ultraSrtFcstResponse.isSuccessful()) {
					List<FinalHourlyForecast> finalHourlyForecasts =
							KmaResponseProcessor.getFinalHourlyForecastList((VilageFcstResponse) ultraSrtFcstResponse.getResponse().body()
									, (VilageFcstResponse) vilageFcstResponse.getResponse().body());
					List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(
							(MidLandFcstResponse)
									midLandFcstResponse.getResponse().body(),
							(MidTaResponse)
									midTaFcstResponse.getResponse().body(),
							Long.parseLong(multipleJsonDownloader.get("tmFc")));
					KmaResponseProcessor.getDailyForecastList(finalDailyForecastList, finalHourlyForecasts);

					zoneId = KmaResponseProcessor.getZoneId();

					int index = 0;

					for (FinalDailyForecast finalDailyForecast : finalDailyForecastList) {
						DailyForecastObj dailyForecastObj;

						if (finalDailyForecast.isSingle()) {
							dailyForecastObj = new DailyForecastObj(true);
							dailyForecastObj.setLeftWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getSky(), false));
							dailyForecastObj.setLeftPop(finalDailyForecast.getProbabilityOfPrecipitation());
						} else {
							dailyForecastObj = new DailyForecastObj(false);
							dailyForecastObj.setLeftWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getAmSky(), false));
							dailyForecastObj.setRightWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getPmSky(), false));
							dailyForecastObj.setLeftPop(finalDailyForecast.getAmProbabilityOfPrecipitation());
							dailyForecastObj.setRightPop(finalDailyForecast.getPmProbabilityOfPrecipitation());
						}

						dailyForecastObj.setDate(finalDailyForecast.getDate().toString());
						dailyForecastObj.setMinTemp(finalDailyForecast.getMinTemp());
						dailyForecastObj.setMaxTemp(finalDailyForecast.getMaxTemp());

						dailyForecastObjList.add(dailyForecastObj);

						index++;
					}
				} else {
					successfulResponse = false;
				}
				break;
			case ACCU_WEATHER:
				if (AccuWeatherResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY))) {
					FiveDaysOfDailyForecastsResponse dailyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
							multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponse().body().toString());

					List<FiveDaysOfDailyForecastsResponse.DailyForecasts> dailyForecasts = dailyForecastsResponse.getDailyForecasts();
					zoneId = ZonedDateTime.parse(dailyForecasts.get(0).getDateTime()).getZone();

					for (FiveDaysOfDailyForecastsResponse.DailyForecasts item : dailyForecasts) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(false);

						dailyForecastObj.setDate(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getEpochDate()) * 1000L, zoneId).toString());

						dailyForecastObj.setMinTemp(item.getTemperature().getMinimum().getValue());
						dailyForecastObj.setMaxTemp(item.getTemperature().getMaximum().getValue());
						dailyForecastObj.setLeftWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getDay().getIcon()));
						dailyForecastObj.setRightWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getNight().getIcon()));
						dailyForecastObj.setLeftPop(item.getDay().getPrecipitationProbability());
						dailyForecastObj.setLeftPop(item.getNight().getPrecipitationProbability());

						dailyForecastObjList.add(dailyForecastObj);
					}
				} else {
					successfulResponse = false;
				}
				break;
			case OPEN_WEATHER_MAP:
				if (OpenWeatherMapResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL))) {
					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().body().toString());
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

					List<OneCallResponse.Daily> daily = oneCallResponse.getDaily();

					for (OneCallResponse.Daily item : daily) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj(true);
						dailyForecastObj.setDate(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getDt()) * 1000L,
								zoneId).toString());
						dailyForecastObj.setMinTemp(item.getTemp().getMin());
						dailyForecastObj.setMaxTemp(item.getTemp().getMax());
						dailyForecastObj.setLeftWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(), false));
						dailyForecastObj.setLeftPop(item.getPop());

						dailyForecastObjList.add(dailyForecastObj);
					}
				} else {
					successfulResponse = false;
				}
				break;
		}

		WeatherJsonObj.DailyForecasts dailyForecasts = new WeatherJsonObj.DailyForecasts();
		dailyForecasts.setSuccessful(successfulResponse);
		if (successfulResponse) {
			dailyForecasts.setDailyForecastObjs(dailyForecastObjList);
			dailyForecasts.setZoneId(successfulResponse ? zoneId.getId() : null);
		}
		return dailyForecasts;
	}
}
