package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ClockUtil;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstItem;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstItems;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaItem;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaItems;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse.UltraSrtFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse.UltraSrtNcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons.VilageFcstItem;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons.VilageFcstItems;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Response;

public class KmaResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_SKY_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> WEATHER_PTY_ICON_DESCRIPTION_MAP = new HashMap<>();
	
	private static final Map<String, Integer> WEATHER_SKY_ICON_ID_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_PTY_ICON_ID_MAP = new HashMap<>();
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
		String[] skyCodes = context.getResources().getStringArray(R.array.KmaSkyIconCodes);
		String[] ptyCodes = context.getResources().getStringArray(R.array.KmaPtyIconCodes);
		String[] skyDescriptions = context.getResources().getStringArray(R.array.KmaSkyIconDescriptionsForCode);
		String[] ptyDescriptions = context.getResources().getStringArray(R.array.KmaPtyIconDescriptionsForCode);
		int[] ptyIconIds = context.getResources().getIntArray(R.array.KmaPtyWeatherIconForCode);
		int[] skyIconIds = context.getResources().getIntArray(R.array.KmaSkyWeatherIconForCode);
		
		WEATHER_SKY_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < skyCodes.length; i++) {
			WEATHER_SKY_ICON_DESCRIPTION_MAP.put(skyCodes[i], skyDescriptions[i]);
			WEATHER_SKY_ICON_ID_MAP.put(skyCodes[i], skyIconIds[i]);
		}
		
		WEATHER_PTY_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < ptyCodes.length; i++) {
			WEATHER_PTY_ICON_DESCRIPTION_MAP.put(ptyCodes[i], ptyDescriptions[i]);
			WEATHER_PTY_ICON_ID_MAP.put(ptyCodes[i], ptyIconIds[i]);
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
	
	public static String getWeatherPtyIconDescription(String code) {
		return WEATHER_PTY_ICON_DESCRIPTION_MAP.get(code);
	}
	
	public static String getPtyFlickrGalleryName(String code) {
		return PTY_FLICKR_MAP.get(code);
	}
	
	public static String getSkyFlickrGalleryName(String code) {
		return SKY_FLICKR_MAP.get(code);
	}
	
	public static FinalCurrentConditions getFinalCurrentConditions(UltraSrtNcstRoot ultraSrtNcstRoot) {
		FinalCurrentConditions finalCurrentConditions = new FinalCurrentConditions();
		List<VilageFcstItem> items = ultraSrtNcstRoot.getResponse().getBody().getItems().getItem();
		
		for (VilageFcstItem item : items) {
			if (item.getCategory().equals(T1H)) {
				finalCurrentConditions.setTemperature(item.getFcstValue());
			} else if (item.getCategory().equals(RN1)) {
				finalCurrentConditions.setPrecipitation1Hour(item.getFcstValue());
			} else if (item.getCategory().equals(REH)) {
				finalCurrentConditions.setHumidity(item.getFcstValue());
			} else if (item.getCategory().equals(PTY)) {
				finalCurrentConditions.setPrecipitationType(item.getFcstValue());
			} else if (item.getCategory().equals(VEC)) {
				finalCurrentConditions.setWindDirection(item.getFcstValue());
			} else if (item.getCategory().equals(WSD)) {
				finalCurrentConditions.setWindSpeed(item.getFcstValue());
			}
		}
		return finalCurrentConditions;
	}
	
	public static List<FinalHourlyForecast> getFinalHourlyForecastList(UltraSrtFcstRoot ultraSrtFcstRoot, VilageFcstRoot vilageFcstRoot) {
		List<VilageFcstItem> ultraSrtFcstItemList = ultraSrtFcstRoot.getResponse().getBody().getItems().getItem();
		List<VilageFcstItem> vilageItemList = vilageFcstRoot.getResponse().getBody().getItems().getItem();
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
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(TimeZone.getTimeZone("Asia/Seoul").getID())).withMinute(0).withSecond(0).withNano(
				0);
		String fcstDate = null;
		
		for (Map.Entry<Long, List<VilageFcstItem>> entry : hourDataListMap.entrySet()) {
			FinalHourlyForecast finalHourlyForecast = new FinalHourlyForecast();
			List<VilageFcstItem> hourlyFcstItems = entry.getValue();
			fcstDate = hourlyFcstItems.get(0).getFcstDate();
			
			now = now.withYear(Integer.parseInt(fcstDate.substring(0, 4))).withMonth(
					Integer.parseInt(fcstDate.substring(4, 6))).withDayOfMonth(Integer.parseInt(fcstDate.substring(6, 8))).withHour(
					Integer.parseInt(hourlyFcstItems.get(0).getFcstTime().substring(0, 2).substring(0, 2)));
			
			finalHourlyForecast.setFcstDateTime(now.toLocalDateTime());
			
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
	
	public static List<FinalDailyForecast> getFinalDailyForecastList(MidLandFcstRoot midLandFcstRoot, MidTaRoot midTaRoot, Long tmFc) {
		//중기예보 데이터 생성 3~10일후
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddhhmm");
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(TimeZone.getTimeZone("Asia/Seoul").getID()));
		
		try {
			now = ZonedDateTime.parse(tmFc.toString(), dateTimeFormatter);
		} catch (Exception e) {
		
		}
		//3일 후로 이동
		now = now.plusDays(3).withHour(0).withMinute(0).withSecond(0).withNano(0);
		
		MidLandFcstItem midLandFcstData = midLandFcstRoot.getResponse().getBody().getItems().getItem().get(0);
		MidTaItem midTaFcstData = midTaRoot.getResponse().getBody().getItems().getItem().get(0);
		List<FinalDailyForecast> finalDailyForecastList = new ArrayList<>();
		
		finalDailyForecastList.add(new FinalDailyForecast(now.toLocalDateTime(), midLandFcstData.getWf3Am(), midLandFcstData.getWf3Pm(),
				midLandFcstData.getRnSt3Am(), midLandFcstData.getRnSt3Pm(), midTaFcstData.getTaMin3(), midTaFcstData.getTaMax3()));
		
		now = now.plusDays(1);
		
		finalDailyForecastList.add(new FinalDailyForecast(now.toLocalDateTime(), midLandFcstData.getWf4Am(), midLandFcstData.getWf4Pm(),
				midLandFcstData.getRnSt4Am(), midLandFcstData.getRnSt4Pm(), midTaFcstData.getTaMin4(), midTaFcstData.getTaMax4()));
		
		now = now.plusDays(1);
		
		finalDailyForecastList.add(new FinalDailyForecast(now.toLocalDateTime(), midLandFcstData.getWf5Am(), midLandFcstData.getWf5Pm(),
				midLandFcstData.getRnSt5Am(), midLandFcstData.getRnSt5Pm(), midTaFcstData.getTaMin5(), midTaFcstData.getTaMax5()));
		
		now = now.plusDays(1);
		
		finalDailyForecastList.add(new FinalDailyForecast(now.toLocalDateTime(), midLandFcstData.getWf6Am(), midLandFcstData.getWf6Pm(),
				midLandFcstData.getRnSt6Am(), midLandFcstData.getRnSt6Pm(), midTaFcstData.getTaMin6(), midTaFcstData.getTaMax6()));
		
		now = now.plusDays(1);
		
		finalDailyForecastList.add(new FinalDailyForecast(now.toLocalDateTime(), midLandFcstData.getWf7Am(), midLandFcstData.getWf7Pm(),
				midLandFcstData.getRnSt7Am(), midLandFcstData.getRnSt7Pm(), midTaFcstData.getTaMin7(), midTaFcstData.getTaMax7()));
		
		now = now.plusDays(1);
		
		finalDailyForecastList.add(new FinalDailyForecast(now.toLocalDateTime(), midLandFcstData.getWf8(), midLandFcstData.getRnSt8(),
				midTaFcstData.getTaMin8(), midTaFcstData.getTaMax8()));
		
		now = now.plusDays(1);
		
		finalDailyForecastList.add(new FinalDailyForecast(now.toLocalDateTime(), midLandFcstData.getWf9(), midLandFcstData.getRnSt9(),
				midTaFcstData.getTaMin9(), midTaFcstData.getTaMax9()));
		
		now = now.plusDays(1);
		
		finalDailyForecastList.add(new FinalDailyForecast(now.toLocalDateTime(), midLandFcstData.getWf10(), midLandFcstData.getRnSt10(),
				midTaFcstData.getTaMin10(), midTaFcstData.getTaMax10()));
		
		return finalDailyForecastList;
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
	
}
