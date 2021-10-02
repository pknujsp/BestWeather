package com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstcommons;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VilageFcstItem {
	@Expose
	@SerializedName("baseDate")
	private String baseDate;
	
	@Expose
	@SerializedName("baseTime")
	private String baseTime;
	
	@Expose
	@SerializedName("category")
	private String category;
	
	@Expose
	@SerializedName("fcstDate")
	private String fcstDate;
	
	@Expose
	@SerializedName("fcstTime")
	private String fcstTime;
	
	@Expose
	@SerializedName("fcstValue")
	private String fcstValue;
	
	@Expose
	@SerializedName("nx")
	private String nx;
	
	@Expose
	@SerializedName("ny")
	private String ny;
	
	public String getBaseDate() {
		return baseDate;
	}
	
	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}
	
	public String getBaseTime() {
		return baseTime;
	}
	
	public void setBaseTime(String baseTime) {
		this.baseTime = baseTime;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getFcstDate() {
		return fcstDate;
	}
	
	public void setFcstDate(String fcstDate) {
		this.fcstDate = fcstDate;
	}
	
	public String getFcstTime() {
		return fcstTime;
	}
	
	public void setFcstTime(String fcstTime) {
		this.fcstTime = fcstTime;
	}
	
	public String getFcstValue() {
		return fcstValue;
	}
	
	public void setFcstValue(String fcstValue) {
		this.fcstValue = fcstValue;
	}
	
	public String getNx() {
		return nx;
	}
	
	public void setNx(String nx) {
		this.nx = nx;
	}
	
	public String getNy() {
		return ny;
	}
	
	public void setNy(String ny) {
		this.ny = ny;
	}
}
