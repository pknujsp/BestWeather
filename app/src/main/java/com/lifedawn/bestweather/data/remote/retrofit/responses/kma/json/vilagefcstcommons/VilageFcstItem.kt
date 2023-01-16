package com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml(name = "item", inheritance = true)
public class VilageFcstItem {
	@Expose
	@SerializedName("baseDate")
	@PropertyElement(name = "baseDate")
	private String baseDate;

	@Expose
	@SerializedName("baseTime")
	@PropertyElement(name = "baseTime")
	private String baseTime;

	@Expose
	@SerializedName("category")
	@PropertyElement(name = "category")
	private String category;

	@Expose
	@SerializedName("fcstDate")
	@PropertyElement(name = "fcstDate")
	private String fcstDate;

	@Expose
	@SerializedName("fcstTime")
	@PropertyElement(name = "fcstTime")
	private String fcstTime;

	@Expose
	@SerializedName("fcstValue")
	@PropertyElement(name = "fcstValue")
	private String fcstValue;

	@Expose
	@SerializedName("obSrValue")
	@PropertyElement(name = "obsrValue")
	private String obsrValue;

	@Expose
	@SerializedName("nx")
	@PropertyElement(name = "nx")
	private String nx;

	@Expose
	@SerializedName("ny")
	@PropertyElement(name = "ny")
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

	public String getObsrValue() {
		return obsrValue;
	}

	public VilageFcstItem setObsrValue(String obsrValue) {
		this.obsrValue = obsrValue;
		return this;
	}
}
