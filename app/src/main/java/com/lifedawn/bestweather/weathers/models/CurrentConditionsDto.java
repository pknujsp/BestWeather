package com.lifedawn.bestweather.weathers.models;

import java.time.ZonedDateTime;

public class CurrentConditionsDto {
	private String temp;
	private String minTemp;
	private String maxTemp;
	private String feelsLikeTemp;
	private int weatherIcon;
	private String weatherDescription;
	private String humidity;
	private String dewPoint;
	private String windDirection;
	private int windDirectionDegree;
	private String windSpeed;
	private String simpleWindStrength;
	private String windStrength;
	private String windGust;
	private String pressure;
	private String uvIndex;
	private String visibility;
	private String cloudiness;
	private String rainVolume;
	private String snowVolume;
	private String precipitationVolume;
	private String precipitationType;
	private ZonedDateTime currentTime;

	private boolean hasRainVolume;
	private boolean hasSnowVolume;
	private boolean hasPrecipitationVolume;

	public CurrentConditionsDto setCurrentTime(ZonedDateTime currentTime) {
		this.currentTime = currentTime;
		return this;
	}

	public ZonedDateTime getCurrentTime() {
		return currentTime;
	}

	public String getTemp() {
		return temp;
	}

	public CurrentConditionsDto setTemp(String temp) {
		this.temp = temp;
		return this;
	}

	public String getMinTemp() {
		return minTemp;
	}

	public void setMinTemp(String minTemp) {
		this.minTemp = minTemp;
	}

	public String getMaxTemp() {
		return maxTemp;
	}

	public void setMaxTemp(String maxTemp) {
		this.maxTemp = maxTemp;
	}

	public String getFeelsLikeTemp() {
		return feelsLikeTemp;
	}

	public CurrentConditionsDto setFeelsLikeTemp(String feelsLikeTemp) {
		this.feelsLikeTemp = feelsLikeTemp;
		return this;
	}

	public int getWeatherIcon() {
		return weatherIcon;
	}

	public CurrentConditionsDto setWeatherIcon(int weatherIcon) {
		this.weatherIcon = weatherIcon;
		return this;
	}

	public String getWindStrength() {
		return windStrength;
	}

	public void setWindStrength(String windStrength) {
		this.windStrength = windStrength;
	}

	public String getWeatherDescription() {
		return weatherDescription;
	}

	public CurrentConditionsDto setWeatherDescription(String weatherDescription) {
		this.weatherDescription = weatherDescription;
		return this;
	}

	public String getHumidity() {
		return humidity;
	}

	public CurrentConditionsDto setHumidity(String humidity) {
		this.humidity = humidity;
		return this;
	}

	public String getDewPoint() {
		return dewPoint;
	}

	public CurrentConditionsDto setDewPoint(String dewPoint) {
		this.dewPoint = dewPoint;
		return this;
	}

	public String getWindDirection() {
		return windDirection;
	}

	public CurrentConditionsDto setWindDirection(String windDirection) {
		this.windDirection = windDirection;
		return this;
	}

	public int getWindDirectionDegree() {
		return windDirectionDegree;
	}

	public CurrentConditionsDto setWindDirectionDegree(int windDirectionDegree) {
		this.windDirectionDegree = windDirectionDegree;
		return this;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public CurrentConditionsDto setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
		return this;
	}

	public String getSimpleWindStrength() {
		return simpleWindStrength;
	}

	public CurrentConditionsDto setSimpleWindStrength(String simpleWindStrength) {
		this.simpleWindStrength = simpleWindStrength;
		return this;
	}

	public String getWindGust() {
		return windGust;
	}

	public CurrentConditionsDto setWindGust(String windGust) {
		this.windGust = windGust;
		return this;
	}

	public String getPressure() {
		return pressure;
	}

	public CurrentConditionsDto setPressure(String pressure) {
		this.pressure = pressure;
		return this;
	}

	public String getUvIndex() {
		return uvIndex;
	}

	public CurrentConditionsDto setUvIndex(String uvIndex) {
		this.uvIndex = uvIndex;
		return this;
	}

	public String getVisibility() {
		return visibility;
	}

	public CurrentConditionsDto setVisibility(String visibility) {
		this.visibility = visibility;
		return this;
	}

	public String getCloudiness() {
		return cloudiness;
	}

	public CurrentConditionsDto setCloudiness(String cloudiness) {
		this.cloudiness = cloudiness;
		return this;
	}

	public String getRainVolume() {
		return rainVolume;
	}

	public CurrentConditionsDto setRainVolume(String rainVolume) {
		this.rainVolume = rainVolume;
		hasRainVolume = true;
		return this;
	}

	public String getSnowVolume() {
		return snowVolume;
	}

	public CurrentConditionsDto setSnowVolume(String snowVolume) {
		this.snowVolume = snowVolume;
		hasSnowVolume = true;
		return this;
	}

	public String getPrecipitationVolume() {
		return precipitationVolume;
	}

	public CurrentConditionsDto setPrecipitationVolume(String precipitationVolume) {
		this.precipitationVolume = precipitationVolume;
		hasPrecipitationVolume = true;
		return this;
	}

	public String getPrecipitationType() {
		return precipitationType;
	}

	public CurrentConditionsDto setPrecipitationType(String precipitationType) {
		this.precipitationType = precipitationType;
		return this;
	}

	public boolean isHasRainVolume() {
		return hasRainVolume;
	}

	public CurrentConditionsDto setHasRainVolume(boolean hasRainVolume) {
		this.hasRainVolume = hasRainVolume;
		return this;
	}

	public boolean isHasSnowVolume() {
		return hasSnowVolume;
	}

	public CurrentConditionsDto setHasSnowVolume(boolean hasSnowVolume) {
		this.hasSnowVolume = hasSnowVolume;
		return this;
	}

	public boolean isHasPrecipitationVolume() {
		return hasPrecipitationVolume;
	}

	public CurrentConditionsDto setHasPrecipitationVolume(boolean hasPrecipitationVolume) {
		this.hasPrecipitationVolume = hasPrecipitationVolume;
		return this;
	}
}
