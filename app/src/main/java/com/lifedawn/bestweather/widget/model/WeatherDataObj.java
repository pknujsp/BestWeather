package com.lifedawn.bestweather.widget.model;

import java.util.List;

public class WeatherDataObj {
	private boolean isSuccessful;
	private String refreshDateTime;
	private CurrentConditionsObj currentConditionsObj;
	private List<HourlyForecastObj> hourlyForecastObjList;
	private List<DailyForecastObj> dailyForecastObjList;
	private AirQualityObj airQualityObj;

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public void setSuccessful(boolean successful) {
		isSuccessful = successful;
	}

	public String getRefreshDateTime() {
		return refreshDateTime;
	}

	public void setRefreshDateTime(String refreshDateTime) {
		this.refreshDateTime = refreshDateTime;
	}

	public CurrentConditionsObj getCurrentConditionsObj() {
		return currentConditionsObj;
	}

	public void setCurrentConditionsObj(CurrentConditionsObj currentConditionsObj) {
		this.currentConditionsObj = currentConditionsObj;
	}

	public List<HourlyForecastObj> getHourlyForecastObjList() {
		return hourlyForecastObjList;
	}

	public void setHourlyForecastObjList(List<HourlyForecastObj> hourlyForecastObjList) {
		this.hourlyForecastObjList = hourlyForecastObjList;
	}

	public List<DailyForecastObj> getDailyForecastObjList() {
		return dailyForecastObjList;
	}

	public void setDailyForecastObjList(List<DailyForecastObj> dailyForecastObjList) {
		this.dailyForecastObjList = dailyForecastObjList;
	}

	public AirQualityObj getAirQualityObj() {
		return airQualityObj;
	}

	public void setAirQualityObj(AirQualityObj airQualityObj) {
		this.airQualityObj = airQualityObj;
	}
}
