package com.lifedawn.bestweather.weathers.models;

import java.time.ZonedDateTime;

public class HourlyForecastDto {
	private ZonedDateTime hours;
	private int weatherIcon;
	private String weatherDescription;
	private String feelsLikeTemp;
	private String temp;
	private String pop;
	private String pos;
	private String por;
	private String windDirection;
	private int windDirectionVal;
	private String windSpeed;
	private String windStrength;
	private String windGust;
	private String pressure;
	private String humidity;
	private String dewPointTemp;
	private String cloudiness;
	private String visibility;
	private String uvIndex;
	private String precipitationVolume;
	private String rainVolume;
	private String snowVolume;
	private String precipitationType;
	private int precipitationTypeIcon;

	private boolean hasPrecipitation;
	private boolean hasRain;
	private boolean hasSnow;

	public boolean isHasPrecipitation() {
		return hasPrecipitation;
	}

	public HourlyForecastDto setHasPrecipitation(boolean hasPrecipitation) {
		this.hasPrecipitation = hasPrecipitation;
		return this;
	}

	public boolean isHasRain() {
		return hasRain;
	}

	public HourlyForecastDto setHasRain(boolean hasRain) {
		this.hasRain = hasRain;
		return this;
	}

	public boolean isHasSnow() {
		return hasSnow;
	}

	public HourlyForecastDto setHasSnow(boolean hasSnow) {
		this.hasSnow = hasSnow;
		return this;
	}

	public ZonedDateTime getHours() {
		return hours;
	}

	public HourlyForecastDto setHours(ZonedDateTime hours) {
		this.hours = hours;
		return this;
	}

	public int getWeatherIcon() {
		return weatherIcon;
	}

	public HourlyForecastDto setWeatherIcon(int weatherIcon) {
		this.weatherIcon = weatherIcon;
		return this;
	}

	public String getWeatherDescription() {
		return weatherDescription;
	}

	public HourlyForecastDto setWeatherDescription(String weatherDescription) {
		this.weatherDescription = weatherDescription;
		return this;
	}

	public String getFeelsLikeTemp() {
		return feelsLikeTemp;
	}

	public HourlyForecastDto setFeelsLikeTemp(String feelsLikeTemp) {
		this.feelsLikeTemp = feelsLikeTemp;
		return this;
	}

	public String getTemp() {
		return temp;
	}

	public HourlyForecastDto setTemp(String temp) {
		this.temp = temp;
		return this;
	}

	public String getPop() {
		return pop;
	}

	public HourlyForecastDto setPop(String pop) {
		this.pop = pop;
		return this;
	}

	public String getPos() {
		return pos;
	}

	public HourlyForecastDto setPos(String pos) {
		this.pos = pos;
		return this;
	}

	public String getPor() {
		return por;
	}

	public HourlyForecastDto setPor(String por) {
		this.por = por;
		return this;
	}

	public String getWindDirection() {
		return windDirection;
	}

	public HourlyForecastDto setWindDirection(String windDirection) {
		this.windDirection = windDirection;
		return this;
	}

	public int getWindDirectionVal() {
		return windDirectionVal;
	}

	public HourlyForecastDto setWindDirectionVal(int windDirectionVal) {
		this.windDirectionVal = windDirectionVal;
		return this;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public HourlyForecastDto setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
		return this;
	}

	public String getWindStrength() {
		return windStrength;
	}

	public HourlyForecastDto setWindStrength(String windStrength) {
		this.windStrength = windStrength;
		return this;
	}

	public String getWindGust() {
		return windGust;
	}

	public HourlyForecastDto setWindGust(String windGust) {
		this.windGust = windGust;
		return this;
	}

	public String getPressure() {
		return pressure;
	}

	public HourlyForecastDto setPressure(String pressure) {
		this.pressure = pressure;
		return this;
	}

	public String getHumidity() {
		return humidity;
	}

	public HourlyForecastDto setHumidity(String humidity) {
		this.humidity = humidity;
		return this;
	}

	public String getDewPointTemp() {
		return dewPointTemp;
	}

	public HourlyForecastDto setDewPointTemp(String dewPointTemp) {
		this.dewPointTemp = dewPointTemp;
		return this;
	}

	public String getCloudiness() {
		return cloudiness;
	}

	public HourlyForecastDto setCloudiness(String cloudiness) {
		this.cloudiness = cloudiness;
		return this;
	}

	public String getVisibility() {
		return visibility;
	}

	public HourlyForecastDto setVisibility(String visibility) {
		this.visibility = visibility;
		return this;
	}

	public String getUvIndex() {
		return uvIndex;
	}

	public HourlyForecastDto setUvIndex(String uvIndex) {
		this.uvIndex = uvIndex;
		return this;
	}

	public String getPrecipitationVolume() {
		return precipitationVolume;
	}

	public HourlyForecastDto setPrecipitationVolume(String precipitationVolume) {
		this.precipitationVolume = precipitationVolume;
		return this;
	}

	public String getRainVolume() {
		return rainVolume;
	}

	public HourlyForecastDto setRainVolume(String rainVolume) {
		this.rainVolume = rainVolume;
		return this;
	}

	public String getSnowVolume() {
		return snowVolume;
	}

	public HourlyForecastDto setSnowVolume(String snowVolume) {
		this.snowVolume = snowVolume;
		return this;
	}

	public HourlyForecastDto setPrecipitationType(String precipitationType) {
		this.precipitationType = precipitationType;
		return this;
	}

	public String getPrecipitationType() {
		return precipitationType;
	}

	public HourlyForecastDto setPrecipitationTypeIcon(int precipitationTypeIcon) {
		this.precipitationTypeIcon = precipitationTypeIcon;
		return this;
	}

	public int getPrecipitationTypeIcon() {
		return precipitationTypeIcon;
	}
}
