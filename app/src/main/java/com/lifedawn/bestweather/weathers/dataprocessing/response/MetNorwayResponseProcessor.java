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

		if (isNight) {
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
			currentConditionsDto.setPrecipitationVolume(String.format(Locale.getDefault(), "%.1fmm", precipitationVolume));
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
		final String zero = "0.0";
		final String pressureUnit = "hpa";

		String precipitationVolume;

		boolean hasPrecipitation = false;

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

			if (hourly.getData().getNext_1_hours() == null) {
				//이후 6시간 강수량 표기
				if (hourly.getData().getNext_6_hours().getDetails().getPrecipitationAmount().equals(zero)) {
					precipitationVolume = zeroPrecipitationVolume;
					hasPrecipitation = false;
				} else {
					precipitationVolume = hourly.getData().getNext_6_hours().getDetails().getPrecipitationAmount() + mm;
					hasPrecipitation = true;
				}
			} else {
				if (hourly.getData().getNext_1_hours().getDetails().getPrecipitationAmount().equals(zero)) {
					precipitationVolume = zeroPrecipitationVolume;
					hasPrecipitation = false;
				} else {
					precipitationVolume = hourly.getData().getNext_1_hours().getDetails().getPrecipitationAmount() + mm;
					hasPrecipitation = true;
				}
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
					.setHasNext6HoursPrecipitation(hourly.getData().getNext_1_hours() == null)
					.setHasPrecipitation(hasPrecipitation)
					.setWeatherDescription(weatherDescription)
					.setFeelsLikeTemp(ValueUnits.convertTemperature(feelsLikeTemp.toString(), tempUnit) + tempDegree)
					.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, instantDetails.getWindFromDirection()))
					.setWindDirectionVal((int) Double.parseDouble(instantDetails.getWindFromDirection()))
					.setWindSpeed(ValueUnits.convertWindSpeed(instantDetails.getWindSpeed(), windUnit) + windUnitStr)
					.setWindStrength(WindUtil.getSimpleWindSpeedDescription(instantDetails.getWindSpeed()))
					.setPressure(instantDetails.getAirPressureAtSeaLevel() + pressureUnit)
					.setHumidity((int) Double.parseDouble(instantDetails.getRelativeHumidity()) + percent)
					.setCloudiness(instantDetails.getCloudAreaFraction() + percent)
					.setUvIndex(instantDetails.getUltravioletIndexClearSky())
					.setPop("-")
					.setPrecipitationVolume(precipitationVolume);

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
		final String zero = "0.0";

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
		Data data = null;
		ZonedDateTime date = null;

		SortedMap<String, DailyForecastDto> forecastDtoSortedMap = new TreeMap<>();
		int i = tomorrowIdx;

		String precipitation = null;
		boolean hasPrecipitation = false;

		while (i < idx_hasNotNext1Hours) {
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
					.setPop("-")
					.setDateTime(date);


			if (!data.getNext_1_hours().getDetails().getPrecipitationAmount().equals(zero)) {
				precipitation = data.getNext_1_hours().getDetails().getPrecipitationAmount() + mm;
				hasPrecipitation = true;
			} else {
				precipitation = zeroPrecipitationVolume;
				hasPrecipitation = false;
			}

			values.setHasPrecipitationVolume(hasPrecipitation)
					.setPrecipitationVolume(precipitation);

			forecastDtoSortedMap.get(date.toLocalDate().toString()).getValuesList().add(values);

			if (date.getHour() == 23) {
				forecastDtoSortedMap.get(date.toLocalDate().toString()).setHaveOnly1HoursForecast(true);
			}

			i++;

			hasPrecipitation = false;
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
					.setPop("-")
					.setDateTime(date);

			if (!data.getNext_6_hours().getDetails().getPrecipitationAmount().equals(zero)) {
				precipitation = data.getNext_6_hours().getDetails().getPrecipitationAmount() + mm;
				hasPrecipitation = true;
			} else {
				precipitation = zeroPrecipitationVolume;
				hasPrecipitation = false;
			}

			values.setHasPrecipitationVolume(hasPrecipitation)
					.setPrecipitationVolume(precipitation);

			forecastDtoSortedMap.get(date.toLocalDate().toString()).getValuesList().add(values);
		}

		List<DailyForecastDto> dailyForecastDtos = new ArrayList<>();
		dailyForecastDtos.addAll(forecastDtoSortedMap.values());


		for (DailyForecastDto dto : dailyForecastDtos) {
			int hours = 0;
			minTemp = Double.MAX_VALUE;
			maxTemp = Double.MIN_VALUE;

			for (DailyForecastDto.Values values : dto.getValuesList()) {
				hours = values.getPrecipitationNextHoursAmount() == 0 ? hours + 1 : hours + values.getPrecipitationNextHoursAmount();

				if (values.isHasPrecipitationNextHoursAmount()) {
					// 6 hours
					minTemp = Math.min(Integer.parseInt(values.getMinTemp().replace(tempDegree, "")), minTemp);
					maxTemp = Math.max(Integer.parseInt(values.getMaxTemp().replace(tempDegree, "")), maxTemp);
				} else {
					// 1 hours
					int temp = Integer.parseInt(values.getTemp().replace(tempDegree, ""));
					minTemp = Math.min(temp, minTemp);
					maxTemp = Math.max(temp, maxTemp);
				}
			}

			if (hours < 23) {
				dto.setAvailable_toMakeMinMaxTemp(false);
			} else {
				// available
				dto.setMinTemp(ValueUnits.convertTemperature(String.valueOf(minTemp), tempUnit) + tempDegree)
						.setMaxTemp(ValueUnits.convertTemperature(String.valueOf(maxTemp), tempUnit) + tempDegree);

				List<DailyForecastDto.Values> newValues = new ArrayList<>();
				DailyForecastDto.Values values = null;
				double precipitationVolume = 0.0;

				if (dto.isHaveOnly1HoursForecast()) {
					//1시간별 예보만 포함

					for (int idx = 0; idx < 24; idx++) {
						int temp = Integer.parseInt(dto.getValuesList().get(idx).getTemp().replace(tempDegree, ""));
						minTemp = Math.min(temp, minTemp);
						maxTemp = Math.max(temp, maxTemp);

						precipitationVolume += Double.parseDouble(dto.getValuesList().get(idx).getPrecipitationVolume().replace(mm, ""));

						if (idx == 5 || idx == 11 || idx == 17 || idx == 23) {
							values = new DailyForecastDto.Values();

							values.setHasPrecipitationNextHoursAmount(true).setPrecipitationNextHoursAmount(6)
									.setMinTemp(ValueUnits.convertTemperature(String.valueOf(minTemp), tempUnit) + tempDegree)
									.setMaxTemp(ValueUnits.convertTemperature(String.valueOf(maxTemp), tempUnit) + tempDegree)
									.setWeatherIcon(dto.getValuesList().get(idx).getWeatherIcon())
									.setWeatherDescription(dto.getValuesList().get(idx).getWeatherDescription())
									.setWindDirection(dto.getValuesList().get(idx).getWindDirection())
									.setWindDirectionVal(dto.getValuesList().get(idx).getWindDirectionVal())
									.setWindSpeed(dto.getValuesList().get(idx).getWindSpeed())
									.setWindStrength(dto.getValuesList().get(idx).getWindStrength())
									.setPrecipitationVolume(precipitationVolume == 0.0 ? zeroPrecipitationVolume :
											String.format(Locale.getDefault(), "%.1f", (float) precipitationVolume) + mm)
									.setHasPrecipitationVolume(precipitationVolume != 0.0)
									.setPop("-")
									.setDateTime(dto.getValuesList().get(idx).getDateTime());

							newValues.add(values);

							minTemp = Double.MAX_VALUE;
							maxTemp = Double.MIN_VALUE;
							precipitationVolume = 0.0;
						}
					}

					dto.getValuesList().clear();
					dto.getValuesList().addAll(newValues);
				} else if (dto.getValuesList().size() > 4) {
					//1시간, 6시간별 예보 혼합

					int startIdx_6HoursForecast = 0;
					int hours_start6HoursForecast = 0;
					int count_6HoursForecast = 0;

					for (int idx = 0; idx < dto.getValuesList().size(); idx++) {
						if (dto.getValuesList().get(idx).isHasPrecipitationNextHoursAmount()) {
							startIdx_6HoursForecast = idx;
							hours_start6HoursForecast = dto.getValuesList().get(idx).getDateTime().getHour();
							count_6HoursForecast = dto.getValuesList().size() - startIdx_6HoursForecast;
							break;
						}
					}

					if (count_6HoursForecast == 4) {
						for (int count = 0; count < startIdx_6HoursForecast; count++) {
							dto.getValuesList().remove(0);
						}
					} else {
						for (int idx = startIdx_6HoursForecast - 1; idx >= 0; idx--) {
							int temp = Integer.parseInt(dto.getValuesList().get(idx).getTemp().replace(tempDegree, ""));
							minTemp = Math.min(temp, minTemp);
							maxTemp = Math.max(temp, maxTemp);

							precipitationVolume += Double.parseDouble(dto.getValuesList().get(idx).getPrecipitationVolume().replace(mm, ""));

							if (idx == startIdx_6HoursForecast - 6 || idx == startIdx_6HoursForecast - 12 ||
									idx == startIdx_6HoursForecast - 18 ||
									idx == startIdx_6HoursForecast - 24) {
								values = new DailyForecastDto.Values();

								values.setHasPrecipitationNextHoursAmount(true).setPrecipitationNextHoursAmount(6)
										.setMinTemp(ValueUnits.convertTemperature(String.valueOf(minTemp), tempUnit) + tempDegree)
										.setMaxTemp(ValueUnits.convertTemperature(String.valueOf(maxTemp), tempUnit) + tempDegree)
										.setWeatherIcon(dto.getValuesList().get(idx).getWeatherIcon())
										.setWeatherDescription(dto.getValuesList().get(idx).getWeatherDescription())
										.setWindDirection(dto.getValuesList().get(idx).getWindDirection())
										.setWindDirectionVal(dto.getValuesList().get(idx).getWindDirectionVal())
										.setWindSpeed(dto.getValuesList().get(idx).getWindSpeed())
										.setWindStrength(dto.getValuesList().get(idx).getWindStrength())
										.setPrecipitationVolume(precipitationVolume == 0.0 ? zeroPrecipitationVolume :
												String.format(Locale.getDefault(), "%.1f", (float) precipitationVolume) + mm)
										.setHasPrecipitationVolume(precipitationVolume != 0.0)
										.setPop("-")
										.setDateTime(dto.getValuesList().get(idx).getDateTime());

								newValues.add(values);

								minTemp = Double.MAX_VALUE;
								maxTemp = Double.MIN_VALUE;
								precipitationVolume = 0.0;
							}
						}

						for (int count = 0; count < startIdx_6HoursForecast; count++) {
							dto.getValuesList().remove(0);
						}
						dto.getValuesList().addAll(newValues);
					}
				}
			}
		}

		int noAvailableDayCount = 0;
		for (i = 0; i < dailyForecastDtos.size(); i++) {
			if (!dailyForecastDtos.get(i).isAvailable_toMakeMinMaxTemp()) {
				noAvailableDayCount = dailyForecastDtos.size() - i;
				break;
			}
		}

		for (i = 0; i < noAvailableDayCount; i++) {
			dailyForecastDtos.remove(dailyForecastDtos.size() - 1);
		}

		return dailyForecastDtos;
	}

	public static ZoneId getZoneId(Double latitude, Double longitude) {
		TimeZoneMap map = TimeZoneMap.forRegion(latitude - 2.0,
				longitude - 2.0, latitude + 2.0
				, longitude + 2.0);

		String area = map.getOverlappingTimeZone(latitude, longitude).getZoneId();
		return ZoneId.of(area);
	}

	public static String getFlickrGalleryName(String code) {
		return FLICKR_MAP.get(code);
	}
}
