package com.lifedawn.bestweather.weathers.simplefragment.aqicn;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class AirQualityForecastObj {
	public LocalDate date;
	public Integer pm10;
	public Integer pm25;
	public Integer o3;

	public AirQualityForecastObj(LocalDate day) {
		date = day;
	}
}
