package com.lifedawn.bestweather.weathers.simplefragment.aqicn;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AirQualityForecastObj {
	public Date date;
	public String pm10Str;
	public String pm25Str;
	public String o3Str;
	public String pm10;
	public String pm25;
	public String o3;

	public AirQualityForecastObj(Date day) {
		date = day;
	}
}
