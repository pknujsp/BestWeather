package com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma;

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
	
	public String getNx() {
		return nx;
	}
	
	public FinalCurrentConditions setNx(String nx) {
		this.nx = nx;
		return this;
	}
}
