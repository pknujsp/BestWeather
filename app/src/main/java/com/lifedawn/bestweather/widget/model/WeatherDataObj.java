package com.lifedawn.bestweather.widget.model;

import com.lifedawn.bestweather.weathers.models.AirQualityDto;
import com.lifedawn.bestweather.weathers.models.CurrentConditionsDto;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import java.util.List;

public class WeatherDataObj {
	private boolean isSuccessful;
	private String refreshDateTime;
	private CurrentConditionsDto currentConditionsDto;
	private List<HourlyForecastDto> hourlyForecastDtoList;
	private List<DailyForecastDto> dailyForecastDtoList;
	private AirQualityDto airQualityDto;

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public WeatherDataObj setSuccessful(boolean successful) {
		isSuccessful = successful;
		return this;
	}

	public String getRefreshDateTime() {
		return refreshDateTime;
	}

	public WeatherDataObj setRefreshDateTime(String refreshDateTime) {
		this.refreshDateTime = refreshDateTime;
		return this;
	}

	public CurrentConditionsDto getCurrentConditionsDto() {
		return currentConditionsDto;
	}

	public WeatherDataObj setCurrentConditionsDto(CurrentConditionsDto currentConditionsDto) {
		this.currentConditionsDto = currentConditionsDto;
		return this;
	}

	public List<HourlyForecastDto> getHourlyForecastDtoList() {
		return hourlyForecastDtoList;
	}

	public WeatherDataObj setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
		this.hourlyForecastDtoList = hourlyForecastDtoList;
		return this;
	}

	public List<DailyForecastDto> getDailyForecastDtoList() {
		return dailyForecastDtoList;
	}

	public WeatherDataObj setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		this.dailyForecastDtoList = dailyForecastDtoList;
		return this;
	}

	public AirQualityDto getAirQualityDto() {
		return airQualityDto;
	}

	public WeatherDataObj setAirQualityDto(AirQualityDto airQualityDto) {
		this.airQualityDto = airQualityDto;
		return this;
	}
}
