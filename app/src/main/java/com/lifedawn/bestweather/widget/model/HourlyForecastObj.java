package com.lifedawn.bestweather.widget.model;

public class HourlyForecastObj {
	private String hours;
	private int weatherIcon;
	private String realFeelTemp;
	private String temp;
	private String pop;
	private String pos;
	private String por;
	private String precipitationVolume;
	private String rainVolume;
	private String snowVolume;

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public int getWeatherIcon() {
		return weatherIcon;
	}

	public void setWeatherIcon(int weatherIcon) {
		this.weatherIcon = weatherIcon;

	}

	public String getTemp() {
		return temp;
	}

	public HourlyForecastObj setTemp(String temp) {
		this.temp = temp;
		return this;
	}

	public String getRealFeelTemp() {
		return realFeelTemp;
	}

	public void setRealFeelTemp(String realFeelTemp) {
		this.realFeelTemp = realFeelTemp;
	}

	public String getPop() {
		return pop;
	}

	public void setPop(String pop) {
		this.pop = pop;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getPor() {
		return por;
	}

	public void setPor(String por) {
		this.por = por;
	}

	public String getPrecipitationVolume() {
		return precipitationVolume;
	}

	public void setPrecipitationVolume(String precipitationVolume) {
		this.precipitationVolume = precipitationVolume;
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
}
