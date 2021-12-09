package com.lifedawn.bestweather.widget.model;

public class DailyForecastObj {

	private String date;
	private boolean isSingle;
	private int amWeatherIcon;
	private int pmWeatherIcon;
	private int singleWeatherIcon;
	private String minTemp;
	private String maxTemp;
	private String minFeelsLikeTemp;
	private String maxFeelsLikeTemp;
	private String amPop;
	private String amPos;
	private String amPor;
	private String pmPop;
	private String pmPos;
	private String pmPor;

	private String singlePop;
	private String singlePos;
	private String singlePor;

	private String precipitationVolume;
	private String rainVolume;
	private String snowVolume;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public boolean isSingle() {
		return isSingle;
	}

	public void setSingle(boolean single) {
		isSingle = single;
	}

	public int getAmWeatherIcon() {
		return amWeatherIcon;
	}

	public void setAmWeatherIcon(int amWeatherIcon) {
		this.amWeatherIcon = amWeatherIcon;
	}

	public int getPmWeatherIcon() {
		return pmWeatherIcon;
	}

	public void setPmWeatherIcon(int pmWeatherIcon) {
		this.pmWeatherIcon = pmWeatherIcon;
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


	public String getMinFeelsLikeTemp() {
		return minFeelsLikeTemp;
	}

	public DailyForecastObj setMinFeelsLikeTemp(String minFeelsLikeTemp) {
		this.minFeelsLikeTemp = minFeelsLikeTemp;
		return this;
	}

	public String getMaxFeelsLikeTemp() {
		return maxFeelsLikeTemp;
	}

	public DailyForecastObj setMaxFeelsLikeTemp(String maxFeelsLikeTemp) {
		this.maxFeelsLikeTemp = maxFeelsLikeTemp;
		return this;
	}

	public String getAmPop() {
		return amPop;
	}

	public void setAmPop(String amPop) {
		this.amPop = amPop;
	}

	public String getAmPos() {
		return amPos;
	}

	public void setAmPos(String amPos) {
		this.amPos = amPos;
	}

	public String getAmPor() {
		return amPor;
	}

	public void setAmPor(String amPor) {
		this.amPor = amPor;
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

	public String getPmPop() {
		return pmPop;
	}

	public DailyForecastObj setPmPop(String pmPop) {
		this.pmPop = pmPop;
		return this;
	}

	public String getPmPos() {
		return pmPos;
	}

	public DailyForecastObj setPmPos(String pmPos) {
		this.pmPos = pmPos;
		return this;
	}

	public String getPmPor() {
		return pmPor;
	}

	public DailyForecastObj setPmPor(String pmPor) {
		this.pmPor = pmPor;
		return this;
	}

	public String getSinglePop() {
		return singlePop;
	}

	public DailyForecastObj setSinglePop(String singlePop) {
		this.singlePop = singlePop;
		return this;
	}

	public String getSinglePos() {
		return singlePos;
	}

	public DailyForecastObj setSinglePos(String singlePos) {
		this.singlePos = singlePos;
		return this;
	}

	public String getSinglePor() {
		return singlePor;
	}

	public DailyForecastObj setSinglePor(String singlePor) {
		this.singlePor = singlePor;
		return this;
	}

	public int getSingleWeatherIcon() {
		return singleWeatherIcon;
	}

	public DailyForecastObj setSingleWeatherIcon(int singleWeatherIcon) {
		this.singleWeatherIcon = singleWeatherIcon;
		return this;
	}
}
