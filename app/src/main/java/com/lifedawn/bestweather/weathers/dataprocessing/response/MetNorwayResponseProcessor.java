package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.res.TypedArray;

import com.google.gson.Gson;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.LocationForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries.Data;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries.Details;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.timeseries.LocationForecastTimeSeriesItem;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindUtil;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import us.dustinj.timezonemap.TimeZoneMap;

public class MetNorwayResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_ICON_ID_MAP = new HashMap<>();
	private static final Map<String, String> FLICKR_MAP = new HashMap<>();

	public static void init(Context context) {
		if (WEATHER_ICON_DESCRIPTION_MAP.size() == 0 || WEATHER_ICON_ID_MAP.size() == 0 || FLICKR_MAP.size() == 0) {

			String[] codes = context.getResources().getStringArray(R.array.MetNorwayWeatherIconSymbols);
			String[] descriptions = context.getResources().getStringArray(R.array.MetNorwayWeatherIconDescriptionsForSymbol);
			TypedArray iconIds = context.getResources().obtainTypedArray(R.array.MetNorwayWeatherIconForSymbol);

			WEATHER_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < codes.length; i++) {
				WEATHER_ICON_DESCRIPTION_MAP.put(codes[i], descriptions[i]);
				WEATHER_ICON_ID_MAP.put(codes[i], iconIds.getResourceId(i, R.drawable.temp_icon));
			}

			String[] flickrGalleryNames = context.getResources().getStringArray(R.array.MetNorwayFlickrGalleryNames);

			FLICKR_MAP.clear();
			for (int i = 0; i < codes.length; i++) {
				FLICKR_MAP.put(codes[i], flickrGalleryNames[i]);
			}
		}
	}

	public static LocationForecastResponse getLocationForecastResponseObjFromJson(String response) {
		return new Gson().fromJson(response, LocationForecastResponse.class);
	}

	public static String getWeatherIconDescription(String symbolCode) {
		return WEATHER_ICON_DESCRIPTION_MAP.get(symbolCode.replace("day", "").replace("night", "")
				.replace("_", ""));
	}

	public static int getWeatherIconImg(String symbolCode) {
		boolean isNight = symbolCode.contains("night");
		symbolCode = symbolCode.replace("day", "").replace("night", "")
				.replace("_", "");
		int iconId = WEATHER_ICON_ID_MAP.get(symbolCode);

		if (symbolCode.contains("night")) {
			if (iconId == R.drawable.day_clear) {
				iconId = R.drawable.night_clear;
			} else if (iconId == R.drawable.day_partly_cloudy) {
				iconId = R.drawable.night_partly_cloudy;
			}
		}
		return iconId;
	}

	public static CurrentConditionsDto makeCurrentConditionsDto(Context context, LocationForecastResponse locationForecastResponse,
	                                                            ZoneId zoneId) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempUnitStr = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String percent = "%";


		ZonedDateTime time =
				ZonedDateTime.parse(locationForecastResponse.getProperties().getTimeSeries().get(0).getTime());
		time = time.withZoneSameInstant(zoneId);

		CurrentConditionsDto currentConditionsDto = new CurrentConditionsDto();
		Data data = locationForecastResponse.getProperties().getTimeSeries().get(0).getData();

		Double feelsLikeTemp =
				WeatherUtil.calcFeelsLikeTemperature(Double.parseDouble(data.getInstant().getDetails().getAirTemperature()),
						ValueUnits.convertWindSpeed(data.getInstant().getDetails().getWindSpeed(), ValueUnits.kmPerHour),
						Double.parseDouble(data.getInstant().getDetails().getRelativeHumidity()));

		currentConditionsDto.setCurrentTime(time);
		currentConditionsDto.setWeatherDescription(getWeatherIconDescription(data.getNext_1_hours().getSummary().getSymbolCode()));
		currentConditionsDto.setWeatherIcon(getWeatherIconImg(data.getNext_1_hours().getSummary().getSymbolCode()));
		currentConditionsDto.setTemp(ValueUnits.convertTemperature(data.getInstant().getDetails().getAirTemperature(), tempUnit) + tempUnitStr);
		currentConditionsDto.setFeelsLikeTemp(ValueUnits.convertTemperature(feelsLikeTemp.toString(), tempUnit) + tempUnitStr);
		currentConditionsDto.setHumidity((int) Double.parseDouble(data.getInstant().getDetails().getRelativeHumidity()) + percent);
		currentConditionsDto.setDewPoint(ValueUnits.convertTemperature(data.getInstant().getDetails().getDewPointTemperature(), tempUnit) + tempUnitStr);
		currentConditionsDto.setWindDirectionDegree((int) Double.parseDouble(data.getInstant().getDetails().getWindFromDirection()));
		currentConditionsDto.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, data.getInstant().getDetails().getWindFromDirection()));
		currentConditionsDto.setWindSpeed(ValueUnits.convertWindSpeed(data.getInstant().getDetails().getWindSpeed(), windUnit) + MyApplication.VALUE_UNIT_OBJ.getWindUnitText());

		currentConditionsDto.setSimpleWindStrength(WindUtil.getSimpleWindSpeedDescription(data.getInstant().getDetails().getWindSpeed()));
		currentConditionsDto.setWindStrength(WindUtil.getWindSpeedDescription(data.getInstant().getDetails().getWindSpeed()));
		currentConditionsDto.setPressure(data.getInstant().getDetails().getAirPressureAtSeaLevel() + "hpa");
		currentConditionsDto.setUvIndex(data.getInstant().getDetails().getUltravioletIndexClearSky());
		currentConditionsDto.setCloudiness(data.getInstant().getDetails().getCloudAreaFraction() + percent);

		double precipitationVolume = 0.0;

		precipitationVolume += Double.parseDouble(data.getNext_1_hours().getDetails().getPrecipitationAmount());

		if (precipitationVolume > 0.0) {
			currentConditionsDto.setPrecipitationVolume(String.format(Locale.getDefault(), "%.2f mm", precipitationVolume));
		}

		return currentConditionsDto;
	}

	public static List<HourlyForecastDto> makeHourlyForecastDtoList(Context context, LocationForecastResponse locationForecastResponse, ZoneId zoneId) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String percent = "%";
		final String mm = "mm";

		final String windUnitStr = MyApplication.VALUE_UNIT_OBJ.getWindUnitText();
		final String zeroPrecipitationVolume = "0.0mm";
		final String zero = "0";
		final String pressureUnit = "hpa";

		String precipitationVolume;

		boolean has1HoursPrecipitation = false;
		boolean has6HoursPrecipitation = false;

		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		HourlyForecastDto hourlyForecastDto = null;
		List<LocationForecastTimeSeriesItem> hourlyList = locationForecastResponse.getProperties().getTimeSeries();
		Details instantDetails = null;
		Double feelsLikeTemp = null;

		ZonedDateTime time = null;

		for (LocationForecastTimeSeriesItem hourly : hourlyList) {
			if (hourly.getData().getNext_1_hours() == null && hourly.getData().getNext_6_hours() == null) {
				break;
			}

			time = ZonedDateTime.parse(hourly.getTime());
			time = time.withZoneSameInstant(zoneId);
			time = time.withMinute(0).withSecond(0).withNano(0);

			instantDetails = hourly.getData().getInstant().getDetails();
			hourlyForecastDto = new HourlyForecastDto();

			feelsLikeTemp =
					WeatherUtil.calcFeelsLikeTemperature(Double.parseDouble(instantDetails.getAirTemperature()),
							ValueUnits.convertWindSpeed(instantDetails.getWindSpeed(), ValueUnits.kmPerHour),
							Double.parseDouble(instantDetails.getRelativeHumidity()));

			has6HoursPrecipitation = false;
			has1HoursPrecipitation = false;

			if (hourly.getData().getNext_1_hours() == null) {
				//이후 6시간 강수량 표기
				if (hourly.getData().getNext_6_hours().getDetails().getPrecipitationAmount().equals(zero)) {
					precipitationVolume = zeroPrecipitationVolume;
					has6HoursPrecipitation = false;
				} else {
					precipitationVolume = hourly.getData().getNext_6_hours().getDetails().getPrecipitationAmount() + mm;
					has6HoursPrecipitation = true;
				}
			} else {
				if (hourly.getData().getNext_1_hours().getDetails().getPrecipitationAmount().equals(zero)) {
					precipitationVolume = zeroPrecipitationVolume;
					has1HoursPrecipitation = false;
				} else {
					precipitationVolume = hourly.getData().getNext_1_hours().getDetails().getPrecipitationAmount() + mm;
					has1HoursPrecipitation = true;
				}
			}

			if (has1HoursPrecipitation) {
				hourlyForecastDto.setPrecipitationVolume(precipitationVolume);
			} else if (has6HoursPrecipitation) {
				hourlyForecastDto.setNext6HoursPrecipitationVolume(precipitationVolume);
			}

			int weatherIcon = 0;
			String weatherDescription = null;

			if (hourly.getData().getNext_1_hours() != null) {
				weatherIcon = getWeatherIconImg(hourly.getData().getNext_1_hours().getSummary().getSymbolCode());
				weatherDescription = getWeatherIconDescription(hourly.getData().getNext_1_hours().getSummary().getSymbolCode());
			} else {
				weatherIcon = getWeatherIconImg(hourly.getData().getNext_6_hours().getSummary().getSymbolCode());
				weatherDescription = getWeatherIconDescription(hourly.getData().getNext_6_hours().getSummary().getSymbolCode());
			}

			hourlyForecastDto.setHours(time)
					.setWeatherIcon(weatherIcon)
					.setTemp(ValueUnits.convertTemperature(instantDetails.getAirTemperature(), tempUnit) + tempDegree)
					.setHasNext6HoursPrecipitation(has6HoursPrecipitation)
					.setHasPrecipitation(has1HoursPrecipitation)
					.setWeatherDescription(weatherDescription)
					.setFeelsLikeTemp(ValueUnits.convertTemperature(feelsLikeTemp.toString(), tempUnit) + tempDegree)
					.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, instantDetails.getWindFromDirection()))
					.setWindDirectionVal((int) Double.parseDouble(instantDetails.getWindFromDirection()))
					.setWindSpeed(ValueUnits.convertWindSpeed(instantDetails.getWindSpeed(), windUnit) + windUnitStr)
					.setWindStrength(WindUtil.getSimpleWindSpeedDescription(instantDetails.getWindSpeed()))
					.setPressure(instantDetails.getAirPressureAtSeaLevel() + pressureUnit)
					.setHumidity((int) Double.parseDouble(instantDetails.getRelativeHumidity()) + percent)
					.setCloudiness(instantDetails.getCloudAreaFraction() + percent)
					.setUvIndex(instantDetails.getUltravioletIndexClearSky());

			hourlyForecastDtoList.add(hourlyForecastDto);

		}
		return hourlyForecastDtoList;
	}

	public static List<DailyForecastDto> makeDailyForecastDtoList(Context context, LocationForecastResponse locationForecastResponse, ZoneId zoneId) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String mm = "mm";
		final String wind = MyApplication.VALUE_UNIT_OBJ.getWindUnitText();
		final String zeroPrecipitationVolume = "0.0mm";
		final String zero = "0";

		final ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime time =
				ZonedDateTime.parse(locationForecastResponse.getProperties().getTimeSeries().get(0).getTime());
		time = time.withZoneSameInstant(zoneId);
		time = time.withMinute(0).withSecond(0).withNano(0);

		List<LocationForecastTimeSeriesItem> hourlyList = locationForecastResponse.getProperties().getTimeSeries();

		int tomorrowIdx = -1;
		int hasNotNext1HoursDayOfYear = -1;
		int idx_hasNotNext1Hours = -1;
		int startIdx_dayHasNotNext1Hours = -1;

		for (int i = 0; i < hourlyList.size(); i++) {

			if (tomorrowIdx == -1) {
				if (time.getDayOfMonth() != now.getDayOfMonth()) {
					tomorrowIdx = i;
				}
			}
			if (hasNotNext1HoursDayOfYear == -1) {
				if (hourlyList.get(i).getData().getNext_1_hours() == null) {
					hasNotNext1HoursDayOfYear = time.getDayOfYear();
					idx_hasNotNext1Hours = i;
					startIdx_dayHasNotNext1Hours = idx_hasNotNext1Hours - time.getHour();
				}
			}

			if (hasNotNext1HoursDayOfYear != -1 && tomorrowIdx != -1) {
				break;
			}

			time = time.plusHours(1);
		}

		double minTemp = Double.MAX_VALUE, maxTemp = Double.MIN_VALUE;
		// 강수량, 날씨 아이콘, 날씨 설명, 풍향, 풍속
		int hour = -1;
		int dayOfYear = -1;

		Data data = null;
		ZonedDateTime date = null;

		SortedMap<String, DailyForecastDto> forecastDtoSortedMap = new TreeMap<>();
		int i = tomorrowIdx;

		String precipitation = null;

		while (i < startIdx_dayHasNotNext1Hours) {
			data = hourlyList.get(i).getData();

			date =
					ZonedDateTime.parse(hourlyList.get(i).getTime());
			date = date.withZoneSameInstant(zoneId);
			date = date.withMinute(0).withSecond(0).withNano(0);
			hour = date.getHour();
			dayOfYear = date.getDayOfYear();

			if (dayOfYear == hasNotNext1HoursDayOfYear) {
				break;
			}

			if (!forecastDtoSortedMap.containsKey(date.toLocalDate().toString())) {
				DailyForecastDto dailyForecastDto = new DailyForecastDto();
				dailyForecastDto.setDate(date);
				forecastDtoSortedMap.put(date.toLocalDate().toString(), dailyForecastDto);
			}

			if (hour <= 23) {
				minTemp = Math.min(Double.parseDouble(data.getInstant().getDetails().getAirTemperature()), minTemp);
				maxTemp = Math.max(Double.parseDouble(data.getInstant().getDetails().getAirTemperature()), maxTemp);
			}

			if (!data.getNext_6_hours().getDetails().getPrecipitationAmount().equals(zero)) {
				precipitation = data.getNext_6_hours().getDetails().getPrecipitationAmount() + mm;
			} else {
				precipitation = null;
			}


			DailyForecastDto.Values values = new DailyForecastDto.Values();
			values.setMinTemp(ValueUnits.convertTemperature(data.getNext_6_hours().getDetails().getAirTemperatureMin(), tempUnit) + tempDegree)
					.setMaxTemp(ValueUnits.convertTemperature(data.getNext_6_hours().getDetails().getAirTemperatureMax(), tempUnit) + tempDegree)
					.setWeatherIcon(getWeatherIconImg(data.getNext_6_hours().getSummary().getSymbolCode()))
					.setWeatherDescription(getWeatherIconDescription(data.getNext_6_hours().getSummary().getSymbolCode()))
					.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, data.getInstant().getDetails().getWindFromDirection()))
					.setWindDirectionVal((int) Double.parseDouble(data.getInstant().getDetails().getWindFromDirection()))
					.setWindSpeed(ValueUnits.convertWindSpeed(data.getInstant().getDetails().getWindSpeed(), windUnit) + wind)
					.setWindStrength(WindUtil.getSimpleWindSpeedDescription(data.getInstant().getDetails().getWindSpeed()))
					.setHasPrecipitationNextHoursAmount(true)
					.setPrecipitationNextHoursAmount(6)
					.setDateTime(date);

			values.setHasPrecipitationVolume(precipitation != null)
					.setPrecipitationVolume(precipitation == null ? zeroPrecipitationVolume : precipitation);

			forecastDtoSortedMap.get(date.toLocalDate().toString()).getValuesList().add(values);

			if (hour == 23) {
				forecastDtoSortedMap.get(date.toLocalDate().toString()).setMinTemp(ValueUnits.convertTemperature(String.valueOf(minTemp), tempUnit) + tempDegree)
						.setMaxTemp(ValueUnits.convertTemperature(String.valueOf(maxTemp), tempUnit) + tempDegree)
						.setDate(date.withHour(0));
			}

			i += 6;
		}

		for (i = startIdx_dayHasNotNext1Hours; i < idx_hasNotNext1Hours; i++) {
			data = hourlyList.get(i).getData();

			date = ZonedDateTime.parse(hourlyList.get(i).getTime());
			date = date.withZoneSameInstant(zoneId);
			date = date.withMinute(0).withSecond(0).withNano(0);

			if (!forecastDtoSortedMap.containsKey(date.toLocalDate().toString())) {
				DailyForecastDto dailyForecastDto = new DailyForecastDto();
				dailyForecastDto.setDate(date);
				forecastDtoSortedMap.put(date.toLocalDate().toString(), dailyForecastDto);
			}

			DailyForecastDto.Values values = new DailyForecastDto.Values();

			values.setTemp(ValueUnits.convertTemperature(data.getInstant().getDetails().getAirTemperature(), tempUnit) + tempDegree)
					.setWeatherIcon(getWeatherIconImg(data.getNext_1_hours().getSummary().getSymbolCode()))
					.setWeatherDescription(getWeatherIconDescription(data.getNext_1_hours().getSummary().getSymbolCode()))
					.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, data.getInstant().getDetails().getWindFromDirection()))
					.setWindDirectionVal((int) Double.parseDouble(data.getInstant().getDetails().getWindFromDirection()))
					.setWindSpeed(ValueUnits.convertWindSpeed(data.getInstant().getDetails().getWindSpeed(), windUnit) + wind)
					.setWindStrength(WindUtil.getSimpleWindSpeedDescription(data.getInstant().getDetails().getWindSpeed()))
					.setHasPrecipitationNextHoursAmount(false)
					.setDateTime(date);

			if (!data.getNext_1_hours().getDetails().getPrecipitationAmount().equals(zero)) {
				precipitation = data.getNext_1_hours().getDetails().getPrecipitationAmount() + mm;
			} else {
				precipitation = null;
			}

			values.setHasPrecipitationVolume(precipitation != null)
					.setPrecipitationVolume(precipitation == null ? zeroPrecipitationVolume : precipitation);

			forecastDtoSortedMap.get(date.toLocalDate().toString()).getValuesList().add(values);
		}

		for (i = idx_hasNotNext1Hours; i < hourlyList.size(); i++) {
			data = hourlyList.get(i).getData();

			if (data.getNext_6_hours() == null) {
				break;
			}

			date = ZonedDateTime.parse(hourlyList.get(i).getTime());
			date = date.withZoneSameInstant(zoneId);
			date = date.withMinute(0).withSecond(0).withNano(0);

			if (!forecastDtoSortedMap.containsKey(date.toLocalDate().toString())) {
				DailyForecastDto dailyForecastDto = new DailyForecastDto();
				dailyForecastDto.setDate(date);
				forecastDtoSortedMap.put(date.toLocalDate().toString(), dailyForecastDto);
			}

			DailyForecastDto.Values values = new DailyForecastDto.Values();

			values.setMinTemp(ValueUnits.convertTemperature(data.getNext_6_hours().getDetails().getAirTemperatureMin(), tempUnit) + tempDegree)
					.setMaxTemp(ValueUnits.convertTemperature(data.getNext_6_hours().getDetails().getAirTemperatureMax(), tempUnit) + tempDegree)
					.setWeatherIcon(getWeatherIconImg(data.getNext_6_hours().getSummary().getSymbolCode()))
					.setWeatherDescription(getWeatherIconDescription(data.getNext_6_hours().getSummary().getSymbolCode()))
					.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, data.getInstant().getDetails().getWindFromDirection()))
					.setWindDirectionVal((int) Double.parseDouble(data.getInstant().getDetails().getWindFromDirection()))
					.setWindSpeed(ValueUnits.convertWindSpeed(data.getInstant().getDetails().getWindSpeed(), windUnit) + wind)
					.setWindStrength(WindUtil.getSimpleWindSpeedDescription(data.getInstant().getDetails().getWindSpeed()))
					.setHasPrecipitationNextHoursAmount(true)
					.setPrecipitationNextHoursAmount(6)
					.setDateTime(date);

			if (!data.getNext_6_hours().getDetails().getPrecipitationAmount().equals(zero)) {
				precipitation = data.getNext_6_hours().getDetails().getPrecipitationAmount() + mm;
			} else {
				precipitation = zeroPrecipitationVolume;
			}

			values.setHasPrecipitationVolume(precipitation != null)
					.setPrecipitationVolume(precipitation == null ? zeroPrecipitationVolume : precipitation);

			forecastDtoSortedMap.get(date.toLocalDate().toString()).getValuesList().add(values);
		}

		List<DailyForecastDto> dailyForecastDtos = new ArrayList<>();
		dailyForecastDtos.addAll(forecastDtoSortedMap.values());
		return dailyForecastDtos;
	}

	public static ZoneId getZoneId(Double latitude, Double longitude) {
		TimeZoneMap map = TimeZoneMap.forRegion(latitude - 4.0,
				longitude - 4.0, latitude + 4.0
				, longitude + 4.0);

		String area = map.getOverlappingTimeZone(latitude, longitude).getZoneId();
		return ZoneId.of(area);
	}

	public static String getFlickrGalleryName(String code) {
		return FLICKR_MAP.get(code);
	}
}
