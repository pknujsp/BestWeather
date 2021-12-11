package com.lifedawn.bestweather.forremoteviews;

import android.content.Context;

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
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindDirectionConverter;
import com.lifedawn.bestweather.widget.model.AirQualityObj;
import com.lifedawn.bestweather.widget.model.CurrentConditionsObj;
import com.lifedawn.bestweather.widget.model.DailyForecastObj;
import com.lifedawn.bestweather.widget.model.HourlyForecastObj;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Response;

public class SimpleWeatherDataProcessor {

	public static AirQualityObj getAirQualityObj(Context context, MultipleJsonDownloader multipleJsonDownloader) {
		MultipleJsonDownloader.ResponseResult responseResult = multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN)
				.get(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

		AirQualityObj airQualityObj = new AirQualityObj();
		if (responseResult.isSuccessful()) {
			GeolocalizedFeedResponse airQualityResponse =
					AqicnResponseProcessor.getAirQualityObjFromJson((Response<JsonElement>) responseResult.getResponse());

			airQualityObj.setSuccessful(true);
			String airQuality = AqicnResponseProcessor.getAirQuality(context, airQualityResponse);
			airQualityObj.setAqi(airQuality);
		}

		return airQualityObj;
	}

	public static CurrentConditionsObj getCurrentConditionsObj(Context context, WeatherSourceType weatherSourceType,
	                                                           MultipleJsonDownloader multipleJsonDownloader) {
		ZoneId zoneId = null;
		CurrentConditionsObj currentConditionsObj = new CurrentConditionsObj();
		boolean successfulResponse = false;

		final ValueUnits tempUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_temp),
						ValueUnits.celsius.name()));
		final ValueUnits windUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_wind),
						ValueUnits.mPerSec.name()));
		final ValueUnits visibilityUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_visibility),
						ValueUnits.km.name()));
		final String degree = "°";

		switch (weatherSourceType) {
			case KMA:
				MultipleJsonDownloader.ResponseResult kmaResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_NCST);

				if (kmaResponseResult.isSuccessful()) {
					successfulResponse = true;
					FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditions(
							(VilageFcstResponse) kmaResponseResult.getResponse().body());
					zoneId = KmaResponseProcessor.getZoneId();

					currentConditionsObj.setTemp(ValueUnits.convertTemperature(finalCurrentConditions.getTemperature(), tempUnit) + degree);
					currentConditionsObj.setPrecipitationType(KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType()));
					currentConditionsObj.setRealFeelTemp(null);
					currentConditionsObj.setWindSpeed(ValueUnits.convertWindSpeed(finalCurrentConditions.getWindSpeed(), windUnit) +
							ValueUnits.convertToStr(context, windUnit));
					currentConditionsObj.setPrecipitationVolume(finalCurrentConditions.getPrecipitation1Hour());
					currentConditionsObj.setHumidity(finalCurrentConditions.getHumidity());
					currentConditionsObj.setWindDirection(WindDirectionConverter.windDirection(context, finalCurrentConditions.getWindDirection()));

					UltraSrtNcstParameter parameter = (UltraSrtNcstParameter) kmaResponseResult.getRequestParameter();

					SunriseSunsetCalculator sunriseSunsetCalculator =
							new SunriseSunsetCalculator(new com.luckycatlabs.sunrisesunset.dto.Location(parameter.getLatitude(), parameter.getLongitude()),
									TimeZone.getTimeZone(zoneId.getId()));

					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
					ZonedDateTime now = ZonedDateTime.now(zoneId);
					calendar.setTimeInMillis(now.toInstant().toEpochMilli());

					Calendar sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar);
					Calendar sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar);

					currentConditionsObj.setWeatherIcon(KmaResponseProcessor.getWeatherPtyIconImg(finalCurrentConditions.getPrecipitationType()
							, SunRiseSetUtil.isNight(calendar, sunRise, sunSet)));
				}
				break;

			case ACCU_WEATHER:
				MultipleJsonDownloader.ResponseResult accuResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

				if (accuResponseResult.isSuccessful()) {
					successfulResponse = true;
					CurrentConditionsResponse currentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
							(JsonElement) accuResponseResult.getResponse().body());
					CurrentConditionsResponse.Item item = currentConditionsResponse.getItems().get(0);
					zoneId = ZonedDateTime.parse(item.getLocalObservationDateTime()).getZone();

					currentConditionsObj.setTemp(ValueUnits.convertTemperature(item.getTemperature().getMetric().getValue(), tempUnit) + degree);
					currentConditionsObj.setRealFeelTemp(ValueUnits.convertTemperature(item.getRealFeelTemperature().getMetric().getValue(), tempUnit) + degree);
					currentConditionsObj.setPrecipitationVolume(item.getPrecip1hr() == null ? null : item.getPrecip1hr().getMetric().getValue());
					currentConditionsObj.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon()));
					currentConditionsObj.setPrecipitationType(AccuWeatherResponseProcessor.getPty(item.getPrecipitationType()));
					currentConditionsObj.setWindSpeed(ValueUnits.convertVisibilityForAccu(item.getWind().getSpeed().getMetric().getValue(),
							windUnit) + ValueUnits.convertToStr(context, windUnit));
					currentConditionsObj.setWindDirection(WindDirectionConverter.windDirection(context,
							item.getWind().getDirection().getDegrees()));
					currentConditionsObj.setCloudiness(item.getCloudCover());
					currentConditionsObj.setUvIndex(item.getuVIndex());
					currentConditionsObj.setVisibility(ValueUnits.convertVisibilityForAccu(item.getVisibility().getMetric().getValue(), visibilityUnit)
							+ ValueUnits.convertToStr(context, visibilityUnit));
				}
				break;

			case OPEN_WEATHER_MAP:
				MultipleJsonDownloader.ResponseResult owmResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);

				if (owmResponseResult.isSuccessful()) {
					successfulResponse = true;
					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(owmResponseResult.getResponse().body().toString());
					OneCallResponse.Current current = oneCallResponse.getCurrent();
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

					currentConditionsObj.setTemp(ValueUnits.convertTemperature(current.getTemp(), tempUnit) + degree);
					currentConditionsObj.setRealFeelTemp(ValueUnits.convertTemperature(current.getFeelsLike(), tempUnit) + degree);
					currentConditionsObj.setWindSpeed(ValueUnits.convertWindSpeed(current.getWind_speed(), windUnit) +
							ValueUnits.convertToStr(context, windUnit));
					currentConditionsObj.setUvIndex(current.getUvi());
					currentConditionsObj.setCloudiness(current.getClouds());
					currentConditionsObj.setVisibility(ValueUnits.convertVisibility(current.getVisibility(), visibilityUnit)
							+ ValueUnits.convertToStr(context, visibilityUnit));
					currentConditionsObj.setHumidity(current.getHumidity());

					if (current.getRain() != null) {
						currentConditionsObj.setPrecipitationVolume(current.getRain().getPrecipitation1Hour());
						currentConditionsObj.setPrecipitationType(context.getString(R.string.rain));
					} else if (current.getSnow() != null) {
						currentConditionsObj.setPrecipitationVolume(current.getSnow().getPrecipitation1Hour());
						currentConditionsObj.setPrecipitationType(context.getString(R.string.snow));
					} else {
						currentConditionsObj.setPrecipitationType(context.getString(R.string.not_precipitation));
					}

					currentConditionsObj.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(current.getWeather().get(0).getId()
							, current.getWeather().get(0).getIcon().contains("n")));
				}
				break;

		}
		currentConditionsObj.setSuccessful(successfulResponse);
		currentConditionsObj.setTimeZoneId(successfulResponse ? zoneId.getId() : null);
		return currentConditionsObj;
	}

	public static List<HourlyForecastObj> getHourlyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                         MultipleJsonDownloader multipleJsonDownloader) {
		boolean successfulResponse = false;
		List<HourlyForecastObj> hourlyForecastObjList = new ArrayList<>();
		ZoneId zoneId = null;

		final ValueUnits tempUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_temp),
						ValueUnits.celsius.name()));
		final ValueUnits windUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_wind),
						ValueUnits.mPerSec.name()));
		final ValueUnits visibilityUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_visibility),
						ValueUnits.km.name()));
		final DateTimeFormatter hoursFormatterIfHours0 = DateTimeFormatter.ofPattern("E 0");
		final String degree = "°";
		final String percent = "%";

		switch (weatherSourceType) {
			case KMA:
				MultipleJsonDownloader.ResponseResult ultraSrtFcstResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
				MultipleJsonDownloader.ResponseResult vilageFcstResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.KMA).get(RetrofitClient.ServiceType.VILAGE_FCST);

				if (ultraSrtFcstResponseResult.isSuccessful() && vilageFcstResponseResult.isSuccessful()) {
					successfulResponse = true;
					List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
							(VilageFcstResponse) ultraSrtFcstResponseResult.getResponse().body(),
							(VilageFcstResponse) vilageFcstResponseResult.getResponse().body());
					zoneId = KmaResponseProcessor.getZoneId();

					ZonedDateTime begin = ZonedDateTime.of(finalHourlyForecastList.get(0).getFcstDateTime().toLocalDateTime(), zoneId);
					ZonedDateTime end =
							ZonedDateTime.of(finalHourlyForecastList.get(finalHourlyForecastList.size() - 1).getFcstDateTime().toLocalDateTime(),
									zoneId);

					VilageFcstParameter vilageFcstParameter = (VilageFcstParameter) vilageFcstResponseResult.getRequestParameter();

					Map<Integer, SunRiseSetUtil.SunRiseSetObj> sunRiseSetObjMap = SunRiseSetUtil.getDailySunRiseSetMap(begin, end,
							vilageFcstParameter.getLatitude(), vilageFcstParameter.getLongitude());
					ZonedDateTime fcstDateTime = ZonedDateTime.of(finalHourlyForecastList.get(0).getFcstDateTime().toLocalDateTime(),
							zoneId);
					Calendar itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
					int dayOfYear = 0;

					for (FinalHourlyForecast hourlyForecast : finalHourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj();

						hourlyForecastObj.setHours(fcstDateTime.getHour() == 0 ? fcstDateTime.format(hoursFormatterIfHours0)
								: String.valueOf(fcstDateTime.getHour()));

						dayOfYear = fcstDateTime.getDayOfYear();

						itemCalendar.setTimeInMillis(fcstDateTime.toInstant().toEpochMilli());
						hourlyForecastObj.setWeatherIcon(KmaResponseProcessor.getWeatherSkyAndPtyIconImg(hourlyForecast.getPrecipitationType(),
								hourlyForecast.getSky(),
								SunRiseSetUtil.isNight(itemCalendar, sunRiseSetObjMap.get(dayOfYear).getSunrise(), sunRiseSetObjMap.get(dayOfYear).getSunset())));
						hourlyForecastObj.setTemp(ValueUnits.convertTemperature(hourlyForecast.getTemp1Hour(), tempUnit) + degree);
						hourlyForecastObj.setPop(hourlyForecast.getProbabilityOfPrecipitation());

						hourlyForecastObjList.add(hourlyForecastObj);
						fcstDateTime = fcstDateTime.plusHours(1);
					}
				}
				break;

			case ACCU_WEATHER:
				MultipleJsonDownloader.ResponseResult accuResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_12_HOURLY);
				if (accuResponseResult.isSuccessful()) {
					successfulResponse = true;

					TwelveHoursOfHourlyForecastsResponse hourlyForecastResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
							(JsonElement) accuResponseResult.getResponse().body());
					List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyForecastList = hourlyForecastResponse.getItems();
					ZonedDateTime zonedDateTime = ZonedDateTime.parse(hourlyForecastList.get(0).getDateTime());
					zoneId = ZonedDateTime.parse(hourlyForecastList.get(0).getDateTime()).getZone();

					for (TwelveHoursOfHourlyForecastsResponse.Item hourlyForecast : hourlyForecastList) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj();

						hourlyForecastObj.setHours(zonedDateTime.getHour() == 0 ? zonedDateTime.format(hoursFormatterIfHours0)
								: String.valueOf(zonedDateTime.getHour()));
						hourlyForecastObj.setTemp(ValueUnits.convertTemperature(hourlyForecast.getTemperature().getValue(), tempUnit) + degree);
						hourlyForecastObj.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(hourlyForecast.getWeatherIcon()));
						hourlyForecastObj.setPop(hourlyForecast.getPrecipitationProbability());

						hourlyForecastObjList.add(hourlyForecastObj);
						zonedDateTime = zonedDateTime.plusHours(1);
					}
				}
				break;

			case OPEN_WEATHER_MAP:
				MultipleJsonDownloader.ResponseResult owmResponseResult =
						multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL);
				if (owmResponseResult.isSuccessful()) {
					successfulResponse = true;

					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(owmResponseResult.getResponse().body().toString());
					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

					List<OneCallResponse.Hourly> hourly = oneCallResponse.getHourly();
					ZonedDateTime zonedDateTime =
							WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourly.get(0).getDt()) * 1000L,
									zoneId);

					for (OneCallResponse.Hourly item : hourly) {
						HourlyForecastObj hourlyForecastObj = new HourlyForecastObj();

						hourlyForecastObj.setHours(zonedDateTime.getHour() == 0 ? zonedDateTime.format(hoursFormatterIfHours0)
								: String.valueOf(zonedDateTime.getHour()));
						hourlyForecastObj.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(),
								item.getWeather().get(0).getIcon().contains("n")));
						hourlyForecastObj.setTemp(ValueUnits.convertTemperature(item.getTemp(), tempUnit) + degree);

						hourlyForecastObjList.add(hourlyForecastObj);
						zonedDateTime = zonedDateTime.plusHours(1);

					}
				}
				break;

		}

		return hourlyForecastObjList;
	}

	public static List<DailyForecastObj> getDailyForecasts(Context context, WeatherSourceType weatherSourceType,
	                                                       MultipleJsonDownloader multipleJsonDownloader) {
		boolean successfulResponse = false;
		List<DailyForecastObj> dailyForecastObjList = new ArrayList<>();
		ZoneId zoneId = null;
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E");
		final ValueUnits tempUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_temp),
						ValueUnits.celsius.name()));
		final ValueUnits windUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_wind),
						ValueUnits.mPerSec.name()));

		final String degree = "°";
		final String percent = "%";

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
					successfulResponse = true;

					List<FinalHourlyForecast> finalHourlyForecasts =
							KmaResponseProcessor.getFinalHourlyForecastList((VilageFcstResponse) ultraSrtFcstResponse.getResponse().body()
									, (VilageFcstResponse) vilageFcstResponse.getResponse().body());
					List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(
							(MidLandFcstResponse) midLandFcstResponse.getResponse().body(),
							(MidTaResponse) midTaFcstResponse.getResponse().body(),
							Long.parseLong(multipleJsonDownloader.get("tmFc")));
					KmaResponseProcessor.getDailyForecastList(finalDailyForecastList, finalHourlyForecasts);

					zoneId = KmaResponseProcessor.getZoneId();
					ZonedDateTime zonedDateTime = ZonedDateTime.of(finalDailyForecastList.get(0).getDate().toLocalDateTime(),
							finalDailyForecastList.get(0).getDate().getZone());

					for (FinalDailyForecast finalDailyForecast : finalDailyForecastList) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj();
						if (finalDailyForecast.isSingle()) {
							dailyForecastObj.setSingle(true);

							dailyForecastObj.setSingleWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getSky(),
									false));
							dailyForecastObj.setSinglePop(finalDailyForecast.getProbabilityOfPrecipitation());
						} else {
							dailyForecastObj.setSingle(false);

							dailyForecastObj.setAmWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getAmSky(), false));
							dailyForecastObj.setPmWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getPmSky(), false));
							dailyForecastObj.setAmPop(finalDailyForecast.getAmProbabilityOfPrecipitation());
							dailyForecastObj.setPmPop(finalDailyForecast.getPmProbabilityOfPrecipitation());
						}

						dailyForecastObj.setDate(zonedDateTime.format(dateFormatter));
						dailyForecastObj.setMinTemp(ValueUnits.convertTemperature(finalDailyForecast.getMinTemp(), tempUnit) + degree);
						dailyForecastObj.setMaxTemp(ValueUnits.convertTemperature(finalDailyForecast.getMaxTemp(), tempUnit) + degree);

						dailyForecastObjList.add(dailyForecastObj);
						zonedDateTime = zonedDateTime.plusDays(1);
					}
				}
				break;

			case ACCU_WEATHER:
				if (AccuWeatherResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY))) {
					successfulResponse = true;

					FiveDaysOfDailyForecastsResponse dailyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
							multipleJsonDownloader.getResponseMap().get(WeatherSourceType.ACCU_WEATHER).get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponse().body().toString());

					List<FiveDaysOfDailyForecastsResponse.DailyForecasts> dailyForecasts = dailyForecastsResponse.getDailyForecasts();
					ZonedDateTime zonedDateTime = ZonedDateTime.parse(dailyForecasts.get(0).getDateTime());
					zoneId = zonedDateTime.getZone();

					for (FiveDaysOfDailyForecastsResponse.DailyForecasts item : dailyForecasts) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj();

						dailyForecastObj.setSingle(false);

						dailyForecastObj.setAmWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getDay().getIcon()));
						dailyForecastObj.setPmWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(item.getNight().getIcon()));
						dailyForecastObj.setAmPop(item.getDay().getPrecipitationProbability());
						dailyForecastObj.setPmPop(item.getNight().getPrecipitationProbability());

						dailyForecastObj.setDate(zonedDateTime.format(dateFormatter));
						dailyForecastObj.setMinTemp(ValueUnits.convertTemperature(item.getTemperature().getMinimum().getValue(), tempUnit) + degree);
						dailyForecastObj.setMaxTemp(ValueUnits.convertTemperature(item.getTemperature().getMaximum().getValue(), tempUnit) + degree);

						dailyForecastObjList.add(dailyForecastObj);
						zonedDateTime = zonedDateTime.plusDays(1);
					}
				}
				break;

			case OPEN_WEATHER_MAP:
				if (OpenWeatherMapResponseProcessor.successfulResponse(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL))) {
					successfulResponse = true;
					OneCallResponse oneCallResponse =
							OpenWeatherMapResponseProcessor.getOneCallObjFromJson(multipleJsonDownloader.getResponseMap().get(WeatherSourceType.OPEN_WEATHER_MAP).get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().body().toString());

					List<OneCallResponse.Daily> daily = oneCallResponse.getDaily();

					zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);
					ZonedDateTime zonedDateTime =
							WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(daily.get(0).getDt()) * 1000L,
									zoneId);

					for (OneCallResponse.Daily item : daily) {
						DailyForecastObj dailyForecastObj = new DailyForecastObj();
						dailyForecastObj.setSingle(true);

						dailyForecastObj.setSingleWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(), false));
						dailyForecastObj.setSinglePop(item.getPop());

						dailyForecastObj.setDate(zonedDateTime.format(dateFormatter));
						dailyForecastObj.setMinTemp(ValueUnits.convertTemperature(item.getTemp().getMin(), tempUnit) + degree);
						dailyForecastObj.setMaxTemp(ValueUnits.convertTemperature(item.getTemp().getMax(), tempUnit) + degree);

						dailyForecastObjList.add(dailyForecastObj);
						zonedDateTime = zonedDateTime.plusDays(1);
					}
				}

				break;
		}

		return dailyForecastObjList;
	}


}
