package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.res.TypedArray;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.currentweather.CurrentWeatherResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.dailyforecast.DailyForecast;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.hourlyforecast.HourlyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindDirectionConverter;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Response;

public class OpenWeatherMapResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_ICON_ID_MAP = new HashMap<>();
	private static final Map<String, String> FLICKR_MAP = new HashMap<>();

	private OpenWeatherMapResponseProcessor() {
	}

	public static void init(Context context) {
		if (WEATHER_ICON_DESCRIPTION_MAP.size() == 0 || WEATHER_ICON_ID_MAP.size() == 0 || FLICKR_MAP.size() == 0) {

			String[] codes = context.getResources().getStringArray(R.array.OpenWeatherMapWeatherIconCodes);
			String[] descriptions = context.getResources().getStringArray(R.array.OpenWeatherMapWeatherIconDescriptionsForCode);
			TypedArray iconIds = context.getResources().obtainTypedArray(R.array.OpenWeatherMapWeatherIconForCode);

			WEATHER_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < codes.length; i++) {
				WEATHER_ICON_DESCRIPTION_MAP.put(codes[i], descriptions[i]);
				WEATHER_ICON_ID_MAP.put(codes[i], iconIds.getResourceId(i, R.drawable.temp_icon));
			}

			String[] flickrGalleryNames = context.getResources().getStringArray(R.array.OpenWeatherMapFlickrGalleryNames);

			FLICKR_MAP.clear();
			for (int i = 0; i < codes.length; i++) {
				FLICKR_MAP.put(codes[i], flickrGalleryNames[i]);
			}
		}
	}

	public static int getWeatherIconImg(String code, boolean night) {
		if (night) {
			switch (code) {
				case "800":
					return R.drawable.night_clear;
				case "801":
					return R.drawable.night_partly_cloudy;
				case "802":
					return R.drawable.night_scattered_clouds;
				case "803":
					return R.drawable.night_mostly_cloudy;
				default:
					return WEATHER_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_ICON_ID_MAP.get(code);
	}

	public static String getWeatherIconDescription(String code) {
		return WEATHER_ICON_DESCRIPTION_MAP.get(code);
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

	public static CurrentWeatherResponse getCurrentWeatherObjFromJson(String response) {
		return new Gson().fromJson(response, CurrentWeatherResponse.class);
	}

	public static HourlyForecastResponse getHourlyForecastObjFromJson(String response) {
		return new Gson().fromJson(response, HourlyForecastResponse.class);
	}


	public static List<HourlyForecastDto> makeHourlyForecastDtoList(Context context,
	                                                                OneCallResponse oneCallResponse,
	                                                                ValueUnits windUnit, ValueUnits tempUnit, ValueUnits visibilityUnit) {
		final String tempDegree = "°";
		final String percent = "%";
		final String mm = "mm";

		final String windUnitStr = ValueUnits.convertToStr(context, windUnit);
		final String zeroSnowVolume = "0.0mm";
		final String zeroRainVolume = "0.0mm";
		final String zeroPrecipitationVolume = "0.0mm";
		final String pressureUnit = "hpa";
		final String visibilityUnitStr = ValueUnits.convertToStr(context, visibilityUnit);

		String snowVolume;
		String rainVolume;
		boolean hasRain;
		boolean hasSnow;

		ZoneId zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);
		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		for (OneCallResponse.Hourly hourly : oneCallResponse.getHourly()) {
			HourlyForecastDto hourlyForecastDto = new HourlyForecastDto();

			if (hourly.getRain() == null) {
				hasRain = false;
				rainVolume = zeroRainVolume;
			} else {
				hasRain = true;
				rainVolume = hourly.getRain().getPrecipitation1Hour() + mm;
			}

			if (hourly.getSnow() == null) {
				hasSnow = false;
				snowVolume = zeroSnowVolume;
			} else {
				hasSnow = true;
				snowVolume = hourly.getSnow().getPrecipitation1Hour() + mm;
			}

			hourlyForecastDto.setHours(WeatherResponseProcessor.convertDateTimeOfHourlyForecast
					(Long.parseLong(hourly.getDt()) * 1000L, zoneId))
					.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(hourly.getWeather().get(0).getId(),
							hourly.getWeather().get(0).getIcon().contains("n")))
					.setTemp(ValueUnits.convertTemperature(hourly.getTemp(), tempUnit) + tempDegree)
					.setPop((int) (Double.parseDouble(hourly.getPop()) * 100.0) + percent)
					.setPrecipitationVolume(zeroPrecipitationVolume)
					.setHasRain(hasRain)
					.setHasSnow(hasSnow)
					.setRainVolume(rainVolume)
					.setSnowVolume(snowVolume)
					.setWeatherDescription(OpenWeatherMapResponseProcessor.getWeatherIconDescription(hourly.getWeather().get(0).getId()))
					.setFeelsLikeTemp(ValueUnits.convertTemperature(hourly.getFeelsLike(), tempUnit) + tempDegree)
					.setWindDirection(WindDirectionConverter.windDirection(context, hourly.getWind_deg()))
					.setWindDirectionVal(Integer.parseInt(hourly.getWind_deg()))
					.setWindSpeed(ValueUnits.convertWindSpeed(hourly.getWind_speed(), windUnit) + windUnitStr)
					.setWindStrength(WeatherResponseProcessor.getSimpleWindSpeedDescription(hourly.getWind_speed()))
					.setWindGust(ValueUnits.convertWindSpeed(hourly.getWindGust(), windUnit) + windUnitStr)
					.setPressure(hourly.getPressure() + pressureUnit)
					.setHumidity(hourly.getHumidity() + percent)
					.setCloudiness(hourly.getClouds() + percent)
					.setVisibility(ValueUnits.convertVisibility(hourly.getVisibility(), visibilityUnit) + visibilityUnitStr)
					.setUvIndex(hourly.getUvi());

			hourlyForecastDtoList.add(hourlyForecastDto);
		}
		return hourlyForecastDtoList;
	}


	public static List<DailyForecastDto> makeDailyForecastDtoList(Context context,
	                                                              OneCallResponse oneCallResponse,
	                                                              ValueUnits windUnit, ValueUnits tempUnit) {
		final String tempDegree = "°";
		final String mm = "mm";
		final String percent = "%";
		final String wind = ValueUnits.convertToStr(context, windUnit);
		final String zeroSnowVolume = "0.0mm";
		final String zeroRainVolume = "0.0mm";
		final String hpa = "hpa";

		//순서 : 날짜, 날씨상태, 최저/최고 기온, 강수확률, 하루 강우량(nullable), 하루 강설량(nullable)
		//풍향, 풍속, 바람세기, 돌풍(nullable), 기압, 습도, 이슬점, 운량, 자외선최고치

		//아침/낮/저녁/밤 기온(체감) 제외
		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		ZoneId zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

		String rainVolume;
		String snowVolume;
		boolean hasRain;
		boolean hasSnow;

		for (OneCallResponse.Daily daily : oneCallResponse.getDaily()) {
			DailyForecastDto dailyForecastDto = new DailyForecastDto();
			dailyForecastDto.setSingle(true).setSingleValues(new DailyForecastDto.Values())
					.setDate(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(daily.getDt()) * 1000L, zoneId))
					.setMinTemp(ValueUnits.convertTemperature(daily.getTemp().getMin(), tempUnit) + tempDegree)
					.setMaxTemp(ValueUnits.convertTemperature(daily.getTemp().getMax(), tempUnit) + tempDegree);

			DailyForecastDto.Values single = dailyForecastDto.getSingleValues();

			if (daily.getRain() == null) {
				hasRain = false;
				rainVolume = zeroRainVolume;
			} else {
				hasRain = true;
				rainVolume = daily.getRain() + mm;
			}

			if (daily.getSnow() == null) {
				hasSnow = false;
				snowVolume = zeroSnowVolume;
			} else {
				hasSnow = true;
				snowVolume = daily.getSnow() + mm;
			}

			single.setPop((int) (Double.parseDouble(daily.getPop()) * 100.0) + percent)
					.setHasRainVolume(hasRain)
					.setRainVolume(rainVolume)
					.setHasSnowVolume(hasSnow)
					.setSnowVolume(snowVolume)
					.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(daily.getWeather().get(0).getId(), false))
					.setWeatherDescription(OpenWeatherMapResponseProcessor.getWeatherIconDescription(daily.getWeather().get(0).getId()))
					.setWindDirection(WindDirectionConverter.windDirection(context, daily.getWindDeg()))
					.setWindDirectionVal(Integer.parseInt(daily.getWindDeg()))
					.setWindSpeed(ValueUnits.convertWindSpeed(daily.getWindSpeed(), windUnit) + wind)
					.setWindStrength(WeatherResponseProcessor.getSimpleWindSpeedDescription(daily.getWindSpeed()))
					.setWindGust(ValueUnits.convertWindSpeed(daily.getWindGust(), windUnit) + wind)
					.setPressure(daily.getPressure() + hpa)
					.setHumidity(daily.getHumidity() + percent)
					.setDewPointTemp(ValueUnits.convertTemperature(daily.getDew_point(), tempUnit) + tempDegree)
					.setCloudiness(daily.getClouds() + percent)
					.setUvIndex(daily.getUvi());

			dailyForecastDtoList.add(dailyForecastDto);
		}
		return dailyForecastDtoList;
	}


	public static CurrentConditionsDto makeCurrentConditionsDto(Context context,
	                                                            OneCallResponse oneCallResponse,
	                                                            ValueUnits windUnit, ValueUnits tempUnit, ValueUnits visibilityUnit) {

		final String tempUnitStr = ValueUnits.convertToStr(context, tempUnit);
		final String percent = "%";
		final ZoneId zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

		CurrentConditionsDto currentConditionsDto = new CurrentConditionsDto();
		OneCallResponse.Current item = oneCallResponse.getCurrent();

		currentConditionsDto.setCurrentTime(WeatherResponseProcessor.convertDateTimeOfCurrentConditions(Long.parseLong(item.getDt()) * 1000L
				, zoneId));
		currentConditionsDto.setWeatherDescription(OpenWeatherMapResponseProcessor.getWeatherIconDescription(item.getWeather().get(0).getId()));
		currentConditionsDto.setWeatherIcon(OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(),
				item.getWeather().get(0).getIcon().contains("n")));
		currentConditionsDto.setTemp(ValueUnits.convertTemperature(item.getTemp(), tempUnit) + tempUnitStr);
		currentConditionsDto.setFeelsLikeTemp(ValueUnits.convertTemperature(item.getFeelsLike(), tempUnit) + tempUnitStr);
		currentConditionsDto.setHumidity(item.getHumidity() + percent);
		currentConditionsDto.setDewPoint(ValueUnits.convertTemperature(item.getDewPoint(), tempUnit) + tempUnitStr);
		currentConditionsDto.setWindDirectionDegree(Integer.parseInt(item.getWind_deg()));
		currentConditionsDto.setWindDirection(WindDirectionConverter.windDirection(context, item.getWind_deg()));
		currentConditionsDto.setWindSpeed(ValueUnits.convertWindSpeed(item.getWind_speed(), windUnit) + ValueUnits.convertToStr(context, windUnit));
		if (item.getWindGust() != null) {
			currentConditionsDto.setWindGust(ValueUnits.convertWindSpeed(item.getWindGust(), windUnit) + ValueUnits.convertToStr(context,
					windUnit));
		}
		currentConditionsDto.setSimpleWindStrength(WeatherResponseProcessor.getSimpleWindSpeedDescription(item.getWind_speed()));
		currentConditionsDto.setWindStrength(WeatherResponseProcessor.getWindSpeedDescription(item.getWind_speed()));
		currentConditionsDto.setPressure(item.getPressure() + "hpa");
		currentConditionsDto.setUvIndex(item.getUvi());
		currentConditionsDto.setVisibility(ValueUnits.convertVisibility(item.getVisibility(),
				visibilityUnit) + ValueUnits.convertToStr(context, visibilityUnit));
		currentConditionsDto.setCloudiness(item.getClouds() + percent);

		double precipitationVolume = 0.0;

		if (item.getRain() != null) {
			precipitationVolume += Double.parseDouble(item.getRain().getPrecipitation1Hour());
			currentConditionsDto.setRainVolume(item.getRain().getPrecipitation1Hour() + "mm");
		}
		if (item.getSnow() != null) {
			precipitationVolume += Double.parseDouble(item.getSnow().getPrecipitation1Hour());
			currentConditionsDto.setSnowVolume(item.getSnow().getPrecipitation1Hour() + "mm");
		}

		if (precipitationVolume > 0.0) {
			currentConditionsDto.setPrecipitationVolume(String.format(Locale.getDefault(), "%.2f mm", precipitationVolume));
		}

		return currentConditionsDto;
	}

	public static DailyForecast getDailyForecastObjFromJson(String response) {
		return new Gson().fromJson(response, DailyForecast.class);
	}

	public static OneCallResponse getOneCallObjFromJson(String response) {
		return new Gson().fromJson(response, OneCallResponse.class);
	}

	public static String getFlickrGalleryName(String code) {
		return FLICKR_MAP.get(code);
	}

	public static ZoneId getZoneId(OneCallResponse oneCallResponse) {
		return ZoneId.of(oneCallResponse.getTimezone());
	}
}