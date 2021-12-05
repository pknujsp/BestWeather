package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.content.res.TypedArray;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.kma.json.kmacommons.KmaHeader;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstItem;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaItem;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.json.ultrasrtfcstresponse.UltraSrtFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.json.ultrasrtncstresponse.UltraSrtNcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstItem;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstresponse.VilageFcstRoot;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class KmaResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_SKY_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> WEATHER_PTY_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> WEATHER_MID_ICON_DESCRIPTION_MAP = new HashMap<>();

	private static final Map<String, Integer> WEATHER_SKY_ICON_ID_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_PTY_ICON_ID_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_MID_ICON_ID_MAP = new HashMap<>();

	private static final Map<String, String> PTY_FLICKR_MAP = new HashMap<>();
	private static final Map<String, String> SKY_FLICKR_MAP = new HashMap<>();

	private static final String POP = "POP";
	private static final String PTY = "PTY";
	private static final String PCP = "PCP";
	private static final String REH = "REH";
	private static final String SNO = "SNO";
	private static final String SKY = "SKY";
	private static final String TMP = "TMP";
	private static final String TMN = "TMN";
	private static final String TMX = "TMX";
	private static final String UUU = "UUU";
	private static final String VVV = "VVV";
	private static final String WAV = "WAV";
	private static final String VEC = "VEC";
	private static final String WSD = "WSD";
	private static final String T1H = "T1H";
	private static final String RN1 = "RN1";
	private static final String LGT = "LGT";

	private KmaResponseProcessor() {
	}

	public static void init(Context context) {
		if (WEATHER_SKY_ICON_DESCRIPTION_MAP.isEmpty() || WEATHER_PTY_ICON_DESCRIPTION_MAP.isEmpty() || WEATHER_MID_ICON_DESCRIPTION_MAP.isEmpty()
				|| WEATHER_SKY_ICON_ID_MAP.isEmpty() || WEATHER_PTY_ICON_ID_MAP.isEmpty() || WEATHER_MID_ICON_ID_MAP.isEmpty() || PTY_FLICKR_MAP.isEmpty() ||
				SKY_FLICKR_MAP.isEmpty()) {
			String[] skyCodes = context.getResources().getStringArray(R.array.KmaSkyIconCodes);
			String[] ptyCodes = context.getResources().getStringArray(R.array.KmaPtyIconCodes);
			String[] midCodes = context.getResources().getStringArray(R.array.KmaMidIconCodes);
			String[] skyDescriptions = context.getResources().getStringArray(R.array.KmaSkyIconDescriptionsForCode);
			String[] ptyDescriptions = context.getResources().getStringArray(R.array.KmaPtyIconDescriptionsForCode);
			String[] midDescriptions = context.getResources().getStringArray(R.array.KmaMidIconDescriptionsForCode);
			TypedArray ptyIconIds = context.getResources().obtainTypedArray(R.array.KmaPtyWeatherIconForCode);
			TypedArray skyIconIds = context.getResources().obtainTypedArray(R.array.KmaSkyWeatherIconForCode);
			TypedArray midIconIds = context.getResources().obtainTypedArray(R.array.KmaMidWeatherIconForCode);

			WEATHER_SKY_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < skyCodes.length; i++) {
				WEATHER_SKY_ICON_DESCRIPTION_MAP.put(skyCodes[i], skyDescriptions[i]);
				WEATHER_SKY_ICON_ID_MAP.put(skyCodes[i], skyIconIds.getResourceId(i, R.drawable.temp_icon));
			}

			WEATHER_PTY_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < ptyCodes.length; i++) {
				WEATHER_PTY_ICON_DESCRIPTION_MAP.put(ptyCodes[i], ptyDescriptions[i]);
				WEATHER_PTY_ICON_ID_MAP.put(ptyCodes[i], ptyIconIds.getResourceId(i, R.drawable.temp_icon));
			}

			WEATHER_MID_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < midCodes.length; i++) {
				WEATHER_MID_ICON_DESCRIPTION_MAP.put(midCodes[i], midDescriptions[i]);
				WEATHER_MID_ICON_ID_MAP.put(midCodes[i], midIconIds.getResourceId(i, R.drawable.temp_icon));
			}

			String[] ptyFlickrGalleryNames = context.getResources().getStringArray(R.array.KmaPtyFlickrGalleryNames);
			String[] skyFlickrGalleryNames = context.getResources().getStringArray(R.array.KmaSkyFlickrGalleryNames);

			PTY_FLICKR_MAP.clear();
			for (int i = 0; i < ptyCodes.length; i++) {
				PTY_FLICKR_MAP.put(ptyCodes[i], ptyFlickrGalleryNames[i]);
			}

			SKY_FLICKR_MAP.clear();
			for (int i = 0; i < skyCodes.length; i++) {
				SKY_FLICKR_MAP.put(skyCodes[i], skyFlickrGalleryNames[i]);
			}
		}
	}

	public static int getWeatherSkyIconImg(String code, boolean night) {
		if (night) {
			if (code.equals("1")) {
				return R.drawable.night_clear;
			} else if (code.equals("3")) {
				return R.drawable.night_mostly_cloudy;
			} else {
				return WEATHER_SKY_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_SKY_ICON_ID_MAP.get(code);

	}

	public static String getWeatherSkyIconDescription(String code) {
		return WEATHER_SKY_ICON_DESCRIPTION_MAP.get(code);
	}

	public static int getWeatherPtyIconImg(String code, boolean night) {
		if (night) {
			if (code.equals("0")) {
				return R.drawable.night_clear;
			} else {
				WEATHER_PTY_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_PTY_ICON_ID_MAP.get(code);

	}

	public static int getWeatherSkyAndPtyIconImg(String pty, String sky, boolean night) {
		if (pty.equals("0")) {
			return getWeatherSkyIconImg(sky, night);
		} else {
			return WEATHER_PTY_ICON_ID_MAP.get(pty);
		}
	}

	public static String getWeatherMidIconDescription(String code) {
		return WEATHER_MID_ICON_DESCRIPTION_MAP.get(code);
	}

	public static int getWeatherMidIconImg(String code, boolean night) {
		if (night) {
			if (code.equals("맑음")) {
				return R.drawable.night_clear;
			} else if (code.equals("구름많음")) {
				return R.drawable.night_mostly_cloudy;
			} else {
				return WEATHER_MID_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_MID_ICON_ID_MAP.get(code);
	}

	public static String getWeatherPtyIconDescription(String code) {
		return WEATHER_PTY_ICON_DESCRIPTION_MAP.get(code);
	}

	public static String convertSkyPtyToMid(String sky, String pty) {
		if (pty.equals("0")) {
			switch (sky) {
				case "1":
					return "맑음";
				case "3":
					return "구름많음";
				default:
					return "흐림";
			}
		} else {
			switch (pty) {
				case "1":
				case "5":
					return "흐리고 비";
				case "2":
				case "6":
					return "흐리고 비/눈";
				case "3":
				case "7":
					return "흐리고 눈";
				default:
					return "흐리고 소나기";
			}
		}
	}

	public static String getPtyFlickrGalleryName(String code) {
		return PTY_FLICKR_MAP.get(code);
	}

	public static String getSkyFlickrGalleryName(String code) {
		return SKY_FLICKR_MAP.get(code);
	}

	public static FinalCurrentConditions getFinalCurrentConditions(VilageFcstResponse ultraSrtNcstResponse) {
		FinalCurrentConditions finalCurrentConditions = new FinalCurrentConditions();
		List<VilageFcstItem> items = ultraSrtNcstResponse.getBody().getItems().getItem();

		for (VilageFcstItem item : items) {
			if (item.getCategory().equals(T1H)) {
				finalCurrentConditions.setTemperature(item.getObsrValue());
			} else if (item.getCategory().equals(RN1)) {
				finalCurrentConditions.setPrecipitation1Hour(item.getObsrValue());
			} else if (item.getCategory().equals(REH)) {
				finalCurrentConditions.setHumidity(item.getObsrValue());
			} else if (item.getCategory().equals(PTY)) {
				finalCurrentConditions.setPrecipitationType(item.getObsrValue());
			} else if (item.getCategory().equals(VEC)) {
				finalCurrentConditions.setWindDirection(item.getObsrValue());
			} else if (item.getCategory().equals(WSD)) {
				finalCurrentConditions.setWindSpeed(item.getObsrValue());
			}
		}
		return finalCurrentConditions;
	}

	public static List<FinalHourlyForecast> getFinalHourlyForecastList(VilageFcstResponse ultraSrtFcstResponse, VilageFcstResponse vilageFcstResponse) {
		List<VilageFcstItem> ultraSrtFcstItemList = ultraSrtFcstResponse.getBody().getItems().getItem();
		List<VilageFcstItem> vilageItemList = vilageFcstResponse.getBody().getItems().getItem();
		Map<Long, List<VilageFcstItem>> hourDataListMap = new HashMap<>();

		long newDateTime = 0L;
		long lastDateTime = 0L;

		//데이터를 날짜별로 분류해서 map에 저장
		for (VilageFcstItem item : ultraSrtFcstItemList) {
			newDateTime = Long.parseLong(item.getFcstDate() + item.getFcstTime());
			if (newDateTime > lastDateTime) {
				hourDataListMap.put(newDateTime, new ArrayList<>());
				lastDateTime = newDateTime;
			}
			hourDataListMap.get(newDateTime).add(item);
		}

		final long lastDateTimeLongOfUltraSrtFcst = lastDateTime;

		for (VilageFcstItem item : vilageItemList) {
			newDateTime = Long.parseLong(item.getFcstDate() + item.getFcstTime());

			if (newDateTime > lastDateTimeLongOfUltraSrtFcst) {
				if (newDateTime > lastDateTime) {
					hourDataListMap.put(newDateTime, new ArrayList<>());
					lastDateTime = newDateTime;
				}
				hourDataListMap.get(newDateTime).add(item);
			}
		}

		List<FinalHourlyForecast> finalHourlyForecastList = new ArrayList<>();
		ZonedDateTime now = ZonedDateTime.now(KmaResponseProcessor.getZoneId()).withMinute(0).withSecond(0).withNano(0);
		String fcstDate = null;

		for (Map.Entry<Long, List<VilageFcstItem>> entry : hourDataListMap.entrySet()) {
			FinalHourlyForecast finalHourlyForecast = new FinalHourlyForecast();
			List<VilageFcstItem> hourlyFcstItems = entry.getValue();
			fcstDate = hourlyFcstItems.get(0).getFcstDate();

			now = now.withYear(Integer.parseInt(fcstDate.substring(0, 4))).withMonth(
					Integer.parseInt(fcstDate.substring(4, 6))).withDayOfMonth(Integer.parseInt(fcstDate.substring(6, 8))).withHour(
					Integer.parseInt(hourlyFcstItems.get(0).getFcstTime().substring(0, 2).substring(0, 2)));

			finalHourlyForecast.setFcstDateTime(ZonedDateTime.of(now.toLocalDateTime(), now.getZone()));

			for (VilageFcstItem item : hourlyFcstItems) {
				if (item.getCategory().equals(POP)) {
					finalHourlyForecast.setProbabilityOfPrecipitation(item.getFcstValue());
				} else if (item.getCategory().equals(PTY)) {
					finalHourlyForecast.setPrecipitationType(item.getFcstValue());
				} else if (item.getCategory().equals(PCP)) {
					finalHourlyForecast.setRainPrecipitation1Hour(item.getFcstValue());
				} else if (item.getCategory().equals(REH)) {
					finalHourlyForecast.setHumidity(item.getFcstValue());
				} else if (item.getCategory().equals(SNO)) {
					finalHourlyForecast.setSnowPrecipitation1Hour(item.getFcstValue());
				} else if (item.getCategory().equals(SKY)) {
					finalHourlyForecast.setSky(item.getFcstValue());
				} else if (item.getCategory().equals(TMP)) {
					finalHourlyForecast.setTemp1Hour(item.getFcstValue());
				} else if (item.getCategory().equals(TMN)) {
					finalHourlyForecast.setMinTemp(item.getFcstValue());
				} else if (item.getCategory().equals(TMX)) {
					finalHourlyForecast.setMaxTemp(item.getFcstValue());
				} else if (item.getCategory().equals(VEC)) {
					finalHourlyForecast.setWindDirection(item.getFcstValue());
				} else if (item.getCategory().equals(WSD)) {
					finalHourlyForecast.setWindSpeed(item.getFcstValue());
				} else if (item.getCategory().equals(T1H)) {
					finalHourlyForecast.setTemp1Hour(item.getFcstValue());
				} else if (item.getCategory().equals(RN1)) {
					finalHourlyForecast.setRainPrecipitation1Hour(item.getFcstValue());
				} else if (item.getCategory().equals(LGT)) {
					finalHourlyForecast.setLightning(item.getFcstValue());
				}
			}
			finalHourlyForecastList.add(finalHourlyForecast);
		}

		Collections.sort(finalHourlyForecastList, new Comparator<FinalHourlyForecast>() {
			@Override
			public int compare(FinalHourlyForecast t1, FinalHourlyForecast t2) {
				return t1.getFcstDateTime().compareTo(t2.getFcstDateTime());
			}
		});

		return finalHourlyForecastList;
	}

	public static List<FinalDailyForecast> getFinalDailyForecastList(MidLandFcstResponse midLandFcstResponse, MidTaResponse midTaFcstResponse, Long tmFc) {
		//중기예보 데이터 생성 3~10일후
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddhhmm");
		ZonedDateTime now = ZonedDateTime.now(KmaResponseProcessor.getZoneId());

		try {
			now = ZonedDateTime.parse(tmFc.toString(), dateTimeFormatter);
		} catch (Exception e) {

		}

		//3일 후로 이동
		now = now.plusDays(3).withHour(0).withMinute(0).withSecond(0).withNano(0);

		MidLandFcstItem midLandFcstData = midLandFcstResponse.getBody().getItems().getItem().get(0);
		MidTaItem midTaFcstData = midTaFcstResponse.getBody().getItems().getItem().get(0);
		List<FinalDailyForecast> finalDailyForecastList = new ArrayList<>();

		finalDailyForecastList.add(new FinalDailyForecast(now, midLandFcstData.getWf3Am(), midLandFcstData.getWf3Pm(),
				midLandFcstData.getRnSt3Am(), midLandFcstData.getRnSt3Pm(), midTaFcstData.getTaMin3(), midTaFcstData.getTaMax3()));

		now = now.plusDays(1);

		finalDailyForecastList.add(new FinalDailyForecast(now, midLandFcstData.getWf4Am(), midLandFcstData.getWf4Pm(),
				midLandFcstData.getRnSt4Am(), midLandFcstData.getRnSt4Pm(), midTaFcstData.getTaMin4(), midTaFcstData.getTaMax4()));

		now = now.plusDays(1);

		finalDailyForecastList.add(new FinalDailyForecast(now, midLandFcstData.getWf5Am(), midLandFcstData.getWf5Pm(),
				midLandFcstData.getRnSt5Am(), midLandFcstData.getRnSt5Pm(), midTaFcstData.getTaMin5(), midTaFcstData.getTaMax5()));

		now = now.plusDays(1);

		finalDailyForecastList.add(new FinalDailyForecast(now, midLandFcstData.getWf6Am(), midLandFcstData.getWf6Pm(),
				midLandFcstData.getRnSt6Am(), midLandFcstData.getRnSt6Pm(), midTaFcstData.getTaMin6(), midTaFcstData.getTaMax6()));

		now = now.plusDays(1);

		finalDailyForecastList.add(new FinalDailyForecast(now, midLandFcstData.getWf7Am(), midLandFcstData.getWf7Pm(),
				midLandFcstData.getRnSt7Am(), midLandFcstData.getRnSt7Pm(), midTaFcstData.getTaMin7(), midTaFcstData.getTaMax7()));

		now = now.plusDays(1);

		finalDailyForecastList.add(new FinalDailyForecast(now, midLandFcstData.getWf8(), midLandFcstData.getRnSt8(),
				midTaFcstData.getTaMin8(), midTaFcstData.getTaMax8()));

		now = now.plusDays(1);

		finalDailyForecastList.add(new FinalDailyForecast(now, midLandFcstData.getWf9(), midLandFcstData.getRnSt9(),
				midTaFcstData.getTaMin9(), midTaFcstData.getTaMax9()));

		now = now.plusDays(1);

		finalDailyForecastList.add(new FinalDailyForecast(now, midLandFcstData.getWf10(), midLandFcstData.getRnSt10(),
				midTaFcstData.getTaMin10(), midTaFcstData.getTaMax10()));

		return finalDailyForecastList;
	}

	public static List<FinalDailyForecast> getDailyForecastList(List<FinalDailyForecast> finalDailyForecasts,
	                                                            List<FinalHourlyForecast> finalHourlyForecasts) {
		final ZonedDateTime firstDateTimeOfDaily = ZonedDateTime.of(finalDailyForecasts.get(0).getDate().toLocalDateTime(),
				finalDailyForecasts.get(0).getDate().getZone());

		ZonedDateTime criteriaDateTime = ZonedDateTime.now(firstDateTimeOfDaily.getZone());
		criteriaDateTime = criteriaDateTime.withHour(23);
		criteriaDateTime = criteriaDateTime.withMinute(59);

		int beginIdx = 0;
		for (; beginIdx < finalHourlyForecasts.size(); beginIdx++) {
			if (criteriaDateTime.isBefore(finalHourlyForecasts.get(beginIdx).getFcstDateTime())) {
				break;
			}
		}
		int minTemp = Integer.MAX_VALUE;
		int maxTemp = Integer.MIN_VALUE;
		int hours = 0;
		String amSky = null;
		String pmSky = null;
		String amPop = null;
		String pmPop = null;
		ZonedDateTime dateTime = null;

		int temp = 0;

		for (; beginIdx < finalHourlyForecasts.size(); beginIdx++) {
			if (firstDateTimeOfDaily.getDayOfYear() == finalHourlyForecasts.get(beginIdx).getFcstDateTime().getDayOfYear()) {
				if (finalHourlyForecasts.get(beginIdx).getFcstDateTime().getHour() == 1) {
					break;
				}
			}

			hours = finalHourlyForecasts.get(beginIdx).getFcstDateTime().getHour();

			if (hours == 0 && minTemp != Integer.MAX_VALUE) {
				dateTime = ZonedDateTime.of(finalHourlyForecasts.get(beginIdx).getFcstDateTime().toLocalDateTime(),
						finalHourlyForecasts.get(beginIdx).getFcstDateTime().getZone());
				dateTime = dateTime.minusDays(1);
				FinalDailyForecast finalDailyForecast = new FinalDailyForecast(dateTime, amSky, pmSky, amPop, pmPop,
						String.valueOf(minTemp), String.valueOf(maxTemp));
				finalDailyForecasts.add(finalDailyForecast);

				minTemp = Integer.MAX_VALUE;
				maxTemp = Integer.MIN_VALUE;
			} else {
				temp = (int) Double.parseDouble(finalHourlyForecasts.get(beginIdx).getTemp1Hour());

				if (hours < 12) {
					minTemp = Math.min(minTemp, temp);
				} else {
					maxTemp = Math.max(maxTemp, temp);
				}

				if (hours == 9) {
					amSky = convertSkyPtyToMid(finalHourlyForecasts.get(beginIdx).getSky(),
							finalHourlyForecasts.get(beginIdx).getPrecipitationType());
					amPop = finalHourlyForecasts.get(beginIdx).getProbabilityOfPrecipitation();
				} else if (hours == 15) {
					pmSky = convertSkyPtyToMid(finalHourlyForecasts.get(beginIdx).getSky(),
							finalHourlyForecasts.get(beginIdx).getPrecipitationType());
					pmPop = finalHourlyForecasts.get(beginIdx).getProbabilityOfPrecipitation();
				}

			}
		}

		Collections.sort(finalDailyForecasts, new Comparator<FinalDailyForecast>() {
			@Override
			public int compare(FinalDailyForecast t1, FinalDailyForecast t2) {
				return t1.getDate().compareTo(t2.getDate());
			}
		});
		return finalDailyForecasts;
	}

	public static boolean successfulVilageResponse(Response<VilageFcstResponse> response) {
		if (response == null) {
			return false;
		}

		if (response.body() == null) {
			return false;
		} else {
			if (response.body().getKmaHeader() == null) {
				return false;
			}
			final String successfulCode = "00";

			if (response.body().getKmaHeader().getResultCode().equals(successfulCode)) {
				return true;
			} else {
				return false;
			}
		}

	}

	public static boolean successfulMidLandFcstResponse(Response<MidLandFcstResponse> response) {
		if (response == null) {
			return false;
		}
		if (response.body() == null) {
			return false;
		} else {
			if (response.body().getKmaHeader() == null) {
				return false;
			}

			final String successfulCode = "00";

			if (response.body().getKmaHeader().getResultCode().equals(successfulCode)) {
				return true;
			} else {
				return false;
			}
		}

	}

	public static boolean successfulMidTaFcstResponse(Response<MidTaResponse> response) {
		if (response == null) {
			return false;
		}
		if (response.body() == null) {
			return false;
		} else {
			if (response.body().getKmaHeader() == null) {
				return false;
			}
			final String successfulCode = "00";

			if (response.body().getKmaHeader().getResultCode().equals(successfulCode)) {
				return true;
			} else {
				return false;
			}
		}

	}

	public static UltraSrtNcstRoot getUltraSrtNcstObjFromJson(String response) {
		return new Gson().fromJson(response, UltraSrtNcstRoot.class);
	}

	public static UltraSrtFcstRoot getUltraSrtFcstObjFromJson(String response) {
		return new Gson().fromJson(response, UltraSrtFcstRoot.class);
	}

	public static VilageFcstRoot getVilageFcstObjFromJson(String response) {
		return new Gson().fromJson(response, VilageFcstRoot.class);
	}

	public static MidLandFcstRoot getMidLandObjFromJson(String response) {
		return new Gson().fromJson(response, MidLandFcstRoot.class);
	}

	public static MidTaRoot getMidTaObjFromJson(String response) {
		return new Gson().fromJson(response, MidTaRoot.class);
	}

	public static ZoneId getZoneId() {
		return ZoneId.of("Asia/Seoul");
	}
}
