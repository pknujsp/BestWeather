package com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class FinalHourlyForecast {
	
	private ZonedDateTime fcstDateTime;
	//nx
	private String nx;
	//ny
	private String ny;
	//강수확률 POP
	private String probabilityOfPrecipitation;
	//강수형태 PTY
	private String precipitationType;
	//1시간 강수량 PCP, RN1
	private String rainPrecipitation1Hour;
	//습도 REH
	private String humidity;
	//1시간 신적설 SNO
	private String snowPrecipitation1Hour;
	//구름상태 SKY
	private String sky;
	//1시간 기온 TMP, T1H
	private String temp1Hour;
	//최저기온 TMN
	private String minTemp;
	//최고기온 TMX
	private String maxTemp;
	//풍향 VEC
	private String windDirection;
	//풍속 WSD
	private String windSpeed;
	//낙뢰 LGT
	private String lightning;
	
	public FinalHourlyForecast setFcstDateTime(ZonedDateTime fcstDateTime) {
		this.fcstDateTime = fcstDateTime;
		return this;
	}
	
	public ZonedDateTime getFcstDateTime() {
		return fcstDateTime;
	}
	
	public String getNx() {
		return nx;
	}
	
	public FinalHourlyForecast setNx(String nx) {
		this.nx = nx;
		return this;
	}
	
	public String getNy() {
		return ny;
	}
	
	public FinalHourlyForecast setNy(String ny) {
		this.ny = ny;
		return this;
	}
	
	public String getProbabilityOfPrecipitation() {
		return probabilityOfPrecipitation;
	}
	
	public FinalHourlyForecast setProbabilityOfPrecipitation(String probabilityOfPrecipitation) {
		this.probabilityOfPrecipitation = probabilityOfPrecipitation;
		return this;
	}
	
	public String getPrecipitationType() {
		return precipitationType;
	}
	
	public FinalHourlyForecast setPrecipitationType(String precipitationType) {
		this.precipitationType = precipitationType;
		return this;
	}
	
	public String getRainPrecipitation1Hour() {
		return rainPrecipitation1Hour;
	}
	
	public FinalHourlyForecast setRainPrecipitation1Hour(String rainPrecipitation1Hour) {
		this.rainPrecipitation1Hour = rainPrecipitation1Hour;
		return this;
	}
	
	public String getHumidity() {
		return humidity;
	}
	
	public FinalHourlyForecast setHumidity(String humidity) {
		this.humidity = humidity;
		return this;
	}
	
	public String getSnowPrecipitation1Hour() {
		return snowPrecipitation1Hour;
	}
	
	public FinalHourlyForecast setSnowPrecipitation1Hour(String snowPrecipitation1Hour) {
		this.snowPrecipitation1Hour = snowPrecipitation1Hour;
		return this;
	}
	
	public String getSky() {
		return sky;
	}
	
	public FinalHourlyForecast setSky(String sky) {
		this.sky = sky;
		return this;
	}
	
	public String getTemp1Hour() {
		return temp1Hour;
	}
	
	public FinalHourlyForecast setTemp1Hour(String temp1Hour) {
		this.temp1Hour = temp1Hour;
		return this;
	}
	
	public String getMinTemp() {
		return minTemp;
	}
	
	public FinalHourlyForecast setMinTemp(String minTemp) {
		this.minTemp = minTemp;
		return this;
	}
	
	public String getMaxTemp() {
		return maxTemp;
	}
	
	public FinalHourlyForecast setMaxTemp(String maxTemp) {
		this.maxTemp = maxTemp;
		return this;
	}
	
	public String getWindDirection() {
		return windDirection;
	}
	
	public FinalHourlyForecast setWindDirection(String windDirection) {
		this.windDirection = windDirection;
		return this;
	}
	
	public String getWindSpeed() {
		return windSpeed;
	}
	
	public FinalHourlyForecast setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
		return this;
	}
	
	public String getLightning() {
		return lightning;
	}
	
	public FinalHourlyForecast setLightning(String lightning) {
		this.lightning = lightning;
		return this;
	}
}
