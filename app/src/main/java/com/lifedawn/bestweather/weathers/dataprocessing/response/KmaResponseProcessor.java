package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse.UltraSrtFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse.UltraSrtNcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons.VilageFcstItem;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons.VilageFcstItems;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KmaResponseProcessor {
	private static final Map<String, String> WEATHER_SKY_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> WEATHER_PTY_ICON_DESCRIPTION_MAP = new HashMap<>();
	
	private static final Map<String, Drawable> WEATHER_SKY_ICON_IMG_MAP = new HashMap<>();
	private static final Map<String, Drawable> WEATHER_PTY_ICON_IMG_MAP = new HashMap<>();
	
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
		
		WEATHER_SKY_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < skyCodes.length; i++) {
			WEATHER_SKY_ICON_DESCRIPTION_MAP.put(skyCodes[i], skyDescriptions[i]);
		}
		
		WEATHER_PTY_ICON_DESCRIPTION_MAP.clear();
		for (int i = 0; i < ptyCodes.length; i++) {
			WEATHER_PTY_ICON_DESCRIPTION_MAP.put(ptyCodes[i], ptyDescriptions[i]);
		}
		
	}
	
	public static Drawable getWeatherSkyIconImg(String code) {
		return null;
	}
	
	public static String getWeatherSkyIconDescription(String code) {
		return WEATHER_SKY_ICON_DESCRIPTION_MAP.get(code);
	}
	
	public static Drawable getWeatherPtyIconImg(String code) {
		return null;
	}
	
	public static String getWeatherPtyIconDescription(String code) {
		return WEATHER_SKY_ICON_DESCRIPTION_MAP.get(code);
	}
	
	public static FinalCurrentConditions getFinalCurrentConditions(UltraSrtNcstRoot ultraSrtNcstRoot) {
		FinalCurrentConditions finalCurrentConditions = new FinalCurrentConditions();
		VilageFcstItems ultraSrtNcstItems = ultraSrtNcstRoot.getResponse().getBody().getItems();
		List<VilageFcstItem> items = ultraSrtNcstItems.getItem();
		
		final String T1H = "T1H";
		final String RN1 = "RN1";
		final String REH = "REH";
		final String PTY = "PTY";
		final String VEC = "VEC";
		final String WSD = "WSD";
		
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
		Map<Long, List<VilageFcstItem>> hourDataListMap = new HashMap();
		
		long newDateTime = 0L;
		long lastDateTime = 0L;
		
		//데이터를 날짜별로 분류해서 map에 저장
		for (VilageFcstItem item : ultraSrtFcstItemList) {
			newDateTime = Long.parseLong(new String(item.getFcstDate() + item.getFcstTime()));
			if (newDateTime > lastDateTime) {
				hourDataListMap.put(newDateTime, new ArrayList<>());
				lastDateTime = newDateTime;
			}
			hourDataListMap.get(newDateTime).add(item);
		}
		
		final long lastDateTimeLongOfUltraSrtFcst = lastDateTime;
		
		for (VilageFcstItem item : vilageItemList) {
			newDateTime = Long.parseLong(new String(item.getFcstDate() + item.getFcstTime()));
			
			if (newDateTime > lastDateTimeLongOfUltraSrtFcst) {
				if (newDateTime > lastDateTime) {
					hourDataListMap.put(newDateTime, new ArrayList<>());
					lastDateTime = newDateTime;
				}
				hourDataListMap.get(newDateTime).add(item);
			}
		}
		
		List<FinalHourlyForecast> finalHourlyForecastList = new ArrayList<>();
		
		for (Map.Entry<Long, List<VilageFcstItem>> entry : hourDataListMap.entrySet()) {
			FinalHourlyForecast finalHourlyForecast = new FinalHourlyForecast();
			List<VilageFcstItem> hourlyFcstItems = entry.getValue();
			String date = hourlyFcstItems.get(0).getFcstDate();
			String time = hourlyFcstItems.get(0).getFcstTime().substring(0, 2);
			
			int year = Integer.parseInt(date.substring(0, 4));
			int month = Integer.parseInt(date.substring(4, 6));
			int day = Integer.parseInt(date.substring(6, 8));
			int hour = Integer.parseInt(time.substring(0, 2));
			
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month - 1, day, hour, 0, 0);
			
			finalHourlyForecast.setFcstDateTime(calendar.getTime());
			
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
}
