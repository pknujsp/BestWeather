package com.lifedawn.bestweather.commons.classes;

import java.util.Date;

public class ForecastObj<T> {
	public final Date dateTime;
	public final T e;
	
	public ForecastObj(Date dateTime, T e) {
		this.dateTime = dateTime;
		this.e = e;
	}
}
