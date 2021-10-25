package com.lifedawn.bestweather.commons.classes;

import java.time.LocalDateTime;
import java.util.Date;

public class ForecastObj<T> {
	public final LocalDateTime dateTime;
	public final T e;
	
	public ForecastObj(LocalDateTime dateTime, T e) {
		this.dateTime = dateTime;
		this.e = e;
	}
}
