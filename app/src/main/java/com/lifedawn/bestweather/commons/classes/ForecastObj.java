package com.lifedawn.bestweather.commons.classes;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class ForecastObj<T> {
	public final ZonedDateTime dateTime;
	public final T e;
	
	public ForecastObj(ZonedDateTime dateTime, T e) {
		this.dateTime = dateTime;
		this.e = e;
	}
}
