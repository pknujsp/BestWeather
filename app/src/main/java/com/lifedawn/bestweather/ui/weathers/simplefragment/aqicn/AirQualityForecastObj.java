package com.lifedawn.bestweather.ui.weathers.simplefragment.aqicn;

import java.time.LocalDate;

public class AirQualityForecastObj {
	public LocalDate date;
	public Integer pm10;
	public Integer pm25;
	public Integer o3;

	public AirQualityForecastObj(LocalDate day) {
		date = day;
	}
}
