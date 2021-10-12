package com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma;

public class FinalDailyForecast {
	private String date;
	private String amSky;
	private String pmSky;
	private String sky;
	private String amProbabilityOfPrecipitation;
	private String pmProbabilityOfPrecipitation;
	private String probabilityOfPrecipitation;
	private String minTemp;
	private String maxTemp;
	
	public String getDate() {
		return date;
	}
	
	public FinalDailyForecast setDate(String date) {
		this.date = date;
		return this;
	}
	
	public String getAmSky() {
		return amSky;
	}
	
	public FinalDailyForecast setAmSky(String amSky) {
		this.amSky = amSky;
		return this;
	}
	
	public String getPmSky() {
		return pmSky;
	}
	
	public FinalDailyForecast setPmSky(String pmSky) {
		this.pmSky = pmSky;
		return this;
	}
	
	public String getSky() {
		return sky;
	}
	
	public FinalDailyForecast setSky(String sky) {
		this.sky = sky;
		return this;
	}
	
	public String getAmProbabilityOfPrecipitation() {
		return amProbabilityOfPrecipitation;
	}
	
	public FinalDailyForecast setAmProbabilityOfPrecipitation(String amProbabilityOfPrecipitation) {
		this.amProbabilityOfPrecipitation = amProbabilityOfPrecipitation;
		return this;
	}
	
	public String getPmProbabilityOfPrecipitation() {
		return pmProbabilityOfPrecipitation;
	}
	
	public FinalDailyForecast setPmProbabilityOfPrecipitation(String pmProbabilityOfPrecipitation) {
		this.pmProbabilityOfPrecipitation = pmProbabilityOfPrecipitation;
		return this;
	}
	
	public String getProbabilityOfPrecipitation() {
		return probabilityOfPrecipitation;
	}
	
	public FinalDailyForecast setProbabilityOfPrecipitation(String probabilityOfPrecipitation) {
		this.probabilityOfPrecipitation = probabilityOfPrecipitation;
		return this;
	}
	
	public String getMinTemp() {
		return minTemp;
	}
	
	public FinalDailyForecast setMinTemp(String minTemp) {
		this.minTemp = minTemp;
		return this;
	}
	
	public String getMaxTemp() {
		return maxTemp;
	}
	
	public FinalDailyForecast setMaxTemp(String maxTemp) {
		this.maxTemp = maxTemp;
		return this;
	}
}
