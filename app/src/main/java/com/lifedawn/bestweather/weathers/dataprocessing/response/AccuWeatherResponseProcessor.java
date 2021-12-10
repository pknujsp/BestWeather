package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.res.TypedArray;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.geopositionsearch.GeoPositionResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WindDirectionConverter;
import com.lifedawn.bestweather.weathers.detailfragment.dto.DailyForecastDto;
import com.lifedawn.bestweather.weathers.detailfragment.dto.HourlyForecastDto;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class AccuWeatherResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> PTY_MAP = new HashMap<>();
	private static final Map<String, Integer> PTY_ICON_MAP = new HashMap<>();
	private static final Map<String, String> FLICKR_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_ICON_ID_MAP = new HashMap<>();

	private AccuWeatherResponseProcessor() {
	}

	public static void init(Context context) {
		if (WEATHER_ICON_DESCRIPTION_MAP.isEmpty() || PTY_MAP.isEmpty() || FLICKR_MAP.isEmpty() || WEATHER_ICON_ID_MAP.isEmpty()) {
			String[] codes = context.getResources().getStringArray(R.array.AccuWeatherWeatherIconCodes);
			String[] descriptions = context.getResources().getStringArray(R.array.AccuWeatherWeatherIconDescriptionsForCode);
			TypedArray iconIds = context.getResources().obtainTypedArray(R.array.AccuWeatherWeatherIconForCode);

			WEATHER_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < codes.length; i++) {
				WEATHER_ICON_DESCRIPTION_MAP.put(codes[i], descriptions[i]);
				WEATHER_ICON_ID_MAP.put(codes[i], iconIds.getResourceId(i, 0));
			}

			String[] flickrGalleryNames = context.getResources().getStringArray(R.array.AccuWeatherFlickrGalleryNames);

			FLICKR_MAP.clear();
			for (int i = 0; i < codes.length; i++) {
				FLICKR_MAP.put(codes[i], flickrGalleryNames[i]);
			}

			//     <!-- precipitation type 값 종류 : Rain, Snow, Ice, Null(Not), or Mixed -->
			PTY_MAP.clear();
			PTY_MAP.put("Rain", context.getString(R.string.accu_weather_pty_rain));
			PTY_MAP.put("Snow", context.getString(R.string.accu_weather_pty_snow));
			PTY_MAP.put("Ice", context.getString(R.string.accu_weather_pty_ice));
			PTY_MAP.put("Null", context.getString(R.string.accu_weather_pty_not));
			PTY_MAP.put("Mixed", context.getString(R.string.accu_weather_pty_mixed));

			PTY_ICON_MAP.clear();
			PTY_ICON_MAP.put("Rain", R.drawable.raindrop);
			PTY_ICON_MAP.put("Snow", R.drawable.snowparticle);
			PTY_ICON_MAP.put("Ice", R.drawable.snowparticle);
			PTY_ICON_MAP.put("Null", R.drawable.pty_null);
			PTY_ICON_MAP.put("Mixed", R.drawable.sleet);
		}
	}

	public static int getWeatherIconImg(String code) {
		return WEATHER_ICON_ID_MAP.get(code);
	}

	public static String getWeatherIconDescription(String code) {
		return WEATHER_ICON_DESCRIPTION_MAP.get(code);
	}

	public static String getPty(String pty) {
		return pty == null ? PTY_MAP.get("Null") : PTY_MAP.get(pty);
	}

	public static int getPtyIcon(String pty) {
		return pty == null ? PTY_ICON_MAP.get("Null") : PTY_ICON_MAP.get(pty);
	}

	public static GeoPositionResponse getGeoPositionObjFromJson(String response) {
		return new Gson().fromJson(response, GeoPositionResponse.class);
	}

	public static CurrentConditionsResponse getCurrentConditionsObjFromJson(JsonElement jsonElement) {
		CurrentConditionsResponse response = new CurrentConditionsResponse();
		response.setItems(jsonElement);
		return response;
	}

	public static TwelveHoursOfHourlyForecastsResponse getHourlyForecastObjFromJson(JsonElement jsonElement) {
		TwelveHoursOfHourlyForecastsResponse response = new TwelveHoursOfHourlyForecastsResponse();
		response.setItems(jsonElement);
		return response;
	}

	public static FiveDaysOfDailyForecastsResponse getDailyForecastObjFromJson(String response) {
		return new Gson().fromJson(response, FiveDaysOfDailyForecastsResponse.class);
	}

	public static String getFlickrGalleryName(String code) {
		return FLICKR_MAP.get(code);
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

	public static List<HourlyForecastDto> makeHourlyForecastDtoList(Context context,
	                                                                List<TwelveHoursOfHourlyForecastsResponse.Item> hourlyForecastList,
	                                                                ValueUnits windUnit, ValueUnits tempUnit, ValueUnits visibilityUnit,
	                                                                ZoneId zoneId) {
		final String tempDegree = "º";
		final String percent = "%";
		final String mm = "mm";
		final String cm = "cm";

		final String zero = "0";
		final String zeroSnowVolume = "0.0cm";
		final String zeroRainVolume = "0.0mm";
		final String zeroTotalRiquidVolume = "0.0mm";
		final String visibilityUnitStr = ValueUnits.convertToStr(context, visibilityUnit);
		final String windUnitStr = ValueUnits.convertToStr(context, windUnit);

		String totalRiquidVolume;
		String snowVolume;
		String rainVolume;

		boolean hasPrecipitation;
		boolean hasSnow;
		boolean hasRain;

		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		for (TwelveHoursOfHourlyForecastsResponse.Item hourly : hourlyForecastList) {
			HourlyForecastDto hourlyForecastDto = new HourlyForecastDto();

			if (!hourly.getRain().getValue().equals(zero)) {
				hasRain = true;
				rainVolume = hourly.getRain().getValue() + mm;
			} else {
				hasRain = false;
				rainVolume = zeroRainVolume;
			}

			if (!hourly.getSnow().getValue().equals(zero)) {
				hasSnow = true;
				snowVolume = hourly.getSnow().getValue() + cm;
			} else {
				hasSnow = false;
				snowVolume = zeroSnowVolume;
			}

			if (!hourly.getTotalLiquid().getValue().equals(zero)) {
				hasPrecipitation = true;
				totalRiquidVolume = hourly.getTotalLiquid().getValue() + mm;
			} else {
				hasPrecipitation = false;
				totalRiquidVolume = zeroTotalRiquidVolume;
			}

			hourlyForecastDto.setHours(WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourly.getEpochDateTime()) * 1000L,
					zoneId))
					.setTemp(ValueUnits.convertTemperature(hourly.getTemperature().getValue(), tempUnit) + tempDegree)
					.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(hourly.getWeatherIcon()))
					.setWeatherDescription(AccuWeatherResponseProcessor.getWeatherIconDescription(hourly.getWeatherIcon()))
					.setHasPrecipitation(hasPrecipitation)
					.setHasRain(hasRain)
					.setHasSnow(hasSnow)
					.setPrecipitationVolume(totalRiquidVolume)
					.setPop((int) (Double.parseDouble(hourly.getPrecipitationProbability())) + percent)
					.setRainVolume(rainVolume)
					.setSnowVolume(snowVolume)
					.setFeelsLikeTemp(ValueUnits.convertTemperature(hourly.getRealFeelTemperature().getValue(), tempUnit) + tempDegree)
					.setPor((int) (Double.parseDouble(hourly.getRainProbability())) + percent)
					.setPos((int) (Double.parseDouble(hourly.getSnowProbability())) + percent)
					.setPrecipitationType(AccuWeatherResponseProcessor.getPty(hourly.getPrecipitationType()))
					.setPrecipitationTypeIcon(AccuWeatherResponseProcessor.getPtyIcon(hourly.getPrecipitationType()))
					.setWindDirectionVal(Integer.parseInt(hourly.getWind().getDirection().getDegrees()))
					.setWindDirection(WindDirectionConverter.windDirection(context, hourly.getWind().getDirection().getDegrees()))
					.setWindSpeed(ValueUnits.convertWindSpeedForAccu(hourly.getWind().getSpeed().getValue(), windUnit) + windUnitStr)
					.setWindGust(ValueUnits.convertWindSpeedForAccu(hourly.getWindGust().getSpeed().getValue(), windUnit) + windUnitStr)
					.setWindStrength(ValueUnits.convertWindSpeedForAccu(hourly.getWind().getSpeed().getValue(), ValueUnits.mPerSec).toString())
					.setHumidity(hourly.getRelativeHumidity() + percent)
					.setDewPointTemp(ValueUnits.convertTemperature(hourly.getDewPoint().getValue(), tempUnit) + tempDegree)
					.setCloudiness(hourly.getCloudCover() + percent)
					.setVisibility(ValueUnits.convertVisibilityForAccu(hourly.getVisibility().getValue(), visibilityUnit) + visibilityUnitStr);

			hourlyForecastDtoList.add(hourlyForecastDto);
		}
		return hourlyForecastDtoList;
	}


	public static List<DailyForecastDto> makeDailyForecastDtoList(Context context,
	                                                              List<FiveDaysOfDailyForecastsResponse.DailyForecasts> dailyForecastList, ValueUnits windUnit, ValueUnits tempUnit,
	                                                              ZoneId zoneId) {
		final String tempDegree = context.getString(R.string.degree_symbol);
		final String mm = "mm";
		final String cm = "cm";
		final String percent = "%";
		final String windUnitStr = ValueUnits.convertToStr(context, windUnit);
		final String zero = "0";

		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();

		String totalRiquidVolume = null;
		String rainVolume = null;
		String snowVolume = null;

		boolean hasPrecipitation;
		boolean hasRain;
		boolean hasSnow;

		for (FiveDaysOfDailyForecastsResponse.DailyForecasts daily : dailyForecastList) {
			DailyForecastDto dailyForecastDto = new DailyForecastDto();

			dailyForecastDto.setDate(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(daily.getEpochDate()) * 1000L, zoneId))
					.setSingle(false).setMinTemp(ValueUnits.convertTemperature(daily.getTemperature().getMinimum().getValue(), tempUnit) + tempDegree)
					.setMaxTemp(ValueUnits.convertTemperature(daily.getTemperature().getMaximum().getValue(), tempUnit) + tempDegree)
					.setMinFeelsLikeTemp(ValueUnits.convertTemperature(daily.getRealFeelTemperature().getMinimum().getValue(), tempUnit) + tempDegree)
					.setMaxFeelsLikeTemp(ValueUnits.convertTemperature(daily.getRealFeelTemperature().getMinimum().getValue(),
							tempUnit) + tempDegree);

			DailyForecastDto.Values am = new DailyForecastDto.Values();
			DailyForecastDto.Values pm = new DailyForecastDto.Values();

			if (!daily.getDay().getRain().getValue().equals(zero)) {
				hasRain = true;
				rainVolume = daily.getDay().getRain().getValue() + mm;
			} else {
				hasRain = false;
				rainVolume = null;
			}

			if (!daily.getDay().getSnow().getValue().equals(zero)) {
				hasSnow = true;
				snowVolume = daily.getDay().getSnow().getValue() + cm;
			} else {
				hasSnow = false;
				snowVolume = null;
			}

			if (!daily.getDay().getTotalLiquid().getValue().equals(zero)) {
				hasPrecipitation = true;
				totalRiquidVolume = daily.getDay().getTotalLiquid().getValue() + mm;
			} else {
				hasPrecipitation = false;
				totalRiquidVolume = null;
			}

			am.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(daily.getDay().getIcon()));
			am.setWeatherDescription(AccuWeatherResponseProcessor.getWeatherIconDescription(daily.getDay().getIcon()));
			am.setPop(daily.getDay().getPrecipitationProbability() + percent);
			am.setPor(daily.getDay().getRainProbability() + percent);
			am.setPos(daily.getDay().getSnowProbability() + percent);
			am.setHasPrecipitationVolume(hasPrecipitation);
			am.setPrecipitationVolume(totalRiquidVolume);
			am.setHasRainVolume(hasRain);
			am.setRainVolume(rainVolume);
			am.setHasSnowVolume(hasSnow);
			am.setSnowVolume(snowVolume);
			am.setWindDirection(WindDirectionConverter.windDirection(context, daily.getDay().getWind().getDirection().getDegrees()));
			am.setWindDirectionVal(Integer.parseInt(daily.getDay().getWind().getDirection().getDegrees()));
			am.setWindSpeed(ValueUnits.convertWindSpeedForAccu(daily.getDay().getWind().getSpeed().getValue(), windUnit) + windUnitStr);
			am.setWindStrength(WeatherResponseProcessor.getSimpleWindSpeedDescription(ValueUnits.convertWindSpeedForAccu(daily.getDay().getWind().getSpeed().getValue(), ValueUnits.mPerSec).toString()));
			am.setWindGust(ValueUnits.convertWindSpeedForAccu(daily.getDay().getWind().getSpeed().getValue(), windUnit) + windUnitStr);
			am.setCloudiness(daily.getDay().getCloudCover() + percent);

			if (!daily.getNight().getRain().getValue().equals(zero)) {
				hasRain = true;
				rainVolume = daily.getNight().getRain().getValue() + mm;
			} else {
				hasRain = false;
				rainVolume = null;
			}

			if (!daily.getNight().getSnow().getValue().equals(zero)) {
				hasSnow = true;
				snowVolume = daily.getNight().getSnow().getValue() + cm;
			} else {
				hasSnow = false;
				snowVolume = null;
			}

			if (!daily.getNight().getTotalLiquid().getValue().equals(zero)) {
				hasPrecipitation = true;
				totalRiquidVolume = daily.getNight().getTotalLiquid().getValue() + mm;
			} else {
				hasPrecipitation = false;
				totalRiquidVolume = null;
			}

			pm.setWeatherIcon(AccuWeatherResponseProcessor.getWeatherIconImg(daily.getNight().getIcon()));
			pm.setWeatherDescription(AccuWeatherResponseProcessor.getWeatherIconDescription(daily.getNight().getIcon()));
			pm.setPop(daily.getNight().getPrecipitationProbability() + percent);
			pm.setPor(daily.getNight().getRainProbability() + percent);
			pm.setPos(daily.getNight().getSnowProbability() + percent);
			pm.setHasPrecipitationVolume(hasPrecipitation);
			pm.setPrecipitationVolume(totalRiquidVolume);
			pm.setHasRainVolume(hasRain);
			pm.setRainVolume(rainVolume);
			pm.setHasSnowVolume(hasSnow);
			pm.setSnowVolume(snowVolume);
			pm.setWindDirection(WindDirectionConverter.windDirection(context, daily.getNight().getWind().getDirection().getDegrees()));
			pm.setWindDirectionVal(Integer.parseInt(daily.getNight().getWind().getDirection().getDegrees()));
			pm.setWindSpeed(ValueUnits.convertWindSpeedForAccu(daily.getNight().getWind().getSpeed().getValue(), windUnit) + windUnitStr);
			pm.setWindStrength(WeatherResponseProcessor.getSimpleWindSpeedDescription(ValueUnits.convertWindSpeedForAccu(daily.getNight().getWind().getSpeed().getValue(), ValueUnits.mPerSec).toString()));
			pm.setWindGust(ValueUnits.convertWindSpeedForAccu(daily.getNight().getWind().getSpeed().getValue(), windUnit) + windUnitStr);
			pm.setCloudiness(daily.getNight().getCloudCover() + percent);

			dailyForecastDto.setAmValues(am).setPmValues(pm);
			dailyForecastDtoList.add(dailyForecastDto);
		}
		return dailyForecastDtoList;
	}
}
