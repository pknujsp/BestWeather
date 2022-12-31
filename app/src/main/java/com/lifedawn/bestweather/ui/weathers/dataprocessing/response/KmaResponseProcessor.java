package com.lifedawn.bestweather.ui.weathers.dataprocessing.response;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.html.KmaCurrentConditions;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.html.KmaDailyForecast;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.html.KmaHourlyForecast;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstItem;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midtaresponse.MidTaItem;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstItem;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.WeatherUtil;
import com.lifedawn.bestweather.ui.weathers.dataprocessing.util.WindUtil;
import com.lifedawn.bestweather.ui.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.ui.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.ui.weathers.models.HourlyForecastDto;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.tickaroo.tikxml.TikXml;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okio.Buffer;
import retrofit2.Response;

public class KmaResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_MID_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> WEATHER_WEB_ICON_DESCRIPTION_MAP = new HashMap<>();

	private static final Map<String, Integer> WEATHER_MID_ICON_ID_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_WEB_ICON_ID_MAP = new HashMap<>();

	private static final Map<String, String> PTY_FLICKR_MAP = new HashMap<>();
	private static final Map<String, String> SKY_FLICKR_MAP = new HashMap<>();

	private static final Map<String, String> HOURLY_TO_DAILY_DESCRIPTION_MAP = new HashMap<>();

	private static final String POP = "POP";
	private static final String PTY = "PTY";
	private static final String PCP = "PCP";
	private static final String REH = "REH";
	private static final String SNO = "SNO";
	private static final String SKY = "SKY";
	private static final String TMP = "TMP";
	private static final String TMN = "TMN";
	private static final String TMX = "TMX";
	private static final String VEC = "VEC";
	private static final String WSD = "WSD";
	private static final String T1H = "T1H";
	private static final String RN1 = "RN1";
	private static final String LGT = "LGT";

	private KmaResponseProcessor() {
	}

	public static void init(Context context) {
		if (WEATHER_MID_ICON_DESCRIPTION_MAP.isEmpty()
				|| WEATHER_MID_ICON_ID_MAP.isEmpty() || PTY_FLICKR_MAP.isEmpty() ||
				SKY_FLICKR_MAP.isEmpty() || HOURLY_TO_DAILY_DESCRIPTION_MAP.isEmpty()) {

			String[] midCodes = context.getResources().getStringArray(R.array.KmaMidIconCodes);
			String[] webIconCodes = context.getResources().getStringArray(R.array.KmaWeatherDescriptionCodes);
			String[] midDescriptions = context.getResources().getStringArray(R.array.KmaMidIconDescriptionsForCode);
			String[] webIconDescriptions = context.getResources().getStringArray(R.array.KmaWeatherDescriptions);

			TypedArray midIconIds = context.getResources().obtainTypedArray(R.array.KmaMidWeatherIconForCode);
			TypedArray webIconIds = context.getResources().obtainTypedArray(R.array.KmaWeatherIconForDescriptionCode);

			WEATHER_MID_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < midCodes.length; i++) {
				WEATHER_MID_ICON_DESCRIPTION_MAP.put(midCodes[i], midDescriptions[i]);
				WEATHER_MID_ICON_ID_MAP.put(midCodes[i], midIconIds.getResourceId(i, R.drawable.temp_icon));
			}

			WEATHER_WEB_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < webIconCodes.length; i++) {
				WEATHER_WEB_ICON_DESCRIPTION_MAP.put(webIconCodes[i], webIconDescriptions[i]);
				WEATHER_WEB_ICON_ID_MAP.put(webIconCodes[i], webIconIds.getResourceId(i, R.drawable.temp_icon));
			}

			String[] ptyFlickrGalleryNames = context.getResources().getStringArray(R.array.KmaPtyFlickrGalleryNames);
			String[] skyFlickrGalleryNames = context.getResources().getStringArray(R.array.KmaSkyFlickrGalleryNames);

			String[] skyCodes = context.getResources().getStringArray(R.array.KmaSkyIconCodes);
			String[] ptyCodes = context.getResources().getStringArray(R.array.KmaPtyIconCodes);

			PTY_FLICKR_MAP.clear();
			for (int i = 0; i < ptyCodes.length; i++) {
				PTY_FLICKR_MAP.put(ptyCodes[i], ptyFlickrGalleryNames[i]);
			}

			SKY_FLICKR_MAP.clear();
			for (int i = 0; i < skyCodes.length; i++) {
				SKY_FLICKR_MAP.put(skyCodes[i], skyFlickrGalleryNames[i]);
			}

			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("비", "흐리고 비");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("비/눈", "흐리고 비/눈");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("눈", "흐리고 눈");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("빗방울", "흐리고 비");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("빗방울/눈날림", "흐리고 비/눈");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("눈날림", "흐리고 눈");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("구름 많음", "구름많음");
		}
	}

		/*
		sky
		<item>맑음</item>
        <item>구름 많음</item>
        <item>흐림</item>

        pty
        <item>비</item>
        <item>비/눈</item>
        <item>눈</item>
        <item>소나기</item>
        <item>빗방울</item>
        <item>빗방울/눈날림</item>
        <item>눈날림</item>
		 */

	public static String convertSkyTextToCode(String text) {
		switch (text) {
			case "맑음":
				return "1";
			case "구름 많음":
				return "3";
			case "흐림":
				return "4";
			default:
				return null;
		}
	}

	public static String convertPtyTextToCode(String text) {
		switch (text) {
			case "없음":
				return "0";
			case "비":
				return "1";
			case "비/눈":
				return "2";
			case "눈":
				return "3";
			case "소나기":
				return "4";
			case "빗방울":
				return "5";
			case "빗방울/눈날림":
				return "6";
			case "눈날림":
				return "7";
			default:
				return null;
		}
	}

	public static int getWeatherSkyIconImg(String code, boolean night) {
		/*
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
		*/
		return 1;

	}

	public static String getWeatherSkyIconDescription(String code) {
		//return WEATHER_SKY_ICON_DESCRIPTION_MAP.get(code);
		return null;
	}

	public static int getWeatherPtyIconImg(String code, boolean night) {
		/*
		if (night) {
			if (code.equals("0")) {
				return R.drawable.night_clear;
			} else {
				WEATHER_PTY_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_PTY_ICON_ID_MAP.get(code);

		 */
		return 1;

	}

	public static int getWeatherSkyAndPtyIconImg(String pty, String sky, boolean night) {
		/*
		if (pty.equals("0")) {
			return getWeatherSkyIconImg(sky, night);
		} else {
			return WEATHER_PTY_ICON_ID_MAP.get(pty);
		}

		 */
		return 1;
	}

	public static String getWeatherDescription(String pty, String sky) {
		if (pty.equals("0")) {
			return getWeatherSkyIconDescription(sky);
		} else {
			return getWeatherPtyIconDescription(pty);
		}
	}

	public static String getWeatherDescriptionWeb(String weatherDescriptionKr) {
		return WEATHER_WEB_ICON_DESCRIPTION_MAP.get(weatherDescriptionKr);
	}

	public static int getWeatherIconImgWeb(String weatherDescriptionKr, boolean night) {
		if (night) {
			if (weatherDescriptionKr.equals("맑음")) {
				return R.drawable.night_clear;
			} else if (weatherDescriptionKr.equals("구름 많음")) {
				return R.drawable.night_mostly_cloudy;
			} else {
				return WEATHER_WEB_ICON_ID_MAP.get(weatherDescriptionKr);
			}
		} else {
			return WEATHER_WEB_ICON_ID_MAP.get(weatherDescriptionKr);
		}
	}

	public static int getWeatherIconImgWeb(String weatherDescriptionKr, boolean night, boolean thunder) {
		if (thunder) {
			return R.drawable.thunderstorm;
		} else {
			return getWeatherIconImgWeb(weatherDescriptionKr, night);
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
		//return WEATHER_PTY_ICON_DESCRIPTION_MAP.get(code);
		return null;
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

	public static String convertHourlyWeatherDescriptionToMid(String description) {
		/*
		hourly -
		<item>맑음</item>
        <item>구름 많음</item>
        <item>흐림</item>

        <item>비</item>
        <item>비/눈</item>
        <item>눈</item>
        <item>소나기</item>
        <item>빗방울</item>
        <item>빗방울/눈날림</item>
        <item>눈날림</item>

		mid -
		<item>맑음</item>
        <item>구름많음</item>
        <item>구름많고 비</item>
        <item>구름많고 눈</item>
        <item>구름많고 비/눈</item>
        <item>구름많고 소나기</item>
        <item>흐림</item>
        <item>흐리고 비</item>
        <item>흐리고 눈</item>
        <item>흐리고 비/눈</item>
        <item>흐리고 소나기</item>
        <item>소나기</item>
		 */

		if (HOURLY_TO_DAILY_DESCRIPTION_MAP.containsKey(description)) {
			return HOURLY_TO_DAILY_DESCRIPTION_MAP.get(description);
		} else {
			return description;
		}


	}

	public static String getPtyFlickrGalleryName(String code) {
		return PTY_FLICKR_MAP.get(code);
	}

	public static String getSkyFlickrGalleryName(String code) {
		return SKY_FLICKR_MAP.get(code);
	}

	public static FinalCurrentConditions getFinalCurrentConditionsByXML(VilageFcstResponse ultraSrtNcstResponse) {
		FinalCurrentConditions finalCurrentConditions = new FinalCurrentConditions();
		List<VilageFcstItem> items = ultraSrtNcstResponse.getBody().getItems().getItem();

		finalCurrentConditions.setBaseDateTime(items.get(0).getBaseDate() + items.get(0).getBaseTime());

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

	public static List<FinalHourlyForecast> getFinalHourlyForecastListByXML(VilageFcstResponse ultraSrtFcstResponse,
	                                                                        @Nullable VilageFcstResponse vilageFcstResponse) {
		List<VilageFcstItem> ultraSrtFcstItemList = ultraSrtFcstResponse.getBody().getItems().getItem();
		List<VilageFcstItem> vilageItemList = null;
		if (vilageFcstResponse != null) {
			vilageItemList = vilageFcstResponse.getBody().getItems().getItem();
		}
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

		if (vilageFcstResponse != null) {
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

	public static List<FinalDailyForecast> getFinalDailyForecastListByXML(MidLandFcstResponse midLandFcstResponse, MidTaResponse midTaFcstResponse, Long tmFc) {
		//중기예보 데이터 생성 3~10일후
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddhhmm");

		//tmfc : 202112180600
		final String tmFcStr = tmFc.toString();
		int year = Integer.parseInt(tmFcStr.substring(0, 4));
		int month = Integer.parseInt(tmFcStr.substring(4, 6));
		int dayOfMonth = Integer.parseInt(tmFcStr.substring(6, 8));
		int hour = Integer.parseInt(tmFcStr.substring(8, 10));
		int minute = Integer.parseInt(tmFcStr.substring(10, 12));
		ZonedDateTime now = ZonedDateTime.now(KmaResponseProcessor.getZoneId());
		now = now.withYear(year).withMonth(month).withDayOfMonth(dayOfMonth).withHour(hour).withMinute(minute);

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

	public static List<FinalDailyForecast> getDailyForecastListByXML(List<FinalDailyForecast> finalDailyForecasts,
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

	public static VilageFcstResponse successfulVilageResponse(Response<String> response) {
		if (response == null) {
			return null;
		}

		if (response.body() == null) {
			return null;
		} else {
			try {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				VilageFcstResponse vilageFcstResponse = tikXml.read(new Buffer().writeUtf8(response.body()), VilageFcstResponse.class);
				if (vilageFcstResponse.getKmaHeader() == null) {
					return null;
				}

				if (vilageFcstResponse.getKmaHeader().getResultCode().equals("00")) {
					return vilageFcstResponse;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}


		}

	}

	public static MidLandFcstResponse successfulMidLandFcstResponse(Response<String> response) {
		if (response == null) {
			return null;
		}
		if (response.body() == null) {
			return null;
		} else {
			try {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				MidLandFcstResponse midLandFcstResponse = tikXml.read(new Buffer().writeUtf8(response.body()), MidLandFcstResponse.class);
				if (midLandFcstResponse.getKmaHeader() == null) {
					return null;
				}

				if (midLandFcstResponse.getKmaHeader().getResultCode().equals("00")) {
					return midLandFcstResponse;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}


		}

	}

	public static MidTaResponse successfulMidTaFcstResponse(Response<String> response) {
		if (response == null) {
			return null;
		}
		if (response.body() == null) {
			return null;
		} else {
			try {
				TikXml tikXml = new TikXml.Builder().exceptionOnUnreadXml(false).build();
				MidTaResponse midTaResponse = tikXml.read(new Buffer().writeUtf8(response.body()), MidTaResponse.class);
				if (midTaResponse.getKmaHeader() == null) {
					return null;
				}

				if (midTaResponse.getKmaHeader().getResultCode().equals("00")) {
					return midTaResponse;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

	}

	public static List<HourlyForecastDto> makeHourlyForecastDtoListOfXML(Context context,
	                                                                     List<FinalHourlyForecast> hourlyForecastList, double latitude, double longitude) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();

		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String percent = "%";
		final String noSnow = "적설없음";
		final String noRain = "강수없음";

		final String zeroRainVolume = "0.0mm";
		final String zeroSnowVolume = "0.0cm";

		ZoneId zoneId = ZoneId.of("Asia/Seoul");

		final Map<Integer, SunRiseSetUtil.SunRiseSetObj> sunSetRiseDataMap = SunRiseSetUtil.getDailySunRiseSetMap(
				ZonedDateTime.of(hourlyForecastList.get(0).getFcstDateTime().toLocalDateTime(), zoneId),
				ZonedDateTime.of(hourlyForecastList.get(hourlyForecastList.size() - 1).getFcstDateTime().toLocalDateTime(),
						zoneId), latitude, longitude);

		boolean isNight = false;
		Calendar itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
		Calendar sunRise = null;
		Calendar sunSet = null;

		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		String snowVolume;
		String rainVolume;
		boolean hasRain;
		boolean hasSnow;

		for (FinalHourlyForecast finalHourlyForecast : hourlyForecastList) {
			HourlyForecastDto hourlyForecastDto = new HourlyForecastDto();

			if (finalHourlyForecast.getRainPrecipitation1Hour().equals(noRain)) {
				hasRain = false;
				rainVolume = zeroRainVolume;
			} else {
				hasRain = true;
				rainVolume = finalHourlyForecast.getRainPrecipitation1Hour();
			}

			if (finalHourlyForecast.getSnowPrecipitation1Hour() == null ||
					finalHourlyForecast.getSnowPrecipitation1Hour().equals(noSnow)) {
				hasSnow = false;
				snowVolume = zeroSnowVolume;
			} else {
				hasSnow = true;
				snowVolume = finalHourlyForecast.getSnowPrecipitation1Hour();
			}

			itemCalendar.setTimeInMillis(finalHourlyForecast.getFcstDateTime().toInstant().toEpochMilli());
			sunRise = sunSetRiseDataMap.get(finalHourlyForecast.getFcstDateTime().getDayOfYear()).getSunrise();
			sunSet = sunSetRiseDataMap.get(finalHourlyForecast.getFcstDateTime().getDayOfYear()).getSunset();
			isNight = SunRiseSetUtil.isNight(itemCalendar, sunRise, sunSet);

			hourlyForecastDto.setHours(finalHourlyForecast.getFcstDateTime())
					.setTemp(ValueUnits.convertTemperature(finalHourlyForecast.getTemp1Hour(), tempUnit) + tempDegree)
					.setRainVolume(rainVolume)
					.setHasRain(hasRain)
					.setHasSnow(hasSnow)
					.setSnowVolume(snowVolume)
					.setWeatherIcon(KmaResponseProcessor.getWeatherSkyAndPtyIconImg(finalHourlyForecast.getPrecipitationType(),
							finalHourlyForecast.getSky(), isNight))
					.setWeatherDescription(KmaResponseProcessor.getWeatherDescription(finalHourlyForecast.getPrecipitationType(),
							finalHourlyForecast.getSky()))
					.setWindDirectionVal(Integer.parseInt(finalHourlyForecast.getWindDirection()))
					.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, finalHourlyForecast.getWindDirection()))
					.setWindStrength(WindUtil.getSimpleWindSpeedDescription(finalHourlyForecast.getWindSpeed()))
					.setWindSpeed(ValueUnits.convertWindSpeed(finalHourlyForecast.getWindSpeed(), windUnit) + MyApplication.VALUE_UNIT_OBJ.getWindUnitText())
					.setHumidity(finalHourlyForecast.getHumidity() + percent);

			if (finalHourlyForecast.getProbabilityOfPrecipitation() != null) {
				hourlyForecastDto.setPop(finalHourlyForecast.getProbabilityOfPrecipitation() + percent);
			} else {
				hourlyForecastDto.setPop("-");
			}

			hourlyForecastDtoList.add(hourlyForecastDto);
		}
		return hourlyForecastDtoList;
	}

	public static List<HourlyForecastDto> makeHourlyForecastDtoListOfWEB(Context context,
	                                                                     List<KmaHourlyForecast> hourlyForecastList, double latitude, double longitude) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String mPerSec = "m/s";

		final String zeroRainVolume = "0.0mm";
		final String zeroSnowVolume = "0.0cm";
		final String percent = "%";
		final ZoneId zoneId = ZoneId.of("Asia/Seoul");

		final Map<Integer, SunRiseSetUtil.SunRiseSetObj> sunSetRiseDataMap = SunRiseSetUtil.getDailySunRiseSetMap(
				ZonedDateTime.of(hourlyForecastList.get(0).getHour().toLocalDateTime(), zoneId),
				ZonedDateTime.of(hourlyForecastList.get(hourlyForecastList.size() - 1).getHour().toLocalDateTime(),
						zoneId), latitude, longitude);

		boolean isNight = false;
		Calendar itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
		Calendar sunRise = null;
		Calendar sunSet = null;

		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		String snowVolume;
		String rainVolume;
		boolean hasRain;
		boolean hasSnow;
		boolean hasThunder;
		String windSpeed = null;
		int humidity = 0;
		Double feelsLikeTemp = 0.0;
		String windDirectionStr = null;
		Integer windDirectionInt = 0;
		final String poong = "풍";

		for (KmaHourlyForecast finalHourlyForecast : hourlyForecastList) {
			HourlyForecastDto hourlyForecastDto = new HourlyForecastDto();
			hasRain = finalHourlyForecast.isHasRain();

			if (!hasRain) {
				rainVolume = zeroRainVolume;
			} else {
				rainVolume = finalHourlyForecast.getRainVolume();
			}

			hasSnow = finalHourlyForecast.isHasSnow();

			if (!hasSnow) {
				snowVolume = zeroSnowVolume;
			} else {
				snowVolume = finalHourlyForecast.getSnowVolume();
			}

			hasThunder = finalHourlyForecast.isHasThunder();

			itemCalendar.setTimeInMillis(finalHourlyForecast.getHour().toInstant().toEpochMilli());
			sunRise = sunSetRiseDataMap.get(finalHourlyForecast.getHour().getDayOfYear()).getSunrise();
			sunSet = sunSetRiseDataMap.get(finalHourlyForecast.getHour().getDayOfYear()).getSunset();
			isNight = SunRiseSetUtil.isNight(itemCalendar, sunRise, sunSet);

			humidity = Integer.parseInt(finalHourlyForecast.getHumidity().replace(percent, ""));

			hourlyForecastDto.setHours(finalHourlyForecast.getHour())
					.setTemp(ValueUnits.convertTemperature(finalHourlyForecast.getTemp(), tempUnit) + tempDegree)
					.setRainVolume(rainVolume)
					.setHasRain(hasRain)
					.setHasSnow(hasSnow)
					.setSnowVolume(snowVolume)
					.setHasThunder(hasThunder)
					.setWeatherIcon(getWeatherIconImgWeb(finalHourlyForecast.getWeatherDescription(),
							isNight, hasThunder))
					.setWeatherDescription(getWeatherDescriptionWeb(finalHourlyForecast.getWeatherDescription()))
					.setHumidity(finalHourlyForecast.getHumidity()).setPop(!finalHourlyForecast.getPop().contains("%") ?
							"-" : finalHourlyForecast.getPop());

			if (finalHourlyForecast.getWindDirection() != null) {
				windSpeed = finalHourlyForecast.getWindSpeed().replace(mPerSec, "");
				windDirectionStr = finalHourlyForecast.getWindDirection().replace(poong, "");
				windDirectionInt = WindUtil.parseWindDirectionStrAsInt(windDirectionStr);

				hourlyForecastDto.setWindDirectionVal(windDirectionInt)
						.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, windDirectionInt.toString()))
						.setWindStrength(WindUtil.getSimpleWindSpeedDescription(windSpeed))
						.setWindSpeed(ValueUnits.convertWindSpeed(windSpeed, windUnit)
								+ MyApplication.VALUE_UNIT_OBJ.getWindUnitText());

				feelsLikeTemp = WeatherUtil.calcFeelsLikeTemperature(Double.parseDouble(finalHourlyForecast.getTemp()),
						ValueUnits.convertWindSpeed(windSpeed, ValueUnits.kmPerHour), humidity);

				hourlyForecastDto.setFeelsLikeTemp(ValueUnits.convertTemperature(feelsLikeTemp.toString(),
						tempUnit) + tempDegree);
			}

			hourlyForecastDtoList.add(hourlyForecastDto);
		}
		return hourlyForecastDtoList;
	}

	public static List<DailyForecastDto> makeDailyForecastDtoListOfXML(List<FinalDailyForecast> dailyForecastList) {
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();

		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();

		for (FinalDailyForecast finalDailyForecast : dailyForecastList) {
			DailyForecastDto dailyForecastDto = new DailyForecastDto();

			dailyForecastDto.setDate(finalDailyForecast.getDate())
					.setMinTemp(ValueUnits.convertTemperature(finalDailyForecast.getMinTemp(), tempUnit) + tempDegree)
					.setMaxTemp(ValueUnits.convertTemperature(finalDailyForecast.getMaxTemp(), tempUnit) + tempDegree);

			DailyForecastDto.Values single = null;
			DailyForecastDto.Values am = null;
			DailyForecastDto.Values pm = null;

			if (finalDailyForecast.isSingle()) {
				single = new DailyForecastDto.Values();
				single.setPop(finalDailyForecast.getProbabilityOfPrecipitation())
						.setWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getSky(), false))
						.setWeatherDescription(finalDailyForecast.getSky());

				dailyForecastDto.getValuesList().add(single);
			} else {
				am = new DailyForecastDto.Values();
				pm = new DailyForecastDto.Values();

				am.setPop(finalDailyForecast.getAmProbabilityOfPrecipitation())
						.setWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getAmSky(), false))
						.setWeatherDescription(finalDailyForecast.getAmSky());
				pm.setPop(finalDailyForecast.getPmProbabilityOfPrecipitation())
						.setWeatherIcon(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getPmSky(), false))
						.setWeatherDescription(finalDailyForecast.getPmSky());

				dailyForecastDto.getValuesList().add(am);
				dailyForecastDto.getValuesList().add(pm);
			}

			dailyForecastDtoList.add(dailyForecastDto);
		}
		return dailyForecastDtoList;
	}

	public static List<DailyForecastDto> makeDailyForecastDtoListOfWEB(List<KmaDailyForecast> dailyForecastList) {
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();

		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();

		for (KmaDailyForecast finalDailyForecast : dailyForecastList) {
			DailyForecastDto dailyForecastDto = new DailyForecastDto();
			dailyForecastDtoList.add(dailyForecastDto);

			dailyForecastDto.setDate(finalDailyForecast.getDate())
					.setMinTemp(ValueUnits.convertTemperature(finalDailyForecast.getMinTemp(), tempUnit) + tempDegree)
					.setMaxTemp(ValueUnits.convertTemperature(finalDailyForecast.getMaxTemp(), tempUnit) + tempDegree);

			if (finalDailyForecast.isSingle()) {
				DailyForecastDto.Values single = new DailyForecastDto.Values();

				single.setPop(finalDailyForecast.getSingleValues().getPop())
						.setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.getSingleValues().getWeatherDescription(), false))
						.setWeatherDescription(getWeatherMidIconDescription(finalDailyForecast.getSingleValues().getWeatherDescription()));
				dailyForecastDto.getValuesList().add(single);
			} else {
				DailyForecastDto.Values am = new DailyForecastDto.Values();
				DailyForecastDto.Values pm = new DailyForecastDto.Values();
				dailyForecastDto.getValuesList().add(am);
				dailyForecastDto.getValuesList().add(pm);

				am.setPop(finalDailyForecast.getAmValues().getPop())
						.setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.getAmValues().getWeatherDescription(), false))
						.setWeatherDescription(getWeatherMidIconDescription(finalDailyForecast.getAmValues().getWeatherDescription()));
				pm.setPop(finalDailyForecast.getPmValues().getPop())
						.setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.getPmValues().getWeatherDescription(), false))
						.setWeatherDescription(getWeatherMidIconDescription(finalDailyForecast.getPmValues().getWeatherDescription()));
			}

		}
		return dailyForecastDtoList;
	}

	public static CurrentConditionsDto makeCurrentConditionsDtoOfXML(Context context,
	                                                                 FinalCurrentConditions item,
	                                                                 FinalHourlyForecast finalHourlyForecast, Double latitude,
	                                                                 Double longitude) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();

		final String tempUnitStr = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String percent = "%";
		final TimeZone koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul");
		final ZoneId koreaZoneId = ZoneId.of("Asia/Seoul");

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
		LocalDateTime localDateTime = LocalDateTime.parse(item.getBaseDateTime(), dateTimeFormatter);
		ZonedDateTime currentTime = ZonedDateTime.of(localDateTime, koreaZoneId);

		CurrentConditionsDto currentConditionsDto = new CurrentConditionsDto();

		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(new Location(latitude, longitude),
				koreaTimeZone);
		Calendar calendar = Calendar.getInstance(koreaTimeZone);
		calendar.set(currentTime.getYear(), currentTime.getMonthValue() - 1, currentTime.getDayOfMonth(),
				currentTime.getHour(), currentTime.getMinute());
		Calendar sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar);
		Calendar sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar);


		currentConditionsDto.setCurrentTime(currentTime);
		currentConditionsDto.setWeatherDescription(KmaResponseProcessor.getWeatherDescription(item.getPrecipitationType(),
				finalHourlyForecast.getSky()));
		currentConditionsDto.setWeatherIcon(KmaResponseProcessor.getWeatherSkyAndPtyIconImg(item.getPrecipitationType(),
				finalHourlyForecast.getSky(),
				SunRiseSetUtil.isNight(calendar, sunRise, sunSet)));
		currentConditionsDto.setTemp(ValueUnits.convertTemperature(item.getTemperature(), tempUnit) + tempUnitStr);

		double feelsLikeTemp =
				WeatherUtil.calcFeelsLikeTemperature(ValueUnits.convertTemperature(item.getTemperature(), ValueUnits.celsius),
						ValueUnits.convertWindSpeed(item.getWindSpeed(), ValueUnits.kmPerHour),
						Double.parseDouble(item.getHumidity()));

		currentConditionsDto.setFeelsLikeTemp(ValueUnits.convertTemperature(String.valueOf((int) feelsLikeTemp), tempUnit) + tempUnitStr);

		currentConditionsDto.setHumidity(item.getHumidity() + percent);
		currentConditionsDto.setWindDirectionDegree(Integer.parseInt(item.getWindDirection()));
		currentConditionsDto.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, item.getWindDirection()));
		currentConditionsDto.setWindSpeed(ValueUnits.convertWindSpeed(item.getWindSpeed(), windUnit) + MyApplication.VALUE_UNIT_OBJ.getWindUnitText());

		currentConditionsDto.setSimpleWindStrength(WindUtil.getSimpleWindSpeedDescription(item.getWindSpeed()));
		currentConditionsDto.setWindStrength(WindUtil.getWindSpeedDescription(item.getWindSpeed()));
		currentConditionsDto.setPrecipitationType(getWeatherPtyIconDescription(item.getPrecipitationType()));

		if (!item.getPrecipitation1Hour().equals("0")) {
			currentConditionsDto.setPrecipitationVolume(item.getPrecipitation1Hour() + "mm");
		}

		return currentConditionsDto;
	}

	public static CurrentConditionsDto makeCurrentConditionsDtoOfWEB(Context context,
	                                                                 KmaCurrentConditions kmaCurrentConditions,
	                                                                 KmaHourlyForecast kmaHourlyForecast,
	                                                                 Double latitude,
	                                                                 Double longitude) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempUnitStr = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();

		CurrentConditionsDto currentConditionsDto = new CurrentConditionsDto();
		ZonedDateTime currentTime = ZonedDateTime.parse(kmaCurrentConditions.getBaseDateTime());

		String currentPtyCode = kmaCurrentConditions.getPty();
		String hourlyForecastDescription = kmaHourlyForecast.getWeatherDescription();

		final TimeZone koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul");
		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(new Location(latitude, longitude),
				koreaTimeZone);
		Calendar calendar = Calendar.getInstance(koreaTimeZone);
		calendar.set(currentTime.getYear(), currentTime.getMonthValue() - 1, currentTime.getDayOfMonth(),
				currentTime.getHour(), currentTime.getMinute());
		Calendar sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar);
		Calendar sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar);

		currentConditionsDto.setCurrentTime(currentTime);
		currentConditionsDto.setWeatherDescription(getWeatherDescriptionWeb(currentPtyCode.isEmpty() ? hourlyForecastDescription : currentPtyCode));
		currentConditionsDto.setWeatherIcon(getWeatherIconImgWeb(currentPtyCode.isEmpty() ? hourlyForecastDescription : currentPtyCode,
				SunRiseSetUtil.isNight(calendar, sunRise, sunSet)));
		currentConditionsDto.setTemp(ValueUnits.convertTemperature(kmaCurrentConditions.getTemp(), tempUnit) + tempUnitStr);
		currentConditionsDto.setFeelsLikeTemp(ValueUnits.convertTemperature(kmaCurrentConditions.getFeelsLikeTemp(), tempUnit) + tempUnitStr);

		currentConditionsDto.setHumidity(kmaCurrentConditions.getHumidity());
		currentConditionsDto.setYesterdayTemp(kmaCurrentConditions.getYesterdayTemp());

		if (kmaCurrentConditions.getWindDirection() != null) {
			Integer windDirectionDegree = WindUtil.parseWindDirectionStrAsInt(kmaCurrentConditions.getWindDirection());
			currentConditionsDto.setWindDirectionDegree(windDirectionDegree);
			currentConditionsDto.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, windDirectionDegree.toString()));
		}
		if (kmaCurrentConditions.getWindSpeed() != null) {
			Double windSpeed = Double.parseDouble(kmaCurrentConditions.getWindSpeed());
			currentConditionsDto.setWindSpeed(ValueUnits.convertWindSpeed(windSpeed.toString(), windUnit) + MyApplication.VALUE_UNIT_OBJ.getWindUnitText());

			currentConditionsDto.setSimpleWindStrength(WindUtil.getSimpleWindSpeedDescription(windSpeed.toString()));
			currentConditionsDto.setWindStrength(WindUtil.getWindSpeedDescription(windSpeed.toString()));
		}

		if (currentPtyCode.isEmpty()) {
			currentPtyCode = "0";
		} else {
			currentPtyCode = convertPtyTextToCode(currentPtyCode);
		}
		currentConditionsDto.setPrecipitationType(getWeatherPtyIconDescription(currentPtyCode));

		if (!kmaCurrentConditions.getPrecipitationVolume().contains("-") && !kmaCurrentConditions.getPrecipitationVolume().contains("0.0")) {
			currentConditionsDto.setPrecipitationVolume(kmaCurrentConditions.getPrecipitationVolume());
		}

		return currentConditionsDto;
	}

	public static ZoneId getZoneId() {
		return ZoneId.of("Asia/Seoul");
	}
}