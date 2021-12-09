package com.lifedawn.bestweather.widget.model;

public class CurrentConditionsObj {
	private boolean isSuccessful;
	private String timeZoneId;
	private int weatherIcon;
	private String temp;
	private String realFeelTemp;
	private String precipitationVolume;
	private String precipitationType;
	private String windStrength;
	private String windSpeed;
	private String windDirection;
	private String rainVolume;
	private String snowVolume;
	private String humidity;
	private String uvIndex;
	private String cloudiness;
	private String visibility;
	private String pressure;

	public int getWeatherIcon() {
		return weatherIcon;
	}

	public void setWeatherIcon(int weatherIcon) {
		this.weatherIcon = weatherIcon;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getRealFeelTemp() {
		return realFeelTemp;
	}

	public void setRealFeelTemp(String realFeelTemp) {
		this.realFeelTemp = realFeelTemp;
	}

	public String getPrecipitationVolume() {
		return precipitationVolume;
	}

	public void setPrecipitationVolume(String precipitationVolume) {
		this.precipitationVolume = precipitationVolume;
	}

	public String getPrecipitationType() {
		return precipitationType;
	}

	public void setPrecipitationType(String precipitationType) {
		this.precipitationType = precipitationType;
	}

	public String getWindStrength() {
		return windStrength;
	}

	public void setWindStrength(String windStrength) {
		this.windStrength = windStrength;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
	}

	public String getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(String windDirection) {
		this.windDirection = windDirection;
	}

	public String getRainVolume() {
		return rainVolume;
	}

	public void setRainVolume(String rainVolume) {
		this.rainVolume = rainVolume;
	}

	public String getSnowVolume() {
		return snowVolume;
	}

	public void setSnowVolume(String snowVolume) {
		this.snowVolume = snowVolume;
	}

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	public String getUvIndex() {
		return uvIndex;
	}

	public void setUvIndex(String uvIndex) {
		this.uvIndex = uvIndex;
	}

	public String getCloudiness() {
		return cloudiness;
	}

	public void setCloudiness(String cloudiness) {
		this.cloudiness = cloudiness;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getPressure() {
		return pressure;
	}

	public void setPressure(String pressure) {
		this.pressure = pressure;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public CurrentConditionsObj setSuccessful(boolean successful) {
		isSuccessful = successful;
		return this;
	}

	public String getTimeZoneId() {
		return timeZoneId;
	}

	public CurrentConditionsObj setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
		return this;
	}
}
