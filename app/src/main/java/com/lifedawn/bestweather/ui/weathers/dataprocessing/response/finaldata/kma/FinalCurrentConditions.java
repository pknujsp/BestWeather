package com.lifedawn.bestweather.ui.weathers.dataprocessing.response.finaldata.kma;

public class FinalCurrentConditions {
	//nx
	private String nx;
	//ny
	private String ny;
	//기온
	private String temperature;
	//1시간 강수량
	private String precipitation1Hour;
	//습도
	private String humidity;
	//강수형태
	private String precipitationType;
	//풍향
	private String windDirection;
	//풍속
	private String windSpeed;
	private String baseDateTime;

	public String getBaseDateTime() {
		return baseDateTime;
	}

	public FinalCurrentConditions setBaseDateTime(String baseDateTime) {
		this.baseDateTime = baseDateTime;
		return this;
	}

	public String getNx() {
		return nx;
	}

	public FinalCurrentConditions setNx(String nx) {
		this.nx = nx;
		return this;
	}

	public String getNy() {
		return ny;
	}

	public FinalCurrentConditions setNy(String ny) {
		this.ny = ny;
		return this;
	}

	public String getTemperature() {
		return temperature;
	}

	public FinalCurrentConditions setTemperature(String temperature) {
		this.temperature = temperature;
		return this;
	}

	public String getPrecipitation1Hour() {
		return precipitation1Hour;
	}

	public FinalCurrentConditions setPrecipitation1Hour(String precipitation1Hour) {
		this.precipitation1Hour = precipitation1Hour;
		return this;
	}

	public String getHumidity() {
		return humidity;
	}

	public FinalCurrentConditions setHumidity(String humidity) {
		this.humidity = humidity;
		return this;
	}

	public String getPrecipitationType() {
		return precipitationType;
	}

	public FinalCurrentConditions setPrecipitationType(String precipitationType) {
		this.precipitationType = precipitationType;
		return this;
	}

	public String getWindDirection() {
		return windDirection;
	}

	public FinalCurrentConditions setWindDirection(String windDirection) {
		this.windDirection = windDirection;
		return this;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public FinalCurrentConditions setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
		return this;
	}
}
